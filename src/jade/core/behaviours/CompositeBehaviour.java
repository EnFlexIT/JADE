/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

import java.util.*;

import jade.core.Agent;

/**
   An abstract superclass for behaviours composed by many parts. This
   class holds inside a list of <b><em>children behaviours</em></b>,
   to which elements can be aded or emoved dynamically.
   When a <code>CompositeBehaviour</code> receives it execution quantum
   from the agent scheduler, it executes one of its children according
   to some policy. This class must be extended to provide the actual
   scheduling policy to apply when running children behaviours.
   @see jade.core.behaviours.SequentialBehaviour
   @see jade.core.behaviours.ParallelBehaviour

   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public abstract class CompositeBehaviour extends Behaviour {

  // This variables mark the states when no child-behaviour has been run
  // yet and when all child-behaviours have been run.
  /**
  @serial
  */
  private boolean starting = true;
  /**
  @serial
  */
  private boolean finished = false;
  
  private boolean currentDone;
  private int currentResult;
  
  protected boolean currentExecuted; 
  
  /**
     Default constructor, does not set the owner agent.
  */
  protected CompositeBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent.
     @param a The agent this behaviour belongs to.
  */
  protected CompositeBehaviour(Agent a) {
    super(a);
  } 

  /**
     This method is just an empty placeholders for subclasses. It is
     executed just once before starting children
     scheduling. Therefore, it acts as a prolog to the composite
     action represented by this <code>CompositeBehaviour</code>.
  */
  protected void preAction() {
  }

  /**
     Abstract policy method for children execution. Different
     subclasses will implement this method to run children according
     to some policy (sequentially, round robin, priority based, ...).
     This abstract method is the policy routine to be used by
     subclasses to schedule children behaviours. This method must be
     used by application programmer only to create new kinds of
     <code>CompositeBehaviour</code> with custom children scheduling. In
     this case, the method must return <code>true</code> when the
     composite behaviour has ended and <code>false</code>
     otherwise. Typically, the value returned will be some function of
     all termination statuses of children behaviours.
     @return <code>true</code> when done, <code>false</code> when
     children behaviours still need to be run.
     @see jade.core.behaviours.SequentialBehaviour
     @see jade.core.behaviours.ParallelBehaviour 
  protected abstract boolean bodyAction();
  */

  /**
     This method is just an empty placeholder for subclasses. It is
     invoked just once after children scheduling has ended. Therefore,
     it acts as an epilog for the composite task represented by this
     <code>CompositeBehaviour</code>. Overriding this method,
     application programmers can build <em>fork()/join()</em>
     execution structures.
     An useful idiom can be used to implement composite cyclic
     behaviours (e.g. a behaviour that continuously follows a specific
     interaction protocol): puttng a <code>reset()</code> call into
     <code>postAction()</code> method makes a complex behaviour
     restart as soon as it terminates, thereby turning it into a
     cyclic composite behaviour.
   */
  protected void postAction() {
  }

  /**
     Executes this <code>CompositeBehaviour</code>. This method starts
     by executing <code>preAction()</code>; then
     <code>bodyAction()</code> is called once per scheduling turn
     until it returns <code>true</code>. Eventually,
     <code>postAction()</code> is called.
   */
  public final void action() {
	if(starting) {
    	//preAction();
      	scheduleFirst();
    	starting = false;
    }
    else {
    	if (currentExecuted) {
	    	scheduleNext(currentDone, currentResult);
    	}
    }
	currentExecuted = false;
    	
    // Get the current child
    Behaviour current = getCurrent();
    currentDone = false;
    currentResult = 0;
   	
    if (current != null) {
    	if (current.isRunnable()) {
	    	// Execute the current child
    		current.actionWrapper();
    		currentExecuted = true;
    	
    		// If it is done --> call its onEnd() method
    		if (current.done()) {
    			currentDone = true;
    			currentResult = current.onEnd();
    		}
    		
	    	// Check if this CompositeBehaviour is finished
    		finished = checkTermination(currentDone, currentResult);
    	}
    	else {
    		// The currently scheduled child is not runnable --> This
    		// Composite behaviour must block too and notify upwards
    		myEvent.init(false, NOTIFY_UP);
    		super.handle(myEvent);
    	}
    }
    else {
    	// There are no children to execute
    	finished = true;
    }
    	
    //if(finished) {
    //	postAction();
    //}
  }

  /**
     Checks whether this behaviour has terminated.
     @return <code>true</code> if this <code>CompositeBehaviour</code>
     has finished executing, <code>false</code>otherwise.
  */
  public final boolean done() {
    return (finished);
  }

  /**
   * This method schedules the first child to be executed
   */
  protected abstract void scheduleFirst();
  
  /**
   * This method schedules the next child to be executed
   * @return true if no more children have to be run (i.e. this
   * CompositeBehaviour is terminating). false otherwise.
   */
  protected abstract void scheduleNext(boolean currentDone, int currentResult);
  
  /**
   */
  protected abstract boolean checkTermination(boolean currentDone, int currentResult);
  
  /**
   * This method returns the child behaviour currently 
   * scheduled for execution
   */
  protected abstract Behaviour getCurrent();
  
  /**
   * This method returns the children of 
   * this CompositeBehaviour seen as a Collection
   */
  protected abstract Collection getChildren();

  /**
     Blocks this behaviour. When <code>block()</code> is called 
     all its children behaviours are notified too.
  */
  public void block() {
    // Notify upwards
    super.block();

    // Then notify downwards
    myEvent.init(false, NOTIFY_DOWN);
    handle(myEvent);
  }
  
  /**
     Restarts this behaviour. When <code>restart()</code> is called 
     all its children behaviours are notified too.
  */
  public void restart() {
    // Notify upwards
    super.restart();

    // Then notify downwards
    myEvent.init(true, NOTIFY_DOWN);
    handle(myEvent);
  }
  
  /**
     Puts a <code>CompositeBehaviour</code> back in initial state. The
     internal state is cleaned up and <code>reset()</code> is
     recursively called for each child behaviour. 
  */
  public void reset() {

  	Iterator it = getChildren().iterator();
  	while (it.hasNext()) {
  		Behaviour b = (Behaviour) it.next();
  		b.reset();
  	}
  	
    starting = true;
    finished = false;
    super.reset();
  }

  public void onStart() {
  	preAction();
  }
  
  public int onEnd() {
  	postAction();
  	return 0;
  }
}
