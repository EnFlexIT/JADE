package test.proto.tests.subscription;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

public class TestSubscriptionInitiatorWithCancelConfirmation extends Test {
	public static final String RESPONDER = "responder";
	
	private AID resp;
	
	private int informCounter = 0;
	private boolean agreeReceived = false;
	private boolean cancelled = false;
	
	public Behaviour load(Agent a) throws TestException {
		
		log("--- Creating Subscription-Responder Agent...");
		resp = TestUtility.createAgent(a, RESPONDER, "test.proto.tests.subscription.SubscriptionResponderAgent", null);
		log("--- Subscription-Responder Agent successfully created");
		
		ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
		msg.addReceiver(resp);
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		SubscriptionInitiator si = new SubscriptionInitiator(a, msg) {
			public void onStart() {
				log("--- Initiating "+FIPANames.InteractionProtocol.FIPA_SUBSCRIBE+" protocol...");
				super.onStart();
			}
			public void handleAgree(ACLMessage agree) {
				log("--- AGREE message received");
				if (informCounter > 0) {
					failed("--- AGREE message received after "+informCounter+" INFORM message(s)");
				}
				else {
					agreeReceived = true;
				}
			}
			
			public void handleInform(ACLMessage inform) {
				log("--- INFORM message received");
				informCounter++;
				if (!cancelled) {
					if (!agreeReceived) {
						failed("--- Missing AGREE message");
					}
					else {
						if (informCounter == 2) {
							// After 2 INFORM, cancel the subscription 
							log("--- Cancel the subscription");
							cancel(resp, false);
							cancelled = true;
						}
					}
				}
				else {
					if (informCounter != 3) {
						failed("--- "+informCounter+" INFORM messages received while 3 were expected");
					}
					log("--- Cancellation done");
					cancellationCompleted(resp);
				}
			}
			
			public void handleFailure(ACLMessage failure) {
				failed("--- FAILURE message received. "+failure);
			}
			
			public void handleRefuse(ACLMessage refuse) {
				failed("--- REFUSE message received. "+refuse);
			}
			
			public void handleNotUnderstood(ACLMessage notUnderstood) {
				failed("--- NOT_UNDERSTOOD message received. "+notUnderstood);
			}
			
			public int onEnd() {
				if (!TestSubscriptionInitiatorWithCancelConfirmation.this.isFailed()) {
					log("--- SubscriptionInitiator behaviour successfully terminated");
				}
				return super.onEnd();
			}
		};
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		sb.addSubBehaviour(si);
		
		// Now wait a bit to be sure no further INFORM messages are received
		sb.addSubBehaviour(new WakerBehaviour(a, 15000) {
			public void onStart() {
				log("--- Wait a bit to be sure no further messages are received...");
				super.onStart();
			}
			
			public void onWake() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					failed("--- Unexpected message received after protocol termination: "+msg);
				}
				else {
					passed("--- No further message received after protocol termination");
				}
			}
		});
		
		
		return sb;
	}
	
	public void clean(Agent a) {
		if (resp != null) {
			try {
				TestUtility.killAgent(a, resp);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
 
}
