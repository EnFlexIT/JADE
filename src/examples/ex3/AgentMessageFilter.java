/*
  $Id$
*/

package examples.ex3;

import jade.core.*;
import jade.lang.acl.*;


// A class using ':reply-with' field to serve messages.
public class AgentMessageFilter extends Agent {

  protected void setup() {

    addBehaviour(new SimpleBehaviour(this) {

      protected void action() {
	System.out.println("Now receiving messages with :reply-with alt.agents.fipa");
	ACLMessage msg = myAgent.blockingReceive(MessageTemplate.MatchReplyWith("alt.agents.fipa"));
	String source = msg.getSource();
	String content = msg.getContent();
	System.out.println("Received from " + source + ": " + content);
	System.out.println("Sending back reply to " + source + "...");
	ACLMessage reply = new ACLMessage();
	reply.setSource(myAgent.getName());
	reply.setDest(source);
	reply.setContent("Thank you for adhering to p1 discussion thread, " + source);
	myAgent.send(reply);
      }

      public boolean done() {
	return false;
      }

    });

  }

}
