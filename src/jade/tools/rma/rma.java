/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.tools.rma;

import java.io.StringReader;
import java.io.StringWriter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.AgentManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.FipaRequestInitiatorBehaviour;
import jade.gui.AgentTreeModel;

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

  private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
  private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);
  private ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);

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
      myGUI.showErrorDialog("NOT-UNDERSTOOD received by RMA during " + actionName, reply);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showErrorDialog("REFUSE received during " + actionName, reply);
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      myGUI.showErrorDialog("FAILURE received during " + actionName, reply);
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
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
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
	  String host = null;
	  InetAddress addr = null;
	  AgentManagementOntology.AMSAgentDescriptor amsd = null;

	  switch(k) {
	  case AgentManagementOntology.AMSEvent.NEWCONTAINER:
	    AgentManagementOntology.AMSContainerEvent ev1 = (AgentManagementOntology.AMSContainerEvent)amse;
	    container = ev1.getContainerName();
	    host = ev1.getContainerAddr();
	    try {
	      addr = InetAddress.getByName(host);
	    }
	    catch(UnknownHostException uhe) {
	      // Do nothing, but leave the address to 'null'
	    }
	    myGUI.addContainer(container, addr);
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
	  case AgentManagementOntology.AMSEvent.MOVEDAGENT:
	    AgentManagementOntology.AMSMotionEvent ev5 = (AgentManagementOntology.AMSMotionEvent)amse;
	    amsd = ev5.getAgentDescriptor();
	    container = ev5.getSrc();
	    myGUI.removeAgent(container, amsd.getName());
	    container = ev5.getDest();
	    myGUI.addAgent(container, amsd.getName(), amsd.getAddress(), "fipa-agent");
	  }

	}
	catch(jade.domain.ParseException pe) {
	  pe.printStackTrace();
	}
	catch(jade.domain.TokenMgrError tme) {
	  tme.printStackTrace();
	}

      }
      else
	block();

    }

  } // End of AMSListenerBehaviour

  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  private transient MainWindow myGUI = new MainWindow(this);

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

  protected void beforeClone() {
  }

  protected void afterClone() {
    // Add yourself to the RMA list
    AMSSubscription.setSource(getLocalName());
    send(AMSSubscription);
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public AgentTreeModel getModel() {
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
      containerName = AgentManagementOntology.PlatformProfile.MAIN_CONTAINER_NAME;

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
    killContainer(AgentManagementOntology.PlatformProfile.MAIN_CONTAINER_NAME);
  }

}
