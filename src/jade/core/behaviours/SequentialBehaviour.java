
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

import jade.util.leap.*;

import jade.core.Agent;

/**
   Composite behaviour with sequential children scheduling. It is a
   <code>CompositeBehaviour</code> that executes its children behaviours
   in sequential order, and terminates when its last child has ended.
   
   
   @author Giovanni Rimassa - Universita` di Parma
   @author Giovanni Caire - Telecom Italia Lab
   @version $Date$ $Revision$

*/
public class SequentialBehaviour extends CompositeBehaviour {
	
  private BehaviourList subBehaviours = new BehaviourList();

  /**
     Default constructor. It does not set the owner agent for this
     behaviour.
  */
  public SequentialBehaviour() {
  }

  /**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
  */
  public SequentialBehaviour(Agent a) {
    super(a);
  }

  /**
     Prepare the first child for execution
     @see jade.core.behaviours.CompositeBehaviour#scheduleFirst
  */
  protected void scheduleFirst() {
  	// Schedule the first child
  	subBehaviours.begin();
  }
  	
  /**
     Sequential policy for children scheduling. This method schedules
     children behaviours one at a time, in a FIFO fashion.
     @see jade.core.behaviours.CompositeBehaviour#scheduleNext(boolean, int)
  */
  protected void scheduleNext(boolean currentDone, int currentResult) {
    if (currentDone) {
    	// Schedule the next child only if the current one is terminated
		subBehaviours.next();
    }
  }
  
  /**
     Check whether this <code>SequentialBehaviour</code> must terminate.
     @return true when the last child has terminated. false otherwise
     @see jade.core.behaviours.CompositeBehaviour#checkTermination
  */
  protected boolean checkTermination(boolean currentDone, int currentResult) {
  	return (currentDone && subBehaviours.currentIsLast());
  }

  /** 
     Get the current child
     @see jade.core.behaviours.CompositeBehaviour#getCurrent
  */
  protected Behaviour getCurrent() {
  	return subBehaviours.getCurrent();
  }
  
  /**
     Return a Collection view of the children of 
     this <code>SequentialBehaviour</code> 
     @see jade.core.behaviours.CompositeBehaviour#getChildren
  */
  protected Collection getChildren() {
	return subBehaviours;
  }
  	
  /** 
     Add a sub behaviour to this <code>SequentialBehaviour</code>
  */
  public void addSubBehaviour(Behaviour b) {
    subBehaviours.addElement(b);
    b.setParent(this);
    b.setAgent(myAgent);
  }
  
  /** 
     Remove a sub behaviour from this <code>SequentialBehaviour</code>
  */
  public void removeSubBehaviour(Behaviour b) {
    boolean rc = subBehaviours.removeElement(b);
    if(rc) {
      b.setParent(null);
    }
    else {
      // The specified behaviour was not found. Do nothing
    }
  }
  
  /**
     Handle block/restart notifications. A
     <code>SequentialBehaviour</code> is blocked <em>only</em> when
     its currently active child is blocked, and becomes ready again
     when its current child is ready. This method takes care of the
     various possibilities.
     @param rce The event to handle.
  */
  protected void handle(RunnableChangedEvent rce) {
    if(rce.isUpwards()) {
      // Upwards notification
      if (rce.getSource() == this) {
      	// If the event is from this behaviour, set the new 
      	// runnable state and notify upwords.
      	super.handle(rce);
      }
      else if (rce.getSource() == getCurrent()) {
  		// If the event is from the currently executing child, 
  		// create a new event, set the new runnable state and
      	// notify upwords.
		myEvent.init(rce.isRunnable(), NOTIFY_UP);
		super.handle(myEvent);
      }
      else {
      	// If the event is from another child, just ignore it
      }
    }
    else {
      // Downwards notifications 
      // Copy the state and pass it downwords only to the
      // current child
	  setRunnable(rce.isRunnable());
	  Behaviour b  = getCurrent();
	  if (b != null) {
	  	b.handle(rce);
	  }
    }  	
  }

}



