package jade;

import java.util.*;
import java.net.*;

    /**
       This class collects all the command line arguments into a single
       object, just for convenience purposes.
       @author Fabio Bellifemine - TILab
     **/
public class BootArguments {
	
	//This separates the single arguments in a list of arguments
	private static final String ARGUMENT_SEPARATOR = ";";
	public static final String SEPARATOR = ":";

	private static int platformPort = 1099; //default value for the port 
    
    String hostName;
    int    portNo;
    String platformName="";
    boolean startRMAGUI;
    boolean isContainer;
    boolean startConfGUI;
    String  confFileName;
    boolean printVersionInfo;
    boolean printUsageInfo;
    boolean noMTP;
    String[] MTPs;  //FIXME.To improve
    String[] aclCodecs; //FIXME to improve
    ArrayList agents; //FIXME to improve the data structure
	
	/** constructor of the class. It sets all the arguments to
	 * the default value
	 **/
	BootArguments() {
	    try {
		hostName= InetAddress.getLocalHost().getHostName();      
	    } catch(UnknownHostException uhe) {
		System.out.print("Unknown host exception in getLocalHost(): ");
		System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
		System.exit(1);
	    }
	    portNo = platformPort;
	    startRMAGUI = false;
	    isContainer = false;
	    startConfGUI = false;
	    confFileName = null;
	    printVersionInfo = false;
	    printUsageInfo = false;
	    noMTP = false;
	    MTPs = null;
	    aclCodecs = null;
	    agents = new ArrayList();
	}   
	
	/**
	 * return a String representation for debugging purposes
	 **/
	public String toString() {
	    String tmp = new String();
	    tmp = tmp + " :host "+hostName;
	    tmp = tmp + " :portNumber "+portNo;
	    tmp = tmp + " :name "+platformName;
	    tmp = tmp + " :startRMAGUI "+startRMAGUI;
	    tmp = tmp + " :isContainer "+isContainer;
	    tmp = tmp + " :startConfGUI "+startConfGUI;
	    tmp = tmp + " :confFileName "+confFileName;
	    tmp = tmp + " :printVersionInfo "+printVersionInfo;
	    tmp = tmp + " :printUsageInfo "+printUsageInfo;
	    tmp = tmp + " :noMTP "+noMTP;
	    if (MTPs != null) {
		tmp = tmp + " :MTPs ";
		for (int i=0; i<MTPs.length; i++)
		    tmp = tmp + MTPs[i] + " ";
	    }
	    if (aclCodecs != null) {
		tmp = tmp + " :aclCodecs ";
		for (int i=0; i<aclCodecs.length; i++)
		    tmp = tmp + aclCodecs[i] + " ";
	    }
	  
	  Iterator agentSpecifiers = agents.iterator();
	    while(agentSpecifiers.hasNext()) {
		Iterator i = ((List)agentSpecifiers.next()).iterator();
		tmp = tmp + " :agentName "+(String)i.next();
		tmp = tmp + " :agentClass "+(String)i.next();
		tmp = tmp + " :agentArguments ";
		for ( ; i.hasNext(); )	         
		    tmp = tmp + (String)i.next() + " ";
	    }
	    return tmp;
	}

    /**
     * returns a list of PropertyType used by the BootGUI to initialize the GUI.
     **/
  public   List getPropertyTypeVector() {
  	
		List propertyVector = new ArrayList();
		PropertyType HostProperty = new PropertyType("host",PropertyType.STRING_TYPE,hostName, "Host Name of the main-container", false);
		PropertyType GuiProperty = new PropertyType("gui",PropertyType.BOOLEAN_TYPE,new Boolean(startRMAGUI).toString(), "Select to launch the RMA Gui", false);
		PropertyType PortProperty = new PropertyType("port",PropertyType.STRING_TYPE,new Integer(portNo).toString(), "Port Number of the main-container", false);
		PropertyType NameProperty = new PropertyType("name",PropertyType.STRING_TYPE,platformName, "The symbolic plaform name", false);
		PropertyType ContainerProperty = new PropertyType("container", PropertyType.BOOLEAN_TYPE, new Boolean(isContainer).toString(), "Select to launch an agent-container",false);
		PropertyType MTPProperty = new PropertyType("mtp", PropertyType.STRING_TYPE, toMTPsString(), "List of MTPs to activate", false); 
		PropertyType NoMTPProperty = new PropertyType("nomtp", PropertyType.BOOLEAN_TYPE, new Boolean(noMTP).toString(), "Disable all external MTPs on this container", false);		
		PropertyType ACLCodecProperty = new PropertyType("aclcodec", PropertyType.STRING_TYPE, toACLCodecsString(),"List of ACLCodec to install",false); 
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

    
    
  /**
  * This method verifies the configuration properties and eventually correct them.
  * It checks if the port number is a number greater than 0 otherwise it throws a BootException,
  * and if the -nomtp has been set, then delete some other mtp wrongly set.
  * If the user wants to start a platform the host
  * must be the local host so if a different name is speficied it
  * will be corrected and an exception will be thrown.   
  * 
  */
    public void check() throws BootException{
    	
    
  		if(portNo <= 0)
  		{		
	  		portNo = platformPort;	
  			throw new BootException("WARNING: Port number must be a number > 0.");
  		}
  		

			// Remove the MTP list if '-nomtp' is specified
			if(noMTP && !(MTPs == null || MTPs.length == 0))
	  	{
	  		MTPs = null;
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
    
    
    //parser a string of arguments of the type property1(arg1);property2(arg2);property3
		//if  the property can have arguments set arguments to true, false otherwise
    public static String[] parseArgumentList(String argList, boolean arguments) throws BootException {

    // Cursor on the given string: marks the parser position
    int cursor = 0;
    List out = new ArrayList();
    while(cursor < argList.length()) {
      int commaPos = argList.indexOf(ARGUMENT_SEPARATOR, cursor);
      if(commaPos == -1)
      	commaPos = argList.length();
      	
      String arg = argList.substring(cursor,commaPos);
      
      int openBracketPos = arg.indexOf('(');
      int closedBracketPos = arg.indexOf(')');

      // No brackets: use default address.
      if((openBracketPos == -1)&&(closedBracketPos == -1)) {
      	String className = arg;
				out.add(className); // Put MTP class name into the list
				if (arguments)
					out.add("");  // with an empty URL as the address
      }
      else {
				// An open bracket, then something, then a closed bracket:
        // the class name is before the open bracket, and the
        // address URL is between brackets.
				if((openBracketPos != -1)&&(closedBracketPos != -1)&&(openBracketPos < closedBracketPos)) {
	  				String className = arg.substring(0, openBracketPos);
	  				String par = arg.substring(openBracketPos + 1, closedBracketPos);
	  				out.add(className);
	  				if (arguments)
	  					out.add(par);
	  				//System.out.println("className: " + className + " par: " + par);	
				}
				else
	  			throw new BootException("Ill-formed MTP specifier: mismatched parentheses.");
      }
      cursor = commaPos + 1;
    }

    String[] tmp = new String[out.size()];
    for (int i=0; i<out.size(); i++) {
			tmp[i]=(String)out.get(i);
    }
    return tmp;
  }
  
 // this method put the values stored in a property into the bootArgument variables.
 // and check the correctness of the value inserted. 
  public void setProperties(Properties p) throws BootException{
		  	
  	try{
  		portNo = Integer.parseInt(p.getProperty("port"));
  	}catch(NumberFormatException nef){
  		portNo = platformPort;
  		throw new BootException("WARNING: Port number must be a number > 0.");
  	}
	  startRMAGUI = (Boolean.valueOf(p.getProperty("gui"))).booleanValue();
	  isContainer = (Boolean.valueOf(p.getProperty("container"))).booleanValue();
	  noMTP = (Boolean.valueOf(p.getProperty("nomtp"))).booleanValue();
	  hostName = p.getProperty("host");
	  platformName = p.getProperty("name");	  
	  
	  MTPs = parseArgumentList(p.getProperty("mtp"),true);
	 	aclCodecs = parseArgumentList(p.getProperty("aclcodec"),false);
	  
	  //Translate the string inserted into an ArrayList.
	  ArrayList agentArray = BootArguments.T2(p.getProperty("agents"),false);
	  agents = new ArrayList();
	  for (Iterator agentSpecifiers = BootArguments.getCommandLineAgentSpecifiers(agentArray); agentSpecifiers.hasNext(); ) {
	  	List i = ((List)agentSpecifiers.next());
			agents.add(i);
	    }

    check();  
  }
  
  // Puts each configuration property into a Properties object.
  
  public Properties toProperties(){
  
  		Properties p = new Properties();
    	p.put("container", (new Boolean(isContainer)).toString());
			p.put("port",(new Integer(portNo)).toString());
			p.put("gui",(new Boolean(startRMAGUI)).toString());
			p.put( "nomtp",(new Boolean(noMTP)).toString());
			p.put( "host",hostName);
			p.put( "name",platformName);
			p.put("mtp",toMTPsString());
			p.put("aclcodec",toACLCodecsString());
			p.put("agents",toAgentString());
			return p;
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
        				Boot.usage();
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

 private static String escapequote(String s) {
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


//Converts the ArrayList of the agents into a String of the type agentName:agentClass(para1 param2..)
private String toAgentString(){

	String tmp = new String();
			
	for(Iterator i=agents.iterator(); i.hasNext();){ 
		Iterator ag = ((List)i.next()).iterator();	
		String agentName = (String)ag.next();
		String agentClass = (String)ag.next();
				
		tmp = tmp + agentName + ":" + agentClass;

		List l = new ArrayList();
				
		String args = new String();
		
		for ( ; ag.hasNext(); )	         
			{
				String argument = escapequote((String)ag.next());
								
				int index = argument.indexOf(" ");
				if (index > 0)
					argument= "\""+ argument +"\"";
					
				args = args + argument + " ";
			}

		if(args.length() > 0)
			tmp = tmp + "( " + args  + ") ";
		else
			tmp = tmp + " ";
	}
	return tmp;		
}

//Converts the MTPs String[] into a string (with the format: className(args)
private String toMTPsString(){
	
	String tmp = new String();
	if (MTPs != null) {
	    int i=0;
	    while (i < MTPs.length) {
		tmp = tmp + MTPs[i];
		if (!MTPs[i++].equals("")) 
		    tmp = tmp +  "(" + MTPs[i]+ ")"; 
		i++;
		if (i<MTPs.length)
		    tmp = tmp + ARGUMENT_SEPARATOR;
	    }
	}
	
	return tmp;
}

//converts the string[] of the ACLCodecs into a String.
private String toACLCodecsString(){
	String tmp = new String();
	if (aclCodecs != null) {
		if(aclCodecs.length >  1){
			for (int i=0; i < aclCodecs.length-1; i++) 
				tmp = tmp + aclCodecs[i] + ARGUMENT_SEPARATOR;
			tmp = tmp + aclCodecs[aclCodecs.length-1];
		}else
		if(aclCodecs.length == 1)
			tmp = aclCodecs[0]; //only an element
			}
	return tmp;
}
}
 

