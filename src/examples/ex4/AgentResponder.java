/*
  $Log$
  Revision 1.4  1998/10/04 18:00:28  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex4;

// This agent plays the responder role in fipa-request protocol.

import jade.core.*;
import jade.lang.acl.*;

public class AgentResponder extends Agent {


  // This behaviour plays responder's role in a fipa-request conversation
  private class ResponderBehaviour extends SimpleBehaviour {

    // A simple behaviour to send an ACL message to an agent
    private class SendBehaviour extends OneShotBehaviour {

      private ACLMessage message;

      public SendBehaviour(ACLMessage msg) {
	message = msg;
      }

      public void action() {
	send(message);
	message.dump();
      }

    } // End of SendBehaviour


    private boolean finished = false;
    private String myPeer;
    private String myConvId;


    public ResponderBehaviour(ACLMessage msg) {
      myPeer = msg.getSource();
      myConvId = msg.getConversationId();
    }

    public void action() {

      // This agent answers 'not-understood' with 20% probability,
      // 'refuse' with 30% probability and 'agree' with 50%
      // probability. If a request is agreed, there is still a 40%
      // failure probability.

      ACLMessage reply = new ACLMessage();
      reply.setSource(getName());
      reply.setDest(myPeer);
      reply.setProtocol("fipa-request");
      reply.setConversationId(myConvId);

      double chance = Math.random();

      if(chance < 0.2) {
	// Reply with 'not-understood'
	reply.setType("not-understood");
	reply.dump();
	send(reply);
      }
      else if(chance < 0.5) {
	// Reply with 'refuse'
	reply.setType("refuse");
	reply.setLanguage("\"Plain Text\"");
	reply.setContent("I'm too busy at the moment. Retry later.");
	reply.dump();
	send(reply);
      }
      else {
	// Reply with 'agree' and schedule next message
	reply.setType("agree");
	reply.dump();
	send(reply);

	chance = Math.random();
	if(chance < 0.4) {
	  // Select a 'failure' message
	  reply.setType("failure");
	  reply.setLanguage("\"Plain Text\"");
	  reply.setContent("Something went wrong with the teleport.");
	}
	else {
	  // Select an 'inform' message
	  reply.setType("inform");
	  reply.setLanguage("\"Plain Text\"");
	  reply.setContent("I hereby imform you that the action has been done.");
	}


	// Schedule a new behaviour to send the message, thereby
	// allowing other behaviours to run between the two send()
	// operations.
	addBehaviour(new SendBehaviour(reply));

      }

      finished = true;

    }


    public boolean done() {
      return finished;
    }

  } // End of ResponderBehaviour


  // This behaviour continously receives 'request' messages and then
  // spawns a ResponderBehaviour to handle them.
  private class MultipleBehaviour extends CyclicBehaviour {

    MessageTemplate pattern;

    public MultipleBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchType("request");

      pattern = MessageTemplate.and(mt1,mt2);

    }

    public void action() {

      ACLMessage request = receive(pattern);

      if(request != null) {
	System.out.println("Received: ");
	request.dump();
	addBehaviour(new ResponderBehaviour(request));
      }

      // With the following two lines no CPU time will be wasted
      else
	block();
    }

  }

  protected void setup() {
    addBehaviour(new MultipleBehaviour());
  }


}
