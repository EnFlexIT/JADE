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

import java.util.Enumeration;
import java.util.Vector;

import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**************************************************************

  Name: CommunicativeAction

  Responsibility and Collaborations:

  + Represents a single step of an agent protocol, holding the ACL
    message to send back to other protocol participants.
    (ACLMessage)

****************************************************************/
class CommunicativeAction {

  private ACLMessage myMessage;
  private Protocol myProtocol;
  private String myName;

  // This can be either Protocol.initiatorRole or Protocol.responderRole.
  private int myRole;

  // This Vector holds the CommunicativeActions that can follow the
  // current one in its protocol.
  private Vector allowedAnswers = new Vector();

  // A CommunicativeAction is bound to an ACL message and an
  // interaction protocol for its whole lifetime. Besides, it has a
  // name with which is registered in a Protocol.
  public CommunicativeAction(ACLMessage msg, Protocol p) {
    myMessage = msg;
    myProtocol = p;
    myName = null;
  }


  // Role handling methods.

  public void makeInitiator() {
    myRole = Protocol.initiatorRole;
  }

  public void makeResponder() {
    myRole = Protocol.responderRole;
  }

  public int getRole() {
    return myRole;
  }

  // Sets the name with which this CommunicativeAction is known inside
  // its Protocol. This method has package visibility because it is to
  // be used only by Protocol class.
  void setName(String name) {
    myName = new String(name);
  }

  // Sets a link between two CommunicativeActions, meaning that 'ca'
  // is an allowed message after 'this' in protocol 'myProtocol'.
  public void addAnswer(CommunicativeAction ca) {
    allowedAnswers.addElement(ca);
  }

  // Returns the allowed answers to the CommunicativeAction in
  // 'myProtocol' interaction protocol.
  public Enumeration getAnswers() {
    return allowedAnswers.elements();
  }

  // Returns the ACL message of the CommunicativeAction
  public ACLMessage getMessage() {
    return myMessage;
  }

}
