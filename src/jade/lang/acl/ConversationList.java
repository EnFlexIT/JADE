package jade.lang.acl;

import jade.core.Agent;

import java.util.Vector;

public class ConversationList {
	private Vector conversations = new Vector();
	protected Agent myAgent = null;
	protected int cnt = 0;
	
	private MessageTemplate myTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
		public boolean match(ACLMessage msg) {
			String convId = msg.getConversationId();
			return (convId == null || conversations.contains(convId));
		}
	} );
	
	public ConversationList(Agent a) {
		myAgent = a;
	}
	
	public String registerConversation() {
		String id = createConversationId();
		conversations.addElement(id);
		return id;
	}
	
	public void deregisterConversation(String convId) {
		conversations.removeElement(convId);
	}

	public void clear() {
		conversations.removeAllElements();
	}
		
	public MessageTemplate getMessageTemplate() {
		return myTemplate;
	}
	
	protected String createConversationId() {
		return myAgent.getName()+(cnt++);
	}
}
		