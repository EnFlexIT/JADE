/*
  $Log$
  Revision 1.8  1998/11/18 22:53:42  Giovanni
  Fixed a bug: receiveAfterAgree Behaviour was added directly to the
  agent instead of being added to mainBehaviour.
  This resulted in a missing reset().

  Revision 1.7  1998/10/31 12:57:09  rimassa
  Modified example to turn AgentRequester in an endless 'fipa-request'
  client; now the main behaviour (a SequentialBehaviour) calls its new
  reset() method from its postAction(), so it restarts automatically.

  Revision 1.6  1998/10/18 16:10:33  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.5  1998/10/04 18:00:27  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex4;

// This agent plays the initiator role in fipa-request protocol.

import java.io.*;

import jade.core.*;
import jade.lang.acl.*;


public class AgentRequester extends Agent {

  private static class Receiver {

    // Utility class with private constructor -- do not instantiate.
    private Receiver() {
    }

    public static final ACLMessage receive(AgentRequester a, String messageType) {

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

  } // End of Receiver class


  // Used to generate conversation IDs.
  private int convCounter = 0;

  // This is the name of the peer agent, i.e. the one who plays
  // responder's role in fipa-request protocol.
  private String myPeer;

  private int howManyRequests;

  // Holds the current conversation ID.
  private String convID;

  private boolean receivedAgree = false;


  private abstract class ReceiveBehaviour extends SimpleBehaviour {

    protected boolean finished = false;

    protected ReceiveBehaviour() {
    }

    public abstract void action();

    public boolean done() {
      return finished;
    }

    public void reset() {
      finished = false;
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


    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this) {

      protected void postAction() {
	reset();
      }

    };

    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {

	// Send a 'request' message to peer
	System.out.println("Sending 'request' message to: " + getPeer());
	receivedAgree = false;
	sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(AgentRequester.this, "not-understood");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(AgentRequester.this, "refuse");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(AgentRequester.this, "agree");
	if(msg != null)
	  receiveAgree(msg);
	finished = (msg != null);
      }

    });

    // Nondeterministically receives not-understood, refuse or agree.
    mainBehaviour.addBehaviour(receive1stReply);

    // If agree is received, also receive inform or failure messages.
    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      private ComplexBehaviour receiveAfterAgree;

      public void action() {
	if(agreed()) {

	  receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(AgentRequester.this);
	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(AgentRequester.this, "failure");
	      if(msg != null)
		handleFailure(msg);
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(AgentRequester.this, "inform");
	      if(msg != null)
		handleInform(msg);
	      finished = (msg != null);
	    }

	  });

	  // Schedules next behaviour for execution
	  parent.addBehaviour(receiveAfterAgree);
	}

	else
	  System.out.println("Protocol ended.");
      }

      public void reset() {
	if(agreed()) {
	  receivedAgree = false;
	  parent.removeBehaviour(receiveAfterAgree);
	  receiveAfterAgree = null;
	}
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

    ACLMessage toSend = ACLMessage.fromText(new StringReader(text));
    send(toSend);

    System.out.println("[Agent.sendRequest()]\tRequest sent");

  }

  public void dumpMessage(ACLMessage msg) {
    System.out.println("[Agent.dumpMessage]\tReceived message:");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }
 
  public void receiveAgree(ACLMessage msg) {
    System.out.println("[Agent.receiveAgree]\tSuccess!!! Responder agreed to do action !");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    receivedAgree = true;
  }

  public void handleFailure(ACLMessage msg) {
    System.out.println("Responder failed to process the request. Reason was:");
    System.out.println(msg.getContent());
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

  public void handleInform(ACLMessage msg) {
    System.out.println("Responder has just informed me that the action has been carried out.");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

}

  
