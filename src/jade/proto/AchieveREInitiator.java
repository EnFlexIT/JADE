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
import jade.proto.states.MsgReceiver;
import jade.util.leap.*;
import java.util.Date;

/**
 * This is a single homogeneous and effective implementation of
 * all the FIPA-Request-like interaction protocols defined by FIPA,
 * that is all those protocols where the initiator sends a single message
 * (i.e. it performs a single communicative act) within the scope
 * of an interaction protocol in order to verify if the RE (Rational
 * Effect) of the communicative act has been achieved or not.
 * This implementation works both for 1:1 and 1:N conversation.
 * <p>
 * FIPA has already specified a number of these interaction protocols, like 
 * FIPA-Request, FIPA-query, FIPA-propose, FIPA-Request-When, FIPA-recruiting,
 * FIPA-brokering, FIPA-subscribe, that allows the initiator to verify if the 
 * expected rational effect of a single communicative act has been achieved. 
 * <p>
 * The structure of these protocols is equal.
 * The initiator sends a message (in general it performs a communicative act).
 * <p>
 * The responder can then reply by sending a <code>not-understood</code>, or a
 * <code>refuse</code> to 
 * achieve the rational effect of the communicative act, or also 
 * an <code>agree</code> message to communicate the agreement to perform 
 * (possibly in the future) the communicative act.  This first category
 * of reply messages has been here identified as a response.
 * <p> The responder performs the action and, finally, must respond with an 
 * <code>inform</code> of the result of the action (eventually just that the 
 * action has been done) or with a <code>failure</code> if anything went wrong.
 * This second category of reply messages has been here identified as a
 * result notification.
 * <p> Notice that we have extended the protocol to make optional the 
 * transmission of the agree message. Infact, in most cases performing the 
 * action takes so short time that sending the agree message is just an 
 * useless and uneffective overhead; in such cases, the agree to perform the 
 * communicative act is subsumed by the reception of the following message in 
 * the protocol.
 * <p>
 * Read carefully the section of the 
 * <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
 * that describes
 * the usage of this class.
 * <p> <b>Known bugs:</b>
 * <i> The handler <code>handleAllResponses</code> is not called if the <code>
 * agree</code> message is skipped and the <code>inform</code> message
 * is received instead.
 * <br> One message for every receiver is sent instead of a single
 * message for all the receivers. </i>
 * @author Giovanni Caire - TILab
 * @author Fabio Bellifemine - TILab
 * @author Tiziana Trucco - TILab
 * @version $Date$ $Revision$
 **/
public class AchieveREInitiator extends FSMBehaviour {
	
    // Private data store keys (can't be static since if we register another instance of this class as stare of the FSM 
    //using the same data store the new values overrides the old one. 
    /** 
     * key to retrieve from the DataStore of the behaviour the ACLMessage 
     *	object passed in the constructor of the class.
     **/
    public final String REQUEST_KEY = "__request" + hashCode();
    /** 
     * key to retrieve from the DataStore of the behaviour the list of
     * ACLMessage objects that have been sent.
     **/
    public final String ALL_REQUESTS_KEY = "__all-requests" +hashCode();
    /** 
     * key to retrieve from the DataStore of the behaviour the last
     * ACLMessage object that has been received (null if the timeout
     * expired). 
     **/
    public final String REPLY_KEY = "__reply" + hashCode();
    /** 
     * key to retrieve from the DataStore of the behaviour the list of
     * ACLMessage objects that have been received as response.
     **/
    public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
    /** 
     * key to retrieve from the DataStore of the behaviour the list of
     * ACLMessage objects that have been received as result notifications.
     **/
    public final String ALL_RESULT_NOTIFICATIONS_KEY = "__all-result-notifications" +hashCode();
 
    // FSM states names
    //FIXME: prova a vedere se registrando un altra classe analoga come stato funziona tutto OK.
    private static final String PREPARE_REQUESTS = "Prepare-requests";
    private static final String SEND_REQUESTS = "Send-requests";
    private static final String RECEIVE_REPLY = "Receive-reply";
    private static final String CHECK_REPLY = "Check-reply";
    private static final String HANDLE_NOT_UNDERSTOOD = "Handle-not-understood";
    private static final String HANDLE_AGREE = "Handle-agree";
    private static final String HANDLE_REFUSE = "Handle-refuse";
    private static final String HANDLE_INFORM = "Handle-inform";
    private static final String HANDLE_FAILURE = "Handle-failure";
    private static final String HANDLE_OUT_OF_SEQ = "Handle-out-of-seq";
    private static final String CHECK_ALL_REPLIES_RECEIVED = "Check-all-replies-received";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    private static final String HANDLE_ALL_RESULT_NOTIFICATIONS = "Handle-all-result-notifications";
    private static final String DUMMY_FINAL = "Dummy-final";
	
    // States exit values
    private static final int ALL_RESPONSES_RECEIVED = 1;
    private static final int ALL_RESULT_NOTIFICATIONS_RECEIVED = 2;
	
    // Session states
    private static final int INIT = 0;
    private static final int POSITIVE_RESPONSE_RECEIVED = 1;
    private static final int NEGATIVE_RESPONSE_RECEIVED = 2;
    private static final int RESULT_NOTIFICATION_RECEIVED = 3;
		
    // This maps the AID of each responder to a Session object 
    // holding the status of the protocol as far as that responder
    // is concerned
    private Map sessions = null;	
	
    // Boolean flag indicating when all expected responses have been received
    private boolean allResponsesReceived = false;
	
    // The MsgReceiver behaviour used to receive replies 
    private MsgReceiver rec = null;
    
    private ACLMessage request;
	
	final String conversationID = "C"+Integer.toString(hashCode());
	final MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);
    
    /**
     * Construct for the class by creating a new empty DataStore
     * @see #AchieveREInitiator(Agent, ACLMessage, DataStore)
     **/
    public AchieveREInitiator(Agent a, ACLMessage msg){
	this(a,msg,new DataStore());
    }

    /**
     * Constructs a <code>AchieveREInitiator</code> behaviour
     * @param a The agent performing the protocol
     * @param msg The message that must be used to initiate the protocol.
     * Notice that the default implementation of the 
     * <code>prepareMessage</code>
     * method returns
     * a <code>List</code> including that message only.
     * @param s The <code>DataStore</code> that will be used by this 
     * <code>AchieveREInitiator</code>
     */
    public AchieveREInitiator(Agent a, ACLMessage msg, DataStore store) {
	super(a);
		
	setDataStore(store);
	//initializeDataStore(msg);
	request = msg;
	

	// Register the FSM transitions
	registerDefaultTransition(PREPARE_REQUESTS, SEND_REQUESTS);
	registerTransition(SEND_REQUESTS, DUMMY_FINAL, 0); // Exit the protocol if no request message is sent
	registerDefaultTransition(SEND_REQUESTS, RECEIVE_REPLY);
	//registerTransition(RECEIVE_REPLY, HANDLE_ALL_RESPONSES, MsgReceiver.TIMEOUT_EXPIRED); 
	registerTransition(RECEIVE_REPLY, CHECK_ALL_REPLIES_RECEIVED, MsgReceiver.TIMEOUT_EXPIRED); 
	registerDefaultTransition(RECEIVE_REPLY, CHECK_REPLY);
	registerTransition(CHECK_REPLY, HANDLE_AGREE, ACLMessage.AGREE);		
	registerTransition(CHECK_REPLY, HANDLE_REFUSE, ACLMessage.REFUSE);		
	registerTransition(CHECK_REPLY, HANDLE_NOT_UNDERSTOOD, ACLMessage.NOT_UNDERSTOOD);		
	registerTransition(CHECK_REPLY, HANDLE_INFORM, ACLMessage.INFORM);		
	registerTransition(CHECK_REPLY, HANDLE_FAILURE, ACLMessage.FAILURE);		
	registerDefaultTransition(CHECK_REPLY, HANDLE_OUT_OF_SEQ);		
	registerDefaultTransition(HANDLE_AGREE, CHECK_ALL_REPLIES_RECEIVED);
	registerDefaultTransition(HANDLE_REFUSE, CHECK_ALL_REPLIES_RECEIVED);
	registerDefaultTransition(HANDLE_NOT_UNDERSTOOD, CHECK_ALL_REPLIES_RECEIVED);
	registerDefaultTransition(HANDLE_INFORM, CHECK_ALL_REPLIES_RECEIVED);
	registerDefaultTransition(HANDLE_FAILURE, CHECK_ALL_REPLIES_RECEIVED);
	registerDefaultTransition(HANDLE_OUT_OF_SEQ, RECEIVE_REPLY);
	registerTransition(CHECK_ALL_REPLIES_RECEIVED, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
	registerTransition(CHECK_ALL_REPLIES_RECEIVED, HANDLE_ALL_RESULT_NOTIFICATIONS, ALL_RESULT_NOTIFICATIONS_RECEIVED);
	registerDefaultTransition(CHECK_ALL_REPLIES_RECEIVED, RECEIVE_REPLY);
	registerDefaultTransition(HANDLE_ALL_RESPONSES, CHECK_ALL_REPLIES_RECEIVED);
			
	// Create and register the states that make up the FSM
	Behaviour b = null;
	// PREPARE_REQUESTS
	b = new OneShotBehaviour(myAgent) {
			
		public void action() {
		    DataStore ds = getDataStore();
		    List allReq = prepareRequests((ACLMessage) ds.get(REQUEST_KEY));
		    getDataStore().put(ALL_REQUESTS_KEY, allReq);
		}
	    };
	b.setDataStore(getDataStore());		
	registerFirstState(b, PREPARE_REQUESTS);
		
	// SEND_REQUESTS
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    long currentTime = System.currentTimeMillis();
		    long minTimeout = -1;
		    long deadline = -1;

		    sessions = new HashMap();
		    
		    DataStore ds = getDataStore();
		    List allReq = (List) ds.get(ALL_REQUESTS_KEY);
		    Iterator it = allReq.iterator();
		    int cnt = 0; // counter for reply-with
		    while(it.hasNext()) {
			ACLMessage request = (ACLMessage) it.next();
			if (request != null) {
			    // Update the list of sessions on the basis of the receivers
			    // FIXME: Maybe this should take the envelope into account first
			    
			    // set the conversation-id. A single conv-id for all the messages in
			    // this protocol must be used, such that the right MessageTemplate
			    // can be later created.
				
			    request.setConversationId(conversationID);

			    ACLMessage toSend = (ACLMessage)request.clone();
			    for (Iterator receivers = request.getAllReceiver(); receivers.hasNext(); ) {
				toSend.clearAllReceiver();
				toSend.addReceiver((AID)receivers.next());
				String sessionKey = "R" + hashCode()+  "_" + Integer.toString(cnt);
				toSend.setReplyWith(sessionKey);
				sessions.put(sessionKey, new Session());
				myAgent.send(toSend);
				cnt++;
			    }
			  
			    // Update the timeout (if any) used to wait for replies according
			    // to the reply-by field
			    // get the miminum  
			    Date d = request.getReplyByDate();
			    if (d != null) {
				long timeout = d.getTime()- currentTime;
				if (timeout > 0 && (timeout < minTimeout || minTimeout <= 0)) {
				    minTimeout = timeout;
				    deadline = d.getTime();
				}
			    }
			}
		    }
		    // Finally set the MessageTemplate and timeout used in the next state 
		    // to accept replies
		    rec.set(mt,deadline,getDataStore(),REPLY_KEY);
		}	
		
		public int onEnd() {
		    // If no session is in place (no request has been sent) 
		    // the protocol will terminate
		    return sessions.size();
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, SEND_REQUESTS);
	
	// RECEIVE_REPLY
	rec = new MsgReceiver(myAgent,null,-1, getDataStore(), REPLY_KEY);
	registerState(rec, RECEIVE_REPLY);
	
	// CHECK_REPLY
	b = new OneShotBehaviour(myAgent) {
		int ret;
		
		public void action() {
		    ret = -1;
		    DataStore ds = getDataStore();
		    ACLMessage reply = (ACLMessage) ds.get(REPLY_KEY);
		    String inReplyTo = reply.getInReplyTo();
		    Session s = (Session) sessions.get(inReplyTo);
		    if (s != null) {
			int perf = reply.getPerformative();
			if (s.update(perf)) {
			    // The reply is compliant to the protocol 
			    switch (s.getState()) {
			    case POSITIVE_RESPONSE_RECEIVED:
			    case NEGATIVE_RESPONSE_RECEIVED:
				// The reply is a response
				ret = perf;
				List allRsp = (List) ds.get(ALL_RESPONSES_KEY);
				allRsp.add(reply);
				break;
			    case RESULT_NOTIFICATION_RECEIVED:
				// The reply is a resultNotification
				ret = perf;
				List allNot = (List) ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
				allNot.add(reply);
				break;
			    default:
				// Something went wrong. Just do nothing and  
				// we will go to the HANDLE_OUT_OF_SEQ state
			    }
			    // If the session is completed then remove it.
			    if (s.isCompleted()) {
				sessions.remove(inReplyTo);
			    }
			}
		    }
		}
		
		public int onEnd() {
		    return ret;
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, CHECK_REPLY);
	
	// HANDLE_AGREE
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleAgree((ACLMessage) getDataStore().get(REPLY_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_AGREE);
	
	// HANDLE_REFUSE
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleRefuse((ACLMessage) getDataStore().get(REPLY_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_REFUSE);
		
	// HANDLE_NOT_UNDERSTOOD
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleNotUnderstood((ACLMessage) getDataStore().get(REPLY_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_NOT_UNDERSTOOD);
	
	// HANDLE_INFORM
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleInform((ACLMessage) getDataStore().get(REPLY_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_INFORM);
	
	// HANDLE_FAILURE
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleFailure((ACLMessage) getDataStore().get(REPLY_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_FAILURE);
	
	// HANDLE_OUT_OF_SEQ
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleOutOfSequence((ACLMessage) getDataStore().get(REPLY_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_OUT_OF_SEQ);
	
	// CHECK_ALL_REPLIES_RECEIVED
	b = new OneShotBehaviour(myAgent) {
		int ret;
		boolean timeoutExpired = false;

		public void action() {
		    ret = -1;
		    //int perf = ((ACLMessage) (getDataStore().get(REPLY_KEY))).getPerformative();
		    ACLMessage msg1 = (ACLMessage) getDataStore().get(REPLY_KEY);
		    if (timeoutExpired) {
			if (sessions.size() == 0)
			    ret = ALL_RESULT_NOTIFICATIONS_RECEIVED;
			// if there are still sessions, then return -1
		    } else if(msg1 == null){
			//timeout was expired.
			timeoutExpired = true;
			// consider now that all the responses have been received
			allResponsesReceived = true;
			//reset the MsgReceiver state to an infinite timeout
		    rec.set(mt,-1,getDataStore(),REPLY_KEY);
			
			ret = ALL_RESPONSES_RECEIVED;
			// remove all the sessions for which no response has been received
			List sessionsToRemove = new ArrayList(sessions.size());
			for (Iterator i=sessions.keySet().iterator(); i.hasNext(); ) {
			    Object key = i.next();
			    Session s = (Session)sessions.get(key);
			    if ( s.getState() == INIT )
				  sessionsToRemove.add(key);
			}
			for (Iterator i=sessionsToRemove.iterator(); i.hasNext(); )
				sessions.remove(i.next());
			sessionsToRemove=null;  //frees memory	
		    }else{
			int perf = msg1.getPerformative();
			if (isResponse(perf) && !allResponsesReceived) {
			    // The current reply is a response.
			    // Check if all responses have been received (this is the 
			    // case when no active session is still in the INIT state).
			    allResponsesReceived = true;
			    Iterator it = sessions.values().iterator();
			    while (it.hasNext()) {
				Session s = (Session) it.next();
				if (s.getState() == INIT) {
				    allResponsesReceived = false;
				    break;
				}
			    }
			    if (allResponsesReceived) {
					//set the Msgreceiver to an infite timeout.
		          rec.set(mt,-1,getDataStore(),REPLY_KEY);
				  ret = ALL_RESPONSES_RECEIVED;
			    }
			}
			else {
			    // If the current reply is a response it has already been
			    // considered. Now check if all result notifications have
			    // been received (this is the case when there are no active
			    // sessions).
			    if (sessions.size() == 0) {
				ret = ALL_RESULT_NOTIFICATIONS_RECEIVED;
			    }
			} 
		    }		
		}
		
		public int onEnd() {
		    return ret;
		}
		public void reset() {
		    super.reset();
		    timeoutExpired=false;
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, CHECK_ALL_REPLIES_RECEIVED);
	
	// HANDLE_ALL_RESPONSES
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleAllResponses((List) getDataStore().get(ALL_RESPONSES_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerState(b, HANDLE_ALL_RESPONSES);
	
	// HANDLE_ALL_RESULT_NOTIFICATIONS
	b = new OneShotBehaviour(myAgent) {
		
		public void action() {
		    handleAllResultNotifications((List) getDataStore().get(ALL_RESULT_NOTIFICATIONS_KEY));
		}
	    };
	b.setDataStore(getDataStore());		
	registerLastState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
	
	// DUMMY_FINAL
	b = new OneShotBehaviour(myAgent) {
		public void action() {}
	    };
	registerLastState(b, DUMMY_FINAL);
    }

    /**
     * This method must return the list of ACLMessage objects to be
     * sent. It is called in the first state of this protocol.
     * This default implementation just return the ACLMessage object
     * passed in the constructor. Programmers might prefer to override
     * this method in order to return a list of objects for 1:N conversations
     * or also to prepare the messages during the execution of the behaviour.
     * @param request the ACLMessage object passed in the constructor
     * @return a List of ACLMessage objects
     **/    
    protected List prepareRequests(ACLMessage request) {
	List l = new ArrayList();
	if (request != null) {
	    l.add(request);
	}
	return l;
    }
    
    /**
     * This method is called every time an <code>agree</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param agree the received agree message
     **/
    protected void handleAgree(ACLMessage agree) {
    }

    /**
     * This method is called every time a <code>refuse</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param refuse the received refuse message
     **/
    protected void handleRefuse(ACLMessage refuse) {
    }

    /**
     * This method is called every time a <code>not-understood</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param notUnderstood the received not-understood message
     **/
    protected void handleNotUnderstood(ACLMessage notUnderstood) {
    }
    
    /**
     * This method is called every time a <code>inform</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param inform the received inform message
     **/
    protected void handleInform(ACLMessage inform) {
    }
    
    /**
     * This method is called every time a <code>failure</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param failure the received failure message
     **/
    protected void handleFailure(ACLMessage failure) {
    }
    
    /**
     * This method is called every time a 
     * message is received, which is out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param msg the received message
     **/
    protected void handleOutOfSequence(ACLMessage msg) {
    }
    
    /**
     * This method is called when all the responses have been
     * collected or when the timeout is expired.
     * The used timeout is the minimum value of the slot <code>replyBy</code> 
     * of all the sent messages. 
     * By response message we intend here all the <code>agree, not-understood,
     * refuse</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     * @param responses the List of ACLMessage object received 
     **/
    protected void handleAllResponses(List responses) {
    }
    
    /**
     * This method is called when all the result notification messages 
     * have been
     * collected. 
     * By result notification message we intend here all the <code>inform, 
     * failure</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     * @param resultNodifications the List of ACLMessage object received 
     **/
    protected void handleAllResultNotifications(List resultNotifications) {
    }
    
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the PREPARE_REQUESTS state. 
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       It is responsibility of the registered behaviour to put the
       List of ACLMessage objects to be sent 
       into the datastore at the <code>ALL_REQUESTS_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerPrepareRequests(Behaviour b) {
	registerState(b, PREPARE_REQUESTS);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_AGREE state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the <code>agree</code> ACLMessage object received
       from the datastore at the <code>REPLY_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleAgree(Behaviour b) {
	registerState(b, HANDLE_AGREE);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_REFUSE state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the <code>refuse</code> ACLMessage object received
       from the datastore at the <code>REPLY_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleRefuse(Behaviour b) {
	registerState(b, HANDLE_REFUSE);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_NOT_UNDERSTOOD state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the <code>not-understood</code> ACLMessage object received
       from the datastore at the <code>REPLY_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleNotUnderstood(Behaviour b) {
	registerState(b, HANDLE_NOT_UNDERSTOOD);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_INFORM state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the <code>inform</code> ACLMessage object received
       from the datastore at the <code>REPLY_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleInform(Behaviour b) {
	registerState(b, HANDLE_INFORM);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_FAILURE state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the <code>failure</code> ACLMessage object received
       from the datastore at the <code>REPLY_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleFailure(Behaviour b) {
	registerState(b, HANDLE_FAILURE);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_ALL_RESPONSES state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the List of ACLMessage objects, received as a response,
       from the datastore at the <code>ALL_RESPONSES_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleAllResponses(Behaviour b) {
	registerState(b, HANDLE_ALL_RESPONSES);
	b.setDataStore(getDataStore());
    }
    
    /**
       This method allows to register a user defined <code>Behaviour</code>
       in the HANDLE_ALL_RESULT_NOTIFICATIONS state.
       This behaviour would override the homonymous method.
       This method also set the 
       data store of the registered <code>Behaviour</code> to the
       DataStore of this current behaviour.
       The registered behaviour can retrieve
       the List of ACLMessage objects, received as a result notification,
       from the datastore at the <code>ALL_RESULT_NOTIFICATIONS_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleAllResultNotifications(Behaviour b) {
	registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
	b.setDataStore(getDataStore());
    }
    
    //FIXME: define a registerhandler also for OutOfSequence state
    //FIXME: call handleAgree and handleAllResponses also when
    // the INFORM/FAILURE is received before or by skipping the AGREE 
    /**
     * reset this behaviour by putting a null ACLMessage as message
     * to be sent
     **/
    public void reset(){
	reset(null);
    }

    /**
     * reset this behaviour
     * @param msg is the ACLMessage to be sent
     **/
    public void reset(ACLMessage msg){
	super.reset();
	rec.reset(null,-1, getDataStore(),REPLY_KEY);
	//initializeDataStore(msg);
	request = msg;
	allResponsesReceived = false;
    }

    /** 
       Override the onStart() method to initialize the lists that
       will keep all the replies in the data store.
     */
    public void onStart() {
    	initializeDataStore(request);
    }
    
    /** 
       Override the setDataStore() method to initialize propagate this
       setting to all children.
     */
    public void setDataStore(DataStore ds) {
    	super.setDataStore(ds);
    	Iterator it = getChildren().iterator();
    	while (it.hasNext()) {
    		Behaviour b = (Behaviour) it.next();
    		b.setDataStore(ds);
    	}
    }
    
    
    /**
       Initialize the data store. 
     **/
    private void initializeDataStore(ACLMessage msg){
	List l = new ArrayList();
	
	getDataStore().put(ALL_RESPONSES_KEY, l);
	l = new ArrayList();
	getDataStore().put(ALL_RESULT_NOTIFICATIONS_KEY, l);
	
	getDataStore().put(REQUEST_KEY, msg);

    }
    /**
       Inner class Session
    */
    class Session {
	private int state = INIT;
	/**
	   return true if the session is terminated.
	 **/
	public boolean update(int perf) {
	    switch (state) {
	    case INIT:
		switch (perf) {
		case ACLMessage.AGREE:
		    state = POSITIVE_RESPONSE_RECEIVED;
		    return true;
		case ACLMessage.REFUSE:
		case ACLMessage.NOT_UNDERSTOOD:
		    state = NEGATIVE_RESPONSE_RECEIVED;
		    return true;
		case ACLMessage.INFORM:
		case ACLMessage.FAILURE:
		    state = RESULT_NOTIFICATION_RECEIVED;
		    return true;
		default:
		    return false;
		}
	    case POSITIVE_RESPONSE_RECEIVED:
		switch (perf) {
		case ACLMessage.INFORM:
		case ACLMessage.FAILURE:
		    state = RESULT_NOTIFICATION_RECEIVED;
		    return true;
		default:
		    return false;
		}
	    default:
		return false;
	    }
	}
	
	public int getState() {
	    return state;
	}
	
	public boolean isCompleted() {
	    return (state == NEGATIVE_RESPONSE_RECEIVED || state == RESULT_NOTIFICATION_RECEIVED);
	}
    } // End of inner class Session
    
    private boolean isResponse(int perf) {
	return (perf == ACLMessage.AGREE || perf == ACLMessage.REFUSE || perf == ACLMessage.NOT_UNDERSTOOD);
    }
}
	
		
		
