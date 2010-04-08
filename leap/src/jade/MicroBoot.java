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
import jade.util.leap.Properties;
import jade.util.Logger;
import java.io.IOException;

import jade.core.Agent;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
   This class handles JADE start-up and shut-down in split-container
   mode, according to the current type of environment (JVM) we are
   running in.
   <br>
   <b>Requires the LEAP add-on</b>
   <br>
   @author Giovanni Caire - TILAB
 */
public class MicroBoot extends MIDlet implements Runnable {

	private Logger logger;

	// Start-up the JADE runtime system
	public void startApp() throws MIDletStateChangeException {
		boolean quit = false;
		if (Agent.midlet != null) {
			// This can happen when the MIDlet is paused and then resumed
			quit = true;
		}

		Agent.midlet = this;

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
			Properties props = new Properties();
			props.load(source);

			if (props.getProperty(MicroRuntime.JVM_KEY) == null) {
				props.setProperty(MicroRuntime.JVM_KEY, MicroRuntime.MIDP);
			}
			Logger.initialize(props);

			customize(props);

			// small trick - moving much memory consuming instruction to the point where it is not so important
			if (null instanceof jade.lang.acl.ACLMessage) {
				;
			} 

			// Start the JADE runtime system
			java.lang.Runtime rt = java.lang.Runtime.getRuntime();
			MicroRuntime.startJADE(props, this);
			rt.gc();

		} 
		catch (Exception e) {
			if(logger.isLoggable(Logger.SEVERE))
				logger.log(Logger.SEVERE,"Error reading configuration properties");
			e.printStackTrace();
			Agent.midlet = null;
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
		// When the MIDlet is killed, kill JADE too
		MicroRuntime.stopJADE();
	} 

	public void run() {
		// When JADE terminates, kill the MIDlet too (if still there)
		if (Agent.midlet != null) {
			if(logger.isLoggable(Logger.INFO))
				logger.log(Logger.INFO,"Destroying MIDlet now");
			Agent.midlet.notifyDestroyed();
		}
		Agent.midlet = null;
	}

	protected void customize(Properties props) {
		Display.getDisplay(this).setCurrent(new Form("JADE version 4.0"));
	}
}



