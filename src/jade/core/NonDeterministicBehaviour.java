/*
  $Id$
*/

package jade.core;

import java.util.Hashtable;

/**************************************************************

  Name: NonDeterministicBehaviour

  Responsibility and Collaborations:

  + It is a ComplexBehaviour that executes its sub-behaviours non
    deterministically, and it terminates when a particular condition
    on its sub-behaviours is met. Static Factory Methods are provided
    to get a NonDeterministicBehaviour that ends when all its
    sub-behaviours are done, when any sub-behaviour terminates or when
    N sub-behaviours have finished.

****************************************************************/
public class NonDeterministicBehaviour extends ComplexBehaviour {

  private static final int WHEN_ALL = 0;
  private static final int WHEN_ANY = 1;

  private int whenToStop;

  private int terminatedSubBehaviours;
  private Hashtable blockedChildren = new Hashtable(); 

  private boolean evalCondition() {

    boolean cond;
    switch(whenToStop) {
    case WHEN_ALL:
      cond = subBehaviours.isEmpty();
      break;
    case WHEN_ANY:
      cond = (terminatedSubBehaviours > 0);
      break;
    default:
      cond = (terminatedSubBehaviours >= whenToStop);
      break;
    }

    return cond;
  }
    

  // Protected constructor: use static Factory Methods instead.
  protected NonDeterministicBehaviour(int endCondition) {
    whenToStop = endCondition;
  }

  // Protected constructor: use static Factory Methods instead.
  protected NonDeterministicBehaviour(Agent a, int endCondition) {
    super(a);
    whenToStop = endCondition;
  }

  protected boolean bodyAction() {

    Behaviour b = subBehaviours.getCurrent();
    b.action();

    boolean partialResult = b.done();
    if(partialResult == true) {
      subBehaviours.removeElement(b);
      ++terminatedSubBehaviours;
    }

    boolean endReached = subBehaviours.next();
    if(endReached)
      subBehaviours.begin();

    return evalCondition();

  }


  // Handle notifications of runnable/not-runnable transitions
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
  public void block() {
    myEvent.init(false, NOTIFY_UP);
    super.handle(myEvent);
  }


  // static Factory Methods to create NonDeterministicBehaviours with
  // various kinds of termination condition.

  public static NonDeterministicBehaviour createWhenAll() {
    return new NonDeterministicBehaviour(WHEN_ALL);
  }

  public static NonDeterministicBehaviour createWhenAll(Agent a) {
    return new NonDeterministicBehaviour(a, WHEN_ALL);
  }

  public static NonDeterministicBehaviour createWhenAny() {
    return new NonDeterministicBehaviour(WHEN_ANY);
  }

  public static NonDeterministicBehaviour createWhenAny(Agent a) {
    return new NonDeterministicBehaviour(a, WHEN_ANY);
  }

  public static NonDeterministicBehaviour createWhenN(int howMany) {
    return new NonDeterministicBehaviour(howMany);
  }

  public static NonDeterministicBehaviour createWhenN(Agent a, int howMany) {
    return new NonDeterministicBehaviour(a, howMany);
  }


}
