/*
 * $Id$
 */

package fipa.core;

import java.util.Enumeration;
import java.util.Vector;
import java.lang.reflect.*;

import fipa.lang.acl.*;

/**************************************************************

  Name: Agent

  Responsibility and Collaborations:

  + Abstract placeholder for user-defined agents.

  + Provides primitives for sending and receiving messages.
    (ACLMessage)

  + Schedules and executes complex behaviours.
    (Behaviour)

****************************************************************/
public class Agent implements Runnable, CommBroadcaster {

  protected static final int AP_INITIATED = 1;
  protected static final int AP_ACTIVE = 2;
  protected static final int AP_SUSPENDED = 3;
  protected static final int AP_WAITING = 4;


  protected Vector msgQueue = new Vector();
  protected Vector listeners = new Vector();
  protected Vector actions = new Vector();


  protected String myName;
  protected Thread myThread;
  protected int APState;
  protected int DomainState;

  private ACLParser parser = null; // FIXME: Must be initialized with a valid ACL parser
  protected ACLMessage currentMessage;

  public Agent() {
    APState = AP_INITIATED;
  }

  public void doStart(String name) { // Transition from Initiated to Active

    myName = new String(name);

    APState = AP_ACTIVE;
    myThread = new Thread(this);
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

  public void doWait() { // Transition from Active to Waiting
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

  protected void localStartup() {}

  public final void run() {

    localStartup();

    System.out.println(myName + ": initializing...");

    schedule();

    System.out.println(myName + ": exiting...");

  }

  public void schedule() {
    
    /*
    int ret;
    Class[] params = new Class[0];
    Object[] args  = new Object[0];

    while( isActive && actions.size() > 0 ) {
      for( int i=0; i<actions.size(); i++ ) {
	String s = (String)actions.elementAt(i);
	System.out.println(myName + ": invoking " + s);
	try{
	  Method m = this.getClass().getMethod(s, params); // FIXME: Change Method to Behaviour
	  try{
	    ret = ((Integer)m.invoke( this, args )).intValue();
	    if( ret == 1 ) actions.removeElementAt(i);
	  }
	  catch( InvocationTargetException e ){
	    e.printStackTrace();
	  } 
	  catch( IllegalAccessException e ){
	    e.printStackTrace();
	  }
	}
	catch( NoSuchMethodException e ){
	  System.out.println(" method " + s + " not found ! ");
	  System.exit(3);
	}

	try {
	  Thread.sleep(500);
	} 
	catch( InterruptedException e ) {
	}

      }
    }
    */

  }


  public void addBehaviour(Behaviour b) {
    actions.addElement(b);
  }

  public void removeBehaviour(Behaviour b) {
    // FIXME: To be implemented
  }


  // Event based message sending -- serialized object
  protected final void send(ACLMessage msg) {
    CommEvent event = new CommEvent(this, msg);
    Enumeration e = listeners.elements();
    while(e.hasMoreElements()) {
      CommListener l = (CommListener)e.nextElement();
      l.CommHandle(event);
    }
  }

  // Blocking receive
  protected final ACLMessage blockingReceive() {
    if(msgQueue.isEmpty()) {
      doWait();
    }
    ACLMessage msg = (ACLMessage)msgQueue.firstElement();
    currentMessage = msg;
    msgQueue.removeElementAt(0);
    return msg;
  }

  // Non-blocking receive
  protected final ACLMessage receive() {
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



