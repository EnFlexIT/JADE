/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.core;

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.util.leap.Collection;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.Set;

import jade.core.behaviours.Behaviour;

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;
import jade.core.mobility.AgentMobilityHelper;

import jade.domain.ams;
import jade.domain.df;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.NotRegistered;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;

import jade.security.JADESecurityException;
import jade.security.JADECertificate;
import jade.security.DelegationCertificate;
import jade.security.PrivilegedExceptionAction;
import jade.security.JADEPrincipal;
import jade.security.Credentials;
import jade.security.dummy.DummyPrincipal;


/**
   This class is a concrete implementation of the JADE main container,
   providing runtime support to JADE agents, and the special, front
   end container where the AMS and the Default DF can run.
   This class cannot be instantiated from applications. Instead, the
   <code>Runtime.createMainContainer(Profile p)</code> method must be
   called.

   @see Runtime#createMainContainer(Profile p);

   @author Giovanni Rimassa - Universita' di Parma
   @version $Date$ $Revision$

*/
public class MainContainerImpl implements MainContainer, AgentManager {

    // The two mandatory system agents.
    private ams theAMS;
    private df defaultDF;

    private ContainerID localContainerID;
    private IMTPManager myIMTPManager;
    private PlatformManager myPlatformManager;

    // FIXME: Temporary Hack
    private CommandProcessor myCommandProcessor;

    private List platformListeners = new LinkedList();
    private List platformAddresses = new LinkedList();
    private List agentTools = new LinkedList();

    private ContainerTable containers = new ContainerTable();
    private GADT platformAgents = new GADT();
  
    private Profile myProfile;

    public MainContainerImpl(Profile p, PlatformManager pm) throws ProfileException {
	myProfile = p;
	myIMTPManager = p.getIMTPManager();
	myCommandProcessor = p.getCommandProcessor();
	myPlatformManager = pm;
    }
	
    public PlatformManager getPlatformManager() {
    	return myPlatformManager;
    }
    
    public void addLocalContainer(NodeDescriptor desc) throws IMTPException, JADESecurityException {

	Node node = desc.getNode();
	ContainerID cid = desc.getContainer();
	containers.addContainer(cid, node, desc.getOwnerPrincipal(), desc.getOwnerCredentials()); // GVFIXME
 	localContainerID = cid;

    }

    public void removeLocalContainer(ContainerID cid) throws IMTPException {

	// Stop the Default DF
	Agent systemAgent = defaultDF;
	systemAgent.doDelete();
	systemAgent.join();
	systemAgent.resetToolkit();

	// Stop the AMS
	systemAgent = theAMS;
	systemAgent.doDelete();
	systemAgent.join();
	systemAgent.resetToolkit();
	removeListener(theAMS);

    }

    public void addRemoteContainer(NodeDescriptor desc) throws JADESecurityException {

	Node node = desc.getNode();
	ContainerID cid = desc.getContainer();
	containers.addContainer(cid, node, null, null); // GVFIXME
	ContainerID[] allContainers = containers.names();

	// Notify listeners
	fireAddedContainer(cid);

    }

    public void removeRemoteContainer(NodeDescriptor desc) {
	Node toRemove = desc.getNode();

	// Find the container ID corresponding to the given node
	ContainerID[] cids = containers.names();
	for(int i = 0; i < cids.length; i++) {
	    try {
		Node n = containers.getContainerNode(cids[i]);
		if(toRemove.getName().equals(n.getName())) {
		    removeRemoteContainer(cids[i]);
		    return;
		}
	    }
	    catch(NotFoundException nfe) {
		// Just ignore it: some other container was removed in the meanwhile...
	    }
	}
    }

    private void removeRemoteContainer(ContainerID cid) {

	// Eradicate all MTPs installed on the dead container (this
	// requires that the container is still present in the
	// Container Table)
	removeAllMTPs(cid);

	containers.removeContainer(cid);

	// Eradicate all the entries for agents living on the dead container
	removeAllAgents(cid);

	// Notify listeners
	fireRemovedContainer(cid);
    }

    void initSystemAgents(AgentContainer ac, boolean startThem) throws IMTPException, NotFoundException, JADESecurityException {
	ContainerID cid = ac.getID();
	
	// Start the AMS
	theAMS = new ams(this);

	// Notify the AMS about the main container existence
	fireAddedContainer(cid);
	// FIXME: To be removed
	fireChangedContainerPrincipal(cid, null, null);

	// Start the Default DF
	defaultDF = new df();

	if(startThem) {
	    startSystemAgents(ac);
	}

    }

    // Start the AMS and the Default DF
    void startSystemAgents(AgentContainer ac) throws IMTPException, NotFoundException, JADESecurityException {
	ContainerID cid = ac.getID();
	JADEPrincipal cp = containers.getPrincipal(cid);
	Credentials cr = containers.getCredentials(cid);

	try {
	    theAMS.resetEvents(true);
	    AID amsId = ac.getAMS();
	    ac.initAgent(amsId, theAMS, cp, null); 
	    ac.powerUpLocalAgent(amsId);
	    theAMS.waitUntilStarted();
	}
	catch(Exception e) {
	    e.printStackTrace();
	    throw new IMTPException("Exception during AMS startup", e);
	}
 
	try {
	    AID dfId = ac.getDefaultDF();
	    ac.initAgent(dfId, defaultDF, cp, null);
	    ac.powerUpLocalAgent(dfId);
	    defaultDF.waitUntilStarted();
	}
	catch(Exception e) {
	    throw new IMTPException("Exception during Default DF startup", e);
	}

    }

    void installAMSBehaviour(Behaviour b) {
	theAMS.addBehaviour(b);
    }

    void uninstallAMSBehaviour(Behaviour b) {
	theAMS.removeBehaviour(b);
    }

  /**
     Notify the platform that an agent has just born on a container
   */
  public void bornAgent(AID name, ContainerID cid, JADEPrincipal principal, String ownership, boolean forceReplacement) throws NameClashException, NotFoundException {
    AgentDescriptor ad = new AgentDescriptor(AgentDescriptor.NATIVE_AGENT);
    ad.setContainerID(cid);
    ad.setPrincipal(principal);
    // Registration to the With Pages service
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setOwnership(ownership);
    amsd.setState(AMSAgentDescription.ACTIVE);
    ad.setDescription(amsd);
    
    AgentDescriptor old = platformAgents.put(name, ad);
    // exception unless the old agent's container is dead.
    if(old != null) {
    	// There's already an agent with name 'name'
      if (old.isNative()) {
      	// The agent lives in the platform. Make sure it is reachable 
      	// and then restore it and throw an Exception 
	if(forceReplacement) {
	    System.out.println("Replacing a dead agent ...");
	    fireDeadAgent(old.getContainerID(), name);
	}
	else {
	    platformAgents.put(name, old);
	    throw new NameClashException("Agent " + name + " already present in the platform ");
	}
      }
      else {
	  // The agent lives outside the platform. Can't check whether it is 
	  // reachable. Restore it and throw an Exception
	  platformAgents.put(name, old);
	  throw new NameClashException("Agent " + name + " already registered to the platform ");
      }
    }

    // Notify listeners
    fireBornAgent(cid, name, ownership);
  }
  
  /**
     Notify the platform that an agent has just died
   */
  public void deadAgent(AID name) throws NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(name);
    if(ad == null)
      throw new NotFoundException("DeadAgent failed to find " + name);
    ContainerID cid = ad.getContainerID();
    platformAgents.remove(name);

    // Notify listeners
    fireDeadAgent(cid, name);
  }

  /**
     Notify the platform that an agent has just suspended
   */
  public void suspendedAgent(AID name) throws NotFoundException {
  	AgentDescriptor ad = platformAgents.acquire(name);
    if (ad == null)
      throw new NotFoundException("SuspendedAgent failed to find " + name);
    AMSAgentDescription amsd = ad.getDescription();
    if (amsd != null) {
    	amsd.setState(AMSAgentDescription.SUSPENDED);
    }
    ContainerID cid = ad.getContainerID();
		platformAgents.release(name);
		
    // Notify listeners
    fireSuspendedAgent(cid, name);
  }

  /**
     Notify the platform that an agent has just resumed
   */
  public void resumedAgent(AID name) throws NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(name);
    if(ad == null)
      throw new NotFoundException("ResumedAgent failed to find " + name);
    AMSAgentDescription amsd = ad.getDescription();
    if (amsd != null) {
    	amsd.setState(AMSAgentDescription.ACTIVE);
    }
    ContainerID cid = ad.getContainerID();
    platformAgents.release(name);

    // Notify listeners
    fireResumedAgent(cid, name);
  }

  /**
     Notify the platform that an agent has just moved
   */
    public void movedAgent(AID agentID, ContainerID srcID, ContainerID destID) throws NotFoundException {

	AgentDescriptor ad = platformAgents.acquire(agentID);
	if (ad == null) {
		throw new NotFoundException("Agent "+agentID.getName()+" not found in GADT");
	}
	ad.setContainerID((ContainerID)destID);
	fireMovedAgent((ContainerID)srcID, (ContainerID)destID, agentID);
	platformAgents.release(agentID);
    }

  /**
     Notify the platform that an agent has just frozen
   */
  public void frozenAgent(AID name, ContainerID bufferContainer) throws NotFoundException {
      AgentDescriptor ad = platformAgents.acquire(name);
      if (ad == null)
	  throw new NotFoundException("FrozenAgent failed to find " + name);
      AMSAgentDescription amsd = ad.getDescription();
      if (amsd != null) {
	  amsd.setState(AMSAgentDescription.SUSPENDED);
      }
      ContainerID cid = ad.getContainerID();
      platformAgents.release(name);
		
      // Notify listeners
      fireFrozenAgent(cid, name, bufferContainer);
  }

  /**
     Notify the platform that an agent has just thawed
   */
  public void thawedAgent(AID name, ContainerID bufferContainer) throws NotFoundException {
      AgentDescriptor ad = platformAgents.acquire(name);
      if (ad == null)
	  throw new NotFoundException("ThawedAgent failed to find " + name);
      AMSAgentDescription amsd = ad.getDescription();
      if (amsd != null) {
	  amsd.setState(AMSAgentDescription.ACTIVE);
      }
      ContainerID cid = ad.getContainerID();
      platformAgents.release(name);
		
      // Notify listeners
      fireThawedAgent(cid, name, bufferContainer);
  }


  /**
     Notify the platform that a new MTP has become active on a given container
   */
  public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
	String containerName = cid.getName();
	String[] mtpAddrs = mtp.getAddresses();
	String mtpAddress = mtpAddrs[0];
	platformAddresses.add(mtpAddress);
	containers.addMTP(cid, mtp);

	// Update the AMS-descriptions of all registered agents living in the platform
	AID[] allIds = platformAgents.keys();
	for (int i = 0; i < allIds.length; ++i) {
	    AgentDescriptor ad = platformAgents.acquire(allIds[i]);
	    AMSAgentDescription dsc = ad.getDescription();	
	    if (dsc != null && ad.isNative()) {
		AID id = dsc.getName();
		id.addAddresses(mtpAddress);
	    } 
	    platformAgents.release(allIds[i]);
	}
      
	// Notify listeners (typically the AMS)
	fireAddedMTP(mtp, cid);
    }
    catch(NotFoundException nfe) {
	System.out.println("Error: the container " + cid.getName() + " was not found.");
    }
  }

  /**
     Notify the platform that an MTP is no longer active on a given container
   */
  public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
    try {
      String containerName = cid.getName();
      String[] mtpAddrs = mtp.getAddresses();
      String mtpAddress = mtpAddrs[0];
      platformAddresses.remove(mtpAddress);
      containers.removeMTP(cid, mtp);

      // Update the AMS-descriptions of all agents living in the platform
	    AID[] allIds = platformAgents.keys();
	    for (int i = 0; i < allIds.length; ++i) {
	    	AgentDescriptor ad = platformAgents.acquire(allIds[i]);
	      AMSAgentDescription dsc = ad.getDescription();	
	      if (ad.isNative()) {
		  AID id = dsc.getName();
		  id.removeAddresses(mtpAddress);
	      } 
	      platformAgents.release(allIds[i]);
	    }
      
      // Notify listeners (typically the AMS)
      fireRemovedMTP(mtp, cid);
    }
    catch(NotFoundException nfe) {
      System.out.println("Error: the container " + cid.getName() + " was not found.");
    }
  }


  /**
     Return the principal of an agent
   *
    public JADEPrincipal getAgentPrincipal(AID agentID) throws IMTPException, NotFoundException {
	return new DummyPrincipal(agentID, JADEPrincipal.NONE ); //getPrincipal(agentID);
    }*/

  //////////////////////////////////////////////////////////////////////
  // AgentManager interface implementation.
  // These methods are called by the AMS to execute the actions that can 
  // be requested by agents in the platform.
  //////////////////////////////////////////////////////////////////////

  public void addTool(AID tool) {
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.ADD_TOOL, jade.core.management.AgentManagementSlice.NAME, null);
      cmd.addParam(tool);

      Object ret = myCommandProcessor.processOutgoing(cmd);
      if (ret != null) {
      	if (ret instanceof Throwable) {
      		((Throwable) ret).printStackTrace();
      	}
      }
  }

  public void removeTool(AID tool) {
      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REMOVE_TOOL, jade.core.management.AgentManagementSlice.NAME, null);
			cmd.addParam(tool);

			Object ret = myCommandProcessor.processOutgoing(cmd);
      if (ret != null) {
      	if (ret instanceof Throwable) {
      		((Throwable) ret).printStackTrace();
      	}
      }
  }


  /**
     Create an agent on a given container
     @see AgentManager#create(String agentName, String className, String arguments[], ContainerID cid, String ownership, CertificateFolder certs) throws UnreachableException, JADESecurityException, NotFoundException
   */
  public void create(String name, String className, String args[], ContainerID cid, JADEPrincipal owner, Credentials initialCredentials, JADEPrincipal requesterPrincipal, Credentials requesterCredentials) throws UnreachableException, JADESecurityException, NotFoundException, NameClashException {

      // Get the container where to create the agent
      // If it is not specified, assume it is the Main
      if (cid == null || cid.getName() == null) {
	  cid = localContainerID;
      }

      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_CREATE, jade.core.management.AgentManagementSlice.NAME, null);

      cmd.addParam(name);
      cmd.addParam(className);
      cmd.addParam(args);
      cmd.addParam(cid);
      cmd.addParam(owner);
      cmd.addParam(initialCredentials);
      cmd.setPrincipal(requesterPrincipal);
      cmd.setCredentials(requesterCredentials);

      Object ret = myCommandProcessor.processOutgoing(cmd);
      if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof NameClashException) {
			      throw (NameClashException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
			  else if (ret instanceof JADESecurityException) {
			      throw (JADESecurityException)ret;
			  }
			  else if (ret instanceof Throwable) {
          ((Throwable) ret).printStackTrace();
			  	// In methods called by the AMS to serve agents requests we throw
			  	// a RuntimeException that will result in a FAILURE message sent
			  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
      }
  }


    /**
       Kill an agent wherever it is
    */
    public void kill(AID agentID, JADEPrincipal requesterPrincipal, Credentials requesterCredentials) throws NotFoundException, UnreachableException, JADESecurityException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_KILL, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
  cmd.setPrincipal(requesterPrincipal);
  cmd.setCredentials(requesterCredentials);

	Object ret = myCommandProcessor.processOutgoing(cmd);
  if (ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if (ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if (ret instanceof JADESecurityException) {
	      throw (JADESecurityException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
	      throw new RuntimeException(((Throwable) ret).getMessage());
	  }
  }
    }

    /**
       Suspend an agent wherever it is
    */
    public void suspend(final AID agentID) throws NotFoundException, UnreachableException, JADESecurityException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(AgentState.getInstance(Agent.AP_SUSPENDED));

	Object ret = myCommandProcessor.processOutgoing(cmd);
  if (ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if (ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if (ret instanceof JADESecurityException) {
	      throw (JADESecurityException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
	  		throw new RuntimeException(((Throwable) ret).getMessage());
	  }
  }
    }

    /**
       Resume an agent wherever it is
    */
    public void activate(final AID agentID) throws NotFoundException, UnreachableException, JADESecurityException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(AgentState.getInstance(Agent.AP_ACTIVE));

	Object ret = myCommandProcessor.processOutgoing(cmd);
  if (ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if (ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if (ret instanceof JADESecurityException) {
	      throw (JADESecurityException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
	      throw new RuntimeException(((Throwable) ret).getMessage());
	  }
  }
    }


    /**
       Put an agent in the WAITING state wherever it is
    */
    public void wait(AID agentID, String password) throws NotFoundException, UnreachableException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(AgentState.getInstance(Agent.AP_WAITING));

	Object ret = myCommandProcessor.processOutgoing(cmd);
  if (ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if (ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
	      throw new RuntimeException(((Throwable) ret).getMessage());
	  }
  }
    }

    /**
       Wake-up an agent wherever it is
    */
    public void wake(AID agentID, String password) throws NotFoundException, UnreachableException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(AgentState.getInstance(Agent.AP_ACTIVE));

	Object ret = myCommandProcessor.processOutgoing(cmd);
  if (ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if (ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
	      throw new RuntimeException(((Throwable) ret).getMessage());
	  }
  }
  }

    /**
       Move an agent to a given destination
    */
    public void move(AID agentID, Location where) throws NotFoundException, UnreachableException, JADESecurityException {

	ContainerID from = getContainerID(agentID);
	ContainerID to = (ContainerID)where;
		
	// Check whether the destination exists
	containers.getContainerNode(to);

	GenericCommand cmd = new GenericCommand(jade.core.mobility.AgentMobilityHelper.REQUEST_MOVE, jade.core.mobility.AgentMobilitySlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(where);

	Object ret = myCommandProcessor.processOutgoing(cmd);
  if (ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if (ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if (ret instanceof JADESecurityException) {
	      throw (JADESecurityException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
	      throw new RuntimeException(((Throwable) ret).getMessage());
	  }
  }

    }


    /**
       Clone an agent to a given destination
    */
    public void copy(AID agentID, Location where, String newName) throws NotFoundException, NameClashException, UnreachableException, JADESecurityException {
	ContainerID from = getContainerID(agentID);
	ContainerID to = (ContainerID)where;
		
	// Check whether the destination exists
	containers.getContainerNode(to);

	GenericCommand cmd = new GenericCommand(jade.core.mobility.AgentMobilityHelper.REQUEST_CLONE, jade.core.mobility.AgentMobilitySlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(where);
	cmd.addParam(newName);

	Object ret = myCommandProcessor.processOutgoing(cmd);
      if(ret != null) {
	  if(ret instanceof NotFoundException) {
	      throw (NotFoundException)ret;
	  }
	  else if(ret instanceof NameClashException) {
	      throw (NameClashException)ret;
	  }
	  else if(ret instanceof UnreachableException) {
	      throw (UnreachableException)ret;
	  }
	  else if(ret instanceof JADESecurityException) {
	      throw (JADESecurityException)ret;
	  }
	  else if (ret instanceof Throwable) {
		  	// In methods called by the AMS to serve agents requests we throw
		  	// a RuntimeException that will result in a FAILURE message sent
		  	// back to the requester
		  	throw new RuntimeException(((Throwable) ret).getMessage());
	  }
      }

  }

    /** 
	Kill a given container
    */
    public void killContainer(ContainerID cid) throws NotFoundException, JADESecurityException {
			GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.KILL_CONTAINER, jade.core.management.AgentManagementSlice.NAME, null);
			cmd.addParam(cid);
			Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof JADESecurityException) {
			      throw (JADESecurityException)ret;
			  }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }

    }

    /**
       Shut down the whole platform
    **/
    public void shutdownPlatform() throws JADESecurityException {

	// First kill all peripheral containers
	ContainerID[] allContainers = containers.names();
	for(int i = 0; i < allContainers.length; i++) {
	    ContainerID targetID = allContainers[i];
	    try {
				if(!getContainerNode(targetID).hasPlatformManager()) {
				    killContainer(targetID);
				    containers.waitForRemoval(targetID);
				}
	    }
	    catch(JADESecurityException ae) {
				System.out.println("Cannot kill container " + targetID.getName() + ": Permission Denied.");
	    }
	    catch(NotFoundException nfe) {
				// Ignore the exception as we are removing a non-existing container
        System.out.println("Container " + targetID.getName() + " does not exist. Ignoring...");
	    }
	}

	// Then kill all other main containers
	allContainers = containers.names();
	for(int i = 0; i < allContainers.length; i++) {
	    ContainerID targetID = allContainers[i];
	    try {
				if(!targetID.equals(localContainerID)) {
				    killContainer(targetID);
				    containers.waitForRemoval(targetID);
				}
	    }
	    catch(JADESecurityException ae) {
				System.out.println("Cannot kill container " + targetID.getName() + ": Permission Denied.");
	    }
	    catch(NotFoundException nfe) {
				// Ignore the exception as we are removing a non-existing container
        System.out.println("Container " + targetID.getName() + " does not exist. Ignoring...");
	    }
	}
	
	// Finally, kill the local container
	try {
	    killContainer(localContainerID);

	    // Make sure all containers are succesfully removed from the table...
	    containers.waitUntilEmpty();

	}
	catch(JADESecurityException ae) {
	    System.out.println("Cannot kill container " + localContainerID.getName() + ": Permission Denied.");
	}
	catch(NotFoundException nfe) {
	    // Should never happen
	    nfe.printStackTrace();
	}
    }

  /** 
     Install a new MTP on a given container
   */
  public MTPDescriptor installMTP(String address, ContainerID cid, String className) throws NotFoundException, UnreachableException, MTPException {

      GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.INSTALL_MTP, jade.core.messaging.MessagingSlice.NAME, null);
      cmd.addParam(address);
      cmd.addParam(cid);
      cmd.addParam(className);

      Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
	      else if (ret instanceof MTPException) {
		  		throw (MTPException)ret;
	      }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }

      MTPDescriptor dsc = (MTPDescriptor)ret;
      /***
      System.out.println("--- New MTP ---");
      System.out.println("Name: " + dsc.getName());
      System.out.println("Addresses: ");
      for(int i = 0; i < dsc.getAddresses().length; i++) {
	  System.out.println("[" + dsc.getAddresses()[i] + "]");
      }
      System.out.println("Supported Protocols: ");
      for(int i = 0; i < dsc.getSupportedProtocols().length; i++) {
	  System.out.println("[" + dsc.getSupportedProtocols()[i] + "]");
      }
      ***/


      return dsc;
  }

  /** 
     Uninstall an MTP on a given container
   */
  public void uninstallMTP(String address, ContainerID cid) throws NotFoundException, UnreachableException, MTPException {

      GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.UNINSTALL_MTP, jade.core.messaging.MessagingSlice.NAME, null);
      cmd.addParam(address);
      cmd.addParam(cid);

      Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
	      else if (ret instanceof MTPException) {
		  		throw (MTPException)ret;
	      }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }
  }

  /**
     Change the ownership of an agent
     // FIXME: implement or remove
   */
	public void take(final AID agentID, final String username, final byte[] password) throws NotFoundException, UnreachableException, JADESecurityException {
	}


  /**
     Activate sniffing on a given agent
  */
  public void sniffOn(AID snifferName, List toBeSniffed) throws NotFoundException, UnreachableException  {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.SNIFF_ON, jade.core.event.NotificationSlice.NAME, null);
      cmd.addParam(snifferName);
      cmd.addParam(toBeSniffed);

      Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }

  }

  /**
     Deactivate sniffing on a given agent
  */
  public void sniffOff(AID snifferName, List notToBeSniffed) throws NotFoundException, UnreachableException {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.SNIFF_OFF, jade.core.event.NotificationSlice.NAME, null);
      cmd.addParam(snifferName);
      cmd.addParam(notToBeSniffed);

      Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }
  }

  /**
     Activate debugging on a given agent
  */
  public void debugOn(AID debuggerName, List toBeDebugged) throws NotFoundException, UnreachableException {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.DEBUG_ON, jade.core.event.NotificationSlice.NAME, null);
      cmd.addParam(debuggerName);
      cmd.addParam(toBeDebugged);

      Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }
  }

  /**
     Deactivate debugging on a given agent
  */
  public void debugOff(AID debuggerName, List notToBeDebugged) throws NotFoundException, UnreachableException {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.DEBUG_OFF, jade.core.event.NotificationSlice.NAME, null);
      cmd.addParam(debuggerName);
      cmd.addParam(notToBeDebugged);

      Object ret = myCommandProcessor.processOutgoing(cmd);
		  if (ret != null) {
			  if(ret instanceof NotFoundException) {
			      throw (NotFoundException)ret;
			  }
			  else if (ret instanceof UnreachableException) {
			      throw (UnreachableException)ret;
			  }
			  else if (ret instanceof Throwable) {
				  	// In methods called by the AMS to serve agents requests we throw
				  	// a RuntimeException that will result in a FAILURE message sent
				  	// back to the requester
			      throw new RuntimeException(((Throwable) ret).getMessage());
			  }
		  }
  }

  /**
     Register an agent to the White Pages service of this platform
   */
  public void amsRegister(AMSAgentDescription dsc) throws AlreadyRegistered, JADESecurityException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
  	AgentDescriptor ad = platformAgents.acquire(agentID);
  	if (ad == null) {
  		System.out.println("No descriptor found for agent "+agentID);
  		// This is a foreign agent registering to this platform
  		ad = new AgentDescriptor(AgentDescriptor.FOREIGN_AGENT);
  		ad.setDescription(dsc);
  		platformAgents.put(agentID, ad);
  	}
  	else {
  		if (ad.getDescription() == null) {
  			System.out.println("Descriptor with null AMSD found for agent "+agentID);
  			// This is an agent living in the platform that had previously deregistered 
  			ad.setDescription(dsc);
  			platformAgents.release(agentID);
  		}
  		else {
  			System.out.println("Descriptor with NON null AMSD found for agent "+agentID);
  			// This agent is already registered --> Exception
  			platformAgents.release(agentID);
	  		throw new AlreadyRegistered();
  		}
  	}
  }
  
  /**
     Deregister an agent from the White Pages service of this platform
   */
  public void amsDeregister(AMSAgentDescription dsc) throws NotRegistered, JADESecurityException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		AgentDescriptor ad = platformAgents.acquire(agentID);
  	if (ad != null) {
  		if (ad.getDescription() != null) {
	  		if (ad.isNative()) {
	  			// This is an agent living in the platform --> just clear its registration
	  			ad.setDescription(null);
	  			platformAgents.release(agentID);
	  		}
	  		else {
	  			// This is a foreign agent --> remove the descriptor completely
	  			platformAgents.remove(agentID);
	  		}
	  		return;
  		}
  	}
  	// This agent was not registered --> Exception
	  throw new NotRegistered();
  }
  
  /**
     Modify the registration of an agent to the White Pages service of 
     this platform.
     If the modification implies a change in the agent state (and the agent
     lives in the platform) --> force that change 
     If the modification implies a change in the agent ownership (and the agent
     lives in the platform) --> force that change 
   */
  public void amsModify(AMSAgentDescription dsc) throws NotRegistered, NotFoundException, UnreachableException, JADESecurityException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
  	AgentDescriptor ad = platformAgents.acquire(agentID);
  	if (ad != null) {
  		AMSAgentDescription oldDsc = ad.getDescription();
  		if (oldDsc != null) {
  			ad.setDescription(dsc);
  			String newState = dsc.getState();
  			String newOwnership = dsc.getOwnership();
  			if (newOwnership == null) {
  				newOwnership = oldDsc.getOwnership();
  			}
  			platformAgents.release(agentID);
	  		if (ad.isNative()) {
	  			// This is an agent living in the platform --> if necessary
	  			// force changes in agent state and ownership 
	  			if (AMSAgentDescription.SUSPENDED.equals(newState) && !AMSAgentDescription.SUSPENDED.equals(oldDsc.getState())) {
	  				suspend(agentID);
	  			}
	  			if (AMSAgentDescription.ACTIVE.equals(newState) && !AMSAgentDescription.ACTIVE.equals(oldDsc.getState())) {
	  				activate(agentID);
	  			}
	  			if (newOwnership != null && newOwnership != oldDsc.getOwnership()) {
						/*byte[] password = Agent.extractPassword(newOwnership);
						String username = Agent.extractUsername(newOwnership);
						take(agentID, username, password);*/
	  			}
	  		}
	  		return;
  		}
  	}
  	// This agent was not registered --> Exception
	  throw new NotRegistered();
  }
  
  /**
     Searches the White Pages for agents whose description matches a given
     template.
   */
  public List amsSearch(AMSAgentDescription template, long maxResults) {
  	List results = new ArrayList();
  	AID[] ids = platformAgents.keys();
  	for (int i = 0; i < ids.length; ++i) {
  		try {
				AMSAgentDescription amsd = getAMSDescription(ids[i]); 
				if (match(template, amsd)) {
					results.add(amsd);
					if (results.size() >= maxResults) {
						break;
					}
				}
  		}
  		catch (NotFoundException nfe) {
  			// The agent disappeared while we were looping. Ignore it
  		}
  	}
  	return results;
  }
  
  /**
     Return the IDs of all containers in the platform
   */
  public ContainerID[] containerIDs() {
  	return containers.names();
  }

  /**
     Return the IDs of all agents in the platform
   */
  public AID[] agentNames() {
    return platformAgents.keys();
  }

  /**
     Return all MTPs in a given container
   */
  public List containerMTPs(ContainerID cid) throws NotFoundException {
  	return containers.getMTPs(cid);
  }

  /**
     Return all agents living on a container
   */
  public List containerAgents(ContainerID cid) throws NotFoundException {
  	List agents = new ArrayList();
    AID[] allIds = platformAgents.keys();

    for (int i = 0; i < allIds.length; ++i) {
    	AID    id = allIds[i];
    	AgentDescriptor ad = platformAgents.acquire(id);
      ContainerID cid1 = ad.getContainerID();

      if (cid.equals(cid1)) {
	  agents.add(id);
      } 
      platformAgents.release(id);
    } 
    return agents;
  }

  public void toolAdded(AID tool) {
      synchronized(agentTools) {
	  if(!agentTools.contains(tool)) {
	      agentTools.add(tool);
	  }
      }
  }

  public void toolRemoved(AID tool) {
      synchronized(agentTools) {
	  agentTools.remove(tool);
      }
  }

  public AID[] agentTools() {
      synchronized(agentTools) {
	  Object[] objs = agentTools.toArray();
	  AID[] result = new AID[objs.length];
	  for(int i = 0; i < result.length; i++) {
	      result[i] = (AID)objs[i];
	  }

	  return result;
      }
  }

  
  /**
     Return the ID of the container an agent lives in
   */
  public ContainerID getContainerID(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if(ad == null)
      throw new NotFoundException("getContainerID() failed to find agent " + agentID.getName());
    ContainerID result = ad.getContainerID();
    platformAgents.release(agentID);
    return result;
  }

  /**
     Return the node a container is deployed at
  */
  public Node getContainerNode(ContainerID cid) throws NotFoundException {
      return containers.getContainerNode(cid);
  }

  /**
     Return the AMS description of an agent
   */
  public AMSAgentDescription getAMSDescription(AID agentID) throws NotFoundException {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if(ad == null)
      throw new NotFoundException("getAMSDescription() failed to find agent " + agentID.getName());
    AMSAgentDescription amsd = ad.getDescription();
    platformAgents.release(agentID);
    return amsd;
  }
  
  /**
     Add a listener of platform events
   */
  public void addListener(AgentManager.Listener l) {
    platformListeners.add(l);
  }

  /**
     Remove a listener of platform events
   */
  public void removeListener(AgentManager.Listener l) {
    platformListeners.remove(l);
  }

	
  JADEPrincipal getPrincipal(ContainerID cid) {
	 	JADEPrincipal cp = null;
	 	try {
			return containers.getPrincipal(cid);
	 	}
	 	catch (NotFoundException nfe) {
	 		// FIXME: Should we create an "empty" Principal
	 		return null;
	 	}
  }

  Credentials getCredentials(ContainerID cid) {
	 	try {
			return containers.getCredentials(cid);
	 	}
	 	catch (NotFoundException nfe) {
	 		// FIXME: Should we create "empty" Credentials
	 		return null;
	 	}
  }

  private boolean match(AMSAgentDescription templateDesc, AMSAgentDescription factDesc) {
		try {
		  String o1 = templateDesc.getOwnership();
		  if(o1 != null) {
		    String o2 = factDesc.getOwnership();
		    if((o2 == null) || (!o1.equalsIgnoreCase(o2)))
		      return false;
		  }
	
		  String s1 = templateDesc.getState();
		  if(s1 != null) {
		    String s2 = factDesc.getState();
		    if((s2 == null) || (!s1.equalsIgnoreCase(s2)))
		      return false;
		  }
	
		  AID id1 = templateDesc.getName();
		  if(id1 != null) {
		    AID id2 = factDesc.getName();
		    if((id2 == null) || (!matchAID(id1, id2)))
		      return false;
		  }
	
		  return true;
		}
		catch (ClassCastException cce) {
		  return false;
		}
  }
  
  // Helper method to match two Agent Identifiers
  private final boolean matchAID(AID template, AID fact) {
    // Match the GUID in the ':name' slot
    String templateName = template.getName();
    if(templateName != null) {
      String factName = fact.getName();
      if((factName == null) || (!templateName.equalsIgnoreCase(factName)))
				return false;
    }

    // Match the address sequence. See 'FIPA Agent Management Specification, Sect. 6.4.2.1'
    Iterator itTemplate = template.getAllAddresses();
    Iterator itFact = fact.getAllAddresses();

    // All the elements in the template sequence must appear in the
    // fact sequence, in the same order
    while(itTemplate.hasNext()) {
      String templateAddr = (String)itTemplate.next();

      // Search 'templateAddr' into the remaining part of the fact sequence
      boolean found = false;
      while(!found && itFact.hasNext()) {
				String factAddr = (String)itFact.next();
				found = templateAddr.equalsIgnoreCase(factAddr);
      }
      if(!found) // An element of the template does not appear in the fact sequence
				return false;
    }

    // Match the resolvers sequence. See 'FIPA Agent Management Specification, Sect. 6.4.2.1'
    itTemplate = template.getAllResolvers();
    itFact = fact.getAllResolvers();

    while(itTemplate.hasNext()) {
      AID templateRes = (AID)itTemplate.next();

      // Search 'templateRes' into the remaining part of the fact sequence
      boolean found = false;
      while(!found && itFact.hasNext()) {
				AID factRes = (AID)itFact.next();
				found = matchAID(templateRes, factRes); // Recursive call
      }
      if(!found) // An element of the template does not appear in the fact sequence
				return false;
    }
    
    return true;
  }

  /////////////////////////////////////////////////////////////////
  // Private methods to notify platform listeners of significant 
  // events.
  /////////////////////////////////////////////////////////////////
  private void fireAddedContainer(ContainerID cid) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.ADDED_CONTAINER, cid);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.addedContainer(ev);
    }
  }

  private void fireRemovedContainer(ContainerID cid) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.REMOVED_CONTAINER, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.removedContainer(ev);
    }
  }

  private void fireChangedContainerPrincipal(ContainerID cid, String from, String to) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.CHANGED_CONTAINER_PRINCIPAL, null, cid, from, to);
    for (int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.changedContainerPrincipal(ev);
    }
  }

  private void fireBornAgent(ContainerID cid, AID agentID, String ownership) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.BORN_AGENT, agentID, cid, null, ownership);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.bornAgent(ev);
    }
  }

  private void fireDeadAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.DEAD_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.deadAgent(ev);
    }
  }

  private void fireSuspendedAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.SUSPENDED_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.suspendedAgent(ev);
    }
  }

  private void fireResumedAgent(ContainerID cid, AID agentID) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.RESUMED_AGENT, agentID, cid);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.resumedAgent(ev);
    }
  }

  private void fireFrozenAgent(ContainerID cid, AID agentID, ContainerID bufferContainer) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.FROZEN_AGENT, agentID, cid, bufferContainer);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.frozenAgent(ev);
    }
  }

  private void fireThawedAgent(ContainerID cid, AID agentID, ContainerID bufferContainer) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.THAWED_AGENT, agentID, cid, bufferContainer);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.thawedAgent(ev);
    }
  }

  private void fireChangedAgentPrincipal(ContainerID cid, AID agentID, String from, String to) {
      PlatformEvent ev = new PlatformEvent(PlatformEvent.CHANGED_AGENT_PRINCIPAL, agentID, cid, from, to);

      for (int i = 0; i < platformListeners.size(); i++) {
	  AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
	  l.changedAgentPrincipal(ev);
      }
  }

  private void fireMovedAgent(ContainerID from, ContainerID to, AID agentID) {
  	PlatformEvent ev = new PlatformEvent(agentID, from, to);

    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.movedAgent(ev);
    }
  }

  private void fireAddedMTP(MTPDescriptor mtp, ContainerID cid) {
    String name = mtp.getName();
    String[] addrs = mtp.getAddresses();
    Channel ch = new Channel("FIXME: missing channel name", name, addrs[0]);
    MTPEvent ev = new MTPEvent(MTPEvent.ADDED_MTP, cid, ch);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.addedMTP(ev);
   } 
  }

  private void fireRemovedMTP(MTPDescriptor mtp, ContainerID cid) {
    String name = mtp.getName();
    String[] addrs = mtp.getAddresses();
    Channel ch = new Channel("FIXME: missing channel name", name, addrs[0]);
    MTPEvent ev = new MTPEvent(MTPEvent.REMOVED_MTP, cid, ch);
    for(int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.removedMTP(ev);
   }
  }

    private void removeAllAgents(ContainerID cid) {
	String name = cid.getName();
	AID[] allIDs = platformAgents.keys();
	for(int i = 0; i < allIDs.length; i++) {
	    AgentDescriptor ad = platformAgents.acquire(allIDs[i]);
	    ContainerID id = ad.getContainerID();
	    platformAgents.release(allIDs[i]);
	    if(CaseInsensitiveString.equalsIgnoreCase(id.getName(), name)) {
		try {
		    deadAgent(allIDs[i]);
		}
		catch(NotFoundException nfe) {
		    nfe.printStackTrace();
		}
	    }
	}
    }

    private void removeAllMTPs(ContainerID cid) {
	try {
	    List l = containers.getMTPs(cid);
	    Object[] objs = l.toArray();
	    for(int i = 0; i < objs.length; i++) {
		MTPDescriptor mtp = (MTPDescriptor)objs[i];

		GenericCommand gCmd = new GenericCommand(jade.core.messaging.MessagingSlice.DEAD_MTP, jade.core.messaging.MessagingSlice.NAME, null);
		gCmd.addParam(mtp);
		gCmd.addParam(cid);
		myCommandProcessor.processOutgoing(gCmd);

	    }
	}
	catch(NotFoundException nfe) {
	    nfe.printStackTrace();
	}
    }

    public AgentDescriptor acquireAgentDescriptor(AID agentID) {
	return platformAgents.acquire(agentID);
    }

    public void releaseAgentDescriptor(AID agentID) {
	platformAgents.release(agentID);
    }

}
