/*
  $Log$
  Revision 1.7  1998/10/04 18:00:31  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex5;

// This agent allows a comprehensive testing of Agent Management
// System agent.

import java.io.*;

import jade.core.*;
import jade.lang.acl.*;


public class amsTester extends Agent {

  private static class Receiver {

    // Utility class with private constructor -- do not instantiate.
    private Receiver() {
    }

    public static final ACLMessage receive(amsTester a, String messageType) {

      // Receive <messageType> message from peer

      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchConversationId(a.getConvID());
      MessageTemplate mt3 = MessageTemplate.MatchSource("ams");
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

  // Name of the action the AMS will be required to perform
  private String myAction;

  // Values for the various parameters of the request to AMS agent
  private String agentName = null;
  private String address = null;
  private String signature = null;
  private String APState = null;
  private String delegateAgent = null;
  private String forwardAddress = null;


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
    
  } // End of ReceiveBehaviour class


  protected void setup() {

    int len = 0;
    byte[] buffer = new byte[1024];

    try {
      System.out.println("Enter AMS agent action to perform:");
      len = System.in.read(buffer);
      myAction = new String(buffer,0,len-1);

      System.out.println("Enter values for parameters (ENTER leaves them blank)");

      System.out.print(":agent-name ");
      len = System.in.read(buffer);
      agentName = new String(buffer,0,len-1);

      System.out.print(":address ");
      len = System.in.read(buffer);
      address = new String(buffer,0,len-1);

      System.out.print(":signature ");
      len = System.in.read(buffer);
      signature = new String(buffer,0,len-1);

      System.out.print(":ap-state ");
      len = System.in.read(buffer);
      APState = new String(buffer,0,len-1);

      System.out.print(":delegate-agent-name ");
      len = System.in.read(buffer);
      delegateAgent = new String(buffer,0,len-1);

      System.out.print(":forward-address ");
      len = System.in.read(buffer);
      forwardAddress = new String(buffer,0,len-1);

      System.out.println("");

    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }


    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this);

    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {

	System.out.println("Sending 'request' message to AMS");

	sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(amsTester.this, "not-understood");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(amsTester.this,"refuse");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(amsTester.this,"agree");
	if(msg != null)
	  receiveAgree(msg);
	finished = (msg != null);
      }

    });

    // Nondeterministically receives not-understood, refuse or agree.
    mainBehaviour.addBehaviour(receive1stReply);

    // If agree is received, also receive inform or failure messages.
    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {
	if(agreed()) {
	  
	  ComplexBehaviour receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(amsTester.this);
	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(amsTester.this,"failure");
	      if(msg != null)
		handleFailure(msg);
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(amsTester.this,"inform");
	      if(msg != null)
		handleInform(msg);
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


  public String getAction() {
    return myAction;
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
      "    :receiver ams" +
      "    :protocol fipa-request" +
      "    :ontology fipa-agent-management" +
      "    :language SL0" +
      "    :content  ( action ams ( " + myAction +
      "    ( :ams-description ";

    if(agentName.length() > 0)
      text = text.concat("( :agent-name " + agentName + " )");
    if(address.length() > 0)
      text = text.concat("( :address " + address + " )");
    if(signature.length() > 0)
      text = text.concat("( :signature " + signature + " )");
    if(APState.length() > 0)
      text = text.concat("( :ap-state " + APState + " )");
    if(forwardAddress.length() > 0)
      text = text.concat("( :forward-address" + forwardAddress + " )");
    if(delegateAgent.length() > 0)
      text = text.concat("( :delegate-agent-name" + delegateAgent + " )");

    text = text +
      " ) ) )" +
      " :conversation-id " + convID +
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

  
