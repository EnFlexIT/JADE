/*
 * $Id$
 */

package fipa.core;

import java.util.Enumeration;
import java.util.Vector;

import fipa.lang.acl.*;

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


  protected Vector msgQueue = new Vector();
  protected Vector listeners = new Vector();


  protected String myName = null;
  protected Thread myThread;
  protected Scheduler myScheduler;
  protected ACLMessage currentMessage;

  protected int APState;
  protected int DomainState;

  private ACLParser myParser = null;


  public Agent() {
    APState = AP_INITIATED;
    myThread = new Thread(this);
    myParser = null; // FIXME: Must be initialized with a valid ACL parser
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
      wait(); // Block on its monitor
    }
    catch(InterruptedException ie) {
      // Do nothing
    }
  }

  public void doWake() { // Transition from Waiting to Active
    APState = AP_ACTIVE;
    notify(); // Wakes up the embedded thread
  }

  public void doDelete() { // Transition to destroy the agent
    APState = -1; // FIXME: Shold do something more
  }

  public final void run() {

    setup();

    mainLoop();

    destroy();
  }

  protected void setup() {}

  private void mainLoop() {
    while(APState == AP_ACTIVE) {

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


  // Event based message sending -- serialized object
  public final void send(ACLMessage msg) {
    CommEvent event = new CommEvent(this, msg);
    Enumeration e = listeners.elements();
    while(e.hasMoreElements()) {
      CommListener l = (CommListener)e.nextElement();
      l.CommHandle(event);
    }
  }

  // Blocking receive
  public final ACLMessage blockingReceive() {
    if(msgQueue.isEmpty()) {
      doWait();
    }
    ACLMessage msg = (ACLMessage)msgQueue.firstElement();
    currentMessage = msg;
    msgQueue.removeElementAt(0);
    return msg;
  }

  // Non-blocking receive
  public final ACLMessage receive() {
    if(msgQueue.isEmpty())
      return null;
    else {
      ACLMessage msg = (ACLMessage)msgQueue.firstElement();
      currentMessage = msg;
      msgQueue.removeElementAt(0);
      return msg;
    }
  }

  // Event handling methods

  public final void addCommListener(CommListener l) {
    listeners.addElement(l);
  }

  public final void removeCommListener(CommListener l) {
    listeners.removeElement(l);
  }

  public final synchronized void postMessage (ACLMessage msg) {
    if(msg != null) msgQueue.addElement(msg);
    System.out.println("Agent: receiving from " + msg.getSource());
    doWake();
  }

}



