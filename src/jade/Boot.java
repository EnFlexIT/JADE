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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.lang.Boolean;
import java.util.StringTokenizer;
import java.util.Stack;
import java.io.*;
import javax.swing.JOptionPane;

import jade.gui.BootGUI;

/**
   Boots <B><em>JADE</em></b> system, parsing command line arguments.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public class Boot {

    /**
     * This static method parses the command line arguments and stores
     * all the information into the passed BootArgument object.
     **/
    private static void parseCommandLine(BootArguments ba, String args[]) {
	int n = 0;
	String fileName = null;
	boolean endCommand = false;
			
	while( n < args.length && !endCommand) {
	    if(args[n].equalsIgnoreCase("-conf")) {
      		if(++n == args.length) 
		    ba.startConfGUI = true;
		else {
		    String tmp = args[n];
		    if (tmp.startsWith("-") || (isAgentName(tmp))) { // needed otherwise not possible to used -conf followed by a list of agent specifiers (it would be read as a file name) 
			ba.startConfGUI = true;
		        n--;
		    } else 
			ba.confFileName = tmp;
		}
	    } else if(args[n].equalsIgnoreCase("-host")) {
      		if(++n  == args.length) 
		    ba.printUsageInfo = true;
		else
		    ba.hostName=args[n];
	    } else if(args[n].equalsIgnoreCase("-name")) {
      		if(++n  == args.length)
		    ba.printUsageInfo = true;
		 else 
		    ba.platformName=args[n];
	    } else if(args[n].equalsIgnoreCase("-port")) {
      		if(++n  == args.length) 
		    ba.printUsageInfo = true;
		else
		    try {
			ba.portNo = Integer.parseInt(args[n]);
		    } catch (NumberFormatException nfe) {
			ba.printUsageInfo = true;
		    }
	    }  else if(args[n].equalsIgnoreCase("-container")) {
		ba.isContainer = true;
	    } else if(args[n].equalsIgnoreCase("-gui")) {
		ba.startRMAGUI=true;
	    } else if(args[n].equalsIgnoreCase("-version") || args[n].equalsIgnoreCase("-v")) {
		ba.printVersionInfo=true;
	    } else if(args[n].equalsIgnoreCase("-help") || args[n].equalsIgnoreCase("-h")) {
		ba.printUsageInfo = true;
	    } else if(args[n].equalsIgnoreCase("-nomtp")) {
		ba.noMTP = true;
	    } else if(args[n].equalsIgnoreCase("-mtp")) { 
      		if(++n  == args.length) 
		    ba.printUsageInfo = true;
		else
		    try {
			ba.MTPs=BootArguments.parseArgumentList(args[n],true); 
		    } catch (BootException be) {
			be.printStackTrace();
			ba.printUsageInfo = true;
		    }
	    } else if(args[n].equalsIgnoreCase("-aclcodec")) {
      		if(++n  == args.length) 
		    ba.printUsageInfo = true;
		else
		    try {
			ba.aclCodecs=BootArguments.parseArgumentList(args[n],false); 
		    } catch (BootException be) {
			be.printStackTrace();
			ba.printUsageInfo = true;
		    }
	    } else if(isAgentName(args[n])) 
		endCommand = true; //no more options on the command line
	    n++;  // go to the next argument
	} // end of while

	// all options, but the list of Agents, have been parsed
	if(endCommand) { // parse the list of agents, now
	    --n; // go to the previous argument
	    //FIXME. CAN BE IMPROVED!
	    String[] ag = new String[(args.length-n)];
	    for(int i = n; i<args.length; i++)
		ag[i-n] = args[i];
	    //T1 -->Translate the string[] to a string
	    String agentString = BootArguments.T1(ag);
	    //Translate the string inserted into an ArrayList.
	    ArrayList agentArray = BootArguments.T2(agentString,false);
	    for (Iterator agentSpecifiers = BootArguments.getCommandLineAgentSpecifiers(agentArray); agentSpecifiers.hasNext(); ) {
				List i = ((List)agentSpecifiers.next());
				ba.agents.add(i);
	    }
	}
	
    }


  
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
    else 
    {
        name = name.replace('-', ' ');
	      name = name.replace('_', '.');
	      name = name.trim();
    }
    colonPos = CVSdate.indexOf(':');
    dollarPos = CVSdate.lastIndexOf('$');
    String date = CVSdate.substring(colonPos + 1, dollarPos);
    date = date.trim();
    return("    This is "+name + " - " + date+"\n    downloaded in Open Source, under LGPL restrictions,\n    at http://jade.cselt.it/\n");
   }

  /**
   * Returns a String with a warning in the case of user specified platform names
  */    
    private static String getNameWarning( boolean isPlatform ) {
	if( isPlatform ) {
	    return("\nWARNING: using user specified platform name. Please note that this option is stronlgy discouraged since uniqueness of the HAP is not enforced. This might result in non-unique agent names.\n");
	} else {
	    return("\nWARNING: ignoring user specified platform name. This should be specified as the argument when starting the main container.\n");
	}
    }


  /**
   * Fires up <b><em>JADE</em></b> system.
   * This method starts the bootstrap process for <B><em>JADE</em></b>
   * agent platform. It parses command line arguments and acts
   * accordingly.
   * Valid command line arguments:
   *
   * <ul>
   * <li>  <b>-host <host name></b>     <em>Host name where the platform is.</em>
   * <li>  <b>-port <port number></b>     <em>Port number where the RMI registry for
   *                            the platform is.</em>
   * <li>  <b>-gui</b>      <em>Starts the Remote Management Agent.</em>
   * <li>  <b>-container</b> <em>If specified, a new Agent Container is added to an existing platform</em>
   *                         <em>Otherwise a new Agent Platform is created.</em>
   * <li>  <b>-conf</b>     <em>Shows the gui to set the configuration properties to start JADE.</em>
   * <li>  <b>-conf <code>filename</code></b> <em>To start JADE using the configuration properties read in the specified file.</em>
   * <li>  <b>-version</b>  <em>Prints out version information and exits.</em>
   * <li>  <b>-help</b>     <em>Prints out usage informations.</em>
   * <li>  <b>-mtp</b>      <em>Specifies a list of external Message Transport Protocols to be activated on this container (by default the JDK1.2 IIOP is activated on the main-container and no MTP is activated on the other containers).</em>
   * <li>  <b>-nomtp</b>    <em>has precedence over -mtp and overrides it. It should be used to override the default behaviour of the main-container (by default the -nomtp option unselected).</em>
   * <li>  <b>-aclcodec</b> <em>To specify an acl encoding.By default the string encoding is used. </em>
   * <li>  <b>-name <platform name></b><em>The symbolic name of the platform. By default this is generated from the hostname and portnumber of the main container and its uniqueness is guaranteed.</em>
   * </ul>
   *
   * In any case the properties specified by command line replace the properties read by a file (if specified) or the default ones.
   */
  public static void main(String args[]) {

    System.out.println(getCopyrightNotice());

    // create an object that contains all the arguments to their default value
    BootArguments ba = new BootArguments();
    
    // parses the command line and stores the passed arguments in ba
    parseCommandLine(ba, args);

    if (ba.printVersionInfo) {
    	System.out.println(getCopyrightNotice());
			return;
    } 
    if (ba.printUsageInfo) {
    	usage();
			return;
    }
    if (ba.startConfGUI) {
			//in this case the gui for the configuration properties is shown.
			//the gui returns the properties after making the needed checking. 
			BootGUI guiForConf = new BootGUI();      
			ba = guiForConf.ShowBootGUI(ba); 
    } else if (ba.confFileName != null) {
    	try{
    		Properties p = loadPropertiesFromFile(ba.confFileName);
				ba.setProperties(p); //set the bootArguments variables and make a first check on these values.
    	}catch(FileNotFoundException fne){
    		System.out.println("FILE Not Found");
    		System.exit(0);
    	}catch(IOException ioe){
    		//FIXME
    		System.out.println("IOException");
    		ioe.printStackTrace();
    		System.exit(0);
    	}catch(BootException be){
    		be.printStackTrace();
    		System.exit(0);
    	}
    }
    
    try{
    	//System.out.println("Configuration values: " +ba.toString());
    	ba.check(); // to verify the correctness of the values inserted.
   		
    	ArrayList agents = new ArrayList();
    	//-gui option given --> start the RMA (it must be the first agent !!!)
    	if(ba.startRMAGUI)
    	{
    		List rma = new ArrayList();
    		rma.add(0,"RMA");
    		rma.add(1,"jade.tools.rma.rma");
    		ba.agents.add(0,rma);
    	}	
    	
    	boolean isPlatform = !(ba.isContainer);
    
    	// For the Main Container, if no '-nomtp' option is given, a
      // single IIOP MTP is installed, using the Sun JDK 1.2 ORB and
      // with a default address.
      
    	int MTPargs = 0;
    	if(ba.MTPs != null)
    		MTPargs = ba.MTPs.length;
    		
      if(isPlatform && (!ba.noMTP) && MTPargs == 0) {
				ba.MTPs = new String[] { "jade.mtp.iiop.MessageTransportProtocol", "" };
      }

      
    	// Build a unique ID for this platform, using host name, port and
      // object name for the main container, taken from default values
      // and command line options.
      String platformID;
 
      if( (ba.platformName==null) || ba.platformName.equals("") ) {
	  platformID= ba.hostName + ":" + ba.portNo + "/JADE";
      } else {
	  System.out.println(getNameWarning(isPlatform));
	  platformID=ba.platformName;
      }


    	// Configure Java runtime system to put the selected host address in RMI messages
      try {
			String localHost;
			if(isPlatform) {
	  		localHost = ba.hostName;
			}
			else {
	  		// FIXME: It should be possible to set the local host also
	  		// on a non-main container.
	  		localHost = InetAddress.getLocalHost().getHostAddress();
			}
			System.getProperties().put("java.rmi.server.hostname", localHost);
      }
      catch(java.net.UnknownHostException jnue) {
				jnue.printStackTrace();
      }

     	// Start a new JADE runtime system, passing along suitable
      // information extracted from command line arguments.
      
      jade.core.Starter.startUp(isPlatform, platformID, ba.hostName, ba.portNo, ba.agents.iterator(), (ba.MTPs == null ? new String[0]:ba.MTPs), (ba.aclCodecs == null ? new String[0]:ba.aclCodecs) );

    }catch(BootException be){
    	be.printStackTrace();
    	System.exit(0);
    }
   
  }

  // verify if a string can be an used to start an agent (name + class).  
  // The string must be of the type agentName:xxx.xxx a string of type agentName: is not considered valid.
  private static boolean isAgentName(String name) {
  	int separatorPos = name.indexOf(BootArguments.SEPARATOR);
   	return ((separatorPos > 0) && (separatorPos < (name.length() - 1)) 
   		&& (name.charAt(separatorPos+1) != '/') && (name.charAt(separatorPos+1) != '\\'));
   		// the last two conditions allow to use -conf c:\temp\p.conf
    }
  
  
  public static void usage(){
    System.out.println("Usage: java jade.Boot [options] [agent specifiers]");
    System.out.println("");
    System.out.println("where options are:");
    System.out.println("  -host <host name>\tHost where RMI registry for the platform is located");
    System.out.println("  -port <port number>\tThe port where RMI registry for the platform resides");
    System.out.println("  -gui\t\t\tIf specified, a new Remote Management Agent is created.");
    System.out.println("  -container\t\tIf specified, a new Agent Container is added to an existing platform");
    System.out.println("  \t\t\tOtherwise a new Agent Platform is created");
    System.out.println("  -conf\t\t\tShows the gui to set the configuration properties to start JADE.");
    System.out.println("  -conf <file name>\tStarts JADE using the configuration properties read in the specified file.");	
    System.out.println("  -version\t\tIf specified, current JADE version number and build date is printed.");
    System.out.println("  -mtp\t\t\tSpecifies a list, separated by ';', of external Message Transport Protocols to be activated.");
    System.out.println("  \t\t\tBy default the JDK1.2 IIOP is activated on the main-container and no MTP is activated on the other containers.");
    System.out.println("  -nomtp\t\tHas precedence over -mtp and overrides it.");
    System.out.println("  \t\t\tIt should be used to override the default behaviour of the main-container (by default the -nomtp option unselected).");
    System.out.println("  -aclcodec\t\tSpecifies a list, separated by ';', of ACLCodec to use. By default the string codec is used.");
    System.out.println("  -name <platform name>\tThe symbolic platform name specified only for the main container.");
    System.out.println("  -help\t\t\tPrints out usage informations.");
    System.out.println("");
    System.out.print("An agent specifier is composed of an agent name and an agent class, separated by \"");
    System.out.println(BootArguments.SEPARATOR + "\"");
    System.out.println("");
    System.out.println("Take care that the specified agent name represents only the local name of the agent."); 
    System.out.println("Its guid (globally unique identifier) is instead assigned by the AMS after concatenating");
    System.out.println("the home agent platform identifier (e.g. john@foo.cselt.it:1099/JADE)");
    System.out.println("");	
    System.out.println("Examples:");
    System.out.println("  Connect to default platform, starting an agent named 'peter'");
    System.out.println("  implemented in 'myAgent' class:");
    System.out.println("  \tjava jade.Boot -container peter:myAgent");
    System.out.println("");
    System.out.println("  Connect to a platform on host zork.zot.za, on port 1100,");
    System.out.println("  starting two agents");
    System.out.println("  java jade.Boot -container -host zork.zot.za -port 1100 peter:heAgent paula:sheAgent");
    System.out.println("");
    System.out.println("  Create an Agent Platform and starts an agent on the local Agent Container");
    System.out.println("  \tjava jade.Boot Willy:searchAgent");
    System.out.println("");
    
    System.exit(0);
  }

 
  static Properties loadPropertiesFromFile(String fileName) throws FileNotFoundException, IOException
  {
  	
  		Properties p = new Properties();
  		FileInputStream in = new FileInputStream(fileName);
  		p.load(in);
  		in.close();
   		return p;
  	
  }

}
