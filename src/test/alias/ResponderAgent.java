package test.alias;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ResponderAgent extends Agent {
	
	protected void setup() {
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(msg.getPerformative());
					reply.setContent(msg.getContent());
					reply.addUserDefinedParameter("sender", msg.getSender().getLocalName());
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		});
	}

}
