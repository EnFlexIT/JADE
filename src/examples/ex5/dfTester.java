/*

  $Log$
  Revision 1.12  1998/11/30 00:12:23  rimassa
  Almost completely rewritten; now it correctly supports a complete DF
  interaction, search action included. Still missing some detail, such
  as search constraints.

  Revision 1.11  1998/11/23 00:11:58  rimassa
  Fixed a little bug: an instance variable vas not reset after each
  message received.

  Revision 1.10  1998/11/18 22:56:49  Giovanni
  Reading input from the standard input has been moved from setup()
  method to an agent Behaviour. Besides, a suitable reset() method was
  added to mainBehaviour, in order to make dfTester agent perform an
  endless loop of requests to the DF, asking for different parameters
  each time.

  Revision 1.9  1998/10/18 17:33:53  rimassa
  Added support for 'search' DF operation.

  Revision 1.8  1998/10/18 16:10:39  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.7  1998/10/11 19:09:17  rimassa
  Removed old,commented out code.

  Revision 1.6  1998/10/04 18:00:32  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex5;

// This agent allows a comprehensive testing of Directory Facilitator
// agent.

import java.io.*;
import java.util.Vector;


import jade.core.*;
import jade.lang.acl.*;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;


public class dfTester extends Agent {

  private static class Receiver {

    // Utility class with private constructor -- do not instantiate.
    private Receiver() {
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

  } // End of Receiver class

  private ComplexBehaviour receiveAfterAgree;

  // Used to generate conversation IDs.
  private int convCounter = 0;

  // Holds the current conversation ID.
  private String convID;

  private boolean receivedAgree = false;

  // Holds a Java object representation of DF action to perform
  private AgentManagementOntology.DFSearchAction myAction;
  private String constraints;


  private abstract class ReceiveBehaviour extends SimpleBehaviour {

    protected boolean finished = false;

    protected ReceiveBehaviour() {
    }

    public void reset() {
      finished = false;
    }

    public abstract void action();

    public boolean done() {
      return finished;
    }

  } // End of ReceiveBehaviour class


  protected void setup() {

    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this) {

      protected void postAction() {
	reset();
      }

    };

    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {

	int len = 0;
	byte[] buffer = new byte[1024];

	try {
	  System.out.println("Enter DF agent action to perform: (register, deregister, modify, search) ");
	  len = System.in.read(buffer);
	  myAction = new AgentManagementOntology.DFSearchAction();
	  AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
	  myAction.setName(new String(buffer,0,len-1));
	  myAction.setArg(dfd);

	  System.out.println("Enter values for parameters (ENTER leaves them blank)");

	  String name = null;
	  String address = null;
	  System.out.print(":agent-name (e.g. Peter; mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    name = new String(buffer,0,len-1);

	  System.out.println(":agent-address (e.g. iiop://fipa.org:50/acc)");
	  System.out.print("(mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    address = new String(buffer,0,len-1);

	  if(address != null) {
	    if(name != null)
	      dfd.setName(name + "@" + address);
	    dfd.addAddress(address);
	  }

	  System.out.print(":agent-type (mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setType(new String(buffer,0,len-1));

	  System.out.println(":interaction-protocols ");
	  System.out.print("  protocol name (ENTER to end) ");
	  len = System.in.read(buffer);
	  while(len > 1) {
	    dfd.addInteractionProtocol(new String(buffer,0,len-1));
	    System.out.print("  protocol name (Enter to end) ");
	    len = System.in.read(buffer);
	  }

	  System.out.print(":ontology ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setOntology(new String(buffer,0,len-1));

	  System.out.print(":ownership (mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setOwnership(new String(buffer,0,len-1));

	  System.out.print(":df-state (active, suspended or retired; mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setDFState(new String(buffer,0,len-1));

	  System.out.println(":agent-services ");
	  System.out.println(":service-description (leave name blank to end)");
	  System.out.print("  :service-name ");
	  len = System.in.read(buffer);
	  while(len > 1) {
	    AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
	    sd.setName(new String(buffer,0,len-1));
	    System.out.print("  :service-type ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setType(new String(buffer,0,len-1));
	    System.out.print("  :service-ontology ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setOntology(new String(buffer,0,len-1));
	    System.out.print("  :fixed-properties ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setFixedProps(new String(buffer,0,len-1));
	    System.out.print("  :negotiable-properties ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setNegotiableProps(new String(buffer,0,len-1));
	    System.out.print("  :communication-properties ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setCommunicationProps(new String(buffer,0,len-1));

	    dfd.addAgentService(sd);

	    System.out.println(":service-description (leave name blank to end)");
	    System.out.print("  :service-name ");
	    len = System.in.read(buffer);
	  }

	  System.out.println("");

	  String actionName = myAction.getName();
	  if(actionName.equalsIgnoreCase("search")) {
	    AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
	    c.setName(AgentManagementOntology.Constraint.DFDEPTH);
	    c.setFn(AgentManagementOntology.Constraint.EXACTLY);
	    c.setArg(1);
	    myAction.addConstraint(c);

	    AgentManagementOntology.DFSearchResult agents = null;
	    System.out.println("Calling searchDF()");
	    try {
	      agents = searchDF("df", myAction.getArg(), null);
	    }
	    catch(FIPAException fe) {
	      fe.printStackTrace();
	      return;
	    }
	    Writer w = new BufferedWriter(new OutputStreamWriter(System.out));
	    agents.toText(w);
	  }
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

      }

    });

    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {

	// Send a 'request' message to peer
	System.out.println("Sending 'request' message to DF");
	sendRequest();

      }
    });

    ComplexBehaviour receive1stReply = NonDeterministicBehaviour.createWhenAny(this);

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(dfTester.this,"not-understood");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(dfTester.this,"refuse");
	if(msg != null)
	  dumpMessage(msg);
	finished = (msg != null);
      }

    });

    receive1stReply.addBehaviour(new ReceiveBehaviour() {

      public void action() {

	ACLMessage msg = Receiver.receive(dfTester.this, "agree");
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
	  
	  receiveAfterAgree = NonDeterministicBehaviour.createWhenAny(dfTester.this);
	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(dfTester.this, "failure");
	      if(msg != null)
		handleFailure(msg);
	      finished = (msg != null);
	    }

	  });

	  receiveAfterAgree.addBehaviour(new ReceiveBehaviour() {

	    public void action() {

	      ACLMessage msg = Receiver.receive(dfTester.this, "inform");
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

    ACLMessage toSend = new ACLMessage("request");

    toSend.setSource(myName);
    toSend.setDest("df");
    toSend.setProtocol("fipa-request");
    toSend.setOntology("fipa-agent-management");
    toSend.setLanguage("SL0");
    toSend.setConversationId(convID);

    StringWriter w = new StringWriter();
    myAction.toText(w);
    toSend.setContent("( action df " + w + " )");

    // toSend.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    send(toSend);

    System.out.println("[Agent.sendRequest()]\tRequest sent");

  }

  public void dumpMessage(ACLMessage msg) {
    System.out.println("[Agent.dumpMessage]\tReceived message:");
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }
 
  public void receiveAgree(ACLMessage msg) {
    System.out.println("[Agent.receiveAgree]\tSuccess!!! Responder agreed to do action !");
    // msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    receivedAgree = true;
  }

  public void handleFailure(ACLMessage msg) {
    System.out.println("Responder failed to process the request. Reason was:");
    System.out.println(msg.getContent());
    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

  public void handleInform(ACLMessage msg) {
    System.out.println("Responder has just informed me that the action has been carried out.");
    //    msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }

}

  
