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
import jade.proto.states.*;
import jade.util.leap.*;


/**
 * This is a single homogeneous and effective implementation of the responder role in 
 * all the FIPA-Subscribe-like interaction protocols defined by FIPA,
 * that is all those protocols 
 * where the initiator sends a single "subscription" message
 * and receive notifications each time a given condition becomes true. 
 * <p>
 * NOTE that this implementation is in an experimental state and the API will 
 * possibly change in next versions also taking into account that the FIPA
 * specifications related to subscribe-like protocols are not yet stable.
 * <p>
 * @see SubscriptionInitiator
 * @author Elisabetta Cortese - TILAB
 * @author Giovanni Caire - TILAB
 */
public class SubscriptionResponder extends FSMBehaviour implements FIPAProtocolNames{
	
    /** 
     *  key to retrieve from the DataStore of the behaviour the ACLMessage 
     *	object sent by the initiator as a subscription.
     **/
    public final String SUBSCRIPTION_KEY = "__subscription" + hashCode();
    /** 
     *  key to retrieve from the DataStore of the behaviour the ACLMessage 
     *	object sent as a response to the initiator.
     **/
    public final String RESPONSE_KEY = "__response" + hashCode();

    // FSM states names
    private static final String RECEIVE_SUBSCRIPTION = "Receive-subscription";
    private static final String PREPARE_RESPONSE = "Prepare-response";
    private static final String SEND_RESPONSE = "Send-response";
    private static final String SEND_NOTIFICATIONS = "Send-notifications";

    // The MsgReceiver behaviour used to receive subscription messages
    private MsgReceiver msgRecBehaviour = null;
    
    private List notifications = new ArrayList();
    
    /**
       The <code>SubscriptionManager</code> used by this 
       <code>SubscriptionResponder</code> to register subscriptions
     */
    protected SubscriptionManager mySubscriptionManager = null;
    
    /**
       This static method can be used 
       to set the proper message Template (based on the interaction protocol 
       and the performative)
       into the constructor of this behaviour.
       @see FIPAProtocolNames
    *
    // FIXME: Commented as FIPA_SUBSCRIBE is not yet defined as a constant
    public static MessageTemplate createMessageTemplate(String iprotocol){
	
	if( CaseInsensitiveString.equalsIgnoreCase(FIPA_SUBSCRIBE,iprotocol) )
	    return MessageTemplate.and( MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE), MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE) );
	else
	    if( CaseInsensitiveString.equalsIgnoreCase(FIPA_REQUEST_WHENEVER, iprotocol) )
			return MessageTemplate.and( MessageTemplate.MatchProtocol(FIPA_REQUEST_WHENEVER),MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHENEVER), MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHENEVER)) );
	    else
			return MessageTemplate.MatchProtocol(iprotocol);
    }*/

    /**
     * Constructor of the behaviour that creates a new empty DataStore
     * @see #SubscriptionResponder(Agent a, MessageTemplate mt, DataStore store) 
     **/
    public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm){
			this(a, mt, sm, new DataStore());
    }
    
    /**
     * Constructor.
     * @param a is the reference to the Agent performing this behaviour.
     * @param mt is the MessageTemplate that must be used to match
     * subscription messages sent by the initiators. Take care that 
     * if mt is null every message is consumed by this protocol.
     * @param sm The <code>SubscriptionManager</code> object that manages
     * subscriptions. 
     * @param store the DataStore for this protocol
     **/
    public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm, DataStore store) {
			super(a);
			setDataStore(store);
			mySubscriptionManager = sm;
		
			// Register the FSM transitions
			registerDefaultTransition(RECEIVE_SUBSCRIPTION, PREPARE_RESPONSE);
			registerTransition(RECEIVE_SUBSCRIPTION, SEND_NOTIFICATIONS, MsgReceiver.INTERRUPTED);
			registerDefaultTransition(PREPARE_RESPONSE, SEND_RESPONSE);
			registerDefaultTransition(SEND_RESPONSE, RECEIVE_SUBSCRIPTION, new String[] {PREPARE_RESPONSE}); 
			registerDefaultTransition(SEND_NOTIFICATIONS, RECEIVE_SUBSCRIPTION); 
		
			//***********************************************
			// For each state create and register a behaviour	
			//***********************************************
			Behaviour b = null;
	
			// RECEIVE_SUBSCRIPTION
			msgRecBehaviour = new MsgReceiver(myAgent, mt, MsgReceiver.INFINITE, getDataStore(), SUBSCRIPTION_KEY);
			registerFirstState(msgRecBehaviour, RECEIVE_SUBSCRIPTION);
	
			// PREPARE_RESPONSE
			b = new OneShotBehaviour(myAgent) {
		
				public void action() {
			    DataStore ds = getDataStore();
			    ACLMessage subscription = (ACLMessage) ds.get(SUBSCRIPTION_KEY);
			    ACLMessage response = null;
			    try {
						response = prepareResponse(subscription); 
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
			b = new ReplySender(myAgent, RESPONSE_KEY, SUBSCRIPTION_KEY);
			b.setDataStore(getDataStore());		
			registerState(b, SEND_RESPONSE);	

			// SEND_NOTIFICATIONS 
			b = new OneShotBehaviour(myAgent) {
				public void action() {
					sendNotifications();
				}
			};
			b.setDataStore(getDataStore());		
			registerState(b, SEND_NOTIFICATIONS);	

		} // End of Constructor


    /**
       Reset this behaviour
     */
    // FIXME: reset deve resettare anche le sottoscrizioni?
    public void reset() {
			super.reset();
			DataStore ds = getDataStore();
			ds.remove(SUBSCRIPTION_KEY);
			ds.remove(RESPONSE_KEY);
    }

    /**
       This method resets the protocol and allows to change the 
       <code>MessageTemplate</code>
       that defines what messages this SubscriptionResponder 
       will react to.
     */
    public void reset(MessageTemplate mt) {
			this.reset();
			msgRecBehaviour.reset(mt, MsgReceiver.INFINITE, getDataStore(), SUBSCRIPTION_KEY);
    }
    

    /**   
     * This method is called when a subscription
     * message is received that matches the message template
     * specified in the constructor. 
     * This default implementation registers the new subscription to the 
     * <code>SubscriptionManager</code> object used by this responder and
     * returns null which has
     * the effect of sending no reponse. Programmers should
     * override the method in case they need to react to this event in a different 
     * way, e.g. by sending back an AGREE.
     * @param subscription the received message
     * @return the ACLMessage to be sent as a response (i.e. one of
     * <code>agree, refuse, not-understood</code>. <b>Remind</b> to
     * use the method createReply of the class ACLMessage in order
     * to create a good response message
     * @see jade.lang.acl.ACLMessage#createReply()
     **/
    protected ACLMessage prepareResponse(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
    	mySubscriptionManager.register(new Subscription(this, subscription));
    	return null;
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the PREPARE_RESPONSE state.
       This behaviour would override the homonymous method.
       This method also sets the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       It is responsibility of the registered behaviour to put the
       response to be sent back into the datastore at the <code>RESPONSE_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerPrepareResponse(Behaviour b) {
			registerState(b, PREPARE_RESPONSE);
			b.setDataStore(getDataStore());
    }
    
    
    /**
       This is called by a Subscription object when a notification has
       to be sent to the corresponding subscribed agent.
       Executed in mutual exclusion with sendNotifications(). Note that this
       synchronization is not needed in general, but we never know how users
       manages Subscription objects (possibly in another thread)
     */
    private synchronized void addNotification(ACLMessage notification, ACLMessage subscription) {
    	ACLMessage[] tmp = new ACLMessage[] {notification, subscription};
    	notifications.add(tmp);
    	msgRecBehaviour.interrupt();
    }
    
    /**
       This is called within the SEND_NOTIFICATIONS state.
       Executed in mutual exclusion with addNotification(). Note that this
       synchronization is not needed in general, but we never know how users
       manages Subscription objects (possibly in another thread)
     */
    private synchronized void sendNotifications() {
    	Iterator it = notifications.iterator();
    	while (it.hasNext()) {
    		ACLMessage[] tmp = (ACLMessage[]) it.next();
    		ReplySender.adjustReply(myAgent, tmp[0], tmp[1]);
    		myAgent.send(tmp[0]);
    	}
    	notifications.clear();
    }
    	
    /**
       Inner interface SubscriptionManager.
       <p>
       A <code>SubscriptionResponder</code> only deals with enforcing and
       controlling the sequence of messages in a subscription conversation, 
       while it delegates the 
       registration/deregistration of subscriptions and the creation of 
       notifications when required to a <code>SubscriptionManager</code>
       object that is passed as parameter in the constructor.
       When a new subscription message arrives, the <code>SubscriptionResponder</code>
       just calls the <code>register()</code> method of its 
       <code>SubscriptionManager</code>. The user is expected to provide 
       a class that implements the <code>register()</code> and 
       <code>deregister()</code> methods appropriately.
		   <p>
     */
    public static interface SubscriptionManager {
    	boolean register(Subscription s) throws RefuseException, NotUnderstoodException;
    	void deregister(Subscription s) throws RefuseException, NotUnderstoodException;
    } // END of inner interface SubscriptionManager

    /**
       Inner calss Subscription
       <p>
       This class represents a subscription. When a notification has to 
       be sent to a subscribed agent the notification message should not 
       be directly sent to the subscribed agent, but should be passed to the
       <code>Subscription</code> object representing the subscription of that 
       agent by means of its <code>notify()</code> method. This automatically 
       handles sequencing and protocol fields appropriately
     */
		public static class Subscription {
		
			private List notifications = new LinkedList();
			private ACLMessage subscription;
			private SubscriptionResponder myResponder;
		
			/**
			   Construct a new <code>Subscription</code> object.
			   @param r The <code>SubscriptionResponder</code> that received
			   the subscription message corresponding to this 
			   <code>Subscription</code>
			   @param s The subscription message corresponding to this 
			   <code>Subscription</code>
			 */
			public Subscription(SubscriptionResponder r, ACLMessage s){
				myResponder = r;
				subscription = s;
			}
			
			/**
			   @return the subscription message corresponding to this 
			   <code>Subscription</code>
			 */
			public ACLMessage getMessage() {
				return subscription;
			}
			
			/** 
			   This method allows sending back a notification message to the subscribed 
			   agent associated to this <code>Subscription</code> object. The user 
			   should call this method, instead of directly using the <code>send()</code>
			   method of the <code>Agent</code> class, as it automatically 
         handles sequencing and protocol fields appropriately.
       */			   
			public void notify(ACLMessage notification){
				myResponder.addNotification(notification, subscription);
			}
		} // END of inner class Subscription

}
