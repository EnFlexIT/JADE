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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.AMSEvent;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;

import jade.gui.AgentTreeModel;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;

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

  private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
  private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);
  private ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);

  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(rma.this, request,
	    MessageTemplate.and(MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME),
				MessageTemplate.MatchLanguage(SL0Codec.NAME)
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

  } // End of AMSClientBehaviour class


  // Used by AMSListenerBehaviour
  private interface EventHandler {
    void handle(AMSEvent ev);
  }

  // Receives notifications by AMS
  private class AMSListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate listenTemplate;

    // Ignore case for event names
    private Map handlers = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    AMSListenerBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchInReplyTo("tool-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      listenTemplate = MessageTemplate.and(mt1, mt2);
      listenTemplate = MessageTemplate.and(listenTemplate, mt12);


      // Fill the event handler table.

      handlers.put(JADEAgentManagementOntology.CONTAINERBORN, new EventHandler() {
	public void handle(AMSEvent ev) {
	  ContainerBorn cb = (ContainerBorn)ev;
	  String container = cb.getName();
	  String host = cb.getHost();
	  try {
	    InetAddress addr = InetAddress.getByName(host);
	    myGUI.addContainer(container, addr);
	  }
	  catch(UnknownHostException uhe) {
	    myGUI.addContainer(container, null);
	  }
	}
      });

      handlers.put(JADEAgentManagementOntology.CONTAINERDEAD, new EventHandler() {
        public void handle(AMSEvent ev) {
	  ContainerDead cd = (ContainerDead)ev;
	  String container = cd.getName();
	  myGUI.removeContainer(container);
	}
      });

      handlers.put(JADEAgentManagementOntology.AGENTBORN, new EventHandler() {
        public void handle(AMSEvent ev) {
	  AgentBorn ab = (AgentBorn)ev;
	  String container = ab.getContainer();
	  AID agent = ab.getAgent();
	  myGUI.addAgent(container, agent);
	  if(agent.equals(getAID()))
	    myContainerName = container;
	}
      });

      handlers.put(JADEAgentManagementOntology.AGENTDEAD, new EventHandler() {
        public void handle(AMSEvent ev) {
	  AgentDead ad = (AgentDead)ev;
	  String container = ad.getContainer();
	  AID agent = ad.getAgent();
	  myGUI.removeAgent(container, agent);
	}
      });

      handlers.put(JADEAgentManagementOntology.AGENTMOVED, new EventHandler() {
        public void handle(AMSEvent ev) {
	  AgentMoved am = (AgentMoved)ev;
	  AID agent = am.getAgent();
	  String from = am.getFrom();
	  myGUI.removeAgent(from, agent);
	  String to = am.getTo();
	  myGUI.addAgent(to, agent);
	}
      });
    }

    public void action() {
      ACLMessage current = receive(listenTemplate);
      if(current != null) {
	// Handle 'inform' messages from the AMS
	try {
	  List l = extractContent(current);
	  EventOccurred eo = (EventOccurred)l.get(0);
	  AMSEvent ev = eo.getEvent();
	  String eventName = ev.getEventName();
	  EventHandler h = (EventHandler)handlers.get(eventName);
	  h.handle(ev);
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
	catch(ClassCastException cce) {
	  cce.printStackTrace();
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

    // Register the supported ontologies 
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
    registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());

    // register the supported languages
    registerLanguage(SL0Codec.NAME, new SL0Codec());	

    // Fill ACL messages fields

    AMSSubscription.setSender(getAID());
    AMSSubscription.clearAllReceiver();
    AMSSubscription.addReceiver(getAMS());
    AMSSubscription.setLanguage(SL0Codec.NAME);
    AMSSubscription.setOntology(JADEAgentManagementOntology.NAME);
    AMSSubscription.setReplyWith("tool-subscription");
    AMSSubscription.setConversationId(getLocalName());

    // Please inform me whenever container list changes and send me
    // the difference between old and new container lists, complete
    // with every AMS agent descriptor
    String content = "platform-events";
    AMSSubscription.setContent(content);

    AMSCancellation.setSender(getAID());
    AMSCancellation.clearAllReceiver();
    AMSCancellation.addReceiver(getAMS());
    AMSCancellation.setLanguage(SL0Codec.NAME);
    AMSCancellation.setOntology(JADEAgentManagementOntology.NAME);
    AMSCancellation.setReplyWith("tool-cancellation");
    AMSCancellation.setConversationId(getLocalName());
    // No content is needed (cfr. FIPA 97 Part 2 page 26)

    requestMsg.setSender(getAID());
    requestMsg.clearAllReceiver();
    requestMsg.addReceiver(getAMS());
    requestMsg.setProtocol("fipa-request");
    requestMsg.setLanguage(SL0Codec.NAME);

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
    AMSSubscription.setSender(getAID());
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

    CreateAgent ca = new CreateAgent();

    if(containerName.equals(""))
      containerName = AgentManager.MAIN_CONTAINER_NAME;

    ca.setAgentName(agentName);
    ca.setClassName(className);
    ca.setContainerName(containerName);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(ca);
      List l = new ArrayList(1);
      l.add(a);

      requestMsg.setOntology(JADEAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("CreateAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendAgent(AID name) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.SUSPENDED);
    Modify m = new Modify();
    m.set_0(amsd);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(m);
      List l = new ArrayList(1);
      l.add(a);

      requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("SuspendAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }
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
  public void resumeAgent(AID name) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.ACTIVE);
    Modify m = new Modify();
    m.set_0(amsd);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(m);
      List l = new ArrayList(1);
      l.add(a);

      requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("ResumeAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }
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
  public void killAgent(AID name) {

    KillAgent ka = new KillAgent();

    ka.setAgent(name);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(ka);
      List l = new ArrayList(1);
      l.add(a);

      requestMsg.setOntology(JADEAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("KillAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killContainer(String name) {

    KillContainer kc = new KillContainer();

    kc.setName(name);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(kc);
      List l = new ArrayList(1);
      l.add(a);

      requestMsg.setOntology(JADEAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("KillContainer", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

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
    killContainer(AgentManager.MAIN_CONTAINER_NAME);
  }

}
