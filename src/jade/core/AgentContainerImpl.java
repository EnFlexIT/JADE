/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

import java.io.StringWriter;

import java.net.InetAddress;
import java.net.MalformedURLException;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.omg.CORBA.*;

import jade.lang.acl.*;

import FIPA_Agent_97;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, CommListener {

  private static final int MAP_SIZE = 50;
  private static final float MAP_LOAD_FACTOR = 0.50f;

  // Local agents, indexed by agent name
  protected Map localAgents = new HashMap(MAP_SIZE, MAP_LOAD_FACTOR);

  // Agents cache, indexed by agent name
  private AgentCache cachedProxies = new AgentCache(MAP_SIZE);

  // The agent platform this container belongs to
  protected AgentPlatform myPlatform;

  protected String myName;

  // IIOP address of the platform, will be used for inter-platform communications
  protected String platformAddress;

  protected ORB myORB;

  public Map SniffedAgents = new HashMap();
  public String theSniffer;           

  private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");
  private ThreadGroup criticalThreads = new ThreadGroup("JADE time-critical threads");

  public AgentContainerImpl(String args[]) throws RemoteException {

    // Configure Java runtime system to put the whole host address in RMI messages
    try {
      System.getProperties().put("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
    }
    catch(java.net.UnknownHostException jnue) {
      // Silently ignore it
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


    // Initialize CORBA runtime
    myORB = ORB.init(args, null);

  }


  // Interface AgentContainer implementation

  public void createAgent(String agentName, String className, boolean startIt) throws RemoteException {

    Agent agent = null;
    try {
      agent = (Agent)Class.forName(new String(className)).newInstance();
    }
    catch(ClassNotFoundException cnfe) {
      System.err.println("Class " + className + " for agent " + agentName + " was not found.");
      return;
    }
    catch( Exception e ){
      e.printStackTrace();
    }

    createAgent(agentName, agent, startIt);
  }


  public void createAgent(String agentName, Agent instance, boolean startIt) throws RemoteException {

    // Subscribe as a listener for the new agent
    instance.addCommListener(this);

    // Insert new agent into local agents table
    localAgents.put(agentName.toLowerCase(), instance);

    if(startIt) {
      try {
	RemoteProxyRMI rp = new RemoteProxyRMI(this);
	myPlatform.bornAgent(agentName + '@' + platformAddress, rp, myName); // RMI call
      }
      catch(NameClashException nce) {
	System.out.println("Agent name already in use");
	localAgents.remove(agentName.toLowerCase());
      }
      catch(RemoteException re) {
	System.out.println("Communication error while adding a new agent to the platform.");
	re.printStackTrace();
      }
      instance.powerUp(agentName, platformAddress, agentThreads);
    }
  }

  public void suspendAgent(String agentName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent == null)
      throw new NotFoundException("SuspendAgent failed to find " + agentName);
    agent.doSuspend();
  }

  public void resumeAgent(String agentName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent == null)
      throw new NotFoundException("ResumeAgent failed to find " + agentName);
    agent.doActivate();
  }

  public void waitAgent(String agentName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent==null)
      throw new NotFoundException("WaitAgent failed to find " + agentName);
    agent.doWait();
  }

  public void wakeAgent(String agentName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent==null)
      throw new NotFoundException("WakeAgent failed to find " + agentName);
    agent.doWake();
  }

  public void moveAgent(String agentName, String where) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent==null)
      throw new NotFoundException("MoveAgent failed to find " + agentName);
    agent.doMove(where);
  }

  public void copyAgent(String agentName, String where, String newName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent == null)
      throw new NotFoundException("CopyAgent failed to find " + agentName);
    agent.doClone(where, newName);
  }

  public void killAgent(String agentName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent == null)
      throw new NotFoundException("KillAgent failed to find " + agentName);
    agent.doDelete();
  }

  public void exit() throws RemoteException {
    shutDown();
    System.exit(0);
  }

  public void postTransferResult(String agentName, boolean result, Vector messages) throws RemoteException, NotFoundException {
    synchronized(localAgents) {
      Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
      if((agent == null)||(agent.getState() != Agent.AP_TRANSIT)) {
	throw new NotFoundException("postTransferResult() unable to find a suitable agent.");
      }
      if(result == TRANSFER_ABORT)
	localAgents.remove(agentName.toLowerCase());
      else {
	// Insert received messages at the start of the queue
	for(int i = messages.size(); i > 0; i--)
	  agent.putBack((ACLMessage)messages.elementAt(i - 1));
	agent.powerUp(agentName, platformAddress, agentThreads);
      }
    }
  }

  protected String getCorrectName(String name) {
    String correctName = null;

    int atPos = name.indexOf('@');
    if ( atPos == -1 ) {
      // it is a local name: we simply force lowercase
      correctName = name.toLowerCase();
    }
    else {
      String agentAddress = name.substring(atPos+1,name.length());
      // System.out.println("Address: "+agentAddress);
      if (agentAddress.equalsIgnoreCase(platformAddress)) {
	correctName = name.substring(0,atPos).toLowerCase();
      }
      else {
	correctName = name.toLowerCase();
      }
    }

    return correctName;
  }

  public void enableSniffer(String SnifferName, Map ToBeSniffed) throws RemoteException {
    /* In the SniffedAgents hashmap the key is the agent name and the value is a vector
       containing the sniffer names for that agent */

    String currentAgent = null;
	
    Set sniffedAgentsSet = ToBeSniffed.keySet();
    Iterator sniffedAgentsIt = sniffedAgentsSet.iterator();

    if (SniffedAgents.size() > 0) {
      // the list is not empty
      while (sniffedAgentsIt.hasNext()){
	currentAgent = (String)sniffedAgentsIt.next();
	if (SniffedAgents.containsKey(getCorrectName(currentAgent))) {
	  // there is at least one sniffer name for this agent
	  Vector curVector = (Vector)SniffedAgents.get(getCorrectName(currentAgent));
	  if (!curVector.contains(getCorrectName(SnifferName))) {
	    // we add it only if there isn't one
	    curVector.add(getCorrectName(SnifferName));
	  }
	}
	else {
	  // there is no sniffer for that agent
	  Vector curVector = new Vector(3);
	  curVector.add(getCorrectName(SnifferName));
	  SniffedAgents.put(getCorrectName(currentAgent),curVector);
	}				
      }
    }
    else {
      // the list is empty
      while (sniffedAgentsIt.hasNext()) {
	currentAgent = (String)sniffedAgentsIt.next();
	Vector curVector = new Vector(3);
	curVector.add(getCorrectName(SnifferName));
	SniffedAgents.put(getCorrectName(currentAgent),curVector);
      }	
    }

  }


  public void disableSniffer(String SnifferName, Map NotToBeSniffed) throws RemoteException {
    /* In the SniffedAgents hashmap the key is the agent name and the value is a vector
       containing the sniffer names for that agent */

    String currentAgent = null;
	
    Set sniffedAgentsSet = NotToBeSniffed.keySet();
    Iterator sniffedAgentsIt = sniffedAgentsSet.iterator();	

    if (SniffedAgents.size() > 0) {
      // the list is not empty
      while (sniffedAgentsIt.hasNext()){
	currentAgent = (String)sniffedAgentsIt.next();
			
	if (SniffedAgents.containsKey(getCorrectName(currentAgent))) {
	  // there is at least one sniffer name for this agent
	  Vector curVector = (Vector)SniffedAgents.get(getCorrectName(currentAgent));
	  if (curVector.contains(getCorrectName(SnifferName))) {
	    // we add it only if there isn't one
	    curVector.remove(getCorrectName(SnifferName));
	    if (curVector.size() == 0)
	      SniffedAgents.remove(getCorrectName(currentAgent));
	  }
	}

      }
    }
  }



  public void dispatch(ACLMessage msg) throws RemoteException, NotFoundException {
    String completeName = msg.getFirstDest(); // FIXME: Not necessarily the first one is the right one
    String receiverName = null;
    int atPos = completeName.indexOf('@');
    if(atPos == -1)
      receiverName = completeName;
    else
      receiverName = completeName.substring(0,atPos);

    // Mutual exclusion with moveSource() method
    synchronized(localAgents) {
      Agent receiver = (Agent)localAgents.get(receiverName.toLowerCase());

      if(receiver == null) {
	throw new NotFoundException("DispatchMessage failed to find " + receiverName);
      }

      receiver.postMessage(msg);
    }

  }

  public void ping() throws RemoteException {
  }

  public void joinPlatform(String platformRMI, Vector agentNamesAndClasses) {

     // Retrieve agent platform from RMI registry and register as agent container
    try {
      myPlatform = lookup3(platformRMI);
      myName = myPlatform.addContainer(this); // RMI call
      platformAddress = myPlatform.getAddress(); // RMI call
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
       b) AgentContainer 1 -> AgentContainer 2 -- Through RMI (cached or retreived from AgentPlatform)
       c) AgentContainer 2 -> Agent 2 -- Through postMessage() (direct insertion in message queue, no events here)

       agentNamesAndClasses is a Vector of String containing, orderly, the name of an agent and the name of Agent
       concrete subclass which implements that agent.
    */
    for( int i=0; i < agentNamesAndClasses.size(); i+=2 ) {
      String agentName = (String)agentNamesAndClasses.elementAt(i);
      String agentClass = (String)agentNamesAndClasses.elementAt(i+1);
      try {
	createAgent(agentName, agentClass, NOSTART);
	RemoteProxyRMI rp = new RemoteProxyRMI(this);
	myPlatform.bornAgent(agentName + '@' + platformAddress, rp, myName);
      }
      catch(RemoteException re) { // It should never happen
	re.printStackTrace();
      }
      catch(NameClashException nce) {
	System.out.println("Agent name already in use");
	localAgents.remove(agentName.toLowerCase());
      }

    }

    // Now activate all agents (this call starts their embedded threads)
    Set names = localAgents.keySet();
    Iterator nameList = names.iterator();
    String currentName = null;
    while(nameList.hasNext()) {
      currentName = (String)nameList.next();
      Agent agent = (Agent)localAgents.get(currentName.toLowerCase());
      agent.powerUp(currentName, platformAddress, agentThreads);
    }

    System.out.println("Agent container " + myName + " is ready.");

  }

  public void shutDown() {
    // Shuts down the Timer Dispatcher
    Agent.stopDispatcher();

    // Remove all agents
    Set s = localAgents.keySet();
		java.lang.Object[] allLocalAgents = s.toArray();
    for(int i = 0; i < allLocalAgents.length; i++) {
      String name = (String)allLocalAgents[i];

			// Kill agent and wait for its termination
			Agent a = (Agent)localAgents.get(name);
			a.doDelete();
			a.join();
		}

    try {
			// Deregister itself as a container
      myPlatform.removeContainer(myName); // RMI call
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }

  }



/*
 * This method returns the vector of the sniffers registered for 
 * theAgent
 */
private Vector getSniffer(String theAgent, java.util.Map theMap) {

  Vector theSniffer = null;

  theAgent = getCorrectName(theAgent);
  int atPos = theAgent.indexOf('@');
  if ( atPos == -1 ) {
    // theAgent is a local name
    theSniffer = (Vector)theMap.get(theAgent);

    // if the search fails let's add the platform address and see if it works
    if ( theSniffer == null ) {
      theSniffer = (Vector)theMap.get(theAgent+"@"+platformAddress);
    }
  } 
  else { 
    // theAgent is an absolute name
    theSniffer = (Vector)theMap.get(theAgent);

    // if the search fails let's remove the platform address ad see if it works
    if ( theSniffer == null ) {
      theSniffer = (Vector)theMap.get(theAgent.substring(0,atPos));
    }
  }
  return theSniffer;
}

  /*
   * Creates the message to be sent to the sniffer. The ontology must be set to 
   * "sniffed-message" otherwise the sniffer doesn't recognize it. The sniffed 
   * message is put in the content field of this message.
   *
   * @param theMsg handler of the sniffed message
   * @param theDests vector of the destination (sniffers)
   */
  private void sendMsgToSniffers(ACLMessage theMsg, Vector theDests){

    String currentSniffer;

    for (int z = 0; z < theDests.size(); z++) {
      currentSniffer = (String)theDests.elementAt(z);
      ACLMessage SniffedMessage = new ACLMessage("inform");
      SniffedMessage.removeAllDests();
      SniffedMessage.addDest(currentSniffer);
      SniffedMessage.setSource("ams");
      SniffedMessage.setContent(theMsg.toString());
      SniffedMessage.setOntology("sniffed-message");
      unicastPostMessage(SniffedMessage,currentSniffer);	    
    }
}

  // Implementation of CommListener interface

  public void CommHandle(CommEvent event) {

    // Get ACL message from the event.
    ACLMessage msg = event.getMessage();

    AgentGroup group = null;
    
    String currentSniffer;
    Vector currentSnifferVector;

    if(event.isMulticast()) {
    	
      String msgSource = msg.getSource();
          	
      group = event.getRecipients();
      Enumeration e = group.getMembers();
      while(e.hasMoreElements()) {
	String dest = (String)e.nextElement();				
	ACLMessage copy = (ACLMessage)msg.clone();
	copy.removeAllDests();
	copy.addDest(dest);
	unicastPostMessage(copy, dest);
      }

      currentSnifferVector = getSniffer(msgSource,SniffedAgents);
      if(currentSnifferVector != null){
      	/* Sniffed sender: don't care about receivers */
      	ACLMessage cloned = (ACLMessage)msg.clone();
      	e = group.getMembers();
      	while(e.hasMoreElements()){
	  String dest = (String)e.nextElement();
	  cloned.removeAllDests();
	  cloned.addDest(dest);
	  sendMsgToSniffers(cloned,currentSnifferVector);
      	}
      }
      else {
      	/* The sender is not sniffed: let's look at all the receivers */
      	ACLMessage cloned = (ACLMessage)msg.clone();
      	e = group.getMembers();
      	while(e.hasMoreElements()) {
	  String dest = (String)e.nextElement();
	  currentSnifferVector = getSniffer(dest,SniffedAgents);
	  if (currentSnifferVector != null) {
	    cloned.removeAllDests();
	    cloned.addDest(dest);
	    sendMsgToSniffers(cloned,currentSnifferVector);
	  }
      	}
      }
    }
    else {  // FIXME: This is probably not compliant
      group = msg.getDests();
      
      boolean sniffedSource = false;
      String msgSource = msg.getSource();
      currentSnifferVector = getSniffer(msgSource, SniffedAgents);
      if (currentSnifferVector != null) {
	sniffedSource = true;
	sendMsgToSniffers(msg,currentSnifferVector);		
      }

      Enumeration e = group.getMembers();
      while(e.hasMoreElements()) {
	String dest = (String)e.nextElement();		
	currentSnifferVector = getSniffer(dest, SniffedAgents);	    
	if((currentSnifferVector != null) && (!sniffedSource)) {
	  sendMsgToSniffers(msg,currentSnifferVector);	    		
	}

	ACLMessage copy = (ACLMessage)msg.clone();
	copy.removeAllDests();
	copy.addDest(dest);
	unicastPostMessage(copy, dest);
      }
    }
  }

  public void endSource(String name) {
    try {
      localAgents.remove(name.toLowerCase());
      myPlatform.deadAgent(name + '@' + platformAddress); // RMI call
      cachedProxies.remove(name + '@' + platformAddress); // FIXME: It shouldn't be needed
    }
    catch(RemoteException re) {
      // FIXME: This happens with 'Resource temporarily unavailable'
      // since RMA GUI disposal has been made asynchronous.
      Throwable t = re.detail;
      if(t instanceof java.net.SocketException)
	System.out.println(t.getMessage());
      else
	re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  public void moveSource(String name, String where) {
    try {
      synchronized(localAgents) {
	AgentContainer ac = myPlatform.lookup(where);
	Agent a = (Agent)localAgents.get(name.toLowerCase());
	if(a == null)
	  throw new NotFoundException("Internal error: moveSource() called with a wrong name !!!");

	// Handle special 'running to stand still' case
	if(where.equalsIgnoreCase(myName)) {
	  a.doExecute();
	  return;
	}

	ac.createAgent(name, a, NOSTART);

	// Perform an atomic transaction for agent identity transfer
	boolean transferResult = myPlatform.transferIdentity(name + '@' + platformAddress, myName, where);
	Vector messages = new Vector();
	if(transferResult == TRANSFER_COMMIT) {

	  // Send received messages to the destination container
	  Iterator i = a.messages();
	  while(i.hasNext())
	    messages.add(i.next());
	  ac.postTransferResult(name, transferResult, messages);

	  // From now on, messages will be routed to the new agent
	  a.doGone();
	  localAgents.remove(name.toLowerCase());
	  cachedProxies.remove(name + '@' + platformAddress); // FIXME: It shouldn't be needed
	}
	else {
	  a.doExecute();
	  ac.postTransferResult(name, transferResult, messages);
	}
      }
    }
    catch(RemoteException re) {
      re.printStackTrace();
      // FIXME: Complete undo on exception
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      // FIXME: Complete undo on exception
    }
  }

  public void copySource(String name, String where, String newName) {
    try {
      AgentContainer ac = myPlatform.lookup(where);
      Agent a = (Agent)localAgents.get(name.toLowerCase());
      if(a == null)
	throw new NotFoundException("Internal error: copySource() called with a wrong name !!!");

      ac.createAgent(newName, a, START);
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
    }
  }

  // Private methods


  // This hack is needed to overcome a bug in java.rmi.Naming class:
  // when an object reference is binded, unbinded and then rebinded
  // with the same URL, the next two lookup() calls will throw an
  // Exception without a reason.
  private AgentPlatform lookup3(String URL)
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
    return (AgentPlatform)o;
  }

  private void unicastPostMessage(ACLMessage msg, String completeName) {
    String receiverName = null;
    String receiverAddr = null;

    int atPos = completeName.indexOf('@');
    if(atPos == -1) {
      receiverName = completeName;
      receiverAddr = platformAddress.toLowerCase();
      completeName = completeName.concat('@' + receiverAddr);
    }
    else {
      receiverName = completeName.substring(0,atPos);
      receiverAddr = completeName.substring(atPos + 1);
    }

    AgentProxy ap = cachedProxies.get(completeName);
    if(ap != null) { // Cache hit :-)
      try {
	ap.dispatch(msg);
      }
      catch(NotFoundException nfe) { // Stale cache entry
	cachedProxies.remove(completeName);
	dispatchUntilOK(msg, completeName, receiverName, receiverAddr);
      }
    }
    else { // Cache miss :-(
      dispatchUntilOK(msg, completeName, receiverName, receiverAddr);
    }

  }

  private void dispatchUntilOK(ACLMessage msg, String completeName, String name, String addr) {
    boolean ok;
    int i = 0;
    do {
      AgentProxy proxy;
      try {
	proxy = getFreshProxy(name, addr);
      }
      catch(NotFoundException nfe) { // Agent not found in GADT: error !!!
	System.err.println("Agent " + name + " was not found on agent platform");
	System.err.println("Message from platform was: " + nfe.getMessage());
	return;
      }
      try {
	proxy.dispatch(msg);
	cachedProxies.put(completeName, proxy);
	ok = true;
      }
      catch(NotFoundException nfe) { // Agent not found in destination LADT: need to recheck GADT
	ok = false;
      }
    } while(!ok);
  }

  private AgentProxy getFreshProxy(String name, String addr) throws NotFoundException {
    AgentProxy result = null;

    // Look first in local agents
    Agent a = (Agent)localAgents.get(name.toLowerCase());
    if((a != null)&&(addr.equalsIgnoreCase(platformAddress))) {
      result = new LocalProxy(a);
    }
    else { // Agent is not local

      // Maybe it's registered with this AP on some other container...
      try {
        result = myPlatform.getProxy(name.toLowerCase(), addr.toLowerCase()); // RMI call
      }
      catch(RemoteException re) {
	System.out.println("Communication error while contacting agent platform");
	re.printStackTrace();
      }
      catch(NotFoundException nfe) { // Agent is neither local nor registered with this platform

        // Then it must be reachable using IIOP, on a different platform
	try {
	  if(addr.equalsIgnoreCase(platformAddress))
	    throw new NotFoundException("No agent named " + name + " present in this platform");
	  OutGoingIIOP outChannel = new OutGoingIIOP(myORB, addr);
	  FIPA_Agent_97 dest = outChannel.getObject();
	  result = new RemoteProxyIIOP(dest, platformAddress);
	}
	catch(IIOPFormatException iiopfe) { // Invalid address
	  throw new NotFoundException("Invalid agent address: [" + iiopfe.getMessage() + "]");
	}

      }

    }

    return result;

  }

}
