/*
  $Log$
  Revision 1.4  1998/10/04 18:02:12  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.proto;

import java.util.Enumeration;
import java.util.Vector;

import jade.lang.acl.*;

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
