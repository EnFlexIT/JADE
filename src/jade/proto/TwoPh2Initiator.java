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

//#MIDP_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import jade.util.leap.*;

/**
 * Class description
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public class TwoPh2Initiator extends Initiator {
  // Data store keys 
	// Private data store keys (can't be static since if we register another instance of this class as state of the FSM 
	// using the same data store the new values overrides the old one.
  /** 
     key to retrieve from the DataStore of the behaviour the ACLMessage 
     object passed in the constructor of the class.
   */
  public final String ACCEPTANCE_KEY = INITIATION_K;
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     ACCEPT_PROPOSAL or REJECT_PROPOSAL messages that have to be sent.
   */
  public final String ALL_ACCEPTANCES_KEY = ALL_INITIATIONS_K;
  /** 
     key to retrieve from the DataStore of the behaviour the last
     ACLMessage object that has been received (null if the timeout
     expired). 
   */
  public final String REPLY_KEY = REPLY_K;
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     all messages that have been received as response.
   */
  public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     INFORM messages that have been received as response.
   */
  public final String ALL_INFORMS_KEY = "__all-informs" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the vector of
     ACCEPT_PROPOSAL or REJECT_PROPOSAL messages for which a response 
     has not been received yet.
   */
  public final String ALL_PENDINGS_KEY = "__all-pendings" + hashCode();
    
    /* Data store keys */
    //public final String REPLY_KEY = REPLY_K;
    //public final String ALL_PROPOSALS_KEY = ALL_INITIATIONS_K;
    //public final String ALL_RESPONSES_RECEIVED_KEY = "__all-responses-received" + hashCode();
    /* FSM states names */
    private static final String HANDLE_INFORM = "Handle-Inform";
    private static final String HANDLE_OLD_RESPONSE = "Handle-old-response";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    /* Data store input key */
    //private String inputKey = null;
    /* Possible TwoPh2Initiator's returned values */
    private static final int OLD_RESPONSE = 1000;
    private static final int ALL_RESPONSES_RECEIVED = 1;

    private int totSessions;
    
    private boolean logging = true; /**@todo REMOVE IT!!!!!!!!!!! */
    private boolean currentLogging = true; /**@todo REMOVE IT!!!!!!!!!!! */

    /**
     * Constructs a <code>TwoPh2Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase2's protocol.
     * @param inputKey Data store key where behaviour can get accept-proposal or
     * reject-proposal messages prepared in the previous phase.
     */
    public TwoPh2Initiator(Agent a, ACLMessage acceptance) {
        this(a, acceptance, new DataStore());
    }

    /**
     * Constructs a <code>TwoPh2Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase2's protocol.
     * @param inputKey Data store key where behaviour can get accept-proposal or
     * reject-proposal messages prepared in the previous phase.
     * @param store <code>DataStore</code> that will be used by this <code>TwoPh2Initiator</code>.
     */
    public TwoPh2Initiator(Agent a, ACLMessage acceptance, DataStore store) {
        super(a, acceptance, store);
        //this.inputKey = inputKey;
        /* Register the FSM transitions specific to the Two-Phase2-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerTransition(CHECK_IN_SEQ, HANDLE_OLD_RESPONSE, OLD_RESPONSE);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_OLD_RESPONSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        /* Create and register the states specific to the Two-Phase2-Commit protocol */
        Behaviour b = null;

        // CHECK_IN_SEQ 
        // We must override this state to distinguish the case in which
        // a response belonging to a previous phase is received (e.g. due
        // to network delay). 
        b = new OneShotBehaviour(myAgent) {
            int ret;
            public void action() {
                ACLMessage reply = (ACLMessage) getDataStore().get(REPLY_K);
                String inReplyTo = reply.getInReplyTo();
                String phase = inReplyTo.substring(inReplyTo.length() - 3);
                if (phase.equals("PH0") || phase.equals("PH1")) {
                	// The reply belongs to a previous phase 
                	oldResponse(reply);
                	ret = OLD_RESPONSE;
                }
                else {
                	if (checkInSequence(reply)) {
                		ret = reply.getPerformative();
                	}
                	else {
                		ret = -1;
                	}
                }
                /*if(checkInSequence(reply)) {
                    String phase = reply.getInReplyTo().substring(reply.getInReplyTo().length() - 4, reply.getInReplyTo().length() - 1);
                    if((reply.getPerformative() == ACLMessage.FAILURE && phase.equals("_PH0")) ||
                       (reply.getPerformative() == ACLMessage.DISCONFIRM && phase.equals("_PH1")))
                        ret = OLD_RESPONSE;
                    else
                        ret = reply.getPerformative();
                }
                else {
                    ret = -1;
                }*/
            }

            public int onEnd() {
                return ret;
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, CHECK_IN_SEQ);

        /* HANDLE_INFORM state activated if arrived an inform message compliant with
        conversationId and a receiver of one of accept/reject-proposal messages sent. */
        b = new OneShotBehaviour(myAgent) {
            int ret = -1;
            public void action() {
                ACLMessage inform = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleInform(inform);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_INFORM);

        /* HANDLE_OLD_RESPONSE state activate if arrived a failure message coming
        from phase 0 (timeout expired), a disconfirm or inform message coming from phase 1
        (timeout expired). */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage old = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleOldResponse(old);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_OLD_RESPONSE);

        /* HANDLE_ALL_RESPONSES state activated when all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
                handleAllResponses(responses);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);

        /* DUMMY_FINAL 
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                Logger.log("(TwoPh2Initiator, TwoPh2Initiator(), DUMMY_FINAL, " + myAgent.getName() + "): " +
                        "started", logging);
                super.onStart();
            }
            public void action() {
            }
            public int onEnd() {
                Logger.log("(TwoPh2Initiator, TwoPh2Initiator(), DUMMY_FINAL, " + myAgent.getName() + "): " +
                    "value returned (ALL_RESPONSES_RECEIVED = 1) = " + getState(CHECK_SESSIONS).onEnd(), logging);
                return getState(CHECK_SESSIONS).onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerLastState(b, DUMMY_FINAL);*/
    }

	private String[] toBeReset = null;
		
  /**
   */
  protected String[] getToBeReset() {
  	if (toBeReset == null) {
			toBeReset = new String[] {
				HANDLE_INFORM,
				HANDLE_OLD_RESPONSE, 
				HANDLE_NOT_UNDERSTOOD,
				HANDLE_FAILURE,
				HANDLE_OUT_OF_SEQ
			};
  	}
  	return toBeReset;
  }
    /* User can override these methods */

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of ACCEPT_PROPOSAL or REJECT_PROPOSAL objects for 1:N 
     * conversations.
     * @param acceptance the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects. The value of the <code>reply-with</code>
     * slot is ignored and regenerated automatically by this
     * class. Instead user can specify the <code>reply-by</code> slot representing phase2
     * timeout.
     */
    protected Vector prepareAcceptances(ACLMessage acceptance) {
        Vector v = new Vector(1);
        v.addElement(acceptance);
        return v;
    }

    /**
     * This method is called every time a <code>inform</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param inform the received propose message
     */
    protected void handleInform(ACLMessage inform) {
    }

    /**
     * This method is called every time a <code>failure</code>, a <code>disconfirm</code>
     * or an <code>inform</code> message is received, which is not out-of-sequence
     * according to the protocol rules. This default implementation does nothing;
     * programmers might wish to override the method in case they need to react
     * to this event.
     * @param old the received propose message
     */
    protected void handleOldResponse(ACLMessage old) {
    }

    /**
     * This method is called when all the responses have been collected. By response
     * message we intend here all the <code>inform</code> (phase 2), <code>failure</code>
     * (phase 0), <code>disconfirm</code> (phase 1) and <code>inform</code> (phase 1)
     * received messages, which are not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might wish to override the
     * method in case they need to react to this event by analysing all the messages in
     * just one call.
     * @param responses all responses received
     */
    protected void handleAllResponses(Vector responses) {
    }

    /** This method allows to register a user-defined <code>Behaviour</code> in the
     * PREPARE_ACCEPTANCES state. This behaviour would override the homonymous method. 
     * This method also set the data store of the registered <code>Behaviour</code> to the
     * DataStore of this current behaviour. It is responsibility of the registered
     * behaviour to put the <code>Vector</code> of ACLMessage objects to be sent into
     * the datastore at the <code>ALL_ACCEPTANCES_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareProposals(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_INFORM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>inform</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleInform(Behaviour b) {
        registerState(b, HANDLE_INFORM);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_OLD_RESPONSE state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>failure, disconfirm or inform</code> ACLMessage object received
     * from the datastore at the <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleOldResponse(Behaviour b) {
        registerState(b, HANDLE_OLD_RESPONSE);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code> to
     * the DataStore of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage received from the datastore at
     * <code>ALL_RESPONSES_RECEIVED_KEY</code>.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setDataStore(getDataStore());
    }

    /* User CAN'T override these methods */

    /**
     * Returns vector of accept/reject-proposal stored in the data store at
     * key <code>inputKey</code> from previouse phase.
     * @param initiation ignored
     * @return Vector of accept/reject-proposal
     */
    protected final Vector prepareInitiations(ACLMessage initiation) {
        return prepareAcceptances(initiation);
    }

    /**
     * This method sets for all prepared accept/reject-proposal
     * <code>conversation-id</code> slot (with value passed in the constructor),
     * <code>protocol</code> slot and <code>reply-with</code> slot with a unique
     * value constructed by concatenating receiver's agent name and phase number
     * (i.e. 2). After that it sends all accept/reject-proposal.
     * @param initiations vector prepared in PREPARE_ACCEPTANCES state
     */
    protected final void sendInitiations(Vector initiations) {
      long currentTime = System.currentTimeMillis();
      long minTimeout = -1;
      long deadline = -1;
        
      Vector pendings = new Vector();
			String conversationID = createConvId(initiations);
			replyTemplate = MessageTemplate.MatchConversationId(conversationID);

		  totSessions = 0; // counter of sessions
      for(Enumeration e = initiations.elements(); e.hasMoreElements();) {
          ACLMessage msg = (ACLMessage) e.nextElement();
          if (msg != null) {
              for(Iterator receivers = msg.getAllReceiver(); receivers.hasNext();) {
	              ACLMessage toSend = (ACLMessage) msg.clone();
	              //toSend.setProtocol(JADE_TWO_PHASE_COMMIT);
	              toSend.setConversationId(conversationID);
                toSend.clearAllReceiver();
                AID r = (AID) receivers.next();
                toSend.addReceiver(r);
								String sessionKey = "R" + hashCode()+  "_" + Integer.toString(totSessions) + "_PH2";
                //String sessionKey = "R_" + r.getName() + "_PH2";
                toSend.setReplyWith(sessionKey);
                /* Creates an object Session for all receivers */
                sessions.put(sessionKey, new Session());
                /* If initiator coincides with receiver */
                adjustReplyTemplate(toSend);
                myAgent.send(toSend);
                pendings.add(toSend);
                totSessions++;
              }
              
					    // Update the timeout (if any) used to wait for replies according
					    // to the reply-by field: get the miminum.
              Date d = msg.getReplyByDate();
              if(d != null) {
                  long timeout = d.getTime() - currentTime;
                  if(timeout > 0 && (timeout < minTimeout || minTimeout <= 0)) {
                      minTimeout = timeout;
                      deadline = d.getTime();
                  }
              }
          }
      }
		  // Finally set the MessageTemplate and timeout used in the RECEIVE_REPLY 
		  // state to accept replies, and store the Vector of pending CFPs
      replyReceiver.setTemplate(replyTemplate);
      replyReceiver.setDeadline(deadline);
      getDataStore().put(ALL_PENDINGS_KEY, pendings);
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding accept/reject-proposal from vector of pendings.
     * @param reply message received
     * @return true if reply is compliant with flow of protocol, false otherwise
     */
    protected final boolean checkInSequence(ACLMessage reply) {
        boolean ret = false;
      	String inReplyTo = reply.getInReplyTo();
        /*String sessionKey = reply.getInReplyTo().substring(0, reply.getInReplyTo().length() - 4);
        Logger.log("(TwoPh2Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                "inReplyTo = " + reply.getInReplyTo() + " sessionKey = " + sessionKey, logging);

        // fix: altrimenti non avrebbe mai trovato la sessione
        //Session s = (Session) sessions.get(sessionKey);
        Logger.log("(TwoPh2Initiator, checkInSequence(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                "sessions size = " + sessions.size(), currentLogging);

        Session s = (Session) sessions.get(sessionKey + "_PH2");*/
        Session s = (Session) sessions.get(inReplyTo);
        if(s != null) {
            int perf = reply.getPerformative();
            //if(s.update(perf)) {
            if(s.update(perf)) {
                // The reply is compliant to the protocol 
                ((Vector) getDataStore().get(ALL_RESPONSES_KEY)).add(reply);
			          if (perf == ACLMessage.INFORM) {
			        		((Vector) getDataStore().get(ALL_INFORMS_KEY)).add(reply);
			          }
                updatePendings(inReplyTo);
                ret = true;
            }
            if(s.isCompleted()) {
                //sessions.remove(sessionKey + "_PH2");
                sessions.remove(inReplyTo);
            }
        }
        return ret;
    }
    
    private void updatePendings(String key) {
    	Vector pendings = (Vector) getDataStore().get(ALL_PENDINGS_KEY);
      for(int i=0; i<pendings.size(); i++) {
          ACLMessage pendingMsg = (ACLMessage) pendings.get(i);
          if(pendingMsg.getReplyWith().equals(key)) {
              pendings.remove(i);
              break;
          }
      }
    }
    	
    private void oldResponse(ACLMessage reply) {
      String inReplyTo = reply.getInReplyTo();
      String sessionKey = inReplyTo.substring(0, inReplyTo.length() - 3) + "PH2";
			int perf = reply.getPerformative();
			if (perf == ACLMessage.FAILURE || perf == ACLMessage.NOT_UNDERSTOOD || perf == ACLMessage.DISCONFIRM) {
				sessions.remove(sessionKey);
				updatePendings(sessionKey);
			}
    }

    /**
     * Check if there are still active sessions or if timeout is expired.
     * @param reply last message received
     * @return ALL_RESPONSES_RECEIVED, -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
    	if (reply == null) {
    		// Timeout expired --> clear all sessions 
    		sessions.clear();
    	}
    	if (sessions.size() == 0) {
    		// We have finished 
    		return ALL_RESPONSES_RECEIVED;
    	}
    	else {
    		// We are still waiting for some responses
    		return -1;
    	}
        /*int ret = ALL_RESPONSES_RECEIVED;
        if(reply != null) {
            if(sessions.size() > 0) {
                // If there are still active sessions
                ret = -1;
                Logger.log("(TwoPh2Initiator, checkSessions(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                        "still active sessions, ret = -1", logging);
            }
        }
        Logger.log("(TwoPh2Initiator, checkSessions(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
            "value returned (ALL_RESPONSES_RECEIVED = 1, -1) = " + ret, logging);
        return ret;*/
    }

    /**
     * Initialize the data store.
     * @param msg Ignored
     */
    protected void initializeDataStore(ACLMessage msg) {
        super.initializeDataStore(msg);
        getDataStore().put(ALL_RESPONSES_KEY, new Vector());
        getDataStore().put(ALL_INFORMS_KEY, new Vector());
    }

    public void reset(ACLMessage cfp) {
        super.reset(cfp);
    }

    /**
     * Inner class Session
     */
    class Session implements Serializable {
        // Possible Session states 
        static final int INIT = 0;
        static final int REPLY_RECEIVED = 1;

        private int state = INIT;

        /**
         * Return true if received ACLMessage is consistent with the protocol.
         * @param perf
         * @return Return true if received ACLMessage is consistent with the protocol
         */
        public boolean update(int perf) {
            if(state == INIT) {
                switch(perf) {
                    case ACLMessage.INFORM:
                    case ACLMessage.FAILURE:
                    case ACLMessage.NOT_UNDERSTOOD:
                      state = REPLY_RECEIVED;
	                    return true;
                    default: 
                    	return false;
                }
            }
            else {
                return false;
            }
        }
        /*public boolean update(ACLMessage reply) {
            String phase = reply.getInReplyTo().substring(reply.getInReplyTo().length() - 4, reply.getInReplyTo().length());
            Logger.log("\n\n(TwoPh2Initiator$Session, update(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                "phase = #" + phase + "#", currentLogging);
            if(state == INIT) {
                //switch(perf) {
                switch(reply.getPerformative()) {
                    case ACLMessage.INFORM:
                        if(!phase.equals("_PH2"))
                            return false;
                        else {
                            Logger.log("\n\n(TwoPh2Initiator$Session, update(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                                "INFORM", currentLogging);
                            state = REPLY_RECEIVED;
                            return true;
                        }
                    case ACLMessage.FAILURE:
                        if(phase.equals("_PH0")) {
                            state = REPLY_RECEIVED;
                            return true;
                        } else {
                            if(phase.equals("_PH2")) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    case ACLMessage.DISCONFIRM:
                        if(!phase.equals("_PH1"))
                            return false;
                        else {
                            Logger.log("\n\n(TwoPh2Initiator$Session, update(), " + getCurrent().getBehaviourName() + ", " + myAgent.getName() + "): " +
                                "DISCONFIRM", currentLogging);
                            state = REPLY_RECEIVED;
                            return true;
                        }
                    case ACLMessage.NOT_UNDERSTOOD:
                        return true;
                    default:
                        return false;
                }
            }
            else {
                return false;
            }
        }*/

        public int getState() {
            return state;
        }

        public boolean isCompleted() {
            return (state == REPLY_RECEIVED);
        }
    }
}


