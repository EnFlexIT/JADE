/*
  $Log$
  Revision 1.8  1999/05/20 14:12:39  rimassa
  Updated import clauses to reflect JADE package structure changes.

  Revision 1.7  1999/03/09 12:52:01  rimassa
  Removed deprecated 'ACLMessage.getDest()' and 'ACLMessage.setDest()'
  calls.

  Revision 1.6  1998/10/18 16:10:28  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.5  1998/10/04 18:00:23  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex3;

import jade.core.*;
import jade.core.behaviours.*;
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
	ACLMessage reply = new ACLMessage("inform");
	reply.setSource(getLocalName());
	reply.removeAllDests();
	reply.addDest(source);
	reply.setContent("Thank you for adhering to alt.agents.fipa discussion thread, " + source);
	reply.setReplyTo("alt.agents.fipa");
	send(reply);
      }

    });

  }

}
