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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.StringWriter;

import java.net.InetAddress;
import java.net.MalformedURLException;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;

import jade.lang.acl.*;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, AgentToolkit {

  private static final int CACHE_SIZE = 10;

  // Local agents, indexed by agent name
  protected LADT localAgents = new LADT();

  // Agents cache, indexed by agent name
  private AgentCache cachedProxies = new AgentCache(CACHE_SIZE);

  // ClassLoader table, used for agent mobility
  private Map loaders = new HashMap();

  // The agent platform this container belongs to
  protected MainContainer myPlatform;

  protected String myName;

  // FIXME: Temporary hack...
  String getName() {
    return myName;
  }

  // The Agent Communication Channel, managing the external MTPs.
  protected acc theACC;

  // Unique ID of the platform, used to build the GUID of resident
  // agents.
  protected String platformID;

  private Map SniffedAgents = new HashMap();
  private String theSniffer;           

  // This monitor is used to hang a remote ping() call from the front
  // end, in order to detect container failures.
  private java.lang.Object pingLock = new java.lang.Object();

  private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");
  private ThreadGroup criticalThreads = new ThreadGroup("JADE time-critical threads");

  public AgentContainerImpl(String args[]) throws RemoteException {

    // Configure Java runtime system to put the whole host address in RMI messages
    try {
      System.getProperties().put("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
    }
    catch(java.net.UnknownHostException jnue) {
      jnue.printStackTrace();
    }

    // Set up attributes for agents thread group
    agentThreads.setMaxPriority(Thread.NORM_PRIORITY);

    // Set up attributes for time critical threads
    criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);

    // Initialize timer dispatcher
    TimerDispatcher td = new TimerDispatcher();
    Thread t = new Thread(criticalThreads, td);
    t.setPriority(criticalThreads.getMaxPriority());

    td.setThread(t);
    // This call starts the timer dispatcher thread
    Agent.setDispatcher(td);

  }


  // Interface AgentContainer implementation

  public void createAgent(AID agentID, String className, boolean startIt) throws RemoteException {

    Agent agent = null;
    try {
      agent = (Agent)Class.forName(new String(className)).newInstance();
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

  public void createAgent(AID agentID, byte[] serializedInstance, AgentContainer classSite, boolean startIt) throws RemoteException {

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
      initAgent(agentID, instance, startIt);

    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
    catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }

  }

  // Accepts the fully qualified class name as parameter and searches
  // the class file in the classpath
  public byte[] fetchClassFile(String name) throws RemoteException, ClassNotFoundException {
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

  void initAgent(AID agentID, Agent instance, boolean startIt) {

    // Subscribe as a listener for the new agent
    instance.setToolkit(this);

    // put the agent in the local table and get the previous one, if any
    Agent previous = localAgents.put(agentID, instance);
    if(startIt) {
      try {
	RemoteProxyRMI rp = new RemoteProxyRMI(this, agentID);
	myPlatform.bornAgent(agentID, rp, myName); // RMI call
	instance.powerUp(agentID, agentThreads);
      }
      catch(NameClashException nce) {
	System.out.println("Agentname already in use:"+nce.getMessage());
	localAgents.remove(agentID);
	localAgents.put(agentID,previous);
      }
      catch(RemoteException re) {
	System.out.println("Communication error while adding a new agent to the platform.");
	re.printStackTrace();
      }
    }
  }

  public void suspendAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("SuspendAgent failed to find " + agentID);
    agent.doSuspend();
  }

  public void resumeAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("ResumeAgent failed to find " + agentID);
    agent.doActivate();
  }

  public void waitAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("WaitAgent failed to find " + agentID);
    agent.doWait();
  }

  public void wakeAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("WakeAgent failed to find " + agentID);
    agent.doWake();
  }

  public void moveAgent(AID agentID, Location where) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent==null)
      throw new NotFoundException("MoveAgent failed to find " + agentID);
    agent.doMove(where);
  }

  public void copyAgent(AID agentID, Location where, String newName) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("CopyAgent failed to find " + agentID);
    agent.doClone(where, newName);
  }

  public void killAgent(AID agentID) throws RemoteException, NotFoundException {
    Agent agent = localAgents.get(agentID);
    if(agent == null)
      throw new NotFoundException("KillAgent failed to find " + agentID);
    agent.doDelete();
  }

  public void exit() throws RemoteException {
    shutDown();
    System.exit(0);
  }

  public void postTransferResult(AID agentID, boolean result, List messages) throws RemoteException, NotFoundException {
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
	agent.powerUp(agentID, agentThreads);
      }
    }
  }

  protected String getCorrectName(String name) {
    String correctName = null;
    /*
    int atPos = name.indexOf('@');
    if ( atPos == -1 ) {
      // it is a local name: we simply force lowercase
      correctName = name.toLowerCase();
    }
    else {
      String agentAddress = name.substring(atPos+1,name.length());
      // System.out.println("Address: " + agentAddress);
      if (agentAddress.equalsIgnoreCase(platformAddress)) {
	correctName = name.substring(0,atPos).toLowerCase();
      }
      else {
	correctName = name.toLowerCase();
      }
    }
    */
    return correctName;
  }

  public void enableSniffer(AID snifferName, Iterator toBeSniffed) throws RemoteException {
    System.out.println("AgentContainerImpl::enableSniffer() called.");
    /* In the SniffedAgents hashmap the key is the agent name and the value is a list
       containing the sniffer names for that agent */
      /*
    String currentAgent = null;

    Set sniffedAgentsSet = ToBeSniffed.keySet();
    Iterator sniffedAgentsIt = sniffedAgentsSet.iterator();

    if (SniffedAgents.size() > 0) {
      // the list is not empty
      while (sniffedAgentsIt.hasNext()){
	currentAgent = (String)sniffedAgentsIt.next();
	if (SniffedAgents.containsKey(getCorrectName(currentAgent))) {
	  // there is at least one sniffer name for this agent
	  List curVector = (List)SniffedAgents.get(getCorrectName(currentAgent));
	  if (!curVector.contains(getCorrectName(SnifferName))) {
	    // we add it only if there isn't one
	    curVector.add(getCorrectName(SnifferName));
	  }
	}
	else {
	  // there is no sniffer for that agent
	  List curVector = new ArrayList(3);
	  curVector.add(getCorrectName(SnifferName));
	  SniffedAgents.put(getCorrectName(currentAgent),curVector);
	}				
      }
    }
    else {
      // the list is empty
      while (sniffedAgentsIt.hasNext()) {
	currentAgent = (String)sniffedAgentsIt.next();
	List curVector = new ArrayList(3);
	curVector.add(getCorrectName(SnifferName));
	SniffedAgents.put(getCorrectName(currentAgent),curVector);
      }	
    }
      */
  }


  public void disableSniffer(AID snifferName, Iterator notToBeSniffed) throws RemoteException {
    System.out.println("AgentContainerImpl::disableSniffer() called.");
    /* In the SniffedAgents hashmap the key is the agent name and the value is a vector
       containing the sniffer names for that agent */
      /*
    String currentAgent = null;
	
    Set sniffedAgentsSet = NotToBeSniffed.keySet();
    Iterator sniffedAgentsIt = sniffedAgentsSet.iterator();	

    if (SniffedAgents.size() > 0) {
      // the list is not empty
      while (sniffedAgentsIt.hasNext()){
	currentAgent = (String)sniffedAgentsIt.next();
			
	if (SniffedAgents.containsKey(getCorrectName(currentAgent))) {
	  // there is at least one sniffer name for this agent
	  List curVector = (List)SniffedAgents.get(getCorrectName(currentAgent));
	  if (curVector.contains(getCorrectName(SnifferName))) {
	    // we add it only if there isn't one
	    curVector.remove(getCorrectName(SnifferName));
	    if (curVector.size() == 0)
	      SniffedAgents.remove(getCorrectName(currentAgent));
	  }
	}

      }
    }
      */
  }



  public void dispatch(ACLMessage msg, AID receiverID) throws RemoteException, NotFoundException {

    // Mutual exclusion with handleMove() method
    synchronized(localAgents) {
      Agent receiver = localAgents.get(receiverID);

      if(receiver == null) {
	throw new NotFoundException("DispatchMessage failed to find " + receiverID);
      }

      receiver.postMessage(msg);
    }

  }

  public void ping(boolean hang) throws RemoteException {
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

  public void joinPlatform(String pID, List agentNamesAndClasses) {

    // This string will be used to build the GUID for every agent on this platform.
    platformID = pID;

    // Build the Agent IDs for the AMS and for the Default DF.
    Agent.initReservedAIDs(globalAID("ams"), globalAID("df"));

    try {
      // Retrieve agent platform from RMI registry and register as agent container
      String platformRMI = "rmi://" + platformID;
      myPlatform = lookup3(platformRMI);

      InetAddress netAddr = InetAddress.getLocalHost();
      myName = myPlatform.addContainer(this, netAddr); // RMI call

      theACC = new acc(this, myPlatform);
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while contacting agent platform.");
      re.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Some problem occurred while contacting agent platform.");
      e.printStackTrace();
    }

    /* Create all agents and set up necessary links for message passing.

       The link chain is:
       a) Agent 1 -> AgentContainer1 -- Through CommEvent
       b) AgentContainer 1 -> AgentContainer 2 -- Through RMI (cached or retreived from MainContainer)
       c) AgentContainer 2 -> Agent 2 -- Through postMessage() (direct insertion in message queue, no events here)

       agentNamesAndClasses is a List of String containing, orderly, the name of an agent and the name of Agent
       concrete subclass which implements that agent.
    */
    for(int i=0; i < agentNamesAndClasses.size(); i += 2) {
      String agentName = (String)agentNamesAndClasses.get(i);
      String agentClass = (String)agentNamesAndClasses.get(i+1);

      AID agentID = globalAID(agentName);
      try {
	createAgent(agentID, agentClass, NOSTART);
	RemoteProxyRMI rp = new RemoteProxyRMI(this, agentID);
	myPlatform.bornAgent(agentID, rp, myName);
      }
      catch(RemoteException re) { // It should never happen
	re.printStackTrace();
      }
      catch(NameClashException nce) {
	System.out.println("Agent name already in use: "+nce.getMessage());
	localAgents.remove(agentID);
      }

    }

    // Now activate all agents (this call starts their embedded threads)
    AID[] allLocalNames = localAgents.keys();
    for(int i = 0; i < allLocalNames.length; i++) {
      AID id = allLocalNames[i];
      Agent agent = localAgents.get(id);
      agent.powerUp(id, agentThreads);
    }

    System.out.println("Agent container " + myName + " is ready.");

  }

  public void shutDown() {
    // Shuts down the Timer Dispatcher
    Agent.stopDispatcher();

    // Remove all agents
    Agent[] allLocalAgents = localAgents.values();

    for(int i = 0; i < allLocalAgents.length; i++) {
      // Kill agent and wait for its termination
      Agent a = allLocalAgents[i];
      a.doDelete();
      a.join();
      a.resetToolkit();
    }

    // Unblock threads hung in ping() method (this will deregister the container)
    synchronized(pingLock) {
      pingLock.notifyAll();
    }

    // Now, close all MTP links to the outside world
    theACC.shutdown();

  }


/*
 * This method returns the vector of the sniffers registered for 
 * theAgent
 */
private List getSniffer(AID id, java.util.Map theMap) {

  List theSniffer = null;
  /*
  String theAgent = id.getName();

  theAgent = getCorrectName(theAgent);
  int atPos = theAgent.indexOf('@');
  if ( atPos == -1 ) {
    // theAgent is a local name
    theSniffer = (List)theMap.get(theAgent);

    // if the search fails let's add the platform address and see if it works
    if ( theSniffer == null ) {
      theSniffer = (List)theMap.get(theAgent + "@" + platformAddress);
    }
  } 
  else { 
    // theAgent is an absolute name
    theSniffer = (List)theMap.get(theAgent);

    // if the search fails let's remove the platform address ad see if it works
    if ( theSniffer == null ) {
      theSniffer = (List)theMap.get(theAgent.substring(0,atPos));
    }
  }
  */
  return theSniffer;
}

  /*
   * Creates the message to be sent to the sniffer. The ontology must be set to 
   * "sniffed-message" otherwise the sniffer doesn't recognize it. The sniffed 
   * message is put in the content field of this message.
   *
   * @param theMsg handler of the sniffed message
   * @param theDests list of the destination (sniffers)
   */
  private void sendMsgToSniffers(ACLMessage theMsg, List theDests){

    AID currentSniffer;

    for (int z = 0; z < theDests.size(); z++) {
      currentSniffer = (AID)theDests.get(z);
      ACLMessage SniffedMessage = new ACLMessage(ACLMessage.INFORM);
      SniffedMessage.clearAllReceiver();
      SniffedMessage.addReceiver(currentSniffer);
      SniffedMessage.setSender(null);
      SniffedMessage.setContent(theMsg.toString());
      SniffedMessage.setOntology("sniffed-message");
      unicastPostMessage(SniffedMessage,currentSniffer);	    
    }
  }


  // Implementation of AgentToolkit interface

  public void handleSend(ACLMessage msg) {

    String currentSniffer;
    List currentSnifferVector;

    boolean sniffedSource = false;

    // The AID of the message sender must have the complete GUID
    AID msgSource = msg.getSender();
    if(!livesHere(msgSource)) {
      String guid = msgSource.getName();
      guid = guid.concat("@" + platformID);
      msgSource.setName(guid);
    }

    currentSnifferVector = getSniffer(msgSource, SniffedAgents);
    if (currentSnifferVector != null) {
      sniffedSource = true;
      sendMsgToSniffers(msg, currentSnifferVector);		
    }

    Iterator it = msg.getAllReceiver();
    while(it.hasNext()) {
      AID dest = (AID)it.next();
      currentSnifferVector = getSniffer(dest, SniffedAgents);	    
      if((currentSnifferVector != null) && (!sniffedSource)) {
	sendMsgToSniffers(msg,currentSnifferVector);	    		
      }


      // If this AID has no explicit addresses, but it does not seem
      // to live here, then the platform ID is appended to the AID
      // name
      Iterator addresses = dest.getAllAddresses();
      if(!addresses.hasNext() && !livesHere(dest)) {
	String guid = dest.getName();
	guid = guid.concat("@" + platformID);
	dest.setName(guid);
      }

      ACLMessage copy = (ACLMessage)msg.clone();
      unicastPostMessage(copy, dest);
    }
  }

  public void handleStart(String localName, Agent instance) {
    AID agentID = globalAID(localName);
    initAgent(agentID, instance, START);
  }

  public void handleEnd(AID agentID) {
    try {
      localAgents.remove(agentID);
      myPlatform.deadAgent(agentID); // RMI call
      cachedProxies.remove(agentID); // FIXME: It shouldn't be needed
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  public void handleMove(AID agentID, Location where) {
    // Mutual exclusion with dispatch() method
    synchronized(localAgents) {
      try {
        String proto = where.getProtocol();
	if(!proto.equalsIgnoreCase("JADE-IPMT"))
	  throw new NotFoundException("Internal error: Mobility protocol not supported !!!");

	String destName = where.getName();
	AgentContainer ac = myPlatform.lookup(destName);
	Agent a = localAgents.get(agentID);
	if(a == null)
	  throw new NotFoundException("Internal error: handleMove() called with a wrong name !!!");

	// Handle special 'running to stand still' case
	if(where.getName().equalsIgnoreCase(myName)) {
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
	ac.createAgent(agentID, bytes, this, NOSTART);

	// Perform an atomic transaction for agent identity transfer
	boolean transferResult = myPlatform.transferIdentity(agentID, myName, destName);
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
	}
	else {
	  a.doExecute();
	  ac.postTransferResult(agentID, transferResult, messages);
	}
      }
      catch(RemoteException re) {
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

  public void handleClone(AID agentID, Location where, String newName) {
    try {
      String proto = where.getProtocol();
      if(!proto.equalsIgnoreCase("JADE-IPMT"))
	throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
      AgentContainer ac = myPlatform.lookup(where.getName());
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

      AID newID = globalAID(newName);
      byte[] bytes = out.toByteArray();
      ac.createAgent(newID, bytes, this, START);

    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  protected AID localAID(String agentName) {
    if(!agentName.endsWith('@' + platformID))
      agentName = agentName.concat('@' + platformID);
    AID id = new AID();
    id.setName(agentName);
    id.clearAllAddresses();
    id.clearAllResolvers();
    return id;
  }

  protected AID globalAID(String agentName) {
    AID id = localAID(agentName);
    // FIXME: Add all platform addresses to this AID
    return id;
  }


  // Private methods

  private boolean livesHere(AID id) {
    String hap = id.getHap();
    return hap.equalsIgnoreCase(platformID);
  }

  // This hack is needed to overcome a bug in java.rmi.Naming class:
  // when an object reference is binded, unbinded and then rebinded
  // with the same URL, the next two lookup() calls will throw an
  // Exception without a reason.
  private MainContainer lookup3(String URL)
    throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
    java.lang.Object o = null;
    try {
      o = Naming.lookup(URL);
    }
    catch(RemoteException re1) { // First one
      try {
	o = Naming.lookup(URL);
      }
      catch(RemoteException re2) { // Second one
	// Third attempt. If this one fails, there's really
	// something wrong, so we let the RemoteException go.
	o = Naming.lookup(URL);
      }
    }
    return (MainContainer)o;
  }


  String getPlatformID()
  {
  	return platformID;
  }
  
  // FIXME: Temporary hack (this should be private)
  void unicastPostMessage(ACLMessage msg, AID receiverID) {

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
	return;
      }
      try {
	proxy.dispatch(msg);
	cachedProxies.put(receiverID, proxy);
	ok = true;
      }
      catch(acc.NoMoreAddressesException nmae) { // The AID has no more valid addresses
	System.err.println("Agent " + receiverID.getLocalName() + " has no valid addresses.");
	return;
      }
      catch(NotFoundException nfe) { // Agent not found in destination LADT: need to recheck GADT
	ok = false;
      }
    } while(!ok);
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
	  result = myPlatform.getProxy(id); // RMI call
	}
	catch(RemoteException re) {
	  System.out.println("Communication error while contacting agent platform");
	  re.printStackTrace();
	}
      }
    }
    else { // It lives outside: then it's a job for the ACC...
    
    	// if the agent apparently does not live here, but it has the same address
    	// of this platform, then maybe the GUID must be updated by concatenating the hap
    	for (Iterator i=id.getAllAddresses(); i.hasNext(); )
    	  if (theACC.isAPlatformAddress((String)i.next())) {
    	  	id.setName(id.getName()+'@'+platformID);
    	  	return getFreshProxy(id);
    	  }
    	// else the agent has no local addresses, then it is surely remote
      result = theACC.getProxy(id);
    }

    return result;

  }

}
