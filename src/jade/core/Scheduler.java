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

import java.util.List;
import java.util.LinkedList;

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

  /**
     @serial
  */
  protected List readyBehaviours = new LinkedList();

  /**
     @serial
  */
  protected List blockedBehaviours = new LinkedList();

  /**
     @serial
  */
  private Agent owner;

  /**
     @serial
  */
  private int currentIndex;

  public Scheduler(Agent a) {
    owner = a;
    currentIndex = 0;
  }

  // Adds a behaviour at the end of the behaviours queue. 
  // This can never change the index of the current behaviour.
  // If the behaviours queue was empty notifies the embedded thread of
  // the owner agent that a behaviour is now available.
  public synchronized void add(Behaviour b) {
    readyBehaviours.add(b);
    notify();
  }

  // Moves a behaviour from the ready queue to the sleeping queue.
  public synchronized void block(Behaviour b) {
    if (removeFromReady(b)) {
	    blockedBehaviours.add(b);
    }
  }

  // Moves a behaviour from the sleeping queue to the ready queue.
  public synchronized void restart(Behaviour b) {
    if (removeFromBlocked(b)) {
	    readyBehaviours.add(b);
    	notify();
    }
  }

  // Restarts all behaviours. This method simply calls
  // Behaviour.restart() on every behaviour. The
  // Behaviour.restart() method then notifies the agent (with the
  // Agent.notifyRestarted() method), causing Scheduler.restart() to
  // be called.
  // Why not restarting only blocked behaviours?
  // Some ready behaviour can be a NDBehaviour with some of its
  // children blocked. These children must be restarted too.
  public synchronized void restartAll() {
    Behaviour[] behaviours = (Behaviour[])readyBehaviours.toArray(new Behaviour[0]);
    for(int i = 0; i < behaviours.length; i++) {
      Behaviour b = behaviours[i];
      b.restart();
    }
    
    behaviours = (Behaviour[])blockedBehaviours.toArray(new Behaviour[0]);
    for(int i = 0; i < behaviours.length; i++) {
      Behaviour b = behaviours[i];
      b.restart();
    }
  }

  // Removes a specified behaviour from the scheduler
  public synchronized void remove(Behaviour b) {
    boolean found = removeFromBlocked(b);
    if(!found)
      removeFromReady(b);
  }

  // Selects the appropriate behaviour for execution, with a trivial
  // round-robin algorithm.
  public synchronized Behaviour schedule() throws InterruptedException {
    while(readyBehaviours.isEmpty()) {
      // System.out.println("Agent " + owner.getLocalName() + " has nothing to do, so it sleeps ...");
      wait();
    }
    Behaviour b = (Behaviour)readyBehaviours.get(currentIndex);
    currentIndex = (currentIndex + 1) % readyBehaviours.size();
    return b;
  }

  // Removes a specified behaviour from the blocked queue.
  private boolean removeFromBlocked(Behaviour b) {
    return blockedBehaviours.remove(b);
  }

  // Removes a specified behaviour from the ready queue.
  // This can change the index of the current behaviour, so a check is
  // made: if the just removed behaviour has an index lesser than the
  // current one, then the current index must be decremented.
  private boolean removeFromReady(Behaviour b) {
    int index = readyBehaviours.indexOf(b);
    if(index != -1) {
      readyBehaviours.remove(b);
      if(index < currentIndex)
	--currentIndex;
      if(currentIndex < 0)
	currentIndex = 0;
    }
    return index != -1;
  }

}

