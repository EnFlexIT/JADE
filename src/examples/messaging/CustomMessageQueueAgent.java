package examples.messaging;

import jade.core.Agent;
import jade.core.ExtendedMessageQueue;
import jade.core.MessageQueue;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This example shows how to create an agent with a custom MessageQueue
 * by redefining the createMessageQueue() method of the Agent class.
 * In particular in this case an instance of the ExtendedMessageQueue is returned
 * with unlimited size (first parameter set to 0) and warningLimit set to 10.
 * When more than 10 messages are present in the queue, all incoming messages 
 * matching the warningDiscardTemplate are discarded.
 * The ExtendedMessageQueue class is further extended overriding its handleDiscarded()
 * method to automatically send back a FAILURE whenever a REQUEST message is discarded. 
 */
public class CustomMessageQueueAgent extends Agent {

	@Override
	protected MessageQueue createMessageQueue() {
		MessageTemplate warningDiscardTemplate = MessageTemplate.or(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		
		return new ExtendedMessageQueue(0, 10, warningDiscardTemplate, this) {
			// Redefine the handleDiscarded() method to send-back a FAILURE whenever 
			// a REQUEST message is discarded
			@Override
			protected void handleDiscarded(ACLMessage msg, boolean warningLimitExceeded) {
				super.handleDiscarded(msg, warningLimitExceeded);
				if (msg.getPerformative() == ACLMessage.REQUEST) {
					myAgent.send(msg.createReply(ACLMessage.FAILURE));
				}
			}
		};
	}
}
