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

import jade.util.leap.SortedSet;
import jade.util.leap.SortedSetImpl;

/**
This class implements the JADE internal timing system. It should not
be used by application developers.
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

public class TimerDispatcher implements Runnable {
	// The singleton TimerDispatcher
	private static TimerDispatcher theDispatcher;
	
  private Thread myThread;
  private SortedSet timers = new SortedSetImpl();
  private boolean active;

  TimerDispatcher() {
    active = true;
  }

  void setThread(Thread t) {
    if(myThread == null) {
      myThread = t;
    }
  }

  public synchronized Timer add(Timer t) {
  	if (myThread == null) {
  		myThread = new Thread();
  		start();
  	}
  	while (!timers.add(t)) {
  		t.setExpirationTime(t.expirationTime()+1);
  	}
    // If this is the first timer, wake up the dispatcher thread
    if(timers.first() == t) {
      notifyAll();
    }
    return t;
  }

  public synchronized void remove(Timer t) {
    timers.remove(t);
  }

  private synchronized boolean emptySet() {
    return timers.isEmpty();
  }

  private synchronized Timer firstTimer() {
    return (Timer)timers.first();
  }
  
  public void run() {
    try {
    	while (active) {
	    	Timer t = null;
    		synchronized(this) {
    			while (active) {
	    			long timeToWait = 0;
	    			if (!timers.isEmpty()) {
	    				t = (Timer) timers.first();
	    				if (t.isExpired()) {
    						timers.remove(t);
	    					break;
	    				}
	    				else {
		      			timeToWait = t.expirationTime() - System.currentTimeMillis();
		      			if(timeToWait <= 0) {
		      				// Avoid wait(0), that means 'for ever'
									timeToWait = 1;
		      			}
	    				}
	    			}
	    			wait(timeToWait);
    			}
    		}
    		// This check just avoids NullPointerException on termination
    		if (active) {
	    		t.fire();
    		}
    	} 
    }
    catch(InterruptedException ie) {
      // Do nothing, but just return, since this is a shutdown.
    }
  }

  void start() {
    synchronized(myThread) {
      myThread.start();
    }
  }

  void stop() {
    synchronized(myThread) {
      if(Thread.currentThread().equals(myThread)) {
				System.out.println("Deadlock avoidance: TimerDispatcher thread calling stop on itself!");
      }
      else {
				active = false;
				synchronized (this) {
					notifyAll();
				}
				try {
	  			myThread.join();
				}
				catch (InterruptedException ignore) {
	  			// Do nothing
				}
      }
    }
  }

  static TimerDispatcher getTimerDispatcher() {
  	if (theDispatcher == null) {
  		theDispatcher = new TimerDispatcher();
  	}
  	return theDispatcher;
  }
  
  static void setTimerDispatcher(TimerDispatcher td) {
  	theDispatcher = td;
  }
}
