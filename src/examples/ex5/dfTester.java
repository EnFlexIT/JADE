/*
  $Id$
*/

package examples.ex5;

// This agent allows a comprehensive testing of Directory Facilitator
// agent.

import java.io.*;

import jade.core.*;
import jade.lang.acl.*;


class DFReceiver {

  // Utility class with private constructor -- do not instantiate.
  private DFReceiver() {
  }

  public static final ACLMessage receive(dfTester a, String messageType) {

    // Receive <messageType> message from peer

    MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
    MessageTemplate mt2 = MessageTemplate.MatchConversationId(a.getConvID());
    MessageTemplate mt3 = MessageTemplate.MatchSource("df");
    MessageTemplate mt4 = MessageTemplate.MatchType(messageType);

    MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);
    MessageTemplate mt34 = MessageTemplate.and(mt3, mt4);

    MessageTemplate mt = MessageTemplate.and(mt12, mt34);

    ACLMessage msg = a.receive(mt);

    return msg;

  }

} // End of DFReceiver


public class dfTester extends Agent {

  // Used to generate conversation IDs.
  private int convCounter = 0;

  // Name of the action the DF will be required to perform
  private String myAction;

  // Values for the various parameters of the request to DF agent
  private String name = null;
  private String address = null;
  private String services = null;
  private String type = null;
  private String interactionProtocols = null;
  private String ontology = null;
  private String ownership = null;
  private String DFState = null;


  // Holds the current conversation ID.
  private String convID;

  private boolean receivedAgree = false;


  private abstract class ReceiveBehaviour extends SimpleBehaviour {

    protected boolean finished = false;
    protected dfTester myAgent;

    protected ReceiveBehaviour(dfTester a) {
      myAgent = a;
    }

    public abstract void action();

    public boolean done() {
      return finished;
    }
    
  } // End of ReceiveBehaviour


  protected void setup() {

    int len = 0;
    byte[] buffer = new byte[1024];

    try {
      System.out.println("Enter DF agent action to perform:");
      len = System.in.read(buffer);
      myAction = new String(buffer,0,len-1);

      System.out.println("Enter values for parameters (ENTER leaves them blank)");

      System.out.print(":agent-name ");
      len = System.in.read(buffer);
      name = new String(buffer,0,len-1);

      System.out.print(":agent-address ");
      len = System.in.read(buffer);
      address = new String(buffer,0,len-1);

      System.out.print(":agent-services ");
      len = System.in.read(buffer);
      services = new String(buffer,0,len-1);

      System.out.print(":agent-type ");
      len = System.in.read(buffer);
      type = new String(buffer,0,len-1);

      System.out.print(":interaction-protocols ");
      len = System.in.read(buffer);
      interactionProtocols = new String(buffer,0,len-1);

      System.out.print(":ontology ");
      len = System.in.read(buffer);
      ontology = new String(buffer,0,len-1);

      System.out.print(":ownership ");
      len = System.in.read(buffer);
      ownership = new String(buffer,0,len-1);

      System.out.print(":df-state ");
      len = System.in.read(buffer);
      DFState = new String(buffer,0,len-1);

      System.out.println("");

    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }


    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this);

    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {

	// Send a 'request' message to peer
        dfTester a = (dfTester)myAgent;

	System.out.println("Sending 'request' message to DF");

	a.sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addBehaviour(new ReceiveBehaviour(this) {

      public void action() {

	ACLMessage msg = DFReceiver.receive(myAgent,"not-understood");
	if(msg != null)
	  myAgent.dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour(this) {

      public void action() {

	ACLMessage msg = DFReceiver.receive(myAgent,"refuse");
	if(msg != null)
	  myAgent.dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour(this) {

      public void action() {

	ACLMessage msg = DFReceiver.receive(myAgent,"agree");
	if(msg != null)
	  myAgent.receiveAgree(msg);
	finished = (msg != null);
      }

    });

    // Nondeterministically receives not-understood, refuse or agree.
    mainBehaviour.addBehaviour(receive1stReply);

    // If agree is received, also receive inform or failure messages.
    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      protected void action() {
	dfTester a = (dfTester)myAgent;
	if(a.agreed()) {
	  
	  ComplexBehaviour receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(a);
	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour(a) {

	    public void action() {

	      ACLMessage msg = DFReceiver.receive(myAgent,"failure");
	      if(msg != null)
		myAgent.handleFailure(msg);
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour(a) {

	    public void action() {

	      ACLMessage msg = DFReceiver.receive(myAgent,"inform");
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
      "    :receiver df" +
      "    :protocol fipa-request" +
      "    :ontology fipa-agent-management" +
      "    :language SL0" +
      "    :content  ( action df ( " + myAction;

    if(name.length() > 0)
      text = text.concat("      :agent-name " + name);
    if(address.length() > 0)
      text = text.concat("      :agent-address " + address);
    if(services.length() > 0)
      text = text.concat("      :agent-services " + services);
    if(type.length() > 0)
      text = text.concat("      :agent-type " + type);
    if(interactionProtocols.length() > 0)
      text = text.concat("      :interaction-protocols" + interactionProtocols);
    if(ontology.length() > 0)
      text = text.concat("      :ontology" + ontology);
    if(ownership.length() > 0)
      text = text.concat("      :ownership" + ownership);
    if(DFState.length() > 0)
      text = text.concat("      :df-state" + DFState);

    text = text +
      " ) )" +
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

  
