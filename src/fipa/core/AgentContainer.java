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

  + Connects each newly created agent and the message dispatcher, to
    allow event-based interaction between the two.
    (Agent, MessageDispatcher)

  + Holds RMI object references for all the other message dispatcher of the platform.
    (MessageDispatcher)

  + Routes outgoing messages to the suitable message dispatcher, caching
    remote agent addresses.
    (Agent, AgentDescriptor, MessageDispatcher)

**************************************************************************************/
class AgentContainer implements CommListener {

  private readInputThread r; // FIXME: Check with Paolo whether it is still needed

  private Hashtable agents = new Hashtable(MAP_SIZE, MAP_LOAD_FACTOR);

  private static final int MAP_SIZE = 20;
  private static final float MAP_LOAD_FACTOR = 0.50f;

  public static void main( String[] args ) {

    Vector agentNames = new Vector();

    try{

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
    AgentContainer theContainer = new AgentContainer(agents, args);

  }

  public AgentContainer( Vector agentNames, String[] args ) {

    Agent agent = null;

    r = new readInputThread();
    r.setPriority( Thread.MIN_PRIORITY );
    r.start();
    r.addCommListener(this);

    MessageDispatcher proxy = new MessageDispatcherImpl();

      for( int i=0; i<agents.size(); i++ ) {
	String agentName = (String)agentNames.elementAt(i);
	System.out.println("new agent: " + agentName);
	try {
	  agent = (Agent)Class.forName(new String(agentName.toLowerCase())).newInstance();
	  agent.activate(args); // FIXME: Check with FIPA specs for agent lifecycle
	} 
	catch( Exception e ){
	  e.printStackTrace();
	}
	proxy.addAgent( agent );
	proxy.addCommListener( agent );
	agent.addCommListener(this);
      }
  }

  public void CommHandle( CommEvent event ) {
    // Get ACL message from the event.
    // Look up in local agents.
    // If it fails, look up in remote agents.
    // If it fails again, ask the Agent Platform.
    // If still fails, raise NotFound exception.
  }

  private static void usage(){
    System.out.println("Usage: java AgentContainer [-n new_agent] [-g debug_level]");
    System.exit(3);
  }

}
