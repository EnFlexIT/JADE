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

package jade.tools.sniffer;


import java.io.StringReader;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.AMSEvent;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;

import jade.proto.FipaRequestInitiatorBehaviour;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *  This is the <em>Sniffer</em> agent.<br>
 *  This class implements the low level part of the Sniffer, interacting with Jade
 *  environment and with the sniffer GUI.<br>
 *  At startup, the sniffer subscribes itself as an rma to be informed every time
 *  an agent is born or dead, a container is created or deleted.<br>
 *  For more information see <a href="../../../../intro.htm" target="_top">Introduction to the Sniffer</a>
 * Javadoc documentation for the file
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>(Developement) 
 * @author Gianluca Tanca (Concept & Early Version)
 * @version $Date$ $Revision$
 * 
 */
public class Sniffer extends jade.core.Agent {

  public static final boolean SNIFF_ON = true;
  public static final boolean SNIFF_OFF = false;

  private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
  private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);
  private ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
  private LinkedList agentsUnderSniff = new LinkedList();


  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(Sniffer.this, request,
	    MessageTemplate.and(MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME),
				MessageTemplate.MatchLanguage(SL0Codec.NAME)
				)
	    );
      actionName = an;
    }

    protected void handleNotUnderstood(ACLMessage reply) {
      myGUI.showError("NOT-UNDERSTOOD received by RMA during " + actionName);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showError("REFUSE received during " + actionName);
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      myGUI.showError("FAILURE received during " + actionName);
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
	  if(h != null)
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


  private class SniffListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate listenSniffTemplate;

    SniffListenerBehaviour() {
      listenSniffTemplate = MessageTemplate.MatchOntology("sniffed-message");
    }

    public void action() {

      ACLMessage current = receive(listenSniffTemplate);
      if(current != null) {

	try {
	  ACLCodec codec = new StringACLCodec();
	  String content = current.getContent();
	  ACLMessage tmp = codec.decode(content.getBytes());
	  Message msg = new Message(tmp);
	  myGUI.mainPanel.panelcan.canvMess.recMessage(msg);
	} 
	catch(Throwable e) {
	  //System.out.println("Serious problem Occurred");
	  myGUI.showError("An error occurred parsing the incoming message.\n" +
			  "          The message was lost.");
	  e.printStackTrace();
	}
      }
      else
      	block();
    }

  } // End of SniffListenerBehaviour


  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  /**
    @serial
  */
  private MainWindow myGUI;

  /**
    @serial
  */
  private String myContainerName;

  /**
   * ACLMessages for subscription and unsubscription as <em>rma</em> are created and
   * corresponding behaviours are set up.
   */
  public void setup() {

    // Register the supported ontology 
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
    requestMsg.setOntology(JADEAgentManagementOntology.NAME);
    requestMsg.setLanguage(SL0Codec.NAME);

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, AMSSubscription));

    // Handle incoming 'inform' messages
    AMSSubscribe.addSubBehaviour(new AMSListenerBehaviour());

    // Schedule Behaviours for execution
    addBehaviour(AMSSubscribe);
    addBehaviour(new SniffListenerBehaviour()); 

    // Show Graphical User Interface
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();

  }

  /**
   * Cleanup during agent shutdown. This method cleans things up when
   * <em>Sniffer</em> agent is destroyed, disconnecting from <em>AMS</em>
   * agent and closing down the Sniffer administration <em>GUI</em>.
   * Currently sniffed agents are also unsniffed to avoid errors.
   */
  public void takeDown() {

    List l = (List)(agentsUnderSniff.clone());
    sniffMsg(l, SNIFF_OFF);

    myGUI.mainPanel.panelcan.canvMess.ml.removeAllMessages();

    // Now we unsubscribe from the rma list
    send(AMSCancellation);
    myGUI.setVisible(false);
    myGUI.disposeAsync();

  }


  /**
   * Creates the ACLMessage to be sent to the <em>Ams</em> with the list of the
   * agent to be sniffed/unsniffed. The internal list of sniffed agents is also 
   * updated.
   *
   * @param agentVect vector containing TreeData item representing the agents
   * @param onFlag can be:<ul>
   *			  <li> Sniffer.SNIFF_ON  to activate sniffer on an agent/group
   *			  <li> Sniffer.SNIFF_OFF to deactivate sniffer on an agent/group
   *		         </ul>
   */
  public void sniffMsg(List agents, boolean onFlag) {

    Iterator it = agents.iterator();

    if(onFlag) {
      SniffOn so = new SniffOn();
      so.setSniffer(getAID());
      boolean empty = true;
      while(it.hasNext()) {
	Agent a = (Agent)it.next();
	AID agentID = new AID();
	agentID.setName(a.agentName + '@' + getHap());
	if(!agentsUnderSniff.contains(a)) {
	  agentsUnderSniff.add(a);
	  so.addSniffedAgents(agentID);
	  empty = false;
	}
      }
      if(!empty) {
	try {
	  Action a = new Action();
	  a.set_0(getAMS());
	  a.set_1(so);
	  List l = new ArrayList(1);
	  l.add(a);

	  fillContent(requestMsg, l);
	  addBehaviour(new AMSClientBehaviour("SniffAgentOn", requestMsg));
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
    }

    else {
      SniffOff so = new SniffOff();
      so.setSniffer(getAID());
      boolean empty = true;
      while(it.hasNext()) {
	Agent a = (Agent)it.next();
	AID agentID = new AID();
	agentID.setName(a.agentName + '@' + getHap());
	if(agentsUnderSniff.contains(a)) {
	  agentsUnderSniff.remove(a);
	  so.addSniffedAgents(agentID);
	  empty = false;
	}
      }
      if(!empty) {
	try {
	  Action a = new Action();
	  a.set_0(getAMS());
	  a.set_1(so);
	  List l = new ArrayList(1);
	  l.add(a);

	  fillContent(requestMsg, l);
	  addBehaviour(new AMSClientBehaviour("SniffAgentOff", requestMsg));
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
    }

  }

}  // End of class Sniffer
