/*
  $Log$
  Revision 1.5  1998/10/04 18:01:17  rimassa
  Added a 'Log:' field to every source file.

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

}
