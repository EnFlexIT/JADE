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
import jade.mtp.TransportAddress;

//import jade.tools.ToolNotifier; // FIXME: This should not be imported


/**
   This class is a concrete implementation of the JADE agent
   container, providing runtime support to JADE agents.

   This class cannot be instantiated from applications. Instead, the
   <code>Runtime.createAgentContainer(Profile p)</code> method must be called.

   @see Runtime#createAgentContainer(Profile p);

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

  private List messageListeners;
  private List agentListeners;

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
      throw new IMTPException("ClassNotFoundException", cnfe);
    }
    catch( Exception e ){
      e.printStackTrace();
      throw new IMTPException("Unexpected Exception", e);
    }

    initAgent(agentID, agent, startIt);
  }

  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException {
  	// Delegate the operation to the MobilityManager
  	myMobilityManager.createAgent(agentID, serializedInstance, classSite, startIt);
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
  	/*
    try{
      Class c = Class.forName(className);
      ACLCodec codec = (ACLCodec)c.newInstance(); 
      myACC.addACLCodec(codec);
      System.out.println("Installed "+ codec.getName()+ " ACLCodec implemented by " + className +"\n");
      // FIXME: notify the AMS of the new Codec to update the APDescritption.
    }
    catch(ClassNotFoundException cnfe){
      throw new jade.lang.acl.ACLCodec.CodecException("ERROR: The class " +className +" for the ACLCodec not found.",cnfe);
    }
    catch(InstantiationException ie) {
      throw new jade.lang.acl.ACLCodec.CodecException("The class " + className + " raised InstantiationException (see NestedException)",ie);
    }
    catch(IllegalAccessException iae) {
      throw new jade.lang.acl.ACLCodec.CodecException("The class " + className  + " raised IllegalAccessException (see nested exception)", iae);
    }
		*/
  }

  public String installMTP(String address, String className) throws IMTPException, MTPException {
  	String result = myACC.addMTP(className, address);
  	myPlatform.newMTP(result, myID);
  	return result;
  	/*
  	try {
      Class c = Class.forName(className);
      MTP proto = (MTP)c.newInstance();
      TransportAddress ta = myACC.addMTP(proto, address);
      String result = proto.addrToStr(ta);
      myMain.newMTP(result, myID);
      return result;
    }
    catch(ClassNotFoundException cnfe) {
      throw new MTPException("ERROR: The class " + className + " for the " + address  + " MTP was not found");
    }
    catch(InstantiationException ie) {
      throw new MTPException("The class " + className + " raised InstantiationException (see nested exception)", ie);
    }
    catch(IllegalAccessException iae) {
      throw new MTPException("The class " + className  + " raised IllegalAccessException (see nested exception)", iae);
    }
    */
  }

  public void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException {
    myACC.removeMTP(address);
    myPlatform.deadMTP(address, myID);
  }

  public void updateRoutingTable(int op, String address, AgentContainer ac) throws IMTPException {
    switch(op) {
    case ADD_RT:
      myACC.addRoute(address, ac);
      break;
    case DEL_RT:
      myACC.removeRoute(address, ac);
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

  public void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {
    //fireChangedAgentState(agentID, from, to);
    myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_STATE,
    	new Object[]{agentID, from, to});
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
      notifyFailureToSender(msg, new InternalError("Agent not found: " + nfe.getMessage()));
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

  /*
  private void restoreMainContainer() throws NotFoundException {
    try {
      myMain = lookup3(platformRMI);

      // Register again with the Main Container.
      String myName = myMain.addContainer(this, myID); // RMI call
      myID.setName(myName);

      ACLMessage regMsg = new ACLMessage(ACLMessage.REQUEST);
      regMsg.setSender(Agent.getAMS());
      regMsg.addReceiver(Agent.getAMS());
      regMsg.setLanguage(jade.lang.sl.SL0Codec.NAME);
      regMsg.setOntology(jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NAME);
      regMsg.setProtocol("fipa-request");

      // Restore Main Container state of agents and containers
      AID[] agentIDs = localAgents.keys();
      for(int i = 0; i < agentIDs.length; i++) {

	AID agentID = agentIDs[i];

	// Register again the agent with the Main Container.
	RemoteContainerProxy rp = new RemoteContainerProxy(this, agentID);
	try {
	  myMain.bornAgent(agentID, rp, myID); // RMI call
	}
	catch(NameClashException nce) {
	  throw new NotFoundException("Agent name already in use: "+ nce.getMessage());
	}

	String content = "((action (agent-identifier :name " + Agent.getAMS().getName() + " ) (register (ams-agent-description :name (agent-identifier :name " + agentID.getName() + " ) :ownership JADE :state active ) ) ))";
	// Register again the agent with the AMS
	regMsg.setContent(content);
	unicastPostMessage(regMsg, Agent.getAMS());

      }

      // Register again all MTPs with the Main Container
      List localAddresses = theACC.getLocalAddresses();
      for(int i = 0; i < localAddresses.size(); i++) {
	myMain.newMTP((String)localAddresses.get(i), myID);
      }

    }
    catch(IMTPException re) {
      System.out.println("The Main Container is down again. Aborting this send operation.");
      throw new NotFoundException("The Main Container is unreachable.");
    }
    catch(NotBoundException nbe) {
      nbe.printStackTrace();
      throw new NotFoundException("The Main Container is not bound with the RMI registry.");
    }
    catch(MalformedURLException murle) {
      murle.printStackTrace();
    }

  }
*/

  /*private ToolNotifier findNotifier(AID observerName) {
    if(messageListeners == null)
      return null;
    Iterator it = messageListeners.iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(obj instanceof ToolNotifier) {
	ToolNotifier tn = (ToolNotifier)obj;
	AID id = tn.getObserver();
	if(id.equals(observerName))
	  return tn;
      }
    }
    return null;

  }
	

  // This lock is used to synchronize operations on the message
  // listeners list. Using lazy processing (the list is set to null
  // when empty) the space overhead is reduced, even with this lock
  // object (an empty LinkedList holds three null pointers).
  private Object messageListenersLock = new Object();

  private void addMessageListener(MessageListener l) {
    synchronized(messageListenersLock) {
      if(messageListeners == null)
	messageListeners = new LinkedList();
      messageListeners.add(l);
    }
  }

  private void removeMessageListener(MessageListener l) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
	messageListeners.remove(l);
	if(messageListeners.isEmpty())
	  messageListeners = null;
      }
    }
  }

  private void fireSentMessage(ACLMessage msg, AID sender) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
	MessageEvent ev = new MessageEvent(MessageEvent.SENT_MESSAGE, msg, sender, myID);
	for(int i = 0; i < messageListeners.size(); i++) {
	  MessageListener l = (MessageListener)messageListeners.get(i);
	  l.sentMessage(ev);
	}
      }
    }
  }

  private void firePostedMessage(ACLMessage msg, AID receiver) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
	MessageEvent ev = new MessageEvent(MessageEvent.POSTED_MESSAGE, msg, receiver, myID);
	for(int i = 0; i < messageListeners.size(); i++) {
	  MessageListener l = (MessageListener)messageListeners.get(i);
	  l.postedMessage(ev);
	}
      }
    }
  }

  private void fireReceivedMessage(ACLMessage msg, AID receiver) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
	MessageEvent ev = new MessageEvent(MessageEvent.RECEIVED_MESSAGE, msg, receiver, myID);
	for(int i = 0; i < messageListeners.size(); i++) {
	  MessageListener l = (MessageListener)messageListeners.get(i);
	  l.receivedMessage(ev);
	}
      }
    }
  }

  private void fireRoutedMessage(ACLMessage msg, Channel from, Channel to) {
    synchronized(messageListenersLock) {
      if(messageListeners != null) {
	MessageEvent ev = new MessageEvent(MessageEvent.ROUTED_MESSAGE, msg, from, to, myID);
	for(int i = 0; i < messageListeners.size(); i++) {
	  MessageListener l = (MessageListener)messageListeners.get(i);
	  l.routedMessage(ev);
	}
      }
    }
  }


  // This lock is used to synchronize operations on the agent
  // listeners list. Using lazy processing (the list is set to null
  // when empty) the space overhead is reduced, even with this lock
  // object (an empty LinkedList holds three null pointers).
  private Object agentListenersLock = new Object();

  private void addAgentListener(AgentListener l) {
    synchronized(messageListenersLock) {
      if(agentListeners == null)
	agentListeners = new LinkedList();
      agentListeners.add(l);
    }
  }

  private void removeAgentListener(AgentListener l) {
    synchronized(messageListenersLock) {
      if(agentListeners != null) {
	agentListeners.remove(l);
	if(agentListeners.isEmpty())
	  agentListeners = null;
      }
    }
  }

  private void fireChangedAgentState(AID agentID, AgentState from, AgentState to) {
    synchronized(messageListenersLock) {
      if(agentListeners != null) {
	AgentEvent ev = new AgentEvent(AgentEvent.CHANGED_AGENT_STATE, agentID, from, to, myID);
	for(int i = 0; i < agentListeners.size(); i++) {
	  AgentListener l = (AgentListener)agentListeners.get(i);
	  l.changedAgentState(ev);
	}
      }
    }
  }
	*/
}
