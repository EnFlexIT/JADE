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

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.proto.states.MsgReceiver;

public class FIPARequestResponder extends FSMBehaviour {
	
	// Private data store keys
	public static final String REQUEST_KEY = "__request";
	public static final String RESPONSE_KEY = "__response";
	public static final String RESULT_NOTIFICATION_KEY = "__result-notification";

	// FSM states names
	public static final String RECEIVE_REQUEST = "Receive-request";
	public static final String PREPARE_RESPONSE = "Prepare-response";
	public static final String SEND_RESPONSE = "Send-response";
	public static final String PREPARE_RESULT_NOTIFICATION = "Prepare-result-notification";
	public static final String SEND_RESULT_NOTIFICATION = "Send-result-notification";
	
	// The MsgReceiver behaviour used to receive request messages
	MsgReceiver rec = null;
		
	public FIPARequestResponder(Agent a, MessageTemplate mt, DataStore store) {
		super(a);
		
		setDataStore(store);
		
		// Register the FSM transitions
		registerDefaultTransition(RECEIVE_REQUEST, PREPARE_RESPONSE);
		registerDefaultTransition(PREPARE_RESPONSE, SEND_RESPONSE);
		registerTransition(SEND_RESPONSE, PREPARE_RESULT_NOTIFICATION, ACLMessage.AGREE);
		registerDefaultTransition(SEND_RESPONSE, RECEIVE_REQUEST);
		registerDefaultTransition(PREPARE_RESULT_NOTIFICATION, SEND_RESULT_NOTIFICATION);		
		registerDefaultTransition(SEND_RESULT_NOTIFICATION, RECEIVE_REQUEST);
				
		// Create and register the states that make up the FSM
		Behaviour b = null;
		// RECEIVE_REQUEST
		// FIXME: Should adjust the MessageTemplate to match performative and interaction-protocol
		rec = new MsgReceiver(myAgent, mt, 0, REQUEST_KEY);
		rec.setDataStore(getDataStore());
		registerFirstState(rec, RECEIVE_REQUEST);
		
		// PREPARE_RESPONSE
		b = new OneShotBehaviour(myAgent) {
			
			public void action() {
				DataStore ds = getDataStore();
				ACLMessage request = (ACLMessage) ds.get(REQUEST_KEY);
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
				ds.put(RESPONSE_KEY, response);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, PREPARE_RESPONSE);
		
		// SEND_RESPONSE
		b = new OneShotBehaviour(myAgent) {
			int ret = -1;
			
			public void action() {
				DataStore ds = getDataStore();
				ACLMessage response = (ACLMessage) ds.get(RESPONSE_KEY);
				if (response != null) {
					// FIXME: Should set protocol fields not explicitly set by the user
					myAgent.send(response);
					ret = response.getPerformative();
				}
			}
			
			public int onEnd() {
				return ret;
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, SEND_RESPONSE);
		
		// PREPARE_RESULT_NOTIFICATION
		b = new OneShotBehaviour(myAgent) {
			
			public void action() {
				DataStore ds = getDataStore();
				ACLMessage request = (ACLMessage) ds.get(REQUEST_KEY);
				ACLMessage response = (ACLMessage) ds.get(RESPONSE_KEY);
				ACLMessage resNotification = null;
				try {
					resNotification = prepareResultNotification(request, response); 
				}
				catch (FailureException fe) {
					resNotification = fe.getACLMessage();
				}
				ds.put(RESULT_NOTIFICATION_KEY, resNotification);
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, PREPARE_RESULT_NOTIFICATION);
		
		// SEND_RESULT_NOTIFICATION
		b = new OneShotBehaviour(myAgent) {
			
			public void action() {
				DataStore ds = getDataStore();
				ACLMessage resNotification = (ACLMessage) ds.get(RESULT_NOTIFICATION_KEY);
				if (resNotification != null) {
					// FIXME: Should set protocol fields not explicitly set by the user
					myAgent.send(resNotification);
				}
				
				// Finally clear the private data store
				ds.clear();
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, SEND_RESULT_NOTIFICATION);
		
	}
		
	/**
	   This method allows to change the <code>MessageTemplate</code>
	   that defines what messages this FIPARequestResponder will react to
	 */
	public void setMessageTemplate(MessageTemplate mt) {
		// FIXME: Should adjust the MessageTemplate to match performative and interaction-protocol
		rec.setMessageTemplate(mt);
	}
	
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		System.out.println("prepareResponse() method not re-defined");
		return null;
	}
	
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		System.out.println("prepareResultNotification() method not re-defined");
		return null;
	}
	
	
	/**
	   This method allows to register a user defined <code>Behaviour</code>
	   in the PREPARE_RESPONSE state.
	   It is the responsibility of the user to ensure that the private
	   data store of the registerd <code>Behaviour</code> is the same
	   as that used by the whole FIPARequestResponder
	 */
	public void registerPrepareResponse(Behaviour b) {
		registerState(b, PREPARE_RESPONSE);
	}
	
	/**
	   This method allows to register a user defined <code>Behaviour</code>
	   in the PREPARE_RESULT_NOTIFICATION state.
	   It is the responsibility of the user to ensure that the private
	   data store of the registerd <code>Behaviour</code> is the same
	   as that used by the whole FIPARequestResponder
	 */
	public void registerPrepareResultNotification(Behaviour b) {
		registerState(b, PREPARE_RESULT_NOTIFICATION);
	}
}
	
		
		