package test.topic.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.*;
import test.topic.TopicMessageEmitterAgent;

public class TestRegistrationDeregistration extends Test {
	private static final String TOPIC_NAME = "test-topic";
	private static final String CONTENT = "Hello World!!";
	private AID emitter;
	private int topicMsgCounter = 0;
	private TopicManagementHelper topicHelper;
	private AID topic;
	
	public Behaviour load(Agent a) throws TestException {
		log("--- Creating Topic-message-emitter Agent...");
		emitter = TestUtility.createAgent(a, "emitter", "test.topic.TopicMessageEmitterAgent", null);
		
		try {
			log("--- Retrieving topic management helper...");
			topicHelper = (TopicManagementHelper) a.getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic(TOPIC_NAME);
		}
		catch (Exception e) {
			throw new TestException("Error retrieving TopicManagementHelper", e);
		}
		
		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY) {
			public int onEnd() {
				if (topicMsgCounter == 1) {
					passed("--- 1 message about topic "+topic.getLocalName()+" received as expected");
				}
				else {
					failed("--- "+topicMsgCounter+" message(s) about topic "+topic.getLocalName()+" received while 1 was expected");
				}
				return super.onEnd();
			}
		};
		
		// The behaviour collecting topic messages
		pb.addSubBehaviour(new CyclicBehaviour(a) {
			public void action() {
				ACLMessage topicMsg = myAgent.receive(MessageTemplate.MatchReceiver(new AID[]{topic}));
				if (topicMsg != null) {
					log("--- Message about topic "+topic.getLocalName()+" received");
					if (CONTENT.equals(topicMsg.getContent())) {
						topicMsgCounter++;						
					}
					else {
						failed("--- Wrong topic message content. Expected "+CONTENT+" found "+topicMsg.getContent());
					}
				}
				else {
					block();
				}
			}
		});
		
		// The behaviour registering/deregistering to topics and triggering topic messages emission
		SequentialBehaviour sb = new SequentialBehaviour(a);
		// Step 1: registers to topic TOPIC_NAME
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- Registering to topic "+topic.getLocalName());
					topicHelper.register(topic);
				}
				catch (Exception e) {
					failed("--- Error registering to topic "+topic.getLocalName()+". "+e);
				}
			}
		} );
		
		// Step 2: triggers the emission of a message about topic TOPIC_NAME (wait a bit to be sure the registration takes effect)
		sb.addSubBehaviour(new WakerBehaviour(a, 1000) {
			public void onWake() {
				log("--- Triggering emission of message about topic "+topic.getLocalName());
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(emitter);
				msg.setContent(TOPIC_NAME+TopicMessageEmitterAgent.SEPARATOR+CONTENT);
				myAgent.send(msg);
			}
		} );
		
		// Step 3: de-registers from topic TOPIC_NAME
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit before deregistering...");
				super.onStart();
			}
			
			public void onWake() {
				try {
					log("--- De-registering from topic "+topic.getLocalName());
					topicHelper.deregister(topic);
				}
				catch (Exception e) {
					failed("--- Error de-registering from topic "+topic.getLocalName()+". "+e);
				}
			}
		} );
		
		// Step 4: triggers the emission of another message about topic TOPIC_NAME
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("--- Triggering emission of message about topic "+topic.getLocalName());
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(emitter);
				msg.setContent(TOPIC_NAME+TopicMessageEmitterAgent.SEPARATOR+CONTENT);
				myAgent.send(msg);
			}
		} );
		
		// Step 5: wait a bit to be sure no other message is received
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit to be sure no other message is received...");
				super.onStart();
			}
			
			public void onWake() {
				// Just do nothing
			}
		} );
		
		pb.addSubBehaviour(sb);
		
		return pb;
	}
	
	public void clean(Agent a) {
		// Kill the emitter agent
		try {
			if (emitter != null) {
				TestUtility.killAgent(a, emitter);
			}
		}
		catch (Exception e) {
			log("--- Error killing emitter agent.");
			e.printStackTrace();
		}
	}
}
