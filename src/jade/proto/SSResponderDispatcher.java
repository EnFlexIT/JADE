package jade.proto;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 * This behaviour is designed to be used together with the Single-Session responder protocol classes.
 * More in details it is aimed at dealing with protocol initiation messages and dispatching them to
 * responders. The latter are created by means of the <code>createResponder()</code> abstract method
 * that developers must implement.  
 * @author Giovanni Caire
 *
 * @see SSContractNetResponder
 * @see SSIteratedContractNetResponder
 * @see SSIteratedAchieveREtResponder
 */
public abstract class SSResponderDispatcher extends CyclicBehaviour {
	private ConversationList activeConversations;
	private MessageTemplate template;
	
	public SSResponderDispatcher(Agent a, MessageTemplate tpl) {
		super(a);
		activeConversations = new ConversationList(a);
		template = MessageTemplate.and(
				tpl,
				activeConversations.getMessageTemplate());
	}
	
	public final void action() {
		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {
			final String convId = msg.getConversationId();
			if (convId != null) {
				activeConversations.registerConversation(convId);
				SequentialBehaviour sb = new SequentialBehaviour() {
					private static final long serialVersionUID = 12345678L;
					
					public int onEnd() {
						activeConversations.deregisterConversation(convId);
						return super.onEnd();
					}
				};
				sb.setBehaviourName(convId+"-Responder");
				sb.addSubBehaviour(createResponder(msg));
				myAgent.addBehaviour(sb);
			}
			else {
				System.out.println("WARNING: Incoming CFP message received with null conversation ID");
			}
		}
		else {
			block();
		}
	}
	
	/**
	 * This method is responsible for creating a suitable <code>Behaviour</code> acting as responder
	 * in the interaction protocol initiated by message <code>initiationMsg</code>.
	 * @param initiationMsg The message initiating the interaction protocol
	 * @return
	 */
	protected abstract Behaviour createResponder(ACLMessage initiationMsg); 
}
