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


package jade.core.replication;

//#MIDP_EXCLUDE_FILE

import jade.core.AgentManager;
import jade.core.ServiceFinder;
import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.NodeEventListener;
import jade.core.NodeFailureMonitor;

import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.MainContainerImpl;
import jade.core.PlatformManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException;
import jade.core.UnreachableException;

import jade.core.AID;
import jade.core.ContainerID;
import jade.core.AgentManager.Listener;
import jade.core.event.MTPEvent;
import jade.core.event.PlatformEvent;

import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import jade.mtp.MTPDescriptor;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;
import jade.util.Logger;


/**
   A kernel-level service to manage a ring of Main Containers,
   keeping the various replicas in sync and providing failure
   detection and recovery to make JADE tolerate Main Container
   crashes.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class MainReplicationService extends BaseService {

    private static final boolean EXCLUDE_MYSELF = false;
    private static final boolean INCLUDE_MYSELF = true;

    private Logger logger = Logger.getMyLogger(this.getClass().getName());

    private static final String[] OWNED_COMMANDS = new String[] {
    };

    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);

	myContainer = ac;

	// Create a local slice
	localSlice = new ServiceComponent();

	// Create the command filters
	outFilter = new CommandOutgoingFilter();
	inFilter = new CommandIncomingFilter();

    }

    public String getName() {
	return MainReplicationSlice.NAME;
    }

    public Class getHorizontalInterface() {
	try {
	    return Class.forName(MainReplicationSlice.NAME + "Slice");
	}
	catch(ClassNotFoundException cnfe) {
	    return null;
	}
    }

    public Service.Slice getLocalSlice() {
	return localSlice;
    }

    public Filter getCommandFilter(boolean direction) {
	if(direction == Filter.OUTGOING) {
	    return outFilter;
	}
	else {
	    return inFilter;
	}
    }

    public Sink getCommandSink(boolean side) {
	return null;
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }

    public void boot(Profile p) throws ServiceException {

	try {

	    // Initialize the label of this node
	    Service.Slice[] slices = getAllSlices();
	    myLabel = slices.length - 1;

	    // Temporarily store the slices into an array...
	    MainReplicationSlice[] temp = new MainReplicationSlice[slices.length];

	    String localNodeName = getLocalNode().getName();
	    for(int i = 0; i < slices.length; i++) {
		try {
		    MainReplicationSlice slice = (MainReplicationSlice)slices[i];
		    String sliceName = slice.getNode().getName();
		    int label = slice.getLabel();

		    temp[label] = slice;

		    if(!sliceName.equals(localNodeName)) {
			slice.addReplica(localNodeName, myPlatformManager.getLocalAddress(), myLabel);
		    }

		    if(label == myLabel - 1) {
			localSlice.attachTo(label, slice);
		    }
		}
		catch(IMTPException imtpe) {
		    // Ignore it: stale slice...
		}
	    }

	    // copy all the slices from the temporary array to the slice list
	    for(int i = 0; i < temp.length; i++) {
		replicas.add(temp[i]);
	    }

	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("An error occurred during service startup.", imtpe);
	}

    }


    private class CommandOutgoingFilter extends Filter {

	public boolean accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.management.AgentManagementSlice.ADD_TOOL)) {
		    handleNewTool(cmd);
		}
		else if(name.equals(jade.core.management.AgentManagementSlice.REMOVE_TOOL)) {
		    handleDeadTool(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }

	    // Never veto a command
	    return true;
	}

	private void handleNewTool(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    AID tool = (AID)params[0];

	    GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_NEWTOOL, MainReplicationSlice.NAME, null);
	    hCmd.addParam(tool);

	    broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	}

	private void handleDeadTool(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    AID tool = (AID)params[0];

	    GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_DEADTOOL, MainReplicationSlice.NAME, null);
	    hCmd.addParam(tool);

	    broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	}

	public void setBlocking(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

    	public boolean isBlocking() {
	    return false; // Blocking and Skipping not implemented
	}

	public void setSkipping(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

	public boolean isSkipping() {
	    return false; // Blocking and Skipping not implemented
	}

    } // End of CommandOutgoingFilter class


    private class CommandIncomingFilter extends Filter {

	public boolean accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
		    handleInformCreated(cmd);
		}
		else if(name.equals(jade.core.management.AgentManagementSlice.INFORM_KILLED)) {
		    handleInformKilled(cmd);
		}
		else if(name.equals(jade.core.management.AgentManagementSlice.INFORM_STATE_CHANGED)) {
		    handleInformStateChanged(cmd);
		}
		else if(name.equals(jade.core.messaging.MessagingSlice.NEW_MTP)) {
		    handleNewMTP(cmd);
		}
		else if(name.equals(jade.core.messaging.MessagingSlice.DEAD_MTP)) {
		    handleDeadMTP(cmd);
		}
	    }
	    catch (Throwable t) {
				cmd.setReturnValue(t);
	    }
	    /*catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(JADESecurityException ae) {
		cmd.setReturnValue(ae);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }*/

	    // Never veto a command
	    return true;
	}

	private void handleInformCreated(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException, ServiceException {

	    Object[] params = cmd.getParams();

	    AID agentID = (AID)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_BORNAGENT, MainReplicationSlice.NAME, null);
	    hCmd.addParam(agentID);
	    hCmd.addParam(cid);
	    hCmd.setPrincipal(cmd.getPrincipal());
	    hCmd.setCredentials(cmd.getCredentials());

	    broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	}

	private void handleInformKilled(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];

	    GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_DEADAGENT, MainReplicationSlice.NAME, null);
	    hCmd.addParam(agentID);

	    broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	}

	private void handleInformStateChanged(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    String newState = (String)params[1];

	    if (newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
		GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_SUSPENDEDAGENT, MainReplicationSlice.NAME, null);
		hCmd.addParam(agentID);

		broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	    }
	    else if(newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
		GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_RESUMEDAGENT, MainReplicationSlice.NAME, null);
		hCmd.addParam(agentID);

		broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	    }

	}

	private void handleNewMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    MTPDescriptor mtp = (MTPDescriptor)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_NEWMTP, MainReplicationSlice.NAME, null);
	    hCmd.addParam(mtp);
	    hCmd.addParam(cid);

	    broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	}

	private void handleDeadMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
	    Object[] params = cmd.getParams();
	    MTPDescriptor mtp = (MTPDescriptor)params[0];
	    ContainerID cid = (ContainerID)params[1];

	    GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_DEADMTP, MainReplicationSlice.NAME, null);
	    hCmd.addParam(mtp);
	    hCmd.addParam(cid);

	    broadcastToReplicas(hCmd, EXCLUDE_MYSELF);
	}

	public void setBlocking(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

    	public boolean isBlocking() {
	    return false; // Blocking and Skipping not implemented
	}

	public void setSkipping(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

	public boolean isSkipping() {
	    return false; // Blocking and Skipping not implemented
	}

    } // End of CommandIncomingFilter class


    private class ServiceComponent implements Service.Slice, NodeEventListener {

	public ServiceComponent() {
    myMain = (MainContainerImpl) myContainer.getMain();
    myPlatformManager = myMain.getPlatformManager();
	}

	private void attachTo(int label, MainReplicationSlice slice) throws IMTPException, ServiceException {

	    // Stop the previous monitor, if any
	    if(nodeMonitor != null) {
	    	//log("Stop monitoring node <"+nodeMonitor.getNode().getName()+">", 2);
                if (logger.isLoggable(Logger.CONFIG))
                  logger.log(Logger.CONFIG,"Stop monitoring node <"+nodeMonitor.getNode().getName()+">");
				nodeMonitor.stop();
	    }

	    // Store the label of the monitored slice
	    monitoredLabel = label;

	    // Avoid monitoring yourself
	    if(monitoredLabel == myLabel) {
		return;
	    }

	    // Store the Service Manager address for the monitored slice
	    monitoredSvcMgr = slice.getPlatformManagerAddress();

	    // Set up a failure monitor on the target slice...
	    nodeMonitor = NodeFailureMonitor.getFailureMonitor();
	    nodeMonitor.start(slice.getNode(), this);
	}

	// Implementation of the Service.Slice interface

	public Service getService() {
	    return MainReplicationService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return MainReplicationService.this.getLocalNode();
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
	    }
	}

	public VerticalCommand serve(HorizontalCommand cmd) {
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(MainReplicationSlice.H_GETLABEL)) {
		    Integer i = new Integer(getLabel());
		    cmd.setReturnValue(i);
		}
		else if(cmdName.equals(MainReplicationSlice.H_GETPLATFORMMANAGERADDRESS)) {
		    cmd.setReturnValue(getPlatformManagerAddress());
		}
		else if(cmdName.equals(MainReplicationSlice.H_ADDREPLICA)) {
		    String sliceName = (String)params[0];
		    String smAddr = (String)params[1];
		    int sliceIndex = ((Integer)params[2]).intValue();
		    addReplica(sliceName, smAddr, sliceIndex);
		}
		else if(cmdName.equals(MainReplicationSlice.H_REMOVEREPLICA)) {
		    String smAddr = (String)params[0];
		    int sliceIndex = ((Integer)params[1]).intValue();
		    removeReplica(smAddr, sliceIndex);
		}
		else if(cmdName.equals(MainReplicationSlice.H_FILLGADT)) {
		    AID[] agents = (AID[])params[0];
		    ContainerID[] containers = (ContainerID[])params[1];
		    fillGADT(agents, containers);
		}
		else if(cmdName.equals(MainReplicationSlice.H_BORNAGENT)) {
		    AID name = (AID)params[0];
		    ContainerID cid = (ContainerID)params[1];
		    bornAgent(name, cid, cmd.getPrincipal(), cmd.getCredentials());
		}
		else if(cmdName.equals(MainReplicationSlice.H_DEADAGENT)) {
		    AID name = (AID)params[0];
		    deadAgent(name);
		}
		else if(cmdName.equals(MainReplicationSlice.H_SUSPENDEDAGENT)) {
		    AID name = (AID)params[0];
		    suspendedAgent(name);
		}
		else if(cmdName.equals(MainReplicationSlice.H_RESUMEDAGENT)) {
		    AID name = (AID)params[0];
		    resumedAgent(name);
		}
		else if(cmdName.equals(MainReplicationSlice.H_NEWMTP)) {
		    MTPDescriptor mtp = (MTPDescriptor)params[0];
		    ContainerID cid = (ContainerID)params[1];
		    newMTP(mtp, cid);
		}
		else if(cmdName.equals(MainReplicationSlice.H_DEADMTP)) {
		    MTPDescriptor mtp = (MTPDescriptor)params[0];
		    ContainerID cid = (ContainerID)params[1];
		    deadMTP(mtp, cid);
		}
		else if(cmdName.equals(MainReplicationSlice.H_NEWTOOL)) {
		    AID tool = (AID)params[0];
		    newTool(tool);
		}
		else if(cmdName.equals(MainReplicationSlice.H_DEADTOOL)) {
		    AID tool = (AID)params[0];
		    deadTool(tool);
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
	    return null;
	}


	private int getLabel() throws IMTPException {
	    return myLabel;
	}

	private String getPlatformManagerAddress() throws IMTPException {
	    return myPlatformManager.getLocalAddress();
	}

	private void addReplica(String sliceName, String smAddr, int sliceIndex) throws IMTPException, ServiceException {
	    MainReplicationSlice slice = (MainReplicationSlice)getSlice(sliceName);
	    replicas.add(sliceIndex, slice);

	    // If first in line, close the ring by monitoring the newly arrived slice,
	    // and start sending data to the new slice...
	    if(myLabel == 0) {
		attachTo(sliceIndex, slice);

		// Send all the data about the GADT...
		AID[] names = myMain.agentNames();
		ContainerID[] containers = new ContainerID[names.length];
		for(int i = 0; i < names.length; i++) {
		    try {
			containers[i] = myMain.getContainerID(names[i]);
		    }
		    catch(NotFoundException nfe) {
			// It should never happen...
			nfe.printStackTrace();
		    }
		}

		// FIXME: What about principal and ownership?
		slice.fillGADT(names, containers);

		// Update the status of each suspended agent...
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setState(AMSAgentDescription.SUSPENDED);
		List suspendedAgents = myMain.amsSearch(amsd, -1); // '-1' means 'all the results'

		Iterator it = suspendedAgents.iterator();
		while(it.hasNext()) {
		    AMSAgentDescription desc = (AMSAgentDescription)it.next();
		    try {
			slice.suspendedAgent(desc.getName());
		    }
		    catch(NotFoundException nfe) {
			// It should never happen...
			nfe.printStackTrace();
		    }
		}

		// Send the tool list...
		AID[] tools = myMain.agentTools();
		for(int i = 0; i < tools.length; i++) {
		    slice.newTool(tools[i]);
		}

	    }

	}

	private void removeReplica(String smAddr, int index) throws IMTPException {
	    replicas.remove(index);
	    adjustLabels(index);
	}

	private void adjustLabels(int index) {
	    if(index < myLabel) {
		myLabel--;
		monitoredLabel--;
		if(monitoredLabel == -1) {
		    monitoredLabel += replicas.size();
		}
	    }
	    else if(myLabel == 0) {
		// Handle the ring wrap-around case...
		monitoredLabel--;
	    }
	}

	private void fillGADT(AID[] agents, ContainerID[] containers) throws JADESecurityException {
	    for(int i = 0; i < agents.length; i++) {

		try {
				// FIXME: What about principal and ownership?
		    myMain.bornAgent(agents[i], containers[i], null, null, true);
				//log("Agent "+agents[i].getName()+" inserted into GADT", 2);
                                if (logger.isLoggable(Logger.CONFIG))
                                  logger.log(Logger.CONFIG,"Agent "+agents[i].getName()+" inserted into GADT");

		}
		catch(NotFoundException nfe) {
		    // It should never happen...
		    nfe.printStackTrace();
		}
		catch(NameClashException nce) {
		    // It should never happen...
		    nce.printStackTrace();
		}
	    }

	}

	private void bornAgent(AID name, ContainerID cid, JADEPrincipal principal, Credentials credentials) throws NameClashException, NotFoundException {
	  // Retrieve the ownership from the credentials
    String ownership = "NONE";
    if (credentials != null) {
    	JADEPrincipal ownerPr = credentials.getOwner();
    	if (ownerPr != null) {
    		ownership = ownerPr.getName();
    	}
    }
	    try {
		// If the name is already in the GADT, throws NameClashException
		myMain.bornAgent(name, cid, principal, ownership, false);
		//log("Agent "+name.getName()+" inserted into GADT", 2);
                if (logger.isLoggable(Logger.CONFIG))
                  logger.log(Logger.CONFIG,"Agent "+name.getName()+" inserted into GADT");

	    }
	    catch(NameClashException nce) {
		try {
		    ContainerID oldCid = myMain.getContainerID(name);
		    Node n = myMain.getContainerNode(oldCid).getNode();

		    // Perform a non-blocking ping to check...
		    n.ping(false);

		    // Ping succeeded: rethrow the NameClashException
		    throw nce;
		}
		catch(NameClashException nce2) {
		    throw nce2; // Let this one through...
		}
		catch(Exception e) {
		    // Ping failed: forcibly replace the dead agent...
		    myMain.bornAgent(name, cid, principal, ownership, true);
				//log("Agent "+name.getName()+" inserted into GADT", 2);
                                if (logger.isLoggable(Logger.CONFIG))
                                  logger.log(Logger.CONFIG,"Agent "+name.getName()+" inserted into GADT");

		}
	    }
	}

	private void deadAgent(AID name) throws NotFoundException {
	    myMain.deadAgent(name);
	}

	private void suspendedAgent(AID name) throws NotFoundException {
	    myMain.suspendedAgent(name);
	}

	private void resumedAgent(AID name) throws NotFoundException {
	    myMain.resumedAgent(name);
	}

	private void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
	    myMain.newMTP(mtp, cid);
	}

	private void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
	    myMain.deadMTP(mtp, cid);
	}

	private void newTool(AID tool) throws IMTPException {
	    myMain.toolAdded(tool);
	}

	private void deadTool(AID tool) throws IMTPException {
	    myMain.toolRemoved(tool);
	}

	public void dumpReplicas() {
	    try {
		System.out.println("--- " + getLocalNode().getName() + "[" + myLabel + "] ---");
		System.out.println("--- Monitoring node [" + monitoredLabel + "] ---");
		System.out.println("--- Replica list ---");
		Object[] slices = replicas.toArray();
		for(int i = 0; i < slices.length; i++) {
		    MainReplicationSlice slice = (MainReplicationSlice)slices[i];
		    System.out.println("----- " + slice.getNode().getName() + "[" + i + "] -----");
		}
		System.out.println("--- End ---");
	    }
	    catch(Throwable t) {
		t.printStackTrace();
	    }
	}

	private void dumpGADT() {
	    AID[] agents = myMain.agentNames();
	    System.out.println("--- Agent List ---");
	    for(int i = 0; i < agents.length; i++) {
		System.out.println("    Agent: " + agents[i].getLocalName());
	    }
	    System.out.println("------------------");
	}

	// Implementation of the NodeEventListener interface

	public void nodeAdded(Node n) {
  	//log("Start monitoring node <"+n.getName()+">", 2);
          if (logger.isLoggable(Logger.CONFIG))
            logger.log(Logger.CONFIG,"Start monitoring node <"+n.getName()+">");

	}

	public void nodeRemoved(Node n) {
	    //log("Node <"+n.getName()+"> TERMINATED", 2);
            if (logger.isLoggable(Logger.CONFIG))
              logger.log(Logger.CONFIG,"Node <"+n.getName()+"> TERMINATED");

	    try {
	    	myPlatformManager.removeReplica(monitoredSvcMgr, false);
	    	myPlatformManager.removeNode(new NodeDescriptor(n), false);


	  replicas.remove(monitoredLabel);
		// Broadcast a 'removeReplica()' method (exclude yourself from bcast)
		GenericCommand hCmd = new GenericCommand(MainReplicationSlice.H_REMOVEREPLICA, MainReplicationSlice.NAME, null);
		hCmd.addParam(monitoredSvcMgr);
		hCmd.addParam(new Integer(monitoredLabel));
		broadcastToReplicas(hCmd, EXCLUDE_MYSELF);

		int oldLabel = myLabel;

		// Adjust the label, and become leader if it is the case...
		adjustLabels(monitoredLabel);

		// -- Attach to the new neighbour slice...
		MainReplicationSlice newSlice = (MainReplicationSlice)replicas.get(monitoredLabel);
		attachTo(monitoredLabel, newSlice);

		if((oldLabel != 0) && (myLabel == 0)) {
		    //log("-- I'm the new leader ---", 1);
                    if (logger.isLoggable(Logger.INFO))
                      logger.log(Logger.INFO,"-- I'm the new leader ---");


		    myContainer.becomeLeader();

		}

	    }
	    catch(IMTPException imtpe) {
		imtpe.printStackTrace();
	    }
	    catch(ServiceException se) {
		se.printStackTrace();
	    }
	}

	public void nodeUnreachable(Node n) {
	    //log("Node <"+n.getName()+"> UNREACHABLE", 2);
            if (logger.isLoggable(Logger.CONFIG))
              logger.log(Logger.CONFIG,"Node <"+n.getName()+"> UNREACHABLE");

	}

	public void nodeReachable(Node n) {
	    //log("Node <"+n.getName()+"> REACHABLE", 2);
            if (logger.isLoggable(Logger.CONFIG))
              logger.log(Logger.CONFIG,"Node <"+n.getName()+"> REACHABLE");

	}

	// The active object monitoring the remote node
	private NodeFailureMonitor nodeMonitor;

	// The integer label of the monitored slice
	private int monitoredLabel;

	private String monitoredSvcMgr;

    } // End of ServiceComponent class

  /*#PJAVA_INCLUDE_BEGIN
  // PJAVA workaround as protected methods inherited from parent class
  // are not accessible to inner classes
  //public void log(String txt, int l) {
    //super.log(txt,l);
  //}
  #PJAVA_INCLUDE_END*/


    private AgentContainer myContainer;

    private ServiceComponent localSlice;

    private Filter outFilter;
    private Filter inFilter;



    // Service specific data

    private int myLabel = -1;
    private final List replicas = new LinkedList();

    // Owned copies of Main Container and Service Manager
    private MainContainerImpl myMain;
    private PlatformManager myPlatformManager;

    private void broadcastToReplicas(HorizontalCommand cmd, boolean includeSelf) throws IMTPException, ServiceException {

	Object[] slices = replicas.toArray();

	String localNodeName = getLocalNode().getName();
	for(int i = 0; i < slices.length; i++) {
	    MainReplicationSlice slice = (MainReplicationSlice)slices[i];

	    String sliceName = slice.getNode().getName();
	    if(includeSelf || !sliceName.equals(localNodeName)) {
		slice.serve(cmd);
		Object ret = cmd.getReturnValue();
		if (ret instanceof Throwable) {
			//log("Error propagating H-command "+cmd.getName()+" to slice "+sliceName, 0);
                        if (logger.isLoggable(Logger.SEVERE))
                          logger.log(Logger.SEVERE,"Error propagating H-command "+cmd.getName()+" to slice "+sliceName);

			((Throwable) ret).printStackTrace();
		}
	    }
	}

    }




}
