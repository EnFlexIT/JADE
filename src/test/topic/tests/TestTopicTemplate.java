package test.topic.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;
import test.topic.TopicMessageEmitterAgent;

public class TestTopicTemplate extends Test {
	private static final String TOPIC_NAME = "foo.bar";
	private static final String CONTENT = "Hello World!!";
	private AID emitter;
	private int topicMsgCounter = 0;
	private TopicManagementHelper topicHelper;
	private AID topicTemplate;
	
	public Behaviour load(Agent a) throws TestException {
		log("--- Creating Topic-message-emitter Agent...");
		emitter = TestUtility.createAgent(a, "emitter", "test.topic.TopicMessageEmitterAgent", null);
		
		try {
			log("--- Retrieving topic management helper...");
			topicHelper = (TopicManagementHelper) a.getHelper(TopicManagementHelper.SERVICE_NAME);
			topicTemplate = topicHelper.createTopic(TOPIC_NAME+'.'+TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD);
		}
		catch (Exception e) {
			throw new TestException("Error retrieving TopicManagementHelper", e);
		}
		
		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY) {
			public int onEnd() {
				if (topicMsgCounter == 2) {
					passed("--- 2 messages matching topic template "+topicTemplate.getLocalName()+" received as expected");
				}
				else {
					failed("--- "+topicMsgCounter+" message(s) matching topic template "+topicTemplate.getLocalName()+" received while 2 were expected");
				}
				return super.onEnd();
			}
		};
		
		// The behaviour collecting topic messages
		pb.addSubBehaviour(new CyclicBehaviour(a) {
			public void action() {
				ACLMessage topicMsg = myAgent.receive(MessageTemplate.MatchTopic(topicTemplate));
				if (topicMsg != null) {
					log("--- Message about topic "+topicTemplate.getLocalName()+" received");
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
		
		// The behaviour registering to the topicTemplate and triggering topic messages emission
		SequentialBehaviour sb = new SequentialBehaviour(a);
		// Step 1: registers to topicTemplate
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- Registering to topic template "+topicTemplate.getLocalName());
					topicHelper.register(topicTemplate);
				}
				catch (Exception e) {
					failed("--- Error registering to topic template "+topicTemplate.getLocalName()+". "+e);
				}
			}
		} );
		
		// Step 2: triggers the emission of a message about topic TOPIC_NAME (wait a bit to be sure the registration takes effect)
		sb.addSubBehaviour(new WakerBehaviour(a, 1000) {
			public void onWake() {
				log("--- Triggering emission of message about topic "+TOPIC_NAME);
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(emitter);
				msg.setContent(TOPIC_NAME+TopicMessageEmitterAgent.SEPARATOR+CONTENT);
				myAgent.send(msg);
			}
		} );
		
		// Step 3: triggers the emission of a message about topic TOPIC_NAME.sub1 (should match)
		sb.addSubBehaviour(new WakerBehaviour(a, 1000) {
			public void onWake() {
				String name = TOPIC_NAME+".sub1";
				log("--- Triggering emission of message about topic "+name);
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(emitter);
				msg.setContent(name+TopicMessageEmitterAgent.SEPARATOR+CONTENT);
				myAgent.send(msg);
			}
		} );
		
		// Step 4: triggers the emission of a message about topic TOPIC_NAMEsub1 (should not match: NOTE that there is no '.')
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				String name = TOPIC_NAME+"sub1";
				log("--- Triggering emission of message about topic "+name);
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(emitter);
				msg.setContent(name+TopicMessageEmitterAgent.SEPARATOR+CONTENT);
				myAgent.send(msg);
			}
		} );
		
		// Step 5: de-registers from topic template 
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit before deregistering...");
				super.onStart();
			}
			
			public void onWake() {
				try {
					log("--- De-registering from topic template "+topicTemplate.getLocalName());
					topicHelper.deregister(topicTemplate);
				}
				catch (Exception e) {
					failed("--- Error de-registering from topic template "+topicTemplate.getLocalName()+". "+e);
				}
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
