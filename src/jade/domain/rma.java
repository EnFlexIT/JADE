/*
  $Log$
  Revision 1.21  1999/05/20 13:43:18  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.20  1999/04/13 16:00:25  rimassa
  Changed GUI destruction and made it occur asynchronously.

  Revision 1.19  1999/04/07 11:42:10  rimassa
  Removed wrong exception handler from takeDown() method. Fixed a bug
  where ACL request messages were sent twice to the AMS.

  Revision 1.18  1999/04/06 16:12:30  rimassa
  Added a check on InterruptedException during dispose().

  Revision 1.17  1999/04/06 00:09:57  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.16  1999/03/30 08:33:44  rimassa
  Added a new behaviour to correctly follow standard 'fipa-request'
  protocol when dealing with the AMS agent.

  Revision 1.15  1999/03/10 06:58:13  rimassa
  Removed some debugging printouts.

  Revision 1.14  1999/03/09 13:21:14  rimassa
  Removed calls to deprecated ACLMessage methods getDest() and
  setDest().
  Removed hardwired name for front end container.

  Revision 1.13  1999/03/07 22:52:36  rimassa
  Added a printout in a catch() block for ParseException.

  Revision 1.12  1999/03/03 16:02:58  rimassa
  Added methods to suspend and resume agents on demand.
  Added a getModel() method to access GUI TreeModel.
  Removed duplicate ACL message objects.

  Revision 1.11  1999/02/25 08:41:36  rimassa
  Added code to remember RMA's container name and an exit() method to
  close 'this' container.
  Changed direct access to 'myName' and 'myAddress' with suitable
  accessor calls.

  Revision 1.10  1999/02/15 11:46:52  rimassa
  Changed a line of code to correctly use Agent.getName().

  Revision 1.9  1999/02/14 23:25:31  rimassa
  Changed addBehaviour() calls to addSubBehaviour() calls where
  appropriate.

  Revision 1.8  1998/12/08 00:11:31  rimassa
  Removed handmade parsing of message content; now updated
  fromText() method is used from various AMS actions.

  Revision 1.7  1998/11/15 23:11:52  rimassa
  Added two public methods killContainer() and shutDownPlatform(), used
  as GUI callbacks.
  Added new ACLMessage object to hold requests to AMS for
  'kill-container' action.

  Revision 1.6  1998/11/09 00:27:11  rimassa
  Added 'RMA' as sender name in ACL messages to the AMS.
  Closing GUI on RMA agent exit.

  Revision 1.5  1998/11/05 23:38:06  rimassa
  Added GUI callback methods to create new agents and to kill them.

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
import java.io.StringWriter;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.gui.*;
import jade.proto.FipaRequestInitiatorBehaviour;

/**
  <em>Remote Management Agent</em> agent. This class implements
  <b>JADE</b> <em>RMA</em> agent. <b>JADE</b> applications cannot use
  this class directly, but interact with it through <em>ACL</em>
  message passing. Besides, this agent has a <em>GUI</em> through
  which <b>JADE</b> Agent Platform can be administered.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class rma extends Agent {

  private ACLMessage AMSSubscription = new ACLMessage("subscribe");
  private ACLMessage AMSCancellation = new ACLMessage("cancel");
  private ACLMessage requestMsg = new ACLMessage("request");

  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(rma.this, request,
	    MessageTemplate.and(MessageTemplate.MatchOntology("fipa-agent-management"),
				MessageTemplate.MatchLanguage("SL0")
				)
	    );
      actionName = an;
    }

    protected void handleNotUnderstood(ACLMessage reply) {
      System.out.println("NOT-UNDERSTOOD received by RMA during " + actionName);
    }

    protected void handleRefuse(ACLMessage reply) {
      System.out.println("REFUSE received by RMA during " + actionName);
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      System.out.println("FAILURE received by RMA during " + actionName);
    }

    protected void handleInform(ACLMessage reply) {
      // System.out.println("INFORM received");
    }

  }

  // Receives notifications by AMS
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
	    String name = amsd.getName();
	    if(name.equalsIgnoreCase(getName())) {
	      myContainerName = new String(container);
	    }
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

  private AMSMainFrame myGUI = new AMSMainFrame(this);

  private String myContainerName;

  /**
   This method starts the <em>RMA</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  public void setup() {

    // Fill ACL messages fields

    AMSSubscription.setSource(getLocalName());
    AMSSubscription.removeAllDests();
    AMSSubscription.addDest("AMS");
    AMSSubscription.setLanguage("SL");
    AMSSubscription.setOntology("jade-agent-management");
    AMSSubscription.setReplyWith("RMA-subscription");
    AMSSubscription.setConversationId(getLocalName());

    // Please inform me whenever container list changes and send me
    // the difference between old and new container lists, complete
    // with every AMS agent descriptor
    String content = "iota ?x ( :container-list-delta ?x )";
    AMSSubscription.setContent(content);

    AMSCancellation.setSource(getLocalName());
    AMSCancellation.removeAllDests();
    AMSCancellation.addDest("AMS");
    AMSCancellation.setLanguage("SL");
    AMSCancellation.setOntology("jade-agent-management");
    AMSCancellation.setReplyWith("RMA-cancellation");
    AMSCancellation.setConversationId(getLocalName());

    // No content is needed (cfr. FIPA 97 Part 2 page 26)

    requestMsg.setSource(getLocalName());
    requestMsg.removeAllDests();
    requestMsg.addDest("AMS");
    requestMsg.setProtocol("fipa-request");
    requestMsg.setOntology("fipa-agent-management");
    requestMsg.setLanguage("SL0");

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, AMSSubscription));

    // Handle incoming 'inform' messages
    AMSSubscribe.addSubBehaviour(new AMSListenerBehaviour());

    // Schedule Behaviour for execution
    addBehaviour(AMSSubscribe);

    // Show Graphical User Interface
    myGUI.ShowCorrect();

  }

  /**
   Cleanup during agent shutdown. This method cleans things up when
   <em>RMA</em> agent is destroyed, disconnecting from <em>AMS</em>
   agent and closing down the platform administration <em>GUI</em>.
  */
  public void takeDown() {
    send(AMSCancellation);
    myGUI.setVisible(false);
    myGUI.disposeAsync();
  }


  /**
   Callback method for platform management <em>GUI</em>.
   */
  public AMSTreeModel getModel() {
    return myGUI.getModel();
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void newAgent(String agentName, String className, String containerName) {

    AgentManagementOntology.CreateAgentAction caa = new AgentManagementOntology.CreateAgentAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    if(agentName.indexOf('@') < 0)
      agentName = agentName.concat('@' + getAddress());

    if(containerName.equals(""))
      containerName = AgentPlatform.MAIN_CONTAINER_NAME;

    amsd.setName(agentName);

    caa.setArg(amsd);
    caa.setClassName(className);
    caa.addProperty(AgentManagementOntology.CreateAgentAction.CONTAINER, containerName);

    StringWriter createText = new StringWriter();
    caa.toText(createText);
    requestMsg.setContent(createText.toString());

    addBehaviour(new AMSClientBehaviour("CreateAgent", requestMsg));

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendAgent(String name) {
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    if(name.indexOf('@') < 0)
      name = name.concat('@' + getAddress());
      
    amsd.setName(name);
    amsd.setAPState(AP_SUSPENDED);
    a.setName(AgentManagementOntology.AMSAction.MODIFYAGENT);
    a.setArg(amsd);

    StringWriter suspendText = new StringWriter();
    a.toText(suspendText);
    requestMsg.setContent(suspendText.toString());

    addBehaviour(new AMSClientBehaviour("SuspendAgent", requestMsg));

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendContainer(String name) {
    // FIXME: Not implemented
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void resumeAgent(String name) {
    AgentManagementOntology.AMSAction a = new AgentManagementOntology.AMSAction();
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();

    if(name.indexOf('@') < 0)
      name = name.concat('@' + getAddress());

    amsd.setName(name);
    amsd.setAPState(AP_ACTIVE);
    a.setName(AgentManagementOntology.AMSAction.MODIFYAGENT);
    a.setArg(amsd);

    StringWriter resumeText = new StringWriter();
    a.toText(resumeText);
    requestMsg.setContent(resumeText.toString());

    addBehaviour(new AMSClientBehaviour("ResumeAgent", requestMsg));
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void resumeContainer(String name) {
    // FIXME: Not implemented
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killAgent(String name) {
    AgentManagementOntology.KillAgentAction kaa = new AgentManagementOntology.KillAgentAction();
    kaa.setAgentName(name);
    StringWriter killText = new StringWriter();
    kaa.toText(killText);
    requestMsg.setContent(killText.toString());

    addBehaviour(new AMSClientBehaviour("KillAgent", requestMsg));

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killContainer(String name) {

    AgentManagementOntology.KillContainerAction kca = new AgentManagementOntology.KillContainerAction();
    kca.setContainerName(name);
    StringWriter killText = new StringWriter();
    kca.toText(killText);
    requestMsg.setContent(killText.toString());

    addBehaviour(new AMSClientBehaviour("KillContainer", requestMsg));

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void exit() {
    killContainer(myContainerName);
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void shutDownPlatform() {
    killContainer(AgentPlatform.MAIN_CONTAINER_NAME);
  }

}
