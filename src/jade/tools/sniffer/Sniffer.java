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

import java.util.Vector;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.AMSEvent;
import jade.domain.FIPAException;
import jade.proto.FipaRequestInitiatorBehaviour;
import jade.lang.sl.SL0Codec;
import java.util.*;
import jade.lang.acl.*;
import jade.onto.basic.Action;

/**
 *  This is the <em>Sniffer</em> agent.<br> 
 *  This class implements the low level part of the Sniffer, interacting with Jade 
 *  environment and with the sniffer GUI.<br>
 *  At startup, the sniffer subscribes itself as an rma to be informed every time 
 *  an agent is born or dead, a container is created or deleted.<br>
 *  For more information see <a href="../../../../intro.htm" target="_top">Introduction to the Sniffer</a>
 * 
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>(Developement) 
 * @author Gianluca Tanca (Concept & Early Version)
 * @version $Date$ $Revision$
 * 
 */
public class Sniffer extends jade.core.Agent {

  public static final boolean SNIFF_ON = true;		//by BENNY
  public static final boolean SNIFF_OFF = false;  //by BENNY

  /**
  @serial
  */
	private ACLMessage AMSSubscription = new ACLMessage(ACLMessage.SUBSCRIBE);
  /**
  @serial
  */
  private ACLMessage AMSCancellation = new ACLMessage(ACLMessage.CANCEL);
  /**
  @serial
  */
  private ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
  /**
  @serial
  */
  private Vector agentsUnderSniff = new Vector();

  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(Sniffer.this, request,
	    MessageTemplate.and(MessageTemplate.MatchOntology("fipa-agent-management"),
				MessageTemplate.MatchLanguage(SL0Codec.NAME)
				)
	    );
      actionName = an;
    }

    protected void handleNotUnderstood(ACLMessage reply) {
      // myGUI.showErrorDialog("NOT-UNDERSTOOD received by RMA during " + actionName, reply);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showError("Could not register with the AMS");
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      // myGUI.showErrorDialog("FAILURE received during " + actionName, reply);
    }

    protected void handleInform(ACLMessage reply) {
      // System.out.println("INFORM received");
    }

  }

  // Receives notifications by AMS
  private class AMSListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate listenTemplate;
		private MessageTemplate listenSniffTemplate;

    AMSListenerBehaviour(jade.core.Agent a) {
      super(a);
      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);
      mt1 = MessageTemplate.MatchInReplyTo("tool-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      listenTemplate = MessageTemplate.and(mt1, mt2);
      listenTemplate = MessageTemplate.and(listenTemplate, mt12);
      listenSniffTemplate = MessageTemplate.MatchOntology("sniffed-message");
    }

    public void action() {
      ACLMessage current = receive(listenTemplate);
      if(current != null) {
	// Handle inform messages from AMS
	//System.err.println("ListenBehaviour: "+current.toString());
      	try {
	  List l = myAgent.extractContent(current);
	  AMSEvent amse = ((EventOccurred)l.get(0)).getEvent();
	  
	  String evName = amse.getEventName();
	  
	  String container = null;
	  AID    aid = null;
	  
	  if (evName.equals(JADEAgentManagementOntology.CONTAINERBORN)) {
	    container = ((ContainerBorn)amse).getName();
	    // System.out.println("SNIFFER: Aggiungere un container con nome "+container);
	    myGUI.addContainer(container.toLowerCase());
	  } else if (evName.equals(JADEAgentManagementOntology.CONTAINERDEAD)) {
	    container = ((ContainerDead)amse).getName();
	    // System.out.println("SNIFFER: Eliminare il container con nome "+container);
	    myGUI.removeContainer(container.toLowerCase());
	  } else if (evName.equals(JADEAgentManagementOntology.AGENTBORN)) {
	    container = ((AgentBorn)amse).getContainer();
	    aid = ((AgentBorn)amse).getAgent();
	    String address = "";
	    for (Iterator i=aid.getAllAddresses(); i.hasNext(); )
	      address = address.concat((String)i.next()+" ");
	    myGUI.addAgent(container.toLowerCase(), aid.getName().toLowerCase(), address, "fipa-agent");
	    /*  Si può leggere lo stato dell'agente con amsd.getAPState() ma questo non serve a 
		molto perche' se dalla rma sospendo un agente questa operazione non viene notificata
		all'agente sniffer che non può quindi cambiare stato .*/
	    // System.out.println("SNIFFR: Aggiungere un nuovo agente:");
	    // System.out.println("Nome: "+amsd.getName());
	    if (aid.equals(getName()))
	      myContainerName = new String(container.toLowerCase());
	  } else if (evName.equals(JADEAgentManagementOntology.AGENTDEAD)) {
	    container = ((AgentDead)amse).getContainer();
	    aid = ((AgentDead)amse).getAgent();
	    // System.out.println("SNIFFER: Rimuovere un agente:");
	    // System.out.println("Nome: "+amsd.getName());
	    myGUI.removeAgent(container.toLowerCase(), aid.getName().toLowerCase());
	  }
      	} catch(FIPAException e) {
	  e.printStackTrace();
	}
	
      } // end of current != null
      else
	block();
    } // end of action

  } // End of AMSListenerBehaviour

	
    private class SniffListenerBehaviour extends CyclicBehaviour { // by BENNY

    private MessageTemplate listenSniffTemplate;

    SniffListenerBehaviour(jade.core.Agent a) {
      super(a);
      listenSniffTemplate = MessageTemplate.MatchOntology("sniffed-message");
    }
	
      public void action(){
		
	ACLMessage current = receive(listenSniffTemplate);
	if(current != null) {
	  StringACLCodec codec = new StringACLCodec();
	  try {
	    // extract the content of this message. 
	    // where the content is an ACLMessage
	    //
	    ACLMessage acl = codec.decode(current.getContent().getBytes());
	    Message msg = new Message(acl); 
	    MMAbstractAction.canvasMess.recMessage(msg);
	  } catch (ACLCodec.CodecException ce) {
	    myGUI.showError("An error occurred parsing the incoming message.\n"+
			    "          The message was lost.");
	    System.out.println("Received a wrong ACL Message");
	    ce.printStackTrace();
	  }
      }else
      	block();		
	
      } // end of action
		
    }// End of SniffListenerBehaviour



  /**
  @serial
  */
	private SnifferGUI myGUI = new SnifferGUI(this);

  /**
  @serial
  */
  private String myContainerName;

  /**
   * ACLMessages for subscription and unsubscription as <em>rma</em> are created and
   * corresponding behaviours are set up.
   */
  public void setup() {
	   // register content language and ontology
  	registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());    
    registerLanguage(SL0Codec.NAME, new SL0Codec());	

    // Fill ACL messages fields

    
    AMSSubscription.clearAllReceiver();
    AMSSubscription.addReceiver(getAMS());
    AMSSubscription.setLanguage(SL0Codec.NAME);
    AMSSubscription.setOntology(JADEAgentManagementOntology.NAME);
    AMSSubscription.setReplyWith("tool-subscription");
    AMSSubscription.setConversationId(getLocalName());

    // Please inform me whenever container list changes and send me
    // the difference between old and new container lists, complete
    // with every AMS agent descriptor
    String content = "iota ?x ( :container-list-delta ?x )";
    AMSSubscription.setContent(content);

    //AMSCancellation.setSource(getLocalName());
    AMSCancellation.clearAllReceiver();
    AMSCancellation.addReceiver(getAMS());
    AMSCancellation.setLanguage(SL0Codec.NAME);
    AMSCancellation.setOntology(JADEAgentManagementOntology.NAME);
    AMSCancellation.setReplyWith("tool-cancellation");
    AMSCancellation.setConversationId(getLocalName());

    // No content is needed (cfr. FIPA 97 Part 2 page 26)

    //requestMsg.set(getLocalName());
    requestMsg.clearAllReceiver();
    requestMsg.addReceiver(getAMS());
    requestMsg.setProtocol("fipa-request");
    requestMsg.setOntology(jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NAME);
    requestMsg.setLanguage(SL0Codec.NAME);

    // Send 'subscribe' message to the AMS
    send(AMSSubscription);
    //System.err.println(AMSSubscription.toString());

    // Handle incoming 'inform' messages
    addBehaviour(new AMSListenerBehaviour(this));

    addBehaviour(new SniffListenerBehaviour(this)); 

    // Show Graphical User Interface
    myGUI.ShowCorrect();

  }

  /**
   * Cleanup during agent shutdown. This method cleans things up when
   * <em>Sniffer</em> agent is destroyed, disconnecting from <em>AMS</em>
   * agent and closing down the Sniffer administration <em>GUI</em>.
   * Currently sniffed agents are also unsniffed to avoid errors.
   */
  public void takeDown() {
    /* tell the ams not to sniff them anymore */
    if (agentsUnderSniff.size() > 0)
      sendSniffMessage(agentsUnderSniff,SNIFF_OFF);
  	
    MMCanvas.ml.removeAllMessages();
  	
    /* Now we unsubscribe from the rma list */	
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
	 *											<li> Sniffer.SNIFF_ON  to activate sniffer on an agent/group
	 *											<li> Sniffer.SNIFF_OFF to deactivate sniffer on an agent/group
	 *											</ul>
	 */
	public void sniffMsg(Vector agentVect, boolean onFlag) { //by BENNY

	  for (int i = 0; i < agentVect.size(); i++) {
	    TreeData alextree = (TreeData)agentVect.elementAt(i);
	    if (onFlag){
	      if (!agentsUnderSniff.contains(alextree))
		agentsUnderSniff.add(alextree);		
	    } else {
	      if (agentsUnderSniff.contains(alextree)) 
		agentsUnderSniff.remove(alextree); 
	    }
	  }
	  sendSniffMessage(agentVect,onFlag);
	}


  /*
   * This method sends a sniffer activate/disable message to the ams according to the 
   * list of agents and status flag
   */
  private void sendSniffMessage(Vector agents, boolean onFlag) { //by BENNY

    ACLMessage SniffV = new ACLMessage(ACLMessage.REQUEST);
    SniffV.addReceiver(getAMS());
    SniffV.setLanguage(SL0Codec.NAME);
    SniffV.setOntology(JADEAgentManagementOntology.NAME);
    SniffV.setProtocol("fipa-request");

    Action act = new Action();
    act.setActor(getAMS());
    if (onFlag) {
      SniffOn s = new SniffOn();
      s.setSniffer(getAID());
      for (int i=0; i<agents.size(); i++)
	s.addSniffedAgents(new AID(((TreeData)agents.get(i)).getName()));
      act.setAction(s);
    } else {
      SniffOff s = new SniffOff();
      s.setSniffer(getAID());
      for (int i=0; i<agents.size(); i++)
	s.addSniffedAgents(new AID(((TreeData)agents.get(i)).getName()));
      act.setAction(s);
    }
    List l = new ArrayList(1);
    l.add(act);
    try { 
      fillContent(SniffV,l);
      send(SniffV);
    } catch (FIPAException e) {
      e.printStackTrace();  // it should never happen
    }
  }



}
