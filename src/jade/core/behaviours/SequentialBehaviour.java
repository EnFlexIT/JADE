/*
  $Log$
  Revision 1.1  1999/05/20 13:43:17  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.7  1999/04/06 00:09:45  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.6  1998/12/07 23:54:37  rimassa
  Changed bodyAction() method to handle the case when a
  SequentialBehaviour has no children. Now an empty SequentialBehaviour
  ends immediately.

  Revision 1.5  1998/10/04 18:01:16  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core.behaviours;

import jade.core.Agent;

/**
   Composite behaviour with sequential children scheduling. It is a
   <code>ComplexBehaviour</code> that executes its children behaviours
   in sequential order, and terminates when its last child has ended.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class SequentialBehaviour extends ComplexBehaviour {

  /**
     Default constructor. It does not set the owner agent for this
     behaviour.
  */
  public SequentialBehaviour() {
  }

  /**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
  */
  public SequentialBehaviour(Agent a) {
    super(a);
  }

  /**
     Sequential policy for children scheduling. This method executes
     children behaviours one at a time, in a FIFO fashion.
     @see jade.core.behaviours.ComplexBehaviour#bodyAction()
  */
  protected boolean bodyAction() {
    boolean result = false;

    if(!subBehaviours.isEmpty()) {

      Behaviour b = subBehaviours.getCurrent();
      b.action();
      if (b.done()) {
	result = subBehaviours.next();
      }

    }
    else {
      result = true;
    }

    return result;

  }


  /**
     Handle block/restart notifications. A
     <code>SequentialBehaviour</code> is blocked <em>only</em> when
     its currently active child is blocked, and becomes ready again
     when its current child is ready. This method takes care of the
     various possibilities.
     @param rce The event to handle.
  */
  protected void handle(RunnableChangedEvent rce) {

    // For upwards notification from the currently executing
    // sub-behaviour, copy the runnable state and create a new event
    if(rce.isUpwards()) {
      if(rce.getSource() == subBehaviours.getCurrent()) {
	myEvent.init(rce.isRunnable(), NOTIFY_UP);
	super.handle(myEvent);
      }
      // Ignore the event and pass it on
      else
	super.handle(rce);
    }
    // For downwards notifications, always copy the state but
    // forward to current sub-behaviour only when runnable == true
    else {
      boolean b = rce.isRunnable();
      if(b == true)
	super.handle(rce);
      else
	setRunnable(b);
    }

  }

}





