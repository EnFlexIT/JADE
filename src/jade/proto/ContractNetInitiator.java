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
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
 * @author Giovanni Caire - TILab
 * @author Fabio Bellifemine - TILab
 * @author Tiziana Trucco - TILab
 * @version $Date$ $Revision$
 **/
public class ContractNetInitiator extends FSMBehaviour {
	
  // Private data store keys (can't be static since if we register another instance of this class as stare of the FSM 
  //using the same data store the new values overrides the old one. 
  /** 
   * key to retrieve from the DataStore of the behaviour the ACLMessage 
   *	object passed in the constructor of the class.
   **/
  public final String CFP_KEY = "__cfp" + hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the vector of
   * CFP ACLMessage objects that have to be sent.
   **/
  public final String ALL_CFPS_KEY = "__all-cfps" +hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the vector of
   * ACCEPT/REJECT_PROPOSAL ACLMessage objects that have to be sent 
   **/
  public final String ALL_ACCEPTANCES_KEY = "__all-acceptances" +hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the last
   * ACLMessage object that has been received (null if the timeout
   * expired). 
   **/
  public final String REPLY_KEY = "__reply" + hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the vector of
   * ACLMessage objects that have been received as response.
   **/
  public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
  /** 
   * key to retrieve from the DataStore of the behaviour the vector of
   * ACLMessage objects that have been received as result notifications.
   **/
  public final String ALL_RESULT_NOTIFICATIONS_KEY = "__all-result-notifications" +hashCode();
 
  // FSM states names
  private static final String PREPARE_CFPS = "Prepare-cfps";
  private static final String SEND_ALL = "Send-all";
  private static final String RECEIVE_REPLY = "Receive-reply";
  private static final String CHECK_REPLY = "Check-reply";
  private static final String HANDLE_NOT_UNDERSTOOD = "Handle-not-understood";
  private static final String HANDLE_PROPOSE = "Handle-propose";
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
  private static final int REPLY_RECEIVED = 1;
		
  // This map holds the Session objects 
  // maintaining the status of the protocol as far as each responder
  // is concerned
  private Map sessions;	
  // When step == 1 we deal with CFP and responses
  // When step == 2 we deal with ACCEPT/REJECT_PROPOSAL and result notifications
	private int step;
	
  // The MsgReceiver behaviour used to receive replies 
  private MsgReceiver rec;
  
  // The states that can be visited more than once (need to be reset)
  private String[] toBeReset;
  
  // If set to true all responses not yet received are skipped
  private boolean skipNextRespFlag;
  
  private ACLMessage cfp;
	
	final String conversationID = "C"+Integer.toString(hashCode());
	final MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);
    
  /**
   * Construct for the class by creating a new empty DataStore
   * @see #ContractNetInitiator(Agent, ACLMessage, DataStore)
   **/
  public ContractNetInitiator(Agent a, ACLMessage msg){
		this(a,msg,new DataStore());
  }

	/**
   * Constructs a <code>ContractNetInitiator</code> behaviour
   * @param a The agent performing the protocol
   * @param msg The message that must be used to initiate the protocol.
   * Notice that the default implementation of the 
   * <code>prepareCfps</code>
   * method returns
   * an array including that message only.
   * @param s The <code>DataStore</code> that will be used by this 
   * <code>ContractNetInitiator</code>
   */
  public ContractNetInitiator(Agent a, ACLMessage msg, DataStore store) {
		super(a);
		
		setDataStore(store);
		cfp = msg;
		sessions = new HashMap();
		toBeReset = new String[] {
			HANDLE_PROPOSE, 
			HANDLE_REFUSE,
			HANDLE_NOT_UNDERSTOOD,
			HANDLE_INFORM,
			HANDLE_FAILURE,
			HANDLE_OUT_OF_SEQ
		};
		step = 1;
		skipNextRespFlag = false;
		
		// Register the FSM transitions
		registerDefaultTransition(PREPARE_CFPS, SEND_ALL);
		registerTransition(SEND_ALL, DUMMY_FINAL, 0); // Exit the protocol if no CFP/ACCEPT_PROPOSAL message is sent
		registerDefaultTransition(SEND_ALL, RECEIVE_REPLY);
		registerTransition(RECEIVE_REPLY, CHECK_ALL_REPLIES_RECEIVED, MsgReceiver.TIMEOUT_EXPIRED); 
		registerDefaultTransition(RECEIVE_REPLY, CHECK_REPLY);
		registerTransition(CHECK_REPLY, HANDLE_PROPOSE, ACLMessage.PROPOSE);		
		registerTransition(CHECK_REPLY, HANDLE_REFUSE, ACLMessage.REFUSE);		
		registerTransition(CHECK_REPLY, HANDLE_NOT_UNDERSTOOD, ACLMessage.NOT_UNDERSTOOD);		
		registerTransition(CHECK_REPLY, HANDLE_INFORM, ACLMessage.INFORM);		
		registerTransition(CHECK_REPLY, HANDLE_FAILURE, ACLMessage.FAILURE);		
		registerDefaultTransition(CHECK_REPLY, HANDLE_OUT_OF_SEQ);		
		registerDefaultTransition(HANDLE_PROPOSE, CHECK_ALL_REPLIES_RECEIVED);
		registerDefaultTransition(HANDLE_REFUSE, CHECK_ALL_REPLIES_RECEIVED);
		registerDefaultTransition(HANDLE_NOT_UNDERSTOOD, CHECK_ALL_REPLIES_RECEIVED);
		registerDefaultTransition(HANDLE_INFORM, CHECK_ALL_REPLIES_RECEIVED);
		registerDefaultTransition(HANDLE_FAILURE, CHECK_ALL_REPLIES_RECEIVED);
		registerDefaultTransition(HANDLE_OUT_OF_SEQ, RECEIVE_REPLY);
		registerTransition(CHECK_ALL_REPLIES_RECEIVED, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
		registerTransition(CHECK_ALL_REPLIES_RECEIVED, HANDLE_ALL_RESULT_NOTIFICATIONS, ALL_RESULT_NOTIFICATIONS_RECEIVED);
		registerDefaultTransition(CHECK_ALL_REPLIES_RECEIVED, RECEIVE_REPLY);
		registerDefaultTransition(HANDLE_ALL_RESPONSES, SEND_ALL);
			
		// Create and register the states that make up the FSM
		Behaviour b = null;
		// PREPARE_CFPS
		b = new OneShotBehaviour(myAgent) {
			
			public void action() {
		    DataStore ds = getDataStore();
		    Vector allCfps = prepareCfps((ACLMessage) ds.get(CFP_KEY));
		    getDataStore().put(ALL_CFPS_KEY, allCfps);
			}
	  };
		b.setDataStore(getDataStore());		
		registerFirstState(b, PREPARE_CFPS);
		
		// SEND_ALL  
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
		    long currentTime = System.currentTimeMillis();
		    long minTimeout = -1;
		    long deadline = -1;

		    DataStore ds = getDataStore();
		    Vector all = (Vector) ds.get(step == 1 ? ALL_CFPS_KEY : ALL_ACCEPTANCES_KEY);
		    int cnt = 0; // counter for reply-with
		    for (Enumeration it = all.elements(); it.hasMoreElements(); ) {
					ACLMessage msg = (ACLMessage) it.nextElement();
					if (msg != null) {
			    	// Update the list of sessions on the basis of the receivers
			    	// FIXME: Maybe this should take the envelope into account first
			    
			    	// set the conversation-id. A single conv-id for all the messages in
			    	// this protocol must be used, such that the right MessageTemplate
			    	// can be later created.
			    	msg.setConversationId(conversationID);

			    	// Each message can have more than one receiver --> clone the message
			    	// to avoid ConcurrentModificationException on the list of receivers
			    	ACLMessage toSend = (ACLMessage) msg.clone();
			    	for (Iterator receivers = msg.getAllReceiver(); receivers.hasNext(); ) {
							toSend.clearAllReceiver();
							toSend.addReceiver((AID)receivers.next());
							if (step == 1 || toSend.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
								String sessionKey = "R"+Integer.toString(step)+hashCode()+"_"+ Integer.toString(cnt);
								toSend.setReplyWith(sessionKey);
								sessions.put(sessionKey, new Session(step));
								cnt++;
							}
							myAgent.send(toSend);
			    	}
			  
			    	// Update the timeout (if any) used to wait for replies according
			    	// to the reply-by field
			    	// Get the miminum (if we are in step 2 only consider ACCEPT_PROPOSALs) 
						if (step == 1 || toSend.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			    		Date d = msg.getReplyByDate();
			    		if (d != null) {
								long timeout = d.getTime()- currentTime;
								if (timeout > 0 && (timeout < minTimeout || minTimeout <= 0)) {
				    			minTimeout = timeout;
				    			deadline = d.getTime();
								}
			    		}
						}
					}
		    }
		    // Finally set the MessageTemplate and timeout used in the next state 
		    // to accept replies
		    rec.set(mt,deadline,getDataStore(),REPLY_KEY);
			}	
		
			public int onEnd() {
		    // If no session is in place (no cfp/accept_proposal has been sent) 
		    // the protocol will terminate
		    return sessions.size();
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, SEND_ALL);
	
		// RECEIVE_REPLY
		// Questo stato e` uguale al RECEIVE_REPLY di AREI
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
						ret = perf;
						Vector all = (Vector) ds.get(step == 1 ? ALL_RESPONSES_KEY : ALL_RESULT_NOTIFICATIONS_KEY);
						all.addElement(reply);
					}
					if (s.isCompleted()) {
						sessions.remove(inReplyTo);
					}
		    }
			}
		
			public int onEnd() {
		    return ret;
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, CHECK_REPLY);
	
		// HANDLE_PROPOSE
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
				ACLMessage propose = (ACLMessage) getDataStore().get(REPLY_KEY);
				Vector acceptances = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
		    handlePropose(propose, acceptances);
			}
	  };
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_PROPOSE);
	
		// HANDLE_REFUSE
		// Questo stato e` uguale al HANDLE_REFUSE di AREI
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
		    handleRefuse((ACLMessage) getDataStore().get(REPLY_KEY));
			}
	  };
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_REFUSE);
		
		// HANDLE_NOT_UNDERSTOOD
		// Questo stato e` uguale al HANDLE_NOT_UNDERSTOOD di AREI
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
		    handleNotUnderstood((ACLMessage) getDataStore().get(REPLY_KEY));
			}
	  };
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_NOT_UNDERSTOOD);
	
		// HANDLE_INFORM
		// Questo stato e` uguale al HANDLE_INFORM di AREI
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
		    handleInform((ACLMessage) getDataStore().get(REPLY_KEY));
			}
	  };
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_INFORM);
	
		// HANDLE_FAILURE
		// Questo stato e` uguale al HANDLE_FAILURE di AREI
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
		    handleFailure((ACLMessage) getDataStore().get(REPLY_KEY));
			}
	  };
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_FAILURE);
	
		// HANDLE_OUT_OF_SEQ
		// Questo stato e` uguale al HANDLE_OUT_OF_SEQ di AREI
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

			public void action() {
				if (skipNextRespFlag) {
					sessions.clear();
				}
				
		  	ACLMessage reply = (ACLMessage) getDataStore().get(REPLY_KEY);
				ret = (step == 1 ? ALL_RESPONSES_RECEIVED : ALL_RESULT_NOTIFICATIONS_RECEIVED);
		    if (reply != null) {
		    	if (sessions.size() > 0) {
		    		// If there are still active sessions we haven't received
		    		// all responses/result_notifications yet
				    ret = -1;
		  		}
		    }
		  	else {
		    	// Timeout has expired --> clear all remaining sessions
		  		sessions.clear();
		  	}
			}
		
			public int onEnd() {
		  	if (ret == ALL_RESPONSES_RECEIVED) {
		  		step = 2;
		  	}
		  	else if (ret == -1) {
		  		((FSMBehaviour) parent).resetStates(toBeReset);
		  	}
		  	return ret;
			}
		};
		b.setDataStore(getDataStore());		
		registerState(b, CHECK_ALL_REPLIES_RECEIVED);
	
		// HANDLE_ALL_RESPONSES
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
				Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
				Vector acceptances = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
		    handleAllResponses(responses, acceptances);
			}
	  };
		b.setDataStore(getDataStore());		
		registerState(b, HANDLE_ALL_RESPONSES);
	
		// HANDLE_ALL_RESULT_NOTIFICATIONS
		// Questo stato e` uguale al HANDLE_ALL_RESULT_NOTIFICATIONS di AREI
		b = new OneShotBehaviour(myAgent) {
		
			public void action() {
		    handleAllResultNotifications((Vector) getDataStore().get(ALL_RESULT_NOTIFICATIONS_KEY));
			}
		};
		b.setDataStore(getDataStore());		
		registerLastState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
	
		// DUMMY_FINAL
		// Questo stato e` uguale al DUMMY_FINAL di AREI
		b = new OneShotBehaviour(myAgent) {
			public void action() {}
		};
		registerLastState(b, DUMMY_FINAL);
	}

  /**
   * This method must return the vector of ACLMessage objects to be
   * sent. It is called in the first state of this protocol.
   * This default implementation just returns the ACLMessage object (a CFP)
   * passed in the constructor. Programmers might prefer to override
   * this method in order to return a vector of CFP objects for 1:N conversations
   * or also to prepare the messages during the execution of the behaviour.
   * @param cfp the ACLMessage object passed in the constructor
   * @return a Vector of ACLMessage objects
   **/    
  protected Vector prepareCfps(ACLMessage cfp) {
		Vector v = new Vector(1);
		v.addElement(cfp);
		return v;
  }
    
  /**
   * This method is called every time a <code>propose</code>
   * message is received, which is not out-of-sequence according
   * to the protocol rules.
   * This default implementation does nothing; programmers might
   * wish to override the method in case they need to react to this event.
   * @param propose the received propose message
   * @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
   * This list can be filled step by step redefining this method or at once
   * redefining the handleAllResponses method.
   **/
  protected void handlePropose(ACLMessage propose, Vector acceptances) {
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
   * @param responses the Vector of ACLMessage objects that have been received 
   * @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
   * This list can be filled at once redefining this method or step by step 
   * redefining the handlePropose method.
   **/
  protected void handleAllResponses(Vector responses, Vector acceptances) {
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
   * @param resultNodifications the Vector of ACLMessage object received 
   **/
  protected void handleAllResultNotifications(Vector resultNotifications) {
  }
    
    
  /**
     This method allows to register a user defined <code>Behaviour</code>
     in the PREPARE_CFPS state. 
     This behaviour would override the homonymous method.
     This method also set the 
     data store of the registered <code>Behaviour</code> to the
     DataStore of this current behaviour.
     It is responsibility of the registered behaviour to put the
     Vector of ACLMessage objects to be sent 
     into the datastore at the <code>ALL_CFPS_KEY</code>
     key.
     @param b the Behaviour that will handle this state
   */
  public void registerPrepareCfps(Behaviour b) {
		registerState(b, PREPARE_CFPS);
		b.setDataStore(getDataStore());
  }
    
  /**
     This method allows to register a user defined <code>Behaviour</code>
     in the HANDLE_PROPOSE state.
     This behaviour would override the homonymous method.
     This method also set the 
     data store of the registered <code>Behaviour</code> to the
     DataStore of this current behaviour.
     The registered behaviour can retrieve
     the <code>propose</code> ACLMessage object received
     from the datastore at the <code>REPLY_KEY</code>
     key and the <code>Vector</code> of ACCEPT/REJECT_PROPOSAL to be 
     sent back at the <code>ALL_ACCEPTANCES_KEY</code>
     @param b the Behaviour that will handle this state
   */
  public void registerHandlePropose(Behaviour b) {
		registerState(b, HANDLE_PROPOSE);
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
       the vector of ACLMessage objects, received as a response,
       from the datastore at the <code>ALL_RESPONSES_KEY</code>
       key and the <code>Vector</code> of ACCEPT/REJECT_PROPOSAL to be 
     	 sent back at the <code>ALL_ACCEPTANCES_KEY</code>
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
       the Vector of ACLMessage objects, received as a result notification,
       from the datastore at the <code>ALL_RESULT_NOTIFICATIONS_KEY</code>
       key.
       @param b the Behaviour that will handle this state
    */
    public void registerHandleAllResultNotifications(Behaviour b) {
	registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
	b.setDataStore(getDataStore());
    }
    
  /**
     This method allows to register a user defined <code>Behaviour</code>
     in the HANDLE_OUT_OF_SEQUENCE state.
     This behaviour would override the homonymous method.
     This method also set the 
     data store of the registered <code>Behaviour</code> to the
     DataStore of this current behaviour.
     The registered behaviour can retrieve
     the out of sequence ACLMessage object received
     from the datastore at the <code>REPLY_KEY</code>
     key.
     @param b the Behaviour that will handle this state
   */
  public void registerHandleOutOfSequence(Behaviour b) {
		registerState(b, HANDLE_OUT_OF_SEQ);
		b.setDataStore(getDataStore());
  }
    
  /**
     This method can be called (typically within the handlePropose() method)
     to skip all responses that have not been received yet.
   */
  public void skipNextResponses() {
  	skipNextRespFlag = true;
  }
  
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
		cfp = msg;
		step = 1;
		skipNextRespFlag = false;
  }

  /** 
     Override the onStart() method to initialize the vectors that
     will keep all the replies and acceptances in the data store.
   */
  public void onStart() {
    initializeDataStore(cfp);
  }
    
  /** 
     Override the setDataStore() method to propagate this
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
   */
  private void initializeDataStore(ACLMessage msg){
		DataStore ds = getDataStore();
		Vector l = new Vector();
		ds.put(ALL_RESPONSES_KEY, l);
		l = new Vector();
		ds.put(ALL_RESULT_NOTIFICATIONS_KEY, l);
		l = new Vector();
		ds.put(ALL_ACCEPTANCES_KEY, l);
		ds.put(CFP_KEY, msg);
  }
  
  
  /**
     Inner class Session
   */
  class Session {
		private int state = INIT;
		private int step;
		
		public Session(int step) {
			this.step = step;
		}
		
		/** Return true if received ACLMessage is consistent with the protocol */
		public boolean update(int perf) {
			if (state == INIT) {
		    state = REPLY_RECEIVED;
	    	if (step == 1) {
					switch (perf) {
					case ACLMessage.PROPOSE:
					case ACLMessage.REFUSE:
					case ACLMessage.NOT_UNDERSTOOD:
		    		return true;
					default:
		    		return false;
					}
				}
				else {
					switch (perf) {
					case ACLMessage.INFORM:
					case ACLMessage.FAILURE:
		    		return true;
					default:
		    		return false;
					}
				}
	    }
	    else {
	    	return false;
	    }
		}
					
	
		public int getState() {
	    return state;
		}
	
		public boolean isCompleted() {
	    return (state == REPLY_RECEIVED);
		}
  } // End of inner class Session
    
}
	
		
