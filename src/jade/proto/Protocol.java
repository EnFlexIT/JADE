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

import java.util.Hashtable;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
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
class Protocol {

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
