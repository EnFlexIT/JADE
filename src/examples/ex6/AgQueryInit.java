/*
  $Log$
  Revision 1.1  1999/09/01 13:46:32  rimassa
  Example programs for 'fipa-query' standard protocol.

  Revision 1.3  1999/05/29 16:30:10  belloli
  Added a 'Log:' field to every source file.

*/

package examples.ex6;

import jade.core.*;
import jade.proto.*;
import jade.lang.acl.*;

/**
 * Example of an Agent using the FipaQueryInitiator protocol
 * and the ACLGui to edit ACL Messages.
 */
public class AgQueryInit extends Agent {



class myBehaviour extends FipaQueryInitiatorBehaviour {

public myBehaviour(Agent a, ACLMessage msg) {
super(a,msg);
}

public void handleOtherMessages(ACLMessage msg) {
 System.out.println(myAgent.getLocalName()+" has received the following unexpected message");
 msg.dump(); 
}



public void handleInformMessages(java.util.Vector messages) {
  // all inform messages are handled
  System.out.println(myAgent.getLocalName()+" has received the following inform messages");
  for (int i=0; i<messages.size(); i++)
    ((ACLMessage)(messages.elementAt(i))).dump();
  System.out.println(myAgent.getLocalName()+" - Please use the GUI to enter the query message");
  ACLMessage msg = new ACLMessage("query-if");
  msg.setSource(getLocalName());
  ACLMessage msgquery = jade.gui.AclGui.editMsgInDialog(msg, null);
  if (msgquery == null) {
    System.err.println(getLocalName()+" pressed Cancel button. It causes the kill of this agent.");
    doDelete();
  }
  else
    addBehaviour(new myBehaviour(myAgent,msgquery));
}


}



protected void setup() {
  System.out.println(getLocalName()+" - Please use the GUI to enter the query message");
  ACLMessage msg = new ACLMessage("query-if");
  msg.setSource(getLocalName());
  ACLMessage msgquery = jade.gui.AclGui.editMsgInDialog(msg, null);
  if (msgquery == null) {
    System.err.println(getLocalName()+" pressed Cancel button. It causes the kill of this agent.");
    doDelete();
  }
  else {
    //    msgquery.dump();
    addBehaviour(new myBehaviour(this,msgquery));
  }
}

}


