/*
  $Log$
  Revision 1.5  1998/10/04 18:00:23  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex3;

import jade.core.*;
import jade.lang.acl.*;


// A class using ':reply-with' field to serve messages.
public class AgentMessageFilter extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
	System.out.println("Now receiving messages with :reply-with alt.agents.fipa");
	ACLMessage msg = blockingReceive(MessageTemplate.MatchReplyWith("alt.agents.fipa"));
	String source = msg.getSource();
	String content = msg.getContent();
	System.out.println("Received from " + source + ": " + content);
	System.out.println("Sending back reply to " + source + "...");
	ACLMessage reply = new ACLMessage();
	reply.setSource(getName());
	reply.setDest(source);
	reply.setContent("Thank you for adhering to alt.agents.fipa discussion thread, " + source);
	reply.setReplyTo("alt.agents.fipa");
	send(reply);
      }

    });

  }

}
