public interface Behaviour {
  public void execute();
}

public class SimpleBehaviour implements Behaviour {

  public void execute() {
    // Do suitable processing for the specific behaviour
  }
}


public class ComplexBehaviour implements Behaviour {

  private Vector steps; // Steps of the composite behaviour

  // This method adds a new subtask to the behaviour
  public void addStep(Behaviour step) {
    steps.addElement(step);
  }

  public void execute() {
    Enumeration cursor = steps.elements();
    while(cursor.hasMoreElements()) {

      // Get next step of the complex behaviour from the enumeration
      Behaviour current = (Behaviour)cursor.nextElement();

      // Execute the step
      current.execute();

      // Allow the execution of other behaviours.
      // Each subtask is an interruption point
      BehaviourMgr.switchBehaviour(this);
    }
  }
}

public class protocolBehaviour implements Behaviour {

  private Protocol myProtocol;

  public protocolBehaviour(Protocol p) {
    myProtocol = p;
  }

  public void execute() {
    // Step through the protocol, repesented as an FSA,
    // sending and receiving the prescribed messages
  }
}

////////////////////////////////////////////

package fipa.proto;

public class FipaStandardProtocols {
  public static final Protocol fipa_request = new Protocol(...);
  public static final Protocol fipa_query = new Protocol(...);
 
}

///////////////////////////////////////////

import fipa.proto.*;

public class myAgent extends Agent {

  protected void local_startup() {
    addTask(fipa_request);
  }

  // ...

} 
