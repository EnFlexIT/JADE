/*
  $Id$
*/

package jade.proto;

import java.util.Hashtable;

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

  // These two constants are used to distinguish between different
  // protocol roles. In particular, the initiator of an interaction is
  // kept distinct from the responders; in FIPA 97 graphical notation
  // for protocols, communicative actions originated by the initiator
  // are represented as white boxes, whereas the ones originated by
  // other agents are drawn in grey.
  static final int initiatorRole = 1;
  static final int responderRole = 2;

  // Name of the initial CommunictiveAction of this Protocol.
  private static final String START_NAME = "Start";

  protected CommunicativeAction startingPoint;

  // This Hashtable allows to refer to protocol elements by name
  // instead of navigating protocol structure.
  protected Hashtable myElements;


  public Protocol(CommunicativeAction start) {
    start.makeInitiator();
    myElements.put(START_NAME, start);
    startingPoint = start;
  }

  public CommunicativeAction getStart() {
    return startingPoint;
  }

  // Inserts a CommunicativeAction into the Protocol structure; a
  // CommunicativeAction can be retrieved later using its name.
  public void addCA(CommunicativeAction ca, String name) {
    myElements.put(name, ca);
    ca.setName(name);
  }

  // Retrieves a CommunicativeAction, using the name as a key.
  public CommunicativeAction getCA(String name) {
    return (CommunicativeAction)myElements.get(name);
  }

}
