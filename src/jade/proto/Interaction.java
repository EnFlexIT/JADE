/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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

import jade.core.AgentGroup;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
/**************************************************************

  Name: Interaction

  Responsibility and Collaborations:

  + Represents a specific instance of an agent protocol, with specific
    agent role, participants, protocol state and conversation ID.

  + Navigates a Protocol, acting as an External Iterator on protocol
    container class and maintaining the current communicative action
    for the interaction.
    (Protocol, CommunicativeAction)

  + Decorates its Protocols with user specific code to be invoked when
    a message is received. When a message can have different answers, an
    Interaction object stores user-supplied code to choose among
    different branches of the protocol graph.
    (MessageHandler, MessageSelector)

****************************************************************/
class Interaction {

  int myRole;
  Protocol myProtocol;
  AgentGroup myPeers;
  String conversationId;

  // Hashtable holding the current interaction state for every peer agent.
  private Hashtable currentState = new Hashtable();

  // MessageHandler and MessageSelector associated to each CA
  private Hashtable handlers = new Hashtable();
  private Hashtable selectors = new Hashtable();


  // Private constructor. Use static Factory Methods instead.
  private Interaction(Protocol p, int role) {
    myRole = role;
    myProtocol = p;
    myPeers = new AgentGroup();
    conversationId = null;
  }


  // A couple of static Factory Methods to create an Interaction
  // object with the two different roles for the active Agent.

  public static Interaction createInitiator(Protocol p, AgentGroup peers, String convId) {
    Interaction i = new Interaction(p, Protocol.initiatorRole);
    i.myPeers = peers; // FIXME: Would a clone() be better ?
    i.conversationId = new String(convId);
    return i;
  }

  public static Interaction createResponder(Protocol p) {
    return new Interaction(p, Protocol.responderRole);
  }


  // Methods to manage the AgentGroup containing the peers of the
  // current interaction.

  public void addPeer(String name) {
    myPeers.addMember(name);
    currentState.put(name, myProtocol.getStart());
  }

  public void removePeer(String name) {
    myPeers.removeMember(name);
    currentState.remove(name);
  }


  // Tests whether this Interaction plays the Initiator role with
  // respect to its Protocol.
  public boolean isInitiator() {
    return myRole == Protocol.initiatorRole;
  }


  // Registers an user-supplied MessageHandler to be invoked when the
  // message contained in a CA is processed. If target CA has the same
  // role of the Interaction role (i.e. the associated message will be
  // sent) the handler will be invoked just before the actual message
  // sending. On the other hand, if target CA and Interaction have
  // opposite roles, the MessageHandler will be invoked just after the
  // receipt of the message.
  public void attachHandler(String CA, MessageHandler handler) {
    handlers.put(CA, handler);
  }

  // Registers an user-supplied MessageSelector to be invoked just
  // after the message contained in a CA is processed. The
  // MessageSelector will be passed a MessageGroup containing the
  // possible choices.
  public void attachSelector(String CA, MessageSelector selector) {
    selectors.put(CA, selector);
  }

}
