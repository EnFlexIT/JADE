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
import jade.domain.AgentManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.FipaRequestInitiatorBehaviour;

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
public class SnifferNoGui extends jade.core.Agent {

	public static final boolean SNIFF_ON = true;		//by BENNY
	public static final boolean SNIFF_OFF = false;  //by BENNY

  private ACLMessage AMSSubscription = new ACLMessage("subscribe");
  private ACLMessage AMSCancellation = new ACLMessage("cancel");
  private ACLMessage requestMsg = new ACLMessage("request");
  private Vector agentsUnderSniff = new Vector();

  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(SnifferNoGui.this, request,
	    MessageTemplate.and(MessageTemplate.MatchOntology("fipa-agent-management"),
				MessageTemplate.MatchLanguage("SL0")
				)
	    );
      actionName = an;
    }

    protected void handleNotUnderstood(ACLMessage reply) {
      // myGUI.showErrorDialog("NOT-UNDERSTOOD received by RMA during " + actionName, reply);
    }

    protected void handleRefuse(ACLMessage reply) {
      // myGUI.showError("Could not register with the AMS");
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

    AMSListenerBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage("SL");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("jade-agent-management");
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);
			
      mt1 = MessageTemplate.MatchReplyTo("RMA-subscription");
      mt2 = MessageTemplate.MatchType("inform");
      listenTemplate = MessageTemplate.and(mt1, mt2);
      listenTemplate = MessageTemplate.and(listenTemplate, mt12);

      listenSniffTemplate = MessageTemplate.MatchOntology("sniffed-message");

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
	    				// System.out.println("SNIFFER: Aggiungere un container con nome "+container);
	    				//myGUI.addContainer(container.toLowerCase());
	    			break;
	  				case AgentManagementOntology.AMSEvent.DEADCONTAINER:
	    				AgentManagementOntology.AMSContainerEvent ev2 = (AgentManagementOntology.AMSContainerEvent)amse;
	    				container = ev2.getContainerName();
	    				// System.out.println("SNIFFER: Eliminare il container con nome "+container);
	    				//myGUI.removeContainer(container.toLowerCase());
	    			break;
	  				case AgentManagementOntology.AMSEvent.NEWAGENT:
	    				AgentManagementOntology.AMSAgentEvent ev3 = (AgentManagementOntology.AMSAgentEvent)amse;
	    				container = ev3.getContainerName();
	    				amsd = ev3.getAgentDescriptor();
	    				//myGUI.addAgent(container.toLowerCase(), amsd.getName().toLowerCase(), amsd.getAddress().toLowerCase(), "fipa-agent");
	    				/*  Si può leggere lo stato dell'agente con amsd.getAPState() ma questo non serve a 
	        				molto perche' se dalla rma sospendo un agente questa operazione non viene notificata
	        				all'agente sniffer che non può quindi cambiare stato .*/
	    				// System.out.println("SNIFFER: Aggiungere un nuovo agente:");
	    				// System.out.println("Nome: "+amsd.getName());
	    				String name = amsd.getName();
	    				if(name.equalsIgnoreCase(getName())) {
	      				myContainerName = new String(container.toLowerCase());
	    				}
	    			break;
	  				case AgentManagementOntology.AMSEvent.DEADAGENT:
	    				AgentManagementOntology.AMSAgentEvent ev4 = (AgentManagementOntology.AMSAgentEvent)amse;
	    				container = ev4.getContainerName();
	    				amsd = ev4.getAgentDescriptor();
	    				// System.out.println("SNIFFER: Rimuovere un agente:");
	    				// System.out.println("Nome: "+amsd.getName());
	    				//myGUI.removeAgent(container.toLowerCase(), amsd.getName().toLowerCase());
	    			break;
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

	
	private class SniffListenerBehaviour extends CyclicBehaviour { // by BENNY

    private MessageTemplate listenSniffTemplate;

    SniffListenerBehaviour() {
      listenSniffTemplate = MessageTemplate.MatchOntology("sniffed-message");
    }
	
		public void action(){
		
      ACLMessage current = receive(listenSniffTemplate);
      if(current != null) {
      	
      	/* Viene creato un messaggio grafico inserito nella gui dove con un
      	   doppio click puo' essere visualizzato */

  			// Message msg = new Message(current);
  			// MMAbstractAction.canvasMess.recMessage(msg);

				/* Secondo le specifiche Fipa, il content di un messaggio, se e' una stringa e
				   contiene degli spazi deve essere racchiuso tra doppi apici ("). Se questo 
				   non succede il parser genera una eccezione. Il DummyAgent non fa alcun 
				   controllo su questo e quindi, spedendo un messaggio, si deve controllare 
				   personalmente di inserire i doppi apici */
				
				try {
        ACLMessage tmp = ACLMessage.fromText(new StringReader(current.getContent()));
  			Message msg = new Message(tmp);
  			MMAbstractAction.canvasMess.recMessage(msg);
				} catch (Throwable e) {
					//System.out.println("Serious problem Occurred");
					/*myGUI.showError("An error occurred parsing the incoming message.\n"+
													"          The message was lost.");*/
				}
      }else
      	block();		
		
		}
		
	}// End of SniffListenerBehaviour


  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  // private SnifferGUI myGUI = new SnifferGUI(this);

  private String myContainerName;

	private SniffListenerBehaviour myBehav = new SniffListenerBehaviour(); //by BENNY

  /**
   * ACLMessages for subscription and unsubscription as <em>rma</em> are created and
   * corresponding behaviours are set up.
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

		addBehaviour(myBehav); //by BENNY


    // Show Graphical User Interface
    // myGUI.ShowCorrect();

  }

  /**
   * Cleanup during agent shutdown. This method cleans things up when
   * <em>Sniffer</em> agent is destroyed, disconnecting from <em>AMS</em>
   * agent and closing down the Sniffer administration <em>GUI</em>.
   * Currently sniffed agents are also unsniffed to avoid errors.
   */
  public void takeDown() {
  	
  	String currentAgent;
  	String agentList = " ";
  	
  	/* Now let's create the list of the currently sniffed agents... */
  	for (int i = 0; i < agentsUnderSniff.size(); i++){
  		currentAgent = (String)agentsUnderSniff.elementAt(i);
  			agentList = agentList.concat(currentAgent+" ");  		
  	}
  	
  	/* ...and tell the ams not to sniff them anymore */
  	if (agentsUnderSniff.size() > 0)
  		sniffMsgStr(agentList.toLowerCase(),SNIFF_OFF);
  	
  	/* Now we unsubscribe from the rma list */	
    send(AMSCancellation);
    //myGUI.setVisible(false);
    //myGUI.disposeAsync();
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

	/* Mantenendo la lista degli agenti sotto sniff posso deregistrarli prima di
	   chiudere l'agente sniffer: se non faccio questo Jade continua a mandare i messaggi
	   sniffati allo sniffer anche quando questo non c'e' più fornendo quindi degli errori.*/

	String agentList = " ", agn = null;
	TreeData alextree = null;
		
		for (int i = 0; i < agentVect.size(); i++) {
			
				alextree = (TreeData)agentVect.elementAt(i);
			
				agn = alextree.getName();	
				
  			agentList = agentList.concat(agn+" ");
		
			
				if (onFlag){
			
					if (!agentsUnderSniff.contains(agn.toLowerCase()))
						agentsUnderSniff.add(agn.toLowerCase());		
				}
				else {
					if (agentsUnderSniff.contains(agn.toLowerCase()))
						agentsUnderSniff.remove(agn.toLowerCase());						
				}
	}
		
		
		sniffMsgStr(agentList,onFlag);
	}


  /*
   * This method sends a sniffer activate/disable message to the ams according to the 
   * list of agents and status flag
   */
	private void sniffMsgStr(String agentStr, boolean onFlag) { //by BENNY

	ACLMessage SniffV = new ACLMessage("request");
	SniffV.setSource(getLocalName());
	SniffV.addDest("ams");
	SniffV.setLanguage("SL0");
	SniffV.setOntology("fipa-agent-management");
	SniffV.setProtocol("fipa-request");

	if (onFlag)
		SniffV.setContent("( action ams ( sniff-agent-on ( :sniffer-name "+getLocalName()+" :agent-list { "+ agentStr.toLowerCase() +" } ) ) )");	
	else
		SniffV.setContent("( action ams ( sniff-agent-off ( :sniffer-name "+getLocalName()+" :agent-list { "+ agentStr.toLowerCase() +" } ) ) )");			
	
  send(SniffV);
  }



}