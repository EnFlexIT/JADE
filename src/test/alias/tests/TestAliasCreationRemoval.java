package test.alias.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.core.messaging.MessagingHelper;
import jade.core.messaging.MessagingService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.*;

public class TestAliasCreationRemoval extends Test {
	private static final String ALIAS_NAME = "test-alias";
	private static final String CONV_ID = "ccccc";
	
	private AID responder;
	
	public Behaviour load(Agent a) throws TestException {
		log("--- Creating Responder Agent...");
		responder = TestUtility.createAgent(a, "responder", "test.alias.ResponderAgent", null);
		log("--- Responder Agent successfully created");
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 1. Create an Alias
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- 1) Creating alias "+ALIAS_NAME);
					MessagingHelper mh = (MessagingHelper) myAgent.getHelper(MessagingService.NAME);
					mh.createAlias(ALIAS_NAME);
					log("--- Alias successfully created");
				}
				catch (Exception e) {
					failed("--- Error creating alias "+ALIAS_NAME+". "+e);
					e.printStackTrace();
				}
			}
		});
		
		// Step 2. Send a message to the Responder Agent using the alias as sender and waits for the response.
		// Wait 5 sec. at most
		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
		pb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				log("--- 2) Sending message to Responder Agent");
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(responder);
				msg.setSender(new AID(ALIAS_NAME, AID.ISLOCALNAME));
				msg.setContent("1");
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
			}
			public void action() {
				ACLMessage reply = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (reply != null) {
					log("--- Reply from Responder Agent received");
					String sender = reply.getUserDefinedParameter("sender");
					if (ALIAS_NAME.equals(sender)) {
						log("--- Responder Agent received a message from "+sender+" as expected");
					}
					else {
						failed("--- WRONG sender: Responder Agent received a message from "+sender+" while "+ALIAS_NAME+" was expeted");
					}
					finished = true;
				}
				else {
					block();
				}
			}
			
			public boolean done() {
				return finished;
			}
		});
		pb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onWake() {
				failed("--- Reply from Responder Agent not received in due time");
			}
		});
		sb.addSubBehaviour(pb);
		
		// Step 3. Send a message to the created alias and waits for it
		// Wait 5 sec. at most
		pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
		pb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				log("--- 3) Sending message to alias "+ALIAS_NAME);
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID(ALIAS_NAME, AID.ISLOCALNAME));
				msg.setContent("2");
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
			}
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (msg != null) {
					log("--- Message received");
					if (msg.getPerformative() == ACLMessage.INFORM) {
						log("--- Message sent to alias "+ALIAS_NAME+" correctly received");
						if ("2".equals(msg.getContent())) {
							log("--- Message content is 2 as expected");
						}
						else {
							failed("--- WRONG message content: Received "+msg.getContent()+" while 2 was expeted");
						}
					}
					else {
						failed("--- Unexpected message received: "+msg);
					}
					finished = true;
				}
				else {
					block();
				}
			}
			
			public boolean done() {
				return finished;
			}
		});
		pb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onWake() {
				failed("--- Message sent to alias "+ALIAS_NAME+" not received in due time");
			}
		});
		sb.addSubBehaviour(pb);
		
		// Step 4. Delete the alias
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- 4) Deleting alias "+ALIAS_NAME);
					MessagingHelper mh = (MessagingHelper) myAgent.getHelper(MessagingService.NAME);
					mh.deleteAlias(ALIAS_NAME);
					log("--- Alias successfully deleted");
				}
				catch (Exception e) {
					failed("--- Error deleting alias "+ALIAS_NAME+". "+e);
					e.printStackTrace();
				}
			}
		});
		
		// Step 5. Send another message to the alias and check that an AMS FAILURE is received as response
		sb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				log("--- 5) Sending second message to alias "+ALIAS_NAME);
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID(ALIAS_NAME, AID.ISLOCALNAME));
				msg.setContent("3");
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
			}
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (msg != null) {
					log("--- Message received");
					if (msg.getPerformative() == ACLMessage.FAILURE && msg.getSender().equals(myAgent.getAMS())) {
						passed("--- AMS FAILURE message received as expected");
					}
					else {
						failed("--- Unexpected message received: "+msg);
					}
					finished = true;
				}
				else {
					block();
				}
			}
			
			public boolean done() {
				return finished;
			}
		});
		
		return sb;
	}
	
	public void clean(Agent a) {
		// Kill the responder agent
		try {
			if (responder != null) {
				TestUtility.killAgent(a, responder);
			}
		}
		catch (Exception e) {
			log("--- Error killing Responder Agent.");
			e.printStackTrace();
		}
	}
}
