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
   * <li>  <b>-host <host name></b>     <em>Host name where the platform is.</em>
   * <li>  <b>-port <port number></b>     <em>Port number where the RMI registry for
   *                            the platform is.</em>
   * <li>  <b>-file <file name></b>     <em>File name to retrieve agent names from.</em>
   * <li>  <b>-gui</b>      <em>Starts the Remote Management Agent.</em>
   * <li>  <b>-container</b> <em>If specified, a new Agent Container is added to an existing platform</em>
   *                         <em>Otherwise a new Agent Platform is created.</em>
   * <li>  <b>-conf</b>     <em>Shows the gui to set the configuration properties to start JADE.</em>
   * <li>  <b>-conf <code>filename</code></b> <em>To start JADE using the configuration properties read in the specified file.</em>
   * <li>  <b>-version</b>  <em>Prints out version information and exits.</em>
   * <li>  <b>-help</b>     <em>Prints out usage informations.</em>
   *   
   * </ul>
   *
   * In any case the properties specified by command line replace the properties read by a file (if specified) or the default ones.
   */
  public static void main(String args[]) {

    System.out.println(getCopyrightNotice());
  
    boolean withConf = false;
    boolean fromFile = false;
    
    String platformHost = null;
    
    try {
      platformHost = InetAddress.getLocalHost().getHostName();      
    }
    catch(UnknownHostException uhe) {
      System.out.print("Unknown host exception in getLocalHost(): ");
      System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
      System.exit(1);
    }
    
    
    // This list stores all the properties used to start JADE.When new
    // properties need to be added then they must be added to this
    // vector.The PropertyType provide a constructor to initialize a
    // property with its name,type, default value, meaning. The last
    // value states if the property is mandatory or not.
    List propertyVector = new ArrayList();
    PropertyType HostProperty = new PropertyType("host",PropertyType.STRING_TYPE,platformHost, "Host Name", false);
    PropertyType GuiProperty = new PropertyType("gui",PropertyType.BOOLEAN_TYPE,"false", "to view the RMA Gui", false);
    PropertyType PortProperty = new PropertyType("port",PropertyType.STRING_TYPE,"1099", "port number", false);
    PropertyType ContainerProperty = new PropertyType("container", PropertyType.BOOLEAN_TYPE, "false", "to start a container",false);
    
    propertyVector.add(HostProperty);
    propertyVector.add(GuiProperty);
    propertyVector.add(PortProperty);
    propertyVector.add(ContainerProperty);
    
    String platformPort = null;
    String platformName = "JADE";
    
    boolean isPlatform = false;
    boolean hasGUI = false;
    boolean isContainer = false;
    
    Properties p = new Properties();
    
    String fileName = null;
    
    List agents = new ArrayList();
    List arguments = new ArrayList(); 


    try{

      int n = 0;
      while( n < args.length ){
      	
      	if(args[n].equals("-conf"))
      	{
      	
      		if(++n == args.length)
      			{	
      				withConf = true;
      				continue;
      			}
      			
      		String tmp = args[n];
      		if (tmp.startsWith("-"))
      		{
      			  // In this case the gui for configuration parameters will be initialized with default or command line values.
      				withConf = true;
      				continue;
      			}
      		else
      		{
      			//Reading properties from file
      			fileName = tmp;
      			try{
      				loadPropertiesFromFile(fileName, propertyVector);
      				fromFile = true;
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
      	else if(args[n].equals("-file")) {
      		if(++n  == args.length) 
      			usage();
	        // fileName = args[n];
	      ++n;
	  // FIXME: Add reading agent names from a file
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
	      else {
	  /* 
	     Every other string is supposed to be the name of an
	     agent, in the form name:class. The two parts must be at
	     least one character long to be put in the List;
	     otherwise they are ignored.
	  */
	  int separatorPos = args[n].indexOf(SEPARATOR);
	  if((separatorPos > 0)&&(separatorPos < args[n].length())) {
	    arguments.add(args[n]);
	    String agentName = args[n].substring(0,separatorPos);
	    String agentClass = args[n].substring(separatorPos + 1);
	    agents.add(new String(agentName));
	    agents.add(new String(agentClass));
	  }
	}
	n++;
      }
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

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
      
      // the command line are translate to properties
      p = new Properties();
      
      Iterator it = propertyVector.iterator();
      while(it.hasNext())
      {
      	PropertyType pt = (PropertyType)it.next();
      	String commandValue = pt.getCommandLineValue();
      	p.put(pt.getName(),commandValue !=null ? commandValue:pt.getDefaultValue());
      }
      
      
      // check if the command line are corrected.
      try{
      		checkProperties(p);
      	  
      }catch(BootException be){
      	System.out.println(be.getMessage());
      }
      
    }
    
    //since the args[] is passed to some method it is necessary to re-build this vector
    hasGUI = (Boolean.valueOf(p.getProperty("gui"))).booleanValue();
    arguments.add("-gui");
    platformHost = p.getProperty("host");
    arguments.add("-host");
    arguments.add(platformHost);
    platformPort = p.getProperty("port");
    arguments.add("-port");
    arguments.add(platformPort);
        
    isPlatform = !((Boolean.valueOf(p.getProperty("container"))).booleanValue());
    
    if(!isPlatform)
    	arguments.add("-container");
    
    if(fromFile)
    {
    	String output = "Agent " + (isPlatform ? "platform on " : "container connecting to ") + "host " + platformHost  + " on port "+platformPort+"\n";
    	System.out.println(output);
    }
    
    //re-build the vector or string according to the properties used
    int size = arguments.size();
    String result[] = new String[size];
    Iterator it = arguments.iterator();
    for(int i = 0; it.hasNext(); i++)
    {
      result[i] = (String)it.next();
    }
    
    args = result;
    
    // If '-gui' option is given, add 'RMA:jade.domain.rma' to
    // startup agents, making sure that the RMA starts before all
    // other agents.
    if(hasGUI) {
      agents.add(0, "RMA");
      agents.add(1, "jade.tools.rma.rma");
    }

    // Build A unique ID ofr this platform, using host name, port and
    // object name for the main container, taken from default values
    // and command line options.
    String platformID = platformHost + ":" + platformPort + "/" + platformName;

    // Start a new JADE runtime system, passing along suitable
    // information axtracted from command line arguments.
    jade.core.Starter.startUp(isPlatform, platformID, agents, args);

  }

  private static void usage(){
    System.out.println("Usage: java jade.Boot [options] [agent specifiers]");
    System.out.println("");
    System.out.println("where options are:");
    System.out.println("  -host <host name>\tHost where RMI registry for the platform is located");
    System.out.println("  -port <port number>\tThe port where RMI registry for the platform resides");
    System.out.println("  -file <file name>\tA file name containing tne agent specifiers");
    System.out.println("  -gui\t\t\tIf specified, a new Remote Management Agent is created.");
    System.out.println("  -container\t\tIf specified, a new Agent Container is added to an existing platform");
    System.out.println("  \t\t\tOtherwise a new Agent Platform is created");
    System.out.println("  -conf\t\t\tShows the gui to set the configuration properties to start JADE.");
    System.out.println("  -conf <file name>\tStarts JADE using the configuration properties read in the specified file.");	
    System.out.println("  -version\t\tIf specified, current JADE version number and build date is printed.");
    System.out.println("  -help \t\tPrints out usage informations.");
    System.out.println("");
    System.out.print("An agent specifier is made by an agent name and an agent class, separated by \"");
    System.out.println(SEPARATOR + "\"");
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
  
  	
  	if(!isContainer) //is a platform
  	   {
  	   	String host = p.getProperty("host");
  	   	try {
  	   		String platformHost = InetAddress.getLocalHost().getHostName();
          if(!(platformHost.equalsIgnoreCase(host)))
          {
          	p.remove("host");
          	p.put("host",platformHost);
          	throw new BootException("WARNING: Not possible to launch a platform \non a different host.\nA platform must be launched on local host.");
          }
  	   	}
        catch(UnknownHostException uhe) {
           System.out.print("Unknown host exception in getLocalHost(): ");
           System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
           System.exit(1);
        }

  	   } 	
  
  }
 

}
