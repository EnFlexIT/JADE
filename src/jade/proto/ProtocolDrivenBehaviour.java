/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package jade.proto;

import jade.core.*;
import jade.core.behaviours.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
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
class ProtocolDrivenBehaviour extends SimpleBehaviour {

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

  public void action() {

    if(starting) {

      if(myInteraction.isInitiator())
	startupAsInitiator();
      else
	startupAsResponder();

      starting = false;

    }

    // Performs currently active behaviours.
    currentBehaviours.action();

  }

  public boolean done() {
    return finished;
  }

}
