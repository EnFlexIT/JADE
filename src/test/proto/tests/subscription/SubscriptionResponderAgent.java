package test.proto.tests.subscription;

import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

public class SubscriptionResponderAgent extends Agent {
	private MessageTemplate template = MessageTemplate.or(
			MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
			MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
	
	private Hashtable tickers = new Hashtable();
	
	protected void setup() {
		addBehaviour(new SubscriptionResponder(this, template) {
			public ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
				final Subscription subs = createSubscription(subscription);
				TickerBehaviour ticker = new TickerBehaviour(myAgent, 10000) {
					public void onTick() {
						ACLMessage inform = subs.getMessage().createReply();
						inform.setPerformative(ACLMessage.INFORM);
						subs.notify(inform);
					}
				};
				myAgent.addBehaviour(ticker);
				tickers.put(subs, ticker);
				ACLMessage agree = subscription.createReply();
				agree.setPerformative(ACLMessage.AGREE);
				return agree;
			}
			
			public ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
				Subscription subs = getSubscription(cancel);
				if (subs != null) {
					subs.close();
					TickerBehaviour ticker = (TickerBehaviour) tickers.remove(subs);
					if (ticker != null) {
						ticker.stop();
						ACLMessage inform = null;
						if (cancel.getReplyWith() != null) {
							inform = cancel.createReply();
							inform.setPerformative(ACLMessage.INFORM);
						}
						return inform;
					}
					else {
						throw new FailureException("Missing ticker for subscription "+cancel);
					}
				}
				else {
					throw new FailureException("Unknown subscription "+cancel);
				}
			}
		});
	}
}
