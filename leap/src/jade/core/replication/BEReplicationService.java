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

//#J2ME_EXCLUDE_FILE

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

import jade.security.JADESecurityException;

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
	BEReplicationSlice.IS_MASTER,
	BEReplicationSlice.START_MONITOR,
	BEReplicationSlice.STOP_MONITOR
    };

    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);
	myContainer = (BackEndContainer)ac;
	myProfile = p;

	myMediatorID = p.getParameter(Profile.BE_MEDIATOR_ID, null);
	myReplicaIndex = Integer.parseInt(p.getParameter(Profile.BE_REPLICA_INDEX, "0"));

	// Create a local slice
	localSlice = new ServiceComponent(p);

	// Create the command sinks
	sourceSink = new CommandSourceSink();

	// Create the command filters
	outFilter = new OutgoingCommandFilter();
	inFilter = new IncomingCommandFilter();

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
	    return inFilter;
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

	    myMonitoredReplicaIndex = -1;

	    masterSliceName = p.getParameter(Profile.MASTER_NODE_NAME, null);
	    if(masterSliceName == null) {

		// This is the master slice: initialize the replicas
		// array and wait for it to be filled by registering
		// replicas through acceptReplica() calls...

		expectedReplicas = Integer.parseInt(p.getParameter(BackEndContainer.BE_REPLICAS_SIZE, "0"));
		myReplicaNames = new String[expectedReplicas + 1];
		masterSliceName = getLocalNode().getName();
		myReplicaNames[0] = masterSliceName;
	    }
	    else {

		// Attach to the master slice
		String localNodeName = getLocalNode().getName();

		BEReplicationSlice masterSlice = (BEReplicationSlice)getSlice(masterSliceName);
		try {
		    masterSlice.acceptReplica(localNodeName, Integer.toString(myReplicaIndex));
		}
		catch(IMTPException imtpe) {
		    // Try again with a newer slice
		    masterSlice = (BEReplicationSlice)getFreshSlice(masterSliceName);
		    masterSlice.acceptReplica(localNodeName, Integer.toString(myReplicaIndex));
		}

		booting = false;
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
		else if(name.equals(BEReplicationSlice.GET_MASTER_NAME)) {
		    cmd.setReturnValue(handleGetMasterName(cmd));
		}
		else if(name.equals(BEReplicationSlice.START_MONITOR)) {
		    handleStartMonitor(cmd);
		}
		else if(name.equals(BEReplicationSlice.STOP_MONITOR)) {
		    handleStopMonitor(cmd);
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

	private String handleGetMasterName(VerticalCommand cmd) throws IMTPException, ServiceException {
	    return masterSliceName;
	}

	private void handleStartMonitor(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();

	    long delay = ((Long)params[0]).longValue();
	    startMonitor(delay);
	}

	private void handleStopMonitor(VerticalCommand cmd) {
	    stopMonitor();
	}


    } // End of OutgoingCommandSink class


    private class OutgoingCommandFilter extends Filter {

	public boolean accept(VerticalCommand cmd) {

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
	    catch(JADESecurityException ae) {
		cmd.setReturnValue(ae);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(se);
	    }

	    // Never veto a command
	    return true;
	}


	private void handleBornFEAgent(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException, ServiceException {

	    Object[] params = cmd.getParams();

	    AID agentID = (AID)params[0];

	    GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_BORNAGENT, BEReplicationSlice.NAME, null);
	    hCmd.addParam(agentID);

	    broadcastToReplicas(hCmd, excludeMyself);
	}


	private void handleDeadFEAgent(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException, ServiceException {

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


    private class IncomingCommandFilter extends Filter {

	public boolean accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();

		if(name.equals(jade.core.management.AgentManagementSlice.KILL_CONTAINER)) {
		    handleKillMasterReplica(cmd);
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

	private void handleKillMasterReplica(VerticalCommand cmd) throws IMTPException, ServiceException {

	    // If this is the master replica, shutdown all the other replicas
	    if(masterSliceName.equals(getLocalNode().getName())) {

		// Broadcast a 'exitReplica()' method (exclude yourself)
		GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_EXITREPLICA, BEReplicationSlice.NAME, null);
		broadcastToReplicas(hCmd, excludeMyself);
	    }
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

    } // End of IncomingCommandFilter class


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
      nodeMonitor = NodeFailureMonitor.getFailureMonitor();
	    nodeMonitor.start(slice.getNode(), this);

	}

	private void updateMonitoredSlice() throws IMTPException, ServiceException {
	    // Select the first 'up' slice, counting down from the
	    // immediate preceding neighbor in the ring
	    int monitoredIndex = mod(myReplicaIndex - 1, myReplicas.length);
	    for(int i = 0; i < myReplicas.length - 1; i++) {
		ReplicaInfo info = myReplicas[monitoredIndex];
		if(info.isReachable()) {
		    if(myMonitoredReplicaIndex != monitoredIndex) {
			myMonitoredReplicaIndex = monitoredIndex;
			attachTo(info.getSlice());
		    }
		    return;
		}

		monitoredIndex = mod(monitoredIndex - 1, myReplicas.length);
	    }

	    // All slices are unreachable: stop the previous monitor, if any
	    myMonitoredReplicaIndex = -1;
	    if(nodeMonitor != null) {
		nodeMonitor.stop();
	    }

	}

	// Modulo operation, correctly extended for negative integers.
	public int mod(int num, int radix) {
	    while(num < 0) {
		num += radix;
	    }

	    return num % radix;
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
		    boolean[] status = (boolean[])params[1];
		    setReplicas(replicas, status);
		}
		else if(cmdName.equals(BEReplicationSlice.H_REPLICAUP)) {
		    int index = ((Integer)params[0]).intValue();
		    replicaUp(index);
		}
		else if(cmdName.equals(BEReplicationSlice.H_REPLICADOWN)) {
		    int index = ((Integer)params[0]).intValue();
		    replicaDown(index);
		}
		else if(cmdName.equals(BEReplicationSlice.H_EXITREPLICA)) {
		    exitReplica();
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

            return result;
	}


	private synchronized void acceptReplica(String sliceName, String replicaIndex) throws IMTPException, ServiceException {
	    if(booting) { try {
		int idx = Integer.parseInt(replicaIndex);
		myReplicaNames[idx] = sliceName;
		expectedReplicas--;

		// Are all replicas registered?
		if(expectedReplicas == 0) {
		    booting = false;

		    // Store the replica list, setting all reachability flags to true
		    boolean[] allTrue = new boolean[myReplicaNames.length];
		    for(int i = 0; i < allTrue.length; i++) {
			allTrue[i] = true;
		    }
		    setReplicas(myReplicaNames, allTrue);

		    boolean[] status = getReplicasReachability();

		    // Broadcast the replica list to all of them
		    for(int i = 1; i < myReplicaNames.length; i++) {
			BEReplicationSlice slice = (BEReplicationSlice)getSlice(myReplicaNames[i]);
			try {
			    slice.setReplicas(myReplicaNames, status);
			}
			catch(IMTPException imtpe) {
			    // Retry with a newer slice
			    slice = (BEReplicationSlice)getFreshSlice(myReplicaNames[i]);
			    slice.setReplicas(myReplicaNames, status);
			}
		    }

		    // Start the replica monitor
		    startMonitor(myContainer.REPLICA_CHECK_DELAY.longValue());

		}
	    }catch(Throwable t) { t.printStackTrace();} }
	    else {

		// Update the replica array for the new slice
		BEReplicationSlice slice = (BEReplicationSlice)getFreshSlice(sliceName);
		slice.setReplicas(myReplicaNames, getReplicasReachability());

		// Notify all other slices (yourself included) that the replica is up again
		GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_REPLICAUP, BEReplicationSlice.NAME, null);
		hCmd.addParam(new Integer(replicaIndex));
		List l = new LinkedList();
		l.add(sliceName);
		broadcastToReplicas(hCmd, l);
	    }
	}

	private void setMaster(String name) throws IMTPException {
	    masterSliceName = name;
	}

	private void setReplicas(String[] replicas, boolean[] status) throws IMTPException, ServiceException {
	    myReplicas = new ReplicaInfo[replicas.length];
	    for(int i = 0; i < replicas.length; i++) {
		try {
		    BEReplicationSlice s = (BEReplicationSlice)getSlice(replicas[i]);
		    myReplicas[i] = new ReplicaInfo(replicas[i], s);
		    myReplicas[i].setReachable(status[i]);
		}
		catch(ServiceException se) {
		    se.printStackTrace();
		}
	    }

	    // Start monitoring your neighbour for failure...
	    updateMonitoredSlice();

	    myReplicaNames = replicas;
	}

	private void replicaUp(int index) throws IMTPException, ServiceException {

	    myReplicas[index].setReachable(true);

	    // Check and adjust the monitored slice if necessary...
	    updateMonitoredSlice();

	}

	private void replicaDown(int index) throws IMTPException, ServiceException {

	    myReplicas[index].setReachable(false);

	    // Check and adjust the monitored slice if necessary...
	    updateMonitoredSlice();

	}

	private void exitReplica() throws IMTPException {
	    myContainer.shutDown();
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
		for(int i = 0; i < myReplicas.length; i++) {
		    BEReplicationSlice slice = myReplicas[i].getSlice();
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

		int deadIndex = myMonitoredReplicaIndex;
		replicaDown(deadIndex);

		String sliceName = n.getName();

		List l = new LinkedList();
		l.add(getLocalNode().getName());
		l.add(n.getName());

		// Broadcast a 'replicaDown()' method (exclude yourself from bcast)
		GenericCommand hCmd = new GenericCommand(BEReplicationSlice.H_REPLICADOWN, BEReplicationSlice.NAME, null);
		hCmd.addParam(new Integer(deadIndex));
		broadcastToReplicas(hCmd, l);
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


	// The active object monitoring the remote node
	private NodeFailureMonitor nodeMonitor;


    } // End of ServiceComponent class


    private class ReplicaInfo {

	public ReplicaInfo(String n, BEReplicationSlice s) {
	    name = n;
	    slice = s;
	    reachable = true;
	}

	public String getName() {
	    return name;
	}

	public void setReachable(boolean b) {
	    reachable = b;

	    // Refresh the slice proxy if is reachable again
	    if(reachable) {
		try {
		    slice = (BEReplicationSlice)getFreshSlice(name);
		}
		catch(ServiceException se) {
		    reachable = false;
		}
	    }
	}

	public boolean isReachable() {
	    return reachable;
	}

	public void setSlice(BEReplicationSlice s) {
	    slice = s;
	}

	public BEReplicationSlice getSlice() {
	    return slice;
	}

	private String name;
	private boolean reachable;
	private BEReplicationSlice slice;

    } // End of ReplicaInfo class


    private class ReplicaMonitor implements Runnable {

	public ReplicaMonitor(long millis) {
	    active = true;
	    delayTime = millis;
	}

	public void run() {

	    while(active) {

		// Try to restart all 'down' replicas
		for(int i = 0; i < myReplicas.length; i++) {
		    ReplicaInfo info = myReplicas[i];
		    if(!info.isReachable()) {
			try {
			    System.out.println("### Restarting replica <" + info.getName() + "> ###");
			    myContainer.restartReplica(i);
			}
			catch(IMTPException imtpe) {
			    // Ignore it, and retry later...
			}
		    }
		}

		// Wait for a bit...
		try {
		    synchronized(this) {
			wait(delayTime);
		    }
		}
		catch(InterruptedException ie) {
		    active = false;
		}
	    }

	}

	public void start() {
	    myThread = new Thread(this);
	    myThread.start();
	}

	public synchronized void stop() {
	    active = false;
	    notifyAll();
	}

	private long delayTime;
	private boolean active;
	private Thread myThread;


    } // End of ReplicaMonitor class

    private BackEndContainer myContainer;
    private Profile myProfile; 

    private ServiceComponent localSlice;

    private Filter outFilter;
    private Filter inFilter;

    private Sink sourceSink;


    // Service specific data

    private ReplicaInfo[] myReplicas = new ReplicaInfo[0];
    private ReplicaMonitor myMonitor;

    private List includeAll;
    private List excludeMyself;

    private String myMediatorID;
    private int myReplicaIndex;
    private int myMonitoredReplicaIndex;
    private String masterSliceName;

    private boolean booting = true;
    private int expectedReplicas;
    private String[] myReplicaNames;


    private boolean[] getReplicasReachability() {
	boolean[] result = new boolean[myReplicas.length];
	for(int i = 0; i < myReplicas.length; i++) {
	    result[i] = myReplicas[i].isReachable();
	}

	return result;
    }

    private void broadcastToReplicas(HorizontalCommand cmd, List excludeList) throws IMTPException, ServiceException {

  	String localNodeName = getLocalNode().getName();
	for(int i = 0; i < myReplicas.length; i++) {
	    BEReplicationSlice slice = myReplicas[i].getSlice();
	    boolean reachable = myReplicas[i].isReachable();

	    String sliceName = slice.getNode().getName();
	    if(reachable && (excludeList.indexOf(sliceName) == -1)) {
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

    private void startMonitor(long delay) {
	stopMonitor();

	myMonitor = new ReplicaMonitor(delay);
	myMonitor.start();
    }

    private void stopMonitor() {
	if(myMonitor != null) {
	    myMonitor.stop();
	    myMonitor = null;
	}
    }

}
