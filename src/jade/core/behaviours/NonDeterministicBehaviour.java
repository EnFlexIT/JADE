/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent
systems in compliance with the FIPA specifications.
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

import jade.core.Agent;

/**
   Composite behaviour with non deterministic children scheduling.
   It is a <code>ComplexBehaviour</code> that executes its children
   behaviours non deterministically, and it terminates when a
   particular condition on its sub-behaviours is met. Static
   <em><b>Factory Methods</b></em> are provided to get a
   <code>NonDeterministicBehaviour</code> that ends when all its
   sub-behaviours are done, when any sub-behaviour terminates or when
   <em>N</em> sub-behaviours have finished.

   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class NonDeterministicBehaviour extends ComplexBehaviour {

  private static final int WHEN_ALL = 0;
  private static final int WHEN_ANY = 1;

  private int whenToStop;

  private Hashtable blockedChildren = new Hashtable(); 
  private BehaviourList terminatedChildren = new BehaviourList();

  private boolean evalCondition() {

    boolean cond;
    switch(whenToStop) {
    case WHEN_ALL:
      cond = subBehaviours.isEmpty();
      break;
    case WHEN_ANY:
      cond = (terminatedChildren.size() > 0);
      break;
    default:
      cond = (terminatedChildren.size() >= whenToStop);
      break;
    }

    return cond;
  }
    

  /**
     Protected constructor: use static <em>Factory Methods</em>
     instead.
  */
  protected NonDeterministicBehaviour(int endCondition) {
    whenToStop = endCondition;
  }

  /**
     Protected constructor: use static <em>Factory Methods</em>
     instead.
  */
  protected NonDeterministicBehaviour(Agent a, int endCondition) {
    super(a);
    whenToStop = endCondition;
  }

  /**
     Nondeterministic policy for children scheduling.  This method
     executes children behaviours one at a time, in a round robin
     fashion.
     @see jade.core.behaviours.ComplexBehaviour#bodyAction()
  */
  protected boolean bodyAction() {

    if(!subBehaviours.isEmpty()) {

      Behaviour b = subBehaviours.getCurrent();
      b.action();

      boolean partialResult = b.done();
      if(partialResult == true) {
	subBehaviours.removeElement(b);
	terminatedChildren.addElement(b);
      }

      boolean endReached = subBehaviours.next();
      if(endReached)
	subBehaviours.begin();

    }

    return evalCondition();

  }

  /**
     Resets this behaviour. This methods puts a
     <code>NonDeterministicBehaviour</code> back in initial state,
     besides calling <code>reset()</code> on each child behaviour
     recursively.
  */
  public void reset() {
    blockedChildren.clear();

    terminatedChildren.begin();
    Behaviour b = terminatedChildren.getCurrent();

    // Restore all terminated sub-behaviours
    while(b != null) {
      terminatedChildren.removeElement(b);
      subBehaviours.addElement(b);
      terminatedChildren.next();
      b = terminatedChildren.getCurrent();
    }
    super.reset();

  }

  /**
     Handle block/restart notifications. A
     <code>NonDeterministicBehaviour</code> object is blocked
     <em>only</em> when all its children behaviours are blocked and
     becomes ready to run as soon as any of its children is
     runnable. This method takes care of the various possibilities.
     @param rce The event to handle.
  */
  protected void handle(RunnableChangedEvent rce) {

    // For upwards notification from sub-behaviours, copy the runnable
    // state and create a new event
    if(rce.isUpwards()) {
      Behaviour b = rce.getSource();
      // Handle the case where a child becomes runnable after being in
      // blocked state. In this case, forward the event only if *all*
      // the children were blocked and thus also 'this' was not
      // runnable
      if(rce.isRunnable()) {
	Object rc = blockedChildren.remove(b);
	// If (the child was in blocked children table) and (this
	// NonDeterministicBehaviour was itself blocked)
	if( (rc != null) && !isRunnable() ) {
	  myEvent.init(true, NOTIFY_UP);
	  super.handle(myEvent);
	}
	else
	  super.handle(rce);
      }
      // Handle the case where a child becomes blocked. If this means
      // that *all* children are now blocked, then the
      // NonDeterministicBehaviour becomes not runnable, too
      else {
	Object rc = blockedChildren.put(b, b);
	// If (the child was not in blocked children table already)
	// and (with the addition of this child all sub-behaviours are
	// blocked)
	if( (rc == null) && (blockedChildren.size() == subBehaviours.size()) ) {
	  myEvent.init(false, NOTIFY_UP);
	  super.handle(myEvent);
	}
	else
	  ; // Do nothing because other sub-behaviours are still active
      }
    }
    // For downwards notifications, always copy the state but
    // forward to sub-behaviours only when runnable == true
    else {
      boolean b = rce.isRunnable();
      if(b == true)
	super.handle(rce);
      else
	setRunnable(b);
    }

  }


  // An overridden version of block() is necessary to allow upwards
  // notification without introducing further branches in handle()
  // method code

  /**
     Blocks this behaviour.
  */
  public void block() {
    myEvent.init(false, NOTIFY_UP);
    super.handle(myEvent);
  }


  // static Factory Methods to create NonDeterministicBehaviours with
  // various kinds of termination condition.

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>all</em></b> its children end. It does not set the owner
     agent for this behaviour.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAll() {
    return new NonDeterministicBehaviour(WHEN_ALL);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>all</em></b> its children end. It sets the owner agent for
     this behaviour.
     @param a The agent this behaviour belongs to.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAll(Agent a) {
    return new NonDeterministicBehaviour(a, WHEN_ALL);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>any</em></b> among its children ends. It does not set the
     owner agent for this behaviour.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAny() {
    return new NonDeterministicBehaviour(WHEN_ANY);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>any</em></b> among its children ends. It sets the owner
     agent for this behaviour.
     @param a The agent this behaviour belongs to.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenAny(Agent a) {
    return new NonDeterministicBehaviour(a, WHEN_ANY);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>at least N</em></b> of its children end. It does not set
     the owner agent for this behaviour.
     @param howMany The number of children behaviour that must
     terminate to make this <code>NonDeterministicBehaviour</code>
     finish.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenN(int howMany) {
    return new NonDeterministicBehaviour(howMany);
  }

  /**
     Static <em>Factory Method</em>. This method creates a new
     <code>NonDeterministicBehaviour</code> that terminates when
     <b><em>at least N</em></b> of its children end. It sets the owner
     agent for this behaviour.
     @param a The agent this behaviour belongs to.
     @param howMany The number of children behaviour that must
     terminate to make this <code>NonDeterministicBehaviour</code>
     finish.
     @return A new <code>NonDeterministicBehaviour</code>.
  */
  public static NonDeterministicBehaviour createWhenN(Agent a, int howMany) {
    return new NonDeterministicBehaviour(a, howMany);
  }


}
