/*
  $Log$
  Revision 1.19  1999/06/04 07:41:05  rimassa
  Removed any direct relation with AgentContainer and AgentPLatform
  classes. Now class jade.core.Starter is used instead.

  Revision 1.18  1999/05/20 15:41:41  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.17  1999/04/06 00:09:19  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.16  1999/03/29 10:31:18  rimassa
  Maintained a singleton for the current AgentContainer, to allow
  calling createAgent() from Agent class (for agent factories).

  Revision 1.15  1999/02/25 08:03:26  rimassa
  Removed older, commented out code.

  Revision 1.14  1999/02/14 22:57:23  rimassa
  Put back System.runFinalizersOnExit() call, for now.

  Revision 1.13  1999/02/03 09:38:41  rimassa
  Commented out deprecated method call 'System.runFinalizersOnExit(true)'.
  Delegated IIOP platform address creation to AgentPlatform class.

  Revision 1.12  1998/12/20 02:03:49  rimassa
  Modified '-version' command line option handling. Now some better
  formatting is used on CVS strings before printing them.

  Revision 1.11  1998/12/08 00:27:44  rimassa
  Updated version number.

  Revision 1.10  1998/11/09 22:10:08  Giovanni
  Added explanation of '-version' option in usage() method.

  Revision 1.9  1998/11/07 23:05:08  rimassa
  Removed explicit "localhost" default value for platformHost; now
  InetAddress.getLocalHost() is used to get local host name implicitly.
  In '-version' command line option handling, CVS keywords are used to
  express JADE version and build date.

  Revision 1.8  1998/10/30 18:17:17  rimassa
  Added a '-version' command-line option to print out JADE version name.

  Revision 1.7  1998/10/25 23:52:46  rimassa
  Added '-gui' command line option to start a Remote Agent Management
  GUI.

  Revision 1.6  1998/10/18 12:40:41  rimassa
  Added code to create an RMI registry embedded within the Agent
  Platform; now you don't need to run rmiregistry anymore.
  Removed some older, commented out code.

  Revision 1.5  1998/10/04 18:00:41  rimassa
  Added a 'Log:' field to every source file.

*/

package jade;


import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Vector;

/**
   Boots <B><em>JADE</em></b> system, parsing command line arguments.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public class Boot {


  // This separates agent name from agent class on the command line
  private static final String SEPARATOR = ":";

  // Private constructor to forbid instantiation
  private Boot() {
  }


  /**
   * Fires up <b><em>JADE</em></b> system.
   * This method starts the bootstrap process for <B><em>JADE</em></b>
   * agent platform. It parses command line arguments and acts
   * accordingly.
   * Valid command line arguments:
   *
   * <ul>
   * <li>  <b>-host</b>     <em>Host name where the platform is.</em>
   * <li>  <b>-port</b>     <em>Port number where the RMI registry for
   *                            the platform is.</em>
   * <li>  <b>-name</b>     <em>Name with which the Agent Platform is
   *                            registered into RMI registry.</em>
   * <li>  <b>-file</b>     <em>File name to retrieve agent names from.</em>
   * <li>  <b>-gui</b>      <em>Starts the Remote Management Agent.</em>
   * <li>  <b>-platform</b> <em>When specified, an Agent Platform is started.
   *                            Otherwise, an Agent Container is added to an
   *                            existing Agent Platform.</em>
   * <li>  <b>-version</b>  <em>Prints out version information and exits.</em>
   * <li>  <b>-help</b>     <em>Prints out usage informations.</em>
   * </ul>
   *
   */
  public static void main(String args[]) {

    // Default values for looking RMI registry bind/lookup

    String platformHost = null;
    try {
      platformHost = InetAddress.getLocalHost().getHostName();
    }
    catch(UnknownHostException uhe) {
      System.out.print("Unknown host exception in getLocalHost(): ");
      System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
      System.exit(1);
    }

    String platformPort = "1099";
    String platformName = "JADE";
    boolean isPlatform = false;
    boolean hasGUI = false;

    Vector agents = new Vector();

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
	else if(args[n].equals("-gui")) {
	  hasGUI = true;
	}
	else if(args[n].equals("-version") || args[n].equals("-v")) {
	  String CVSname = "$Name$";
	  String CVSdate = "$Date$";
	  int colonPos = CVSname.indexOf(":");
	  int dollarPos = CVSname.lastIndexOf('$');
	  String name = CVSname.substring(colonPos + 1, dollarPos);
	  if(name.indexOf("JADE") == -1)
	    name = "JADE snapshot";
	  else {
	    name = name.replace('-', ' ');
	    name = name.replace('_', '.');
	    name = name.trim();
	  }

	  colonPos = CVSdate.indexOf(':');
	  dollarPos = CVSdate.lastIndexOf('$');
	  String date = CVSdate.substring(colonPos + 1, dollarPos);
	  date = date.trim();
	  System.out.println(name + " - " + date);
	  System.exit(0);
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

    // If '-gui' option is given, add 'RMA:jade.domain.rma' to
    // startup agents, making sure that the RMA starts before all
    // other agents.
    if(hasGUI) {
      agents.insertElementAt(new String("RMA"), 0);
      agents.insertElementAt(new String("jade.tools.rma.rma"), 1);
    }

    // Build the complete URL of the agent platform from default
    // values and command line options, for use with RMI calls.
    String platformRMI = "rmi://" + platformHost + ":" + platformPort + "/" + platformName;

    // Start a new JADE runtime system, passing along suitable
    // information axtracted from command line arguments.
    jade.core.Starter.startUp(isPlatform, platformRMI, agents, args);

  }

  private static void usage(){
    System.out.println("Usage: java jade.Boot [options] [agent specifiers]");
    System.out.println("");
    System.out.println("where options are:");
    System.out.println("  -host\t\tHost where RMI registry for the platform is located");
    System.out.println("  -port\t\tThe port where RMI registry for the platform resides");
    System.out.println("  -name\t\tThe name with which the platform is bound in RMI registry");
    System.out.println("  -file\t\tA file name containing tne agent specifiers");
    System.out.println("  -gui\tIf specified, a new Remote Management Agent is created.");
    System.out.println("  -platform\tIf specified, a new Agent Platform is created.");
    System.out.println("  \t\tOtherwise a new Agent Container is added to an existing platform");
    System.out.println("  -version\tIf specified, current JADE version number and build date is printed.");
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
    System.exit(0);
  }


}
