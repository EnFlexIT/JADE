/*
 * $Id$
 */

package jade.core;

import java.io.Reader;
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
public class Agent implements Runnable, CommBroadcaster {


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
  protected ACLMessage currentMessage;

  private int myAPState;
  private int myDomainState;

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

  // State transition methods for Agent Platform Life-Cycle

  public void doStart(String name, String platformAddress) { // Transition from Initiated to Active

    // Register this agent with platform AMS and start its embedded
    // thread
    myName = new String(name);
    myAddress = new String(name + "@" + platformAddress);

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
    try {
      wait(); // Blocks on its monitor
    }
    catch(InterruptedException ie) {
      // Do nothing
    }
  }

  public synchronized void doWake() { // Transition from Waiting to Active
    myAPState = AP_ACTIVE;
    notify(); // Wakes up the embedded thread
  }

  public void doDelete() { // Transition to destroy the agent
    myAPState = AP_DELETED; // FIXME: Should do something more
  }

  public final void run() {

    try{
      registerWithAMS(null,null,null,Agent.AP_ACTIVE);
      
      setup();

      mainLoop();

      destroy();
    }
    catch(Exception e) {
      System.err.println("***  Uncaught Exception for agent " + myName + "  ***");
      e.printStackTrace();
      destroy();
    }

  }

  protected void setup() {}

  private void mainLoop() {
    while(myAPState != AP_DELETED) {

      // Select the next behaviour to execute
      Behaviour b = myScheduler.schedule();

      // Just do it!
      b.execute();

      // When is needed no more, delete it from the behaviours queue
      if(b.done()) {
	myScheduler.remove(b);
      }

    }
  }

  private void destroy() { // FIXME: Should remove the agent from all agents tables.
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

  // Build an ACL message from a character stream
  public ACLMessage parse(Reader text) {
    ACLMessage msg = null;
    try {
      msg = myParser.parse(text);
    }
    catch(ParseException pe) {
      pe.printStackTrace();
      System.exit(1);
    }
    return msg;
  }


  // Register yourself with platform AMS
  public void registerWithAMS(String signature, String delegateAgent,
			      String forwardAddress, int APState) throws FIPAException {

    String replyString = myName + "-ams-registration";

    ACLMessage request = new ACLMessage();

    request.setType("request");
    request.setSource(myName);
    request.setDest("ams");
    request.setLanguage("SL0");
    request.setOntology("fipa-agent-management");
    request.setProtocol("fipa-request");
    request.setReplyWith(replyString);

    AgentManagementOntology o = AgentManagementOntology.instance();

    String APStateName = o.getAPStateByCode(APState);

    // Put mandatory attributes in content string
    String content = "( action ams ( register-agent (" +
      " :agent-name " + myName +
      " :address " + myAddress +
      " :ap-state " + APStateName;

    // Add optional attributes if presents
    if(signature != null)
      content = content.concat(" :signature " + signature);

    if(delegateAgent != null)
      content = content.concat(" :delegate-agent " + delegateAgent);

    if(forwardAddress != null)
      content = content.concat(" :forward-address " + forwardAddress);

    content = content.concat(") ) )");

    request.setContent(content);

    send(request);

    ACLMessage reply = blockingReceive(MessageTemplate.MatchReplyTo(replyString));

    // FIXME: Should unmarshal content of 'refuse' and 'failure'
    // messages and convert them in Java exceptions
    if(reply.getType().equalsIgnoreCase("agree")) {
      reply =  blockingReceive(MessageTemplate.MatchReplyTo(replyString));

      if(!reply.getType().equalsIgnoreCase("inform")) {
	System.out.println("AMS registration failed !!!");
	doDelete();
      }

    }
    else {
      System.out.println("AMS registration refused !!!");
      doDelete();
    }

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

  // Puts an incoming message in agent's message queue
  public final synchronized void postMessage (ACLMessage msg) {
    if(msg != null) msgQueue.addElement(msg);
    System.out.println("Agent: receiving from " + msg.getSource());
    doWake();
  }

}



