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

package jade.core.behaviours;

//#MIDP_EXCLUDE_FILE

import jade.core.Agent;

import java.util.Vector;
import java.util.Enumeration;

/**
   This class provides support for executing JADE Behaviours 
   in dedicated Java Threads. In order to do that it is sufficient 
   to add to an agent a normal JADE Behaviour "wrapped" into 
   a "threaded behaviour" as returned by the <code>wrap()</code> method
   of this class (see the example below).
   
	 <pr><hr><blockquote><pre>
	 ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	 Behaviour b = // create a JADE behaviour
	 addBehaviour(tbf.wrap(b));
	 </pre></blockquote><hr>
   
   This class also provides methods to control the termination of 
   the threads dedicated to the execution of wrapped behaviours
   
   <br>
   <b>NOT available in MIDP</b>
   <br>
   
   @author Giovanni Caire - TILAB
 */
public class ThreadedBehaviourFactory {
	private Vector threadedBehaviours = new Vector();

	/**
	   Wraps a normal JADE Behaviour into a "threaded behaviour" so that
	   <code>b</code> will be executed by a dedicated Therad.
	 */
	public Behaviour wrap(Behaviour b) {
		return new ThreadedBehaviour(b);
	}
	
	/**
	   @return The number of active threads dedicated to the execution of 
	   wrapped behaviours.
	 */
	public int size() {
		return threadedBehaviours.size();
	}
	
	/**
	   Interrupt all threads dedicated to the execution of 
	   wrapped behaviours.
	 */
	public void interrupt() {
		synchronized (threadedBehaviours) { 
			// Mutual exclusion with threaded behaviour addition/removal
			ThreadedBehaviour[] tt = new ThreadedBehaviour[threadedBehaviours.size()];
			for (int i = 0; i < tt.length; ++i) {
				tt[i] = (ThreadedBehaviour) threadedBehaviours.elementAt(i);
			}
			for (int i = 0; i < tt.length; ++i) {
				tt[i].interrupt();
			}
		}
	}

	/**
	   Blocks until all threads dedicated to the execution of threaded 
	   behaviours complete.
	   @param timeout The maximum timeout to wait for threaded behaviour
	   termination.
	   @return <code>true</code> if all threaded behaviour have actually
	   completed, <code>false</code> otherwise.
	 */
	public synchronized boolean waitUntilEmpty(long timeout) {
		long time = System.currentTimeMillis();
		long deadline = time + timeout;
  	try {
	    while(!threadedBehaviours.isEmpty()) {
	    	if (timeout > 0 && time >= deadline) {
	    		// Timeout expired
	    		break;
	    	}
	      wait(deadline - time);
	      time = System.currentTimeMillis();
		  }
  	}
  	catch (InterruptedException ie) {
  		// Interrupted while waiting for threaded behaviour termination
  	}
  	return threadedBehaviours.isEmpty();  	
	}

	/**
	   @return the Thread dedicated to the execution of the Behaviour <code>b</code>
	 */
	public Thread getThread(Behaviour b) {
		synchronized(threadedBehaviours) {
			Enumeration e = threadedBehaviours.elements();
			while (e.hasMoreElements()) {
				ThreadedBehaviour tb = (ThreadedBehaviour) e.nextElement();
				if (tb.getBehaviour().equals(b)) {
					return tb.getThread();
				}
			}
			return null;
		}
	}
	
	/**
	   Inner class ThreadedBehaviour
	 */
	private class ThreadedBehaviour extends Behaviour implements Runnable {
		private Thread myThread;
		private Behaviour myBehaviour;
		private boolean restarted = false;
		private boolean finished = false;
		private int exitValue;
	
		private ThreadedBehaviour(Behaviour b) {
			super(b.myAgent);
			myBehaviour = b;
		}
		
		public void onStart() {
			myBehaviour.setAgent(myAgent);
			myBehaviour.setParent(new DummyCompositeBehaviour(myAgent, this));
			myThread = new Thread(this);
			myThread.setName(myAgent.getLocalName()+"#"+myBehaviour.getBehaviourName());
			myThread.start();
		}
		
		public void action() {
			if (!finished) {
				block();
			}
		}
		
		public boolean done() {
			return finished;
		}
		
		public int onEnd() {
			// This check only makes sense if the ThreadedBehaviour is a child
			// of a SerialBehaviour. In this case in fact the ThreadedBehaviour 
			// terminates, but the parent remains blocked.
			if (!myBehaviour.isRunnable()) {
				block();
			}
			return exitValue;
		}
		
		public void setDataStore(DataStore ds) {
			myBehaviour.setDataStore(ds);
		}
		
		public DataStore getDataStore() {
			return myBehaviour.getDataStore();
		}
		
		// This is synchronized to avoid that, in case the wrapped behaviour
		// is a SerialBehaviour we end up with the current child still blocked
		// while the thread is restarted.
		public synchronized void restart() {
			myBehaviour.restart();
		}
		
		private synchronized void go() {
			restarted = true;
			notifyAll();
		}
		
		public void run() {
			try {
				threadedBehaviours.addElement(this);
				while (true) {
					restarted = false;
					myBehaviour.actionWrapper();
					
					synchronized (this) {
						// If the behaviour was restarted from outside during the action()
						// method, give it another chance
						if (restarted) {
							myBehaviour.setRunnable(true);
						}
						
						if (myBehaviour.done()) {
							break;
						}
						else {
							if (!myBehaviour.isRunnable()) {
								wait();
							}
						}
					}
					
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
				}
				exitValue = myBehaviour.onEnd();
			}
			catch (InterruptedException ie) {
				System.out.println("Threaded behaviour "+getBehaviourName()+" interrupted before termination");
			}
			catch (Agent.Interrupted ae) {
				System.out.println("Threaded behaviour "+getBehaviourName()+" interrupted before termination");
			}
			terminate();
		}
		
		private void interrupt() {
			myThread.interrupt();
		}

		private void terminate() {
			finished = true;
			super.restart();
			threadedBehaviours.removeElement(this);
			synchronized(ThreadedBehaviourFactory.this) {
				ThreadedBehaviourFactory.this.notifyAll();
			}
		}
			
		private final Thread getThread() {
			return myThread;
		}
		
		private final Behaviour getBehaviour() {
			return myBehaviour;
		}
	} // END of inner class ThreadedBehaviour
	
	/**
	   Inner class DummyCompositeBehaviour.
	   This class has the only purpose of propagating restart events
	   in the actual wrapped behaviour to the ThreadedBehaviour.
	 */
	private class DummyCompositeBehaviour extends CompositeBehaviour {
		private ThreadedBehaviour myChild;
		
		private DummyCompositeBehaviour(Agent a, ThreadedBehaviour b) {
			super(a);
			myChild = b;
		}
		
		public boolean isRunnable() {
			return false;
		}
		
  	protected void handle(RunnableChangedEvent rce) {
  		if (rce.isRunnable()) {
  			myChild.go();
  		}
  	}
  	
  	/**
  	   Redefine the root() method so that both the DummyCompositeBehaviour
  	   and the ThreadedBehaviour are invisible in the behaviours hierarchy
  	 */
  	public Behaviour root() {
  		Behaviour r = myChild.root();
  		if (r == myChild) {
  			return myChild.getBehaviour();
  		}
  		else {
  			return r;
  		}
  	}
  	
	  protected void scheduleFirst() {
	  }
	  
	  protected void scheduleNext(boolean currentDone, int currentResult) {
	  }
	  
	  protected boolean checkTermination(boolean currentDone, int currentResult) {
	  	return false;
	  }
	  
	  protected Behaviour getCurrent() {
	  	return null;
	  }
	  
	  public jade.util.leap.Collection getChildren() {
	  	return null;
	  }
	} // END of inner class DummyCompositeBehaviour
}