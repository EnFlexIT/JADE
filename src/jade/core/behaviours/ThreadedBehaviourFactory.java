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
		return new ThreadedBehaviourWrapper(b);
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
			ThreadedBehaviourWrapper[] tt = new ThreadedBehaviourWrapper[threadedBehaviours.size()];
			for (int i = 0; i < tt.length; ++i) {
				tt[i] = (ThreadedBehaviourWrapper) threadedBehaviours.elementAt(i);
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
				ThreadedBehaviourWrapper tb = (ThreadedBehaviourWrapper) e.nextElement();
				if (tb.getBehaviour().equals(b)) {
					return tb.getThread();
				}
			}
			return null;
		}
	}
	
	/**
	 * @return All the wrapper behaviours currently used by theis ThreadedBehaviourFactory
	 */
	public Behaviour[] getWrappers() {
		synchronized (threadedBehaviours) {
			Behaviour[] wrappers = new Behaviour[threadedBehaviours.size()];
			for (int i = 0; i < wrappers.length; ++i) {
				wrappers[i] = (Behaviour) threadedBehaviours.elementAt(i);
			}
			return wrappers;
		}
	}
	
	/**
	   Inner class ThreadedBehaviourWrapper
	 */
	public class ThreadedBehaviourWrapper extends Behaviour implements Runnable {
		private Thread myThread;
		private Behaviour myBehaviour;
		private volatile boolean restarted = false;
		private boolean finished = false;
		private int exitValue;
	
		private ThreadedBehaviourWrapper(Behaviour b) {
			super(b.myAgent);
			myBehaviour = b;
			myBehaviour.setParent(new DummyParentBehaviour(myAgent, this));
		}
		
		public void onStart() {
			// Be sure both the wrapped behaviour and its dummy parent are linked to the 
			// correct agent
			myBehaviour.setAgent(myAgent);
			myBehaviour.parent.setAgent(myAgent);
			
			// Start the dedicated thread
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
			// This check only makes sense if the ThreadedBehaviourWrapper is a child
			// of a SerialBehaviour. In this case in fact the ThreadedBehaviourWrapper 
			// terminates, but the parent remains blocked.
			if (!myBehaviour.isRunnable()) {
				block();
			}
			return exitValue;
		}
		
		/**
		   Propagate the parent to the wrapped behaviour. 
		   NOTE that the <code>parent</code> member variable of the wrapped behaviour
		   must point to the DummyParentBehaviour --> From the wrapped behaviour
		   accessing the actual parent must always be retrieved through the
		   getParent() method.
		 */
		protected void setParent(CompositeBehaviour parent) {
			super.setParent(parent);
			myBehaviour.setThreadedParent(parent);
		}
		
		public void setDataStore(DataStore ds) {
			myBehaviour.setDataStore(ds);
		}
		
		public DataStore getDataStore() {
			return myBehaviour.getDataStore();
		}
		
		public void reset() {
			restarted = false;
			finished = false;
			myBehaviour.reset();
			super.reset();
		}
			
		/**
		   Propagate a restart() call (typically this happens when this 
		   ThreadedBehaviourWrapped is directly added to the agent Scheduler
		   and a message is received) to the wrapped threaded behaviour.
		 */
		public void restart() {
			myBehaviour.restart();
		}
		
		/**
		   Propagate a DOWNWARDS event (typically this happens when this
		   ThreadedBehaviourWrapper is added as a child of a CompositeBehaviour
		   and the latter, or an ancestor, is blocked/restarted)
		   to the wrapped threaded behaviour.
		   If the event is a restart, also notify the dedicated thread.
		 */
		protected void handle(RunnableChangedEvent rce) {
			super.handle(rce);
			if (!rce.isUpwards()) {
				myBehaviour.handle(rce);
				if (rce.isRunnable()) {
					go();
				}
			}
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
						if (restarted && (!myBehaviour.isRunnable())) {
							// We can't just set the runnable state of myBehaviour to true since, if myBehaviour
							// is a CompositeBehaviour, we may end up with myBehaviour runnable, but some of its children not runnable. 
							// However we can't call myBehaviour.restart() here because there could be a deadlock between a thread
							// posting a message and the current thread (monitors are this and the agent scheduler)
							myBehaviour.myEvent.init(true, Behaviour.NOTIFY_DOWN);
							myBehaviour.handle(myBehaviour.myEvent);
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
				System.out.println("Threaded behaviour "+myBehaviour.getBehaviourName()+" interrupted before termination");
			}
			catch (Agent.Interrupted ae) {
				System.out.println("Threaded behaviour "+myBehaviour.getBehaviourName()+" interrupted before termination");
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
			
		public final Thread getThread() {
			return myThread;
		}
		
		public final Behaviour getBehaviour() {
			return myBehaviour;
		}
	} // END of inner class ThreadedBehaviourWrapper
	
	/**
	   Inner class DummyParentBehaviour.
	   This class has the only purpose of propagating restart events
	   in the actual wrapped behaviour to the ThreadedBehaviourWrapper.
	 */
	private class DummyParentBehaviour extends CompositeBehaviour {
		private ThreadedBehaviourWrapper myChild;
		
		private DummyParentBehaviour(Agent a, ThreadedBehaviourWrapper b) {
			super(a);
			myChild = b;
		}
		
		public boolean isRunnable() {
			return false;
		}
		
		protected void handle(RunnableChangedEvent rce) {
			// This is always an UPWARDS event from the threaded behaviour, but
			// there is no need to propagate it to the wrapper since it will 
			// immediately block again. It would be just a waste of time.
			if (rce.isRunnable()) {
				myChild.go();
			}
		}
  	
		/**
		 * Redefine the root() method so that both the DummyParentBehaviour
		 * and the ThreadedBehaviourWrapper are invisible in the behaviours hierarchy
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
	} // END of inner class DummyParentBehaviour
}