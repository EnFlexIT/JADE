package test.behaviours.tests;

import examples.content.eco.ECommerceOntology;
import examples.content.eco.elements.Item;
import examples.content.eco.elements.Sell;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.OntologyServer;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.Test;
import test.common.TestException;

public class TestOntologyServer extends Test {
	private static final long serialVersionUID = -7462690229347904109L;
	
	public static final String DUMMY_ONTOLOGY = "DUMMY-ONTOLOGY"; 
	public static final int N_MESSAGES = 100; 
	private Agent agent;

	public Behaviour load(Agent a) throws TestException { 
		agent = a;
		// Add an OntologyServer to the local TesterAgent serving REQUESTs refering to the 
		// ECommerceontology defined in JADE examples.
		// Redirect calls to serving methods to this Test object. Responses will be either INFORM (if the 
		// right method with the right parameters is invoked) or FAILURE (otherwise) and will be marked with
		// a DUMMY-ONTOLOGY so that we can collect them easily.
		try {
			OntologyServer os = new OntologyServer(a, ECommerceOntology.getInstance(), ACLMessage.REQUEST, this);
			// Read up to 3 messages per run
			os.setMaxProcessedMessagesPerRun(3);
			agent.addBehaviour(os);
			
			SequentialBehaviour sb = new SequentialBehaviour(a);
			// Step 1: Wait a bit to be sure the OntologyServer has started and registered CL and Onto
			sb.addSubBehaviour(new WakerBehaviour(a, 1000) {
			});
			// Step 2: Sends a number of requests
			sb.addSubBehaviour(new OneShotBehaviour(a) {
				@Override
				public void action() {
					try {
						for (int i = 0; i < N_MESSAGES; ++i) {
							sendRequest(i);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						failed("--- Error encoding requests. "+e);
					}
				}
			});
			// Step 3: Collect replies
			sb.addSubBehaviour(new CyclicBehaviour(a) {
                private int responsesCnt = 0;
                
				@Override
				public void action() {
					ACLMessage msg = myAgent.receive(MessageTemplate.MatchOntology(DUMMY_ONTOLOGY));
					if (msg != null) {
						responsesCnt++;
						if (msg.getPerformative() == ACLMessage.INFORM) {
							log("--- INFORM reply received for item "+msg.getContent());
							if (responsesCnt == N_MESSAGES) {
								passed("--- All replies correctly received");
							}
						}
						else {
							failed("--- FAILURE reply received: Content="+msg.getContent());
						}
					}
					else {
						block();
					}
				}
			});
			
			return sb;
			
		} catch (Exception e) {
			throw new TestException("Unexpected error in test initialization", e);
		}
	}
	
	private void sendRequest(int i) throws Exception {
		Action actExpr = new Action(agent.getAID(), new Sell(agent.getAID(), new Item(i), null));
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(agent.getAID());
		request.setOntology(ECommerceOntology.ONTOLOGY_NAME);
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
		agent.getContentManager().fillContent(request, actExpr);
		agent.send(request);
	}
	
	public void serveSellRequest(Sell s, ACLMessage msg) {
		log("Sell REQUEST received for item "+s.getItem().getSerialID());
		ACLMessage inform = msg.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		inform.setContent(String.valueOf(s.getItem().getSerialID()));
		inform.setOntology(DUMMY_ONTOLOGY);
		agent.send(inform);
	}
}
