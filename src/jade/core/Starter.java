package jade.core;

import java.rmi.*;
import java.rmi.registry.*;

import java.util.Vector;

/**
   This utility class is a <em>Facade</em> to JADE runtime system and
   is only used to start up JADE.
   @see jade.Boot
 */
public class Starter {

  // Private constructor to forbid instantiation
  private Starter() {
  }

  // The singleton Agent Container
  private static AgentContainerImpl theContainer;

  static AgentContainerImpl getContainer() {
    return theContainer;
  }

  /**
     Starts up a suitable JADE runtime system, according to its
     parameters.

     @param isPlatform <code>true</code> if <code>-platform</code> is
     given on the command line, <code>false</code> otherwise.
     @param platformRMI An <em>URL</em> for the platform <em>RMI</em>
     address, built from JADE default settings and command line
     parameters.
     @param agents A <code>Vector</code> containing names and classes
     of the agents to fire up during JADE startup.
     @param args Command line arguments, used by CORBA ORB.
   */
  public static void startUp(boolean isPlatform, String platformRMI, Vector agents, String args[]) {

    try{

      if(isPlatform) {
	theContainer = new AgentPlatformImpl(args);

	// Create an embedded RMI Registry within the platform and
	// bind the Agent Platform to it

	int colonPos = platformRMI.lastIndexOf(':');
	int slashPos = platformRMI.indexOf('/', colonPos + 1);
	
	String platformPort = platformRMI.substring(colonPos + 1, slashPos);

	int port = Integer.parseInt(platformPort);
	Registry theRegistry = LocateRegistry.createRegistry(port);
	Naming.bind(platformRMI, theContainer);

      }
      else {
	theContainer = new AgentContainerImpl(args);
      }
      theContainer.joinPlatform(platformRMI, agents);

    }
    catch(RemoteException re) {
      System.err.println("Communication failure while starting JADE Runtime System.");
      re.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Some other error while starting JADE Runtime System.");
      e.printStackTrace();
    }

  }

}
