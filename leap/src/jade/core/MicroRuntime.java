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

package jade.core;

import jade.util.leap.Properties;
import jade.util.Logger;

/**
@author Giovanni Caire - TILAB
*/
public class MicroRuntime {
	public static final String AGENTS_KEY = "agents";
	public static final String JVM_KEY = "jvm";
	
	public static final String J2SE = "pjava";
	public static final String PJAVA = "pjava";
	public static final String MIDP = "midp";
	
	private static Runnable terminator;
	private static FrontEnd myFrontEnd;
	
	/**
	   Start up the JADE runtime
	 */
	public static void startJADE(Properties p, Runnable r) {
		if (myFrontEnd == null) {
			terminator = r;
			myFrontEnd = new FrontEndContainer(p);
		}
	}
	
	/**
	   Shut down the JADE runtime
	 */
	public static void stopJADE() {
		if (myFrontEnd != null) {
	  	try {
	  		// Self-initiated shutdown
		  	myFrontEnd.exit(true);
			}
			catch (IMTPException imtpe) {
				// Should never happen as this is a local call
				imtpe.printStackTrace();
			}
		}
	}
	
	/**
	   Start a new agent.
	 */
	public static void startAgent(String name, String className, String[] args) throws Exception {
		if (myFrontEnd != null) {
			try {
		  	myFrontEnd.createAgent(name, className, args);
			}
			catch (IMTPException imtpe) {
				// This is a local call --> an IMTPxception always wrap some other exception
				throw (Exception) imtpe.getNested();
			}
		}
	}
	
	/**
	   Kill an agent
	 */
	public static void killAgent(String name) throws NotFoundException {
		if (myFrontEnd != null) {
			try {
		  	myFrontEnd.killAgent(name);
			}
			catch (IMTPException imtpe) {
				// Should never happen as this is a local call
				imtpe.printStackTrace();
			}
		}
	}
	
	/**
	   Activate the Java environment terminator when the JADE runtime
	   has stopped.
	 */
	static void handleTermination(boolean self) {
		myFrontEnd = null;
		Thread t = null;
		if (self) {
			t = new Thread(terminator);
		}
		else {
			// If the termination was activated frome remote, then let
			// the current thread complete before closing down everything
			final Thread current = Thread.currentThread();
			t = new Thread(new Runnable() {
				public void run() {
					try {
						current.join();
					}
					catch (InterruptedException ie) {
						Logger.println("Interrupted in join");
					}
					terminator.run();
				}
			} );
		}
		t.start();
	}
}

