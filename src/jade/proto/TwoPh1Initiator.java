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
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.*;

/**
 * Class description
 * @author Elena Quarantotto - TILAB
 */
public class TwoPh1Initiator extends Initiator implements TwoPhConstants {
    /* Data store keys */
    public final String REPLY_KEY = REPLY_K;
    public final String ALL_QUERYIFS_KEY = ALL_INITIATIONS_K;
    public final String ALL_CONFIRMS_KEY = "__all-confirms" + hashCode();
    public final String ALL_DISCONFIRMS_KEY = "__all-disconfirms" + hashCode();
    public final String ALL_INFORMS_KEY = "__all-informs" + hashCode();
    /* FSM states names */
    private static final String HANDLE_CONFIRM = "Handle-Confirm";
    private static final String HANDLE_DISCONFIRM = "Handle-Disconfirm";
    private static final String HANDLE_INFORM = "Handle-Inform";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    /* Unique conversation Id */
    private String conversationId = null;
    /* Data store input key */
    private String inputKey = null;
    /* Data store output key */
    private String outputKey = null;
    /* QueryIfs messages still pending (i.e. for which it doesn't still received a response */
    private Vector ph1Pendings = new Vector();

    /**
     * Constructs a <code>TwoPh1Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase1's protocol.
     * @param inputKey Data store key where behaviour can get queryIf messages
     * prepared in the previous phase.
     * @param outputKey Data store key where the behaviour prepares a vector of
     * messages which will be send by a <code>TwoPh2Initiator</code> behaviour.
     * If phase 1 ends with all confirm or inform than messages prepared are
     * <code>accept-proposal</code>, otherwise they are <code>reject-proposal</code>.
     */
    public TwoPh1Initiator(Agent a, String conversationId, String inputKey, String outputKey) {
        this(a, conversationId, inputKey, outputKey, new DataStore());
    }

    /**
     * Constructs a <code>TwoPh1Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase1's protocol.
     * @param inputKey Data store key where behaviour can get queryIf messages
     * prepared in the previous phase.
     * @param outputKey Data store key where the behaviour prepares a vector of
     * messages which will be send by a <code>TwoPh2Initiator</code> behaviour.
     * If phase 1 ends with all confirm or inform than messages prepared are
     * <code>accept-proposal</code>, otherwise they are <code>reject-proposal</code>.
     * @param store <code>DataStore</code> that will be used by this <code>TwoPh1Initiator</code>.
     */
    public TwoPh1Initiator(Agent a, String conversationId,
                           String inputKey, String outputKey, DataStore store) {
        super(a, null, store);
        this.conversationId = conversationId;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        /* Register the FSM transitions specific to the Two-Phase1-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_CONFIRM, ACLMessage.CONFIRM);
        registerTransition(CHECK_IN_SEQ, HANDLE_DISCONFIRM, ACLMessage.DISCONFIRM);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerDefaultTransition(HANDLE_CONFIRM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_DISCONFIRM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_CONFIRM);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, SOME_DISCONFIRM);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, PH1_TIMEOUT_EXPIRED);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_CONFIRM_OR_INFORM);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        /* Create and register the states specific to the Two-Phase1-Commit protocol */
        Behaviour b = null;

        /* HANDLE_CONFIRM state activated if arrived a confirm message compliant with
        conversationId and a receiver of one of queryIf messages sent. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_CONFIRM started ************");
                super.onStart();
            }
            public void action() {
                ACLMessage confirm = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleConfirm(confirm);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_CONFIRM ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_CONFIRM);

        /* HANDLE_DISCONFIRM state activated if arrived a disconfirm message
        compliant with conversationId and a receiver of one of queryIf messages
        sent. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_DISCONFIRM started ************");
                super.onStart();
            }
            public void action() {
                ACLMessage disconfirm = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleDisconfirm(disconfirm);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_DISCONFIRM ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_DISCONFIRM);

        /* HANDLE_INFORM state activated if arrived an inform message
        compliant with conversationId and a receiver of one of queryIf messages
        sent. */
        b = new OneShotBehaviour(myAgent) {
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
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_INFORM);

        /* HANDLE_ALL_RESPONSES state activated when timeout is expired or
        all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_ALL_RESPONSES started ************");
                super.onStart();
            }
            public void action() {
                Vector confirms = (Vector) getDataStore().get(ALL_CONFIRMS_KEY);
                Vector disconfirms = (Vector) getDataStore().get(ALL_DISCONFIRMS_KEY);
                Vector informs = (Vector) getDataStore().get(ALL_INFORMS_KEY);
                Vector responses = (Vector) getDataStore().get(TwoPh1Initiator.this.outputKey);
                handleAllResponses(confirms, disconfirms, informs,
                        ph1Pendings, responses);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_ALL_RESPONSES ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);

        /* DUMMY_FINAL state returns ALL_CONFIRM, ALL_CONFIRM_OR_INFORM,
        SOME_DISCONFIRM or PH1_TIMEOUT_EXPIRED code. */
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
     * This method is called every time a <code>confirm</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param confirm the received propose message
     */
    protected void handleConfirm(ACLMessage confirm) {
    }

    /**
     * This method is called every time a <code>disconfirm</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param disconfirm the received propose message
     */
    protected void handleDisconfirm(ACLMessage disconfirm) {
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
     * This method is called every time a message is received, which is out-of-sequence
     * according to the protocol rules. This default implementation does nothing;
     * programmers might wish to override the method in case they need to react
     * to this event.
     * @param msg the received message
     **/
    protected void handleOutOfSequence(ACLMessage msg) {
    }

    /**
     * This method is called when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     * <code>reply-By</code> of all the sent messages. By response message we
     * intend here all the <code>disconfirm, confirm, inform</code> received messages,
     * which are not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event by analysing all the messages in
     * just one call.
     * @param confirms all confirms received
     * @param disconfirms all disconfirms received
     * @param pendings all queryIfs still pending
     * @param responses prepared responses for next phase: <code>accept-proposal</code>
     * or <code>reject-proposal</code>
     */
    protected void handleAllResponses(Vector confirms, Vector disconfirms,
                                      Vector informs, Vector pendings, Vector responses) {
    }

    /*public void registerPrepareQueryIfs(Behaviour b) {
        registerPrepareInitiations(b);
    }*/

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_CONFIRM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>confirm</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleConfirm(Behaviour b) {
        registerState(b, HANDLE_CONFIRM);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_DISCONFIRM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>disconfirm</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleDisconfirm(Behaviour b) {
        registerState(b, HANDLE_DISCONFIRM);
        b.setDataStore(getDataStore());
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
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code> to
     * the DataStore of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage confirms, disconfirms, informs, pending and responses
     * from the datastore at <code>ALL_CONFIRMS_KEY</code>, <code>ALL_DISCONFIRMS_KEY</code>,
     * <code>ALL_INFORMS_KEY</code>, <code>ALL_PH1_PENDINGS_KEY</code> and
     * <code>output</code> field.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setDataStore(getDataStore());
    }

    /* User CAN'T override these methods */

    /**
     * Returns vector of queryif stored in the data store at key <code>inputKey</code>
     * from previouse phase.
     * @param initiation ignored
     * @return Vector of queryIfs
     */
    protected final Vector prepareInitiations(ACLMessage initiation) {
        return (Vector) getDataStore().get(inputKey);
    }

    /**
     * This method sets for all prepared queryIfs <code>conversation-id</code> slot (with
     * value passed in the constructor), <code>protocol</code> slot and
     * <code>reply-with</code> slot with a unique value constructed by concatenating
     * receiver's agent name and phase number (i.e. 1). After that it sends all cfps.
     * @param initiations vector prepared in PREPARE_QUERYIFS state
     */
    protected final void sendInitiations(Vector initiations) {
        long currentTime = System.currentTimeMillis();
        long minTimeout = -1;
        long deadline = -1;
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
                    String sessionKey = "R_" + r.getName() + "_PH1";
                    toSend.setReplyWith(sessionKey);
                    /* Creates an object Session for all receivers */
                    sessions.put(sessionKey, new Session());
                    /* If initiator coincides with receiver */
                    adjustReplyTemplate(toSend);
                    myAgent.send(toSend);
                    System.out.println("---> " + toSend);
                    ph1Pendings.add(toSend);
                }
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
        replyReceiver.setTemplate(replyTemplate);
        replyReceiver.setDeadline(deadline);
        getDataStore().put(outputKey, new Vector());
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding queryif from vector of pendings.
     * @param reply message received
     * @return true if reply is compliant with flow of protocol, false otherwise
     */
    protected final boolean checkInSequence(ACLMessage reply) {
        boolean ret = false;
        String inReplyTo = reply.getInReplyTo();
        Session s = (Session) sessions.get(inReplyTo);
        if(s != null) {
            int perf = reply.getPerformative();
            if(s.update(perf)) {
                /* The reply is compliant to the protocol */
                switch(perf) {
                    case ACLMessage.CONFIRM:
                        ((Vector) getDataStore().get(ALL_CONFIRMS_KEY)).add(reply);
                    case ACLMessage.DISCONFIRM:
                        ((Vector) getDataStore().get(ALL_DISCONFIRMS_KEY)).add(reply);
                    case ACLMessage.INFORM:
                        ((Vector) getDataStore().get(ALL_INFORMS_KEY)).add(reply);
                }
                for(int i=0; i<ph1Pendings.size(); i++) {
                    ACLMessage pendingMsg = (ACLMessage) ph1Pendings.get(i);
                    if(pendingMsg.getReplyWith().equals(reply.getInReplyTo())) {
                        ph1Pendings.remove(i);
                        break;
                    }
                }
                ret = true;
            }
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
        int ret;
        Vector responses = (Vector) getDataStore().get(outputKey);
        Vector confirms = (Vector) getDataStore().get(ALL_CONFIRMS_KEY);
        Vector disconfirms = (Vector) getDataStore().get(ALL_DISCONFIRMS_KEY);
        Vector informs = (Vector) getDataStore().get(ALL_INFORMS_KEY);
        if(reply != null) {
            if(sessions.size() > 0) {
                /* If there are still active sessions */
                ret = -1;
                System.out.println("still active sessions, ret = -1");
            } else {
                /* All responses received before timeout has been expired */
                if(disconfirms.size() != 0) {
                    /* Received some disconfirms, so prepare vector containing reject
                    messages stored in the datastore at outputKey. Content for all
                    rejects will be replaced by the user during handleAllResponse
                    method call. CheckSessions returns SOME_DISCONFIRM. */
                    for(int i=0; i<confirms.size(); i++) {
                        ACLMessage msg = (ACLMessage) confirms.get(i);
                        ACLMessage reject = (ACLMessage) msg.clone();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        responses.add(reject);
                    }
                    ret = SOME_DISCONFIRM;
                } else {
                    /* Received all confirms or informs, so prepare vector containing
                    accept-proposal messages stored in the datastore at outputKey and
                    returns ALL_CONFIRM or ALL_CONFIRM_OR_INFORM. Receivers of accept-
                    proposal messages are all confirms. Content of accept messages is
                    replaced by the user during handleAllResponses method call. */
                    for(int i=0; i<confirms.size(); i++) {
                        ACLMessage msg = (ACLMessage) confirms.get(i);
                        ACLMessage accept = (ACLMessage) msg.clone();
                        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        responses.add(accept);
                    }
                    ret = (informs.size() == 0) ? ALL_CONFIRM : ALL_CONFIRM_OR_INFORM;
                }
                getDataStore().put(outputKey, responses);
            }
        }
        else {
            /* Timeout was expired or we were interrupted, so clear all remaining
            sessions, prepare vector containing reject messages stored in the datastore
            at outputKey and returns PH1_TIMEOUT_EXPIRED. Receivers of reject messages
            are all confirms or pendings. Content of reject messages is replaced by
            the user during handleAllResponses method call. */
            sessions.clear();
            Vector confirmsAndPendings = (Vector) getDataStore().get(ALL_CONFIRMS_KEY);
            confirmsAndPendings.addAll(ph1Pendings);
            for(int i=0; i<confirmsAndPendings.size(); i++) {
                ACLMessage msg = (ACLMessage) confirmsAndPendings.get(i);
                ACLMessage reject = (ACLMessage) msg.clone();
                reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                responses.add(reject);
            }
            getDataStore().put(outputKey, responses);
            ret = PH1_TIMEOUT_EXPIRED;
            System.out.println("timeout expired, ret = TIMEOUT_EXPIRED");
        }
        System.out.println("checkSessions() - " + ret);
        return ret;
    }

    protected final void handlePositiveResponse(ACLMessage positiveResp) {
    }

    protected final void handleFailure(ACLMessage failure) {
    }

    protected final void handleNotUnderstood(ACLMessage notUnderstood) {
    }

    protected final void handleRefuse(ACLMessage refuse) {
    }

    public void reset(ACLMessage cfp) {
        super.reset(cfp);
    }

    /**
     * Initialize the data store.
     * @param msg Ignored
     */
    protected final void initializeDataStore(ACLMessage msg) {
        super.initializeDataStore(msg);
        getDataStore().put(ALL_CONFIRMS_KEY, new Vector());
        getDataStore().put(ALL_DISCONFIRMS_KEY, new Vector());
        getDataStore().put(ALL_INFORMS_KEY, new Vector());
    }

    /**
     * Inner class Session
     */
    class Session implements Serializable {
        /* Possible Session states */
        static final int INIT = 0;
        static final int CONFIRM_RECEIVED = 1;
        static final int DISCONFIRM_RECEIVED = 2;
        static final int INFORM_RECEIVED = 3;
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
                    case ACLMessage.CONFIRM: state = CONFIRM_RECEIVED;
                    case ACLMessage.DISCONFIRM: state = DISCONFIRM_RECEIVED;
                    case ACLMessage.INFORM: state = INFORM_RECEIVED;
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
            return(state == CONFIRM_RECEIVED || state == DISCONFIRM_RECEIVED ||
                    state == INFORM_RECEIVED);
        }
    }
}


