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
   class holds inside a number of <b><em>children behaviours</em></b>.
   When a <code>CompositeBehaviour</code> receives it execution quantum
   from the agent scheduler, it executes one of its children according
   to some policy. This class must be extended to provide the actual
   scheduling policy to apply when running children behaviours.
   @see jade.core.behaviours.SequentialBehaviour
   @see jade.core.behaviours.ParallelBehaviour
   @see jade.core.behaviours.FSMBehaviour

   
   @author Giovanni Rimassa - Universita` di Parma
   @author Giovanni Caire - TILAB
   @version $Date$ $Revision$

 */
public abstract class CompositeBehaviour extends Behaviour {

  /**
    This variable marks the state when no child-behaviour has been run yet.
  */
  private boolean starting = true;
  /**
    This variable marks the state when all child-behaviours have been run.
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
     Executes this <code>CompositeBehaviour</code>. This method 
     executes children according to the scheduling policy 
     defined by concrete subclasses that implements 
     the <code>scheduleFirst()</code> and <code>scheduleNext()</code>
     methods.
  */
  public final void action() {
	if(starting) {
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
   * @param currentDone a flag indicating whether the just executed
   * child has completed or not.
   * @param currentResult the termination value (as returned by
   * <code>onEnd()</code>) of the just executed child in the case this
   * child has completed (otherwise this parameter is meaningless)
   */
  protected abstract void scheduleNext(boolean currentDone, int currentResult);
  
  /**
   * This methods is called after the execution of each child
   * in order to check whether the <code>CompositeBehaviour</code>
   * should terminate.
   * @param currentDone a flag indicating whether the just executed
   * child has completed or not.
   * @param currentResult the termination value (as returned by
   * <code>onEnd()</code>) of the just executed child in the case this
   * child has completed (otherwise this parameter is meaningless)
   * @return true if the <code>CompositeBehaviour</code>
   * should terminate. false otherwise.
   */
  protected abstract boolean checkTermination(boolean currentDone, int currentResult);
  
  /**
   * This method returns the child behaviour currently 
   * scheduled for execution
   */
  protected abstract Behaviour getCurrent();
  
  /**
   * This method returns a Collection view of the children of 
   * this <code>CompositeBehaviour</code> 
   */
  protected abstract Collection getChildren();

  /**
     Blocks this behaviour. When <code>block()</code> is called 
     all its children are notified too.
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
     all its children are notified too.
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

  /**
     Overrides the <code>onStart()</code> method in the 
     <code>Behaviour</code> class by simply calling the 
     <code>preAction()</code> method for backward compatibility.
     @see jade.core.behaviours.Behaviour#onStart()
  */
  public void onStart() {
  	preAction();
  }
  
  /**
     Overrides the <code>onEnd()</code> method in the 
     <code>Behaviour</code> class by simply calling the 
     <code>postAction()</code> method for backward compatibility.
     @see jade.core.behaviours.Behaviour#onEnd()
  */
  public int onEnd() {
  	postAction();
  	return 0;
  }
  
  /**
     @deprecated Use <code>onStart()</code> instead.
     This method is just an empty placeholders for subclasses. It is
     executed just once before starting children
     scheduling. Therefore, it acts as a prolog to the composite
     task represented by this <code>CompositeBehaviour</code>.
  */
  protected void preAction() {
  }

  /**
     @deprecated Use <code>onEnd()</code> instead.
     This method is just an empty placeholder for subclasses. It is
     invoked just once after this behaviour has ended. Therefore,
     it acts as an epilog for the composite task represented by this
     <code>CompositeBehaviour</code>. 
  */
  protected void postAction() {
  }  
}
