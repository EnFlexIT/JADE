/*
  $Id$
*/

package jade.core;

// This abstract class models atomic behaviours that cannot be interrupted
public abstract class SimpleBehaviour extends Behaviour {


  public SimpleBehaviour() {
    super();
  }

  public SimpleBehaviour(Agent a) {
    super(a);
  }    

  /*
  final void setRunnable(boolean runnable) {
    // No-op
  }

  final boolean isRunnable() {
    return true;
  }
  */
}
