/*
  $Log$
  Revision 1.7  1999/05/20 14:12:39  rimassa
  Updated import clauses to reflect JADE package structure changes.

  Revision 1.6  1998/10/18 16:10:29  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.5  1998/10/04 18:00:24  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex3;

import java.io.StringReader;
import java.io.IOException;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

// This agent starts a conversation thread and collect the reply.
public class AgentThread extends Agent {

  protected void setup() {

    addBehaviour(new OneShotBehaviour(this) {

      public void action() {
	try {
	  byte[] buffer = new byte[1024];
	  System.out.println("Enter a message:");
	  int len = System.in.read(buffer);
	  String content = new String(buffer,0,len-1);

	  System.out.println("Enter destination agent name:");
	  len = System.in.read(buffer);
	  String dest = new String(buffer,0,len-1);

	  System.out.println("Sending message to " + dest);


	  String text = new String("( request :sender " + getName());
	  text = text + " :receiver " + dest + " :content ( " + content + " ) )";
	  System.out.println(text);
	  ACLMessage msg = ACLMessage.fromText(new StringReader(text));


	  send(msg);
	  send(msg);
	  send(msg);

	  msg.setReplyWith("alt.agents.fipa");

	  send(msg);

	  System.out.println("Waiting for reply..");

	  MessageTemplate mt = MessageTemplate.MatchReplyTo("alt.agents.fipa");
	  ACLMessage reply = blockingReceive(mt);
	  System.out.println("Received from " + reply.getSource());
	  System.out.println(reply.getContent());
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

	doDelete(); // Terminates the agent
      }

    });

  }

}
