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

import jade.core.MicroRuntime;
import jade.core.IMTPException;
import jade.util.leap.Properties;
import jade.util.Logger;
import java.io.IOException;

/*#MIDP_INCLUDE_BEGIN
import jade.core.Agent;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
#MIDP_INCLUDE_END*/

/**
   This class handles JADE start-up and shut-down according to 
   the current type of environment (JVM) we are running in. 
   @author Giovanni Caire - TILAB
 */
//#MIDP_EXCLUDE_BEGIN
public class MicroBoot {
	
  /**
   * Fires up the <b><em>JADE</em></b> runtime.
   */
  public static void main(String args[]) {
  	String propsFile = null;
    try {
    	Properties props = parseCmdLineArgs(args);
    	propsFile = props.getProperty("conf");
    	if (propsFile != null) {
    		props.load(propsFile);
    	}
	  	if (props.getProperty(MicroRuntime.JVM_KEY) == null) {
	  		//PJAVA_EXCLUDE_BEGIN
	  		props.setProperty(MicroRuntime.JVM_KEY, MicroRuntime.J2SE);
	  		//PJAVA_EXCLUDE_END
	  		/*PJAVA_INCLUDE_BEGIN
	  		props.setProperty(MicroRuntime.JVM_KEY, MicroRuntime.PJAVA);
	  		PJAVA_INCLUDE_END*/
	  	}
  	
	  	customize(props);
	  	
    	MicroRuntime.startJADE(props, new Runnable() {
		    public void run() {
		    	// Wait a bit before killing the JVM
		    	try {
		    		Thread.sleep(1000);
		    	}
		    	catch (InterruptedException ie) {
		    	}
		      Logger.println("Exiting now!");
		      System.exit(0);
		    } 
		  });
    }
    catch (IllegalArgumentException iae) {
      Logger.println("Error reading command line configuration properties. "+iae.getMessage());
      iae.printStackTrace();
      printUsage();
      System.exit(-1);
    }
    catch (IOException ioe) {
      Logger.println("Error reading configuration properties from file "+propsFile+". "+ioe.getMessage());
      ioe.printStackTrace();
      printUsage();
      System.exit(-1);
    }
  }
    
  private static Properties parseCmdLineArgs(String[] args) throws IllegalArgumentException {
  	Properties props = new Properties();
  	
  	int i = 0;
  	while (i < args.length) {
  		if (args[i].startsWith("-")) {
  			// Parse next option
				String name = args[i].substring(1);
  			if (++i < args.length) {
  				props.setProperty(name, args[i]);
  			}
  			else {
  				throw new IllegalArgumentException("No value specified for property \""+name+"\"");
  			}
  			++i;
  		}
  		else {
  			// Get agents at the end of command line
  			if (props.getProperty(MicroRuntime.AGENTS_KEY) != null) {
  				Logger.println("WARNING: overriding agents specification set with the \"-agents\" option");
  			}
  			String agents = args[i];
  			props.setProperty(MicroRuntime.AGENTS_KEY, args[i]);
  			if (++i < args.length) {
  				Logger.println("WARNING: ignoring command line argument "+args[i]+" occurring after agents specification");
					if (agents != null && agents.indexOf('(') != -1 && !agents.endsWith(")")) {
						Logger.println("Note that agent arguments specifications must not contain spaces");
					}
  				if (args[i].indexOf(':') != -1) {
						Logger.println("Note that agent specifications must be separated by a semicolon character \";\" without spaces");
					}
  			}
  			break;
  		}
  	}
	
  	return props;
  }
  
  private static void printUsage() {
  	Logger.println("Usage:");
  	Logger.println("java -cp <classpath> jade.MicroBoot [options] [agents]");
  	Logger.println("Options:");
  	Logger.println("    -conf <file-name>. Read configuration properties from the specified file name");
  	Logger.println("    -host <host-name>. The name/address of the host where the BackEnd has to be created");
  	Logger.println("    -port <port-number>. The port of the J2SE container active on \"host\"");
  	Logger.println("    -<key> <value>");
  	Logger.println("Agents: [-agents] <semicolon-separated agent-specifiers>");
  	Logger.println("     where agent-specifier = <agent-name>:<agent-class>[(comma separated args)]"); 
  	Logger.println();
  }
  
  protected static void customize(Properties props) {
  }
}
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
public class MicroBoot extends MIDlet implements Runnable {

  // Start-up the JADE runtime system
  public void startApp() throws MIDletStateChangeException {
  	if (Agent.midlet != null) {
  		// This can happen when the MIDlet is paused and then resumed
  		Logger.println("JADE runtime already active");
  		return;
  	}
  	    
    Agent.midlet = this;
    
    try {
    	String source = getAppProperty("MIDlet-LEAP-conf");
      if (source == null) {
      	// Use the JAD by default 
      	source = "jad";
			}
    	Properties props = new Properties();
    	props.load(source);

	  	if (props.getProperty(MicroRuntime.JVM_KEY) == null) {
	  		props.setProperty(MicroRuntime.JVM_KEY, MicroRuntime.MIDP);
	  	}
  	
  		customize(props);

      // small trick - moving much memory consuming instruction to the point where it is not so important
      if (null instanceof jade.lang.acl.ACLMessage) {
        ;
      } 

      // Start the JADE runtime system
    	java.lang.Runtime rt = java.lang.Runtime.getRuntime();
    	MicroRuntime.startJADE(props, this);
			rt.gc();
			
			//#NODEBUG_EXCLUDE_BEGIN
      //System.out.println("Used memory = "+((rt.totalMemory()-rt.freeMemory())/1024)+"K");
			//#NODEBUG_EXCLUDE_END
    } 
    catch (Exception e) {
      Logger.println("Error reading configuration properties");
      e.printStackTrace();
      Agent.midlet = null;
      notifyDestroyed();
    } 
  } 

  public void pauseApp() {
		Logger.println("pauseApp() called");
  } 

  public void destroyApp(boolean unconditional) {
		Logger.println("destroyApp() called");
		// When the MIDlet is killed, kill JADE too
  	MicroRuntime.stopJADE();
  } 
  
  public void run() {
  	// When JADE terminates, kill the MIDlet too (if still there)
  	if (Agent.midlet != null) {
  		Logger.println("Destroying MIDlet now");
    	Agent.midlet.notifyDestroyed();
    }
    Agent.midlet = null;
  }
  
  protected void customize(Properties props) {
    Display.getDisplay(this).setCurrent(new Form("JADE-LEAP 3.0"));
  }
}
#MIDP_INCLUDE_END*/



