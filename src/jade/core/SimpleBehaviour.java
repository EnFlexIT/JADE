/*
  $Log$
  Revision 1.7  1999/04/06 00:09:46  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.6  1998/10/30 18:23:23  rimassa
  Added an empty implementation of reset() method.

  Revision 1.5  1998/10/04 18:01:17  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;


/**
   An atomic behaviour. This abstract class models behaviours that are
   made by a single, monolithic task and cannot be interrupted.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public abstract class SimpleBehaviour extends Behaviour {

  /**
     Default constructor. It does not set the owner agent for this
     behaviour.
  */
  public SimpleBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
  */
  public SimpleBehaviour(Agent a) {
    super(a);
  }    

  /**
     Resets a <code>SimpleBehaviour</code>. This method does nothing,
     but concrete subclasses can override it with specific code to put
     an object back into its starting state.
  */
  public void reset() {
  }

}
