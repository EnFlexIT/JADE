/*
  $Id$
*/

package jade.core;


// This class models atomic behaviours that must be executed forever
public abstract class CyclicBehaviour extends SimpleBehaviour {

  public CyclicBehaviour() {
    super();
  }

  public CyclicBehaviour(Agent a) {
    super(a);
  }

  public final boolean done() {
    return false;
  }

}
