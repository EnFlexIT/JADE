/*
  $Id$
*/

package jade.core;

// This class models atomic behaviours that must be executed only one time
public abstract class OneShotBehaviour extends SimpleBehaviour {

  public OneShotBehaviour() {
    super();
  }

  public OneShotBehaviour(Agent a) {
    super(a);
  }

  public final boolean done() {
    return true;
  }

}
