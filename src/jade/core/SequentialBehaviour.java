/*
  $Id$
*/

package jade.core;

/**************************************************************

  Name: MessageTemplate

  Responsibility and Collaborations:

  + Represents set of ACL messages

  + Performs a pattern matching against a given ACL message
    (ACLMessage)

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
