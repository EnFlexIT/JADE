package test.proto.tests.achieveRE;

import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.IteratedAchieveREInitiator;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

public class TestIteratedAchieveRE extends Test {
	private AID responder;
	private int cnt = 0;

	public Behaviour load(Agent a) throws TestException {
		// Create a responder agent that will continue sending INFORM at each new REQUEST
		log("--- Creating responder agent...");
		responder = TestUtility.createAgent(a, "responder", IteratedAchieveREResponderAgent.class.getName(), new Object[]{-1});
		log("--- Responder agent successfully created");

		// Create the IteratedAchieveREInitiator
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(responder);
		request.setProtocol(FIPANames.InteractionProtocol.ITERATED_FIPA_REQUEST);
		request.setContent(String.valueOf(cnt));
		IteratedAchieveREInitiator initiator = new IteratedAchieveREInitiator(a, request) {
			private boolean informReceived = false;
			
			@Override
			protected void handleInform(ACLMessage inform, Vector nextRequests) {
				log("--- INFORM message received");
				informReceived = true;
				if (isSessionTerminated(inform)) {
					failed("--- Session unexpectedly terminated by the responder");
				}
				else {
					try {
						int n = Integer.parseInt(inform.getContent());
						if (n == cnt) {
							log("--- Message content is "+cnt+" as expected");
							ACLMessage next = inform.createReply();
							if (n == 2) {
								// 3rd round OK. Send a CANCEL to terminate the session
								log("--- Prepare CANCEL message");
								next.setPerformative(ACLMessage.CANCEL);
							}
							else {
								next.setPerformative(ACLMessage.REQUEST);
								cnt++;
								next.setContent(String.valueOf(cnt));
							}
							nextRequests.add(next);
						}
					}
					catch (Exception e) {
						failed("--- Wrong message content: expected "+cnt+", found "+inform.getContent());
					}
				}
			}

			@Override
			protected void handleAllResultNotifications(Vector resultNotifications, Vector nextRequests) {
				if (!informReceived) {
					// We got here without having received any INFORM message -->
					// We should have received a FAILURE, REFUSE... or nothing
					failed("--- No INFORM message received");
				}
				// Reset for next round
				informReceived = false;
			}
			
			public int onEnd() {
				if (!isFailed()) {
					passed("--- Test completed successfully");
				}
				return super.onEnd();
			}
		};

		return initiator;
	}

	@Override
	public void clean(Agent a) {
		if (responder != null) {
			try {
				TestUtility.killAgent(a, responder);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
