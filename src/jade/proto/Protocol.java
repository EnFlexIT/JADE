package jade.proto;


/**************************************************************

  Name: Protocol

  Responsibility and Collaborations:

  + Gathers all communicative actions needed to carry out an
    interaction in a single composite object.
    (CommunicativeAction)

  + Maintains the structure of an agent protocol, allowing navigation
    of that structure by multiple simultaneous agent interactions.
    (Interaction)

****************************************************************/
public class Protocol {

  // These two contants are used to distinguish between different
  // protocol roles. In particular, the initiator of an interaction is
  // kept distinct from the responders; in FIPA 97 graphical notation
  // for protocols, communicative actions originated by the initiator
  // are represented as white boxes, whereas the ones originated by
  // other agents are drawn in grey.
  public static final int initiatorRole = 1;
  public static final int responderRole = 2;

  private CommunicativeAction startingPoint;

}
