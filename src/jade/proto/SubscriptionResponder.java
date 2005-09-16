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

//#CUSTOM_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPANames;
import jade.proto.states.*;
import jade.util.leap.*;

import java.util.Hashtable;


/**
 * This is a single homogeneous and effective implementation of the responder role in 
 * all the FIPA-Subscribe-like interaction protocols defined by FIPA,
 * that is all those protocols 
 * where the initiator sends a single "subscription" message
 * and receives notifications each time a given condition becomes true. 
 * @see SubscriptionInitiator
 * @author Elisabetta Cortese - TILAB
 * @author Giovanni Caire - TILAB
 */
public class SubscriptionResponder extends FSMBehaviour implements FIPANames.InteractionProtocol {
	
    /** 
     *  key to retrieve from the DataStore of the behaviour the ACLMessage 
     *	object sent by the initiator as a subscription.
     **/
    public final String SUBSCRIPTION_KEY = "__subs_canc" + hashCode();
    /** 
     *  key to retrieve from the DataStore of the behaviour the ACLMessage 
     *	object sent by the initiator to cancel a subscription.
     **/
    public final String CANCEL_KEY = SUBSCRIPTION_KEY;
    /** 
     *  key to retrieve from the DataStore of the behaviour the ACLMessage 
     *	object sent as a response to the initiator.
     **/
    public final String RESPONSE_KEY = "__response" + hashCode();

    // FSM states names
    private static final String RECEIVE_SUBSCRIPTION = "Receive-subscription";
    private static final String PREPARE_RESPONSE = "Prepare-response";
  	private static final String HANDLE_CANCEL = "Handle-cancel";
    private static final String SEND_RESPONSE = "Send-response";
    private static final String SEND_NOTIFICATIONS = "Send-notifications";

    // The MsgReceiver behaviour used to receive subscription messages
    private MsgReceiver msgRecBehaviour = null;

 		private Hashtable subscriptions = new Hashtable();
    private List notifications = new ArrayList();
    
    /**
       The <code>SubscriptionManager</code> used by this 
       <code>SubscriptionResponder</code> to register subscriptions
     */
    protected SubscriptionManager mySubscriptionManager = null;
    
    /**
       This static method can be used 
       to set the proper message Template (based on the performative of the
       subscription message) into the constructor of this behaviour.
       @param perf The performative of the subscription message
     */
    public static MessageTemplate createMessageTemplate(int perf) {
    	return MessageTemplate.and(
    		MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE),
    		MessageTemplate.or(MessageTemplate.MatchPerformative(perf), MessageTemplate.MatchPerformative(ACLMessage.CANCEL)));
    }

    /**
     * Constructor of the behaviour that creates a new empty DataStore
     * @see #SubscriptionResponder(Agent,MessageTemplate,SubscriptionResponder.SubscriptionManager,DataStore)
	
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
	    registerTransition(RECEIVE_SUBSCRIPTION, HANDLE_CANCEL, ACLMessage.CANCEL);
			registerTransition(RECEIVE_SUBSCRIPTION, SEND_NOTIFICATIONS, MsgReceiver.INTERRUPTED);
			registerDefaultTransition(PREPARE_RESPONSE, SEND_RESPONSE);
	    registerDefaultTransition(HANDLE_CANCEL, SEND_RESPONSE);
			registerDefaultTransition(SEND_RESPONSE, RECEIVE_SUBSCRIPTION, new String[] {PREPARE_RESPONSE, HANDLE_CANCEL}); 
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

			// HANDLE_CANCEL 
			b = new OneShotBehaviour(myAgent) {
				public void action() {
			    DataStore ds = getDataStore();
			    ACLMessage cancel = (ACLMessage) ds.get(CANCEL_KEY);
			    ACLMessage response = null;
			    try {
						response = handleCancel(cancel); 
			    }
			    catch (FailureException fe) {
						response = fe.getACLMessage();
			    }
			    ds.put(RESPONSE_KEY, response);
				}
			};
			b.setDataStore(getDataStore());		
			registerState(b, HANDLE_CANCEL);	
			
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
     * The default implementation creates an new <code>Subscription</code>
     * object and registers it to the <code>SubscriptionManager</code> 
     * used by this responder. Then it returns null which has
     * the effect of sending no reponse. Programmers in general do not need
     * to override this method, but just implement the <code>register()</code>
     * method of the <code>SubscriptionManager</code> used by this
     * <code>SubscriptionResponder</code>. However they could
     * override it in case they need to react to the reception of a 
     * subscription message in a different way, e.g. by sending back an AGREE.
     * @param subscription the received message
     * @return the ACLMessage to be sent as a response (i.e. one of
     * <code>agree, refuse, not-understood</code>. <b>Remind</b> to
     * use the method createReply of the class ACLMessage in order
     * to create a good response message
     * @see jade.lang.acl.ACLMessage#createReply()
     **/
    protected ACLMessage prepareResponse(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
    	mySubscriptionManager.register(createSubscription(subscription));
    	return null;
    }
    
    /**   
     * This method is called when a CANCEL
     * message is received for a previous subscription. 
     * The default implementation retrieves the <code>Subscription</code>
     * object the received cancel message refers to and deregisters it from the 
     * <code>SubscriptionManager</code> used by this responder. Then it
     * returns null which has the effect of sending no reponse. 
     * Programmers in general do not need
     * to override this method, but just implement the <code>deregister()</code>
     * method of the <code>SubscriptionManager</code> used by this
     * <code>SubscriptionResponder</code>. However they could
     * override it in case they need to react to the reception of a 
     * cancel message in a different way, e.g. by sending back an INFORM.
     * @param cancel the received CANCEL message
     * @return the ACLMessage to be sent as a response to the 
     * cancel operation (i.e. one of
     * <code>inform</code> and <code>failure</code>. 
     */
    protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
    	Subscription s = getSubscription(cancel);
    	if (s != null) {
    		mySubscriptionManager.deregister(s);
    		s.close();
    	}
    	return null;
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the PREPARE_RESPONSE state.
       This behaviour overrides the homonymous method.
       This method also sets the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       It is responsibility of the registered behaviour to put the
       response (if any) to be sent back into the datastore at the 
       <code>RESPONSE_KEY</code> key.
       The incoming subscription message can be retrieved from the 
       datastore at the <code>SUBSCRIPTION_KEY</code> key
       @param b the Behaviour that will handle this state
    */
    public void registerPrepareResponse(Behaviour b) {
			registerState(b, PREPARE_RESPONSE);
			b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_CANCEL state.
       This behaviour overrides the homonymous method.
       This method also sets the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       It is responsibility of the registered behaviour to put the
       response (if any) to be sent back into the datastore at the 
       <code>RESPONSE_KEY</code> key.
       The incoming CANCEL message can be retrieved from the 
       datastore at the <code>CANCEL_KEY</code> key
       @param b the Behaviour that will handle this state
    */
    public void registerHandleCancel(Behaviour b) {
			registerState(b, HANDLE_CANCEL);
			b.setDataStore(getDataStore());
    }
    
    /**
       Utility method to correctly create a new <code>Subscription</code> object 
       managed by this <code>SubscriptionResponder</code>
     */
    public Subscription createSubscription(ACLMessage subsMsg) {
    	Subscription s = new Subscription(this, subsMsg);
    	String convId = subsMsg.getConversationId();
    	if (convId != null) {
    		subscriptions.put(convId, s);
    	}
    	return s;
    }
    
    /**
       Utility method to correctly retrieve the 
       <code>Subscription</code> object that is related to the conversation
       message <code>msg</code> belongs to.
     */
    public Subscription getSubscription(ACLMessage msg) {
    	Subscription s = null;
    	String convId = msg.getConversationId();
    	if (convId != null) {
    		s = (Subscription) subscriptions.get(convId);
    	}
    	return s;
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
    		boolean receiversNull = true;
    		boolean replyWithNull = true;
    		ACLMessage[] tmp = (ACLMessage[]) it.next();
    		if (tmp[0].getAllReceiver().hasNext()) {
    			receiversNull = false;
    		}
    		if (tmp[0].getReplyWith() != null) {
    			replyWithNull = false;
    		}
    		ReplySender.adjustReply(myAgent, tmp[0], tmp[1]);
    		myAgent.send(tmp[0]);
    		// If the message was modified --> restore it
    		if (receiversNull) {
    			tmp[0].clearAllReceiver();
    		}
    		if (replyWithNull) {
    			tmp[0].setReplyWith(null);
    		}
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
    	/**
    	   Register a new Subscription object
    	   @param s The Subscription object to be registered
    	   @return The boolean value returned by this method provides an 
    	   indication to the <code>SubscriptionResponder</code> about whether
    	   or not an AGREE message should be sent back to the initiator. The
    	   default implementation of the <code>prepareResponse</code> method
    	   of the <code>SubscriptionResponder</code> ignores this indication,
    	   but programmers can override it.
    	 */
    	boolean register(Subscription s) throws RefuseException, NotUnderstoodException;
    	/**
    	   Deregister a Subscription object
    	   @return The boolean value returned by this method provides an 
    	   indication to the <code>SubscriptionResponder</code> about whether
    	   or not an INFORM message should be sent back to the initiator. The
    	   default implementation of the <code>handleCancel()</code> method
    	   of the <code>SubscriptionResponder</code> ignores this indication,
    	   but programmers can override it.
    	 */
    	boolean deregister(Subscription s) throws FailureException;
    } // END of inner interface SubscriptionManager

    /**
       Inner calss Subscription
       <p>
       This class represents a subscription. When a notification has to 
       be sent to a subscribed agent the notification message should not 
       be directly sent to the subscribed agent, but should be passed to the
       <code>Subscription</code> object representing the subscription of that 
       agent by means of its <code>notify()</code> method. This automatically 
       handles sequencing and protocol fields appropriately.
       <code>Subscription</code> objects must be created by means of the
       <code>createSubscription()</code> method.
     */
		public static class Subscription {
		
			private ACLMessage subscription;
			private SubscriptionResponder myResponder;
		
			/**
			   Private constructor. The <code>createSubscription()</code>
			   must be used instead.
			   @param r The <code>SubscriptionResponder</code> that received
			   the subscription message corresponding to this 
			   <code>Subscription</code>
			   @param s The subscription message corresponding to this 
			   <code>Subscription</code>
			 */
			private Subscription(SubscriptionResponder r, ACLMessage s){
				myResponder = r;
				subscription = s;
			}
			
			/**
			   Retrieve the ACL message with which this
			   subscription object was created.
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
			
			/** 
			   This method should be called after a <code>Subscription</code> object
			   has been deregistered (typically from within the <code>deregister()</code>
			   method of the <code>SubscriptionManager</code>) and allows the 
			   <code>SubscriptionResponder</code> to release the resources allocated
			   for this subscription.
			   Not calling this method may have unexpected and undesirable side effects.
       */			   
			public void close(){
	    	String convId = subscription.getConversationId();
	    	if (convId != null) {
	    		myResponder.subscriptions.remove(convId);
	    	}
			}
		} // END of inner class Subscription

    //#MIDP_EXCLUDE_BEGIN

    // For persistence service
    private SubscriptionResponder() {
    }

    //#MIDP_EXCLUDE_END

}
