/*
  $Id$
*/

package examples.ex2;

import java.io.IOException;

import java.util.Vector;

import jade.core.*;
import jade.lang.acl.*;

// An agents which sends a message to three other agents, collects the
// replies and prints them.
public class AgentBroadcaster extends Agent {

  private Vector messages = new Vector();

  private class BehaviourElement extends SimpleBehaviour {

    private ACLMessage myMessage;
    private boolean msgSent = false;
    private boolean replyReceived = false;


    public BehaviourElement(String source, String dest, String content) {
      myMessage = new ACLMessage();
      myMessage.setType("request");
      myMessage.setSource(source);
      myMessage.setContent(content);
      myMessage.setDest(dest);
    }

    public void action() {
      if(msgSent == false) {
	System.out.println("Sending to " + myMessage.getDest());
	send(myMessage);
	msgSent = true;
      }
      else {
	ACLMessage reply = receive();
	if(reply != null) {
	  System.out.println("Received reply");
	  appendMessage(reply);
	  replyReceived = true;
	}
      }
    }

    public boolean done() {
      return replyReceived;
    }

  }

  protected void setup() {

    ComplexBehaviour cb = new SequentialBehaviour(this) {

      protected void preAction() {
	try {
	  byte[] buffer = new byte[1024];
	  System.out.println("Enter a message:");
	  int len = System.in.read(buffer);
	  String content = new String(buffer,0,len-1);

	  System.out.println("Enter 3 destination agent names:");
	  len = System.in.read(buffer);
	  String dest1 = new String(buffer,0,len-1);
	  len = System.in.read(buffer);
	  String dest2 = new String(buffer,0,len-1);
	  len = System.in.read(buffer);
	  String dest3 = new String(buffer,0,len-1);

	  ACLMessage msg = new ACLMessage();
	  String source = getName();

	  addBehaviour(new BehaviourElement(source,dest1,content));
	  addBehaviour(new BehaviourElement(source,dest2,content));
	  addBehaviour(new BehaviourElement(source,dest3,content));
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}
      }

      protected void postAction() {
	System.out.println("Post-Action");
	dumpMessage();
	doDelete(); // Terminates the agent
      }

    };

    addBehaviour(cb);

  }

  public synchronized void appendMessage(ACLMessage msg) {
    messages.addElement(msg.getContent());
  }

  public void dumpMessage() {
    System.out.print("The three pieces are: " + messages.elementAt(0) + ", ");
    System.out.println(messages.elementAt(1) + ", " + messages.elementAt(2));
    System.out.println("The result is: " + messages.elementAt(0) + messages.elementAt(1) + messages.elementAt(2));
  }

}
