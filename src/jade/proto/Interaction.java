package jade.proto;


/**************************************************************

  Name: Interaction

  Responsibility and Collaborations:

  + Represents a specific instance of an agent protocol, with specific
    agent role, participants, protocol state and conversation ID.

  + Navigates a Protocol, acting as an External Iterator on protocol
    container class and maintaining the current communication action
    for the interaction.
    (Protocol, CommunicativeAction)

  + When a message can have different answers, an Interaction object
    invokes user-supplied code to choose among different branches of
    the protocol graph.
    (ProtocolMessageHandler)

****************************************************************/
public class Interaction {
}
