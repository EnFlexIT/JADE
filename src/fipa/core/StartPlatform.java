package fipa.core;

import java.rmi.*;

public class StartPlatform {

  public static void main(String args[]) {
    System.setSecurityManager(new RMISecurityManager());
    AgentPlatform thePlatform = null;

    // Default values for binding to the RMI registry
    String PlatformName = "FEPF";
    String PlatformHost = "localhost";
    String PlatformPort = ":1099"
    String PlatformURL = "rmi://" + PlatformHost + PlatformPort + "/" + PlatformName;

    /* FIXME: Add command line options handling.
       -n name
       -h host
       -p port
    */

    try {
      thePlatform = new AgentPlatformImpl();
      Naming.bind(platformURL, thePlatform);
    }
    catch(AlreadyBoundException abe) {
      System.err.print("Some other Agent Platform is already active on this host, using the name " + PlatformName);
      System.err.print(" and the port " + PlatformPort);
      System.exit(1);
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while starting Agent Platform.");
      System.exit(1);
    }
    catch(Exception e) {
      System.err.println("Some other error while starting AgentPlatform");
      System.exit(1);
    }
  }
}
