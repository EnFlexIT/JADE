/*
 * $Id$
 */

package fipa.core;

import java.net.*;
import java.io.*;
import java.util.*;
import java.applet.*;
import java.lang.reflect.*;
import netscape.security.PrivilegeManager;

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
 * All the agents belonging to the agent platform must inherit from this Agent template.
 * Agent is composed by a single execution thread for the active part.
 * All the tasks that must be performed by an agent are implemenmted as methods,
 * which are called and scheduled in a round-robin fashion by the agent scheduler.
 * All general purpose tasks that can be carried out by all platform agents
 * are already available in the sbstract Agent class as methods.
 * In particular the <em>register-agent</em> (the registration with the AMS)
 * and the <en>register</em> (the registration with the DF) actions
 * are called by default by the initialization Agent method.
 * The following tasks are also provided to the Agent developer by the Agent abstract class:
 * deregister, deregister-agent, retrieveDFAddress and getServices.
 * All the specific tasks an agent has to implement must be added as further methods to the
 * particular Agent derived class.
 * The abstract Agent class provides to the agent developer also some methods
 * able to implement particular FIPA protocols or simply some communication paradigms:
 * FipaRequest (corresponding to the FIPA-request protocol), SendReceive (when
 * a message requires only a single answer notification), blockingSendReceive
 * (the corresponding blocking paradigm).
 * In fact, by default all protocols and communication paradigms are non-blocking.
 * In order to allow the user to be able to add its own tasks, the method
 * addTask is provided by the abstract Agent, requiring as a parameter
 * the name of the method implementing the task. As soon as the method is called,
 * the task specified is added to the queue of ready tasks for the scheduler.
 * 
 * @version 2.0 5/98
 *
 */
public abstract class Agent implements Runnable, CommListener, CommBroadcaster {

/**
 * Agent physical address.
 */
  protected String agentName;

  protected URL codeBase;
  protected Vector listeners;

/**
 * Pending messages queue
 */
  protected Vector       msgQueue;

  protected aclMessage   lastMessage;

  ACLParser parser = null;


  protected    boolean      isApplet;

  protected    Thread       myThread;

  protected    boolean      isActive;
  protected    boolean      isRegisteredWithDf;

  protected    Vector       actions;
  protected    int          statRegister;
  protected    int          statDeregister;
  protected    int          statDeregisterAgent;
  protected    int          statDFRetrieve;
  protected    int          statSearchServices;

  protected    String[]     args;

  protected    String       myName;
  protected    String       myService;

/**
 * Nel caso l'agente sia un applet bisogna abilitare i privilegi di connessione,
 * chiedendo l'autorizzazione all'utente.
 */
  public void play( String host, int port, URL codeBase, boolean isApplet, String[] args ) {

    this.isApplet = isApplet;
    this.codeBase = codeBase;
    this.args     = args;
    myPort = port; // FIXME: To remove

    listeners = new Vector();

    AMSDescription = new AMSAgentDescription(host, port);

    msgQueue = new Vector();
    actions  = new Vector();

    isActive           = true;
    isRegisteredWithDf = false;

    statRegister = 0;
    statDeregister = 0;
    statDeregisterAgent = 0;
    statDFRetrieve = 0;

    services  = new Vector();

    myThread = new Thread( this );
    myThread.start();

  }

  protected void local_startup() {}

  public void run() {

    int ret;
    Class[] params = new Class[0];
    Object[] args  = new Object[0];

    local_startup();
    AMSDescription.setPort( myPort );
    AMSDescription.setName( myName );

    System.out.println(myName + ": initializing...");

    registerAgent();
    if( !myName.equalsIgnoreCase("df") ) {
      addLightTask( new String( "retrieveDFAddress" ) );
      addLightTask( new String( "register" ) );
    }

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
	  Method m = this.getClass().getMethod(s, params);
	  try{
	    ret = ((Integer)m.invoke( this, args )).intValue();
	    if( ret == 1 ) actions.removeElementAt(i);
	  } catch( InvocationTargetException e ){
	    e.printStackTrace();
	  } catch( IllegalAccessException e ){
	    e.printStackTrace();
	  }
	} catch( NoSuchMethodException e ){
	  System.out.println(" method " + s + " not found ! ");
	  System.exit(3);
	}

	//Thread.yield();
	try {
	  Thread.sleep(500);
	} catch( InterruptedException e ) {
	}

      }
    }

  }

  public void addLightTask( String task ) {
    actions.addElement( task );
  }

  public synchronized void blockingFipaRequest( String content, String dest, String cont1, String cont2 ) {

    aclMessage registerMsg = new aclMessage(agentName, myPort);
    registerMsg.setDest( dest );
    registerMsg.setOntology("fipa-agent-management");
    registerMsg.setProtocol("fipa-request");
    registerMsg.setType("request");
    registerMsg.setContent( content );

    send( registerMsg );

    aclMessage okMsg = receiveIf( dest, "agree",  cont1, null );
    while( okMsg == null ) {
      //System.out.println(agentName + " getting null message");
      try {
	wait();
      } catch( InterruptedException ioe ) {
	ioe.printStackTrace();
      }
      //System.out.println(agentName + " verifying 1...");
      okMsg = receiveIf( dest, "agree", cont1, null );
    }
    //System.out.println(agentName + " getting " + okMsg.getType());

    okMsg            = receiveIf( dest, "inform", cont2, null );
    while( okMsg == null ) {
      //System.out.println(agentName + " getting 2 null message");
      try {
	wait();
      } catch( InterruptedException ioe ) {
	ioe.printStackTrace();
      }
      //System.out.println(agentName + " verifying 2...");
      okMsg = receiveIf( dest, "inform", cont2, null );
    }
    System.out.println(agentName + " exiting blockingFipaRequest...");

  }

/**
 * Implementa il protocollo FIPA-request,
 * controllando che arrivino un "agree" ed un "inform"
 * e matchando la destinazione ed il content di entrambi i messaggi.
 * @see aclMessage
 * @return Ritorna il content del secondo messaggio.
 */
  public int FipaRequest( String content, String reply, String dest, String cont1, String cont2, int status ) {

    int returnValue = status;
    aclMessage okMsg = null;

    if( returnValue == 0 ) {
      aclMessage registerMsg = new aclMessage(agentName, myPort);
      registerMsg.setDest( dest );
      registerMsg.setOntology("fipa-agent-management");
      registerMsg.setProtocol("fipa-request");
      if( reply != null ) registerMsg.setReplyWith(reply);
      registerMsg.setType("request");
      registerMsg.setContent(content);
      
      send( registerMsg );
      returnValue++;
    }
    if( returnValue == 1 ) {
      okMsg = receiveIf( dest, "agree",  cont1, reply );
      if( okMsg != null ) {
	returnValue++;
	System.out.print(agentName + " getting 1... ");
	System.out.println(okMsg.getReplyTo());
      } else return returnValue;
    }
    if( returnValue == 2 ) {
      okMsg = receiveIf( dest, "inform", cont2, reply );
      if( okMsg != null ) {
	returnValue++;
	System.out.print(agentName + " getting 2... ");
	System.out.println(okMsg.getReplyTo());
      } else return returnValue;
    }

    lastMessage = okMsg;

    return returnValue;

  }

  public synchronized void blockingSendReceive( String type, String content, String reply, String dest, String type1 ) {

    aclMessage okMsg = null;

    aclMessage registerMsg = new aclMessage(agentName, myPort);
    registerMsg.setDest( dest );
    if( reply != null ) registerMsg.setReplyWith(reply);
    registerMsg.setType(type);
    registerMsg.setContent(content);
    
    send( registerMsg );

    okMsg = receiveIf( dest, type1, null, reply );
    while( okMsg == null ) {
      //System.out.println(agentName + " getting null message");
      try {
	wait();
      } catch( InterruptedException ioe ) {
	ioe.printStackTrace();
      }
      //System.out.println(agentName + " verifying 1...");
      okMsg = receiveIf( dest, type1, null, reply );
    }

    lastMessage = okMsg;

  }

  public int SendReceive( String type, String content, String reply, String dest, String type1, int status ) {

    int returnValue = status;
    aclMessage okMsg = null;

    if( returnValue == 0 ) {
      aclMessage registerMsg = new aclMessage(agentName, myPort);
      registerMsg.setDest( dest );
      if( reply != null ) registerMsg.setReplyWith(reply);
      registerMsg.setType(type);
      registerMsg.setContent(content);
      
      send( registerMsg );
      returnValue++;
    }
    if( returnValue == 1 ) {
      okMsg = receiveIf( dest, type1, null, reply );
      if( okMsg != null ) {
	returnValue++;
	System.out.print(agentName + " getting 1... ");
	System.out.println(okMsg.getReplyTo());
      } else return returnValue;
    }

    lastMessage = okMsg;

    return returnValue;

  }

/**
 * Implementa la registrazione con il DF, indicando il servizio di competenza.
 * @param myName E' il nome logico dell'agente.
 */
  public int register() {

    if( dfAddress != null ) {
      String content = new String();
      if( myService == null )
	content = content.concat("(register (:agent-name " + myName + ") (:agent-services (:service-type any)))");
      else
	content = content.concat("(register (:agent-name " + myName + ") (:agent-services (:service-type " + myService + ")))");
      statRegister = FipaRequest( content, null, dfAddress, "register", "(done (register", statRegister );
    }

    if( statRegister == 3 ) {isRegisteredWithDf = true; return 1;}
    else                    return 0;

  }

/**
 * Implementa la registrazione con l'AMS
 * @param myName E' il nome logico dell'agente.
 */
  public void registerAgent() {

    agentName = new String( AMSDescription.getAddress() );
    String content = new String("(register-agent (:agent-name " + myName + "))");
    blockingFipaRequest( content, "AMS", "register-agent", "(done (register-agent" );

  }

/**
 * Implementa la deregistrazione con l'AMS.
 * @param myName E' il nome logico dell'agente.
 */
  public void deregisterAgent() {

    aclMessage registerMsg = new aclMessage(agentName, myPort);
    registerMsg.setDest("AMS");
    registerMsg.setOntology("fipa-agent-management");
    registerMsg.setProtocol("fipa-request");
    registerMsg.setType("request");
    registerMsg.setContent("(deregister-agent (:agent-name " + myName + "))");

    send( registerMsg );

    //statDeregisterAgent = FipaRequest( "AMS", "deregister-agent", "(done (deregister-agent", statDeregisterAgent, 1 );

  }

/**
 * Implementa la deregistrazione con il DF, indicando il servizio di competenza.
 * @param myName E' il nome logico dell'agente.
 */
  public void deregister() {

    aclMessage registerMsg = new aclMessage(agentName, myPort);
    registerMsg.setDest("DF");
    registerMsg.setOntology("fipa-agent-management");
    registerMsg.setProtocol("fipa-request");
    registerMsg.setType("request");
    if( myService == null )
      registerMsg.setContent("(deregister (:agent-name " + myName + " :agent-services (:service-type any)))");
    else
      registerMsg.setContent("(deregister (:agent-name " + myName + " :agent-services (:service-type " + myService + ")))");

    send( registerMsg );

    //statDeregister = FipaRequest( "DF", "deregister", "(done (deregister", statDeregister, 1 );

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

/**
 * Metodo per ricavare dal DF l'indirizzo di un agente.
 * @param agent E' il nome logico dell'agente da ricercare.
 * Per motivi storici in pratica serve solo per richiedere l'indirizzo dell'UPA.
 * <B>Dovrebbe essere generalizzato</B>
 * Riempie la stringa upaAddress.
 */
  public int retrieveDFAddress() {

    String content = new String("(search (:agent-address DF))");
    String reply   = new String("DFaddress");
    statDFRetrieve = FipaRequest( content, reply, "AMS", "search", "(result", statDFRetrieve );
    System.out.println(agentName + ": statDFRetrieve " + statDFRetrieve);
    if( statDFRetrieve == 3 && lastMessage != null ) {
      content = lastMessage.getContent();
      String inReply = lastMessage.getReplyTo();
      if( inReply.equals( reply ) ) {
	try {
	  StreamTokenizer st = new StreamTokenizer( new  StringBufferInputStream( content ));
	  st.wordChars( '/', '/' );
	  st.wordChars( ':', ':' );
	  st.wordChars( '@', '@' );
	  st.nextToken();
	  st.nextToken();
	  if( st.sval.equals("result") ) {
	    st.nextToken();
	    st.nextToken();
	    if( st.sval.equals(":agent-name") ) {
	      st.nextToken();
	      dfAddress = st.sval; System.out.println("DF address: " + dfAddress);
	    }
	  }
	} catch( IOException ioe ) {
	  ioe.printStackTrace();
	}
      }
    }

    if( statDFRetrieve == 3 ) return 1;
    else                      return 0;

  }

/**
 * Metodo per richiedere al DF la lista di servizi disponibili ed i relativi CA.
 */
  public int getServices() {

    if( dfAddress != null && isRegisteredWithDf ) {
      String content = new String("(search (:agent-name ?CA) (:agent-services (:service-type ?type)))");
      statSearchServices = FipaRequest( content, "services", dfAddress, "search", "(result", statSearchServices );
      System.out.println(agentName + ": statSearchServices " + statSearchServices);
      if( statSearchServices == 3 && lastMessage != null ) {
	content = lastMessage.getContent();
	String inReply = lastMessage.getReplyTo();
	if( inReply.equals( "services" ) ) {
	  try {
	    StreamTokenizer st = new StreamTokenizer( new  StringBufferInputStream( content ));
	    st.nextToken();
	    st.nextToken();
	    if( st.sval != null && st.sval.equals("result") ) {
	      st.wordChars( '/', '/' );
	      st.wordChars( ':', ':' );
	      st.wordChars( '@', '@' );
	      st.nextToken();
	      st.nextToken();
	      st.nextToken();
	      if( st.sval.equals(":agent-name") ) {
		st.nextToken();
		if( st.sval.substring(0,2).equals("CA") ) {
		  if (Debug.getLevel() > 1) System.out.println("CA address: " + st.sval);
		  caAddress.addElement( new String(st.sval) );
		  st.nextToken();
		  if( st.sval.equals(":agent-services") ) {
		    st.nextToken();
		    st.nextToken();
		    if( st.sval.equals(":service-type") ) {
		      st.nextToken();
		      if (Debug.getLevel() > 1) System.out.println("service: " + st.sval);
		      services.addElement( st.sval );
		    }
		  }
		}
	      }
	    }
	  } catch( IOException ioe ) {
	    ioe.printStackTrace();
	  }
	}
	return 1;
      } else return 0;
    }
    return 0;

  }

  public String getAgentName() {
    if( AMSDescription.getAddress() != null ) 
      return new String( AMSDescription.getAddress() );
    else
      return null;
  }

  public void CommListener( CommListener l ) {
    listeners.addElement( l );
  }

  public void removeCommListener( CommListener l ) {
    listeners.removeElement( l );
  }

  public void processCommEvent( CommEvent event ) {

    Enumeration e = listeners.elements();
    while( e.hasMoreElements() ) {
      CommListener l = (CommListener)e.nextElement();
      l.CommHandle( event );
    }

  }

  public synchronized void localCommChanged( CommEvent event ) {}

  public synchronized void CommChanged( CommEvent event ) {

    aclMessage msg = event.getMessage();
    if( msg != null ) msgQueue.addElement( msg );
    System.out.println("Agent: receiving from " + msg.getSource());
    notify();

  }

}
