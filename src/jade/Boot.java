/*
  $Log$
  Revision 1.5  1998/10/04 18:00:41  rimassa
  Added a 'Log:' field to every source file.

*/

package jade;


import java.rmi.*;
import java.util.Vector;


import jade.core.AgentContainer;
import jade.core.AgentContainerImpl;
import jade.core.AgentPlatform;
import jade.core.AgentPlatformImpl;


public class Boot {


  // This separates agent name from agent class on the command line
  private static final String SEPARATOR = ":";

  // Private constructor to forbid instantiation
  private Boot() {
  }

  public static void main(String args[]) {

    // Default values for looking RMI registry bind/lookup
    String platformHost = "localhost";
    String platformPort = "1099";
    String platformName = "JADE";
    boolean isPlatform = false;

    Vector agents = new Vector();

    /* Command line options for Boot:

       -host     Host name where the platform is.
       -port     Port number where the RMI registry for
                 the platform is.
       -name     Name with which the Agent Platform is
                 registered into RMI registry.

       -file     File name to retrieve agent names from.

       -platform When specified, an Agent Platform is started.
                 Otherwise, an Agent Container is added to an
		 existing Agent Platform.

       -help     Prints out usage informations.

    */
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
	else if(args[n].equals("-file")) {
	  if(++n  == args.length) usage();
	  // fileName = args[n];
	  ++n;
	  // FIXME: Add reading agent names from a file
	}
	else if(args[n].equals("-platform")) {
	  isPlatform = true;
	}
	else if(args[n].equals("-help") || args[n].equals("-h")) {
	  usage();
	}
	else {
	  /* 
	     Every other string is supposed to be the name of an
	     agent, in the form name:class. The two parts must be at
	     least one character long to be put in the Vector;
	     otherwise they are ignored.
	  */
	  int separatorPos = args[n].indexOf(SEPARATOR);
	  if((separatorPos > 0)&&(separatorPos < args[n].length())) {
	    String agentName = args[n].substring(0,separatorPos);
	    String agentClass = args[n].substring(separatorPos + 1);
	    agents.addElement(new String(agentName));
	    agents.addElement(new String(agentClass));
	  }
	}
	n++;
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // This will run all finalization code when this Java VM ends.
    System.runFinalizersOnExit(true);

    // Build the complete URL of the agent platform from default
    // values and command line options, both for RMI and IIOP calls.
    String platformRMI = "rmi://" + platformHost + ":" + platformPort + "/" + platformName;
    String platformIIOP = "iiop://" + platformHost + ":" + platformPort + "/" + "acc";
    try{
      AgentContainerImpl theContainer = null;
      if(isPlatform) {
	theContainer = new AgentPlatformImpl();
	Naming.bind(platformRMI, theContainer);
      }
      else {
	theContainer = new AgentContainerImpl();
      }
      theContainer.joinPlatform(platformRMI, platformIIOP, agents);
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while starting Agent Container.");
      re.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Some other error while starting Agent Container");
      e.printStackTrace();
    }

    /* FIXME: Temporary code; will go away when a GUI administration is put in
    System.out.println("Type 'quit' to close the container.");
    String input = "";
    byte[] buffer = new byte[10];
    while(!input.equals("quit")) {
      int len = System.in.read(buffer);
      input = new String(buffer,0,len-1);
    }
    // Shut down the container

    theContainer.shutDown();
    */

  }

  private static void usage(){
    System.out.println("Usage: java jade.Boot [options] [agent specifiers]");
    System.out.println("");
    System.out.println("where options are:");
    System.out.println("  -host\t\tHost where RMI registry for the platform is located");
    System.out.println("  -port\t\tThe port where RMI registry for the platform resides");
    System.out.println("  -name\t\tThe name with which the platform is bound in RMI registry");
    System.out.println("  -file\t\tA file name containing tne agent specifiers");
    System.out.println("  -platform\tIf specified, a new Agent Platform is created.");
    System.out.println("  \t\tOtherwise a new Agent Container is added to an existing platform");
    System.out.println("");
    System.out.print("An agent specifier is made by an agent name and an agent class, separated by \"");
    System.out.println(SEPARATOR + "\"");
    System.out.println("");
    System.out.println("Examples:");
    System.out.println("  Connect to default platform, starting an agent named 'peter'");
    System.out.println("  implemented in 'myAgent' class:");
    System.out.println("  \tjava jade.Boot peter:myAgent");
    System.out.println("");
    System.out.println("  Connect to a platform on host zork.zot.za, on port 1100,");
    System.out.println("  with name 'Platform', starting two agents");
    System.out.println("  java jade.Boot -host zork.zot.za -port 1100 -name Platform peter:heAgent paula:sheAgent");
    System.out.println("");
    System.out.println("  Create an Agent Platform and starts an agent on the local Agent Container");
    System.out.println("  \tjava jade.Boot -platform Willy:searchAgent");
    System.out.println("");
    System.exit(3);
  }


}
