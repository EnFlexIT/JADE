/*
 * $Id$
 */

package jade.core;

import java.io.Reader;
import java.util.Enumeration;
import java.util.Vector;

import jade.lang.acl.*;

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
  protected static final int AP_INITIATED = 1;
  protected static final int AP_ACTIVE = 2;
  protected static final int AP_SUSPENDED = 3;
  protected static final int AP_WAITING = 4;
  protected static final int AP_DELETED = -1;


  protected Vector msgQueue = new Vector();
  protected Vector listeners = new Vector();


  protected String myName = null;
  protected Thread myThread;
  protected Scheduler myScheduler;
  protected ACLMessage currentMessage;

  protected int APState;
  protected int DomainState;

  protected ACLParser myParser = ACLParser.create();


  public Agent() {
    APState = AP_INITIATED;
    myThread = new Thread(this);
    myScheduler = new Scheduler(this);
  }

  public String getName() {
    return myName;
  }

  // State transition methods for Agent Platform Life-Cycle

  public void doStart(String name) { // Transition from Initiated to Active

    myName = new String(name);
    APState = AP_ACTIVE;
    myThread.start();

  }

  public void doMove() { // Transition from Active to Initiated
    APState = AP_INITIATED;
    // FIXME: Should do something more
  }

  public void doSuspend() { // Transition from Active to Suspended
    APState = AP_SUSPENDED;
    // FIXME: Should do something more
  }

  public void doActivate() { // Transition from Suspended to Active
    APState = AP_ACTIVE;
    // FIXME: Should do something more
  }

  public synchronized void doWait() { // Transition from Active to Waiting
    APState = AP_WAITING;
    try {
      wait(); // Blocks on its monitor
    }
    catch(InterruptedException ie) {
      // Do nothing
    }
  }

  public synchronized void doWake() { // Transition from Waiting to Active
    APState = AP_ACTIVE;
    notify(); // Wakes up the embedded thread
  }

  public void doDelete() { // Transition to destroy the agent
    APState = AP_DELETED; // FIXME: Should do something more
  }

  public final void run() {

    setup();

    mainLoop();

    destroy();
  }

  protected void setup() {}

  private void mainLoop() {
    while(APState != AP_DELETED) {

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



