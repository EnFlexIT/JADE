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

 
  // This separates agent name from agent class on the command line
	private static final String SEPARATOR = ":";
	//This separates the single arguments in a list of arguments
	private static final String ARGUMENT_SEPARATOR = ";";
  //Default port used to start the platform
  private static String platformPort = "1099";
  
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
   * </ul>
   *
   * In any case the properties specified by command line replace the properties read by a file (if specified) or the default ones.
   */
  public static void main(String args[]) {

    System.out.println(getCopyrightNotice());
  
    //This two properties will not be shown in the gui for configuration so they will not be added to the list of properties to show.
    PropertyType withConfProp = new PropertyType("conf",PropertyType.BOOLEAN_TYPE,"false","Not to show in the gui", false);
    PropertyType fromFileProp = new PropertyType("file", PropertyType.BOOLEAN_TYPE,"false","Not to show in the gui", false); 
   
    // This list stores all the properties used to start JADE.When new
    // properties need to be added then they must be added to this
    // vector (see the method getCommandLineOptions()).
    // The PropertyType provide a constructor to initialize a property with 
    // its name,type, default value, meaning. 
    // The last value states if the property is mandatory or not.
    List propertyVector = new ArrayList();
   
    String platformName = "JADE";
    
    boolean isPlatform = false;
    boolean hasGUI = false;
    boolean isContainer = false;
    
    Properties p = new Properties();
    
    List agents = new ArrayList();
    // read all command-line options and returns the index of the first agent specifier
    // FIXME it is supposed that all agent-specifiers are at the end, so other -option after an agent-specifer will not be recognized correctly...
    int index = getCommandLineOptions(args,withConfProp,fromFileProp, propertyVector);
    
    String tmp = withConfProp.getCommandLineValue();
    // set to true if to start jade with default properties. 
    boolean withConf = (tmp == null ? new Boolean(withConfProp.getDefaultValue()).booleanValue() : new  Boolean(tmp).booleanValue());
    tmp = fromFileProp.getCommandLineValue();
    // set to true if to start jade using values from a file.
    boolean fromFile = (tmp == null ? new Boolean(fromFileProp.getDefaultValue()).booleanValue() : new  Boolean(tmp).booleanValue());

	  //This property maintains all the agent-specifiers inserted or by commandline or by the gui
	  PropertyType AgentToStartProperty = new PropertyType("agents",PropertyType.STRING_TYPE, "","Agents to launch", false);
    
	  if(index >= 0)
     {
     	// by command line some agent-specifiers have been inserted.
     	// construct an array of string that must be translate before eventually shown in the gui of configuration.
	     String[] ag = new String[(args.length-index)];
	     int in = 0;
	     for(int i = index; i<args.length; i++)
	     	{
	     		ag[in] = args[i];
	     	  in++;
	     	}
	     //T1 -->Translate the string[] to a string
	     AgentToStartProperty.setCommandLineValue(T1(ag));
	    } 
	      
	  //add the agent property Type to the vector of the prop to show.   
	  propertyVector.add(AgentToStartProperty);
	 
    if (withConf)
    {
      //in this case the gui for the configuration properties is shown.
      //the gui returns the properties after making the needed checking. 
      BootGUI guiForConf = new BootGUI();      
      p = guiForConf.ShowBootGUI(propertyVector);
    
    }
    else
    if(fromFile)
    {
      System.out.println("WARNING: Any additional command line option has been ignored and overloaded by the configuration file");
    	p = new Properties();

      Iterator it = propertyVector.iterator();
      while(it.hasNext())
      {
      	PropertyType pt = (PropertyType)it.next();
      	String fileValue = pt.getFileValue();
      	p.put(pt.getName(),fileValue !=null ? fileValue : pt.getDefaultValue());
      }

    	try{
      	checkProperties(p);
      }catch(BootException be){
      	System.out.println(be.getMessage());
      }

    }
    else
    {	 
      // the command line are translated to properties
      p = new Properties();
      
      Iterator it = propertyVector.iterator();
      while(it.hasNext())
      {
      	PropertyType pt = (PropertyType)it.next();
      	String commandValue = pt.getCommandLineValue();
      	p.put(pt.getName(),commandValue != null ? commandValue : pt.getDefaultValue());
      }
       
      // check if the command line is correct.
      try{
       checkProperties(p);	  
      }catch(BootException be){
      	System.out.println(be.getMessage());
      }
      
    }
         
    isPlatform = !((Boolean.valueOf(p.getProperty("container"))).booleanValue());
    hasGUI = (Boolean.valueOf(p.getProperty("gui"))).booleanValue();
    
    String agentNames = p.getProperty("agents");
    String output = "Agent " + (isPlatform ? "platform on " : "container connecting to ") + "host " + p.getProperty("host")  + " on port "+p.getProperty("port")+(hasGUI ? " launching the RMA gui" : "")+"\n";
    output = output + (agentNames.length() > 0 ? " launching the following list of agents: " + agentNames : "");
    System.out.println(output);
     
    // If '-gui' option is given, add 'RMA:jade.domain.rma' to
    // startup agents, making sure that the RMA starts before all
    // other agents.
  
    if(hasGUI) {
    	List rma = new ArrayList();
      rma.add(0, "RMA");
      rma.add(1, "jade.tools.rma.rma");
      agents.add(rma);
    }
    
    String agentString = p.getProperty("agents");
    
    //Translate the string inserted into an ArrayList.
    ArrayList agentArray = T2(agentString,false);
        
    for (Iterator agentSpecifiers = getCommandLineAgentSpecifiers(agentArray); agentSpecifiers.hasNext(); ) 
    {
      List i = ((List)agentSpecifiers.next());
      agents.add(i);
    }

    // Translate the value of the 'mtp' property into an array of Strings
    String mtpList = p.getProperty("mtp");
    try {
      List l = parseArgumentList(mtpList,true);

      String[] containerMTPs = new String[l.size()];
      for(int i = 0; i < l.size(); i++) {
	String s = (String)l.get(i);
	containerMTPs[i] = s;
      }
    
      // For the Main Container, if no '-nomtp' option is given, a
      // single IIOP MTP is installed, using the Sun JDK 1.2 ORB and
      // with a default address.
      String noMTP = p.getProperty("nomtp");
      if(isPlatform && noMTP.equals("false") && (containerMTPs.length == 0)) {
	containerMTPs = new String[] { "jade.mtp.iiop.MessageTransportProtocol", "" };
      }

      //Translate the value of the "aclcodec" property into an array of Strings
      String aclCodecList = p.getProperty("aclcodec");
      List ls = parseArgumentList(aclCodecList,false);
      String[] ACLCodecs = new String[ls.size()];
      for(int i = 0;i<ls.size(); i++){
      	String s = (String)ls.get(i);
      	ACLCodecs[i] = s;
      }
      
      // Build a unique ID for this platform, using host name, port and
      // object name for the main container, taken from default values
      // and command line options.
      String platformID = p.getProperty("host") + ":" + p.getProperty("port") + "/" + platformName;

      // Configure Java runtime system to put the selected host address in RMI messages
      try {
	String localHost;
	if(isPlatform) {
	  localHost = p.getProperty("host");
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
      jade.core.Starter.startUp(isPlatform, platformID, agents.iterator(), containerMTPs, ACLCodecs);

    }
    catch(BootException be) {
      System.out.println(be.getMessage());
    }

  }

  // verify if a string can be an used to start an agent (name + class).  
  // The string must be of the type agentName:xxx.xxx a string of type agentName: is not considered valid.
  private static int isAgentName(String name)
  {
  	int separatorPos = name.indexOf(SEPARATOR);
 
  	if ((separatorPos > 0) && (separatorPos < (name.length() - 1)))
  	 return separatorPos;
  	else
  	return -1;
  }
  
  
  private static void usage(){
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
    System.out.println("  -mtp\t\t\tSpecifies a list of external Message Transport Protocols to be activated.");
    System.out.println("  \t\t\tBy default the JDK1.2 IIOP is activated on the main-container and no MTP is activated on the other containers.");
    System.out.println("  -nomtp\t\tHas precedence over -mtp and overrides it.");
    System.out.println("  \t\t\tIt should be used to override the default behaviour of the main-container (by default the -nomtp option unselected).");
    System.out.println("  -aclcodec\t\tSpecifies a list of ACLCodec to use. By default the string codec is used.");
    System.out.println("  -help\t\t\tPrints out usage informations.");
    System.out.println("");
    System.out.print("An agent specifier is composed of an agent name and an agent class, separated by \"");
    System.out.println(SEPARATOR + "\"");
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

  //Reads the properties from file then update the vector of properties.
  
  static void loadPropertiesFromFile(String fileName, List prop) throws FileNotFoundException, IOException
  {
  	Properties p = new Properties();
  	FileInputStream in = new FileInputStream(fileName);
  	p.load(in);
  	in.close();
  	
  	// update the properties in the vector of properties
  	Iterator it = prop.iterator();
  	
  	while(it.hasNext())
  	{
  		PropertyType pt = (PropertyType)it.next();
  		String name = pt.getName();
  		pt.setFileValue(p.getProperty(name));
  	}
  	
  }
  
  /**
  This method verifies the configuration properties and eventually correct them.
  If both the properties "platform" and "container" are set to true the a platform will 
  be launched an and exception will be thrown.
  If the user wants to start a platform the host must be the local host so 
  if a different name is speficied it will be corrected and an exception will be thrown.  
  */
  public static void checkProperties(Properties p) throws BootException
  {
  
    String container = p.getProperty("container");
    boolean isContainer = (Boolean.valueOf(container)).booleanValue();	

  	String port = p.getProperty("port");
  	int portNumber = -1;
  	try{
  		portNumber = Integer.parseInt(port);
  	}catch(Exception e){}
  	if(portNumber <= 0)
  		{
  			//set property to default port.
  			p.remove("port");
  			p.put("port", platformPort);
  			throw new BootException("WARNING: Port number must be a number > 0.");
  		}

	// Remove the MTP list if '-nomtp' is specified
	String noMTP = p.getProperty("nomtp");
	if(noMTP.equals("true"))
	  p.setProperty("mtp", "");
  }

  /**
  parses the command line to find the -option and returns the index of the first agent specifiers 
  or -1 if no agent-specifers are inserted.
  */
   private static int getCommandLineOptions(String args[], PropertyType withConf, PropertyType fromFile, List propertyVector)
   {
   	  int n = 0;
   	  String fileName = null;
   	  boolean endCommand = false;
   	  String platformHost = null;
   	  	  
   	  try {
      platformHost= InetAddress.getLocalHost().getHostName();      
      }
      catch(UnknownHostException uhe) {
        System.out.print("Unknown host exception in getLocalHost(): ");
        System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
        System.exit(1);
      }

      PropertyType HostProperty = new PropertyType("host",PropertyType.STRING_TYPE,platformHost, "Host Name of the main-container", false);
      PropertyType GuiProperty = new PropertyType("gui",PropertyType.BOOLEAN_TYPE,"false", "Select to launch the RMA Gui", false);
      PropertyType PortProperty = new PropertyType("port",PropertyType.STRING_TYPE,platformPort, "Port Number of the main-container", false);
      PropertyType ContainerProperty = new PropertyType("container", PropertyType.BOOLEAN_TYPE, "false", "Select to launch an agent-container",false);
      PropertyType MTPProperty = new PropertyType("mtp", PropertyType.STRING_TYPE, "", "List of MTPs to activate", false);
      PropertyType NoMTPProperty = new PropertyType("nomtp", PropertyType.BOOLEAN_TYPE, "false", "Disable all external MTPs on this container", false);
      PropertyType ACLCodecProperty = new PropertyType("aclcodec", PropertyType.STRING_TYPE,"","List of ACLCodec to install",false);
      
      //update the propertyVector with all the -option 
      propertyVector.add(HostProperty);
      propertyVector.add(PortProperty);
      propertyVector.add(GuiProperty);
      propertyVector.add(ContainerProperty);
      propertyVector.add(MTPProperty);
      propertyVector.add(NoMTPProperty);
			propertyVector.add(ACLCodecProperty);
			
      while( n < args.length && !endCommand)
      {
      	
      	if(args[n].equals("-conf"))
      	{
      	
      		if(++n == args.length)
      			{	
      				withConf.setCommandLineValue("true");
      				continue;
      			}
      			
      		String tmp = args[n];
      		if (tmp.startsWith("-") || (isAgentName(tmp) > -1)) // needed otherwise not possible to used -conf followed by a list of agent specifiers (it would be read as a file name)
      		{
      			  // In this case the gui for configuration parameters will be initialized with default or command line values.
      				withConf.setCommandLineValue("true");
      				continue;
      			}
      		else
      		{
      			//Reading properties from file
      			fileName = tmp;
      			try{
      				loadPropertiesFromFile(fileName, propertyVector);
      				fromFile.setCommandLineValue("true");
      			}catch (FileNotFoundException fe)
      			{
      				Object[] options = {"Yes", "No"};
      				int val = JOptionPane.showOptionDialog(null,"WARNING: file "+fileName +" not found.\n" + "Using command line or default properties.", "WARNING MESSAGE", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, null, options,options[0]);
      				
      				fileName = null;
      				//FILE Not found --> EXIT
      				if(val == JOptionPane.NO_OPTION)
      					System.exit(0);
      					
      			}catch(IOException ioe)
      			{
      				JOptionPane.showMessageDialog(null,"WARNING: IO Exception\n" + "Using default configuration values.", "WARNING MESSAGE",JOptionPane.WARNING_MESSAGE);
      			}
      			
      		}
      			
      	}
      	else
      	if(args[n].equals("-host")) {
      		if(++n  == args.length) usage();
      		  HostProperty.setCommandLineValue(args[n]);
      		
      	}
      	else if(args[n].equals("-port")) {
      		if(++n  == args.length) usage();
      		PortProperty.setCommandLineValue(args[n]);
      	}
      
	else if(args[n].equals("-container")){
	        ContainerProperty.setCommandLineValue("true");
	}
	else if(args[n].equals("-gui")) {
	      	GuiProperty.setCommandLineValue("true");
	}
	else if(args[n].equals("-version") || args[n].equals("-v")) {
	    System.out.println(getCopyrightNotice());
	    System.exit(0);
	}
	else if(args[n].equals("-help") || args[n].equals("-h")) {
	      	usage();
	}
	else if(args[n].equals("-nomtp")) {
	    NoMTPProperty.setCommandLineValue("true");
	}
	else if(args[n].equals("-mtp")) {
	    if(++n  == args.length) usage();
	    MTPProperty.setCommandLineValue(args[n]);
	}
	else if(args[n].equals("-aclcodec")){
		if(++n == args.length) usage();
		ACLCodecProperty.setCommandLineValue(args[n]);
	}
	else
	  if(isAgentName(args[n])>-1)
	    endCommand = true; //no more command line
	          
	n++;
      }

      
      if(endCommand)
      	return --n;
      else
        return -1; //no agent specifiers.	
   }
  
   	/**
		@param args is a string of agent specifiers of the type "name:class(arguments)"
		**/

	public static Iterator getCommandLineAgentSpecifiers(ArrayList args) { //0
			ArrayList all = new ArrayList();
		  int i = 0;
			while (i<args.size()) { //1
				String cur = (String)args.get(i);
			  
				// search for the agent name
				int index1 = cur.indexOf(':');
        if (index1 > 0 && (index1 <(cur.length()-1))) { //2
        	// in every cycle we generate a new object CommandLineAgentSpecifier
				  List as = new ArrayList();
      	  //agent name exists, colon exists, and is followed by the class name
      		as.add(cur.substring(0,index1));
      	
      	  // set the agent class
          int index2 = cur.indexOf('(',index1);
          if (index2 < 0) 
        	  // no arguments to this agent
        	  as.add(cur.substring(index1+1,cur.length()));
	        else { //3
	      	  as.add(cur.substring(index1+1,index2));

	      	  // having removed agentName,':',agentClass, and '(', 
	      	  // what remains is the firstArgument of the Agent 
	      	  // search for all the arguments up to the closed parenthesis
	      	  String nextArg = cur.substring(index2+1); //from the '(' to the end
        		while (! getAgentArgument(nextArg,as)) { //4
        		  i++;
        			if (i >= args.size()) { //5
        				System.err.println("Missing closed bracket to delimit agent arguments. The system cannot be launched");
        				usage();
        				System.exit(0);		
        			} //5
        			nextArg = (String)args.get(i);
        		} // 4 
	        } // 3
	        all.add(as);
        } //2 
        i++;
			} //1
      return all.iterator();
	} // 0 

  
	//parser a string og arguments of the type property1(arg1);property2(arg2);property3
	//if  the property can have arguments set arguments to true, false otherwise
  private static List parseArgumentList(String argList, boolean arguments) throws BootException {

    // Cursor on the given string: marks the parser position
    int cursor = 0;
    List out = new ArrayList();
    while(cursor < argList.length()) {
      int commaPos = argList.indexOf(ARGUMENT_SEPARATOR, cursor);
      if(commaPos == -1)
				commaPos = argList.length();
      int openBracketPos = argList.indexOf('(', cursor);
      int closedBracketPos = argList.indexOf(')', cursor);

      // No brackets: use default address.
      if((openBracketPos == -1)&&(closedBracketPos == -1)) {
      	String className = argList.substring(cursor, commaPos);
				out.add(className); // Put MTP class name into the list
				if (arguments)
					out.add("");  // with an empty URL as the address
      }
      else {
				// An open bracket, then something, then a closed bracket:
        // the class name is before the open bracket, and the
        // address URL is between brackets.
				if((openBracketPos != -1)&&(closedBracketPos != -1)&&(openBracketPos < closedBracketPos)) {
	  				String className = argList.substring(cursor, openBracketPos);
	  				String arg = argList.substring(openBracketPos + 1, closedBracketPos);
	  				out.add(className);
	  				if (arguments)
	  					out.add(arg);
				}
				else
	  			throw new BootException("Ill-formed MTP specifier: mismatched parentheses.");
      }
      cursor = commaPos + 1;
    }
    return out;
  }

	/**
	@param arg is the argument passed on the command line
	@param as is the List where arguments must be added
	@return true if this was the last argument, i.e. it found a closed bracket
	**/
  private static boolean getAgentArgument(String arg, List as) {
  
  boolean isLastArg = false;
  String  realArg;
  // if the last char is a closed parenthesis, then this is the last argument
  if (arg.endsWith(")"))
     {
     	if(arg.endsWith("\\)"))
     	  {
     	  	isLastArg = false;
     	  	realArg = arg; 
     	  } 
     	else {
     	  	isLastArg = true;
     	  	realArg = arg.substring(0,arg.length()-1); //remove last parenthesis
     	  }
     } 
   else {
   	isLastArg = false;
   	realArg = arg;
   }
     		
  if (realArg.length() > 0) {
  	// replace the escaped closed parenthesis with a simple parenthesis
  	as.add(replace(realArg,"\\)", ")"));     	  
  }
  return isLastArg;
}


private static String replace(String arg, String oldStr, String newStr)
{
	int index = arg.indexOf(oldStr);
	String tmp = arg;
	while (index >= 0) {
    tmp = tmp.substring(0, index) + newStr + tmp.substring(index + oldStr.length());
    index = tmp.indexOf(oldStr);
	}
	return tmp;
}

 public static String escapequote(String s) {
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

public static String T1(String args[]){
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

public static final int BETWEENTOKENS = 0;
public static final int WORDTOKEN = 1;
public static final int STRINGTOKEN = 2;
public static final int ESCAPE = 3;

/**
@param keepquote when true no char is removed, apart from blanks between tokens.
when false, quote chars are removed and also escaped quotes become just quotes.
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


}
