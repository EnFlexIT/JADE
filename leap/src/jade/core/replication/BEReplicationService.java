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
import jade.core.BackEndContainer;
import jade.core.MainContainer;
import jade.core.MainContainerImpl;
import jade.core.ServiceManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.NameClashException; 
import jade.core.UnreachableException;

import jade.core.AID;
import jade.core.ContainerID;

import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import jade.mtp.MTPDescriptor;

import jade.security.CertificateFolder;
import jade.security.IdentityCertificate;
import jade.security.ContainerPrincipal;
import jade.security.AuthException;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Iterator;


/**
   A kernel-level service to manage a ring of Back-End Containers,
   keeping the various replicas in sync and providing failure
   detection and recovery to make JADE tolerate Back-End Container
   crashes.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class BEReplicationService extends BaseService {

    private static final String[] OWNED_COMMANDS = new String[] {
	BEReplicationSlice.BECOME_MASTER,
	BEReplicationSlice.IS_MASTER
    };

    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);

	myContainer = (BackEndContainer)ac;

	myMediatorID = p.getParameter(Profile.BE_MEDIATOR_ID, null);
	myReplicaIndex = p.getParameter(Profile.BE_REPLICA_INDEX, "0");

	// Create a local slice
	localSlice = new ServiceComponent(p);

	// Create the command sinks
	sourceSink = new CommandSourceSink();

	// Create the command filters
	outFilter = new OutgoingCommandFilter();

    }

    public String getName() {
	return BEReplicationSlice.NAME;
    }

    public Class getHorizontalInterface() {
	try {
	    return Class.forName(BEReplicationSlice.NAME + "Slice");
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
	    return null;
	}
    }

    public Sink getCommandSink(boolean side) {
	if(side == Sink.COMMAND_SOURCE) {
	    return sourceSink;
	}
	else {
	    return null;
	}
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }

    public void boot(Profile p) throws ServiceException {

	try {

	    // Create an empty list
	    includeAll = new LinkedList();

	    // Create a singleton list 
	    excludeMyself = new LinkedList();
	    excludeMyself.add(getLocalNode().getName());
	    if(myReplicaIndex.equals("0")) {

		// This is the master slice: initialize the replicas
		// array and wait for it to be filled by registering
		// replicas through acceptReplica() calls...

		expectedReplicas = Integer.parseInt(p.getParameter(BackEndContainer.BE_REPLICAS_SIZE, "0"));
		myReplicasArray = new String[expectedReplicas + 1];
		masterSliceName = getLocalNode().getName();
		myReplicasArray[0] = masterSliceName;
	    }
	    else {

		// Attach to the master node
		String localNodeName = getLocalNode().getName();
		masterSliceName = p.getParameter(Profile.MASTER_NODE_NAME, null);
		BEReplicationSlice masterSlice = (BEReplicationSlice)getSlice(masterSliceName);
		try {
		    masterSlice.acceptReplica(localNodeName, myReplicaIndex);
		}
		catch(IMTPException imtpe) {
		    // Try again with a newer slice
		    masterSlice = (BEReplicationSlice)getFreshSlice(masterSliceName);
		    masterSlice.acceptReplica(localNodeName, myReplicaIndex);
		}
	    }

	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("An error occurred during service startup.", imtpe);
	}

    }


    private class CommandSourceSink implements Sink {

	public void consume(VerticalCommand cmd) {
	    try {
		String name = cmd.getName();
		if(name.equals(BEReplicationSlice.BECOME_MASTER)) {
		    handleBecomeMaster(cmd);
		}
		else if(name.equals(BEReplicationSlice.IS_MASTER)) {
		    cmd.setReturnValue(new Boolean(handleIsMaster(cmd)));
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(new UnreachableException("Remote container is unreachable", imtpe));
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(new UnreachableException("A Service Exception occurred", se));
	    }
	}


	// Vertical command handler methods

	private void handleBecomeMaster(VerticalCommand cmd) throws IMTPException, ServiceException {
	    GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_SETMASTER, BEReplicationSlice.NAME, null);
	    hCmd.addParam(getLocalNode().getName());
	    broadcastToReplicas(hCmd, includeAll);
	}

	private boolean handleIsMaster(VerticalCommand cmd) throws IMTPException, ServiceException {
	    return masterSliceName.equals(getLocalNode().getName());
	}


    } // End of OutgoingCommandSink class


    private class OutgoingCommandFilter implements Filter {

	public void accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
		    handleBornFEAgent(cmd);
		}
		else if(name.equals(jade.core.management.AgentManagementSlice.INFORM_KILLED)) {
		    handleDeadFEAgent(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(AuthException ae) {
		cmd.setReturnValue(ae);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }
	}


	private void handleBornFEAgent(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, AuthException, ServiceException {

	    Object[] params = cmd.getParams();

	    AID agentID = (AID)params[0];

	    GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_BORNAGENT, BEReplicationSlice.NAME, null);
	    hCmd.addParam(agentID);

	    broadcastToReplicas(hCmd, excludeMyself);
	}


	private void handleDeadFEAgent(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, AuthException, ServiceException {

	    Object[] params = cmd.getParams();

	    AID agentID = (AID)params[0];

	    GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_DEADAGENT, BEReplicationSlice.NAME, null);
	    hCmd.addParam(agentID);

	    broadcastToReplicas(hCmd, excludeMyself);
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


    } // End of OutgoingCommandFilter class


    private class ServiceComponent implements Service.Slice, NodeEventListener {

	public ServiceComponent(Profile p) {
	}

	private void attachTo(BEReplicationSlice slice) throws IMTPException, ServiceException {

	    // Stop the previous monitor, if any
	    if(nodeMonitor != null) {
		nodeMonitor.stop();
	    }

	    // Avoid monitoring yourself
	    if(getLocalNode().getName().equals(slice.getNode().getName())) {
		return;
	    }

	    // Set up a failure monitor on the target slice...
	    nodeMonitor = new NodeFailureMonitor(slice.getNode(), this);
	    Thread monitorThread = new Thread(nodeMonitor);
	    monitorThread.start();

	}

	// Implementation of the Service.Slice interface

	public Service getService() {
	    return BEReplicationService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return BEReplicationService.this.getLocalNode();
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
	    }
	}

	public VerticalCommand serve(HorizontalCommand cmd) {
	    VerticalCommand result = null;
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(BEReplicationSlice.H_ACCEPTREPLICA)) {
		    String sliceName = (String)params[0];
		    String replicaIndex = (String)params[1];
		    acceptReplica(sliceName, replicaIndex);
		}
		else if(cmdName.equals(BEReplicationSlice.H_SETMASTER)) {
		    String name = (String)params[0];
		    setMaster(name);
		}
		else if(cmdName.equals(BEReplicationSlice.H_SETREPLICAS)) {
		    String[] replicas = (String[])params[0];
		    setReplicas(replicas);
		}
		else if(cmdName.equals(BEReplicationSlice.H_GETLABEL)) {
		    cmd.setReturnValue(getLabel());
		}
		else if(cmdName.equals(BEReplicationSlice.H_ADDREPLICA)) {
		    String sliceName = (String)params[0];
		    addReplica(sliceName);
		}
		else if(cmdName.equals(BEReplicationSlice.H_REMOVEREPLICA)) {
		    String name = (String)params[0];
		    removeReplica(name);
		}
		else if(cmdName.equals(BEReplicationSlice.H_BORNAGENT)) {
		    AID name = (AID)params[0];
		    bornAgent(name);
		}
		else if(cmdName.equals(BEReplicationSlice.H_DEADAGENT)) {
		    AID name = (AID)params[0];
		    deadAgent(name);
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
		if(result != null) {
		    result.setReturnValue(t);
		}
	    }
	    finally {
		return result;
	    }
	}


	private void acceptReplica(String sliceName, String replicaIndex) throws IMTPException, ServiceException {
	    int idx = Integer.parseInt(replicaIndex);
	    myReplicasArray[idx] = sliceName;
	    expectedReplicas--;

	    // Are all replicas registered?
	    if(expectedReplicas == 0) {

		// Store the replica list
		setReplicas(myReplicasArray);

		// Broadcast the replica list to all of them
		for(int i = 1; i < myReplicasArray.length; i++) {
		    BEReplicationSlice slice = (BEReplicationSlice)getSlice(myReplicasArray[i]);
		    try {
			slice.setReplicas(myReplicasArray);
		    }
		    catch(IMTPException imtpe) {
			// Retry with a newer slice
			slice = (BEReplicationSlice)getFreshSlice(myReplicasArray[i]);
			slice.setReplicas(myReplicasArray);
		    }
		}
	    }
	}

	private void setMaster(String name) throws IMTPException {
	    masterSliceName = name;
	}

	private void setReplicas(String[] replicas) throws IMTPException {

	    // Fill the slice list...
	    myReplicas.clear();
	    for(int i = 0; i < replicas.length; i++) {
		try {
		    myReplicas.add(getSlice(replicas[i]));
		}
		catch(ServiceException se) {
		    se.printStackTrace();
		}
	    }

	    // Start monitoring your neighbour for failure...
	    int idx = Integer.parseInt(myReplicaIndex);
	    BEReplicationSlice targetSlice;
	    try {
		switch(idx) {
		case 0:
		    // First replica: monitor the last replica
		    targetSlice = (BEReplicationSlice)myReplicas.get(myReplicas.size() - 1);
		    break;
		default:
		    // Any other: monitor the immediately previous one
		    targetSlice = (BEReplicationSlice)myReplicas.get(idx - 1);
		    break;
		}

		attachTo(targetSlice);
	    }
	    catch(ServiceException se) {
		throw new IMTPException("Failure in slice lookup", se);
	    }

	}

	private String getLabel() throws IMTPException {
	    return "<" + myMediatorID + ";" + myReplicaIndex + ">" ;
	}

	private void addReplica(String sliceName) throws IMTPException, ServiceException {

	    BEReplicationSlice slice = (BEReplicationSlice)getSlice(sliceName);
	    myReplicas.add(slice);

	    // If first in line, close the ring by monitoring the newly arrived slice
	    if(myReplicaIndex.equals("0")) {
		attachTo(slice);
	    }

	}

	private void removeReplica(String name) throws IMTPException {

	    // A replica slice is dead
	    int index = findReplicaIndex(name);
	    myReplicas.remove(index);

	    int myIndex = Integer.parseInt(myReplicaIndex);
	    if(index < myIndex) {
		myIndex--;
		myReplicaIndex = Integer.toString(myIndex);
	    }

	}

	private void bornAgent(AID name) {
	    BackEndContainer.AgentImage img = myContainer.createAgentImage(name);
	    myContainer.addAgentImage(name, img);
	}

	private void deadAgent(AID name) {
	    myContainer.removeAgentImage(name);
	}

	public void dumpReplicas() {
	    try {
		System.out.println("--- " + getLocalNode().getName() + "[" + myReplicaIndex + "] ---");
		System.out.println("--- Master replica is: " + masterSliceName + " ---");
		System.out.println("--- Replica list ---");
		Object[] slices = myReplicas.toArray();
		for(int i = 0; i < slices.length; i++) {
		    BEReplicationSlice slice = (BEReplicationSlice)slices[i];
		    System.out.println("----- " + slice.getNode().getName() + "[" + i + "] -----");
		}
		System.out.println("--- End ---");
	    }
	    catch(Throwable t) {
		t.printStackTrace();
	    }
	}

	// Implementation of the NodeEventListener interface

	public void nodeAdded(Node n) {
	    // Do nothing...
	}

	public void nodeRemoved(Node n) {
	    try {

		String sliceName = n.getName();

		List l = new LinkedList();
		l.add(getLocalNode().getName());
		l.add(n.getName());

		// Broadcast a 'removeReplica()' method (exclude yourself and the dead node from bcast)
		GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_REMOVEREPLICA, BEReplicationSlice.NAME, null);
		hCmd.addParam(sliceName);
		broadcastToReplicas(hCmd, l);

		removeReplica(sliceName);


		// -- Attach to the new neighbour slice...
		int idx = Integer.parseInt(myReplicaIndex);
		BEReplicationSlice targetSlice;

		switch(idx) {
		case 0:
		    // First replica: monitor the last replica, if there is one
		    if(myReplicas.isEmpty()) {
			return;
		    }
		    targetSlice = (BEReplicationSlice)myReplicas.get(myReplicas.size() - 1);
		    break;
		default:
		    // Any other: monitor the immediately previous one
		    targetSlice = (BEReplicationSlice)myReplicas.get(idx - 1);
		    break;
		}

		attachTo(targetSlice);

	    }
	    catch(IMTPException imtpe) {
		imtpe.printStackTrace();
	    }
	    catch(ServiceException se) {
		se.printStackTrace();
	    }

	}

	public void nodeUnreachable(Node n) {
	    // Do nothing...
	}

	public void nodeReachable(Node n) {
	    // Do nothing...
	}

	private int findReplicaIndex(String name) {
	    for(int i = 0; i < myReplicas.size(); i++) {
		try {
		    BEReplicationSlice slice = (BEReplicationSlice)myReplicas.get(i);
		    String sliceName = slice.getNode().getName();
		    if(sliceName.equals(name)) {
			return i;
		    }
		}
		catch(ServiceException se) {
		    se.printStackTrace();
		}
	    }

	    return -1;
	}

	// The active object monitoring the remote node
	private NodeFailureMonitor nodeMonitor;


    } // End of ServiceComponent class



    private BackEndContainer myContainer;

    private ServiceComponent localSlice;

    private Filter outFilter;

    private Sink sourceSink;


    // Service specific data

    private final List myReplicas = new LinkedList();
    private List includeAll;
    private List excludeMyself;

    private String myMediatorID;
    private String myReplicaIndex;
    private String masterSliceName;

    private int expectedReplicas;
    private String[] myReplicasArray;


    private void broadcastToReplicas(HorizontalCommand cmd, List excludeList) throws IMTPException, ServiceException {

	Object[] slices = myReplicas.toArray();

	String localNodeName = getLocalNode().getName();
	for(int i = 0; i < slices.length; i++) {
	    BEReplicationSlice slice = (BEReplicationSlice)slices[i];

	    String sliceName = slice.getNode().getName();
	    if(excludeList.indexOf(sliceName) == -1) {
		slice.serve(cmd);

		// Check the command return value for exceptions
		Object ret = cmd.getReturnValue();
		if((ret != null) && (ret instanceof Throwable)) {
		    Throwable t = (Throwable)ret;
		    System.out.println("### Exception in broadcasting to slice " + sliceName + ": " + t.getMessage() + " ###");
		}
	    }
	}

    }

}
