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
import java.util.Properties;
import java.lang.Boolean;
import java.util.StringTokenizer;
import java.util.Stack;
import java.io.*;
import javax.swing.JOptionPane;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.ProfileException;
import jade.core.Specifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *  Boots <B><em>JADE</em></b> system, parsing command line arguments.
 *  @author Giovanni Rimassa - Universita` di Parma
 * @version $Date$ $Revision$
 *
 */
public class Boot {

    /*
     * FIXME. Limitations:
     * the aclcodecs and MTPs cannot have more than one argument
     */
    String hostName;       // -host <hostName>
    int    portNo;         // -port <portNo>
    String platformName; // -name <platformName>
    boolean startRMAGUI;    // -gui
    boolean isContainer;    // -container
    boolean startConfGUI;   // -conf
    String  confFileName;   // -conf <confFileName>
    boolean printVersionInfo; // -version
    boolean printUsageInfo;  // -help or -h or wrong command line
    boolean noMTP;           // -nomtp
    List MTPs;      // -mtp <className>(<arg>)? (; <className>(<arg>)?)*
    List aclCodecs; // -aclcodec <aclCodecName>:<className>(<arg>)? (; <aclCodecName>:<className>(<arg>)?)*
    List agents; // (<agentName>:<className>(<arg>[ <arg> ]*)?)*  at the end of the command



    /**
     * This method parses the command line arguments and stores
     * all the information into the class variables of this object 
     **/
    private void parseCommandLine(String args[]) {
	int n = 0;
	boolean endCommand = false; // true when there are no more options on the command line
			
	while( n < args.length && !endCommand) {
	    if(args[n].equalsIgnoreCase("-conf")) {
      		if(++n == args.length) 
		    startConfGUI = true;
		else {
		    String tmp = args[n];
		    if (tmp.startsWith("-") || (isAgentSpecifier(tmp))) { // needed otherwise not possible to used -conf followed by a list of agent specifiers (it would be read as a file name) 
			startConfGUI = true;
		        n--;
		    } else 
			confFileName = tmp;
		} 
	    } else if(args[n].equalsIgnoreCase("-host")) {
      		if(++n  == args.length) {
		    System.err.println("Missing host name ");
		    printUsageInfo = true;
		} else
		    hostName=args[n];
	    } else if(args[n].equalsIgnoreCase("-name")) {
      		if(++n  == args.length) {
		    System.err.println("Missing platform name");
		    printUsageInfo = true;
		 } else 
		    platformName=args[n];
	    } else if(args[n].equalsIgnoreCase("-port")) {
      		if(++n  == args.length) {
		    System.err.println("Missing port number");
		    printUsageInfo = true; 
		} else
		    try {
			portNo = Integer.parseInt(args[n]);
		    } catch (NumberFormatException nfe) {
			System.err.println("Wrong int for the port number");
			printUsageInfo = true;
		    }
	    }  else if(args[n].equalsIgnoreCase("-container")) {
		isContainer = true;
	    } else if(args[n].equalsIgnoreCase("-gui")) {
		startRMAGUI=true;
	    } else if(args[n].equalsIgnoreCase("-version") || args[n].equalsIgnoreCase("-v")) {
		printVersionInfo=true;
	    } else if(args[n].equalsIgnoreCase("-help") || args[n].equalsIgnoreCase("-h")) {
		printUsageInfo = true;
	    } else if(args[n].equalsIgnoreCase("-nomtp")) {
		noMTP = true;
	    } else if(args[n].equalsIgnoreCase("-mtp")) { 
      		if(++n  == args.length) {
		    System.err.println("Missing mtp specifiers");
		    printUsageInfo = true;
		} else
		    try {
			parseSpecifiers(args[n], MTPs); 
		    } catch (BootException be) {
			be.printStackTrace();
			System.err.println("Wrong MTP Specifiers");
			printUsageInfo = true;
		    }
	    } else if(args[n].equalsIgnoreCase("-aclcodec")) {
      		if(++n  == args.length) {
		    System.err.println("Missing aclcodec specifiers");
		    printUsageInfo = true;
		} else
		    try {
			parseSpecifiers(args[n], aclCodecs); 
		    } catch (BootException be) {
			be.printStackTrace();
			System.err.println("Wrong aclcodecs specifiers");
			printUsageInfo = true;
		    }
	    } else if(isAgentSpecifier(args[n])) {
		endCommand = true; //no more options on the command line
	    }
	    n++;  // go to the next argument
	} // end of while

	// all options, but the list of Agents, have been parsed
	if(endCommand) { // parse the list of agents, now
	    --n; // go to the previous argument
	    String[] ag = new String[(args.length-n)];
	    for(int i = n; i<args.length; i++)
		ag[i-n] = args[i];
	    //T1 -->Translate the string[] to a string
	    String agentString = T1(ag);
	    //Translate the string inserted into a List of Strings
	    List agentArray = T2(agentString,false);
	    for (Iterator agentSpecifiers = getCommandLineAgentSpecifiers(agentArray); agentSpecifiers.hasNext(); ) 
		agents.add((Specifier)agentSpecifiers.next());
	}
    } 


    private static final String ARGUMENT_SEPARATOR = ";";

    /**
     * Parse a String reading for a set of 
     * <code>parameter(arg)</code>
     * each delimited by a <code>;</code> and no space in between.
     * <p>
     * For instance
     * <code>jade.mtp.iiop(50);http.mtp.http(8080)</code> is a valid
     * string, while  <code>jade.mtp.iiop(50 80);http.mtp.http(8080)</code> 
     * is not valid
     * For each object specifier, a new java object <code>Specifier</code>
     * is added to the passed <code>out</code> List parameter.
     **/
    private void parseSpecifiers(String str, List out) throws BootException {
	// Cursor on the given string: marks the parser position
	int cursor = 0;
	while(cursor < str.length()) {
	    int commaPos = str.indexOf(ARGUMENT_SEPARATOR, cursor);
	    if(commaPos == -1)
		commaPos = str.length();
      	
	    String arg = str.substring(cursor,commaPos);
      
	    int openBracketPos = arg.indexOf('(');
	    int closedBracketPos = arg.indexOf(')');
	    
	    Specifier s = new Specifier();
	    if((openBracketPos == -1)&&(closedBracketPos == -1)) {
		// No brackets: no argument
		s.setClassName(arg);
	    } else {
		// An open bracket, then something, then a closed bracket:
		// the class name is before the open bracket, and the
		// argument is between brackets.
		if((openBracketPos != -1)&&(closedBracketPos != -1)&&(openBracketPos < closedBracketPos)) {
		    s.setClassName(arg.substring(0, openBracketPos));
		    Object a[] = new Object[1];
		    a[0] = arg.substring(openBracketPos + 1, closedBracketPos);
		    s.setArgs(a);
		} else
		    throw new BootException("Ill-formed MTP specifier: mismatched parentheses.");
	    }
	    cursor = commaPos + 1;
	    out.add(s);
	} // while (cursor)
    }

  
    /** Private constructor to forbid instantiation.
     * Set all variables to their default values.
     **/
  private Boot() {
      try {
	  hostName= InetAddress.getLocalHost().getHostName();      
      } catch(UnknownHostException uhe) {
	  System.out.print("Unknown host exception in getLocalHost(): ");
	  System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
	  System.exit(1);
      }
      portNo = 1099; 
      startRMAGUI = false;
      isContainer = false;
      startConfGUI = false;
      confFileName = null;
      printVersionInfo = false;
      printUsageInfo = false;
      noMTP = false;
      MTPs = new ArrayList();
      aclCodecs = new ArrayList();
      agents = new ArrayList();
      platformName="";
  }




  /**
   * Returns a String with a warning in the case of user specified platform names
  */    
    private String getNameWarning( boolean isPlatform ) {
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
   try {
      // create a new object in order to avoid static methods
      Boot b = new Boot();  

      // print Copyright Notice
      //System.out.println(b.getCopyrightNotice());

      // parses the command line and stores the passed arguments into
      // the class variables
      b.parseCommandLine(args);

      if (b.printVersionInfo) {
	  System.out.println(Runtime.getCopyrightNotice());
	  return;
      } 
      if (b.printUsageInfo) {
	  b.usage();
	  return;
      }
      if (b.startConfGUI) 
	  //in this case the gui for the configuration properties is shown.
	  //the gui returns the properties after making the needed checking. 
	  (new BootGUI()).getConfValues(b);      
      //b.dump();

      if (b.confFileName != null) {
	  try {
	      Properties p = b.loadPropertiesFromFile(b.confFileName); 
	      b.setProperties(p);
	  } catch (FileNotFoundException fnfe) {
	      System.err.println("Configuration File "+b.confFileName+" not Found");
	      b.usage();
	      return;
	  } catch (BootException be) {
	      be.printStackTrace();
	      b.usage();
	  } catch (IOException ioe) {
	      System.err.println("Configuration File "+b.confFileName+" contains wrong parameters");
	      b.usage();
	  }
      }

      //b.dump();

      b.check(); // to verify the correctness of the values inserted.
      
      //-gui option given --> start the RMA (it must be the first agent !!!)
      if(b.startRMAGUI) {
	  Specifier rma = new Specifier();
	  rma.setName("RMA");
	  rma.setClassName("jade.tools.rma.rma");
	  //This method is not available in jade.util.leap 
	  b.agents.add(0,rma);
	  /* List copy = b.agents;
	  b.agents = new ArrayList();
	  b.agents.add(rma);
	  for (Iterator i=copy.iterator(); i.hasNext(); )
	  b.agents.add(i.next());*/
	  rma = null; // frees memory
      }	
    	
      // For the Main Container, if no '-nomtp' option is given, a
      // single IIOP MTP is installed, using the Sun JDK 1.2 ORB and
      // with a default address.
      if(!b.isContainer && (!b.noMTP) && b.MTPs.size() == 0) {
	  Specifier s = new Specifier();
	  s.setClassName("jade.mtp.iiop.MessageTransportProtocol"); 
	  b.MTPs.add(s);
      }
      
      
      // Build a unique ID for this platform, using host name, port and
      // object name for the main container, taken from default values
      // and command line options.
      String platformID;
      
      if( (b.platformName==null) || b.platformName.equals("") ) {
	  platformID= b.hostName + ":" + b.portNo + "/JADE";
      } else {
	  System.out.println(b.getNameWarning(!b.isContainer));
	  platformID=b.platformName;
      }

      //b.dump();

      // Configure Java runtime system to put the selected host address in RMI messages
      try {
	  String localHost;
	  if(!b.isContainer) {
	      localHost = b.hostName;
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
      // initialize the profile
      ProfileImpl p = new ProfileImpl(); 
      p.putProperty(p.MAIN, (new Boolean(!b.isContainer)).toString());
      p.putProperty(p.MAIN_PROTO, "rmi");
      p.putProperty(p.MAIN_HOST, b.hostName);
      p.putProperty(p.MAIN_PORT, Integer.toString(b.portNo));
      p.putProperty(p.PLATFORM_ID, platformID);
      //p.putProperty(p.GUI, (new Boolean(b.startRMAGUI)).toString());
      p.putSpecifierList(p.AGENTS, b.agents);
      p.putSpecifierList(p.MTPS, b.MTPs);
      p.putSpecifierList(p.ACLCODECS, b.aclCodecs);

      boolean isMain = !b.isContainer;
      b = null; // frees memory

      // Exit the JVM when there are no more containers around
      Runtime.instance().setCloseVM(true);

      // Check whether this is the Main Container or a peripheral container
      if (isMain)
	  Runtime.instance().createMainContainer(p);
      else 
	  Runtime.instance().createAgentContainer(p);
      p = null; // frees memory
      /*    } catch (ProfileException pe) {
	pe.printStackTrace();
	return;*/
    }  catch(BootException be){
    	be.printStackTrace();
	return;
    }
  }

    /** separator between the agent name and the agent class **/
    private static final String NAME2CLASS_SEPARATOR = ":";

    /* verify if a string can be an used to start an agent (name + class).  
     * The string must be of the type agentName:xxx.xxx 
     * A string of type agentName: is not considered valid.
     * @return true if the passed string is an agent specifier
     **/
  private boolean isAgentSpecifier(String name) {
      int separatorPos = name.indexOf(NAME2CLASS_SEPARATOR);
      return ((separatorPos > 0) && (separatorPos < (name.length() - 1)) 
	      && (name.charAt(separatorPos+1) != '/') && (name.charAt(separatorPos+1) != '\\'));
      // the last two conditions allow to use -conf c:\temp\p.conf
    }
  
  
  public void usage(){
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
    System.out.println(NAME2CLASS_SEPARATOR + "\"");
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

 
  Properties loadPropertiesFromFile(String fileName) throws FileNotFoundException, IOException
  {
  	
  		Properties p = new Properties();
  		FileInputStream in = new FileInputStream(fileName);
  		p.load(in);
  		in.close();
   		return p;
  	
  }


    /**
     * Just for debugging.
     **/
    private void dump() {
	System.out.println("-host "+hostName); 
	System.out.println("-port "+portNo);
	System.out.println("-name "+platformName); 
	System.out.println("-gui "+startRMAGUI);
	System.out.println("-container "+isContainer);
	System.out.println("-conf (GUI) "+startConfGUI); 
	System.out.println("-conf (file) "+confFileName); 
	System.out.println("-version "+printVersionInfo); 
	System.out.println("-usage "+printUsageInfo); 
	System.out.println("-noMTP "+noMTP); 
	System.out.println("-MTP ");
	for (Iterator i=MTPs.iterator(); i.hasNext(); )
	    System.out.println(i.next().toString());
	System.out.println("-ACLCodec ");
	for (Iterator i=aclCodecs.iterator(); i.hasNext(); )
	    System.out.println(i.next().toString());
	System.out.println("agents: ");
	for (Iterator i=agents.iterator(); i.hasNext(); )
	    System.out.println(i.next().toString());
    }





    /**
     * Replace all the quote " chars with escaped quote \" chars
     * @ return a String with the replaced chars
     **/
    private String escapequote(String s) {
	int ind = s.indexOf("\"");
	int offset=0;
	StringBuffer buf=new StringBuffer(s);
	while (ind >= 0) {
	    buf.insert(ind+offset,'\\');
	    offset++;
	    ind = s.indexOf("\"",ind+1);
	}
	return buf.toString();
    }

    /**
     * Transform an array of Strings into a concatenated String (separated
     * by blanks) where all the quote chars have been escaped.
     **/
    private String T1(String args[]){
	String tmp = new String();
	for (int i=0; i<args.length; i++) { 
	    String t1=escapequote(args[i]);
	    if (t1.indexOf(' ')>0)
		tmp = tmp + "\"" + t1 + "\" ";
	    else
		tmp = tmp + t1 + " ";
	}
	return tmp;
    }


    /** These constants are used by the mini-parser implemented by the method T2 **/
    private static final int BETWEENTOKENS = 0;
    private static final int WORDTOKEN = 1;
    private static final int STRINGTOKEN = 2;
    private static final int ESCAPE = 3;
    
    /** 
     * Reads the passed String and decompose it into an array of Strings.
     * Each String is a token of type agentName: or className( or argument
     * This method is declared public because it is currently used by
     * <code>jade.tools.rma.StartNewAgentAction</code>
     * @param keepquote when true no char is removed, 
     * apart from blanks between tokens.
     * when false, quote chars are removed and also escaped quotes become 
     * just quotes.
     **/
    public static ArrayList T2(String s1, boolean keepquote){
	ArrayList l = new ArrayList();
	int state=BETWEENTOKENS;
	Stack returnState = new Stack();
	StringBuffer token = new StringBuffer();
	int i;
	for (i=0; i<s1.length(); i++) {
	    char ch = s1.charAt(i);
	    switch (state) {
	    case BETWEENTOKENS:
		if (ch != ' ') {
		    if (ch == '"') {
			state = STRINGTOKEN;
			if (keepquote)
			    token.append(ch);
			returnState.push(new Integer(BETWEENTOKENS));
		    } else if (ch == '\\') {
			state = ESCAPE;
			returnState.push(new Integer(BETWEENTOKENS));
		    } else {
			token.append(ch);
			state = WORDTOKEN;
		    } 
		} else if (token.length() > 0) {
		    l.add(token.toString());
		    token = new StringBuffer();
		}
		break;
	    case WORDTOKEN:
		if (ch == ' ') {
		    state = BETWEENTOKENS;
		    l.add(token.toString());
		    token = new StringBuffer();
		} 
		else if (ch =='"') {
		    state = STRINGTOKEN;
		    if (keepquote)
			token.append(ch);	
		    returnState.push(new Integer(WORDTOKEN));
		} else if (ch == '\\') {
		    state = ESCAPE;
		    returnState.push(new Integer(WORDTOKEN));
		} else
		    token.append(ch);
		break;
	    case STRINGTOKEN:
		if (ch == '"') { 
		    if (keepquote)
			token.append(ch);
		    state = ((Integer)returnState.pop()).intValue();
		} else if (ch == '\\') {
		    state = ESCAPE;
		    returnState.push(new Integer(STRINGTOKEN));
		} else
		    token.append(ch);
		break;
	    case ESCAPE:
		if ((ch != '"') || (keepquote))
		    token.append('\\');
		token.append(ch);
		state = ((Integer)returnState.pop()).intValue();
		break;
	    }
	}
	if (token.length() > 0) 
	    l.add(token.toString());
	return l;
    }


    /**
     * parse an array of Strings and return an Iterator of 
     * <code>Specifier</code>
     * This method is declared public because it is currently used by
     * <code>jade.tools.rma.StartNewAgentAction</code>
     * @param args is an array of string of agent specifiers of the 
     * type "name:class(arguments)"
     * @return an Iterator over a List of <code>Specifier</code>
    **/
    public static Iterator getCommandLineAgentSpecifiers(List args) { 
	ArrayList all = new ArrayList();
	int i = 0;
	while (i<args.size()) { //1
	    String cur = (String)args.get(i);
	    // search for the agent name
	    int index1 = cur.indexOf(':');
	    if (index1 > 0 && (index1 <(cur.length()-1))) { //2
        	// in every cycle we generate a new object Specifier
		Specifier as = new Specifier();
		as.setName(cur.substring(0,index1));
		// set the agent class
		int index2 = cur.indexOf('(',index1);
		if (index2 < 0) 
		    // no arguments to this agent
		    as.setClassName(cur.substring(index1+1,cur.length()));
	        else { //3
		    as.setClassName(cur.substring(index1+1,index2));
		    // having removed agentName,':',agentClass, and '(', 
		    // what remains is the firstArgument of the Agent 
		    // search for all the arguments up to the closed parenthesis
		    List asArgs = new ArrayList();
		    String nextArg = cur.substring(index2+1); //from the '(' to the end
		    while (! getAgentArgument(nextArg,asArgs)) { //4
			i++;
			if (i >= args.size()) { //5
			    System.err.println("Missing closed bracket to delimit agent arguments. The system cannot be launched");
			    //FIXME printUsageInfo = true;
			} //5
			nextArg = (String)args.get(i);
		    } // 4 
		    Object agentArgs[] = new Object[asArgs.size()];
		    for (int i3=0; i3<asArgs.size(); i3++) 
			agentArgs[i3]=(String)asArgs.get(i3);
		    as.setArgs(agentArgs);
	        } // 3
	        all.add(as);
	    } //2 
	    i++;
	} //1
	return all.iterator();
    } // 0 

 


    /**
       @param arg is the argument passed on the command line
       @param as is the List where arguments must be added
       @return true if this was the last argument, i.e. it found a closed bracket
    **/
    private static boolean getAgentArgument(String arg, List as) {
	boolean isLastArg = false;
	String  realArg;
	// if the last char is a closed parenthesis, then this is the last argument
	if (arg.endsWith(")")) {
	    if(arg.endsWith("\\)")) {
     	  	isLastArg = false;
     	  	realArg = arg; 
	    } else {
     	  	isLastArg = true;
     	  	realArg = arg.substring(0,arg.length()-1); //remove last parenthesis
	    }
	} else {
	    isLastArg = false;
	    realArg = arg;
	}
	if (realArg.length() > 0) {
	    // replace the escaped closed parenthesis with a simple parenthesis
	    as.add(replace(realArg,"\\)", ")"));     	  
	}
	return isLastArg;
    }
   
    private static String replace(String arg, String oldStr, String newStr) {
	int index = arg.indexOf(oldStr);
	String tmp = arg;
	while (index >= 0) {
	    tmp = tmp.substring(0, index) + newStr + tmp.substring(index + oldStr.length());
	    index = tmp.indexOf(oldStr);
	}
	return tmp;
    }



  
    /**
     * this method put the values stored in a property into the class variables.
     * and check the correctness of the value inserted. 
     **/
    void setProperties(Properties p) throws BootException{
  	try{
	    portNo = Integer.parseInt(p.getProperty("port"));
  	}catch(NumberFormatException nef){
	    portNo = 1099; 
	    throw new BootException("WARNING: Port number must be a number > 0.");
  	}
	startRMAGUI = (Boolean.valueOf(p.getProperty("gui"))).booleanValue();
	isContainer = (Boolean.valueOf(p.getProperty("container"))).booleanValue();
	noMTP = (Boolean.valueOf(p.getProperty("nomtp"))).booleanValue();
	hostName = p.getProperty("host");
	platformName = p.getProperty("name");	  
	 
	MTPs.clear();
	parseSpecifiers(p.getProperty("mtp"), MTPs);  
	aclCodecs.clear();
	parseSpecifiers(p.getProperty("aclcodec"),aclCodecs);
	  
	//Translate the string inserted into an ArrayList.
	List agentArray = T2(p.getProperty("agents"),false);
	agents.clear(); 
	for (Iterator agentSpecifiers = getCommandLineAgentSpecifiers(agentArray); agentSpecifiers.hasNext(); ) 
	    agents.add((Specifier)agentSpecifiers.next());
	check();  
    }
  
    /** 
     * Puts each configuration property into a Properties object.
     * Called by BootGUI in order to save properties into a file
     **/
  Properties toProperties(){
      Properties p = new Properties();
      p.put("container", (new Boolean(isContainer)).toString());
      p.put("port",(new Integer(portNo)).toString());
      p.put("gui",(new Boolean(startRMAGUI)).toString());
      p.put( "nomtp",(new Boolean(noMTP)).toString());
      p.put( "host",hostName);
      p.put( "name",platformName);
      p.put("mtp",SpecifierList2String(MTPs)); 
      p.put("aclcodec",SpecifierList2String(aclCodecs)); 
      p.put("agents",toAgentString());
      return p;
  }




//Converts the ArrayList of the agents into a String of the type agentName:agentClass(para1 param2..)
private String toAgentString(){
    StringBuffer tmp = new StringBuffer();
    for(Iterator i=agents.iterator(); i.hasNext();) { 
	Specifier s = (Specifier)i.next();
	tmp.append(s.getName()+":"+s.getClassName()); 
	StringBuffer tmp2 = new StringBuffer();
	Object[] args = s.getArgs();
	if (args != null) {
	    for (int ii=0; ii<args.length; ii++) {
		String argument = escapequote((String)args[ii]); 
		int index = argument.indexOf(" ");
		if (index > 0)
		    argument= "\""+ argument +"\"";
		tmp2.append(argument + " ");
	    }
	    tmp.append("( " + tmp2  + ") ");
	} else
	    tmp.append(" ");
    }
    return tmp.toString();		
}

    /**
     * This method verifies the configuration properties and eventually correct them.
     * It checks if the port number is a number greater than 0 otherwise it throws a BootException,
     * and if the -nomtp has been set, then delete some other mtp wrongly set.
     * If the user wants to start a platform the host
     * must be the local host so if a different name is speficied it
     * will be corrected and an exception will be thrown.   
     * 
     */
    private void check() throws BootException {
	if(portNo <= 0) {
	    portNo = 1099; 
	    throw new BootException("WARNING: Port number must be a number > 0.");
	}
	// Remove the MTP list if '-nomtp' is specified
	if(noMTP && !(MTPs == null || MTPs.size() == 0)) {
	    MTPs.clear(); 
	    throw new BootException("WARNING: If the option noMTP is on, then no MTP can be inserted.");
	}
	if (!isContainer) { // then it is a platform
	    try {
		InetAddress myPlatformAddrs[] =
		    InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
		InetAddress hostAddrs[] = InetAddress.getAllByName(hostName);
	      
		// If the platform address equals the host address, then
		// the user is starting the main container (platform) on
		// the local host.  The trick here is to compare the InetAddress
		// objects, not the strings since the one string might be a
		// fully qualified Internet domain name for the host and the 
		// other might be a simple name.  
		// Example: myHost.hpl.hp.com and myHost might
		// acutally be the same host even though the hostname strings do
		// not match.  When the InetAddress objects are compared, the IP
		// addresses will be compared.
		int i = 0;
		boolean isLocal = false;
		while ( (!isLocal) && (i < myPlatformAddrs.length) ) {
		    int j = 0;
		    while ( (!isLocal) && (j < hostAddrs.length) ) {
			isLocal = myPlatformAddrs[i].equals(hostAddrs[j]);
			j++;
		    }
		    i++;
		}

		if( !isLocal ) {
		    hostName = myPlatformAddrs[0].getHostName();
		    throw new BootException("WARNING: Not possible to launch a platform"
					    + "\n"
					    + "on a different host."
					    + "\n"
					    + "A platform must be launched on local host."
					    );
		}
	    } catch(UnknownHostException uhe) {
		// uhe.printStackTrace();
		//hostName = myPlatformAddrs[0].getHostName();
		throw new BootException("WARNING: Not possible to launch a platform"
					+ "\n"
					+ "on a different host."
					+ "\n"
					+ "A platform must be launched on local host."
					);
	    }
	} //END if(!isContainer) -- i.e. (this is a platform)
    }
    

    /**
     * returns a list of PropertyType used by the BootGUI to initialize the GUI.
     **/
    List getPropertyTypeVector() {
	List propertyVector = new ArrayList();
	PropertyType HostProperty = new PropertyType("host",PropertyType.STRING_TYPE,hostName, "Host Name of the main-container", false);
	PropertyType GuiProperty = new PropertyType("gui",PropertyType.BOOLEAN_TYPE,new Boolean(startRMAGUI).toString(), "Select to launch the RMA Gui", false);
	PropertyType PortProperty = new PropertyType("port",PropertyType.STRING_TYPE,new Integer(portNo).toString(), "Port Number of the main-container", false);
	PropertyType NameProperty = new PropertyType("name",PropertyType.STRING_TYPE,platformName, "The symbolic plaform name", false);
	PropertyType ContainerProperty = new PropertyType("container", PropertyType.BOOLEAN_TYPE, new Boolean(isContainer).toString(), "Select to launch an agent-container",false);
	PropertyType MTPProperty = new PropertyType("mtp", PropertyType.STRING_TYPE, SpecifierList2String(MTPs), "List of MTPs to activate", false); 
	PropertyType NoMTPProperty = new PropertyType("nomtp", PropertyType.BOOLEAN_TYPE, new Boolean(noMTP).toString(), "Disable all external MTPs on this container", false);		
	PropertyType ACLCodecProperty = new PropertyType("aclcodec", PropertyType.STRING_TYPE, SpecifierList2String(aclCodecs),"List of ACLCodec to install",false); 
	PropertyType AgentProperty = new PropertyType("agents",PropertyType.STRING_TYPE,toAgentString(),"Agents to launch",false);
	//update the propertyVector with all the -option 
	propertyVector.add(HostProperty);
	propertyVector.add(PortProperty);
	propertyVector.add(NameProperty);
	propertyVector.add(GuiProperty);
	propertyVector.add(ContainerProperty);
	propertyVector.add(MTPProperty);
	propertyVector.add(NoMTPProperty);
	propertyVector.add(ACLCodecProperty);
	propertyVector.add(AgentProperty);
	return propertyVector;
    }

    

    private String SpecifierList2String(List l) {
      StringBuffer tmp = new StringBuffer();
      for (Iterator i=l.iterator(); i.hasNext(); ) {
	  tmp.append(i.next().toString());
	  if (i.hasNext())
	      tmp.append(";");
      }
      return tmp.toString();
    }

}
