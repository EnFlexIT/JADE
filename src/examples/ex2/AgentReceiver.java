/*
  $Log$
  Revision 1.6  1998/10/04 18:00:18  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex2;

import jade.core.*;
import jade.lang.acl.*;

// An agent who continuously receives messages and sends back replies.
public class AgentReceiver extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
        System.out.println("Now receiving (blocking style)...");
        ACLMessage msg = blockingReceive();
        msg.dump();
        System.out.println("Sending back reply to sender ...");
        ACLMessage reply = new ACLMessage();
        reply.setType("inform");
        reply.setSource(getName());
        reply.setDest(msg.getSource());
        reply.setContent("\"Thank you for calling, " + msg.getSource() + "\"");
        //reply.dump();
        send(reply);
      }

    });

  }

}
