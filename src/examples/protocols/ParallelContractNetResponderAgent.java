package examples.protocols;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSContractNetResponder;
import jade.proto.SSResponderDispatcher;

/**
This example shows how to implement the responder role in 
a FIPA-contract-net interaction protocol. In this case in particular 
we use a <code>SSResponderDispatcher</code> to receive incoming CFPs in combination with 
<code>SSContractNetResponder</code> instances   
to participate into negotiations (possibly occurring in parallel) where an initiator needs to assign
a task to an agent among a set of candidates.
@author Giovanni Caire - TILAB
*/
public class ParallelContractNetResponderAgent extends Agent {

	protected void setup() {
		System.out.println("Agent "+getLocalName()+" waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );

		addBehaviour(new SSResponderDispatcher(this, template) {

			@Override
			protected Behaviour createResponder(ACLMessage cfp) {
				return new SSContractNetResponder(myAgent, cfp) {
					@Override
					protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
						System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
						int proposal = evaluateAction();
						if (proposal > 2) {
							// We provide a proposal
							System.out.println("Agent "+getLocalName()+": Proposing "+proposal);
							ACLMessage propose = cfp.createReply();
							propose.setPerformative(ACLMessage.PROPOSE);
							propose.setContent(String.valueOf(proposal));
							return propose;
						}
						else {
							// We refuse to provide a proposal
							System.out.println("Agent "+getLocalName()+": Refuse");
							throw new RefuseException("evaluation-failed");
						}
					}

					@Override
					protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
						System.out.println("Agent "+getLocalName()+": Proposal accepted");
						if (performAction()) {
							System.out.println("Agent "+getLocalName()+": Action successfully performed");
							ACLMessage inform = accept.createReply();
							inform.setPerformative(ACLMessage.INFORM);
							return inform;
						}
						else {
							System.out.println("Agent "+getLocalName()+": Action execution failed");
							throw new FailureException("unexpected-error");
						}	
					}

					protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
						System.out.println("Agent "+getLocalName()+": Proposal rejected");
					}
				}; 
			}
		});
	}

	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		return (int) (Math.random() * 10);
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		return (Math.random() > 0.2);
	}
}
