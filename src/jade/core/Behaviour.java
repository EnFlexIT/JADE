/*
  $Id$
  */

package jade.core;

/***************************************************************

  Name: Behaviour

  Responsibilities and Collaborations:

  + Provides an abstract interface for agent behaviours, allowing
    behaviour scheduling independently of the actual kind of the
    behaviours.
    (Agent)

  + Sets the basis for Behaviour scheduling, allowing for state
    transitions "runnable"<->"not runnable".
    (Scheduler, Agent)

******************************************************************/
public abstract class Behaviour {

  protected final int NOTIFY_UP = -1;
  protected final int NOTIFY_DOWN = 1;

  // This class is used to notify interested behaviours when a
  // Behaviour changes its runnable state. It may be sent to
  // behaviour's parent ('upward notification' or to behaviour's
  // children ('downward notification').
  protected class RunnableChangedEvent {
    private boolean runnable;
    private int direction;

    // Re-init event content (much cheaper than making a new object)
    public void init(boolean b, int d) {
      runnable = b;
      direction = d;
    }


    // Read-only methods for event receivers

    public Behaviour getSource() {
      return Behaviour.this;
    }

    public boolean isRunnable() {
      return runnable;
    }

    public boolean isUpwards() {
      return direction == NOTIFY_UP;
    }

  } // End of RunnableChangedEvent class


  private boolean runnableState = true;

  // The agent this behaviour belongs to
  protected Agent myAgent;

  // This object will be re-used for every Behaviour notification
  protected RunnableChangedEvent myEvent = new RunnableChangedEvent();

  // Back pointer to the enclosing Behaviour (if present)
  protected ComplexBehaviour parent;

  final void setParent(ComplexBehaviour cb) {
    parent = cb;
  }

  public Behaviour() {
  }

  public Behaviour(Agent a) {
    myAgent = a;
  }

  // Runs the behaviour
  public abstract void action();

  // returns true if the behaviour has completely executed
  public abstract boolean done();

  // This method handles notification by copying its runnable state and
  // then by simply forwarding the events when it is travelling
  // upwards and by doing nothing when it is travelling downwards
  protected void handle(RunnableChangedEvent rce) {
    setRunnable(rce.isRunnable());
    if( (parent != null) && (rce.isUpwards()) ) {
      parent.handle(rce);
    }
  }


  // FIXME: These two methods have package access, but it's still to be decided ...

  // Sets the runnable/not-runnable state
  void setRunnable(boolean runnable) {
    runnableState = runnable;
  }

  // Returns runnable/not-runnable state
  boolean isRunnable() {
    return runnableState;
  }

  // block()/restart() are the public interface to Behaviour
  // scheduling subsystem

  public void block() {
    myEvent.init(false, NOTIFY_UP);
    handle(myEvent);
  }

  public void restart() {
    myEvent.init(true, NOTIFY_UP);
    handle(myEvent);
  }


}
