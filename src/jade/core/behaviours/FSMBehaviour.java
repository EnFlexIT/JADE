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

import java.util.Hashtable;
import jade.util.leap.*;
import jade.util.leap.Serializable;

import jade.core.Agent;

// DEBUG
//import jade.proto.*;

/**
   Composite behaviour with Finite State Machine based children scheduling. 
   It is a <code>CompositeBehaviour</code> that executes its children 
   behaviours according to a FSM defined by the user. More specifically 
   each child represents a state in the FSM.
   The class provides methods to register states (sub-behaviours) and 
   transitions that defines how sub-behaviours will be scheduled.
   <p> At a minimum, the following steps are needed in order to properly
   define a <code>FSMBehaviour</code>:
   <ul>
   <li> register a single Behaviour as the initial state of the FSM by calling
   the method <code>registerFirstState</code>;
   <li> register one or more Behaviours as the final states of the FSM
   by calling the method <code>registerLastState</code>;
   <li> register one or more Behaviours as the intermediate states of the FSM
   by calling the method <code>registerState</code>;
   <li> for each state of the FSM, register the transitions to the other
   states by calling the method <code>registerTransition</code>;
   <li> the method <code>registerDefaultTransition</code> is also useful
   in order to register a default transition from a state to another state
   independently on the termination event of the source state.
   </ul>
   A number of other methods are available in this class for generic
   tasks, such as getting the current state or the name of a state, ...
   @see jade.core.behaviours.SequentialBehaviour
   @see jade.core.behaviours.ParallelBehaviour
   
   @author Giovanni Caire - CSELT
   @version $Date$ $Revision$

 */
public class FSMBehaviour extends SerialBehaviour {
  
  private Map states = new HashMap();
  private Behaviour current = null;
  private List lastStates = new ArrayList();
  private String firstName = null;
  private String currentName = null;
  private String previousName = null;
  private int lastExitValue;
  
  // These variables are used to force a transition on a given state at runtime
  private boolean transitionForced = false;
  private String forcedTransitionDest = null;
  
  private TransitionTable theTransitionTable = new TransitionTable();
  
  /**
     Default constructor, does not set the owner agent.
  */
  public FSMBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent.
     @param a The agent this behaviour belongs to.
  */
  public FSMBehaviour(Agent a) {
    super(a);
  } 

  /** 
     Register a <code>Behaviour</code> as a state of this 
     <code>FSMBehaviour</code>. When the FSM reaches this state
     the registered <code>Behaviour</code> will be executed.
     @param state The <code>Behaviour</code> representing the state
     @param name The name identifying the state.
  */
  public void registerState(Behaviour state, String name) {
  	state.setBehaviourName(name);
  	state.setParent(this);
  	states.put(name, state);
  }
  
  /** 
     Register a <code>Behaviour</code> as the initial state of this 
     <code>FSMBehaviour</code>. 
     @param state The <code>Behaviour</code> representing the state
     @param name The name identifying the state.
  */
  public void registerFirstState(Behaviour state, String name) {
  	registerState(state, name);
  	firstName = name;
  }
  
  /** 
     Register a <code>Behaviour</code> as a final state of this 
     <code>FSMBehaviour</code>. When the FSM reaches this state
     the registered <code>Behaviour</code> will be executed and, 
     when completed, the <code>FSMBehaviour</code> will terminate too. 
     @param state The <code>Behaviour</code> representing the state
     @param name The name identifying the state.
  */
  public void registerLastState(Behaviour state, String name) {
  	registerState(state, name);
  	if (!lastStates.contains(name)) {
  		lastStates.add(name);
  	}
  }

  /** 
     Register a transition in the FSM defining the policy for
     children scheduling of this <code>FSMBehaviour</code>.
     @param s1 The name of the state this transition starts from
     @param s2 The name of the state this transition leads to
     @param event The termination event that fires this transition
     as returned by the <code>onEnd()</code> method of the 
     <code>Behaviour</code> representing state s1.
     @see jade.core.behaviours.Behaviour#onEnd()
  */
  public void registerTransition(String s1, String s2, int event) {
  	theTransitionTable.addTransition(s1, s2, event, null);
  }
  	
  /** 
     Register a transition in the FSM defining the policy for
     children scheduling of this <code>FSMBehaviour</code>. 
     When this transition is fired the states indicated in the
     <code>toBeReset</code> parameter are reset. This is
     particularly useful for transitions that lead to states that
     have already been visited.
     @param s1 The name of the state this transition starts from
     @param s2 The name of the state this transition leads to
     @param event The termination event that fires this transition
     as returned by the <code>onEnd()</code> method of the 
     <code>Behaviour</code> representing state s1.
     @param toBeReset An array of strings including the names of 
     the states to be reset.
     @see jade.core.behaviours.Behaviour#onEnd()
  */
  public void registerTransition(String s1, String s2, int event, String[] toBeReset) {
  	theTransitionTable.addTransition(s1, s2, event, toBeReset);
  }
  	
  /** 
     Register a default transition in the FSM defining the policy for
     children scheduling of this <code>FSMBehaviour</code>.
     This transition will be fired when state s1 terminates with 
     an event that is not explicitly associated to any transition. 
     @param s1 The name of the state this transition starts from
     @param s2 The name of the state this transition leads to
  */
  public void registerDefaultTransition(String s1, String s2) {
    theTransitionTable.addDefaultTransition(s1, s2, null);
  }
  	
  /** 
     Register a default transition in the FSM defining the policy for
     children scheduling of this <code>FSMBehaviour</code>.
     This transition will be fired when state s1 terminates with 
     an event that is not explicitly associated to any transition. 
     When this transition is fired the states indicated in the
     <code>toBeReset</code> parameter are reset. This is
     particularly useful for transitions that lead to states that
     have already been visited.
     @param s1 The name of the state this transition starts from
     @param s2 The name of the state this transition leads to
     @param toBeReset An array of strings including the names of 
     the states to be reset.
  */
  public void registerDefaultTransition(String s1, String s2, String[] toBeReset) {
    theTransitionTable.addDefaultTransition(s1, s2, toBeReset);
  }
  	
  /** 
     @return the <code>Behaviour</code> representing the state whose
     name is name.
  */
  public Behaviour getState(String name) {
  	Behaviour b = null;
  	if (name != null) {
  		b = (Behaviour) states.get(name);
  	}
  	return b;
  }
  
  /** 
     @return the name of the state represented by <code>Behaviour</code>
     state.
  */
  public String getName(Behaviour state) {
  	Iterator it = states.keySet().iterator();
  	while (it.hasNext()) {
  		String name = (String) it.next();
  		Behaviour s = (Behaviour) states.get(name);
  		if (state == s) {
  			return name;
  		}
  	}
  	return null;
  }
  
  /** 
     @return the exit value of the last executed state.
  */
  public int getLastExitValue() {
  	return lastExitValue;
  }
  	
  /** 
     Override the onEnd() method to return the exit value of the
     last executed state.
  */
  public int onEnd() {
  	return getLastExitValue();
  }
  
  /**
     Prepare the first child for execution. The first child is the 
     <code>Behaviour</code> registered as the first state of this
     <code>FSMBehaviour</code>
     @see jade.core.behaviours.CompositeBehaviour#scheduleFirst
  */
  protected void scheduleFirst() {
  	if (transitionForced) {
  		currentName = forcedTransitionDest;
  		transitionForced = false;
  	}
  	else {
  		// Normal case: go to the first state
  		currentName = firstName;
  	}
  	current = getState(currentName);
  	// DEBUG
  	//if (this instanceof SubscriptionResponder || this instanceof SubscriptionInitiator)
  	//System.out.println(myAgent.getLocalName()+" is Executing state "+currentName);
  }
  
  /**
     This method schedules the next child to be executed. It checks 
     whether the current child is completed and, in this case, fires
     a suitable transition (according to the termination event of 
     the current child) and schedules the child representing the 
     new state.
     @param currentDone a flag indicating whether the just executed
     child has completed or not.
     @param currentResult the termination value (as returned by
     <code>onEnd()</code>) of the just executed child in the case this
     child has completed (otherwise this parameter is meaningless)
     @see jade.core.behaviours.CompositeBehaviour#scheduleNext(boolean, int)
  */
  protected void scheduleNext(boolean currentDone, int currentResult) {
  	if (currentDone) {
  		try {
  			previousName = currentName;
  			if (transitionForced) {
  				currentName = forcedTransitionDest;
  				transitionForced = false;
  			}
  			else {
  				// Normal case: use the TransitionTable to select the next state
				 	Transition t = theTransitionTable.getTransition(currentName, currentResult);
					resetStates(t.toBeReset);
				 	currentName = t.dest;
  			}
				current = getState(currentName);
				if (current == null) {
					throw new NullPointerException();
  			}
  		}
  		catch (NullPointerException npe) {
  			throw new RuntimeException("Inconsistent FSM. State: "+previousName+" event: "+currentResult);
  		}
  		// DEBUG
  		//if (this instanceof SubscriptionResponder || this instanceof SubscriptionInitiator)
  		//System.out.println(myAgent.getLocalName()+ " is Executing state "+currentName);
  	}
  }
  
  /**
     Check whether this <code>FSMBehaviour</code> must terminate.
     @return true when the last child has terminated and it 
     represents a final state. false otherwise
     @see jade.core.behaviours.CompositeBehaviour#checkTermination
  */
  protected boolean checkTermination(boolean currentDone, int currentResult) { 
  	if (currentDone) {
  		lastExitValue = currentResult;
  		return lastStates.contains(currentName);
  	}
  	return false;
  }  		
  
  /** 
     Get the current child
     @see jade.core.behaviours.CompositeBehaviour#getCurrent
  */
  protected Behaviour getCurrent() {
  	return current;
  }
  
  /**
     Return a Collection view of the children of 
     this <code>SequentialBehaviour</code> 
     @see jade.core.behaviours.CompositeBehaviour#getChildren
  */
  public Collection getChildren() {
  	return states.values();
  }
  
  /**
   */
  protected void forceTransitionTo(String next) {
  	// Just check that the forced transition leads into a valid state
  	Behaviour b = getState(next);
  	if (b != null) {
  		transitionForced = true;
  		forcedTransitionDest = next;
  	}
  }
  
  /**
     Put this FSMBehaviour back in the initial condition
   */ 
  public void reset() {
  	super.reset();
  	transitionForced = false;
  	forcedTransitionDest = null;
  }
  	
  /**
     Reset the children behaviours registered in the states indicated in
     the <code>states</code> parameter
     @param states the names of the states that have to be reset
   */
  public void resetStates(String[] states) {	
  	if (states != null) {
	  	for(int i=0; i < states.length; i++){
				Behaviour b = getState(states[i]);
				b.reset();
			}
  	}
  }
  
  /**
   * Handle block/restart notifications. An
   * <code>FSMBehaviour</code> is blocked <em>only</em> when
   * its currently active child is blocked, and becomes ready again
   * when its current child is ready. This method takes care of the
   * various possibilities.
   * @param rce The event to handle.
   *
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
  }*/

  /** 
   * Inner class implementing the FSM transition table
   */
  class TransitionTable implements Serializable {
  	private Hashtable transitions = new Hashtable();
  	private static final long     serialVersionUID = 3487495895819003L;
  	
  	void addTransition(String s1, String s2, int event, String[] toBeReset) {
  		TransitionsFromState tfs = null;
  		
  		if (!transitions.containsKey(s1)) {
  			tfs = new TransitionsFromState();
  			transitions.put(s1, tfs);
  		}
  		else {
  			tfs = (TransitionsFromState) transitions.get(s1);
  		}
  		
  		tfs.put(new Integer(event), new Transition(s2, toBeReset));
  	}
  	
  	void addDefaultTransition(String s1, String s2, String[] toBeReset) {
  		TransitionsFromState tfs = null;
  		
  		if (!transitions.containsKey(s1)) {
  			tfs = new TransitionsFromState();
  			transitions.put(s1, tfs);
  		}
  		else {
  			tfs = (TransitionsFromState) transitions.get(s1);
  		}
  		
  		tfs.setDefaultTransition(new Transition(s2, toBeReset));
  	}
  		
  	Transition getTransition(String s, int event) {
  		TransitionsFromState tfs = (TransitionsFromState) transitions.get(s);
  		Transition t = (Transition) tfs.get(new Integer(event));
  		return t;
  	}
  }
  
  class Transition implements Serializable {
  	private String dest;
  	private String[] toBeReset;
  	private static final long     serialVersionUID = 3487495895819004L;
  	
  	public Transition(String d, String[] rs) {
  		dest = d;
  		toBeReset = rs;
  	}
  }
  
  class TransitionsFromState extends Hashtable {
  	private Transition defaultTransition = null;
  	private static final long     serialVersionUID = 3487495895819005L;
  	
  	void setDefaultTransition(Transition dt) {
  		defaultTransition = dt;
  	}
  	
  	public Object get(Object key) {
  		Transition t = (Transition) super.get(key);
  		if (t == null) {
  			t = defaultTransition;
  		}
  		return t;
  	}
  }
}
