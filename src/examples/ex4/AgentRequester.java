/*
  $Id$
*/

package examples.ex4;

// This agent plays the initiator role in fipa-request protocol.

import java.io.*;

import jade.core.*;
import jade.lang.acl.*;


class Receiver {

  // Utility class with private constructor -- do not instantiate.
  private Receiver() {
  }

  public static final ACLMessage receive(AgentRequester a,String messageType) {
    
    // Receive <messageType> message from peer

    MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
    MessageTemplate mt2 = MessageTemplate.MatchConversationId(a.getConvID());
    MessageTemplate mt3 = MessageTemplate.MatchSource(a.getPeer());
    MessageTemplate mt4 = MessageTemplate.MatchType(messageType);

    MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);
    MessageTemplate mt34 = MessageTemplate.and(mt3, mt4);

    MessageTemplate mt = MessageTemplate.and(mt12, mt34);

    ACLMessage msg = a.receive(mt);

    return msg;

  }

} // End of Receiver


public class AgentRequester extends Agent {

  // Used to generate conversation IDs.
  private int convCounter = 0;

  // This is the name of the peer agent, i.e. the one who plays
  // responder's role in fipa-request protocol.
  private String myPeer;

  private int howManyRequests;

  // Holds the current conversation ID.
  private String convID;

  private boolean receivedAgree = false;


  private abstract class ReceiveBehaviour implements Behaviour {

    protected boolean finished = false;
    protected AgentRequester myAgent;

    protected ReceiveBehaviour(AgentRequester a) {
      myAgent = a;
    }

    public abstract void execute();

    public boolean done() {
      return finished;
    }
    
  } // End of ReceiveBehaviour


  protected void setup() {

    int len = 0;
    byte[] buffer = new byte[1024];

    try {
      System.out.println("Enter responder agent name:");
      len = System.in.read(buffer);
      myPeer = new String(buffer,0,len-1);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }


    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this);

    mainBehaviour.addBehaviour(new SimpleBehaviour(this) {

      public void action() {

	// Send a 'request' message to peer
        AgentRequester a = (AgentRequester)myAgent;

	System.out.println("Sending 'request' message to: " + a.getPeer());

	a.sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addBehaviour(new ReceiveBehaviour(this) {

      public void execute() {

	ACLMessage msg = Receiver.receive(myAgent,"not-understood");
	if(msg != null)
	  myAgent.dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour(this) {

      public void execute() {

	ACLMessage msg = Receiver.receive(myAgent,"refuse");
	if(msg != null)
	  myAgent.dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour(this) {

      public void execute() {

	ACLMessage msg = Receiver.receive(myAgent,"agree");
	if(msg != null)
	  myAgent.receiveAgree(msg);
	finished = (msg != null);
      }

    });

    // Nondeterministically receives not-understood, refuse or agree.
    mainBehaviour.addBehaviour(receive1stReply);

    // If agree is received, also receive inform or failure messages.
    mainBehaviour.addBehaviour(new SimpleBehaviour(this) {

      protected void action() {
	AgentRequester a = (AgentRequester)myAgent;
	if(a.agreed()) {
	  
	  ComplexBehaviour receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(a);
	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour(a) {

	    public void execute() {

	      ACLMessage msg = Receiver.receive(myAgent,"failure");
	      if(msg != null)
		myAgent.handleFailure(msg);
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour(a) {

	    public void execute() {

	      ACLMessage msg = Receiver.receive(myAgent,"inform");
	      if(msg != null)
		myAgent.handleInform(msg);
	      finished = (msg != null);
	    }

	  });

	  // Schedules next behaviour for execution
	  myAgent.addBehaviour(receiveAfterAgree);
	}

	else
	  System.out.println("Protocol ended.");
      }

    });

    addBehaviour(mainBehaviour);

  } // End of setup()


  public String getPeer() {
    return myPeer;
  }

  public boolean agreed() {
    return receivedAgree;
  }

  private String newConvID() {
    String s = new String(myName + (new Integer(convCounter).toString()));
    ++convCounter;
    return s;
  } 

  public String getConvID() {
    return convID;
  }

  // Message handlers for various protocol steps.

  public void sendRequest() {

    convID = newConvID();

    String text = "( request " +
      "    :sender " + myName +
      "    :receiver " + myPeer +
      "    :protocol fipa-request" +
      "    :language \"Plain Text\"" +
      "    :content  ( \"Example Request\" )" +
      "    :conversation-id " + convID +
      ")";

    ACLMessage toSend = parse(new StringReader(text));
    send(toSend);

    System.out.println("[Agent.sendRequest()]\tRequest sent");

  }

  public void dumpMessage(ACLMessage msg) {
    System.out.println("[Agent.dumpMessage]\tReceived message:");
    msg.dump();
  }
 
  public void receiveAgree(ACLMessage msg) {
    System.out.println("[Agent.receiveAgree]\tSuccess!!! Responder agreed to do action !");
    msg.dump();
    receivedAgree = true;
  }

  public void handleFailure(ACLMessage msg) {
    System.out.println("Responder failed to process the request. Reason was:");
    System.out.println(msg.getContent());
    msg.dump();
  }

  public void handleInform(ACLMessage msg) {
    System.out.println("Responder has just informed me that the action has been carried out.");
    msg.dump();
  }

}

  
