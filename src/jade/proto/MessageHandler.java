/*
  $Id$
*/

package jade.proto;

import jade.lang.acl.ACLMessage;

/**************************************************************

  Name: MessageHandler

  Responsibility and Collaborations:

  + Provides an abstract interface for ProtocolDrivenBehaviour to
    invoke agent-specific code in order to perform some action when a
    specific message is received. An Interaction object is passed to
    user code, thus making the current interaction state accessible.
    (ProtocolDrivenBehaviour, ACLMessage, Interaction)


****************************************************************/
public interface MessageHandler {

  public void handle(ACLMessage msg, Interaction i);
}
