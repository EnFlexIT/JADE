package jade.proto;

import jade.core.*;

/**************************************************************

  Name: ProtocolDrivenBehaviour

  Responsibility and Collaborations:

  + Simplifies the realization of an agent behaviour adhering to a
    known interaction protocol.
    (Protocol)

  + Relies on an Interaction object to maintain specific data for
    every interaction the agent participates in.
    (Interaction)

****************************************************************/
public class ProtocolDrivenBehaviour implements Behaviour {

  private boolean starting = true;
  private boolean finished = false;

  private Agent myAgent;
  private Interaction myInteraction;
  private NonDeterministicBehaviour currentBehaviours;


  public ProtocolDrivenBehaviour(Agent a, Interaction i) {
    myAgent = a;
    myInteraction = i;
    currentBehaviours = new NonDeterministicBehaviour(myAgent);

  }


  // TODO: Starting code must distinguish the role. When initiator it
  // must send the first message to peers; when responder, it must
  // receive the first message and read the peer and convId from it.

  // TODO: Suitable sub-behaviours must be written. A
  // receiveSubBehaviour receives the message expected in a given CA,
  // invokes the message handler and advances interaction state
  // corresponding to message sender. A sendSubBehaviour clones the
  // message of a given CA, invokes the message handler and advances
  // interaction state corresponding to message destination.

  public void execute() {

    if(starting) {
      // Do initialization stuff, according to whether playing
      // initiator or responder role.
      starting = false;
    }

    // Schedules currently active behaviours.
    currentBehaviours.execute();

  }

  public boolean done() {
    return finished;
  }

}
