/*
 * $Id$
 */

package fipa.core;

import java.net.*;
import java.io.*;
import java.util.*;
import java.applet.*;
import java.lang.reflect.*;

import fipa.lang.acl.*;

/**************************************************************

  Name: Agent

  Responsibility and Collaborations:

  + Abstract placeholder for user-defined agents.

  + Provides primitives for sending and receiving messages.
    (ACLmessage)

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
public abstract class Agent implements Runnable, CommBroadcaster {

/**
 * Pending messages queue
 */
  protected Vector       msgQueue;

  protected aclMessage   lastMessage;

  ACLParser parser = null;


  protected    boolean      isApplet;

  protected    Thread       myThread;

  protected    boolean      isActive = false;

  protected    Vector       actions;

  protected    String[]     args;

  protected    String       myName;
  protected    String       myService;


  public void activate(String[] args) { // FIXME: Check with FIPA specs for agent lifecycle

    this.args = args;

    listeners = new Vector();

    msgQueue = new Vector();
    actions  = new Vector();

    isActive = true;

    services = new Vector(); // FIXME: Maybe it's not needed

    myThread = new Thread(this);
    myThread.start();
  }

  protected void local_startup() {}

  public void run() {

    int ret;
    Class[] params = new Class[0];
    Object[] args  = new Object[0];

    local_startup();

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
	System.out.println(agentName + ": invoking " + s);
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

  public void addLightTask( String task ) {
    actions.addElement( task );
  }

/**
 * Serve per il parsing dei messaggi.
 * Ogni agente si deve implementare il proprio.
 */
  protected void parseMsg( aclMessage msg ) {}

/**
 * Metodo per spedire un aclMessage.
 * @param msg Il messaggio da spedire.
 * @see aclMessage
 */
  public void send( aclMessage msg ) {

    CommEvent event = new CommEvent( this, msg );
    processCommEvent( event );

  }

/**
 * Metodo per fare il match fra un messaggio ed una serie di campi dati.
 * @param msg E' il messaggio da controllare
 * Gli altri parametri sono i campi da macthare.
 * @see aclMessage
 * @return Ritorna falso se il messaggio e' sbagliato.
 */
  public boolean verifyMsg( aclMessage msg, String source, String type, String content, String reply ) {
    // FIXME: Check with Paolo whether this is still needed...
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

/**
 * Metodo per ricevere un messaggio con determinati campi.
 * Prima vengono controllati i messaggi non consumati nella coda msgQueue.
 * Quindi eventualmente si attendono i messaggi in arrivo sul socket
 * I messaggi ricevuti ma non corretti vengono accodati a msgQueue.
 * @see Agent#receive
 * @see aclMessage
 * @return Ritorna il messaggio corretto oppure null.
 */
  public synchronized aclMessage receiveIf( String source, String type, String content, String reply ) {
    // FIXME: Check with Paolo whether this is still needed...
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

  public void addCommListener(CommListener l) {
    listeners.addElement(l);
  }

  public void removeCommListener(CommListener l) {
    listeners.removeElement(l);
  }

  public void processCommEvent(CommEvent event) {

    Enumeration e = listeners.elements();
    while( e.hasMoreElements() ) {
      CommListener l = (CommListener)e.nextElement();
      l.CommHandle(event);
    }

  }

  public synchronized void localCommChanged(CommEvent event) {}

  public synchronized void postMessage (ACLmessage msg) {
    if(msg != null) msgQueue.addElement(msg);
    System.out.println("Agent: receiving from " + msg.getSource());
    notify();
  }

}



