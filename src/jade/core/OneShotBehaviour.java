/*
  $Log$
  Revision 1.3  1999/04/06 00:09:42  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.2  1998/10/04 18:01:12  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

/**
   Atomic behaviour that executes just once. This abstract class can
   be extended by application programmers to create behaviours for
   operations that need to be done just one time.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public abstract class OneShotBehaviour extends SimpleBehaviour {

  /**
     Default constructor. It does not set the owner agent.
  */
  public OneShotBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent for this
     <code>OneShotBehaviour</code>.
     @param a The agent this behaviour belongs to.
  */
  public OneShotBehaviour(Agent a) {
    super(a);
  }

  /**
     This is the method that makes <code>OneShotBehaviour</code>
     one-shot, because it always returns <code>true</code>.
     @return Always <code>true</code>.
  */
  public final boolean done() {
    return true;
  }

}
