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
public class TwoPh0Initiator extends Initiator implements TwoPhConstants {
    /* Data store keys */
    public final String REPLY_KEY = REPLY_K;
    public final String ALL_CFPS_KEY = ALL_INITIATIONS_K;
    public final String ALL_PROPOSES_KEY = "__all-proposes" + hashCode();
    public final String ALL_FAILURES_KEY = "__all-failures" + hashCode();
    /* FSM states names */
    private static final String HANDLE_PROPOSE = "Handle-Propose";
    private static final String HANDLE_FAILURE = "Handle-Failure";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    /* Unique conversation Id */
    private String conversationId = null;
    /* Cfps messages still pending (i.e. for which it doesn't still received a response */
    private Vector ph0Pendings = new Vector();
    /* Data store output key */
    private String outputKey = null;

    /**
     * Constructs a <code>TwoPh0Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param cfp The message that must be used to initiate the protocol.
     * Notice that the default implementation of the <code>prepareCfps</code> method
     * returns an array composed of that message only.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase0's protocol.
     * @param outputKey Data store key where the behaviour prepares a vector of
     * messages which will be send by a <code>TwoPh1Initiator</code> behaviour
     * (if phase0 ends with all receivers proposing) or <code>TwoPh2Initiator</code>
     * behaviour (if phase0 ends with some failure or timeout expired).
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp,
                           String conversationId, String outputKey) {
        this(a, cfp, conversationId, outputKey, new DataStore());
    }

    /**
     * Constructs a <code>TwoPh0Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param cfp The message that must be used to initiate the protocol.
     * Notice that the default implementation of the <code>prepareCfps</code> method
     * returns an array composed of that message only.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase0's protocol.
     * @param outputKey Data store key where the behaviour prepares a vector of
     * messages which will be send by a <code>TwoPh1Initiator</code> behaviour
     * (if phase0 ends with all receivers proposing) or <code>TwoPh2Initiator</code>
     * behaviour (if phase0 ends with some failure or timeout expired).
     * @param store <code>DataStore</code> that will be used by this <code>TwoPh0Initiator</code>.
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp, String conversationId,
                           String outputKey, DataStore store) {
        super(a, cfp, store);
        this.conversationId = conversationId;
        this.outputKey = outputKey;
        /* Register the FSM transitions specific to the Two-Phase0-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerTransition(CHECK_IN_SEQ, HANDLE_FAILURE, ACLMessage.FAILURE);
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_FAILURE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_PROPOSE);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, SOME_FAILURE);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, PH0_TIMEOUT_EXPIRED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        /* Create and register the states specific to the Two-Phase0-Commit protocol */
        Behaviour b = null;

        /* HANDLE_PROPOSE state activated if it is arrived a propose message
        compliant with conversationId and some receiver of a cfp message. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_PROPOSE started ************");
                super.onStart();
            }
            public void action() {
                ACLMessage propose = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handlePropose(propose);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_PROPOSE ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_PROPOSE);

        /* HANDLE_FAILURE state activated if it is arrived a failure message
        compliant with conversationId and some receiver of a cfp message. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_FAILURE started ************");
                super.onStart();
            }
            public void action() {
                ACLMessage failure = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleFailure(failure);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_FAILURE ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_FAILURE);

        /* HANDLE_ALL_RESPONSES state activated when timeout is expired or
        all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_ALL_RESPONSES started ************");
                super.onStart();
            }
            public void action() {
                Vector proposes = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
                Vector failures = (Vector) getDataStore().get(ALL_FAILURES_KEY);
                Vector responses = (Vector) getDataStore().get(TwoPh0Initiator.this.outputKey);
                handleAllResponses(proposes, failures, ph0Pendings, responses);
            }
            public int onEnd() {
                System.out.println("************ HANDLE_ALL_RESPONSES ended ************");
                return super.onEnd();
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);

        /* DUMMY_FINAL state returns ALL_PROPOSE, SOME_FAILURE or
        PH0_TIMEOUT_EXPIRED code. */
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
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object (a CFP) passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of CFP objects for 1:N conversations.
     * @param cfp the ACLMessage object passed in the constructor
     * @return a vector of ACLMessage objects. The values of the slot <code>reply-with</code>
     * and <code>conversation-id</code> are ignored and regenerated automatically by this
     * class. Instead user can specify <code>reply-by</code> slot representing phase0
     * timeout.
     */
    protected Vector prepareCfps(ACLMessage cfp) {
        Vector v = new Vector(1);
        v.addElement(cfp);
        return v;
    }

    /**
     * This method is called every time a <code>propose</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param propose the received propose message
     */
    protected void handlePropose(ACLMessage propose) {
    }

    /**
     * This method is called every time a <code>failure</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param failure the received propose message
     */
    protected void handleFailure(ACLMessage failure) {
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
     * This method is called when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     * <code>reply-By</code> of all the sent messages. By response message we
     * intend here all the <code>propose, failure</code> received messages, which
     * are not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     * @param proposes all proposes received
     * @param failures all failures received
     * @param pendings all cfps still pending
     * @param responses prepared responses for next phase: <code>queryIfs</code> for phase 1 or
     * <code>reject-proposal</code> for phase 2
     */
    protected void handleAllResponses(Vector proposes, Vector failures,
                                      Vector pendings, Vector responses) {
    }

    /** This method allows to register a user-defined <code>Behaviour</code> in the
     * PREPARE_CFPS state. This behaviour would override the homonymous method. This
     * method also set the data store of the registered <code>Behaviour</code> to the
     * DataStore of this current behaviour. It is responsibility of the registered
     * behaviour to put the <code>Vector</code> of ACLMessage objects to be sent into
     * the datastore at the <code>ALL_CFPS_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareCfps(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /** This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_PROPOSE state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>propose</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandlePropose(Behaviour b) {
        registerState(b, HANDLE_PROPOSE);
        b.setDataStore(getDataStore());
    }

    /** This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_FAILURE state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>failure</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleFailure(Behaviour b) {
        registerState(b, HANDLE_FAILURE);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code> to
     * the DataStore of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage proposes, failures, pending and responses from the
     * datastore at <code>ALL_PROPOSES_KEY</code>, <code>ALL_FAILURES_KEY</code>,
     * <code>ALL_PH0_PENDINGS_KEY</code> and <code>output</code> field.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setDataStore(getDataStore());
    }

    /* User CAN'T override these methods */

    /**
     * Prepare vector containing cfps.
     * @param initiation cfp passed in the constructor
     * @return Vector of cfps
     */
    protected final Vector prepareInitiations(ACLMessage initiation) {
        return prepareCfps(initiation);
    }

    /**
     * This method sets for all prepared cfps <code>conversation-id</code> slot (with
     * value passed in the constructor), <code>protocol</code> slot and
     * <code>reply-with</code> slot with a unique value
     * constructed by concatenating receiver's agent name and phase number (i.e. 0).
     * After that it sends all cfps.
     * @param initiations vector prepared in PREPARE_CFPS state
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
                    String sessionKey = "R_" + r.getName() + "_PH0";
                    toSend.setReplyWith(sessionKey);
                    /* Creates an object Session for all receivers */
                    sessions.put(sessionKey, new Session());
                    /* If initiator coincides with receiver */
                    adjustReplyTemplate(toSend);
                    myAgent.send(toSend);
                    System.out.println("---> " + toSend);
                    ph0Pendings.add(toSend);
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
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding cfp from vector of pendings.
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
                    case ACLMessage.PROPOSE:
                        ((Vector) getDataStore().get(ALL_PROPOSES_KEY)).add(reply);
                    case ACLMessage.FAILURE:
                        ((Vector) getDataStore().get(ALL_FAILURES_KEY)).add(reply);
                }
                for(int i=0; i<ph0Pendings.size(); i++) {
                    ACLMessage pendingMsg = (ACLMessage) ph0Pendings.get(i);
                    if(pendingMsg.getReplyWith().equals(reply.getInReplyTo())) {
                        ph0Pendings.remove(i);
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
     * @return ALL_PROPOSE, SOME_FAILURE, PH0_TIMEOUT_EXPIRED, -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
        int ret;
        Vector responses = (Vector) getDataStore().get(outputKey);
        if(reply != null) {
            if(sessions.size() > 0) {
                /* If there are still active sessions */
                ret = -1;
                System.out.println("still active sessions, ret = -1");
            } else {
                /* All responses received before timeout has been expired */
                Vector proposes = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
                if(((Vector) getDataStore().get(ALL_FAILURES_KEY)).size() == 0) {
                    /* Received all propose, so it prepares vector containing
                    queryIf messages stores in the datastore at outputKey. Timeout
                    and content for all queryifs will be replaced by the user during
                    handleAllResponse method call. CheckSessions returns ALL_PROPOSE. */
                    for(int i=0; i<proposes.size(); i++) {
                        ACLMessage msg = (ACLMessage) proposes.get(i);
                        ACLMessage queryIf = (ACLMessage) msg.clone();
                        queryIf.setPerformative(ACLMessage.QUERY_IF);
                        responses.add(queryIf);
                    }
                    ret = ALL_PROPOSE;
                }
                else {
                    /* Received some failures, so it prepares vector containing reject
                    messages stores in the datastore at outputKey. Content for all
                    rejects will be replaced by the user during handleAllResponse
                    method call. CheckSessions returns SOME_FAILURE. */
                    for(int i=0; i<proposes.size(); i++) {
                        ACLMessage msg = (ACLMessage) proposes.get(i);
                        ACLMessage reject = (ACLMessage) msg.clone();
                        reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        responses.add(reject);
                    }
                    ret = SOME_FAILURE;
                }
                getDataStore().put(outputKey, responses);
            }
        }
        else {
            /* Timeout was expired or we were interrupted, so clear all remaining
            sessions, prepare vector containing reject messages stored in the datastore
            at outputKey and returns PH0_TIMEOUT_EXPIRED. Receivers of reject messages
            are all proposes or pendings. Content of reject messages is replaced by
            the user during handleAllResponses method call. */
            sessions.clear();
            Vector proposesAndPendings = (Vector) getDataStore().get(ALL_PROPOSES_KEY);
            proposesAndPendings.addAll(ph0Pendings);
            for(int i=0; i<proposesAndPendings.size(); i++) {
                ACLMessage msg = (ACLMessage) proposesAndPendings.get(i);
                ACLMessage reject = (ACLMessage) msg.clone();
                reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                responses.add(reject);
            }
            getDataStore().put(outputKey, responses);
            System.out.println("timeout expired, ret = TIMEOUT_EXPIRED");
            ret = PH0_TIMEOUT_EXPIRED;
        }
        System.out.println("checkSessions() - " + ret);
        return ret;
    }

    protected final void handlePositiveResponse(ACLMessage positiveResp) {
    }

    protected final void handleRefuse(ACLMessage refuse) {
    }

    protected final void handleNotUnderstood(ACLMessage notUnderstood) {
    }

    protected final void handleInform(ACLMessage inform) {
    }

    /**
     * Initialize the data store.
     * @param msg Message passed in the constructor
     */
    protected final void initializeDataStore(ACLMessage msg) {
        super.initializeDataStore(msg);
        getDataStore().put(ALL_PROPOSES_KEY, new Vector());
        getDataStore().put(ALL_FAILURES_KEY, new Vector());
        //getDataStore().put(ALL_PH0_PENDINGS_KEY, new Vector());
        getDataStore().put(outputKey, new Vector());
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
        static final int PROPOSE_RECEIVED = 1;
        static final int FAILURE_RECEIVED = 2;
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
                    case ACLMessage.PROPOSE: state = PROPOSE_RECEIVED;
                    case ACLMessage.FAILURE: state = FAILURE_RECEIVED;
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
            return(state == PROPOSE_RECEIVED || state == FAILURE_RECEIVED);
        }
    }
}


