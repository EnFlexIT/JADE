/*
  $Id$
*/

package examples.ex2;

import jade.core.*;
import jade.lang.acl.*;

// An agent who continuously receives messages and sends back replies.
public class AgentReceiver extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      protected void action() {
        System.out.println("Now receiving (blocking style)...");
        ACLMessage msg = myAgent.blockingReceive();
        msg.dump();
        System.out.println("Sending back reply to sender ...");
        ACLMessage reply = new ACLMessage();
        reply.setType("inform");
        reply.setSource(myAgent.getName());
        reply.setDest(msg.getSource());
        reply.setContent("\"Thank you for calling, " + msg.getSource() + "\"");
        //reply.dump();
        myAgent.send(reply);
      }

    });

  }

}
