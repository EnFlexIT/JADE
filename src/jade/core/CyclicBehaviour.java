/*
  $Log$
  Revision 1.2  1998/10/04 18:01:07  rimassa
  Added a 'Log:' field to every source file.

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
