package test.proto.tests.contractNet;

import test.common.TestException;
import test.common.Test;
import test.common.TestUtility;
import test.domain.ams.tests.TestCreateAgentWithObjectArgs;
import jade.content.onto.BasicOntology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.SSIteratedContractNetResponder;
import jade.proto.SSResponderDispatcher;

import java.util.Vector;

public class TestSSIteratedContractNetResponder extends Test {
	private AID initiatorAgent;
	
	@Override
	public Behaviour load(Agent a) throws TestException {
		final int totIterations = 3;
		
		// Create the behaviour implementing the test
		final ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY) {
			public int onEnd() {
				if (!isFailed()) {
					passed("--- Test completed successfully");
				}
				return 1;
			}
		};
		
		// Add the child listening for the initiation massage.
		// This will add the SSIteratedContractNetResponder as second child
		pb.addSubBehaviour(new SSResponderDispatcher(a, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {
			public void onStart() {
				super.onStart();
				// Create the agent acting as initiator
				// NOTE: Since the Initiator agent immediately sends the initiation message,
				// we cannot create it in the Test initialization phase, otherwise the TestGroupExecutor
				// would steal the initiation message.
				log("--- Creating Initiator Agent");
				try {
					initiatorAgent = TestUtility.createAgent(myAgent, "initiator", TestSSIteratedContractNetResponder.this.getClass().getName()+"$Initiator", new Object[]{totIterations, myAgent.getAID()});
					log("--- Initiator agent successfully created");
				}
				catch (TestException te) {
					te.printStackTrace();
					failed("--- Error creating initiator agent. "+te);
				}
			}
			
			@Override
			public Behaviour createResponder(ACLMessage initiation) {
				log("--- Initiation CFP received");
				return new SSIteratedContractNetResponder(myAgent, initiation) {
					private int expectedIteration = 0;
					
					protected ACLMessage handleCfp(ACLMessage cfp) {
						int iteration = Integer.parseInt(cfp.getContent());
						if (iteration != expectedIteration) {
							failed("--- Wrong iteration: found "+iteration+" while "+expectedIteration+" was expected");
							return null;
						}
						else {
							log("--- Processing iteration "+iteration+" as expected");
							expectedIteration++;
							if (expectedIteration == totIterations) {
								// Next time we should get an ACCEPT_PROPOSAL
								expectedIteration = -1;
							}
							ACLMessage propose = cfp.createReply();
							propose.setPerformative(ACLMessage.PROPOSE);
							return propose;
						}
					}
					
					@Override
					protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
						if (expectedIteration >= 0) {
							// We were still expecting iterations
							failed("--- ACCEPT_PROPOSAL message received while more iterations ("+(totIterations - expectedIteration)+") were expected");
							return null;
						}
						else {
							log("--- ACCEPT_PROPOSAL message received as expected");
							ACLMessage inform = accept.createReply();
							inform.setPerformative(ACLMessage.INFORM);
							return inform;
						}
					}
				};
			}
			
			@Override
			public void addBehaviour(Behaviour b) {
				// Instead of adding the responder directly to the agent, add it as second child
				pb.addSubBehaviour(b);
			}
		});
		return pb;
	}
	
	@Override
	public void clean(Agent a) {
		// Kill the initiator agent;
		try {
			if (initiatorAgent != null) {
				TestUtility.killAgent(a, initiatorAgent);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Inner class Initiator
	 */
	public static class Initiator extends Agent {
		private int totIterations;
		private int currentIteration = 0;
		
		public void setup() {
			// We expect 2 arguments: 
			// - the number of iterations
			// - the AID of the responder
			Object[] args = getArguments();
			if (args != null && args.length > 1) {
				totIterations = (Integer) BasicOntology.adjustPrimitiveValue(args[0], int.class);
				AID responder = (AID) args[1];
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.setContent(String.valueOf(currentIteration));
				cfp.addReceiver(responder);
				addBehaviour(new ContractNetInitiator(this, cfp) {
					@Override
					protected void handleAllResponses(Vector responses, Vector acceptances) {
						ACLMessage propose = (ACLMessage) responses.get(0);
						currentIteration++;
						if (currentIteration == totIterations) {
							ACLMessage accept = propose.createReply();
							accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							acceptances.add(accept);
						}
						else {
							// Next iteration
							Vector v = new Vector();
							ACLMessage nextCfp = propose.createReply();
							nextCfp.setPerformative(ACLMessage.CFP);
							nextCfp.setContent(String.valueOf(currentIteration));
							v.add(nextCfp);
							newIteration(v);
						}
					}
				});
			}
			else {
				System.out.println("WRONG ARGUMENTS!!!!!!!!!!!!");
				doDelete();
			}
		}
	}

}
