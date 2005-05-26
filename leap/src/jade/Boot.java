/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade;

//#MIDP_EXCLUDE_BEGIN
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.ProfileException;
import jade.core.Specifier;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.Properties;
import jade.util.Logger;
import java.util.StringTokenizer;
import java.util.Stack;
import java.util.Enumeration;
import java.io.*;

/**
 * Boots the <B><em>JADE</em></b> system, parsing command line arguments.
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Giovanni Caire - TILAB
 * @author Nicolas Lhuillier - Motorola
 * @author Jerome Picault - Motorola
 * @version $Date$ $Revision$
 *
 */
public class Boot {
  public static final String DEFAULT_FILENAME = "leap.properties";
  private static Logger logger = Logger.getMyLogger("jade.Boot");

  /**
   * Fires up the <b><em>JADE</em></b> system.
   * This method initializes the Profile Manager and then starts the
   * bootstrap process for the <B><em>JADE</em></b>
   * agent platform.
   */
  public static void main(String args[]) {
    try {
    	// Create the Profile 
    	ProfileImpl p = null;
    	if (args.length > 0) {
    		if (args[0].startsWith("-")) {
    			// Settings specified as command line arguments
    			p = new ProfileImpl(parseCmdLineArgs(args));
    		}
    		else {
    			// Settings specified in a property file
    			p = new ProfileImpl(args[0]);
    		}
    	} 
    	else {
    		// Settings specified in the default property file
    		p = new ProfileImpl(DEFAULT_FILENAME);
    	} 

      // Start a new JADE runtime system
      Runtime.instance().setCloseVM(true);
      //#PJAVA_EXCLUDE_BEGIN
      // Check whether this is the Main Container or a peripheral container
      if (p.getBooleanProperty(Profile.MAIN, true)) {
        Runtime.instance().createMainContainer(p);
      } else {
        Runtime.instance().createAgentContainer(p);
      }
      //#PJAVA_EXCLUDE_END
      /*#PJAVA_INCLUDE_BEGIN
        // Starts the container in SINGLE_MODE (Only one per JVM)
        Runtime.instance().startUp(p);
        #PJAVA_INCLUDE_END*/
    }
    catch (ProfileException pe) {
      System.err.println("Error creating the Profile ["+pe.getMessage()+"]");
      pe.printStackTrace();
      printUsage();
      //System.err.println("Usage: java jade.Boot <filename>");
      System.exit(-1);
    }
    catch (IllegalArgumentException iae) {
      System.err.println("Command line arguments format error. "+iae.getMessage());
      iae.printStackTrace();
      printUsage();
      //System.err.println("Usage: java jade.Boot <filename>");
      System.exit(-1);
    }
  }

  /**
     Default constructor.
  */
  public Boot() {
  }

  
  public static Properties parseCmdLineArgs(String[] args) throws IllegalArgumentException {
  	Properties props = new Properties();
  	
  	int i = 0;
  	while (i < args.length) {
  		if (args[i].startsWith("-")) {
  			// Parse next option
	  		if (args[i].equalsIgnoreCase("-container")) {
	  			props.setProperty(Profile.MAIN, "false");
	  		}
			else if(args[i].equalsIgnoreCase("-backupmain")) {
			    props.setProperty(Profile.LOCAL_SERVICE_MANAGER, "true");
			}
	  		else if (args[i].equalsIgnoreCase("-gui")) {
	  			props.setProperty("gui", "true");
	  		}
	  		else if (args[i].equalsIgnoreCase("-name")) {
	  			if (++i < args.length) {
	  				props.setProperty(Profile.PLATFORM_ID, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No platform name specified after \"-name\" option");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-host")) {
	  			if (++i < args.length) {
	  				props.setProperty(Profile.MAIN_HOST, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No host name specified after \"-host\" option");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-port")) {
	  			if (++i < args.length) {
	  				props.setProperty(Profile.MAIN_PORT, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No port number specified after \"-port\" option");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-conf")) {
	  			if (++i < args.length) {
	  				// Some parameters are specified in a properties file
	  				try {
		  				Properties pp = new Properties();
		  				pp.load(args[i]);
		  				Enumeration kk = pp.keys();
		  				while (kk.hasMoreElements()) {
		  					String key = (String) kk.nextElement();
		  					if (!props.containsKey(key)) {
		  						props.setProperty(key, pp.getProperty(key));
		  					}
		  				}
	  				}
	  				catch (Exception e) {
	  					if(logger.isLoggable(Logger.SEVERE))
	  						logger.log(Logger.SEVERE, "WARNING: error loading properties from file "+args[i]+". "+e);
	  				}	
	  			}
	  			else {
	  				throw new IllegalArgumentException("No configuration file name specified after \"-conf\" option");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-mtp")) {
	  			if (++i < args.length) {
	  				props.setProperty(Profile.MTPS, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No mtps specified after \"-mtp\" option");
	  			}
	  			if (props.getProperty("nomtp") != null) {
	  				if(logger.isLoggable(Logger.WARNING))
	  					logger.log(Logger.WARNING,"WARNING: both \"-mtp\" and \"-nomtp\" options specified. The latter will be ignored");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-nomtp")) {
	  			props.setProperty("nomtp", "true");
	  			if (props.getProperty(Profile.MTPS) != null) {
	  				if(logger.isLoggable(Logger.WARNING))
	  					logger.log(Logger.WARNING,"WARNING: both \"-mtp\" and \"-nomtp\" options specified. The latter will be ignored");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-agents")) {
	  			if (++i < args.length) {
	  				props.setProperty(Profile.AGENTS, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No agents specified after \"-agents\" option");
	  			}
	  		}
  			else {
  				String name = args[i].substring(1);
	  			if (++i < args.length) {
	  				props.setProperty(name, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No value specified for property \""+name+"\"");
	  			}
  			}
  			++i;
  		}
  		else {
  			// Get agents at the end of command line
  			if (props.getProperty(Profile.AGENTS) != null) {
  				if(logger.isLoggable(Logger.WARNING))
  					logger.log(Logger.WARNING,"WARNING: overriding agents specification set with the \"-agents\" option");
  			}
  			String agents = args[i];
  			props.setProperty(Profile.AGENTS, args[i]);
  			if (++i < args.length) {
  				if(logger.isLoggable(Logger.WARNING))
  					logger.log(Logger.WARNING,"WARNING: ignoring command line argument "+args[i]+" occurring after agents specification");
					if (agents != null && agents.indexOf('(') != -1 && !agents.endsWith(")")) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"Note that agent arguments specifications must not contain spaces");
					}
  				if (args[i].indexOf(':') != -1) {
						if(logger.isLoggable(Logger.WARNING))
							logger.log(Logger.WARNING,"Note that agent specifications must be separated by a semicolon character \";\" without spaces");
					}
  			}
  			break;
  		}
  	}
  	
  	return props;
  }
  
  public static void printUsage() {
  	System.out.println("Usage 1:");
  	System.out.println("java -cp <classpath> jade.Boot <property-file-name>");
  	System.out.println("\nUsage 2:");
  	System.out.println("java -cp <classpath> jade.Boot [options] [agents]");
  	System.out.println("Options:");
  	System.out.println("    -container");
  	System.out.println("    -gui");
  	System.out.println("    -name <platform-name>");
  	System.out.println("    -host <main-host>");
  	System.out.println("    -port <main-port>");
  	System.out.println("    -mtp <semicolon-separated mtp-specifiers>");
  	System.out.println("     where mtp-specifier = [in-address:]<mtp-class>[(comma-separated args)]");     
  	System.out.println("    -nomtp");
  	System.out.println("    -<property-name> <property-value>");
  	System.out.println("Agents: [-agents] <semicolon-separated agent-specifiers>");
  	System.out.println("     where agent-specifier = <agent-name>:<agent-class>[(comma separated args)]"); 
  	System.out.println();
  }
//#MIDP_EXCLUDE_END

/*#MIDP_INCLUDE_BEGIN
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;
import jade.util.leap.*;
import jade.util.Logger;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Agent;

public class Boot extends MIDlet implements Runnable {
  public static MIDlet midlet;
  private Logger logger ;

  public void startApp() throws MIDletStateChangeException {

    boolean quit = false;
    if (Agent.midlet != null) {
      // This can happen when the MIDlet is paused and then resumed
      quit = true;
    }

    Agent.midlet = this;
    midlet = this;
    
    logger = Logger.getMyLogger(this.getClass().getName());
    if (quit) {
      if(logger.isLoggable(Logger.SEVERE))
      	logger.log(Logger.SEVERE,"JADE runtime already active");
  		return; 
    }
    
    
    try {
		  // in MIDP2.0, names of user-def properties cannot start with MIDlet
			// for backward compatibility with JADE 3.3 we try both names
    	String source = getAppProperty("LEAP-conf");
      if (source == null) {
    	  source = getAppProperty("MIDlet-LEAP-conf");
				if (source == null) // Use the JAD by default 
      	   source = "jad";
			}
      ProfileImpl p = new ProfileImpl(source);

  		customize(p);

      // small trick - moving much memory consuming instruction to the point where it is not so important
      if (null instanceof jade.lang.acl.ACLMessage) {
        ;
      } 

      // Start the JADE runtime system
    	java.lang.Runtime rt = java.lang.Runtime.getRuntime();
			rt.gc();
      Runtime.instance().invokeOnTermination(this);
      Runtime.instance().startUp(p);
			rt.gc();
			
			//#NODEBUG_EXCLUDE_BEGIN
      //System.out.println("Used memory = "+((rt.totalMemory()-rt.freeMemory())/1024)+"K");
			//#NODEBUG_EXCLUDE_END
    } 
    catch (Exception e) {
      if(logger.isLoggable(Logger.SEVERE))
	      logger.log(Logger.SEVERE,"Error creating the Profile Manager ["+e.getMessage()+"]");
      e.printStackTrace();
      Agent.midlet = null;
      midlet = null;
      notifyDestroyed();
    } 
  } 

  public void pauseApp() {
		if(logger.isLoggable(Logger.INFO))
			logger.log(Logger.INFO,"pauseApp() called");
  } 

  public void destroyApp(boolean unconditional) {
		if(logger.isLoggable(Logger.INFO))
			logger.log(Logger.INFO,"destroyApp() called");
		// If the MIDlet is killed, kill JADE too
  	Runtime.instance().shutDown();
  } 
  
  public void run() {
  	// When JADE terminates, kill the MIDlet too (if still there)
  	if (Agent.midlet != null) {
  		if(logger.isLoggable(Logger.INFO))
  			logger.log(Logger.INFO, "Destroying MIDlet now");
	    Agent.midlet.notifyDestroyed();
		}
  	Agent.midlet = null;
    midlet = null;
  }
  
  protected void customize(Profile p) {
    Display.getDisplay(this).setCurrent(new Form("JADE-LEAP 3.1"));
  }
#MIDP_INCLUDE_END*/
}

