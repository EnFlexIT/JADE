/*
  $Id$
*/

package jade.core;

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

  private int terminatedSubBehaviours = 0;


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

  protected boolean action() {

    Behaviour b = subBehaviours.getCurrent();
    b.execute();

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
