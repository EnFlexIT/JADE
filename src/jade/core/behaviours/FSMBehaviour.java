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

   
   @author Giovanni Caire - CSELT
   @version $Date$ $Revision$

 */
public class FSMBehaviour extends CompositeBehaviour {

  public static final int DEFAULT_EVENT = 0;
  
  private Map states = new HashMap();
  private Behaviour current = null;
  private String firstName = null;
  private String lastName = null;
  private String currentName = null;
  private String previousName = null;
  
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

  public void registerState(Behaviour state, String name) {
  	state.setParent(this);
  	states.put(name, state);
  }
  
  public void registerFirstState(Behaviour state, String name) {
  	registerState(state, name);
  	firstName = name;
  }
  
  public void registerLastState(Behaviour state, String name) {
  	registerState(state, name);
  	lastName = name;
  }

  public void registerTransition(String s1, String s2, int event) {
  	theTransitionTable.addTransition(s1, s2, event);
  }
  	
  public void registerDefaultTransition(String s1, String s2) {
    theTransitionTable.addDefaultTransition(s1, s2);
  }
  	
  public Behaviour getState(String name) {
  	Behaviour b = null;
  	if (name != null) {
  		b = (Behaviour) states.get(name);
  	}
  	return b;
  }
  
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
  
  protected void scheduleFirst() {
  	currentName = firstName;
  	current = getState(currentName);
  }
  
  protected void scheduleNext(boolean currentDone, int currentResult) {
  	if (currentDone) {
  		try {
  			previousName = currentName;
		 	Transition t = theTransitionTable.getTransition(currentName, currentResult);
			currentName = t.dest;
			current = getState(currentName);
			if (current == null) {
				throw new NullPointerException();
  			}
  		}
  		catch (NullPointerException npe) {
  			throw new RuntimeException("Inconsistent FSM. State: "+previousName+" event: "+currentResult);
  		}
  	}
  }
  
  protected boolean checkTermination(boolean currentDone, int currentResult) { 
  	return (currentDone && (current == getState(lastName)));
  }  		
  
  protected Behaviour getCurrent() {
  	return current;
  }
  
  protected Collection getChildren() {
  	return states.values();
  }
  
  /**
   * Handle block/restart notifications. An
   * <code>FSMBehaviour</code> is blocked <em>only</em> when
   * its currently active child is blocked, and becomes ready again
   * when its current child is ready. This method takes care of the
   * various possibilities.
   * @param rce The event to handle.
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

  /** 
   * Inner class implementing the FSM transition table
   */
  class TransitionTable {
  	private Hashtable transitions = new Hashtable();
  	
  	void addTransition(String s1, String s2, int event) {
  		TransitionsFromState tfs = null;
  		
  		if (!transitions.containsKey(s1)) {
  			tfs = new TransitionsFromState();
  			transitions.put(s1, tfs);
  		}
  		else {
  			tfs = (TransitionsFromState) transitions.get(s1);
  		}
  		
  		tfs.put(new Integer(event), new Transition(s2));
  	}
  	
  	void addDefaultTransition(String s1, String s2) {
  		TransitionsFromState tfs = null;
  		
  		if (!transitions.containsKey(s1)) {
  			tfs = new TransitionsFromState();
  			transitions.put(s1, tfs);
  		}
  		else {
  			tfs = (TransitionsFromState) transitions.get(s1);
  		}
  		
  		tfs.setDefaultTransition(new Transition(s2));
  	}
  		
  	Transition getTransition(String s, int event) {
  		TransitionsFromState tfs = (TransitionsFromState) transitions.get(s);
  		Transition t = (Transition) tfs.get(new Integer(event));
  		return t;
  	}
  }
  
  class Transition {
  	private String dest;
  	
  	public Transition(String d) {
  		dest = d;
  	}
  }
  
  class TransitionsFromState extends Hashtable {
  	private Transition defaultTransition = null;
  	
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
