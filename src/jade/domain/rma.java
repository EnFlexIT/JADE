/*
  $Log$
  Revision 1.4  1998/11/03 00:39:52  rimassa
  Added processing of 'inform' messages received from AMS in response to
  AgentPlatform events.

  Revision 1.3  1998/11/02 02:06:23  rimassa
  Started to add a Behaviour to handle 'inform' messages the AMS sends
  when some AgentPlatform event occurs that can be of interest of Remote
  Management Agent.

  Revision 1.2  1998/11/01 15:02:29  rimassa
  Added a Behaviour to register with the AMS as a listener of Agent
  Container Event notifications.

  Revision 1.1  1998/10/26 00:12:30  rimassa
  New domain agent to perform platform administration: this agent has a GUI to
  manage the Agent Platform and special access rights to the AMS.

*/


package jade.domain;

import java.io.StringReader;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.gui.*;

/**************************************************************

  Name: rma

  Responsibility and Collaborations:

  + Serves as Remote Management Agent for the Agent Platform,
    according to our proposal to FIPA 97 specification.

  + Relies on the AMS to perform Agent Management actions, talking
    with it through simple ACL mesages.
    (ams)

****************************************************************/
public class rma extends Agent {

  private ACLMessage AMSSubscription = new ACLMessage("subscribe");
  private ACLMessage AMSCancellation = new ACLMessage("cancel");

  private class AMSListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate listenTemplate;

    AMSListenerBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage("SL");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("jade-agent-management");
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyTo("RMA-subscription");
      mt2 = MessageTemplate.MatchType("inform");
      listenTemplate = MessageTemplate.and(mt1, mt2);
      listenTemplate = MessageTemplate.and(listenTemplate, mt12);

    }

    public void action() {

      ACLMessage current = receive(listenTemplate);
      if(current != null) {
	// Handle inform messages from AMS
	StringReader text = new StringReader(current.getContent());
	try {

	  AgentManagementOntology.AMSEvent amse = AgentManagementOntology.AMSEvent.fromText(text);
	  int k = amse.getKind();

	  String container = null;
	  AgentManagementOntology.AMSAgentDescriptor amsd = null;

	  switch(k) {
	  case AgentManagementOntology.AMSEvent.NEWCONTAINER:
	    AgentManagementOntology.AMSContainerEvent ev1 = (AgentManagementOntology.AMSContainerEvent)amse;
	    container = ev1.getContainerName();
	    myGUI.addContainer(container);
	    break;
	  case AgentManagementOntology.AMSEvent.DEADCONTAINER:
	    AgentManagementOntology.AMSContainerEvent ev2 = (AgentManagementOntology.AMSContainerEvent)amse;
	    container = ev2.getContainerName();
	    myGUI.removeContainer(container);
	    break;
	  case AgentManagementOntology.AMSEvent.NEWAGENT:
	    AgentManagementOntology.AMSAgentEvent ev3 = (AgentManagementOntology.AMSAgentEvent)amse;
	    container = ev3.getContainerName();
	    amsd = ev3.getAgentDescriptor();
	    myGUI.addAgent(container, amsd.getName(), amsd.getAddress(), "fipa-agent");
	    break;
	  case AgentManagementOntology.AMSEvent.DEADAGENT:
	    AgentManagementOntology.AMSAgentEvent ev4 = (AgentManagementOntology.AMSAgentEvent)amse;
	    container = ev4.getContainerName();
	    amsd = ev4.getAgentDescriptor();
	    myGUI.removeAgent(container, amsd.getName());
	    break;
	  }

	}
	catch(ParseException pe) {
	  pe.printStackTrace();
	}
	catch(TokenMgrError tme) {
	  tme.printStackTrace();
	}

      }
      else
	block();

    }

  } // End of AMSListenerBehaviour

  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  private AMSMainFrame myGUI = new AMSMainFrame();

  public void setup() {

    // Fill ACL messages fields

    AMSSubscription.setDest("AMS");
    AMSSubscription.setLanguage("SL");
    AMSSubscription.setOntology("jade-agent-management");
    AMSSubscription.setReplyWith("RMA-subscription");
    AMSSubscription.setConversationId(myName+'@'+myAddress);

    // Please inform me whenever container list changes and send me
    // the difference between old and new container lists, complete
    // with every AMS agent descriptor
    String content = "iota ?x ( :container-list-delta ?x )";
    AMSSubscription.setContent(content);

    AMSCancellation.setDest("AMS");
    AMSCancellation.setLanguage("SL");
    AMSCancellation.setOntology("jade-agent-management");
    AMSCancellation.setReplyWith("RMA-cancellation");
    AMSCancellation.setConversationId(myName+'@'+myAddress);

    // No content is needed (cfr. FIPA 97 Part 2 page 26)

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addBehaviour(new SenderBehaviour(this, AMSSubscription));

    // Handle incoming 'inform' messages
    AMSSubscribe.addBehaviour(new AMSListenerBehaviour());

    // Schedule Behaviour for execution
    addBehaviour(AMSSubscribe);

    // Show Graphical User Interface
    myGUI.ShowCorrect();

  }

}
