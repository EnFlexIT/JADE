package test.proto.tests.achieveRE;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSIteratedAchieveREResponder;
import jade.proto.SSResponderDispatcher;

public class IteratedAchieveREResponderAgent extends Agent {
	
	protected void setup() {
		final long nRounds;
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			nRounds = (Long) args[0];
		}
		else {
			nRounds = -1;
		}
		
		addBehaviour(new SSResponderDispatcher(this, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.ITERATED_FIPA_REQUEST)) {

			@Override
			protected Behaviour createResponder(ACLMessage initiationMsg) {
				return new SSIteratedAchieveREResponder(myAgent, initiationMsg) {
					@Override
					protected ACLMessage handleRequest(ACLMessage request) throws RefuseException, FailureException, NotUnderstoodException {
						try {
							int val = Integer.parseInt(request.getContent());
							if (nRounds > 0 && val == nRounds) {
								closeSessionOnNextReply();
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent(request.getContent());
						
						return reply;
					}
					
					@Override
					protected void handleCancel(ACLMessage cancel) {
					}
				};
			}
			
		});
	}

}
