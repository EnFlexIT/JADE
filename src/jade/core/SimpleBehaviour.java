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

  // This method must be implemented by subclasses
  protected abstract void action();

  public final void execute() {
    action();
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
