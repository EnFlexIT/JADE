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
import jade.util.leap.*;

/**
 * Class description
 * @author Elena Quarantotto - TILAB
 */
public class TwoPh2Initiator extends Initiator implements TwoPhConstants {
    /* Data store keys */
    public final String REPLY_KEY = REPLY_K;
    public final String ALL_PROPOSALS_KEY = ALL_INITIATIONS_K;
    public final String ALL_RESPONSES_RECEIVED_KEY = "__all-responses-received" + hashCode();
    /* FSM states names */
    private static final String HANDLE_INFORM = "Handle-Inform";
    private static final String HANDLE_OLD_RESPONSE = "Handle-old-response";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    /* Unique conversation Id */
    private String conversationId = null;
    /* Data store input key */
    private String inputKey = null;
    /* Possible TwoPh2Initiator's returned values */
    private static final int OLD_RESPONSE = 0;
    private static final int ALL_RESPONSES_RECEIVED = 1;

    /**
     * Constructs a <code>TwoPh2Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase2's protocol.
     * @param inputKey Data store key where behaviour can get accept-proposal or
     * reject-proposal messages prepared in the previous phase.
     */
    public TwoPh2Initiator(Agent a, String conversationId, String inputKey) {
        this(a, conversationId, inputKey, new DataStore());
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
    public TwoPh2Initiator(Agent a, String conversationId, String inputKey, DataStore store) {
        super(a, null, store);
        this.conversationId = conversationId;
        this.inputKey = inputKey;
        /* Register the FSM transitions specific to the Two-Phase2-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerTransition(CHECK_IN_SEQ, HANDLE_OLD_RESPONSE, OLD_RESPONSE);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_OLD_RESPONSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        /* Create and register the states specific to the Two-Phase2-Commit protocol */
        Behaviour b = null;

        /* HANDLE_INFORM state activated if arrived an inform message compliant with
        conversationId and a receiver of one of accept/reject-proposal messages sent. */
        b = new OneShotBehaviour(myAgent) {
            int ret = -1;
            public void onStart() {
                System.out.println("\n\n************ HANDLE_INFORM started ************");
                super.onStart();
            }
            public void action() {
                ACLMessage inform = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleInform(inform);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_INFORM ended ************");
                return ret;
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_INFORM);

        /* HANDLE_OLD_RESPONSE state activate if arrived a failure message coming
        from phase 0 (timeout expired), a disconfirm or inform message coming from phase 1
        (timeout expired). */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_FAILURE started ************");
                super.onStart();
            }
            public void action() {
                ACLMessage old = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleOldResponse(old);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_FAILURE ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_OLD_RESPONSE);

        /* HANDLE_ALL_RESPONSES state activated when all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_ALL_RESPONSES started ************");
                super.onStart();
            }
            public void action() {
                Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_RECEIVED_KEY);
                handleAllResponses(responses);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_ALL_RESPONSES ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);

        /* DUMMY_FINAL */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ DUMMY_FINAL started ************");
                super.onStart();
            }
            public void action() {
            }
            public int onEnd() {
                System.out.println("value returned = " + getState(CHECK_SESSIONS).onEnd());
                System.out.println("************ DUMMY_FINAL ended ************");
                return getState(CHECK_SESSIONS).onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerLastState(b, DUMMY_FINAL);
    }

    /* User can override these methods */

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
     * This method is called every time a message is received, which is out-of-sequence
     * according to the protocol rules. This default implementation does nothing;
     * programmers might wish to override the method in case they need to react
     * to this event.
     * @param msg the received message
     */
    protected void handleOutOfSequence(ACLMessage msg) {
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

    /*public void registerPrepareProposals(Behaviour b) {
        registerPrepareInitiations(b);
    }*/

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
        return (Vector) getDataStore().get(inputKey);
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
        replyTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(JADE_TWO_PHASE_COMMIT),
                MessageTemplate.MatchConversationId(conversationId));
        System.out.println("initiations size = " + initiations.size());
        for(Enumeration e = initiations.elements(); e.hasMoreElements();) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg != null) {
                ACLMessage toSend = (ACLMessage) msg.clone();
                toSend.setProtocol(JADE_TWO_PHASE_COMMIT);
                toSend.setConversationId(conversationId);
                for(Iterator receivers = msg.getAllReceiver(); receivers.hasNext();) {
                    toSend.clearAllReceiver();
                    AID r = (AID) receivers.next();
                    toSend.addReceiver(r);
                    String sessionKey = "R_" + r.getName() + "_PH2";
                    toSend.setReplyWith(sessionKey);
                    /* Creates an object Session for all receivers */
                    sessions.put(sessionKey, new Session());
                    /* If initiator coincides with receiver */
                    adjustReplyTemplate(toSend);
                    myAgent.send(toSend);
                    System.out.println("---> " + toSend);
                }
            }
        }
        replyReceiver.setTemplate(replyTemplate);
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
        String sessionKey = inReplyTo.substring(0, inReplyTo.length() - 4);
        Session s = (Session) sessions.get(sessionKey);
        if(s != null) {
            int perf = reply.getPerformative();
            if(s.update(perf))
                /* The reply is compliant to the protocol */
                ret = true;
            if(s.isCompleted())
                sessions.remove(inReplyTo);
        }
        return ret;
    }

    /**
     * Check if there are still active sessions or if timeout is expired.
     * @param reply last message received
     * @return ALL_CONFIRM, ALL_CONFIRM_OR_INFORM, SOME_DISCONFIRM, PH1_TIMEOUT_EXPIRED,
     * -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
        int ret = ALL_RESPONSES_RECEIVED;
        if(reply != null) {
            if(sessions.size() > 0) {
                /* If there are still active sessions */
                ret = -1;
                System.out.println("still active sessions, ret = -1");
            }
        }
        System.out.println("checkSessions() - " + ret);
        return ret;
    }

    protected final void handlePositiveResponse(ACLMessage positiveResp) {
    }

    protected final void handleNotUnderstood(ACLMessage notUnderstood) {
    }

    protected final void handleRefuse(ACLMessage refuse) {
    }

    protected final void handleFailure(ACLMessage failure) {
    }

    /**
     * Initialize the data store.
     * @param msg Ignored
     */
    protected final void initializeDataStore(ACLMessage msg) {
        super.initializeDataStore(msg);
        getDataStore().put(ALL_RESPONSES_RECEIVED_KEY, new Vector());
    }

    public void reset(ACLMessage cfp) {
        super.reset(cfp);
    }

    /**
     * Inner class Session
     */
    class Session implements Serializable {
        /* Possible Session states */
        static final int INIT = 0;
        static final int INFORM_RECEIVED = 1;
        static final int FAILURE_RECEIVED = 2;
        static final int DISCONFIRM_RECEIVED = 2;
        /* Session state */
        private int state = INIT;

        /**
         * Return true if received ACLMessage is consistent with the protocol.
         * @param perf
         * @return Return true if received ACLMessage is consistent with the protocol
         */
        public boolean update(int perf) {
            if(state == INIT) {
                switch(perf) {
                    case ACLMessage.INFORM: state = INFORM_RECEIVED;
                    case ACLMessage.FAILURE: state = FAILURE_RECEIVED;
                    case ACLMessage.DISCONFIRM: state = DISCONFIRM_RECEIVED;
                    return true;
                    default: return false;
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
            return(state == INFORM_RECEIVED || state == FAILURE_RECEIVED ||
                    state == DISCONFIRM_RECEIVED);
        }
    }
}


