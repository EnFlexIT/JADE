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

import java.util.Vector;

import java.io.Serializable;

import jade.core.behaviours.Behaviour;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

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
class Scheduler implements Serializable {

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
  public synchronized Behaviour schedule() throws InterruptedException {

    while(behaviours.isEmpty()) {
      // System.out.println("Agent " + owner.getLocalName() + " has nothing to do, so it sleeps ...");
      wait();
    }

    Behaviour b = (Behaviour)behaviours.elementAt(currentIndex);
    currentIndex = (currentIndex + 1) % behaviours.size();
    return b;
  }

}

