/*
  $Log$
  Revision 1.1  1999/05/20 13:43:16  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.3  1999/04/06 00:09:39  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.2  1998/10/04 18:01:07  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core.behaviours;

import jade.core.Agent;


/**
   Atomic behaviour that must be executed forever. This abstract class
   can be extended by application programmers to create behaviours
   that keep executing continuously (e.g. simple reactive behaviours).

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public abstract class CyclicBehaviour extends SimpleBehaviour {

  /**
     Default constructor. It does not set the owner agent.
  */
  public CyclicBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent for this
     <code>CyclicBehaviour</code>.
     @param a The agent this behaviour must belong to.
  */
  public CyclicBehaviour(Agent a) {
    super(a);
  }

  /**
     This is the method that makes <code>CyclicBehaviour</code>
     cyclic, because it always returns <code>false</code>.
     @return Always <code>false</code>.
  */
  public final boolean done() {
    return false;
  }

}

