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
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.proto.states.*;

/**
 * todo@ Inserire commenti!!!
 */
public class TwoPhResponder extends FSMBehaviour {
    /* FSM states names */
    private static final String RECEIVE_CFP_STATE = "Receive-CallForProposal";
    private static final String PREPARE_PROPOSE_STATE = "Prepare-Propose";
    private static final String SEND_PROPOSE_STATE = "Send-Propose";
    private static final String RECEIVE_QUERY_IF_STATE = "Receive-Query-If";
    private static final String HANDLE_OUT_OF_SEQUENCE_PH1_STATE = "Handle-Out-Of-Sequence-Ph1";
    private static final String HANDLE_QUERY_IF_STATE = "Handle-Query-If";
    private static final String HANDLE_REJECT_STATE ="Handle-Reject";
    private static final String SEND_CONFIRM_STATE = "Send-Confirm";
    private static final String RECEIVE_ACCEPTANCE_STATE = "Receive-Acceptance";
    private static final String HANDLE_OUT_OF_SEQUENCE_PH2_STATE = "Handle-Out-Of-Sequence-Ph2";
    private static final String HANDLE_ACCEPTANCE_STATE = "Handle-Acceptance";
    private static final String DUMMY_FINAL = "Dummy-final";

    /* Data store keys */
    /**
     * key to retrieve from the DataStore of the behaviour the ACLMessage
     * object received by the responder.
     **/
    public final String CFP_KEY = "__Cfp_key" + hashCode();
    /**
     * key to retrieve from the DataStore of the behaviour the ACLMessage
     * object sent as a response to the initiator's ACLMessage CFP.
     **/
    public final String PROPOSE_KEY = "__Propose_key" + hashCode();
    /**
     * key to retrieve from the DataStore of the behaviour the ACLMessage
     * object received after an ACLMessage PROPOSE reply.
     **/
    public final String QUERY_IF_KEY = "__Query_If_key" + hashCode();
    /**
     * key to retrieve from the DataStore of the behaviour the ACLMessage
     * object sent by SEND_CONFIRM_STATE.
     **/
    public final String REPLY_KEY = "__Reply_key" + hashCode();
    /**
     * key to retrieve from the DataStore of the behaviour the ACLMessage
     * object received after an ACLMessage CONFIRM reply.
     **/
    public final String ACCEPTANCE_KEY = "__Acceptance_key" + hashCode();
    /**
     * key to retrieve from the DataStore of the behaviour the ACLMessage
     * object received by SEND_CONFIRM_STATE.
     **/
    public final String QUERY_KEY = "__Query_key" + hashCode();

    /**/
    private MsgReceiver cfp_req, accept_req, queryIf_req;
    //private SequentialBehaviour queryIf_req;
    private MessageTemplate msgt = null;

    /**
    * Constructor of the behaviour that creates a new empty DataStore
    * @see #TwoPhResponder(Agent a, MessageTemplate mt, DataStore store)
    **/
    public TwoPhResponder(Agent a, MessageTemplate mt) {
         this(a, mt, new DataStore());
    }

    /**
     * Constructor of the behaviour.
     * @param a is the reference to the Agent object
     * @param mt is the MessageTemplate that must be used to match
     * the initiator message. Take care that if mt is null every message is
     * consumed by this protocol.
     * The best practice is to have a MessageTemplate that matches
     * the protocol slot; the static method <code>createMessageTemplate</code>
     * might be usefull.
     * @param store the DataStore for this protocol behaviour
     **/
    public TwoPhResponder(Agent a, MessageTemplate mt, DataStore store) {
        super(a);
        setDataStore(store);
        this.msgt = mt;
        registerDefaultTransition(RECEIVE_CFP_STATE, PREPARE_PROPOSE_STATE);
        registerDefaultTransition(PREPARE_PROPOSE_STATE, SEND_PROPOSE_STATE);
        registerTransition(SEND_PROPOSE_STATE, RECEIVE_QUERY_IF_STATE, ACLMessage.PROPOSE);
        registerTransition(SEND_PROPOSE_STATE, DUMMY_FINAL, ACLMessage.FAILURE);
        registerDefaultTransition(RECEIVE_QUERY_IF_STATE, HANDLE_OUT_OF_SEQUENCE_PH1_STATE);
        registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE_PH1_STATE, RECEIVE_QUERY_IF_STATE);
        registerTransition(RECEIVE_QUERY_IF_STATE, HANDLE_QUERY_IF_STATE, ACLMessage.QUERY_IF);
        registerTransition(RECEIVE_QUERY_IF_STATE, HANDLE_REJECT_STATE, ACLMessage.REJECT_PROPOSAL);
        registerTransition(RECEIVE_QUERY_IF_STATE, PREPARE_PROPOSE_STATE, ACLMessage.CFP);
        registerDefaultTransition(HANDLE_QUERY_IF_STATE, SEND_CONFIRM_STATE);
        registerDefaultTransition(HANDLE_REJECT_STATE, SEND_CONFIRM_STATE);
        registerTransition(SEND_CONFIRM_STATE, RECEIVE_ACCEPTANCE_STATE, ACLMessage.CONFIRM);
        registerTransition(SEND_CONFIRM_STATE, DUMMY_FINAL, ACLMessage.DISCONFIRM);
        registerTransition(SEND_CONFIRM_STATE, DUMMY_FINAL, ACLMessage.INFORM);
        registerDefaultTransition(RECEIVE_ACCEPTANCE_STATE, HANDLE_OUT_OF_SEQUENCE_PH2_STATE);
        registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE_PH2_STATE, RECEIVE_ACCEPTANCE_STATE);
        registerTransition(RECEIVE_ACCEPTANCE_STATE, HANDLE_ACCEPTANCE_STATE, ACLMessage.ACCEPT_PROPOSAL);
        registerTransition(RECEIVE_ACCEPTANCE_STATE, HANDLE_REJECT_STATE, ACLMessage.REJECT_PROPOSAL);
        registerDefaultTransition(HANDLE_ACCEPTANCE_STATE, SEND_CONFIRM_STATE);

        Behaviour b;

        /* RECEIVE_CFP */
        cfp_req = new MsgReceiver(myAgent, msgt, -1, getDataStore(), CFP_KEY) {
            public void onStart() {
                System.out.println("\n\n************ RECEIVE_CFP started ************");
                System.out.println("msgt = " + msgt.toString());
                super.onStart();
            }
            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(CFP_KEY);
                System.out.println("Received msg put in CFP_KEY");
                System.out.println("CFP_KEY = (" + ACLMessage.getPerformative(msg.getPerformative()) + "," + msg.getContent() + ")");
                msgt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId(msg.getConversationId()),
                        MessageTemplate.MatchProtocol(TwoPhConstants.JADE_TWO_PHASE_COMMIT));
                queryIf_req.setTemplate(msgt);
                System.out.println("msgt = " + msgt.toString());
                System.out.println("************ RECEIVE_CFP ended ************");
                return super.onEnd();
            }
        };
        registerFirstState(cfp_req, RECEIVE_CFP_STATE);

        /* PREPARE_PROPOSE */
        b = new OneShotBehaviour(myAgent) {
            ACLMessage response = null;
            public void onStart() {
                System.out.println("\n\n************ PREPARE_PROPOSE started ************");
                super.onStart();
            }

            public void action() {
                DataStore ds = getDataStore();
                ACLMessage cfp = (ACLMessage) ds.get(CFP_KEY);
                try {
                    response = preparePropose(cfp);
                }
                catch(NotUnderstoodException e) {
                    response = e.getACLMessage();
                }
                ds.put(PROPOSE_KEY, response);
            }
            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(PROPOSE_KEY);
                System.out.println("Response msg put in PROPOSE_KEY");
                System.out.println("PROPOSE_KEY = (" + ACLMessage.getPerformative(msg.getPerformative()) + "," + msg.getContent() + ")");
                System.out.println("************ PREPARE_PROPOSE ended ************");
                return response.getPerformative();
            }
        };
        registerDSState(b, PREPARE_PROPOSE_STATE);

        /* SEND_PROPOSE */
        b = new ReplySender(myAgent, PROPOSE_KEY, CFP_KEY) {
            public void onStart() {
                System.out.println("\n\n************ SEND_PROPOSE started ************");
                super.onStart();
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage toReply = (ACLMessage) ds.get(CFP_KEY);
                ACLMessage msg = (ACLMessage) ds.get(PROPOSE_KEY);
                System.out.println("Msg to reply CFP_KEY = (" + ACLMessage.getPerformative(toReply.getPerformative()) +
                        "," + toReply.getContent() + ")");
                System.out.println("Response msg PROPOSE_KEY = (" + ACLMessage.getPerformative(msg.getPerformative()) +
                        "," + msg.getContent() + ")");
                System.out.println("************ SEND_PROPOSE ended ************");
                return super.onEnd();
            }
        };
        registerDSState(b, SEND_PROPOSE_STATE);

        /* RECEIVE_QUERY_IF */
        queryIf_req = new MsgReceiver(myAgent, msgt, -1, getDataStore(), QUERY_IF_KEY) {
            public void onStart() {
                System.out.println("\n\n************ RECEIVE_QUERY_IF started ************");
                System.out.println("msgt = " + msgt.toString());
                super.onStart();
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage request = (ACLMessage) ds.get(QUERY_IF_KEY);
                ds.put(QUERY_KEY, request);
                System.out.println("Received msg put in QUERY_IF_KEY and QUERY_KEY");
                System.out.println("QUERY_IF_KEY = QUERY_KEY = (" + ACLMessage.getPerformative(request.getPerformative()) +
                        "," + request.getContent() + ")");
                if(request.getPerformative() == ACLMessage.CFP) {
                    System.out.println("Received msg put in CFP_KEY");
                    ds.put(CFP_KEY, request);
                }
                System.out.println("************ RECEIVE_QUERY_IF ended ************");
			    return super.onEnd();
            }
        };
        registerDSState(queryIf_req, RECEIVE_QUERY_IF_STATE);

        /* HANDLE_OUT_OF_SEQUENCE_PH1 */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_OUT_OF_SEQUENCE_PH1 started ************");
                super.onStart();
            }

            public void action() {
                DataStore ds = getDataStore();
                ACLMessage outMsg = (ACLMessage) ds.get(QUERY_IF_KEY);
                handleOutOfSequencePh1(outMsg);
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(QUERY_IF_KEY);
                System.out.println("************ HANDLE_OUT_OF_SEQUENCE_PH1 ended ************");
                return msg.getPerformative();
            }
        };
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE_PH1_STATE);

        /* HANDLE_QUERY_IF */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_QUERY_IF started ************");
                super.onStart();
            }

            public void action() {
                DataStore ds = getDataStore();
                ACLMessage queryIf = (ACLMessage) ds.get(QUERY_IF_KEY);
                ds.put(QUERY_KEY, queryIf);
                System.out.println("Msg to reply QUERY_IF_KEY = (" + ACLMessage.getPerformative(queryIf.getPerformative()) +
                        "," + queryIf.getContent() + ")");
                System.out.println("QUERY_IF_KEY put in QUERY_KEY");
                ACLMessage response = null;
                try {
                    response = handleQueryIf(queryIf);
                }
                catch(FailureException e) {
                    response = e.getACLMessage();
                }
                ds.put(REPLY_KEY, response);
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(REPLY_KEY);
                System.out.println("Response msg put in REPLY_KEY = " + ACLMessage.getPerformative(msg.getPerformative()) +
                        "," + msg.getContent() + ")");
                System.out.println("************ HANDLE_QUERY_IF ended ************");
                return msg.getPerformative();
            }
        };
        registerDSState(b, HANDLE_QUERY_IF_STATE);

        /* HANDLE_REJECT */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_REJECT started ************");
                super.onStart();
            }

            public void action() {
                DataStore ds = getDataStore();
                ACLMessage reject = (ACLMessage) ds.get(QUERY_IF_KEY);
                System.out.println("Msg to reply QUERY_IF_KEY = (" + ACLMessage.getPerformative(reject.getPerformative()) +
                        "," + reject.getContent() + ")");
                ds.put(QUERY_KEY, reject);
                System.out.println("QUERY_IF_KEY put in QUERY_KEY");
                ACLMessage response = null;
                try {
                    response = handleRejectProposal(reject);
                }
                catch(FailureException e) {
                    response = e.getACLMessage();
                }
                ds.put(REPLY_KEY, response);
            }
            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(REPLY_KEY);
                System.out.println("Response msg put in REPLY_KEY = " + ACLMessage.getPerformative(msg.getPerformative()) +
                                        "," + msg.getContent() + ")");
                System.out.println("************ HANDLE_REJECT ended ************");
                return msg.getPerformative();
            }
        };
        registerDSState(b, HANDLE_REJECT_STATE);

        /* SEND_CONFIRM */
        b = new ReplySender(myAgent, REPLY_KEY, QUERY_KEY) {
            public void onStart() {
                System.out.println("\n\n************ SEND_CONFIRM started ************");
                super.onStart();
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(REPLY_KEY);
                ACLMessage query = (ACLMessage) ds.get(QUERY_KEY);
                System.out.println("Msg to reply QUERY_KEY = (" + ACLMessage.getPerformative(query.getPerformative()) +
                        "," + query.getContent() + ")");
                System.out.println("Response msg REPLY_KEY = (" + ACLMessage.getPerformative(msg.getPerformative()) +
                        "," + msg.getContent() + ")");

                accept_req.setTemplate(msgt);
                System.out.println("************ SEND_CONFIRM endend ************");
                return super.onEnd();
            }
        };
        registerDSState(b, SEND_CONFIRM_STATE);

        /* WAIT_ACCEPTANCE */
        accept_req = new MsgReceiver(myAgent, msgt, -1, getDataStore(), ACCEPTANCE_KEY) {
            public void onStart() {
                System.out.println("\n\n************ WAIT_ACCEPTANCE started ************");
                System.out.println("msgt = " + msgt.toString());
                super.onStart();
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(ACCEPTANCE_KEY);
                System.out.println("Received msg put in ACCEPTANCE_KEY = " + ACLMessage.getPerformative(msg.getPerformative()) +
                                        "," + msg.getContent() + ")");
                System.out.println("************ WAIT_ACCEPTANCE ended ************");
                return super.onEnd();
            }
        };
        registerDSState(accept_req, RECEIVE_ACCEPTANCE_STATE);

        /* HANDLE_OUT_OF_SEQUENCE_PH2 */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_OUT_OF_SEQUENCE_PH2 started ************");
                super.onStart();
            }

            public void action() {
                DataStore ds = getDataStore();
                ACLMessage outMsg = (ACLMessage) ds.get(ACCEPTANCE_KEY);
                handleOutOfSequencePh2(outMsg);
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(ACCEPTANCE_KEY);
                System.out.println("ACCEPTANCE_KEY = " + ACLMessage.getPerformative(msg.getPerformative()) +
                                        "," + msg.getContent() + ")");
                System.out.println("************ HANDLE_OUT_OF_SEQUENCE_PH2 ended ************");
                return msg.getPerformative();
            }
        };
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE_PH2_STATE);

        /* HANDLE_ACCEPTANCE */
        b = new OneShotBehaviour(myAgent) {
            public void onStart() {
                System.out.println("\n\n************ HANDLE_ACCEPTANCE started ************");
                super.onStart();
            }

            public void action() {
                DataStore ds = getDataStore();
                ACLMessage accept = (ACLMessage) ds.get(ACCEPTANCE_KEY);
                System.out.println("Msg to reply ACCEPTANCE_KEY = (" + ACLMessage.getPerformative(accept.getPerformative()) +
                        "," + accept.getContent() + ")");
                ds.put(QUERY_KEY, accept);
                System.out.println("ACCEPTANCE_KEY put in QUERY_KEY");
                ACLMessage response = null;
                try {
                    response = handleAcceptance(accept);
                }
                catch(FailureException e) {
                    response = e.getACLMessage();
                }
                ds.put(REPLY_KEY, response);
            }

            public int onEnd() {
                DataStore ds = getDataStore();
                ACLMessage msg = (ACLMessage) ds.get(REPLY_KEY);
                System.out.println("Response msg put in REPLY_KEY = " + ACLMessage.getPerformative(msg.getPerformative()) +
                                        "," + msg.getContent() + ")");
                System.out.println("************ HANDLE_ACCEPTANCE ended ************");
                return super.onEnd();
            }

        };
        registerDSState(b, HANDLE_ACCEPTANCE_STATE);

        /* DUMMY_FINAL */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                System.out.println("\n\n************ DUMMY_FINAL ************");
            }
        };
        registerLastState(b, DUMMY_FINAL);
    }

    /**
     * This method is called when the initiator's
     * message is received that matches the message template
     * passed in the constructor.
     * This default implementation return null which has
     * the effect of sending no reponse. Programmers should
     * override the method in case they need to react to this event.
     * @param cfp the received message
     * @return the ACLMessage to be sent as a response (i.e. one of
     * <code>PROPOSE, FAILURE</code>. <b>Remind</b> to
     * use the method <code>createReply</code> of the class ACLMessage in order
     * to create a valid reply message
     * @see jade.lang.acl.ACLMessage#createReply()
     **/
    protected ACLMessage preparePropose(ACLMessage cfp)
        throws NotUnderstoodException {
        System.out.println("preparePropose() method not re-defined");
        return null;
    }

    /**
    * This method is called after the <code>QUERY-IF</code> has been received.
    * This default implementation return null which has
    * the effect of sending no result notification. Programmers should
    * override the method in case they need to react to this event.
    * @param queryIf the received message
    * @return the ACLMessage to be sent as a result notification (i.e. one of
    * <code>CONFIRM, INFORM, DISCONFIRM</code>. <b>Remind</b> to
    * use the method createReply of the class ACLMessage in order
    * to create a valid reply message
    * @see jade.lang.acl.ACLMessage#createReply()
    **/
    protected ACLMessage handleQueryIf(ACLMessage queryIf)
        throws FailureException {
        System.out.println("handleQueryIf() method not re-defined");
        return null;
    }

    /**
    * This method is called after the <code>REJECT-PROPOSAL</code> has been received.
    * This default implementation do nothing.
    * Programmers should override the method in case they need to react to this event.
    * @param reject the received message
    * @return the ACLMessage to be sent as a result notification (i.e. an
    * <code>INFORM</code>. <b>Remind</b> to
    * use the method createReply of the class ACLMessage in order
    * to create a valid reply message
    * @see jade.lang.acl.ACLMessage#createReply()
    **/
    protected ACLMessage handleRejectProposal(ACLMessage reject)
        throws FailureException {
        System.out.println("handleRejectProposal() method not re-defined");
        return null;
    }

    /**
    * This method is called after the <code>ACCEPT-PROPOSAL</code> has been received.
    * This default implementation return null which has
    * the effect of sending no result notification. Programmers should
    * override the method in case they need to react to this event.
    * @param accept the received message
    * @return the ACLMessage to be sent as a result notification (i.e. an
    * <code>INFORM</code>. <b>Remind</b> to use the method createReply of
    * the class ACLMessage in order to create a valid reply message
    * @see jade.lang.acl.ACLMessage#createReply()
    **/
    protected ACLMessage handleAcceptance(ACLMessage accept)
        throws FailureException {
        System.out.println("handleAcceptance() method not re-defined");
        return null;
    }

    /**
    * This callback method is called when arrives an incorrect message
    * (i.e. a message with the correct in-reply-to field but with a
    * unexpected performative) after has been sent a <code>PROPOSE</code>.
    * This default implementation do nothing.
    * Programmers should override the method in case they need to react to this event.
    * @param  outOfSequenceMsg the received message that does not respect the protocol
    **/
    protected void handleOutOfSequencePh1(ACLMessage outOfSequenceMsg) {
        System.out.println("handleOutOfSequencePh1() method not re-defined");
    }

    /**
    * This callback method is called when arrives an incorrect message
    * (i.e. a message with the correct in-reply-to field but with a
    * unexpected performative) after has been sent a <code>CONFIRM</code>.
    * This default implementation do nothing.
    * Programmers should override the method in case they need to react to this event.
    * @param  outOfSequenceMsg the received message that does not respect the protocol
    **/
    protected void handleOutOfSequencePh2(ACLMessage outOfSequenceMsg) {
        System.out.println("handleOutOfSequencePh2() method not re-defined");
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the PREPARE_PROPOSE state. This behaviour would override the homonymous
     * method. This method also set the data store of the registered
     * <code>Behaviour</code> to the DataStore of this current behaviour.
     * It is responsibility of the registered behaviour to put the response
     * to be sent into the datastore at the <code>PROPOSE_KEY</code> key.
     * @param b the Behaviour that will handle this state
     **/
    public void registerPreparePropose(Behaviour b) {
        registerDSState(b, PREPARE_PROPOSE_STATE);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_QUERY_IF state. This behaviour would override the homonymous
     * method. This method also set the data store of the registered
     * <code>Behaviour</code> to the DataStore of this current behaviour.
     * It is responsibility of the registered behaviour to put the response
     * to be sent into the datastore at the <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     **/
    public void registerHandleQueryIf(Behaviour b) {
       registerDSState(b, HANDLE_QUERY_IF_STATE);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_REJECT state. This behaviour would override the homonymous
     * method. This method also set the data store of the registered
     * <code>Behaviour</code> to the DataStore of this current behaviour.
     * It is responsibility of the registered behaviour to put the response
     * to be sent into the datastore at the <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     **/
    public void registerHandleRejectProposal(Behaviour b) {
        registerDSState(b, HANDLE_REJECT_STATE);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_ACCEPTANCE state. This behaviour would override the homonymous
     * method. This method also set the data store of the registered
     * <code>Behaviour</code> to the DataStore of this current behaviour.
     * It is responsibility of the registered behaviour to put the response
     * to be sent into the datastore at the <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     **/
    public void registerHandleAcceptProposal(Behaviour b) {
        registerDSState(b, HANDLE_ACCEPTANCE_STATE);
    }

    /**todo@ Vedere se e' possibile usare un solo stato HANDLE_OUT_OF_SEQUENCE */
    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_OUT_OF_SEQUENCE_PH1 state. This behaviour would override
     * the homonymous method. This method also set the data store of the registered
     * <code>Behaviour</code> to the DataStore of this current behaviour.
     * The registered behaviour can found the send and received message in the
     * datastore at the keys <code>PROPOSE_KEY</code> and <code>QUERY_IF_KEY</code>.
     * @param b the Behaviour that will handle this state
     **/
    public void registerHandleOutOfSequencePh1(Behaviour b) {
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE_PH1_STATE);
    }

    /**todo@ Vedere se e' possibile usare un solo stato HANDLE_OUT_OF_SEQUENCE */
    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_OUT_OF_SEQUENCE_PH2 state. This behaviour would override
     * the homonymous method. This method also set the data store of the registered
     * <code>Behaviour</code> to the DataStore of this current behaviour.
     * The registered behaviour can found the send and received message in the
     * datastore at the keys <code>REPLY_KEY</code> and <code>ACCEPTANCE_KEY</code>.
     * @param b the Behaviour that will handle this state
     **/
    public void registerHandleOutOfSequencePh2(Behaviour b) {
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE_PH2_STATE);
    }

    private void registerDSState(Behaviour b, String name) {
            b.setDataStore(getDataStore());
            registerState(b,name);
    }

    /**todo@ Da rivedere il createMessageTemplate E I COMMENTI!!!! */
    /**  This static method can be used to set the proper message Template
     * (based on the interaction protocol and the performative) to be passed to the constructor of this behaviour.
      *  @see jade.domain.FIPANames.InteractionProtocol
      **/
    public static MessageTemplate createMessageTemplate() {
        return MessageTemplate.and(MessageTemplate.MatchProtocol(TwoPhConstants.JADE_TWO_PHASE_COMMIT),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
    }

    public void reset() {
		super.reset();
		DataStore ds = getDataStore();
		ds.remove(CFP_KEY);
		ds.remove(PROPOSE_KEY);
		ds.remove(QUERY_IF_KEY);
        ds.remove(REPLY_KEY);
        ds.remove(ACCEPTANCE_KEY);
        ds.remove(REPLY_KEY);
		ds.remove(QUERY_KEY);
        msgt = createMessageTemplate();
        cfp_req.reset(msgt, -1, getDataStore(), CFP_KEY);
    }
}
