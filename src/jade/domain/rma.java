/*
  $Log$
  Revision 1.2  1998/11/01 15:02:29  rimassa
  Added a Behaviour to register with the AMS as a listener of Agent
  Container Event notifications.

  Revision 1.1  1998/10/26 00:12:30  rimassa
  New domain agent to perform platform administration: this agent has a GUI to
  manage the Agent Platform and special access rights to the AMS.

*/


package jade.domain;

import jade.core.*;
import jade.lang.acl.ACLMessage;
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

    public void action() {

      // Handle inform messages from AMS
      block();

    }
  }

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
