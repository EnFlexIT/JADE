/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

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
   * Return a String with copyright Notice, Name and Version of this version of JADE
  */
  public static String getCopyrightNotice() {
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
    return("    This is "+name + " - " + date+"\n    downloaded in Open Source, under LGPL restrictions,\n    at http://sharon.cselt.it/projects/jade\n");
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

    System.out.println(getCopyrightNotice());
    // Default values for looking RMI registry bind/lookup

    String platformHost = null;
    
    // Host inserted from command line
    String insertedHost = null;
    
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
	  insertedHost = args[n]; 
	  
	}
	else if(args[n].equals("-port")) {
	  if(++n  == args.length) usage();
	  platformPort = args[n];
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
          System.out.println(getCopyrightNotice());
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

    // If -host is given with a platform different from local host
    // the platform exit.
    if (isPlatform) {
    	if ((insertedHost != null) && (!(platformHost.equalsIgnoreCase(insertedHost))))
           {
            System.out.println("    WARNING: Not possible to lunch a platform on a different host.");
            System.out.println("    The platform will be launched on local host.\n");
           }
    }
    else 
    	platformHost = insertedHost;
    
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
    System.out.println("  -file\t\tA file name containing tne agent specifiers");
    System.out.println("  -gui\t\tIf specified, a new Remote Management Agent is created.");
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
    System.out.println("  starting two agents");
    System.out.println("  java jade.Boot -host zork.zot.za -port 1100 peter:heAgent paula:sheAgent");
    System.out.println("");
    System.out.println("  Create an Agent Platform and starts an agent on the local Agent Container");
    System.out.println("  \tjava jade.Boot -platform Willy:searchAgent");
    System.out.println("");
    System.exit(0);
  }


}