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


package jade.proto;


import jade.proto.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import jade.proto.states.*;

import jade.domain.FIPANames;

import java.util.Date;

/**
* Behaviour class for <code>fipa-contract-net</code>
* <em>Responder</em> role. This  behaviour implements the
* <code>fipa-contract-net</code> interaction protocol from the point
* of view of a responder to a call for proposal (<code>cfp</code>)
* message.<p>
* The API of this class is similar and homogeneous to the 
* <code>AchieveREResponder</code>. 
* <p>
* Read also the introduction to
* <a href="ContractNetInitiator.html">ContractNetInitiator</a>
* for a description of the protocol.
* <p>
* When a message arrives
* that matches the message template passed to the constructor,
* the callback method <code>prepareResponse</code> is executed
* that must return the wished response, for instance the <code>PROPOSE</code>
* reply message. Any other type of returned communicative act 
* is sent and then closes the
* protocol.
* <p>
* Then, if the initiator accepted the proposal, i.e. if 
* an <code>ACCEPT-PROPOSAL</code> message was received, the callback
* method <code>prepareResultNotification</code> would be executed that
* must return the message with the result notification, i.e. 
* <code>INFORM</code> or <code>FAILURE</code>.
* <br>
* In alternative, if the initiator rejected the proposal, i.e. if 
* an <code>REJECT-PROPOSAL</code> message was received, the callback
* method <code>handleRejectProposal</code> would be executed and
* the protocol terminated. 
* <p>
* If a message were received, with the same value of this 
* <code>conversation-id</code>, but that does not comply with the FIPA 
* protocol, than the method <code>handleOutOfSequence</code> would be called.
* <p>
* This class can be extended by the programmer by overriding all the needed
* handle methods or, in alternative, appropriate behaviours can be
* registered for each handle via the <code>registerHandle</code>-type
* of methods. This last case is more difficult to use and proper
* care must be taken to properly use the <code>DataStore</code> of the
* <code>Behaviour</code> as a shared memory mechanism with the
* registered behaviour.
* <p>
* Read carefully the section of the 
* <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
* that describes
* the usage of this class.
* @see jade.proto.ContractNetInitiator;
* @see jade.proto.AchieveREresponder;
* 
* @author Fabio Bellifemine - TILAB
* @author Giovanni Caire - TILAB
* @author Marco Monticone - TILAB
* @version $Date$ $Revision$
*/

public class ContractNetResponder extends FSMBehaviour {
	

//FSM states names

	private static final String RESET_STATE = "Reset_state";
	private static final String RECEIVE_CFP_STATE = "Receive-CallForProposal";
	private static final String PREPARE_PROPOSE_STATE = "Prepare-Propose";
	private static final String SEND_PROPOSE_STATE = "Send-response";
	private static final String WAIT_ACCEPTANCE_STATE = "Wait-Acceptance";
  private static final String HANDLE_REJECT_STATE ="Handle-Reject";
  private static final String PREPARE_RESULT_NOTIFICATION_STATE = "Prepare-result-notification";
  private static final String SEND_RESULT_NOTIFICATION_STATE = "Send-result-notification";
	private static final String HANDLE_OUT_OF_SEQUENCE_STATE = "Handle-Out-Of-Sequence";
// Data store keys
 /** 
 * key to retrieve from the DataStore of the behaviour the ACLMessage 
 *	object received by the initiator.
 **/
public final String CFP_KEY = "__CFP_key" + hashCode();

/** 
 * key to retrieve from the DataStore of the behaviour the ACLMessage 
 *	object sent as a response to the initiator CFP.
 **/
public final String PROPOSE_KEY = "__Propose" + hashCode();

/** 
 * key to retrieve from the DataStore of the behaviour the ACLMessage 
 *	object sent as an ACCEPT/REJECT PROPOSAL response to the initiator CFP.
 **/             
public final String PROPOSE_ACCEPTANCE_KEY = "___propose_acceptance"+hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the ACLMessage 
   *	object sent as a result notification to the initiator.
   **/
public final String RESULT_NOTIFICATION_KEY = "__result-notification" + hashCode();


private MsgReceiver cfp_rec,accept_rec;


/**
* Constructor of the behaviour that creates a new empty DataStore
* @see #ContractNetResponder(Agent a, MessageTemplate mt, DataStore store) 
**/
public ContractNetResponder(Agent a,MessageTemplate mt){
	 this(a,mt, new DataStore());
}

/**
 * Constructor of the behaviour.
 * @param a is the reference to the Agent object
 * @param mt is the MessageTemplate that must be used to match
 * the initiator message. Take care that 
 * if mt is null every message is consumed by this protocol.
 * The best practice is to have a MessageTemplate that matches
 * the protocol slot; the static method <code>createMessageTemplate</code>
 * might be usefull. 
 * @param store the DataStore for this protocol behaviour
 **/
public ContractNetResponder(Agent a,MessageTemplate mt,DataStore store){
	super(a);

	setDataStore(store); 
		  
  registerDefaultTransition(RECEIVE_CFP_STATE,PREPARE_PROPOSE_STATE);
  registerDefaultTransition(PREPARE_PROPOSE_STATE, SEND_PROPOSE_STATE);
	registerTransition(SEND_PROPOSE_STATE,WAIT_ACCEPTANCE_STATE,ACLMessage.PROPOSE);
  
	registerTransition(WAIT_ACCEPTANCE_STATE,HANDLE_REJECT_STATE,ACLMessage.REJECT_PROPOSAL);
  registerTransition(WAIT_ACCEPTANCE_STATE,HANDLE_REJECT_STATE,MsgReceiver.TIMEOUT_EXPIRED);//Time Out
  registerTransition(WAIT_ACCEPTANCE_STATE,PREPARE_RESULT_NOTIFICATION_STATE,ACLMessage.ACCEPT_PROPOSAL);
  registerDefaultTransition(WAIT_ACCEPTANCE_STATE,HANDLE_OUT_OF_SEQUENCE_STATE);

  registerDefaultTransition(PREPARE_RESULT_NOTIFICATION_STATE,SEND_RESULT_NOTIFICATION_STATE);
  
  registerDefaultTransition(SEND_PROPOSE_STATE, RESET_STATE);
  registerDefaultTransition(HANDLE_REJECT_STATE, RESET_STATE);
  registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE_STATE, RESET_STATE);
	registerDefaultTransition(SEND_RESULT_NOTIFICATION_STATE, RESET_STATE);

	Behaviour b;
	
	//reset state
	
	b=new StateResetter();
  registerState(b,RESET_STATE);
	
	// RECEIVE_CFP
	cfp_rec=new MsgReceiver(myAgent,mt,-1,getDataStore(),CFP_KEY);
	registerFirstState(cfp_rec,RECEIVE_CFP_STATE);
  
  	// PREPARE_PROPOSE
	b = new OneShotBehaviour(myAgent) {
		
	  public void action() {
		    DataStore ds = getDataStore();
		    ACLMessage request = (ACLMessage) ds.get(CFP_KEY);
		  
		    ACLMessage response = null;
		    try {
			response = prepareResponse(request); 
		    }
		    catch (NotUnderstoodException nue) {
			response = nue.getACLMessage();
		    }
		    catch (RefuseException re) {
			response = re.getACLMessage();
		    }
		    ds.put(PROPOSE_KEY, response);
		}
	    };
	registerDSState(b, PREPARE_PROPOSE_STATE);
	

	
	
	// SEND_PROPOSE
  b = new ReplySender(myAgent,PROPOSE_KEY,CFP_KEY);
	registerDSState(b, SEND_PROPOSE_STATE);
	
	
	// RECEIVE_ACCEPT
	accept_rec=new MsgReceiver(myAgent,mt,-1,getDataStore(),PROPOSE_ACCEPTANCE_KEY){
	  public void onStart(){
	        DataStore ds = getDataStore();
					ACLMessage propose=(ACLMessage)ds.get(PROPOSE_KEY);
					if(propose==null) return;
					long t_out;
					Date reply_by=propose.getReplyByDate();
					if(reply_by==null) t_out=-1;
					else t_out=reply_by.getTime();
					MessageTemplate mtemplate=MessageTemplate.and(					          			
										MessageTemplate.MatchConversationId(propose.getConversationId()),
										MessageTemplate.MatchInReplyTo(propose.getReplyWith())
									 );
																
					set(mtemplate,t_out,ds,PROPOSE_ACCEPTANCE_KEY);
	  }
	};
	registerDSState(accept_rec,WAIT_ACCEPTANCE_STATE);

	
	// PREPARE_RESULT_NOTIFICATION
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    DataStore ds = getDataStore();
		    ACLMessage cfp = (ACLMessage) ds.get(CFP_KEY);
		    ACLMessage propose = (ACLMessage) ds.get(PROPOSE_KEY);
		    ACLMessage accept = (ACLMessage) ds.get(PROPOSE_ACCEPTANCE_KEY);
		    ACLMessage resNotification = null;
		    try {
					resNotification = prepareResultNotification(cfp,propose,accept); 
		    }
		    catch (FailureException fe) {
			resNotification = fe.getACLMessage();
		    }
		    ds.put(RESULT_NOTIFICATION_KEY, resNotification);
		}
	    };
	registerDSState(b, PREPARE_RESULT_NOTIFICATION_STATE);
	
	// SEND_RESULT_NOTIFICATION
  b = new ReplySender(myAgent, RESULT_NOTIFICATION_KEY, PROPOSE_ACCEPTANCE_KEY);
	registerDSState(b,SEND_RESULT_NOTIFICATION_STATE );
	
	// Handle reject/Time out
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    DataStore ds = getDataStore();
		    ACLMessage cfp = (ACLMessage) ds.get(CFP_KEY);
		    ACLMessage propose = (ACLMessage) ds.get(PROPOSE_KEY);
		    ACLMessage reject = (ACLMessage) ds.get(PROPOSE_ACCEPTANCE_KEY);

		    handleRejectProposal(cfp,propose,reject); 
		    
		}
	    };

	registerDSState(b,HANDLE_REJECT_STATE);

	  // Handle Out of sequence in wait acceptance
	    
	    
	b = new OneShotBehaviour(myAgent) {
	
				public void action() {
		  	  DataStore ds = getDataStore();
		    	ACLMessage cfp = (ACLMessage) ds.get(CFP_KEY);
		    	ACLMessage propose = (ACLMessage) ds.get(PROPOSE_KEY);
		    	ACLMessage outMsg = (ACLMessage) ds.get(PROPOSE_ACCEPTANCE_KEY);
				    handleOutOfSequence(cfp,propose,outMsg); 
 				}
	    };
	    
	registerDSState(b,HANDLE_OUT_OF_SEQUENCE_STATE);

	
}


/**   
* This method is called after the <code>reject-propose</code> has been received
* or the timeout expires, as expressed by the <code>reply-by</code> value of
* the <code>PROPOSE</code> reply.
* This default implementation do nothing.
* Programmers should override the method in case they need to react to this event.
* After the execution of this method this responder behaviour is 
* automatically resetted. 
* @param cfp the received message
* @param propose the previously sent propose message
* @param rejectProposal the received reject-propose message, 
* null if the timeout expired
**/
protected void handleRejectProposal(ACLMessage cfp,ACLMessage propose,ACLMessage rejectProposal){
}

/**   
* This callback method is called after a message arrives
* that does not respect the 
* protocol (i.e.
* a message with the correct in-reply-to field but with a unexpected
* performative).
* This default implementation do nothing.
* Programmers should override the method in case they need to react to this event.
* After the execution of this method this responder behaviour is reset. 
* @param cfp the received message
* @param propose the previously sent propose message
* @param  outOfSequence the received message that does not respect the protocol
**/
protected void handleOutOfSequence(ACLMessage cfp,ACLMessage propose,ACLMessage outOfSequenceMsg){
}


/**   
* This callback method is called after the acceptance has been sent
* and only if the response was an <code>accept-propose</code> message. 
* This default implementation return null which has
* the effect of sending no result notification. Programmers should
* override the method in case they need to react to this event.
* @param cfp the received message
* @param propose the previously sent propose message
* @param accept the received accept-propose message
* @return the ACLMessage to be sent as a result notification (i.e. one of
* <code>inform, failure</code>. <b>Remind</b> to
* use the method createReply of the class ACLMessage in order
* to create a valid reply message
* @see jade.lang.acl.ACLMessage#createReply()
**/
protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose,ACLMessage accept ) throws FailureException {
		System.out.println("prepareResultNotification() method not re-defined");
		return null;
}
    

/**
   This method allows to register a user defined <code>Behaviour</code>
   in the HANDLE_REJECT state.
   This behaviour would override the homonymous method.
   This method also set the  data store of the registered <code>Behaviour</code> to the
   DataStore of this current behaviour.
   The registered behaviour can find the sent and receivet messages 
   within the datastore 
   at the keys: <code>CFP_KEY</code>, <code>PROPOSE_KEY</code>,<code> PROPOSE_ACCEPTANCE_KEY_KEY</code>,
   @param b the Behaviour that will handle this state
  **/
public void registerHandleRejectProposal(Behaviour b){
		registerDSState(b,HANDLE_REJECT_STATE);
}

    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the PREPARE_RESPONSE state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       It is responsibility of the registered behaviour to put the
       response to be sent into the datastore at the <code>RESPONSE_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
public void registerPrepareResponse(Behaviour b) {
	registerDSState(b, PREPARE_PROPOSE_STATE);
}

 /**
   This method allows to register a user defined <code>Behaviour</code>
   in the PREPARE_RESULT_NOTIFICATION state.
   This behaviour would override the homonymous method.
   This method also set the  data store of the registered <code>Behaviour</code> to the
   DataStore of this current behaviour.
   It is responsibility of the registered behaviour to put the
   result notification message to be sent into the datastore at the 
   <code>RESULT_NOTIFICATION_KEY</code>  key.
       @param b the Behaviour that will handle this state
  **/
public void registerPrepareResultNotification(Behaviour b) {
	registerDSState(b, PREPARE_RESULT_NOTIFICATION_STATE);
}

/**
   This method allows to register a user defined <code>Behaviour</code>
   in the HANDLE_OUT_OF_SEQUENCE state.
   This behaviour would override the homonymous method.
   This method also set the  data store of the registered <code>Behaviour</code> to the
   DataStore of this current behaviour.
   The registered behaviour can found the send and received message in the datastore 
   at the keys: <code>CFP_KEY</code>, <code>PROPOSE_KEY</code>,<code> PROPOSE_ACCEPTANCE_KEY_KEY</code>,
   @param b the Behaviour that will handle this state
  **/
public void registerHandleOutOfSequnece(Behaviour b) {
	registerDSState(b, HANDLE_OUT_OF_SEQUENCE_STATE);
}


 /**
  *  This static method can be used 
  *  to set the proper message Template (based on the interaction protocol 
  *  and the performative) to be passed to the constructor of this behaviour.
  *  @see FIPAProtocolNames 
  **/
    public static MessageTemplate createMessageTemplate(String iprotocol){

       if(CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET,iprotocol))
	         	return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET),MessageTemplate.MatchPerformative(ACLMessage.CFP));
   	 	 else
   	 	 if(CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET,iprotocol))
	    			return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),MessageTemplate.MatchPerformative(ACLMessage.CFP));
			 else
	  				return MessageTemplate.MatchProtocol(iprotocol);
    }


    /**   
     * This method is called when the initiator's
     * message is received that matches the message template
     * passed in the constructor. 
     * This default implementation return null which has
     * the effect of sending no reponse. Programmers should
     * override the method in case they need to react to this event.
     * @param request the received message
     * @return the ACLMessage to be sent as a response (i.e. one of
     * <code>PROPOSE, refuse, not-understood</code>. <b>Remind</b> to
     * use the method <code>createReply</code> of the class ACLMessage in order
     * to create a valid reply message
     * @see jade.lang.acl.ACLMessage#createReply()
     **/
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
	System.out.println("prepareResponse() method not re-defined");
	return null;
    }

    
    
  /**
   * Reset the behaviour.
   */
  public void reset() {
		super.reset();
		DataStore ds = getDataStore();
		ds.remove(CFP_KEY);
		ds.remove(PROPOSE_ACCEPTANCE_KEY);
		ds.remove(PROPOSE_KEY);
		ds.remove(RESULT_NOTIFICATION_KEY);
  }
    
  /**
   * This method reset the behaviour and 
   * allows to change the <code>MessageTemplate</code>
   * that defines what messages this ContractNetResponder will react to. 
   **/
  public void reset(MessageTemplate mt) {
		this.reset();
		cfp_rec.reset(mt, -1, getDataStore(), CFP_KEY);
		accept_rec.reset(mt, -1, getDataStore(), PROPOSE_ACCEPTANCE_KEY);
	
  }
    

private void registerDSState(Behaviour b,String name){
		b.setDataStore(getDataStore());		
		registerState(b,name);
}

}
