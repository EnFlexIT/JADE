/*
  $Id$
*/

package examples.ex2;

import jade.core.*;
import jade.lang.acl.*;

// An agent who continuously receives messages and sends back replies.
public class AgentReceiver extends Agent {

  protected void setup() {

    addBehaviour(new SimpleBehaviour(this) {

      protected void action() {
	System.out.println("Now receiving (blocking style)...");
	ACLMessage msg = myAgent.blockingReceive();
	String source = msg.getSource();
	String content = msg.getContent();
	System.out.println("Received from " + source + ": " + content);
	System.out.println("Sending back reply to " + source + "...");
	ACLMessage reply = new ACLMessage();
	reply.setSource(myAgent.getName());
	reply.setDest(source);
	reply.setContent("Thank you for calling, " + source);
	myAgent.send(reply);
      }

      public boolean done() {
	return false;
      }

    });

  }

}
