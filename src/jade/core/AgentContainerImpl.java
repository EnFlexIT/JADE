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

/*import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import java.net.InetAddress;
import java.net.MalformedURLException;

import java.lang.reflect.*;
*/
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

import jade.core.event.MessageEvent;
import jade.core.event.MessageListener;
import jade.core.event.AgentEvent;
import jade.core.event.AgentListener;

import jade.lang.acl.ACLMessage;

import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLCodec;

import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;

import jade.tools.ToolNotifier; // FIXME: This should not be imported


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

  private static final int CACHE_SIZE = 10;


  // Local agents, indexed by agent name
  private LADT localAgents = new LADT();

  // Agents cache, indexed by agent name
  private AgentCache cachedProxies = new AgentCache(CACHE_SIZE);

  // ClassLoader table, used for agent mobility
  //private Map loaders = new HashMap();

  // This Map holds the mapping between an agent that arrived on this
  // container and the container where its classes can be retrieved
  //private Map sites = new HashMap();

  // The Profile defining the configuration of this Container
  private Profile myProfile;
  
  // The agent platform this container belongs to
  private MainContainer myMain;

  // The IMTP manager, used to access IMTP-dependent functionalities
  private IMTPManager myIMTPManager;
  
  // The Agent Communication Channel, managing the external MTPs.
  private acc myACC;

  // The Object this container delegates all operations related to
  // agent mobility
  private MobilityManager myMobilityManager;
  
  // Unique ID of the platform, used to build the GUID of resident
  // agents.
  private static String platformID;
  private ContainerID myID;

  private List messageListeners;
  private List agentListeners;

  // This monitor is used to hang a remote ping() call from the front
  // end, in order to detect container failures.
  private java.lang.Object pingLock = new java.lang.Object();

  //private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");

  // FIXME: Temporary hack
  //private jade.imtp.rmi.AgentContainerAdapter myAdapter;
  //public jade.imtp.rmi.AgentContainerAdapter getAdapter() {
  //  return myAdapter;
  //}
  
  // Package scoped constructor, so that only the Runtime and Starter
  // classes can actually create a new Agent Container.
  AgentContainerImpl(Profile p) {

    // Set up attributes for agents thread group
    //agentThreads.setMaxPriority(Thread.NORM_PRIORITY);
    myProfile = p;
    
    // FIXME: Temporary hack
    //try {
    //  myAdapter = new jade.imtp.rmi.AgentContainerAdapter(new jade.imtp.rmi.AgentContainerRMIImpl(this));
    //}
    //catch(Exception e) {
    //  e.printStackTrace();
    //}

  }


  // Interface AgentContainer implementation

  public void createAgent(AID agentID, String className, String[] args, boolean startIt) throws IMTPException {

    Agent agent = null;
    try {
        agent = (Agent)Class.forName(new String(className)).newInstance();
        agent.setArguments(args);
    }
    catch(ClassNotFoundException cnfe) {
      System.err.println("Class " + className + " for agent " + agentID + " was not found.");
      return;
    }
    catch( Exception e ){
      e.printStackTrace();
    }

    initAgent(agentID, agent, startIt);
  }

  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws IMTPException {
  	myMobilityManager.createAgent(agentID, serializedInstance, classSite, startIt);
  }
/*
    final AgentContainer ac = classSite;

    class Deserializer extends ObjectInputStream {

      public Deserializer(InputStream inner) throws IOException {
	super(inner);
      }

      protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
	ClassLoader cl = (ClassLoader)loaders.get(ac);
	if(cl == null) {
	  cl = new JADEClassLoader(ac);
	  loaders.put(ac, cl);
	}
	return(cl.loadClass(v.getName()));
      }

    }

    try {
      ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance));

      Agent instance = (Agent)in.readObject();
      // Store the container where the classes for this agent can be
      // retrieved
      sites.put(instance, classSite);
      initAgent(agentID, instance, startIt);

    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
    catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
  }
*/

  // Accepts the fully qualified class name as parameter and searches
  // the class file in the classpath
  public byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException {
  	return myMobilityManager.fetchClassFile(name);
  }
/*  
    name = name.replace( '.' , '/') + ".class";
    InputStream classStream = ClassLoader.getSystemResourceAsStream(name);
    if (classStream == null) 
      throw new ClassNotFoundException();
    try {
      byte[] bytes = new byte[classStream.available()];
      classStream.read(bytes);
      return(bytes);
    } catch (IOException ioe) {
	throw new ClassNotFoundException();
    }
  }
*/

  public void initAgent(AID agentID, Agent instance, boolean startIt) {

    // Subscribe as a listener for the new agent
    instance.setToolkit(this);

    // put the agent in the local table and get the previous one, if any
    Agent previous = localAgents.put(agentID, instance);
    if(startIt) {
      try {
	RemoteProxy rp = myIMTPManager.createAgentProxy(this, agentID);
	myMain.bornAgent(agentID, rp, myID); // RMI call
	instance.powerUp(agentID, ResourceManager.USER_AGENTS);
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
  	myMobilityManager.moveAgent(agentID, where);
  }
/*
  	Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("MoveAgent failed to find " + agentID);
    agent.doMove(where);
  }
*/

  public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
  	myMobilityManager.copyAgent(agentID, where, newName);
  }
/*  
  	Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("CopyAgent failed to find " + agentID);
    agent.doClone(where, newName);
  }
*/

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
		myMobilityManager.handleTransferResult(agentID, result, messages);
  }
/*  
  	synchronized(localAgents) {
      Agent agent = localAgents.get(agentID);
      if((agent == null)||(agent.getState() != Agent.AP_TRANSIT)) {
	throw new NotFoundException("postTransferResult() unable to find a suitable agent.");
      }
      if(result == TRANSFER_ABORT)
	localAgents.remove(agentID);
      else {
	// Insert received messages at the start of the queue
	for(int i = messages.size(); i > 0; i--)
	  agent.putBack((ACLMessage)messages.get(i - 1));
	agent.powerUp(agentID, ResourceManager.USER_AGENTS);
      }
    }
  }
*/

  /**
    @param snifferName The Agent ID of the sniffer to send messages to.
    @param toBeSniffed The <code>AID</code> of the agent to be sniffed
  **/
  public void enableSniffer(AID snifferName, AID toBeSniffed) throws IMTPException {

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

  }


  /**
    @param snifferName The Agent ID of the sniffer to send messages to.
    @param notToBeSniffed The <code>AID</code> of the agent to stop sniffing
  **/
  public void disableSniffer(AID snifferName, AID notToBeSniffed) throws IMTPException {
    ToolNotifier tn = findNotifier(snifferName);
    if(tn != null) { // The sniffer must be here
      tn.removeObservedAgent(notToBeSniffed);
      if(tn.isEmpty()) {
	removeMessageListener(tn);
	tn.doDelete();
      }
    }

  }


  /**
    @param debuggerName The Agent ID of the debugger to send messages to.
    @param toBeDebugged The <code>AID</code> of the agent to start debugging.
  **/
  public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
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

  }

  /**
    @param debuggerName The Agent ID of the debugger to send messages to.
    @param notToBeDebugged The <code>AID</code> of the agent to stop debugging.
  **/
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
    ToolNotifier tn = findNotifier(debuggerName);
    if(tn != null) { // The debugger must be here
      tn.removeObservedAgent(notToBeDebugged);
      if(tn.isEmpty()) {
	removeMessageListener(tn);
	removeAgentListener(tn);
	tn.doDelete();
      }
    }
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

  }

  public String installMTP(String address, String className) throws IMTPException, MTPException {
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
  }

  public void uninstallMTP(String address) throws IMTPException, NotFoundException, MTPException {
    myACC.removeMTP(address);
    myMain.deadMTP(address, myID);
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
  		myIMTPManager.remotize(this);
  		
  		// Get the Main
      myMain = myProfile.getMain();

      // This string will be used to build the GUID for every agent on
      // this platform.
      platformID = myMain.getPlatformName();

      // Build the Agent IDs for the AMS and for the Default DF.
      Agent.initReservedAIDs(new AID("ams", AID.ISLOCALNAME), new AID("df", AID.ISLOCALNAME));

      // Create the ACC.
      myACC = myProfile.getAcc();

      // Create and initialize the MobilityManager.
      myMobilityManager = myProfile.getMobilityManager();
      myMobilityManager.initialize(myProfile, this, localAgents);
      
      // Initialize the Container ID and register to the platform
      // FIXME: ContainerID should be modified so that to take
      // a list of addresses
      TransportAddress addr = (TransportAddress) myIMTPManager.getLocalAddresses().get(0);
      myID = new ContainerID("No-Name", addr);
      myMain.register(this, myID);

      // Install MTPs and ACLCodecs. Must be done after registering with the Main
      myACC.initialize(this, myProfile);
      
    }
    catch(IMTPException re) {
      System.err.println("Communication failure while contacting agent platform.");
      re.printStackTrace();
      Runtime.instance().endContainer();
    }
    catch(Exception e) {
      System.err.println("Some problem occurred while contacting agent platform.");
      e.printStackTrace();
      Runtime.instance().endContainer();
    }

    // Create and activate agents that must be launched at bootstrap
    try {
	List l = myProfile.getSpecifiers(Profile.AGENTS);
    	Iterator agentSpecifiers = l.iterator();
    	while(agentSpecifiers.hasNext()) {
    		Specifier s = (Specifier) agentSpecifiers.next();
      
      	AID agentID = new AID(s.getName(), AID.ISLOCALNAME);
      	try {
	  createAgent(agentID, s.getClassName(), s.getArgs(), NOSTART);
	  RemoteProxy rp = myIMTPManager.createAgentProxy(this, agentID);
	  myMain.bornAgent(agentID, rp, myID);
      	}
      	catch(IMTPException re) { // It should never happen as this is a local call
        	re.printStackTrace();
      	}
      	catch(NameClashException nce) {
        	System.out.println("Agent name already in use: " + nce.getMessage());
        	// FIXME: If we have two agents with the same name among the initial 
        	// agents, the second one replaces the first one, but then a 
        	// NameClashException is thrown --> both agents are removed even if
        	// the platform believes that the first on is alive.
        	localAgents.remove(agentID);
      	}
    	}

    	// Now activate all agents (this call starts their embedded threads)
    	AID[] allLocalNames = localAgents.keys();
    	for(int i = 0; i < allLocalNames.length; i++) {
      	AID id = allLocalNames[i];
      	Agent agent = localAgents.get(id);
      	agent.powerUp(id, ResourceManager.USER_AGENTS);
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

    // Remove all agents
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
      // Deregister this container from the platform
      myMain.deregister(this);


      // Unblock threads hung in ping() method (this will deregister the container)
      synchronized(pingLock) {
	pingLock.notifyAll();
      }

      myIMTPManager.unremotize(this); 
      myIMTPManager.shutDown();
    }
    catch(IMTPException imtpe) {
      imtpe.printStackTrace();
    }


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

    //System.out.println("Sniffer to Notify- sender: "+ sniffersToNotify.size());
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
    fireSentMessage(msg, msg.getSender());

  }

  public void handlePosted(AID agentID, ACLMessage msg) {
    firePostedMessage(msg, agentID);
  }

  public void handleReceived(AID agentID, ACLMessage msg) {
    fireReceivedMessage(msg, agentID);
  }

  public void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {
    fireChangedAgentState(agentID, from, to);
  }

  public void handleStart(String localName, Agent instance) {
    AID agentID = new AID(localName, AID.ISLOCALNAME);
    initAgent(agentID, instance, START);
  }

  public void handleEnd(AID agentID) {
    try {
      localAgents.remove(agentID);
      myMain.deadAgent(agentID); // RMI call
      cachedProxies.remove(agentID); // FIXME: It shouldn't be needed
    }
    catch(IMTPException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  public void handleMove(AID agentID, Location where) {
    myMobilityManager.handleMove(agentID, where);
  }
/*
    // Mutual exclusion with dispatch() method
    synchronized(localAgents) {
      try {
        String proto = where.getProtocol();
	if(!proto.equalsIgnoreCase(ContainerID.DEFAULT_IMTP))
	  throw new NotFoundException("Internal error: Mobility protocol not supported !!!");

	AgentContainer ac = myMain.lookup((ContainerID)where);
	Agent a = localAgents.get(agentID);
	if(a == null)
	  throw new NotFoundException("Internal error: handleMove() called with a wrong name !!!");

	// Handle special 'running to stand still' case
	if(where.getName().equalsIgnoreCase(myID.getName())) {
	  a.doExecute();
	  return;
	}

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	  ObjectOutputStream encoder = new ObjectOutputStream(out);
	  encoder.writeObject(a);
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

	byte[] bytes = out.toByteArray();
    // Gets the container where the agent classes can be retrieved
    AgentContainer classSite = (AgentContainer) sites.get(a);
    if (classSite == null) {    // The agent was born on this container
      classSite = this;
    } 
	ac.createAgent(agentID, bytes, classSite, NOSTART);

	// Perform an atomic transaction for agent identity transfer
	boolean transferResult = myMain.transferIdentity(agentID, myID, (ContainerID)where);
	List messages = new ArrayList();
	if(transferResult == TRANSFER_COMMIT) {

	  // Send received messages to the destination container
	  Iterator i = a.messages();
	  while(i.hasNext())
	    messages.add(i.next());
	  ac.postTransferResult(agentID, transferResult, messages);

	  // From now on, messages will be routed to the new agent
	  a.doGone();
	  localAgents.remove(agentID);
	  cachedProxies.remove(agentID); // FIXME: It shouldn't be needed
      sites.remove(a);
	}
	else {
	  a.doExecute();
	  ac.postTransferResult(agentID, transferResult, messages);
	}
      }
      catch(IMTPException re) {
	re.printStackTrace();
	// FIXME: Complete undo on exception
	Agent a = localAgents.get(agentID);
	if(a != null)
	  a.doDelete();
      }
      catch(NotFoundException nfe) {
	nfe.printStackTrace();
	// FIXME: Complete undo on exception
	Agent a = localAgents.get(agentID);
	if(a != null)
	  a.doDelete();
      }
    }
  }
*/

  public void handleClone(AID agentID, Location where, String newName) {
  	myMobilityManager.handleClone(agentID, where, newName);
  }
/*  
    try {
      String proto = where.getProtocol();
      if(!proto.equalsIgnoreCase(ContainerID.DEFAULT_IMTP))
	throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
      AgentContainer ac = myMain.lookup((ContainerID)where);
      Agent a = localAgents.get(agentID);
      if(a == null)
	throw new NotFoundException("Internal error: handleCopy() called with a wrong name !!!");

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
	ObjectOutputStream encoder = new ObjectOutputStream(out);
	encoder.writeObject(a);
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }

      AID newID = new AID(newName, AID.ISLOCALNAME);
      byte[] bytes = out.toByteArray();
      // Gets the container where the agent classes can be retrieved
      AgentContainer classSite = (AgentContainer) sites.get(a);
      if (classSite == null) {    // The agent was born on this container
        classSite = this;
      } 
      ac.createAgent(newID, bytes, classSite, START);

    }
    catch(IMTPException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }
*/
  // Private methods

    /**
     * This method is used by the class AID in order to get the HAP.
     **/
  static String getPlatformID()
  {
  	return platformID;
  }

  private void unicastPostMessage(ACLMessage msg, AID receiverID) {

    AgentProxy ap = cachedProxies.get(receiverID);
    if(ap != null) { // Cache hit :-)
      try {
	ap.dispatch(msg);
      }
      catch(NotFoundException nfe) { // Stale cache entry
	cachedProxies.remove(receiverID);
	dispatchUntilOK(msg, receiverID);
      }
    }
    else { // Cache miss :-(
      dispatchUntilOK(msg, receiverID);
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

  private void dispatchUntilOK(ACLMessage msg, AID receiverID) {
    boolean ok;
    int i = 0;
    do {
      AgentProxy proxy;
      try {
	proxy = getFreshProxy(receiverID);
      }
      catch(NotFoundException nfe) { // Agent not found in GADT: error !!!
	System.err.println("Agent " + receiverID.getLocalName() + " was not found on agent platform.");
	System.err.println("Message from platform was: " + nfe.getMessage());
	notifyFailureToSender(msg, new InternalError("LocalAgentNotFound"));
	return;
      }
      try {
	proxy.dispatch(msg);
	cachedProxies.put(receiverID, proxy);
	ok = true;
      }
      catch(acc.NoMoreAddressesException nmae) { // The AID has no more valid addresses
	System.err.println("Agent " + receiverID.getLocalName() + " has no valid addresses.");
	notifyFailureToSender(msg, new InternalError("RemoteAgentNotFound"));
	return;
      }      
      catch(acc.UnknownACLEncodingException uae) { // No ACLcodec available 
	System.err.println(uae.getMessage()+" - message is undeliverable to " + receiverID.getLocalName());
	notifyFailureToSender(msg, new InternalError("NoACLCodec_Available"));
	return;
      }

      catch(NotFoundException nfe) { // Agent not found in destination LADT: need to recheck GADT
	ok = false;
      }
      /*
      i++;
      if(i > 100) { // Watchdog counter...
	System.out.println("===================================================================");
	System.out.println(" Possible livelock in message dispatching:");
	System.out.println(" Receiver is:");
	receiverID.toText(new java.io.OutputStreamWriter(System.out));
	System.out.println();
	System.out.println();
	System.out.println(" Message is:");
	msg.toText(new java.io.OutputStreamWriter(System.out));
	System.out.println();
	System.out.println();
	System.out.println("===================================================================");
	try {
	  Thread.sleep(3000);
	}
	catch(InterruptedException ie) {
	  System.out.println("Interrupted !!!");
	}
	return;
      }
      */
    } while(!ok);
  }

 // Tells whether the given AID refers to an agent of this platform
  // or not.
  private boolean livesHere(AID id) {
    String hap = id.getHap();
    return hap.equalsIgnoreCase(platformID);
  }

  private AgentProxy getFreshProxy(AID id) throws NotFoundException {
    AgentProxy result = null;
  if(livesHere(id)) { // the receiver agent lives in this platform...
      // Look first in local agents
      Agent a = localAgents.get(id);
      if(a != null) {
	result = new LocalProxy(localAgents, id);
      }
      else { // Agent is not local
      
	// Maybe it's registered with this AP on some other container...
        try {
	  result = myMain.getProxy(id); // RMI call
	}
	catch(IMTPException re) {
	  System.out.println("Communication error while contacting agent platform");
	  System.out.print("Trying to reconnect... ");
	  try {
	    // restoreMainContainer();
	    result = myMain.getProxy(id); // RMI call
	    System.out.println("OK.");
	  }
	  catch(IMTPException rex) {
	    throw new NotFoundException("The Main Container is unreachable (again).");
	  }
	}

      }
    }
    else { // It lives outside: then it's a job for the ACC...
      result = myACC.getProxy(id);
    }

    return result;

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

  private ToolNotifier findNotifier(AID observerName) {
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

}
