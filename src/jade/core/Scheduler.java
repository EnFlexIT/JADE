/*
  $Id$
*/

package jade.core;

import java.util.Vector;

/**************************************************************

  Name: Scheduler

  Responsibility and Collaborations:

  + Selects the behaviour to execute.
    (Behaviour)

  + Holds together all the behaviours of an agent.
    (Agent, Behaviour)

  + Manages the resources needed to synchronize and execute agent
    behaviours, such as thread pools, locks, etc.

****************************************************************/
class Scheduler {

  protected Vector behaviours = new Vector();
  private Agent owner;
  private int currentIndex;

  // A static instance of an unnamed inner class to provide a
  // do-nothing behaviour
  private static Behaviour idleBehaviour = null;

  public Scheduler(Agent a) {
    owner = a;
    currentIndex = 0;
  }

  protected void finalize() {
    // Should terminate all threads of the pool.
    // Now, no thread pool has been implemented.
  }

  // Adds a behaviour at the end of the behaviours queue. 
  // This can never change the index of the current behaviour.
  // If the behaviours queue was empty notifies the embedded thread of
  // the owner agent that a behaviour is now available.
  public synchronized void add(Behaviour b) {
    behaviours.addElement(b);
    notify();
  }


  // Removes a specified beaviour from the behaviours queue.

  // This can change the index of the current behaviour, so a check is
  // made: if the just removed behaviour has an index lesser than the
  // current one, then the current index must be decremented.
  public synchronized void remove(Behaviour b) {
    int index = behaviours.indexOf(b);
    behaviours.removeElement(b);
    if(index < currentIndex)
      --currentIndex;
    if(currentIndex < 0)
      currentIndex = 0;
  }

  // Selects the appropriate behaviour for execution, with a trivial
  // round-robin algorithm.
  public synchronized Behaviour schedule() {

    if(behaviours.isEmpty()) {
      // System.out.println("Agent " + owner.getName() + " has nothing to do, so it sleeps ...");
      try {
	wait();
      }
      catch(InterruptedException ie) {
	// Do nothing ...
      }
    }

    Behaviour b = (Behaviour)behaviours.elementAt(currentIndex);
    currentIndex = (currentIndex + 1) % behaviours.size();
    return b;
  }

}

