/*
 * $Id$
 */

package Agents;

import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;

import Bare.*;

public class ams implements Runnable, ThreadListener {

/**
 * E' il socket corrente dell'istanza del server.
 */
  private Socket       sock;

/**
 * E' il vettore che memorizza le classi AMSAgentDescription che contengono
 * le descrizioni dei vari agenti che si registrano.
 */
  private Vector       agents;

/**
 * E' il vettore che memorizza inogni elemento il socket verso l'agente corrispondenti
 * indicato dalla classe del vettore <it>agents</it>.
 */
  private Vector       sockets;

/**
 * E' il thread corrente.
 */
  private Thread       dfThread;

  private String       peerName;
  private String       peerSource;
  private String       peerService;

  private boolean      isLocal;
  private int          myPort;
  public    OutputStream os;
  public    InputStream  is;

  ACLParser parser = null;

  public ams(int dfPort, Socket sock, Vector agents, Vector sockets ) {

    this.sock      = sock;
    this.agents    = agents;
    this.sockets   = sockets;

    myPort = dfPort;
    is = null;
    os = null;
    dfThread = new Thread( this );
    dfThread.start();

  }

  public void run() {

    try{
      is = sock.getInputStream();
      os = sock.getOutputStream();
    } catch( IOException ioe ) {
      ioe.printStackTrace();
    }

    waiting();

  }

/**
 * Il thread dopo aver ricavato l'input e l'output stream del socket
 * si mette in attesa dei messaggi in arrivo da leggere e di cui poi
 * fare il parsing. Il thread da questo momento in poi sara' sempre
 * in ascolto sul socket dei messaggi in arrivo.
 */
  public void waiting() {

    try {
      while( is != null ) {
	System.out.println("receiving... ");
        aclMessage msgRead = receive();
	System.out.println("receiving from " + msgRead.getSource() + " " + msgRead.getContent());
        isLocal = false;
        parseMsg( msgRead );
      }
    } catch( ParseException pe ) {
      System.out.println("disconnecting from " + peerName + " " + peerService);
      try {
        is.close();   is = null;
        os.close();   os = null;
        sock.close(); sock = null;
      } catch( IOException ioe ) {
        ioe.printStackTrace();
      }
      //removeService( peerSource, peerName, peerService );
      //removeFromDb( peerSource, peerName );
      dfThread.stop();
    }

  }

  public aclMessage receive() throws ParseException {

    aclMessage msgRead = null;

    parser  = new ACLParser( is );
    msgRead = parser.Message();

    return msgRead;

  }

  public void ThreadChanged( ThreadEvent event ) {
    System.exit(3);
  }

/**
 * E' il metodo che deve fare il parsing di ogni messaggio letto dal socket.
 * A seconda che il destinatario sia uno dei tre agenti implementati il
 * thread chiama il metodo di corrispondente.
 * Vengono sfruttati i metodi offerti da <it>aclMessage</it>
 * e che restituiscono direttamente i campi del messaggio.
 * @see aclMessage
 */
  public void parseMsg( aclMessage msg ) {

    String dest = msg.getDest();
    String msgType = msg.getType();
    String content = msg.getContent();

    if( dest.equals("AMS") ) {
      if( msgType.equals("request") ) {
	try {
	  StreamTokenizer st = new StreamTokenizer( new  StringBufferInputStream( content ));
	  st.wordChars( '/', '/' );
	  st.wordChars( ':', ':' );
	  st.wordChars( '-', '-' );
	  st.nextToken();
	  st.nextToken();
	  if( st.sval.equals("register-agent") ) {
	    if( Debug.getLevel() > 0 ) System.out.println("       ...a register-agent...");
	    st.nextToken();
	    st.nextToken();
	    if( st.sval.equals(":agent-name") ) {
	      st.nextToken();
	      String name   = st.sval;
	      String source = msg.getSource();
	      peerName   = new String( name );
	      peerSource = new String( source );
	      updateDb( source, name );
	    }
	  } else if( st.sval.equals("deregister-agent") ) {
	    if( Debug.getLevel() > 0 ) System.out.println("       ...a deregister-agent...");
	    st.nextToken();
	    st.nextToken();
	    if( st.sval.equals(":agent-name") ) {
	      st.nextToken();
	      String name = st.sval;
	      //removeFromDb( msg.getSource(), name );
	    }
	  }
	  if( st.sval.equals("search") ) {
	    if( Debug.getLevel() > 0 ) System.out.println("       ...a search...");
	    st.nextToken();
	    st.nextToken();
	    if( st.sval.equals(":agent-address") ) {
	      st.nextToken();
	      String name = st.sval;
	      sendAddress( name, msg, null );
	    }
	  }
	} catch( IOException ioe ) {
	  ioe.printStackTrace();
	}
      }
    } else {
      try {
	System.out.println("Getting address of " + dest);
	for( int i=0; i<agents.size(); i++ ) {
	  System.out.println("  comparing to " + ((AMSAgentDescription)agents.elementAt(i)).getAddress());
	  if( ((AMSAgentDescription)agents.elementAt(i)).getAddress().equals(dest) ) {
	    OutputStream nos = ((Socket)sockets.elementAt(i)).getOutputStream();
	    if( Debug.getLevel() > 0 ) msg.dump();
	    isLocal = true;
	    send( msg, nos );
	    break;
	  }
	}
      } catch( IOException ioe ) {
	ioe.printStackTrace();
      }
    }

  }

  private synchronized void updateDb( String source, String name ) {

    if( Debug.getLevel() > 0 ) System.out.println(" address: " + source + " name: " + name);
    AMSAgentDescription AMSDescription = new AMSAgentDescription();
    AMSDescription.setName( name );
    AMSDescription.setAddress( source );

    agents.addElement( AMSDescription );
    sockets.addElement( sock );

    aclMessage okMsg = new aclMessage("AMS", myPort);
    okMsg.setDest( source );
    okMsg.setType("agree");
    okMsg.setOntology("fipa-agent-management");
    okMsg.setProtocol("fipa-request");
    okMsg.setContent("register-agent");
    send( okMsg, os );

    okMsg = new aclMessage("AMS", myPort);
    okMsg.setDest( source );
    okMsg.setType("inform");
    okMsg.setOntology("fipa-agent-management");
    okMsg.setProtocol("fipa-request");
    okMsg.setContent("(done (register-agent))");
    send( okMsg, os );

  }

  private void sendAddress( String requestedAdd, aclMessage msg, String type ) {

    boolean found  = false;
    String address = null;
    String name    = null;
    String service = null;

    String source  = msg.getSource();
    String reply   = msg.getReplyWith();

    aclMessage okMsg = new aclMessage("AMS", myPort);
    okMsg.setDest( source );
    okMsg.setType("agree");
    okMsg.setOntology("fipa-agent-management");
    okMsg.setProtocol("fipa-request");
    okMsg.setReplyTo(reply);
    okMsg.setContent("search");
    send( okMsg, os );

    for( int i=0; i<agents.size(); i++ ) {
      AMSAgentDescription AMSDescription = (AMSAgentDescription)agents.elementAt(i);
      name = AMSDescription.getName();
      if( name.equals(requestedAdd) ) {
	address = AMSDescription.getAddress();
	service = (String)AMSDescription.getService();
	if( Debug.getLevel() > 0 ) System.out.println("AMS: sending address of " + requestedAdd + " to " + source);
	break;
      }
    }

    aclMessage informAddressMsg = new aclMessage("AMS", myPort);
    informAddressMsg.setDest(source);
    informAddressMsg.setOntology("fipa-agent-management");
    informAddressMsg.setProtocol("fipa-request");
    informAddressMsg.setReplyTo(reply);
    if( source == null ) {
      if( Debug.getLevel() > 0 ) System.out.println("destination unresolved");
      informAddressMsg.setType("failure");
      informAddressMsg.setContent("search");
      System.exit(3);
    } else {
      informAddressMsg.setType("inform");
      if( type == null ) informAddressMsg.setContent("(result (:agent-name " + address + "))");
      else               informAddressMsg.setContent("(result ((:agent-name " + address + " :agent-services (:service-type " + service + "))))");
    }
    send( informAddressMsg, os );

  }

  private void send( aclMessage msg, OutputStream localOs ) {

    try {
      msg.send();
      localOs.write( msg.getMessage() );
      try {
	Thread.sleep(600);
      } catch (Exception e) {
      }
    } catch( IOException ioe ) {
      ioe.printStackTrace();
    }

  }

}
