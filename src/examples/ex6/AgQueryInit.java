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

package examples.ex6;

import jade.core.*;
import jade.proto.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

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
  ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
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
  ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
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


