/*
  $Id$
*/

package fipa.core;

/**************************************************************

  Name: Scheduler

  Responsibility and Collaborations:

  + Selects the behaviour to execute.
    (Behaviour)

  + Holds together all the behaviours of an agent.
    (Agent, Behaviour)

  + Manages the resources needed to synchronize and execute agent
    behaviours, such as thread pools, locks, etc.

****************************************************************/
class Scheduler {

  public Scheduler(Agent a) {
    owner = a;
  }

  protected void finalize() {
    // Should cancel all threads from the pool.
    // Now, no thread pool has been implemented.
  }

  public void addBehaviour(Behaviour b) {
  }

  public void removeBehaviour(Behaviour b) {
  }

  public Behaviour schedule() {
    // Selects the appropriate behaviour for execution

  protected Vector behaviours = new Vector();


  private Agent owner;

}

