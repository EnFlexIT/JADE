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

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;

import jade.domain.ams;
import jade.domain.df;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.NotRegistered;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;

import jade.security.Authority;
import jade.security.AuthException;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.JADECertificate;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.DelegationCertificate;
import jade.security.IdentityCertificate;
import jade.security.CertificateFolder;
import jade.security.PrivilegedExceptionAction;


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

    private String platformID;
    private ContainerID localContainerID;
    private IMTPManager myIMTPManager;

    // FIXME: Temporary Hack
    private CommandProcessor myCommandProcessor;

    private List platformListeners = new LinkedList();
    private List platformAddresses = new LinkedList();
    private List agentTools = new LinkedList();

    private ContainerTable containers = new ContainerTable();
    private GADT platformAgents = new GADT();
  
    private Authority authority;
    private Profile myProfile;

    public MainContainerImpl(Profile p) throws ProfileException {
	myProfile = p;
	myIMTPManager = p.getIMTPManager();
	myCommandProcessor = p.getCommandProcessor();

	platformID = p.getParameter(Profile.PLATFORM_ID, null);
	if (platformID == null || platformID.equals("")) {
	    try {
		// Build the PlatformID using the local host and port
		List l = myIMTPManager.getLocalAddresses();
		TransportAddress localAddr = (TransportAddress) l.get(0);
		platformID = localAddr.getHost() + ":" + localAddr.getPort() + "/JADE";
	    }
	    catch (Exception e) {
		throw new ProfileException("Can't set PlatformID");
	    }
	}

	try {
	    if (p.getParameter(Profile.OWNER, null) != null) {
		// if there is an owner for this container
		// then try to use the full implementation of security
		p.setParameter(Profile.MAINAUTH_CLASS,"jade.security.impl.PlatformAuthority");
		p.setParameter(Profile.AUTHORITY_CLASS,"jade.security.impl.ContainerAuthority");
	    }
	    String type = p.getParameter(Profile.MAINAUTH_CLASS, null);
	    if (type != null) {
		authority = (Authority)Class.forName(type).newInstance();
		authority.setName("main-authority");
		authority.init(p, this);
	    }
	}
	catch (Exception e1) {
	    System.out.println("Some problems occured during the initialization of the security. JADE will continue execution by using dummy security.");
	    authority = null;
	    //			e1.printStackTrace();
	}
		
	try {
	    if (authority == null) {
		authority = new jade.security.dummy.DummyAuthority();
		authority.setName("main-authority");
		authority.init(p, this);
	    }
	}
	catch (Exception e2) {
	    System.err.println("Could not init dummy platform authority");
	    //e2.printStackTrace();
	}
	
    }

    public void addLocalContainer(NodeDescriptor desc) throws IMTPException, AuthException {

	Node node = desc.getNode();
	ContainerID cid = desc.getContainer();
	String username = desc.getPrincipalName();
	byte[] password = desc.getPrincipalPwd();

	// Authenticate user
	ContainerPrincipal principal = getAuthority().createContainerPrincipal(cid, username);
	CertificateFolder certs = authority.authenticate(principal, password);
	authority.checkAction(Authority.PLATFORM_CREATE, principal, certs);
	authority.checkAction(Authority.CONTAINER_CREATE, principal, certs);

	// Add the calling container as the main container
	containers.addContainer(cid, node, principal);

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

    public void addRemoteContainer(NodeDescriptor desc) throws AuthException {

	Node node = desc.getNode();
	ContainerID cid = desc.getContainer();
	String username = desc.getPrincipalName();
	byte[] password = desc.getPrincipalPwd();

	// Authenticate user
	ContainerPrincipal principal = authority.createContainerPrincipal(cid, username);
	CertificateFolder certs = authority.authenticate(principal, password);
	authority.checkAction(Authority.CONTAINER_CREATE, principal, certs);
		
	// Set the container-principal
	//	ac.changeContainerPrincipal(certs);

	// add to the platform's container list
	containers.addContainer(cid, node, principal);

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

    public void initSystemAgents(AgentContainer ac, boolean startThem) throws IMTPException, NotFoundException, AuthException {
	ContainerID cid = ac.getID();
	ContainerPrincipal cp = containers.getPrincipal(cid);
	String agentOwnership = cp.getOwnership();

	// Start the AMS
	theAMS = new ams(this);
	theAMS.setOwnership(agentOwnership);
	AgentPrincipal amsPrincipal = authority.createAgentPrincipal(ac.getAMS(), agentOwnership);
	//	CertificateFolder amsCerts = authority.authenticate(amsPrincipal, password);
	CertificateFolder amsCerts = authority.authenticate(amsPrincipal, new byte[] {}); // FIXME: Temporary Hack 
	theAMS.setPrincipal(amsCerts);

	// Notify the AMS about the main container existence
	fireAddedContainer(cid);
	fireChangedContainerPrincipal(cid, null, cp);

	// Start the Default DF
	defaultDF = new df();
	defaultDF.setOwnership(agentOwnership);
	AgentPrincipal dfPrincipal = authority.createAgentPrincipal(ac.getDefaultDF(), agentOwnership);
	//	CertificateFolder dfCerts = authority.authenticate(dfPrincipal, password);
	CertificateFolder dfCerts = authority.authenticate(dfPrincipal, new byte[] {}); // FIXME: Temporary Hack
	defaultDF.setPrincipal(dfCerts);

	if(startThem) {
	    startSystemAgents(ac);
	}

    }

    // Start the AMS and the Default DF
    public void startSystemAgents(AgentContainer ac) throws IMTPException, NotFoundException, AuthException {

	try {
	    theAMS.resetEvents(true);
	    ac.initAgent(ac.getAMS(), theAMS, AgentContainerImpl.CREATE_AND_START);
	    theAMS.waitUntilStarted();
	}
	catch(Exception e) {
	    e.printStackTrace();
	    throw new IMTPException("Exception during AMS startup", e);
	}
 
	try {
	    ac.initAgent(ac.getDefaultDF(), defaultDF, AgentContainerImpl.CREATE_AND_START);
	    defaultDF.waitUntilStarted();
	}
	catch(Exception e) {
	    throw new IMTPException("Exception during Default DF startup", e);
	}

    }


  public String getPlatformName() {
    return platformID;
  }

  /**
     Notify the platform that an agent has just born on a container
   */
  public void bornAgent(AID name, ContainerID cid, CertificateFolder certs, boolean forceReplacement) throws NameClashException, NotFoundException, AuthException {

    // verify identity certificate
    authority.verify(certs.getIdentityCertificate());

    AgentDescriptor ad = new AgentDescriptor(AgentDescriptor.NATIVE_AGENT);
    ad.setContainerID(cid);
    AgentPrincipal principal = (AgentPrincipal) certs.getIdentityCertificate().getSubject();
    ad.setPrincipal(principal);
    // CertificateFolder to be used by the AMS to perform actions on
    // behalf of (requested  by) the new agent
    ad.setAMSDelegation(prepareAMSDelegation(certs));
    // Registration to the With Pages service
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setOwnership(principal.getOwnership());
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
    fireBornAgent(cid, name, principal);
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
     Notify the platform that the principal of an agent has changed
   */
	public void changedAgentPrincipal(AID name, CertificateFolder certs) throws IMTPException, NotFoundException {

	    /***

		// FIXME: Probably we should let the AuthException pass through as in bornAgent()
		try {
			authority.verify(certs.getIdentityCertificate());
			
			AgentDescriptor ad = platformAgents.acquire(name);
			if (ad == null)
				throw new NotFoundException("ChangedAgentPrincipal failed to find " + name);			
			AgentPrincipal from = ad.getPrincipal();
			AgentPrincipal to = (AgentPrincipal)certs.getIdentityCertificate().getSubject();
			ad.setPrincipal(to);
			AMSAgentDescription amsd = ad.getDescription();
			if (amsd != null) {
				amsd.setOwnership(to.getOwnership());
			}
			ad.setAMSDelegation(prepareAMSDelegation(certs));
			ContainerID cid = ad.getContainerID();
			platformAgents.release(name);
			
			// Add the new principal to the principals table of all the containers. 
      // Synchronized to avoid additions/removals of containers during update
      synchronized(containers) {
				AgentContainer[] allContainers = containers.containers();
				for (int i = 0; i < allContainers.length; i++) {
					AgentContainer ac = allContainers[i];
					// FIXME: If some container is temporarily disconnected it will not be
					// notified. We should investigate the side-effects
					try {
						ac.changedAgentPrincipal(name, to);
					}
					catch (IMTPException imtpe) {
						imtpe.printStackTrace();
					}
				}
      }
			
			// Notify listeners
			fireChangedAgentPrincipal(cid, name, from, to);    
		}
		catch (AuthException ae) {
			System.err.println(ae.getMessage());
		}
	    ***/

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
   */
    public AgentPrincipal getAgentPrincipal(AID agentID) throws IMTPException, NotFoundException {
	return getPrincipal(agentID);
    }

    /**
       Make the platform authority sign a certificate
    */
  public JADECertificate sign(JADECertificate certificate, CertificateFolder certs) throws IMTPException, AuthException {
    authority.sign(certificate, certs);
    return certificate;
  }
  
  /**
     Return the public key of the platform
   */
  public byte[] getPublicKey() throws IMTPException {
  	return authority.getPublicKey();
  }

  //////////////////////////////////////////////////////////////////////
  // AgentManager interface implementation.
  // These methods are called by the AMS to execute the actions that can 
  // be requested by agents in the platform.
  //////////////////////////////////////////////////////////////////////

  public void addTool(AID tool) {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.ADD_TOOL, jade.core.event.NotificationSlice.NAME, null);
      cmd.addParam(tool);

      myCommandProcessor.processOutgoing(cmd);
  }

  public void removeTool(AID tool) {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.REMOVE_TOOL, jade.core.event.NotificationSlice.NAME, null);
	cmd.addParam(tool);

	myCommandProcessor.processOutgoing(cmd);
  }


  /**
     Create an agent on a given container
     @see AgentManager#create(String agentName, String className, String arguments[], ContainerID cid, String ownership, CertificateFolder certs) throws UnreachableException, AuthException, NotFoundException
   */
  public void create(String name, String className, String args[], ContainerID cid, String ownership, CertificateFolder certs) throws UnreachableException, AuthException, NotFoundException, NameClashException {

      // Get the container where to create the agent
      // If it is not specified, assume it is the Main
      if (cid == null || cid.getName() == null) {
	  cid = localContainerID;
      }

      // --- This code should go into the Security Service ---

      // Check permissions
      authority.checkAction(Authority.AGENT_CREATE, (AgentPrincipal)certs.getIdentityCertificate().getSubject(), null);
      authority.checkAction(Authority.CONTAINER_CREATE_IN, getPrincipal(cid), null);

      // --- End of code that should go into the Security Service ---

      GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_CREATE, jade.core.management.AgentManagementSlice.NAME, null);

      cmd.addParam(name);
      cmd.addParam(className);
      cmd.addParam(args);
      cmd.addParam(cid);
      cmd.addParam(ownership);
      cmd.addParam(certs);

      Object result = myCommandProcessor.processOutgoing(cmd);
      if(result != null) {
	  if(result instanceof NotFoundException) {
	      throw (NotFoundException)result;
	  }
	  if(result instanceof NameClashException) {
	      throw (NameClashException)result;
	  }
	  if(result instanceof UnreachableException) {
	      ((Throwable)result).printStackTrace();
	      throw (UnreachableException)result;
	  }
	  if(result instanceof AuthException) {
	      throw (AuthException)result;
	  }
      }
  }


    /**
       Kill an agent wherever it is
    */
    public void kill(final AID agentID) throws NotFoundException, UnreachableException, AuthException {

	// --- This code should go into the Security Service ---

	// Check permissions
	authority.checkAction(Authority.CONTAINER_KILL_IN, getPrincipal(getContainerID(agentID)), null);
	authority.checkAction(Authority.AGENT_KILL, getPrincipal(agentID), null);

	// --- End of code that should go into the Security Service ---

	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_KILL, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);

	myCommandProcessor.processOutgoing(cmd);
    }

    /**
       Suspend an agent wherever it is
    */
    public void suspend(final AID agentID) throws NotFoundException, UnreachableException, AuthException {

	// --- This code should go into the Security Service ---

	// Check permissions
	authority.checkAction(Authority.AGENT_SUSPEND, getPrincipal(agentID), null);

	// --- End of code that should go into the Security Service ---

	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(new AgentState(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED));

	myCommandProcessor.processOutgoing(cmd);
    }

    /**
       Resume an agent wherever it is
    */
    public void activate(final AID agentID) throws NotFoundException, UnreachableException, AuthException {

	// --- This code should go into the Security Service ---

	// Check permissions
	authority.checkAction(Authority.AGENT_RESUME, getPrincipal(agentID), null);

	// --- End of code that should go into the Security Service ---

	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(new AgentState(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE));

	myCommandProcessor.processOutgoing(cmd);
    }


    /**
       Put an agent in the WAITING state wherever it is
    */
    public void wait(AID agentID, String password) throws NotFoundException, UnreachableException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(new AgentState(jade.domain.FIPAAgentManagement.AMSAgentDescription.WAITING));

	myCommandProcessor.processOutgoing(cmd);
    }

    /**
       Wake-up an agent wherever it is
    */
    public void wake(AID agentID, String password) throws NotFoundException, UnreachableException {
	GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.REQUEST_STATE_CHANGE, jade.core.management.AgentManagementSlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(new AgentState(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE));

	myCommandProcessor.processOutgoing(cmd);
  }

    /**
       Move an agent to a given destination
    */
    public void move(AID agentID, Location where) throws NotFoundException, UnreachableException, AuthException {

	ContainerID from = getContainerID(agentID);
	ContainerID to = (ContainerID)where;
		
	// Check whether the destination exists
	containers.getContainerNode(to);


	// --- This code should go into the Security Service ---
		
	// Check permissions
	authority.checkAction(Authority.CONTAINER_MOVE_FROM, getPrincipal(from), null);
	authority.checkAction(Authority.CONTAINER_MOVE_TO, getPrincipal(to), null);
	authority.checkAction(Authority.AGENT_MOVE, getPrincipal(agentID), null);

	// --- End of code that should go into the Security Service ---

	GenericCommand cmd = new GenericCommand(jade.core.mobility.AgentMobilitySlice.REQUEST_MOVE, jade.core.mobility.AgentMobilitySlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(where);

	myCommandProcessor.processOutgoing(cmd);

    }


    /**
       Clone an agent to a given destination
    */
    public void copy(AID agentID, Location where, String newName) throws NotFoundException, NameClashException, UnreachableException, AuthException {
	ContainerID from = getContainerID(agentID);
	ContainerID to = (ContainerID)where;
		
	// Check whether the destination exists
	containers.getContainerNode(to);

	// --- This code should go into the Security Service ---

	// Check permissions
	authority.checkAction(Authority.AGENT_CLONE, getPrincipal(agentID), null);
	authority.checkAction(Authority.CONTAINER_CLONE_FROM, getPrincipal(from), null);
	authority.checkAction(Authority.CONTAINER_CLONE_TO, getPrincipal(to), null);

	// --- End of code that should go into the Security Service ---

	GenericCommand cmd = new GenericCommand(jade.core.mobility.AgentMobilitySlice.REQUEST_CLONE, jade.core.mobility.AgentMobilitySlice.NAME, null);
	cmd.addParam(agentID);
	cmd.addParam(where);
	cmd.addParam(newName);

	Object result = myCommandProcessor.processOutgoing(cmd);
      if(result != null) {
	  if(result instanceof NotFoundException) {
	      throw (NotFoundException)result;
	  }
	  if(result instanceof NameClashException) {
	      throw (NameClashException)result;
	  }
	  if(result instanceof UnreachableException) {
	      ((Throwable)result).printStackTrace();
	      throw (UnreachableException)result;
	  }
	  if(result instanceof AuthException) {
	      throw (AuthException)result;
	  }
      }

  }

    /** 
	Kill a given container
    */
    public void killContainer(final ContainerID cid) throws NotFoundException, AuthException {

	// --- This code should go into the Security Service ---

	// Check permissions
	authority.checkAction(Authority.CONTAINER_KILL, getPrincipal(cid), null);

	// --- End of code that should go into the Security Service ---


	// Do the action in a separate thread to avoid deadlock (we
	// need again full permissions to start a thread)
	try {
	    authority.doPrivileged(new jade.security.PrivilegedExceptionAction() {
		    public Object run() {
			GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.KILL_CONTAINER, jade.core.management.AgentManagementSlice.NAME, null);
			cmd.addParam(cid);
			myCommandProcessor.processOutgoing(cmd);

			return null;
		    }
		});
	}
	catch (Exception e) {
	    // Should never happen
	    e.printStackTrace();
	}
    }

    /**
       Shut down the whole platform
    **/
    public void shutdownPlatform() throws AuthException {

	// Kill every other container
	ContainerID[] allContainers = containers.names();

	for(int i = 0; i < allContainers.length; i++) {
	    ContainerID targetID = allContainers[i];
	    try {
		if(!targetID.equals(localContainerID)) {
		    killContainer(targetID);
		    containers.waitForRemoval(targetID);
		}
	    }
	    catch(AuthException ae) {
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
	catch(AuthException ae) {
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

      Object result = myCommandProcessor.processOutgoing(cmd);

      if(result instanceof NotFoundException) {
	  throw (NotFoundException)result;
      }
      if(result instanceof UnreachableException) {
	  throw (UnreachableException)result;
      }
      if(result instanceof MTPException) {
	  throw (MTPException)result;
      }


      MTPDescriptor dsc = (MTPDescriptor)result;
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


      return (MTPDescriptor)result;
  }

  /** 
     Uninstall an MTP on a given container
   */
  public void uninstallMTP(String address, ContainerID cid) throws NotFoundException, UnreachableException, MTPException {

      GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.UNINSTALL_MTP, jade.core.messaging.MessagingSlice.NAME, null);
      cmd.addParam(address);
      cmd.addParam(cid);

      myCommandProcessor.processOutgoing(cmd);
  }

  /**
     Change the principal of an agent
   */
	public void take(final AID agentID, final String username, final byte[] password) throws NotFoundException, UnreachableException, AuthException {
		// Check permissions	
		authority.checkAction(Authority.AGENT_TAKE, getPrincipal(agentID), null);

		// Create the certificate folder for the new principal
		final AgentPrincipal principal = authority.createAgentPrincipal(agentID, username);
		final CertificateFolder certs = authority.authenticate(principal, password);
		
		// Do the action (we need again full permissions to execute a remote call)
		try {	
			authority.doPrivileged(new jade.security.PrivilegedExceptionAction() {
		  	public Object run() throws IMTPException, NotFoundException {
			    //	  			getContainerFromAgent(agentID).changeAgentPrincipal(agentID, certs);
	  			return null;
				}
			});
		}
		catch (IMTPException re) {
			throw new UnreachableException(re.getMessage());
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}


  /**
     Activate sniffing on a given agent
  */
  public void sniffOn(AID snifferName, List toBeSniffed) throws NotFoundException, UnreachableException  {
      GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.SNIFF_ON, jade.core.event.NotificationSlice.NAME, null);
      cmd.addParam(snifferName);
      cmd.addParam(toBeSniffed);

      Object result = myCommandProcessor.processOutgoing(cmd);
      if(result != null) {
	  if(result instanceof NotFoundException) {
	      throw (NotFoundException)result;
	  }
	  if(result instanceof UnreachableException) {
	      throw (UnreachableException)result;
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

      Object result = myCommandProcessor.processOutgoing(cmd);
      if(result != null) {
	  if(result instanceof NotFoundException) {
	      throw (NotFoundException)result;
	  }
	  if(result instanceof UnreachableException) {
	      throw (UnreachableException)result;
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

      Object result = myCommandProcessor.processOutgoing(cmd);
      if(result != null) {
	  if(result instanceof NotFoundException) {
	      throw (NotFoundException)result;
	  }
	  if(result instanceof UnreachableException) {
	      throw (UnreachableException)result;
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

      Object result = myCommandProcessor.processOutgoing(cmd);
      if(result != null) {
	  if(result instanceof NotFoundException) {
	      throw (NotFoundException)result;
	  }
	  if(result instanceof UnreachableException) {
	      throw (UnreachableException)result;
	  }
      }
  }

  /**
     Register an agent to the White Pages service of this platform
   */
  public void amsRegister(AMSAgentDescription dsc) throws AlreadyRegistered, AuthException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		// Check permissions	
		authority.checkAction(Authority.AMS_REGISTER, getPrincipal(agentID), null);

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
  public void amsDeregister(AMSAgentDescription dsc) throws NotRegistered, AuthException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		// Check permissions	
		authority.checkAction(Authority.AMS_DEREGISTER, getPrincipal(agentID), null);
  	
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
     lives in the platform) --> force that chage 
     If the modification implies a change in the agent ownership (and the agent
     lives in the platform) --> force that chage 
   */
  public void amsModify(AMSAgentDescription dsc) throws NotRegistered, NotFoundException, UnreachableException, AuthException {
  	// Mandatory slots have already been checked
  	AID agentID = dsc.getName();
  	
		// Check permissions	
		authority.checkAction(Authority.AMS_MODIFY, getPrincipal(agentID), null);

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
						byte[] password = Agent.extractPassword(newOwnership);
						String username = Agent.extractUsername(newOwnership);
						take(agentID, username, password);
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
     Return the delegation certificate to be used by the AMS to executed
     actions on behalf of (requested by) a given agent
   */
  public CertificateFolder getAMSDelegation(AID agentID) {
    AgentDescriptor ad = platformAgents.acquire(agentID);
    if (ad == null) {
    	// Create a CertificateFolder with no delegated permissions
    	// FIXME: With should give the permissions of the AgentPrincipal.NONE user
	    try {
	    	AgentPrincipal amsPrincipal = theAMS.getPrincipal();
		    DelegationCertificate amsDelegation = authority.createDelegationCertificate();
		    amsDelegation.setSubject(amsPrincipal);
		    authority.sign(amsDelegation, theAMS.getCertificateFolder());
		    return new CertificateFolder(theAMS.getCertificateFolder().getIdentityCertificate(), amsDelegation);
	    }
	    catch (AuthException ae) {
	    	// Should never happen
	    	ae.printStackTrace();
	    }
    }
    CertificateFolder cf = ad.getAMSDelegation();
    platformAgents.release(agentID);
    return cf;
  }

  public void addServiceManagerAddress(String smAddr) {
      GenericCommand cmd = new GenericCommand(jade.core.replication.AddressNotificationSlice.SM_ADDRESS_ADDED, jade.core.replication.AddressNotificationSlice.NAME, null);
      cmd.addParam(smAddr);

      myCommandProcessor.processOutgoing(cmd);
  }

  public void removeServiceManagerAddress(String smAddr) {
      GenericCommand cmd = new GenericCommand(jade.core.replication.AddressNotificationSlice.SM_ADDRESS_REMOVED, jade.core.replication.AddressNotificationSlice.NAME, null);
      cmd.addParam(smAddr);

      myCommandProcessor.processOutgoing(cmd);
  }


  /**
     Return the platform main authority
   */
	public Authority getAuthority() {
		return authority;
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

    /***
  ////////////////////////////////////////////////
  // Private utility methods
  ////////////////////////////////////////////////
  private AgentContainer getContainerFromAgent(AID agentID) throws NotFoundException {
    return containers.getContainer(getContainerID(agentID));
  }
    ***/

  private CertificateFolder prepareAMSDelegation(CertificateFolder certs) throws AuthException {
    AgentPrincipal amsPrincipal = theAMS.getPrincipal();
    DelegationCertificate amsDelegation = authority.createDelegationCertificate();
    amsDelegation.setSubject(amsPrincipal);
    for (int c = 0; c < certs.getDelegationCertificates().size(); c++) {
      amsDelegation.addPermissions(((DelegationCertificate)certs.getDelegationCertificates().get(c)).getPermissions());
    }
    authority.sign(amsDelegation, certs);
    return new CertificateFolder(theAMS.getCertificateFolder().getIdentityCertificate(), amsDelegation);
  }

	public AgentPrincipal getPrincipal(AID agentID) {
		AgentPrincipal ap;
		AgentDescriptor ad = platformAgents.acquire(agentID);

		if (ad == null) {
			return authority.createAgentPrincipal(agentID, AgentPrincipal.NONE);
		}
		else {
			ap = ad.getPrincipal();
			platformAgents.release(agentID);
			return ap;
		}
	}
	
  private ContainerPrincipal getPrincipal(ContainerID cid) {
	 	ContainerPrincipal cp = null;
	 	try {
			cp = containers.getPrincipal(cid);
	 	}
	 	catch (NotFoundException nfe) {
	 	}
	 	if (cp == null) {
	 		cp = authority.createContainerPrincipal(cid, ContainerPrincipal.NONE);
	 	}
	 	return cp;
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

  private void fireChangedContainerPrincipal(ContainerID cid, ContainerPrincipal from, ContainerPrincipal to) {
  	if (from == null) {
  		from = authority.createContainerPrincipal(cid, ContainerPrincipal.NONE);
  	}
    PlatformEvent ev = new PlatformEvent(PlatformEvent.CHANGED_CONTAINER_PRINCIPAL, null, cid, from, to);
    for (int i = 0; i < platformListeners.size(); i++) {
      AgentManager.Listener l = (AgentManager.Listener)platformListeners.get(i);
      l.changedContainerPrincipal(ev);
    }
  }

  private void fireBornAgent(ContainerID cid, AID agentID, AgentPrincipal principal) {
    PlatformEvent ev = new PlatformEvent(PlatformEvent.BORN_AGENT, agentID, cid, null, principal);

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

  private void fireChangedAgentPrincipal(ContainerID cid, AID agentID, AgentPrincipal from, AgentPrincipal to) {
      if (from == null) {
	  from = authority.createAgentPrincipal(agentID, AgentPrincipal.NONE);
      }
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

    public void lockEntryForAgent(AID agentID) {
	platformAgents.acquire(agentID);
    }

    public void updateEntryForAgent(AID agentID, Location srcID, Location destID) throws IMTPException, NotFoundException {

	AgentDescriptor ad = platformAgents.acquire(agentID);
	ad.setContainerID((ContainerID)destID);
	fireMovedAgent((ContainerID)srcID, (ContainerID)destID, agentID);
	platformAgents.release(agentID);
    }

    public void unlockEntryForAgent(AID agentID) {
	platformAgents.release(agentID);
    }

}
