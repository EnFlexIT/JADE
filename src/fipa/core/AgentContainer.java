/*
 * $Id$
 */

package fipa.core;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


import java.net.*;
import java.io.*;
import java.util.*;


/***********************************************************************************

  Name: AgentContainer

  Responsibilities and Collaborations:

  + Creates agents on the local Java VM, and starts the communication thread.
    (Agent, MessageDispatcher)

  + Connects each newly created agent and the communication thread, to
    allow event-based interaction between the two.
    (Agent, MessageDispatcher)

  + Holds RMI object references for all the other agent containers of the platform.

  + Routes outgoing messages to the suitable communication thread, caching
    remote agent addresses.
    (Agent, AgentDescriptor, MessageDispatcher)

**************************************************************************************/
public class AgentContainerImpl extends UnicastRemoteObject implements AgentContainer, CommListener {

  private readInputThread           r;
  private int                       port;
  private String                    host;
  private boolean                   hasACCMaster;
  private boolean                   hasAMSMaster;

  public static void main( String[] args ) {

    Vector          agents = new Vector(); // FIXME: An hashtable would be better

    String os      = new String( System.getProperty("os.name") );
    String file = new String();
    if( os.equals("Windows NT") ) file = new String("file:///");
    else                          file = new String("file://");
    String userdir = new String( file+System.getProperty("user.dir") + "/");

    try{

      codeBase     = new URL( userdir.replace( '\\', '/' ) );

      int n = 0;
      while( n < args.length ){

	if( args[n].equals("-n") ) {
	  if( ++n  == args.length ) usage();
	  agents.addElement( new String( args[n] ) );
	} else if( args[n].equals("-g") ) {
	  if( ++n  == args.length ) usage();
	  Debug.setLevel(Integer.parseInt(args[n]));
	}
	n++;
      }

    } catch( Exception e ) { e.printStackTrace(); }

    if (Debug.getLevel() > 0) {
       System.out.print("Commandline: AgentContainer ");
       int n = 0;
       while( n < args.length ) System.out.print(args[n++] + " ");
       System.out.println();
    }
    AcentContainer theContainer = new AgentContainer(agents, args);

  }

  public AgentContainer( Vector agents, String[] args ) {

    Agent agent = null;

    r = new readInputThread();
    r.setPriority( Thread.MIN_PRIORITY );
    r.start();
    r.addCommListener( this );

    hasACCMaster = false;
    hasAMSMaster = false;
    for( int i=0; i<agents.size(); i++ ) {
      String s = (String)agents.elementAt(i);
      if( s.equalsIgnoreCase("acc") ) hasACCMaster = true;
      if( s.equalsIgnoreCase("ams") ) hasAMSMaster = true;
    }

    MessageDispatcher proxy;
    if( !hasAMSMaster ) {
      proxy = new MessageDispatcher();
      proxy.start();

      for( int i=0; i<agents.size(); i++ ) {
	String agentName = (String)agents.elementAt(i);
	System.out.println("new agent: " + agentName);
	try {
	  agent = (Agent)Class.forName(new String("Agents."+agentName.toLowerCase())).newInstance();
	  agent.play( host, port, relativeURL, false, args );
	} catch( Exception e ){
	  e.printStackTrace();
	}
	proxy.addAgent( agent );
	proxy.addCommListener( agent );
	agent.addCommListener( proxy );
      }
    } else {
      try {
	while( true ){
	  System.out.println("Accepting connections ...");
	  new ams( PORT, sock, addresses, sockets );
	}
      } catch( IOException ioe ) {
	ioe.printStackTrace();
      }
    }

  }

  public void CommHandle( CommEvent event ) {
    System.out.println(event.getSource());
    System.out.println(event.getCommand());
  }

  private static void usage(){
    System.out.println("Usage: java AgentContainer [-n new_agent] [-g debug_level]");
    System.exit(3);
  }

}
