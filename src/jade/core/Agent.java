/*
  $Log$
  Revision 1.25  1998/11/08 23:57:50  rimassa
  Added a join() method to allow AgentContainer objects to wait for all
  their agents to terminate before exiting.

  Revision 1.24  1998/11/05 23:31:20  rimassa
  Added a protected takeDown() method as a placeholder for
  agent-specific destruction actions.
  Added automatic AMS deregistration on agent exit.

  Revision 1.23  1998/11/01 19:11:19  rimassa
  Made doWake() activate all blocked behaviours.

  Revision 1.22  1998/10/31 16:27:36  rimassa
  Completed doDelete() method: now an Agent can be explicitly terminated
  from outside or end implicitly when one of its Behaviours calls
  doDelete(). Besides, when an agent dies informs its CommListeners.

  Revision 1.21  1998/10/25 23:54:30  rimassa
  Added an 'implements Serializable' clause to support Agent code
  downloading through RMI.

  Revision 1.20  1998/10/18 15:50:08  rimassa
  Method parse() is now deprecated, since ACLMessage class provides a
  fromText() static method.
  Removed any usage of deprecated ACLMessage default constructor.

  Revision 1.19  1998/10/11 19:11:13  rimassa
  Written methods to access Directory Facilitator and removed some dead
  code.

  Revision 1.18  1998/10/07 22:13:12  Giovanni
  Added a correct prototype to DF access methods in Agent class.

  Revision 1.17  1998/10/05 20:09:02  Giovanni
  Fixed comment indentation.

  Revision 1.16  1998/10/05 20:07:53  Giovanni
  Removed System.exit() in parse() method. Now it simply prints
  exception stack trace on failure.

  Revision 1.15  1998/10/04 18:00:55  rimassa
  Added a 'Log:' field to every source file.

  revision 1.14
  date: 1998/09/28 22:33:10;  author: Giovanni;  state: Exp;  lines: +154 -40
  Changed registerWithAMS() method to take advantage of new
  AgentManagementOntology class.
  Added public methods to access ACC, AMS and DF agents without explicit
  message passing.

  revision 1.13
  date: 1998/09/28 00:13:41;  author: rimassa;  state: Exp;  lines: +20 -16
  Added a name for the embedded thread (same name as the agent).
  Changed parameters ordering and ACL message format to comply with new
  FIPA 98 AMS.

  revision 1.12
  date: 1998/09/23 22:59:47;  author: Giovanni;  state: Exp;  lines: +3 -1
  *** empty log message ***

  revision 1.11
  date: 1998/09/16 20:05:20;  author: Giovanni;  state: Exp;  lines: +2 -2
  Changed code to reflect a name change in Behaviour class from
  execute() to action().

  revision 1.10
  date: 1998/09/09 01:37:04;  author: rimassa;  state: Exp;  lines: +30 -10
  Added support for Behaviour blocking and restarting. Now when a
  behaviour blocks it is removed from the Scheduler and put into a
  blocked behaviour queue (actually a Vector). When a message arrives,
  postMessage() method puts all blocked behaviours back in the Scheduler
  and calls restart() on each one of them.
  Since when the Scheduler is empty the agent thread is blocked, the
  outcome is that an agent whose behaviours are all waiting for messages
  (e.g. the AMS) does not waste CPU cycles.

  revision 1.9
  date: 1998/09/02 23:56:02;  author: rimassa;  state: Exp;  lines: +4 -1
  Added a 'Thread.yield() call in Agent.mainLoop() to improve fairness
  among different agents and thus application responsiveness.

  revision 1.8
  date: 1998/09/02 00:30:22;  author: rimassa;  state: Exp;  lines: +61 -38
  Now using jade.domain.AgentManagementOntology class to map AP
  Life-Cycle states to their names.

  AP Life-Cycle states now made public. Changed protection level for
  some instance variables. Better error handling during AMS registration.

  revision 1.7
  date: 1998/08/30 23:57:10;  author: rimassa;  state: Exp;  lines: +8 -1
  Fixed a bug in Agent.registerWithAMS() method, where reply messages
  from AMS were ignored. Now the agent receives the replies, but still
  does not do a complete error checking.

  revision 1.6
  date: 1998/08/30 22:52:18;  author: rimassa;  state: Exp;  lines: +71 -9
  Improved Agent Platform Life-Cycle management. Added constants for
  Domain Life-Cycle. Added support for IIOP address. Added automatic
  registration with platform AMS.

  revision 1.5
  date: 1998/08/25 18:08:43;  author: Giovanni;  state: Exp;  lines: +5 -1
  Added Agent.putBack() method to insert a received message back in the
  message queue.

  revision 1.4
  date: 1998/08/16 12:34:56;  author: rimassa;  state: Exp;  lines: +26 -7
  Communication event broadcasting is now done in a separate
  broadcastEvent() method. Added a multicast send() method using
  AgentGroup class.

  revision 1.3
  date: 1998/08/16 10:30:15;  author: rimassa;  state: Exp;  lines: +48 -18
  Added AP_DELETED state to Agent Platform life cycle, and made
  Agent.mainLoop() exit only when the state is AP_DELETED.
  Agent.doWake() is now synchronized, and so are all kinds of receive
  operations.

  Agent class now has two kinds of message receive operations: 'get
  first available message' and 'get first message matching a particular
  template'; both kinds come in blocking and nonblocking
  flavour. Blocking receives mplementations rely on nonblocking
  versions.

  revision 1.2
  date: 1998/08/08 17:23:31;  author: rimassa;  state: Exp;  lines: +3 -3
  Changed 'fipa' to 'jade' in package and import directives.

  revision 1.1
  date: 1998/08/08 14:27:50;  author: rimassa;  state: Exp;
  Renamed 'fipa' to 'jade'.

  */

package jade.core;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Serializable;

import java.util.Enumeration;
import java.util.Vector;

import jade.lang.acl.*;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

/**************************************************************

  Name: Agent

  Responsibility and Collaborations:

  + Abstract placeholder for user-defined agents.

  + Provides primitives for sending and receiving messages.
    (ACLMessage)

  + Schedules and executes complex behaviours.
    (Behaviour, Scheduler)

****************************************************************/
public class Agent implements Runnable, Serializable, CommBroadcaster {


  // Agent Platform Life-Cycle states

  public static final int AP_MIN = -1;   // Hand-made type checking
  public static final int AP_INITIATED = 1;
  public static final int AP_ACTIVE = 2;
  public static final int AP_SUSPENDED = 3;
  public static final int AP_WAITING = 4;
  public static final int AP_DELETED = 5;
  public static final int AP_MAX = 6;    // Hand-made type checking

  // Domain Life-Cycle states

  public static final int D_MIN = 9;     // Hand-made type checking
  public static final int D_ACTIVE = 10;
  public static final int D_SUSPENDED = 20;
  public static final int D_RETIRED = 30;
  public static final int D_UNKNOWN = 40;
  public static final int D_MAX = 41;    // Hand-made type checking

  protected Vector msgQueue = new Vector();
  protected Vector listeners = new Vector();

  protected String myName = null;
  protected String myAddress = null;

  protected Thread myThread;
  protected Scheduler myScheduler;
  protected Behaviour currentBehaviour;
  protected ACLMessage currentMessage;

  private int myAPState;
  private int myDomainState;
  private Vector blockedBehaviours = new Vector();

  protected ACLParser myParser = ACLParser.create();


  public Agent() {
    myAPState = AP_INITIATED;
    myDomainState = D_UNKNOWN;
    myThread = new Thread(this);
    myScheduler = new Scheduler(this);
  }

  public String getName() {
    return myName;
  }

  // This is used by the agent container to wait for agent termination
  void join() {
    try { // FIXME: Some deadlock problems, since thread.interrupt() does not seem to work
      doWake();
      myThread.join(500); // Wait at most 500 milliseconds
    }
    catch(InterruptedException ie) {
      ie.printStackTrace();
    }

  }

  // State transition methods for Agent Platform Life-Cycle

  public void doStart(String name, String platformAddress) { // Transition from Initiated to Active

    // Set this agent's name and address and start its embedded thread
    myName = new String(name);
    myAddress = new String(platformAddress);

    myThread.setName(myName);
    myThread.start();

  }

  public void doMove() { // Transition from Active to Initiated
    myAPState = AP_INITIATED;
    // FIXME: Should do something more
  }

  public void doSuspend() { // Transition from Active to Suspended
    myAPState = AP_SUSPENDED;
    // FIXME: Should do something more
  }

  public void doActivate() { // Transition from Suspended to Active
    myAPState = AP_ACTIVE;
    // FIXME: Should do something more
  }

  public synchronized void doWait() { // Transition from Active to Waiting
    myAPState = AP_WAITING;
    while(myAPState == AP_WAITING) {
      try {
	wait(); // Blocks on its monitor
      }
      catch(InterruptedException ie) {
	myThread.interrupt();
	System.out.println("Thread " + Thread.currentThread().getName() + " Interrupted");
	myAPState = AP_DELETED;
      }
    }
  }

  public synchronized void doWake() { // Transition from Waiting to Active
    myAPState = AP_ACTIVE;
    activateAllBehaviours();
    notify(); // Wakes up the embedded thread
  }

  // This method handles both the case when the agents decides to exit
  // and the case in which someone else kills him from outside.
  public void doDelete() { // Transition to destroy the agent
    myAPState = AP_DELETED;
    if(!myThread.equals(Thread.currentThread()))
       myThread.interrupt();
  }

  public final void run() {

    try{
      registerWithAMS(null,Agent.AP_ACTIVE,null,null,null);

      setup();

      mainLoop();

    }
    catch(InterruptedException ie) {
      // Do Nothing, since this is a killAgent from outside
    }
    catch(Exception e) {
      System.err.println("***  Uncaught Exception for agent " + myName + "  ***");
      e.printStackTrace();
    }
    finally {
      takeDown();
      destroy();
    }

  }

  protected void setup() {}

  protected void takeDown() {}

  private void mainLoop() throws InterruptedException {
    while(myAPState != AP_DELETED) {

      // Select the next behaviour to execute
      currentBehaviour = myScheduler.schedule();

      // Just do it!
      currentBehaviour.action();

      if(myAPState == AP_DELETED)
	return;

      // When it is needed no more, delete it from the behaviours queue
      if(currentBehaviour.done()) {
	myScheduler.remove(currentBehaviour);
	currentBehaviour = null;
      }
      else if(!currentBehaviour.isRunnable()) {
	// Remove blocked behaviours from scheduling queue and put it
	// in blocked behaviours queue
	myScheduler.remove(currentBehaviour);
	blockedBehaviours.addElement(currentBehaviour);
	currentBehaviour = null;
      }

      // Now give CPU control to other agents
      Thread.yield();

    }
  }

  private void destroy() {

    try {
      deregisterWithAMS();
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }
    notifyDestruction();
  }

  public void addBehaviour(Behaviour b) {
    myScheduler.add(b);
  }

  public void removeBehaviour(Behaviour b) {
    myScheduler.remove(b);
  }


  // Event based message sending -- unicast
  public final void send(ACLMessage msg) {
    CommEvent event = new CommEvent(this, msg);
    broadcastEvent(event);
  }

  // Event based message sending -- multicast
  public final void send(ACLMessage msg, AgentGroup g) {
    CommEvent event = new CommEvent(this, msg, g);
    broadcastEvent(event);
  }

  // Non-blocking receive
  public final synchronized ACLMessage receive() {
    if(msgQueue.isEmpty()) {
      return null;
    }
    else {
      ACLMessage msg = (ACLMessage)msgQueue.firstElement();
      currentMessage = msg;
      msgQueue.removeElementAt(0);
      return msg;
    }
  }

  // Non-blocking receive with pattern matching on messages
  public final synchronized ACLMessage receive(MessageTemplate pattern) {
    ACLMessage msg = null;

    Enumeration messages = msgQueue.elements();

    while(messages.hasMoreElements()) {
      ACLMessage cursor = (ACLMessage)messages.nextElement();
      if(pattern.match(cursor)) {
	msg = cursor;
	currentMessage = cursor;
	msgQueue.removeElement(cursor);
	break; // Exit while loop
      }
    }

    return msg;
  }

  // Blocking receive
  public final synchronized ACLMessage blockingReceive() {
    ACLMessage msg = receive();
    while(msg == null) {
      doWait();
      msg = receive();
    }
    return msg;
  }

  // Blocking receive with pattern matching on messages
  public final synchronized ACLMessage blockingReceive(MessageTemplate pattern) {
    ACLMessage msg = receive(pattern);
    while(msg == null) {
      doWait();
      msg = receive(pattern);
    }
    return msg;
  }

  // Put a received message back in message queue
  public final synchronized void putBack(ACLMessage msg) {
    msgQueue.insertElementAt(msg,0);
  }


  /**
     @deprecated Builds an ACL message from a character stream. Now
     ACLMessage class has this capabilities itself, through fromText()
     method.
     @see ACLMessage
  */
  public ACLMessage parse(Reader text) {
    ACLMessage msg = null;
    try {
      msg = myParser.parse(text);
    }
    catch(ParseException pe) {
      pe.printStackTrace();
    }
    catch(TokenMgrError tme) {
      tme.printStackTrace();
    }
    return msg;
  }

  private ACLMessage FipaRequestMessage(String dest, String replyString) {
    ACLMessage request = new ACLMessage("request");

    request.setSource(myName);
    request.setDest(dest);
    request.setLanguage("SL0");
    request.setOntology("fipa-agent-management");
    request.setProtocol("fipa-request");
    request.setReplyWith(replyString);

    return request;
  }

  private void doFipaRequestClient(ACLMessage request, String replyString) {

    send(request);

    ACLMessage reply = blockingReceive(MessageTemplate.MatchReplyTo(replyString));

    // FIXME: Should unmarshal content of 'refuse' and 'failure'
    // messages and convert them to Java exceptions
    if(reply.getType().equalsIgnoreCase("agree")) {
      reply =  blockingReceive(MessageTemplate.MatchReplyTo(replyString));

      if(!reply.getType().equalsIgnoreCase("inform")) {
	System.out.println(replyString + " failed for " + myName + "!!!");
      }

    }
    else {
      System.out.println(replyString + " refused for " + myName + "!!!");
    }

  }


  // Register yourself with platform AMS
  public void registerWithAMS(String signature, int APState, String delegateAgent,
			      String forwardAddress, String ownership) throws FIPAException {
				
    String replyString = myName + "-ams-registration";
    ACLMessage request = FipaRequestMessage("ams", replyString);

    // Build an AMS action object for the request
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    amsd.setName(myName + "@" + myAddress);
    amsd.setAddress(myAddress);
    amsd.setAPState(APState);
    amsd.setDelegateAgentName(delegateAgent);
    amsd.setForwardAddress(forwardAddress);
    amsd.setOwnership(ownership);

    a.setName(AgentManagementOntology.AMSAction.REGISTERAGENT);
    a.setArg(amsd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action ams " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  // Authenticate yourself with platform AMS
  public void authenticateWithAMS(String signature, int APState, String delegateAgent,
				  String forwardAddress, String ownership) throws FIPAException {
    // FIXME: Not implemented
  }

  // Deregister yourself with platform AMS
  public void deregisterWithAMS() throws FIPAException {

    String replyString = myName + "-ams-deregistration";

    // Get a semi-complete request message
    ACLMessage request = FipaRequestMessage("ams", replyString);
    
    // Build an AMS action object for the request
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    amsd.setName(myName + "@" + myAddress);
    a.setName(AgentManagementOntology.AMSAction.DEREGISTERAGENT);
    a.setArg(amsd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action ams " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  // Modify your registration with platform AMS
  public void modifyAMSRegistration(String signature, int APState, String delegateAgent,
				    String forwardAddress, String ownership) throws FIPAException {

    String replyString = myName + "-ams-modify";
    ACLMessage request = FipaRequestMessage("ams", replyString);

    // Build an AMS action object for the request
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    amsd.setName(myName + "@" + myAddress);
    amsd.setAddress(myAddress);
    amsd.setAPState(APState);
    amsd.setDelegateAgentName(delegateAgent);
    amsd.setForwardAddress(forwardAddress);
    amsd.setOwnership(ownership);

    a.setName(AgentManagementOntology.AMSAction.MODIFYAGENT);
    a.setArg(amsd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action ams " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  public void forwardWithACC(ACLMessage msg) {

    String replyString = myName + "-acc-forward";
    ACLMessage request = FipaRequestMessage("acc", replyString);

    // Build an ACC action object for the request
    AgentManagementOntology.ACCAction a = new AgentManagementOntology.ACCAction();
    a.setName(AgentManagementOntology.ACCAction.FORWARD);
    a.setArg(msg);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action acc " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  // Register yourself with a DF
  public void registerWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {

    String replyString = myName + "-df-register";
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
    a.setName(AgentManagementOntology.DFAction.REGISTER);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action " + dfName + " ( " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  // Deregister yourself with a DF 
  public void deregisterWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {

    String replyString = myName + "-df-deregister";
    ACLMessage request = FipaRequestMessage(dfName, replyString);
    
    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
    a.setName(AgentManagementOntology.DFAction.DEREGISTER);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action " + dfName + " ( " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  // Modify registration data with a DF
  public void modifyDFRegistration(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {

    String replyString = myName + "-df-modify";
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFAction();
    a.setName(AgentManagementOntology.DFAction.MODIFY);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action " + dfName + " ( " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }

  // Search a DF for information
  public void searchDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) { // FIXME: Constraints still missing

    String replyString = myName + "-df-search";
    ACLMessage request = FipaRequestMessage(dfName, replyString);

    // Build a DF action object for the request
    AgentManagementOntology.DFAction a = new AgentManagementOntology.DFSearchAction();
    a.setName(AgentManagementOntology.DFAction.SEARCH);
    a.setArg(dfd);

    // Convert it to a String and write it in content field of the request
    StringWriter text = new StringWriter();
    a.toText(text);
    request.setContent("( action " + dfName + " ( " + text + " )");

    // Send message and collect reply
    doFipaRequestClient(request, replyString);

  }


  // Event handling methods


  // Broadcast communication event to registered listeners
  private void broadcastEvent(CommEvent event) {
    Enumeration e = listeners.elements();
    while(e.hasMoreElements()) {
      CommListener l = (CommListener)e.nextElement();
      l.CommHandle(event);
    }
  }

  // Register a new listener
  public final void addCommListener(CommListener l) {
    listeners.addElement(l);
  }

  // Remove a registered listener
  public final void removeCommListener(CommListener l) {
    listeners.removeElement(l);
  }

  // Notify listeners of the destruction of the current agent
  private void notifyDestruction() {
    Enumeration e = listeners.elements();
    while(e.hasMoreElements()) {
      CommListener l = (CommListener)e.nextElement();
      l.endSource(myName);
    }
  }

  private void activateAllBehaviours() {
    // Put all blocked behaviours back in ready queue
    while(!blockedBehaviours.isEmpty()) {
      Behaviour b = (Behaviour)blockedBehaviours.lastElement();
      blockedBehaviours.removeElementAt(blockedBehaviours.size() - 1);
      b.restart();
      myScheduler.add(b);
    }
  }

  // Put an incoming message in agent's message queue and activate all
  // blocking behaviours waiting for a message
  public final synchronized void postMessage (ACLMessage msg) {
    if(msg != null) msgQueue.addElement(msg);
    doWake();
  }

}
