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
public class NonDeterministicBehaviour extends ComplexBehaviour {


  public NonDeterministicBehaviour() {
  }

  public NonDeterministicBehaviour(Agent a) {
    super(a);
  }

  protected boolean action() {

    Behaviour b = subBehaviours.getCurrent();
    b.execute();

    boolean result = b.done();

    if(result == false) {
      boolean endReached = subBehaviours.next();
      if(endReached)
	subBehaviours.begin();
    }

    return result;

  }

}
