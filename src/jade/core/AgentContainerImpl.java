/*
  $Log$
  Revision 1.39  1999/06/04 07:44:02  rimassa
  Made package scoped this previously public class.

  Revision 1.38  1999/04/13 15:58:55  rimassa
  Added a hack to catch a SocketException arising during RMA shutdown.

  Revision 1.37  1999/04/08 12:07:55  rimassa
  Added a missing clone() to multicast messages.

  Revision 1.36  1999/04/08 12:00:03  rimassa
  Changed multicast code to make it work, even if now isn't probably
  compliant.

  Revision 1.35  1999/04/07 11:39:00  rimassa
  Fixed a shutdown problem: a ConcurrentModificationException was thrown
  during local agents destruction.
  Removed calls to ACLMessage.getDest() method.

  Revision 1.34  1999/03/30 06:49:44  rimassa
  Fixed a bug: when an agent on another platform had the same local name
  of a local agent, it was not contacted with IIOP.

  Revision 1.33  1999/03/24 12:16:57  rimassa
  Ported most data structures to newer Java 2 Collection
  framework. Changed unicastPostMessage() method to provide transparent
  address caching for every kind of agent address (local, RMI or IIOP).

  Revision 1.32  1999/03/17 12:55:26  rimassa
  Implemented a complete, general caching mechanism for agent addresses,
  using the same cache to keep local and remote agent proxies. Besides,
  caching works now independently from the specific remote protocol used
  for message transport (i.e. RMI, CORBA or other).
  Now a cached local proxy can fault and be replaced with a remote one,
  thus setting some bases for agent mobility.

  Revision 1.31  1999/03/15 15:25:10  rimassa
  Changed priority setting for user agents.

  Revision 1.30  1999/03/09 12:54:55  rimassa
  Some minor modifications for a better container name handling.

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.omg.CORBA.*;

import jade.lang.acl.*;

import FIPA_Agent_97;


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

  private ThreadGroup agentThreads = new ThreadGroup("JADE Agents");

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

    RemoteProxyRMI rp = new RemoteProxyRMI(this);

    // Subscribe as a listener for the new agent
    instance.addCommListener(this);

    // Insert new agent into local agents table
    localAgents.put(agentName.toLowerCase(), instance);

    try {
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
    String completeName = msg.getFirstDest(); // FIXME: Not necessarily the first one is the right one
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
      }
      catch(RemoteException re) { // It should never happen
	re.printStackTrace();
      }

    }

    // Now activate all agents (this call starts their embedded threads)
    Set names = localAgents.keySet();
    Iterator nameList = names.iterator();
    String currentName = null;
    while(nameList.hasNext()) {
      currentName = (String)nameList.next();
      Agent agent = (Agent)localAgents.get(currentName.toLowerCase());
      agent.doStart(currentName, platformAddress, agentThreads);
    }
  }

  public void shutDown() {
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
	ACLMessage copy = (ACLMessage)msg.clone();
	copy.removeAllDests();
	copy.addDest(dest);
	unicastPostMessage(copy, dest);
      }
    }
    else {  // FIXME: This is probably not compliant
      group = msg.getDests();
      Enumeration e = group.getMembers();
      while(e.hasMoreElements()) {
	String dest = (String)e.nextElement();
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
      catch(NotFoundException nfe1) { // Stale cache entry
	cachedProxies.remove(completeName);
	try {
          AgentProxy freshOne = getFreshProxy(receiverName, receiverAddr);
	  freshOne.dispatch(msg);
	  cachedProxies.put(completeName, freshOne);
	}
	catch(NotFoundException nfe2) { // Some serious problem
	  System.err.println("Agent " + receiverName + " was not found on agent platform");
	  System.err.println("Message from platform was: " + nfe2.getMessage());
	}
      }
    }

    else { // Cache miss :-(
      try {
	AgentProxy newOne = getFreshProxy(receiverName, receiverAddr);
	newOne.dispatch(msg);
	cachedProxies.put(completeName, newOne);
      }
      catch(NotFoundException nfe) { // Some serious problem
	System.err.println("Agent " + receiverName + " was not found on agent platform");
        System.err.println("Message from platform was: " + nfe.getMessage());
      }

    }

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
