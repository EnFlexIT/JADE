/*
  $Log$
  Revision 1.5  1998/10/04 18:01:16  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

/**************************************************************

  Name: SequentialBehaviour

  Responsibility and Collaborations:

  + It is a ComplexBehaviour that executes its sub-behaviours
    sequentially, and it terminates when all sub-behaviours are done.

****************************************************************/
public class SequentialBehaviour extends ComplexBehaviour {

  public SequentialBehaviour() {
  }

  public SequentialBehaviour(Agent a) {
    super(a);
  }

  protected boolean bodyAction() {
    boolean result = false;
    Behaviour b = subBehaviours.getCurrent();
    b.action();
    if (b.done()) {
      result = subBehaviours.next();
    }

    return result;

  }


  // Handle notifications of runnable/not-runnable transitions
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
