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
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

class TimerDispatcher implements Runnable {

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

  synchronized void add(Timer t) {
    timers.add(t);
    // If this is the first timer, wake up the dispatcher thread
    if(timers.first() == t)
      notify();
  }

  synchronized void remove(Timer t) {
    timers.remove(t);
  }

  private synchronized boolean emptySet() {
    return timers.isEmpty();
  }

  private synchronized Timer firstTimer() {
    return (Timer)timers.first();
  }

  public void run() {
    // Server loop, demultiplexing between timer dispatching and timer
    // addition/removal.

    try {
      // Used as a flag. The dispatcher must recheck the timer list
      // whenever the last timer was expired.
      boolean checkAgain = false;

      long timeToWait = 0;
      Timer t = null;
      synchronized(this) {
				while(active) {
	  			checkAgain = false;
	  			// If no timers are armed, wait until one is added.
	  			if(emptySet()) {
	    			timeToWait = 0;
	  			}
	  			// Otherwise...
	  			else {
	    			t = firstTimer();
	    			// If t was expired, calling this function executes the
	    			// time-out action; then the timer is removed and the need
	    			// for further inspections of the timer list is flagged.
	    			if(t.isExpired()) {
	      			remove(t);
	      			checkAgain = true;
	    			}
	    			else {
	      			// The first timer is still armed. Then the dispatcher
	      			// calculates the remaining time to wait.
	      			timeToWait = t.expirationTime() - System.currentTimeMillis();
	      			if(timeToWait <= 0) // Avoid wait(0), that means 'for ever'...
								timeToWait = 1;
	    			}
	  			}
	  			if(!checkAgain) {
	    			// System.out.println("Waiting for " + timeToWait + " ms.");
	    			wait(timeToWait);
	  			}
	  			
				}  // END of while
				
      }  // END of synchronized
      
    }
    catch(InterruptedException ie) {
      // Do nothing, but just return, since this is a shutdown.
    }
    // System.out.println("Timer Dispatcher shutting down ...");
  }

  public void start() {
    synchronized(myThread) {
      myThread.start();
    }
  }

  public void stop() {
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

}
