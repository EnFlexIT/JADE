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

/**
 * The abstract Agent class.
 * All the agents belonging to the agent platform must inherit from
 * this Agent class.  Agent is composed by a single execution thread
 * for the active part.  All the tasks that must be performed by an
 * agent are implemented as Behaviours, which are called and scheduled
 * in a round-robin fashion by the agent scheduler.  All the specific
 * tasks an agent has to implement must be added as Behaviours to the
 * particular Agent derived class.  The abstract Agent class provides
 * the agent developer with some methods to implement some
 * communication paradigms: FipaRequest (corresponding to the
 * FIPA-request protocol), SendReceive (when a message requires only a
 * single answer notification), blockingSendReceive (the corresponding
 * blocking paradigm).  In fact, by default all protocols and
 * communication paradigms are non-blocking.  In order to allow the
 * user to be able to add its own tasks, the method addTask is
 * provided by the abstract Agent, requiring as a parameter the name
 * of the method implementing the task. As soon as the method is
 * called, the task specified is added to the queue of ready tasks for
 * the scheduler.
 * 
 * @version 2.0 5/98
 * */
public class Agent implements Runnable, CommBroadcaster {

  protected Vector msgQueue = new Vector();
  protected Vector listeners = new Vector();
  protected Vector actions = new Vector();

  protected ACLMessage   currentMessage;
  protected String       myName;
  protected Thread       myThread;
  protected boolean      isActive = false;

  private ACLParser parser = null; // FIXME: Must be initialized with a valid ACL parser

  public void activate(String name) { // FIXME: Check with FIPA specs for agent lifecycle

    myName = new String(name);

    isActive = true;
    myThread = new Thread(this);
    myThread.start();

  }

  protected void localStartup() {}

  public final void run() {

    localStartup();

    System.out.println(myName + ": initializing...");

    schedule();

    System.out.println(myName + ": exiting...");

  }

  public void schedule() {

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

  // Event based message sending -- plain string
  protected final void send(String msg) {
    // FIXME: To be implemented
  }

  // Blocking receive
  protected final ACLMessage blockingReceive() {
    if(msgQueue.isEmpty()) {
      try {
	wait();
      }
      catch(InterruptedException ie) {
	// Do nothing
      }
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

  // FIXME: Check with Paolo whether this is still needed.
/**
 * Metodo per fare il match fra un messaggio ed una serie di campi dati.
 * @param msg E' il messaggio da controllare
 * Gli altri parametri sono i campi da macthare.
 * @see aclMessage
 * @return Ritorna falso se il messaggio e' sbagliato.
 */
  /*
  public boolean verifyMsg( aclMessage msg, String source, String type, String content, String reply ) {
    String mySource  = msg.getSource();
    String myType    = msg.getType();
    String myContent = msg.getContent();
    String myReply   = msg.getReplyTo();
    if( source  != null && !source.equals(mySource) ) return false;
    if( type    != null && !type.equals(myType) )     return false;
    if( content != null ) {
      int len = content.length();
      if( !content.equals(myContent.substring(0,len)) ) return false;
    }
    if( reply   != null ) {
      int len = reply.length();
      if( !reply.equals(myReply.substring(0,len)) ) return false;
    }
    return true;

  }
  */

  // FIXME: Check with Paolo whether this is still needed...
/**
 * Metodo per ricevere un messaggio con determinati campi.
 * Prima vengono controllati i messaggi non consumati nella coda msgQueue.
 * Quindi eventualmente si attendono i messaggi in arrivo sul socket
 * I messaggi ricevuti ma non corretti vengono accodati a msgQueue.
 * @see Agent#receive
 * @see aclMessage
 * @return Ritorna il messaggio corretto oppure null.
 */
  /*
  public synchronized aclMessage receiveIf( String source, String type, String content, String reply ) {
    aclMessage msg = null;
    for( int i=0; i<msgQueue.size(); i++ ) {
      msg = (aclMessage)msgQueue.elementAt(i);
      if( verifyMsg( msg, source, type, content, reply ) ) {
	//System.out.println("verifying " + msg.getDest() + " " + msg.getType());
	msgQueue.removeElementAt(i);
	return msg;
      } else msg = null;
    }

    return msg;

  }
  */

  public final void addCommListener(CommListener l) {
    listeners.addElement(l);
  }

  public final void removeCommListener(CommListener l) {
    listeners.removeElement(l);
  }

  public final synchronized void postMessage (ACLMessage msg) {
    if(msg != null) msgQueue.addElement(msg);
    System.out.println("Agent: receiving from " + msg.getSource());
    notify();
  }

}



