package fipa.core;

import java.rmi.RemoteException;
import java.util.Vector;

public class StartContainer {

  // This separates agent name from agent class on the command line
  private static final String SEPARATOR = ":";

  public static void main(String args[]) {

    // Default values for looking up the RMI registry
    String platformHost = "localhost";
    String platformPort = "1099";
    String platformName = "FEPF";

    Vector agents = new Vector();

    /* Command line options for StartContainer:

       -host     Host name of the platform to connect to.
       -port     Port number of the platform to connect to.
       -name     Name of the platform to connect to.

       -file     File name to retrieve agent names from.
       
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
	else {
	  /* Every other string is supposed to be the name of an
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
    } catch( Exception e ) { e.printStackTrace(); }

    // Build the complete URL of the agent platform from default values and command line options
    String platformURL = "rmi://" + platformHost + ":" + platformPort + "/" + platformName;
    try{
      AgentContainer theContainer = new AgentContainerImpl(platformURL, agents); // RMI call
    }
    catch(RemoteException re) {
      System.err.println("Communication failure while starting Agent Container.");
      re.printStackTrace();
    }
    catch(Exception e) {
      System.err.println("Some other error while starting Agent Container");
      e.printStackTrace();
    }

  }

  private static void usage(){
    System.out.println("Usage: java StartContainer [options] [agent specifiers]");
    System.out.println("");
    System.out.println("where options are:");
    System.out.println("  -host\tHost where RMI registry for the platform is located");
    System.out.println("  -port\tThe port where RMI registry for the platform resides");
    System.out.println("  -name\tThe name with which the platform is bound in RMI registry");
    System.out.println("  -file\tA file name containing tne agent specifiers");
    System.out.println("");
    System.out.print("An agent specifier is made by an agent name and an agent class, separated by \"");
    System.out.println(SEPARATOR + "\"");
    System.out.println("");
    System.out.println("Examples:");
    System.out.println("  Connect to default platform, starting an agent named 'peter'");
    System.out.println("  implemented in 'myAgent' class:");
    System.out.println("  \tjava StartContainer peter:myAgent");
    System.out.println("");
    System.out.println("  Connect to a platform on host zork.zot.za, on port 1100,");
    System.out.println("  with name 'Platform', starting two agents");
    System.out.println("  java StartContainer -host zork.zot.za -port 1100 -name Platform peter:heAgent paula:sheAgent");
    System.out.println("");
    System.exit(3);
  }


}
