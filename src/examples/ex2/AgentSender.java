import java.io.StringReader;
import java.io.IOException;

import jade.core.*;
import jade.lang.acl.*;


// A simple agent that can send a custom message to another agent.
public class AgentSender extends Agent {

  protected void setup() {

    addBehaviour(new SimpleBehaviour(this) {

      protected void action() {
	try {
	  byte[] buffer = new byte[1024];
	  System.out.println("Enter a message:");
	  int len = System.in.read(buffer);
	  String content = new String(buffer,0,len-1);

	  System.out.println("Enter destination agent name:");
	  len = System.in.read(buffer);
	  String dest = new String(buffer,0,len-1);

	  System.out.println("Sending message to " + dest);


	  String text = new String("( request :sender " + myAgent.getName());
	  text = text + " :receiver " + dest + " :content ( " + content + " ) )";
	  System.out.println(text);
	  ACLMessage msg = myAgent.parse(new StringReader(text));

	  myAgent.send(msg);

	  System.out.println("Waiting for reply..");

	  ACLMessage reply = myAgent.blockingReceive();
	  System.out.println("Received from " + reply.getSource());
	  System.out.println(reply.getContent());
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

	myAgent.doDelete(); // Terminates the agent
      }

    });

  }

}
