/*
  $Id$
*/

package fipa.core;

import java.rmi.*;

public class StartPlatform {

  public static void main(String args[]) {
    System.setSecurityManager(new RMISecurityManager());
    AgentPlatform thePlatform = null;

    // Default values for binding to the RMI registry
    String platformName = "JADE";
    String platformHost = "localhost";
    String platformPort = "1099";

    try{

      int n = 0;
      while( n < args.length ){
	if(args[n].equals("-host")) {
	  if(++n  == args.length) usage();
	  platformHost = args[n];
	}
	else if(args[n].equals("-port")) {
	  if(++n  == args.length) usage();
	  platformPort = args[n];
	}
	else if(args[n].equals("-name")) {
	  if(++n  == args.length) usage();
	  platformName = args[n];
	}
	else if(args[n].equals("-help") || args[n].equals("-h")) {
	  usage();
	}
      }
    } 
    catch( Exception e ) {
      e.printStackTrace();
      System.exit(1);
    }

    String platformURL = "rmi://" + platformHost + ":" + platformPort + "/" + platformName;

    try {
      thePlatform = new AgentPlatformImpl();
      Naming.bind(platformURL, thePlatform);
    }
    catch(AlreadyBoundException abe) {
      System.err.print("Some other Agent Platform is already active on this host, using the name " + platformName);
      System.err.print(" and the port " + platformPort);
      System.exit(1);
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while starting Agent Platform.");
      re.printStackTrace();
      System.exit(1);
    }
    catch(Exception e) {
      System.err.println("Some other error while starting Agent Platform");
      e.printStackTrace();
      System.exit(1);
    }

    System.out.println("Agent Platform started.");
  }

  private static void usage() {
    System.out.println("Usage: java StartPlatform [options]");
    System.out.println("");
    System.out.println("where options are:");
    System.out.println("");
    System.out.println("  -host\tHost where RMI registry for the platform is located");
    System.out.println("  -port\tThe port where RMI registry for the platform resides");
    System.out.println("  -name\tThe name with which the platform is bound in RMI registry");
    System.out.println("");
    System.exit(3);
  }

}
