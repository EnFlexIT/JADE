/*
  $Log$
  Revision 1.2  1998/10/04 18:01:12  rimassa
  Added a 'Log:' field to every source file.

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
