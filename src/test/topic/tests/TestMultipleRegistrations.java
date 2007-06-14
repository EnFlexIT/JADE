package test.topic.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.*;
import test.topic.TopicMessageCollectorAgent;
import test.topic.TopicMessageEmitterAgent;

public class TestMultipleRegistrations extends Test {
	private static final String TOPIC_NAME = "test-topic";
	private static final String CONTAINER_NAME = "remote-container";
	private static final String CONTENT = "Hello World!!";
	private AID emitter, collector1, collector2;
	private JadeController jc;
	private int topicMsgCounter = 0;

	public Behaviour load(Agent a) throws TestException {
		log("--- Creating Topic-message-emitter Agent...");
		emitter = TestUtility.createAgent(a, "emitter", "test.topic.TopicMessageEmitterAgent", null);
		
		log("--- Creating 2 Topic-message-collector Agents one in the Main Container and one in a different peripheral container...");
		collector1 = TestUtility.createAgent(a, "c1", "test.topic.TopicMessageCollectorAgent", new Object[]{TOPIC_NAME, a.getAID()}, a.getAMS(), jade.core.AgentContainer.MAIN_CONTAINER_NAME);
		
		String mainHost = TestUtility.getContainerHostName(a, null);
		jc = TestUtility.launchJadeInstance(CONTAINER_NAME, null, "-container-name " + CONTAINER_NAME + " -container -host " + mainHost + " -port " + Test.DEFAULT_PORT + " -services jade.core.event.NotificationService;jade.core.mobility.AgentMobilityService;jade.core.messaging.TopicManagementService", null);
		collector2 = TestUtility.createAgent(a, "c2", "test.topic.TopicMessageCollectorAgent", new Object[]{TOPIC_NAME, a.getAID()}, a.getAMS(), jc.getContainerName());
		
		
		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY) {
			public int onEnd() {
				if (topicMsgCounter == 2) {
					passed("--- 2 messages about topic "+TOPIC_NAME+" received as expected");
				}
				else {
					failed("--- "+topicMsgCounter+" message(s) about topic "+TOPIC_NAME+" received while 2 were expected");
				}
				return super.onEnd();
			}
		};
		
		// The behaviour receiving messages forwarded by collector agents
		pb.addSubBehaviour(new CyclicBehaviour(a) {
			public void action() {
				ACLMessage topicMsg = myAgent.receive(MessageTemplate.MatchConversationId(TopicMessageCollectorAgent.COLLECTOR_CONV_ID));
				if (topicMsg != null) {
					log("--- Message received from agent "+topicMsg.getSender().getLocalName());
					if (CONTENT.equals(topicMsg.getContent())) {
						topicMsgCounter++;						
					}
					else {
						failed("--- Wrong message content. Expected "+CONTENT+" found "+topicMsg.getContent());
					}
				}
				else {
					block();
				}
			}
		});
		
		// The behaviour triggering topic message emission
		SequentialBehaviour sb = new SequentialBehaviour(a);
		// Step 1: triggers the emission of a message about topic TOPIC_NAME
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit to be sure the registration takes effect...");
				super.onStart();
			}
			
			public void onWake() {
				log("--- Triggering emission of message about topic "+TOPIC_NAME);
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(emitter);
				msg.setContent(TOPIC_NAME+TopicMessageEmitterAgent.SEPARATOR+CONTENT);
				myAgent.send(msg);
			}
		} );
		
		// Step 5: Wait a bit to be sure all expected messages are received
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit to be sure all expected messages are received...");
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
		
		// Kill the first collector agent
		try {
			if (collector1 != null) {
				TestUtility.killAgent(a, collector1);
			}
		}
		catch (Exception e) {
			log("--- Error killing collector agent "+collector1.getLocalName());
			e.printStackTrace();
		}
		
		// Kill the remote container with the second collector agent
		if (jc != null) {
			jc.kill();
		}
	}
}
