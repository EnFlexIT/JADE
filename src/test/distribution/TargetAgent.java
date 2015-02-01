package test.distribution;

import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TargetAgent extends Agent {
	public static final String TARGET_TYPE = "TARGET";
	
	private List<String> myItems = new ArrayList<String>();
	
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType(TARGET_TYPE);
		dfd.addServices(sd);
		try {
			System.err.println("TargetAgent "+getLocalName()+" - Registering with the DF");
			DFService.register(this, dfd);
		}
		catch (Exception e) {
			System.err.println("TargetAgent "+getLocalName()+" - Error registering with the DF");
			e.printStackTrace();
		}
		
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.QUERY_REF) {
						System.err.println("TargetAgent "+getLocalName()+" - QUERY-REF message received");
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent(myItems.toString());
						myAgent.send(reply);
					}
					else if (msg.getPerformative() == ACLMessage.REQUEST) {
						System.err.println("TargetAgent "+getLocalName()+" - REQUEST message received");
						myItems.add(msg.getContent());
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						myAgent.send(reply);
					}
				}
				else {
					block();
				}
			}
		});
	}
	
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (Exception e) {
			System.err.println("TargetAgent "+getLocalName()+" - Error de-registering from the DF");
			e.printStackTrace();
		}
	}
}
