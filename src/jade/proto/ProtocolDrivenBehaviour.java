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
    currentBehaviours = NonDeterministicBehaviour.createWhenAll(myAgent);

  }


  // FIXME: for one-to-one protocols we spawn a ReceiveBehaviour for
  // each protocol branch. For one-to-many protocols, instead, we
  // spawn a ReceiveUntilConditionBehaviour for each protocol
  // branch. The condition will be 'all participants have sent their
  // messages or deadline expired' for fipa-contract-net protocol.

  // TODO: Starting code must distinguish the role. When initiator it
  // must send the first message to peers; when responder, it must
  // receive the first message and read the peer group and convId from
  // it.

  private void startupAsInitiator() {
    
  }

  private void startupAsResponder() {
    
  }

  // TODO: Suitable sub-behaviours must be written. A
  // receiveSubBehaviour receives the message expected in a given CA,
  // invokes the message handler and advances interaction state
  // corresponding to message sender. A sendSubBehaviour clones the
  // message of a given CA, invokes the message handler and advances
  // interaction state corresponding to message destination.

  public void execute() {

    if(starting) {

      if(myInteraction.isInitiator())
	startupAsInitiator();
      else
	startupAsResponder();

      starting = false;

    }

    // Performs currently active behaviours.
    currentBehaviours.execute();

  }

  public boolean done() {
    return finished;
  }

}
