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
import java.util.StringTokenizer;
import java.util.Stack;
import java.io.*;

/**
 * Boots <B><em>JADE</em></b> system, parsing command line arguments.
 *
 * @author Giovanni Rimassa - Universita` di Parma
 * @version $Date$ $Revision$
 *
 */
public class Boot {
  private static final String DEFAULT_FILENAME = "leap.properties";

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
      Runtime.instance().startUp(p);
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
  
  private static Properties parseCmdLineArgs(String[] args) throws IllegalArgumentException {
  	Properties props = new Properties();
  	
  	int i = 0;
  	while (i < args.length) {
  		if (args[i].startsWith("-")) {
  			// Parse next option
	  		if (args[i].equalsIgnoreCase("-container")) {
	  			props.setProperty(Profile.MAIN, "false");
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
	  		else if (args[i].equalsIgnoreCase("-mtp")) {
	  			if (++i < args.length) {
	  				props.setProperty(Profile.MTPS, args[i]);
	  			}
	  			else {
	  				throw new IllegalArgumentException("No mtps specified after \"-mtp\" option");
	  			}
	  			if (props.getProperty("nomtp") != null) {
	  				System.out.println("WARNING: both \"-mtp\" and \"-nomtp\" options specified. The latter will be ignored");
	  			}
	  		}
	  		else if (args[i].equalsIgnoreCase("-nomtp")) {
	  			props.setProperty("nomtp", "true");
	  			if (props.getProperty(Profile.MTPS) != null) {
	  				System.out.println("WARNING: both \"-mtp\" and \"-nomtp\" options specified. The latter will be ignored");
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
  				System.out.println("WARNING: overriding agents specification set with the \"-agents\" option");
  			}
  			props.setProperty(Profile.AGENTS, args[i]);
  			if (++i < args.length) {
  				System.out.println("WARNING: ignoring command line argument "+args[i]+" occurring after agents specification");
  			}
  			break;
  		}
  	}
  	
  	return props;
  }
  
  private static void printUsage() {
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
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Agent;

public class Boot extends MIDlet {
  public static MIDlet midlet;

  public Boot() {
    midlet = this;
    Agent.midlet = this;
  }

  public void startApp() throws MIDletStateChangeException {

    // System.out.println(getCopyrightNotice());
    System.out.println("This is JADE - LEAP J2ME kernel v2.1");

    try {
      ProfileImpl p = new ProfileImpl();


      Display.getDisplay(this).setCurrent(new Form("JADE-LEAP 2.1"));


      // small trick - moving much memory consuming instruction to the point where it is not so important
      if (null instanceof jade.lang.acl.ACLMessage) {
        ;
      } 

      Runtime.instance().gc("Before start-up");

      // Start a new JADE runtime system
      Runtime.instance().startUp(p);
      Runtime.instance().gc("After start-up");
    } 
    catch (Exception e) {
      System.err.println("Error creating the Profile Manager ["+e.getMessage()+"]");
      e.printStackTrace();
      notifyDestroyed();
    } 
  } 

  public void pauseApp() {
  } 

  public void destroyApp(boolean unconditional) {
  } 
#MIDP_INCLUDE_END*/
}

