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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.Iterator;
import jade.util.leap.HashMap;
import jade.util.leap.Set;

import jade.lang.acl.ACLMessage;

import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;
import jade.mtp.TransportAddress;

//__JADE_ONLY__BEGIN
import jade.security.AgentPrincipal;
//__JADE_ONLY__END


/**
   This class is a concrete implementation of the JADE agent
   container, providing runtime support to JADE agents.

   This class cannot be instantiated from applications. Instead, the
   <code>Runtime.createAgentContainer(Profile p)</code> method must be called.

   @see Runtime#createAgentContainer(Profile)

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class AgentContainerImpl implements AgentContainer, AgentToolkit {

  // Local agents, indexed by agent name
  private LADT localAgents = new LADT();

  // The Profile defining the configuration of this Container
  private Profile myProfile;
  
  // The agent platform this container belongs to
  private Platform myPlatform;

  // The IMTP manager, used to access IMTP-dependent functionalities
  private IMTPManager myIMTPManager;
  
  // The Agent Communication Channel, managing the external MTPs.
  private acc myACC;

  // The Object managing all operations related to agent mobility
  // in this container
  private MobilityManager myMobilityManager;
  
  // The Object managing Thread resources in this container
  private ResourceManager myResourceManager;
  
  // The Object managing all operations related to event notification
  // in this container
  private NotificationManager myNotificationManager;
  
  // Unique ID of the platform, used to build the GUID of resident
  // agents.
  private static String platformID;
  private ContainerID myID;

  // This monitor is used to hang a remote ping() call from the front
  // end, in order to detect container failures.
  private Object pingLock = new Object();

  //private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");

  // Package scoped constructor, so that only the Runtime  
  // class can actually create a new Agent Container.
  AgentContainerImpl(Profile p) {

    // Set up attributes for agents thread group
    //agentThreads.setMaxPriority(Thread.NORM_PRIORITY);
    myProfile = p;
  }


  // /////////////////////////////////////////
  // AgentContainer INTERFACE
  // /////////////////////////////////////////
  public void createAgent(AID agentID, String className, Object[] args, boolean startIt) throws IMTPException {

    Agent agent = null;
    try {
        agent = (Agent)Class.forName(new String(className)).newInstance();
				agent.setArguments(args);
    }
    catch(ClassNotFoundException cnfe) {
      System.err.println("Class " + className + " for agent " + agentID + " was not found.");
  		throw new IMTPException("Remote exception is: "+cnfe.getClass().getName());
    }
    catch( Exception e ){
      e.printStackTrace();
  		throw new IMTPException("Remote exception is: "+e.getClass().getName());
    }

    initAgent(agentID, agent, startIt);
  }

  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException {
  	// Delegate the operation to the MobilityManager
  	try {
	  	myMobilityManager.createAgent(agentID, serializedInstance, classSite, startIt);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  		throw new IMTPException("Remote exception is: "+e.getClass().getName());
  	}
  }
  
  // Accepts the fully qualified class name as parameter and searches
  // the class file in the classpath
  public byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException {
  	// Delegate the operation to the MobilityManager
  	return myMobilityManager.fetchClassFile(name);
  }

  public void initAgent(AID agentID, Agent instance, boolean startIt) {

    // Subscribe as a listener for the new agent
    instance.setToolkit(this);

    // put the agent in the local table and get the previous one, if any
    Agent previous = localAgents.put(agentID, instance);
    if(startIt) {
      try {
	myPlatform.bornAgent(agentID, myID);
	instance.powerUp(agentID, myResourceManager);
      }
      catch(NameClashException nce) {
	System.out.println("Agentname already in use:"+nce.getMessage());
	localAgents.remove(agentID);
	if (previous != null) {
		localAgents.put(agentID,previous);
	}
      }
      catch(IMTPException re) {
	System.out.println("Communication error while adding a new agent to the platform.");
	re.printStackTrace();
	localAgents.remove(agentID);
      }
      catch(NotFoundException nfe) {
	System.out.println("This container does not appear to be registered with the main container.");
	localAgents.remove(agentID);
      }
    }
  }

  public void suspendAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("SuspendAgent failed to find " + agentID);
    agent.doSuspend();
  }

  public void resumeAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("ResumeAgent failed to find " + agentID);
    agent.doActivate();
  }

//__JADE_ONLY__BEGIN
  public void changeAgentPrincipal(AID agentID, AgentPrincipal newPrincipal) throws IMTPException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if (agent == null)
      throw new NotFoundException("ChangeAgentPrincipal failed to find " + agentID);
    agent.setPrincipal(newPrincipal);
  }
//__JADE_ONLY__END

  public void waitAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("WaitAgent failed to find " + agentID);
    agent.doWait();
  }

  public void wakeAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("WakeAgent failed to find " + agentID);
    agent.doWake();
  }

  public void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
  	// Delegate the operation to the MobilityManager
  	myMobilityManager.moveAgent(agentID, where);
  }

  public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
  	// Delegate the operation to the MobilityManager
  	myMobilityManager.copyAgent(agentID, where, newName);
  }

  public void killAgent(AID agentID) throws IMTPException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("KillAgent failed to find " + agentID);
    agent.doDelete();
  }

  public void exit() throws IMTPException {
    shutDown();
	}

  public void postTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
  	// Delegate the operation to the MobilityManager
		myMobilityManager.handleTransferResult(agentID, result, messages);
  }

  /**
    @param snifferName The Agent ID of the sniffer to send messages to.
    @param toBeSniffed The <code>AID</code> of the agent to be sniffed
  **/
  public void enableSniffer(AID snifferName, AID toBeSniffed) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.enableSniffer(snifferName, toBeSniffed);
		/*
    ToolNotifier tn = findNotifier(snifferName);
    if(tn != null && tn.getState() == Agent.AP_DELETED) { // A formerly dead notifier
      removeMessageListener(tn);
      tn = null;
    }
    if(tn == null) { // New sniffer
      tn = new ToolNotifier(snifferName);
      AID id = new AID(snifferName.getLocalName() + "-on-" + myID.getName(), AID.ISLOCALNAME);
      initAgent(id, tn, START);
      addMessageListener(tn);
    }
    tn.addObservedAgent(toBeSniffed);
		*/
  }


  /**
    @param snifferName The Agent ID of the sniffer to send messages to.
    @param notToBeSniffed The <code>AID</code> of the agent to stop sniffing
  **/
  public void disableSniffer(AID snifferName, AID notToBeSniffed) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.disableSniffer(snifferName, notToBeSniffed);
  	/*
    ToolNotifier tn = findNotifier(snifferName);
    if(tn != null) { // The sniffer must be here
      tn.removeObservedAgent(notToBeSniffed);
      if(tn.isEmpty()) {
	removeMessageListener(tn);
	tn.doDelete();
      }
    }
		*/
  }


  /**
    @param debuggerName The Agent ID of the debugger to send messages to.
    @param toBeDebugged The <code>AID</code> of the agent to start debugging.
  **/
  public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.enableDebugger(debuggerName, toBeDebugged);
  	/*
    ToolNotifier tn = findNotifier(debuggerName);
    if(tn != null && tn.getState() == Agent.AP_DELETED) { // A formerly dead notifier
      removeMessageListener(tn);
      // removeAgentListener(tn);
      tn = null;
    }
    if(tn == null) { // New debugger
      tn = new ToolNotifier(debuggerName);
      AID id = new AID(debuggerName.getLocalName() + "-on-" + myID.getName(), AID.ISLOCALNAME);
      initAgent(id, tn, START);
      addMessageListener(tn);
      addAgentListener(tn);
    }
    tn.addObservedAgent(toBeDebugged);

    //  FIXME: Need to send a complete, transactional snapshot of the
    //  agent state.
    Agent a = localAgents.get(toBeDebugged);
    AgentState as = a.getAgentState();
    fireChangedAgentState(toBeDebugged, as, as);
		*/
  }

  /**
    @param debuggerName The Agent ID of the debugger to send messages to.
    @param notToBeDebugged The <code>AID</code> of the agent to stop debugging.
  **/
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
  	// Delegate the operation to the NotificationManager
  	myNotificationManager.disableDebugger(debuggerName, notToBeDebugged); 
  	/*
    ToolNotifier tn = findNotifier(debuggerName);
    if(tn != null) { // The debugger must be here
      tn.removeObservedAgent(notToBeDebugged);
      if(tn.isEmpty()) {
	removeMessageListener(tn);
	removeAgentListener(tn);
	tn.doDelete();
      }
    }
    */
  }

  public void dispatch(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException {

    // Mutual exclusion with handleMove() method
    synchronized(localAgents) {
      Agent receiver = localAgents.get(receiverID);

      if(receiver == null) {
	throw new NotFoundException("DispatchMessage failed to find " + receiverID);
      }

      receiver.postMessage(msg);
    }

  }

  public void ping(boolean hang) throws IMTPException {
    if(hang) {
      synchronized(pingLock) {
	try {
	  pingLock.wait();
	}
	catch(InterruptedException ie) {
	  // Do nothing
	}
      }
    }
  }

  public void installACLCodec(String className) throws jade.lang.acl.ACLCodec.CodecException {
  	myACC.addACLCodec(className);
  }

  public MTPDescriptor installMTP(String address, String className) throws IMTPException, MTPException {
  	MTPDescriptor result = myACC.addMTP(className, address);
  	
    // Add the address of the new MTP to the AIDs of all local agents
    Agent[] allLocalAgents = localAgents.values();
  	for(int i = 0; i < allLocalAgents.length; i++) {
	  String[] addrs = result.getAddresses();
	  allLocalAgents[i].addPlatformAddress(addrs[0]);
  	}

  	myPlatform.newMTP(result, myID);
  	return result;
  }

  public void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException {
    MTPDescriptor mtp = myACC.removeMTP(address);
    
    // Remove the address of the old MTP to the AIDs of all local agents
    Agent[] allLocalAgents = localAgents.values();
    for(int i = 0; i < allLocalAgents.length; i++) {
      allLocalAgents[i].removePlatformAddress(address);
    }

    myPlatform.deadMTP(mtp, myID);
  }

  public void updateRoutingTable(int op, MTPDescriptor mtp, AgentContainer ac) throws IMTPException {
    Agent[] allLocalAgents = localAgents.values();
    switch(op) {
    case ADD_RT:
      myACC.addRoute(mtp, ac);
      // Add the address of the new MTP to the AIDs of all local agents
      for(int i = 0; i < allLocalAgents.length; i++) {
	String[] addrs = mtp.getAddresses();
	allLocalAgents[i].addPlatformAddress(addrs[0]);	
      }
      break;
    case DEL_RT:
      myACC.removeRoute(mtp, ac);
      // Remove the address of the old MTP to the AIDs of all local agents
      for(int i = 0; i < allLocalAgents.length; i++) {
	String[] addrs = mtp.getAddresses();
	allLocalAgents[i].removePlatformAddress(addrs[0]);
      }
      break;
    }

  }

  public void routeOut(ACLMessage msg, AID receiver, String address) throws IMTPException, MTPException {
    myACC.forwardMessage(msg, receiver, address);
  }


  void routeIn(ACLMessage msg, AID receiver) {
    unicastPostMessage(msg, receiver);
  }

  void joinPlatform() {
  	try {
  		// Create and initialize the IMTPManager
  		myIMTPManager = myProfile.getIMTPManager();
  		myIMTPManager.initialize(myProfile);
  		
  		// Make itself accessible from remote JVMs
  		myIMTPManager.remotize(this);
  		
  		// Get the Main
      myPlatform = myProfile.getPlatform();

      // This string will be used to build the GUID for every agent on
      // this platform.
      platformID = myPlatform.getPlatformName();

      // Build the Agent IDs for the AMS and for the Default DF.
      Agent.initReservedAIDs(new AID("ams", AID.ISLOCALNAME), new AID("df", AID.ISLOCALNAME));

      // Create the ResourceManager
      myResourceManager = myProfile.getResourceManager();
      
      // Create and initialize the NotificationManager
      myNotificationManager = myProfile.getNotificationManager();
      myNotificationManager.initialize(this, localAgents);
      
      // Create and initialize the MobilityManager.
      myMobilityManager = myProfile.getMobilityManager();
      myMobilityManager.initialize(myProfile, this, localAgents);
      
      // Create the ACC.
      myACC = myProfile.getAcc();

      // Initialize the Container ID
      TransportAddress addr = (TransportAddress) myIMTPManager.getLocalAddresses().get(0);
      myID = new ContainerID("No-Name", addr);
      
      // Register to the platform. If myPlatform is the real MainContainerImpl
      // this call also starts the AMS and DF
      myPlatform.register(this, myID);

      // Install MTPs and ACLCodecs. Must be done after registering with the Main
      myACC.initialize(this, myProfile);
    }
    catch(IMTPException re) {
      System.err.println("Communication failure while contacting agent platform.");
      re.printStackTrace();
      Runtime.instance().endContainer();
      return;
    }
    catch(Exception e) {
      System.err.println("Some problem occurred while contacting agent platform.");
      e.printStackTrace();
      Runtime.instance().endContainer();
      return;
    }

    // Create and activate agents that must be launched at bootstrap
    try {
			List l = myProfile.getSpecifiers(Profile.AGENTS);
    	Iterator agentSpecifiers = l.iterator();
    	while(agentSpecifiers.hasNext()) {
	  Specifier s = (Specifier) agentSpecifiers.next();
      
	  AID agentID = new AID(s.getName(), AID.ISLOCALNAME);
	  try {
	    try {
	      createAgent(agentID, s.getClassName(), s.getArgs(), NOSTART);
	    }
	    catch (IMTPException imtpe) {
	      // The call to createAgent() in this case is local --> no need to
	      // print the exception again. Just skip this agent
	      continue;
	    }
	    myPlatform.bornAgent(agentID, myID);
	  }
	  catch(IMTPException imtpe1) {
	    imtpe1.printStackTrace();
	    localAgents.remove(agentID);
	  }
	  catch(NameClashException nce) {
	    System.out.println("Agent name already in use: " + nce.getMessage());
	    // FIXME: If we have two agents with the same name among the initial 
	    // agents, the second one replaces the first one, but then a 
	    // NameClashException is thrown --> both agents are removed even if
	    // the platform "believes" that the first on is alive.
	    localAgents.remove(agentID);
	  }
	  catch(NotFoundException nfe) {
	    System.out.println("This container does not appear to be registered with the main container.");
	    localAgents.remove(agentID);
	  }
    	}

    	// Now activate all agents (this call starts their embedded threads)
    	AID[] allLocalNames = localAgents.keys();
    	for(int i = 0; i < allLocalNames.length; i++) {
      	AID id = allLocalNames[i];
      	Agent agent = localAgents.get(id);
      	agent.powerUp(id, myResourceManager);
    	}
    }
    catch (ProfileException pe) {
    	System.out.println("Warning: error reading initial agents");
    }
    	
    System.out.println("Agent container " + myID + " is ready.");
  }


  public void shutDown() {
    // Close down the ACC
    myACC.shutdown();

    // Remove all non-system agents 
    Agent[] allLocalAgents = localAgents.values();

    for(int i = 0; i < allLocalAgents.length; i++) {
      // Kill agent and wait for its termination
      Agent a = allLocalAgents[i];

      // Skip the Default DF and the AMS
      AID id = a.getAID();
      if(id.equals(Agent.AMS) || id.equals(Agent.DEFAULT_DF))
        continue;

      a.doDelete();
      a.join();
      a.resetToolkit();
    }

    try {
      // Deregister this container from the platform.
    	// If this is the Main Container this call also stop the AMS and DF
      myPlatform.deregister(this);

      // Unblock threads hung in ping() method (this will deregister the container)
      synchronized(pingLock) {
				pingLock.notifyAll();
      }

  		// Make itself no longer accessible from remote JVMs
      myIMTPManager.unremotize(this); 
    }
    catch(IMTPException imtpe) {
      imtpe.printStackTrace();
    }


    // Releases Thread resources
    myResourceManager.releaseResources();
    // Destroy the (now empty) thread groups
    //try {
    //  agentThreads.destroy();
    //}
    //catch(IllegalThreadStateException itse) {
			//System.out.println("Active threads in 'JADE-Agents' thread group:");
			//agentThreads.list();
    //}
    //finally {
    //  agentThreads = null;
    //}

    // Notify the JADE Runtime that the container has terminated
    // execution
    Runtime.instance().endContainer();

  }


  // Implementation of AgentToolkit interface

  public Location here() {
    return myID;
  }

  public void handleSend(ACLMessage msg) {

    // 26-Mar-2001. The receivers set into the Envelope of the message, 
    // if present, must have precedence over those set into the ACLMessage.
    // If no :intended-receiver parameter is present in the Envelope, 
    // then the :to parameter
    // is used to generate :intended-receiver field. 
    //
    // create an Iterator with all the receivers to which the message must be 
    // delivered
    Iterator it=null;
    Envelope env = msg.getEnvelope();
    if(env != null) {
      it = env.getAllIntendedReceiver();
      if((it != null) && (it.hasNext()) ) {
	//System.out.println("WARNING: Envelope.intendedReceiver taking precedence over ACLMessage.to");
	// ok. use the intendedreceiver
      }
      else {
	it = env.getAllTo();
	if((it != null) && (it.hasNext())) {
	  //System.out.println("WARNING: Envelope.to taking precedence over ACLMessage.to");
	  // ok. use the :to
	  // FIXME. Should I copy all the :to values in the :IntendedReceiver?
	}
	else {
	  it = msg.getAllReceiver();
	  // ok. use the receivers set in the ACLMessage
	}
      }
    }
    else 
      it = msg.getAllReceiver(); //use the receivers set in the ACLMessage
    if(it == null)
      return; // No Message is sent in this case because no receiver was found
    // now it contains the Iterator with all the receivers of this message
    // Iterator it = msg.getAllReceiver();
    while(it.hasNext()) {
      AID dest = (AID)it.next();
      ACLMessage copy = (ACLMessage)msg.clone();
      unicastPostMessage(copy, dest);
    }

    // Notify message listeners
    //fireSentMessage(msg, msg.getSender());
    myNotificationManager.fireEvent(NotificationManager.SENT_MESSAGE,
    	new Object[]{msg, msg.getSender()});

  }

  public void handlePosted(AID agentID, ACLMessage msg) {
    //firePostedMessage(msg, agentID);
    myNotificationManager.fireEvent(NotificationManager.POSTED_MESSAGE,
    	new Object[]{msg, agentID});
  }

  public void handleReceived(AID agentID, ACLMessage msg) {
    //fireReceivedMessage(msg, agentID);
    myNotificationManager.fireEvent(NotificationManager.RECEIVED_MESSAGE,
    	new Object[]{msg, agentID});
  }

//__JADE_ONLY__BEGIN
  public void handleChangedAgentPrincipal(AID agentID, AgentPrincipal oldPrincipal, AgentPrincipal newPrincipal) {
    myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_PRINCIPAL,
    	new Object[]{agentID, oldPrincipal, newPrincipal});
    try {
      myPlatform.changedAgentPrincipal(agentID, oldPrincipal, newPrincipal);
    }
    catch(IMTPException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }
//__JADE_ONLY__END

  public void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {
    //fireChangedAgentState(agentID, from, to);
    myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_STATE,
    	new Object[]{agentID, from, to});
    if (to.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
      try {
        myPlatform.suspendedAgent(agentID);
      }
      catch(IMTPException re) {
        re.printStackTrace();
      }
      catch(NotFoundException nfe) {
        nfe.printStackTrace();
      }
    }
    else if (from.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
      try {
        myPlatform.resumedAgent(agentID);
      }
      catch(IMTPException re) {
        re.printStackTrace();
      }
      catch(NotFoundException nfe) {
        nfe.printStackTrace();
      }
    }
  }

  public void handleStart(String localName, Agent instance) {
    AID agentID = new AID(localName, AID.ISLOCALNAME);
    initAgent(agentID, instance, START);
  }

  public void handleEnd(AID agentID) {
    try {
      localAgents.remove(agentID);
      myPlatform.deadAgent(agentID);
    }
    catch(IMTPException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  public void handleMove(AID agentID, Location where) {
  	// Delegate the operation to the MobilityManager
    myMobilityManager.handleMove(agentID, where);
  }

  public void handleClone(AID agentID, Location where, String newName) {
  	// Delegate the operation to the MobilityManager
  	myMobilityManager.handleClone(agentID, where, newName);
  }

	public void setPlatformAddresses(AID id) {
		myACC.setPlatformAddresses(id);
	}
	
	
  // Private methods

    /**
     * This method is used by the class AID in order to get the HAP.
     **/
  static String getPlatformID()
  {
  	return platformID;
  }

  private void unicastPostMessage(ACLMessage msg, AID receiverID) {

    try {
      if(livesHere(receiverID)) {
				// Dispatch it through the MainContainerProxy
				myPlatform.dispatch(msg, receiverID);
      }
      else {
				// Dispatch it through the ACC
				myACC.dispatch(msg, receiverID);
      }
    }
    catch(NotFoundException nfe) {
      notifyFailureToSender(msg, new InternalError("\"Agent not found: " + nfe.getMessage()+"\""));
    }

  }

    /**
     * This private method is used internally by the platform in order
     * to notify the sender of a message that a failure was reported by
     * the Message Transport Service.
     **/
    private void notifyFailureToSender(ACLMessage msg, InternalError ie) {
	//if (the sender is not the AMS and the performative is not FAILURE)
	if ( (msg.getSender()==null) || ((msg.getSender().equals(Agent.getAMS())) && (msg.getPerformative()==ACLMessage.FAILURE))) // sanity check to avoid infinte loops
	    return;
	// else send back a failure message
	ACLMessage failure = msg.createReply();
	failure.setPerformative(ACLMessage.FAILURE);
	//System.err.println(failure.toString());
	failure.setSender(Agent.getAMS());
	// FIXME the content is not completely correct, but that should
	// also avoid creating wrong content
	String content = "( (action " + msg.getSender().toString();
	content = content + " ACLMessage ) "+ie.getMessage()+")" ;
	failure.setContent(content);
	handleSend(failure);
    }


 	// Tells whether the given AID refers to an agent of this platform
  // or not.
  private boolean livesHere(AID id) {
    String hap = id.getHap();
    return CaseInsensitiveString.equalsIgnoreCase(hap, platformID);
  }
  
  LADT getLocalAgents() {
  	return localAgents;
  }

}
