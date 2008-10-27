package test.behaviours.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import test.common.Test;
import test.common.TestException;

public class TestSendReceiveLoop extends Test {
	private static final long serialVersionUID = 1L;

	// FSM states
	private static final String SEND = "_SEND_";
	private static final String RECEIVE = "_RECEIVE_";
	private static final String SEND_AND_RECEIVE = "_SEND_AND_RECEIVE_";
	private static final String DONE = "_DONE_";

	// FSM events
	private static final int EV_LOOP = 1;
	private static final int EV_ENDLOOP = 0;

	// test configuration parameters
	private static final int TOTAL_LOOPS = 5000;
	private static final long TEST_TIMEOUT = 10000;

	AID myAID;

	public Behaviour load(Agent a) throws TestException {

		myAID = new AID(a.getLocalName(), AID.ISLOCALNAME);

		SequentialBehaviour grandParent = new SequentialBehaviour();

		FSMBehaviour innerChildren = new FSMBehaviour(a);

		innerChildren.registerFirstState(new OneShotBehaviour() {
			private static final long serialVersionUID = 1L;

			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(myAID);
				myAgent.send(msg);
			}
		}, SEND);

		innerChildren.registerState(new SimpleBehaviour() {
			private static final long serialVersionUID = 1L;

			boolean received;

			public void onStart() {
				super.onStart();
				received = false;
			}

			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg == null) {
					block();
				} else {
					received = true;
				}
			}

			public boolean done() {
				return received;
			}
		}, RECEIVE);

		innerChildren.registerState(new SimpleBehaviour() {
			private static final long serialVersionUID = 1L;

			private boolean received;
			private int counter;

			{
				counter = TOTAL_LOOPS;
			}

			public void onStart() {
				super.onStart();
				received = false;
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(myAID);
				myAgent.send(msg);
			}

			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg == null) {
					block();
				} else {
					received = true;
				}
			}

			public boolean done() {
				return received;
			}

			public int onEnd() {
				if (counter-- > 0) {
					return EV_LOOP;
				} else {
					return EV_ENDLOOP;
				}
			}
		}, SEND_AND_RECEIVE);

		innerChildren.registerLastState(new OneShotBehaviour() {
			private static final long serialVersionUID = 1L;

			public void action() {
				passed("All messages received.");
			}
		}, DONE);

		innerChildren.registerDefaultTransition(SEND, RECEIVE);
		innerChildren.registerDefaultTransition(RECEIVE, SEND_AND_RECEIVE);
		innerChildren.registerTransition(SEND_AND_RECEIVE, SEND, EV_LOOP, new String [] {SEND, RECEIVE, SEND_AND_RECEIVE});
		innerChildren.registerTransition(SEND_AND_RECEIVE, DONE, EV_ENDLOOP);

		// in order to have a long notification chain, our test behaviour is made of 30 nested SequentialBehaviour and an innermost FSM
		SequentialBehaviour parent = grandParent;
		SequentialBehaviour child;
		for (int i = 0; i < 30; i++) {
			child = new SequentialBehaviour();
			parent.addSubBehaviour(child);
			parent = child;
		}

		parent.addSubBehaviour(innerChildren);

		setTimeout(TEST_TIMEOUT);
		return grandParent;
	}
}
