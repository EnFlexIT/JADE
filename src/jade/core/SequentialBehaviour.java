/*
  $Id$
*/

package jade.core;

/**************************************************************

  Name: SequentialBehaviour

  Responsibility and Collaborations:

  + It is a ComplexBehaviour that executes its sub-behaviours
    sequentially, and it terminates when all sub-behaviours are done.

****************************************************************/
public class SequentialBehaviour extends ComplexBehaviour {

  public SequentialBehaviour() {
  }

  public SequentialBehaviour(Agent a) {
    super(a);
  }

  protected boolean action() {
    boolean result = false;
    Behaviour b = subBehaviours.getCurrent();
    b.execute();
    if (b.done()) {
      result = subBehaviours.next();
    }

    return result;

  }

}
