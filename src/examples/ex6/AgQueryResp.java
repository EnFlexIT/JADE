/*
  $Log$
  Revision 1.1  1999/09/01 13:46:33  rimassa
  Example programs for 'fipa-query' standard protocol.


*/

package examples.ex6;

import jade.core.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.lang.*;

public class AgQueryResp extends Agent {


class myBehaviour extends FipaQueryResponderBehaviour {


public ACLMessage handleQueryMessage(ACLMessage msg) {
  System.err.println(myAgent.getLocalName()+" has replied to the following message: ");
  msg.dump();
  ACLMessage msg1 = msg.createReply(); 
  msg1.setContent(msg.getContent());
  msg1.setType("inform");
  return msg1;
 }

public myBehaviour(Agent a) {
 super(a);
 }

}

protected void setup() {
    addBehaviour(new myBehaviour(this));
  }

}


