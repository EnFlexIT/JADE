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

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;
import jade.util.leap.*;
import jade.util.Logger;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Agent;

/**
 * Boots the <B><em>JADE</em></b> system, parsing command line arguments.
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Giovanni Caire - TILAB
 * @author Nicolas Lhuillier - Motorola
 * @author Jerome Picault - Motorola
 * @version $Date: $ $Revision: -1 $
 *
 */
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
		} 
		catch (Exception e) {
			logger.log(Logger.SEVERE,"Error creating the Profile Manager", e);
			Agent.midlet = null;
			midlet = null;
			notifyDestroyed();
		} 
	} 

	public void pauseApp() {
	} 

	public void destroyApp(boolean unconditional) {
		// If the MIDlet is killed, kill JADE too
		Runtime.instance().shutDown();
	} 

	public void run() {
		// When JADE terminates, kill the MIDlet too (if still there)
		if (Agent.midlet != null) {
			logger.log(Logger.INFO, "Destroying MIDlet now");
			Agent.midlet.notifyDestroyed();
		}
		Agent.midlet = null;
		midlet = null;
	}

	protected void customize(Profile p) {
		Display.getDisplay(this).setCurrent(new Form("JADE-LEAP 4.0"));
	}
}

