/*
 * $Id$
 */

package fipa.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.net.*;
import java.io.*;
import java.util.*;

import Parser.*;

/***********************************************************************

  Name: MessageDispatcher

  Responsibilities and Collaborations:

  + Receives incoming messages from other containers or from the
    platform registry.
    (ACLmessage, AgentPlatform)

  + Maintains a collection of local agents, indexed by agent names.
    (Agent)

  + Builds a suitable ACL message from the received string.
    (ACLmessage, ACLParser)

  + Notifies receiver agent, enqueueing the incoming message.
    (ACLmessage, Agent)

************************************************************************/
interface MessageDispatcher extends Remote {

  void dispatch(ACLMessage msg) throws RemoteException;
}

class MessageDispatcherImpl extends UnicastRemoteObject 
implements MessageDispatcher, CommListener, CommBroadcaster {

  private    Vector       listeners;

  private    ACLParser    parser = null;

  private    Hashtable    localAgents;

  private    long         lastTime;

  public MessageDispatcher( String HOST, int PORT ) {

    ACCHost = new String( HOST );
    ACCPort = PORT;

    localAgents = new Vector();
    listeners = new Vector();

    try{
      if( sock == null ) {
	sock = new Socket(ACCHost, ACCPort);
	os   = sock.getOutputStream();
	is   = sock.getInputStream();
      }
      myPort = sock.getLocalPort();
    } catch( Exception ioe ) {
      ioe.printStackTrace();
    }

  }

  public void run() {

    try {
      boolean flag = true;
      while( flag ) {
        System.out.println(" Accepting connections ...");
        aclMessage msgRead = receive();
        //if( msgRead != null ) parseMsg( msgRead );
        //else                  flag = false;
	CommEvent event = new CommEvent( this, msgRead );
	processCommEvent( event );
      }
    } catch( ParseException pe ) {
      connectionClosed();
    }

  }

  public int getLocalPort() {
    return myPort;
  }

  public String getLocalHost() {
    String s = null;
    try{
      s = ((InetAddress)InetAddress.getLocalHost()).getHostName();
    } catch( Exception ioe ) {
      ioe.printStackTrace();
    }
    return s;
  }

  public aclMessage receive() throws ParseException {

    aclMessage msgRead = null;

    parser  = new ACLParser( is );
    msgRead = parser.Message();

    return msgRead;

  }

  public void connectionClosed() {

    try {
      is.close();
      os.close();
      sock.close();
    } catch( IOException ioe ) {
      ioe.printStackTrace();
    }
    System.out.println(" closing...");
    System.exit(3);

  }

  public void addAgent( Agent a ) {
    localAgents.addElement( a );
    /* It should be ...
       AgentDescr ad = new AgentDescr();
       String address = a.getAgentName();
       ad.setHomeAddress( address );
       ad.setCurrentAddress( address );
    */
  }

  public void addCommListener( CommListener l ) {
    listeners.addElement( l );
  }

  public void removeCommListener( CommListener l ) {
    listeners.removeElement( l );
  }

  public void processCommEvent( CommEvent event ) {

    aclMessage msg = event.getMessage();
    Enumeration e = listeners.elements();
    int i = 0;
    while( e.hasMoreElements() ) {
      /* It should be ...
	 AgentDescr a = (AgentDescr)localAgents.elementAt(i);
	 String homeAdd = a.getHomeAddress();
	 String currAdd = a.getCurrentAddress();
	 ecc.
      */
      Agent a = (Agent)localAgents.elementAt(i);
      String agentName = a.getAgentName();
      String dest = msg.getDest();
      CommListener l = (CommListener)e.nextElement();
      if( agentName != null && agentName.equalsIgnoreCase(dest) ) {
	System.out.println("MessageDispatcher: sending to " + dest);
	l.CommHandle( event );
      }
      i++;
    }

  }

  public void CommHandle( CommEvent event ) {

    System.out.print("comm thread: " + event.getSource());
    //System.out.print(" " + event.getCommand());
    System.out.println(" " + event.getMessage());
    aclMessage msg = event.getMessage();

    String name = msg.getDest();

    // FIXME: Use an Hashtable
    boolean found = false;
    for( int i=0; i<localAgents.size(); i++ ) {
      Agent a = (Agent)localAgents.elementAt(i);
      String localName = a.getAgentName();
      if( localName != null && name.equalsIgnoreCase(localName) ) found = true;
    }

    if( !found ) sendRemote( msg );
    else         sendLocal(  msg );

  }

  public void sendLocal( aclMessage msg ) { // FIXME: Searches two times for the same agent !!!

    CommEvent event = new CommEvent( this, msg );
    processCommEvent( event );

  }

  public void sendRemote( aclMessage msg ) {

    try{

      long time = System.currentTimeMillis();
      while( time-lastTime < 600 ) {
	sleep(100);
	time = System.currentTimeMillis();
      }
      lastTime = time;
      msg.send();
      os.write( msg.getMessage() );
      System.out.println("MessageDispatcher: sending to: " + msg.getDest());

    } catch( Exception ioe ) {
      ioe.printStackTrace();
    }

  }

}
