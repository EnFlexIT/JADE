/*
  $Log$
  Revision 1.16  1998/10/25 23:58:26  rimassa
  Moved agent creation code into a 'createAgent()' method. Besides,
  createAgent() and killAgent() are now Remote Methods, thus allowing to
  create an agent on a different container.

  Revision 1.15  1998/10/18 15:53:13  rimassa
  Added a private lookup3() method to avoid a bug in java.rmi.Naming
  class.
  Set Java runtime property "java.rmi.server.hostname" to the complete
  host name, to allow agent platforms to be distributed over WAN and
  interoperate without DNS lookups.

  Revision 1.14  1998/10/15 18:03:51  Giovanni
  Fixed an horrible bug: a chunk of code was moved from outside a for
  loop to inside the loop !!! This resulted in platform misbehaviour
  whenever more than one agent was started on a container.

  Revision 1.13  1998/10/14 21:32:06  Giovanni
  Moved a piece of code inside a try { ... } block; now when a new agent
  has a name clashing with a previous one and a NameClashException is
  thrown the agent is not started anymore and it is removed from the
  local agents table.

  Revision 1.12  1998/10/11 19:20:14  rimassa
  Changed code to comply with new MessageDispatcher constructor.
  Implemented invalidateCacheEntry() remote method.
  New name clash exception handled properly.
  Implemented a cache refresh on communication failures: when a cached
  AgentDescriptor results in a RemoteException, main Agent Platform is
  called to update remote agent cache and retry with the newer RMI
  object reference before giving up for good with a NotFoundException.

  Revision 1.11  1998/10/05 20:13:51  Giovanni
  Made every agent name table is case insensitive, according to FIPA
  specification.

  Revision 1.10  1998/10/04 18:00:57  rimassa
  Added a 'Log:' field to every source file.
  */

package jade.core;

import java.net.InetAddress;
import java.net.MalformedURLException;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import jade.lang.acl.*;

/***********************************************************************************

  Name: AgentContainerImpl

  Responsibilities and Collaborations:

  + Creates agents on the local Java VM, and starts the message dispatcher.
    (Agent, MessageDispatcher)

  + Connects with each newly created agent, to allow event-based
    interaction between the two.
    (Agent, MessageDispatcher)

  + Routes outgoing messages to the suitable message dispatcher, caching
    remote agent addresses.
    (Agent, AgentDescriptor, MessageDispatcher)

  + Holds an RMI object reference for the agent platform, used to
    retrieve the addresses of unknown agents.
    (AgentPlatform, MessageDispatcher)


**************************************************************************************/
public class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, CommListener {

  private static final int MAP_SIZE = 20;
  private static final float MAP_LOAD_FACTOR = 0.50f;

  // Local agents, indexed by agent name
  protected Hashtable localAgents = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // Remote agents cache, indexed by agent name
  private Hashtable remoteAgentsCache = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // The message dispatcher of this container
  protected MessageDispatcherImpl myDispatcher;

  // The agent platform this container belongs to
  private AgentPlatform myPlatform;

  // IIOP address of the platform, will be used for inter-platform communications
  protected String platformAddress;


  public AgentContainerImpl() throws RemoteException {
    myDispatcher = new MessageDispatcherImpl(this, localAgents);

    // Configure Java runtime system to put the whole host address in RMI messages
    try {
      System.getProperties().put("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
    }
    catch(java.net.UnknownHostException jnue) {
      // Silently ingnore it
    }

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

    AgentDescriptor desc = new AgentDescriptor();

    // Subscribe as a listener for the new agent
    instance.addCommListener(this);

    // Insert new agent into local agents table
    localAgents.put(agentName.toLowerCase(), instance);

    desc.setDemux(myDispatcher);

    try {
      myPlatform.bornAgent(agentName, desc); // RMI call
    }
    catch(NameClashException nce) {
      System.out.println("Agent name already in use");
      nce.printStackTrace();
      localAgents.remove(agentName.toLowerCase());
    }
    catch(RemoteException re) {
      System.out.println("Communication error while adding a new agent to the platform.");
      re.printStackTrace();
    }

    if(startIt)
      instance.doStart(agentName, platformAddress);
  }

  public void killAgent(String agentName) throws RemoteException, NotFoundException {
    Agent agent = (Agent)localAgents.get(agentName.toLowerCase());
    if(agent == null)
      throw new NotFoundException("KillAgent failed to find " + agentName);
    agent.doDelete();
  }

  public void invalidateCacheEntry(String key) throws RemoteException {
    remoteAgentsCache.remove(key);
  }


  public void joinPlatform(String platformRMI, String platformIIOP, Vector agentNamesAndClasses) {

    platformAddress = platformIIOP;

    // Retrieve agent platform from RMI registry and register as agent container
    try {
      myPlatform = lookup3(platformRMI);
      myPlatform.addContainer(this); // RMI call
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
       b) AgentContainer 1 -> MessageDispatcher 2 -- Through RMI (cached or retreived from AgentPlatform)
       c) MessageDispatcher 2 -> Agent 2 -- Through postMessage() (direct insertion in message queue, no events here)

       agentNamesAndClasses is a Vector of String containing, orderly, the name of an agent and the name of Agent
       concrete subclass which implements that agent.
    */
    for( int i=0; i < agentNamesAndClasses.size(); i+=2 ) {
      String agentName = (String)agentNamesAndClasses.elementAt(i);
      String agentClass = (String)agentNamesAndClasses.elementAt(i+1);
      try {
	createAgent(agentName, agentClass, NOSTART);
      }
      catch(RemoteException re) { // It should never happen
	re.printStackTrace();
      }

    }

    // Now activate all agents (this call starts their embedded threads)
    Enumeration nameList = localAgents.keys();
    String currentName = null;
    while(nameList.hasMoreElements()) {
      currentName = (String)nameList.nextElement();
      Agent agent = (Agent)localAgents.get(currentName.toLowerCase());
      agent.doStart(currentName, platformAddress);
    }
  }

  public void shutDown() {
    Enumeration agentNames = localAgents.keys();

    try {

      // Remove all agents
      while(agentNames.hasMoreElements()) {
	String name = (String)agentNames.nextElement();
	Agent a = (Agent)localAgents.get(name);
	localAgents.remove(name);
	a.doDelete();
	myPlatform.deadAgent(name); // RMI call
      }

      // Deregister itself as a container
      myPlatform.removeContainer(this); // RMI call
    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
  }

  protected void finalize() {
    shutDown();
  }

  public void CommHandle(CommEvent event) {

    // Get ACL message from the event.
    ACLMessage msg = event.getMessage();

    // FIXME: Must use multicast also when more than a recipient is
    // present in ':receiver' field.
    if(event.isMulticast()) {
      AgentGroup group = event.getRecipients();
      while(group.hasMoreMembers()) {
	msg.setDest(group.getNextMember());
	unicastPostMessage(msg);
      }
    }
    else
      unicastPostMessage(msg);
  }

  // This hack is needed to overcome a bug in java.rmi.Naming class:
  // when an object reference is binded, unbinded and then rebinded
  // with the same URL, the next two lookup() calls will throw an
  // Exception without a reason.
  private AgentPlatform lookup3(String URL)
    throws RemoteException, NotBoundException, MalformedURLException, UnknownHostException {
    Object o = null;
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

  private void unicastPostMessage(ACLMessage msg) {
    String receiverName = msg.getDest();

    // Look up in local agents.
    Agent receiver = (Agent)localAgents.get(receiverName.toLowerCase());

    if(receiver != null)
      receiver.postMessage(msg);
    else
      // Search failed; look up in remote agents.
      postRemote(msg, receiverName);

    // If it fails again, ask the Agent Platform.
    // If still fails, raise NotFound exception.
  }

  private void postRemote(ACLMessage msg, String receiverName) {

    // Look first in descriptor cache
    AgentDescriptor desc = (AgentDescriptor)remoteAgentsCache.get(receiverName.toLowerCase());
    try {

      if(desc == null) { // Cache miss :-( . Ask agent platform and update agent cache
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName.toLowerCase(),desc);  // FIXME: The cache grows indefinitely ...
      }
      MessageDispatcher md = desc.getDemux();
      try {
	md.dispatch(msg); // RMI call
      }
      catch(RemoteException re) { // Communication error: retry with a newer object reference from the platform
	remoteAgentsCache.remove(receiverName.toLowerCase());
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName.toLowerCase(),desc);  // FIXME: The cache grows indefinitely ...
	md = desc.getDemux();
	md.dispatch(msg); // RMI call
      }

    }
    catch(NotFoundException nfe) {
      System.err.println("Agent was not found on agent platform");
      nfe.printStackTrace();
    }
    catch(RemoteException re) {
      System.out.println("Communication error while contacting agent platform");
      re.printStackTrace();
    }
  }

}
