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
    String platformName = "FEPF";
    String platformHost = "localhost";
    String platformPort = "1099";
    String platformURL = "rmi://" + platformHost + ":" + platformPort + "/" + platformName;

    /* FIXME: Add command line options handling.
       -host host
       -port port
       -name name
    */

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
    }
    catch(Exception e) {
      System.err.println("Some other error while starting AgentPlatform");
      e.printStackTrace();
    }

    System.out.println("Agent Platform started.");
  }

}
