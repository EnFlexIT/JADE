/*
  $Log$
  Revision 1.29  1999/03/07 22:50:12  rimassa
  Added support for ACL messages with more than one receiver.

  Revision 1.28  1999/03/03 16:09:34  rimassa
  Implemented new methods from AgentContainer interface to remotely
  suspend and resume agents.

  Revision 1.27  1999/02/25 08:18:03  rimassa
  Added separate ThreadGroup objects for JADE user agents and JADE
  system agents.
  Moved container shutdown code from finalization to explicit invocation
  of an RMI exit() method.

  Revision 1.26  1999/02/16 08:06:32  rimassa
  Caught CORBA System Exception to trap inter-platform communications
  problems.

  Revision 1.25  1999/02/14 23:08:16  rimassa
  Changed AgentGroup handling to comply with new version of that class.

  Revision 1.24  1999/02/03 10:03:17  rimassa
  Added client-side CORBA support. Now every AgentContainer can call
  another platform through IIOP directly, without intervention from
  AgentPlatform or ACC.
  Now an AgentContainer receives the IIOP address for the Agent Platform
  at registration time.
  Filled in 'postOtherPlatform()' method to resort to IIOP for
  inter-platform communication.

  Revision 1.23  1998/12/07 23:48:56  rimassa
  Modified message dispatching methods to allow both a simple name
  (e.g. 'peter') and a complete name (e.g. 'peter@fipa.org:50/acc') to
  be present as message receiver.
  Added an empty postOtherPlatform() method for a future IIOP transport.

  Revision 1.22  1998/11/09 22:12:25  Giovanni
  Added AgentContainer interface's exit() method implementation to allow
  shutting down an AgentContainer remotely.

  Revision 1.21  1998/11/09 00:05:31  rimassa
  Now when an AgentContainer terminates and its shutDown() method is
  called each agent is killed and the AgentContainer waits for its
  thread to end by calling Agent.join() method.

  Revision 1.20  1998/11/03 00:27:52  rimassa
  Fixed a bug in ACL message multicast send: a reset() call was missing
  on target AgentGroup.

  Revision 1.19  1998/11/01 19:13:43  rimassa
  Removed every reference to now-deleted MessageDispatcher
  interface. Added code to do what MessageDispatcherImpl used to do.

  Revision 1.18  1998/11/01 14:58:25  rimassa
  Now shutDown() method is correctly called on exit.

  Revision 1.17  1998/10/31 16:30:58  rimassa
  Added support for correct agent and container termination. Now when an
  Agent informs its AgentContainer it is ended, the container removes
  the dead agent from the agent table and informs the
  AgentPlatform. Besides, when the last agent of an agent container
  ends, the container itself is shut down.

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

import java.io.StringWriter;

import java.net.InetAddress;
import java.net.MalformedURLException;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.omg.CORBA.*;

import jade.lang.acl.*;

import FIPA_Agent_97;

/***********************************************************************************

  Name: AgentContainerImpl

  Responsibilities and Collaborations:

  + Creates agents on the local Java VM, and starts the message dispatcher.
    (Agent)

  + Connects with each newly created agent, to allow event-based
    interaction between the two.
    (Agent)

  + Routes outgoing messages to the suitable message dispatcher, caching
    remote agent addresses.
    (Agent, AgentDescriptor)

  + Holds an RMI object reference for the agent platform, used to
    retrieve the addresses of unknown agents.
    (AgentPlatform)


**************************************************************************************/
public class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, CommListener {

  private static final int MAP_SIZE = 20;
  private static final float MAP_LOAD_FACTOR = 0.50f;

  // Local agents, indexed by agent name
  protected Hashtable localAgents = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // Remote agents cache, indexed by agent name
  private Hashtable remoteAgentsCache = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  // The agent platform this container belongs to
  private AgentPlatform myPlatform;

  // IIOP address of the platform, will be used for inter-platform communications
  protected String platformAddress;

  protected ORB myORB;

  private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");
  private ThreadGroup systemAgentThreads = new ThreadGroup("JADE System Agents");

  public AgentContainerImpl(String args[]) throws RemoteException {

    // Configure Java runtime system to put the whole host address in RMI messages
    try {
      System.getProperties().put("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
    }
    catch(java.net.UnknownHostException jnue) {
      // Silently ignore it
    }

    // Set up attributes for agents thread group
    agentThreads.setMaxPriority(Thread.MIN_PRIORITY);
    systemAgentThreads.setMaxPriority(Thread.MAX_PRIORITY);

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

    AgentDescriptor desc = new AgentDescriptor();

    // Subscribe as a listener for the new agent
    instance.addCommListener(this);

    // Insert new agent into local agents table
    localAgents.put(agentName.toLowerCase(), instance);

    desc.setContainer(this);

    try {
      myPlatform.bornAgent(agentName, desc); // RMI call
    }
    catch(NameClashException nce) {
      System.out.println("Agent name already in use");
      localAgents.remove(agentName.toLowerCase());
    }
    catch(RemoteException re) {
      System.out.println("Communication error while adding a new agent to the platform.");
      re.printStackTrace();
    }

    if(startIt)
      instance.doStart(agentName, platformAddress, agentThreads);
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
      throw new NotFoundException("WaitAgent failed to find " + agentName);
    agent.doWake();
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

  public void dispatch(ACLMessage msg) throws RemoteException, NotFoundException {
    String completeName = msg.getDest();
    String receiverName = null;
    int atPos = completeName.indexOf('@');
    if(atPos == -1)
      receiverName = completeName;
    else
      receiverName = completeName.substring(0,atPos);

    Agent receiver = (Agent)localAgents.get(receiverName.toLowerCase());

    if(receiver == null) 
      throw new NotFoundException("DispatchMessage failed to find " + receiverName);

    receiver.postMessage(msg);
  }

  public void ping() throws RemoteException {
  }

  public void invalidateCacheEntry(String key) throws RemoteException {
    remoteAgentsCache.remove(key);
  }

  public void joinPlatform(String platformRMI, Vector agentNamesAndClasses) {

     // Retrieve agent platform from RMI registry and register as agent container
    try {
      myPlatform = lookup3(platformRMI);
      platformAddress = myPlatform.addContainer(this); // RMI call
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
      agent.doStart(currentName, platformAddress, agentThreads);
    }
  }

  public void shutDown() {

    Enumeration agentNames = localAgents.keys();
    try {

      // Remove all agents
      while(agentNames.hasMoreElements()) {
	String name = (String)agentNames.nextElement();
	// Kill agent and wait for its termination
	Agent a = (Agent)localAgents.get(name);
	a.doDelete();
	a.join();
      }

      // Deregister itself as a container
      myPlatform.removeContainer(this); // RMI call

    }
    catch(RemoteException re) {
      re.printStackTrace();
    }
  }


  // Implementation of CommListener interface

  public void CommHandle(CommEvent event) {

    // Get ACL message from the event.
    ACLMessage msg = event.getMessage();

    AgentGroup group = null;
    if(event.isMulticast()) {
      group = event.getRecipients();
      Enumeration e = group.getMembers();
      while(e.hasMoreElements()) {
	String dest = (String)e.nextElement();
	msg.removeAllDests();
	msg.addDest(dest);
	unicastPostMessage(msg, dest);
      }
    }
    else {
      group = msg.getDests();
      Enumeration e = group.getMembers();
      while(e.hasMoreElements()) {
	String dest = (String)e.nextElement();
	unicastPostMessage(msg, dest);
      }
    }
  }

  public void endSource(String name) {
    try {

      localAgents.remove(name);
      myPlatform.deadAgent(name); // RMI call

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
      receiverAddr = platformAddress;
    }
    else {
      receiverName = completeName.substring(0,atPos);
      receiverAddr = completeName.substring(atPos + 1);
    }

    if(receiverAddr.equalsIgnoreCase(platformAddress)) {

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
    else
      // Use IIOP for inter-platform communication
      postOtherPlatform(msg, receiverName, receiverAddr);

  }

  private void postRemote(ACLMessage msg, String receiverName) {

    // Look first in descriptor cache
    AgentDescriptor desc = (AgentDescriptor)remoteAgentsCache.get(receiverName.toLowerCase());
    try {

      if(desc == null) { // Cache miss :-( . Ask agent platform and update agent cache
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName.toLowerCase(),desc);  // FIXME: The cache grows indefinitely ...
      }
      AgentContainer ac = desc.getContainer();
      try {
	ac.dispatch(msg); // RMI call
      }
      catch(RemoteException re) { // Communication error: retry with a newer object reference from the platform
	remoteAgentsCache.remove(receiverName.toLowerCase());
	desc = myPlatform.lookup(receiverName); // RMI call
	remoteAgentsCache.put(receiverName.toLowerCase(),desc);  // FIXME: The cache grows indefinitely ...
	ac = desc.getContainer();
	ac.dispatch(msg); // RMI call
      }

    }
    catch(NotFoundException nfe) {
      System.err.println("Agent " + receiverName + " was not found on agent platform");
      System.err.println("Message from platform was: " + nfe.getMessage());
      // nfe.printStackTrace();
    }
    catch(RemoteException re) {
      System.out.println("Communication error while contacting agent platform");
      re.printStackTrace();
    }
  }

  private void postOtherPlatform(ACLMessage msg, String receiverName, String receiverAddr) {
    try {
      OutGoingIIOP outChannel = new OutGoingIIOP(myORB, receiverAddr);
      FIPA_Agent_97 dest = outChannel.getObject();

      String sender = msg.getSource();
      if(sender.indexOf('@') == -1)
        msg.setSource(sender + '@' + platformAddress);

      StringWriter msgText = new StringWriter();
      msg.toText(msgText);
      dest.message(msgText.toString()); // CORBA call
    }
    catch(IIOPFormatException iiopfe) {
      iiopfe.printStackTrace();
    }
    catch(org.omg.CORBA.SystemException oocse) {
      System.out.println("Communication error while contacting foreign agent platform");
      oocse.printStackTrace();
    }
  }

}
