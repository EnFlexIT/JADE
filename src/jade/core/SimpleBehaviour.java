/*
  $Id$
  */

package jade.core;

public abstract class SimpleBehaviour implements Behaviour {

  protected Agent myAgent;
  protected boolean finished = false;

  public SimpleBehaviour() {
    myAgent = null;
    finished = false;
  }

  public SimpleBehaviour(Agent a) {
    myAgent = a;
    finished = false;
  }    

  // This method must be implemented by subclasses
  protected abstract void action();

  public final void execute() {
    action();
    finished = true;
  }

  public boolean done() {
    return finished;
  }

}
