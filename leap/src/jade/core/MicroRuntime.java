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
	
	/**
	   Start up the JADE runtime
	 */
	public static FrontEnd startJADE(Properties p, Runnable r) {
		terminator = r;
		return new FrontEndContainer(p);
	}
	
	/**
	   Activate the Java environment terminator when the JADE runtime
	   has stopped.
	 */
	static void stopJADE(boolean join) {
		/*if (join) {
			final Thread toBeJoined = Thread.currentThread();
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						toBeJoined.join();
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					terminator.run();
				}
			} );
		}
		else {*/
			Thread t = new Thread(terminator);
			t.start();
		//}
	}
}

