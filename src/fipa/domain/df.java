/*
 * $Id$
 */

package Agents;

import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;

import Bare.*;

public class df extends Agent {

  private Vector       agents;

  public void local_startup() {

    myName = new String("DF");
    myService = null;
    agents = new Vector();

    addLightTask( new String( "parseMsg" ) );

  }

  public int parseMsg() {

    aclMessage msg = null;
    for( int i=0; i<msgQueue.size(); i++ ) {
      msg = (aclMessage)msgQueue.elementAt(i);

      String dest = msg.getDest();
      String msgType = msg.getType();
      String content = msg.getContent();

      if( dest.equals(agentName) ) {
	if( msgType.equals("request") ) {
	  try {
	    StreamTokenizer st = new StreamTokenizer( new  StringBufferInputStream( content ));
	    st.wordChars( '/', '/' );
	    st.wordChars( ':', ':' );
	    st.wordChars( '-', '-' );
	    st.nextToken();
	    st.nextToken();
	    if( st.sval.equals("search") ) {
	      if( Debug.getLevel() > 0 ) System.out.println("       ...a search...");
	      st.nextToken();
	      st.nextToken();
	      if( st.sval.equals(":agent-address") ) {
		/*
		st.nextToken();
		String name = st.sval;
		sendAddress( name, msg, null );
		*/
	      } else if( st.sval.equals(":agent-name") ) {
		st.nextToken();
		st.nextToken();
		String name = new String( st.sval );
		st.nextToken();
		st.nextToken();
		st.nextToken();
		if( st.sval.equals(":agent-services") ) {
		  st.nextToken();
		  st.nextToken();
		  if( st.sval.equals(":service-type") ) {
		    st.nextToken();
		    st.nextToken();
		    String type = new String( st.sval );
		    if( name.equals("CA") && type.equals("type") ) {
		      sendAddress( name, msg, type );
		      msgQueue.removeElementAt(i);
		    }
		  }
		}
	      }
	    } else if( st.sval.equals("register") ) {
	      if( Debug.getLevel() > 0 ) System.out.println("       ...a register...");
	      st.nextToken();
	      st.nextToken();
	      if( st.sval.equals(":agent-name") ) {
		st.nextToken();
		String name = st.sval;
		st.nextToken();
		st.nextToken();
		st.nextToken();
		if( st.sval.equals(":agent-services") ) {
		  st.nextToken();
		  st.nextToken();
		  if( st.sval.equals(":service-type") ) {
		    st.nextToken();
		    String service = st.sval;
		    //peerService = new String( service );
		    updateService( msg.getSource(), name, service );
		    msgQueue.removeElementAt(i);
		  }
		}
	      }
	    } else if( st.sval.equals("deregister") ) {
	      if( Debug.getLevel() > 0 ) System.out.println("       ...a deregister...");
	    }
	  } catch( IOException ioe ) {
	    ioe.printStackTrace();
	  }
	}
      }
    }

    return 0;

  }

/**
 * E' il metodo caratteristico del DF che deve rispondere ai messaggi
 * di tipo <em>search</em> per richiedere l'indirizzo di un agente registrato.
 * Si tratta dei messaggi <em>agent-address</em> per richiedere l'indirizzo
 * di un agente di cui si conosce il nome e <em>agent-name</em> per conoscere
 * il nome degli agenti che implementano servizi. In particolare per quest'ultimo
 * caso viene sempre chiesto il nome ed il servizio dei CA della piattaforma,
 * per cui il metodo e' dedicato solo a quello.
 * <br>N.B.: Il metodo deve essere reso piu' general purpose.
 * <P>Ricordiamo che in risposta ad una <em>request</em> di questo tipo
 * il DF deve seguire il protocollo FIPA-request, quindi mandare due messaggi,
 * uno di <em>agree</em> (visto che la richiesta e' riconosciuta) ed uno di
 * <em>done</em> oppure <em>failure</em> in caso di riuscita o fallimento della richiesta.
 * @param requestedAdd E' l'indirizzo da matchare nel vettore <em>agents</em>.
 * @param msg E' il messaggio di richiesta arrivato.
 * @param type Se null e' un messaggio di <em>agent-address</em>, altrimenti e'
 * un <em>agent-name</em> e viene richiesto anche il servizio di cui l'agente e' responsabile.
 */
  private void sendAddress( String requestedAdd, aclMessage msg, String type ) {

    boolean found  = false;
    String address = null;
    String name    = null;
    String service = null;

    String source  = msg.getSource();
    String reply   = msg.getReplyWith();

    aclMessage okMsg = new aclMessage(agentName, myPort);
    okMsg.setDest( source );
    okMsg.setType("agree");
    okMsg.setOntology("fipa-agent-management");
    okMsg.setProtocol("fipa-request");
    okMsg.setReplyTo(reply);
    okMsg.setContent("search");
    send( okMsg );

    for( int i=0; i<agents.size(); i++ ) {
      AMSAgentDescription AMSDescription = (AMSAgentDescription)agents.elementAt(i);
      name = AMSDescription.getName();
      if( Debug.getLevel() > 0 ) System.out.println("df: comparing " + requestedAdd + " to " + name);
      if( name.equals(requestedAdd) ) {
	address = AMSDescription.getAddress();
	service = (String)AMSDescription.getService();
	if( Debug.getLevel() > 0 ) System.out.println("df: sending address of " + requestedAdd + " to " + source);
	break;
      }
    }

    aclMessage informAddressMsg = new aclMessage(agentName, myPort);
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
    send( informAddressMsg );

  }

  private void updateService( String source, String name, String service ) {

    aclMessage okMsg = new aclMessage(agentName, myPort);
    okMsg.setDest( source );
    okMsg.setType("agree");
    okMsg.setOntology("fipa-agent-management");
    okMsg.setProtocol("fipa-request");
    okMsg.setContent("register");
    send( okMsg );

    AMSAgentDescription AMSDescription = new AMSAgentDescription( source );
    AMSDescription.setService( service );
    agents.addElement( AMSDescription );
    okMsg = new aclMessage(agentName, myPort);
    okMsg.setDest( source );
    okMsg.setType("inform");
    okMsg.setOntology("fipa-agent-management");
    okMsg.setProtocol("fipa-request");
    okMsg.setContent("(done (register))");
    send( okMsg );

  }

}
