package test.persDelivery.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

import jade.core.messaging.PersistentDeliveryFilter;

import test.common.*;
import test.common.testSuite.TestSuiteAgent;

/**
   Tests the basic feature of the JADE Persistent Delivery 
   service, i.e. the capability of storing certain messages whose intented 
   receiver does not exist and delivering them as soon as the receiver 
   starts.   
   @author Giovanni Caire - TILAB
 */
public class TestBasicPersistentDelivery extends Test {
	private static final String FORWARDER_NAME = "forwarder";
	private static final String PING_NAME = "ping";
	
	private static final String CONV_ID = "conv-id";
	private static final String MATCHING_ONTO = "onto1";
	private static final String NON_MATCHING_ONTO = "onto2";
	
	private JadeController jc;
	private AID forwarder, ping;
  
  public Behaviour load(Agent a) throws TestException {
  	enablePause(false);
  	
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 1: Start a container with the Persistent delivery service
		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					log("1) Starting container with the Persistent Delivery service...");
		  		String mainHost = TestUtility.getContainerHostName(myAgent, TestSuiteAgent.mainController.getContainerName());
					jc = TestUtility.launchJadeInstance("Pers-delivery", null, " -container -services jade.core.messaging.PersistentDeliveryService;jade.core.event.NotificationService -persistent-delivery-filter test.persDelivery.tests.TestBasicPersistentDelivery$TestFilter -persistent-delivery-sendfailureperiod 10000 -host "+mainHost+" -port "+Test.DEFAULT_PORT, null);
					log("Container with the Persistent Delivery service correctly started");
  			}
  			catch (Exception e) {
  				failed("Error starting container with the Persistent Delivery service");
  			}
  		}
  	} );
  	
		// Step 2: Start the forwarder and ping agent
		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					log("2) Starting Forwarder agent...");
					forwarder = TestUtility.createAgent(myAgent, FORWARDER_NAME, "test.persDelivery.tests.TestBasicPersistentDelivery$ForwarderAgent", new String[]{myAgent.getLocalName(), PING_NAME}, null, jc.getContainerName());
					log("Forwarder agent correctly started");
  			}
  			catch (Exception e) {
  				failed("Error starting Forwarder agent");
  			}
  		}
  	} );
  	
		// Step 3: Send a "persistent" message (to the Ping agent) through the Forwarder agent
  	// Then wait for the reply for a while: nothing should be received
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+10000);
				log("3) Sending \"persistent\" message...");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(forwarder);
				msg.setConversationId(CONV_ID);
				msg.setOntology(MATCHING_ONTO);
				myAgent.send(msg);
  		}
  		
  		protected void handleMessage(ACLMessage msg) {
  			if (msg == null) {
  				log("\"Persistent\" message correctly stored. No reply received as expected");
  			}
  			else {
  				failed("\"Persistent\" message not stored. Unexpected reply received");
  			}
  		}
  	} );
  	
		// Step 4: Send a "non-persistent" message (to the Ping agent) through the Forwarder agent
  	// Then wait for the reply for a while: a FAILURE should immediately be received
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+10000);
				log("4) Sending \"non-persistent\" message...");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(forwarder);
				msg.setConversationId(CONV_ID);
				msg.setOntology(NON_MATCHING_ONTO);
				myAgent.send(msg);
  		}
  		
  		protected void handleMessage(ACLMessage msg) {
  			if (msg != null) {
  				if (msg.getPerformative() == ACLMessage.FAILURE) {
	  				log("\"Non-persistent\" message correctly handled. FAILURE notification received as expected");
  				}
  				else {
  					failed("Unexpected reply to \"non-persistent\" message"+msg);
  				}
  			}
  			else {
  				failed("\"Non-persistent\" message handling error. No failure notification received");
  			}
  		}
  	} );
  
		// Step 5: Start the PingAgent. Then wait for the reply to the 
  	// "persistent-message" that should be delivered 
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+10000);
				try {
					log("5) Starting Ping agent...");
					ping = TestUtility.createAgent(myAgent, PING_NAME, "test.persDelivery.tests.TestBasicPersistentDelivery$PingAgent", null);
					log("Ping agent correctly started");
  			}
  			catch (Exception e) {
  				failed("Error starting Ping agent");
  			}
  		}
  		
  		protected void handleMessage(ACLMessage msg) {
  			if (msg != null) {
  				if (msg.getPerformative() == ACLMessage.INFORM) {
	  				passed("Reply to \"persistent\" message correctly received.");
  				}
  				else {
  					failed("Unexpected reply to \"persistent\" message"+msg);
  				}
  			}
  			else {
  				failed("\"Persistent\" message delivery error. No reply received");
  			}
  		}
  	} );
  	
  	return sb;
  }
  
  public void clean(Agent a) {
  	try {
  		jc.kill();
  		TestUtility.killAgent(a, ping);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  	
  
  /**
     Inner class ForwarderAgent
   */
  public static class ForwarderAgent extends Agent {
  	AID tester = null;
  	AID receiver = null;
  	
  	protected void setup() {
  		Object[] args = getArguments();
  		if (args != null && args.length > 1) {
  			tester = new AID((String) args[0], AID.ISLOCALNAME);
  			receiver = new AID((String) args[1], AID.ISLOCALNAME);
  			
	  		addBehaviour(new CyclicBehaviour(this) {
	  			public void action() {
	  				ACLMessage msg = myAgent.receive();
	  				if (msg != null) {
	  					if (msg.getSender().equals(tester)) {
	  						// Forward the message to the receiver;
	  						msg.setSender(myAgent.getAID());
	  						msg.clearAllReceiver();
	  						msg.addReceiver(receiver);
	  						myAgent.send(msg);
	  					}
	  					else {
	  						// Forward the message to the tester
	  						msg.setSender(myAgent.getAID());
	  						msg.clearAllReceiver();
	  						msg.addReceiver(tester);
	  						myAgent.send(msg);
	  					}
	  				}
	  				else {
	  					block();
	  				}
	  			}
	  		} );
  		}
  		else {
  			System.out.println("No tester/receiver name specified");
  		}
  	}
  } // END of inner class ForwarderAgent
  
  /**
     Inner class PingAgent
   */
  public static class PingAgent extends Agent {
  	protected void setup() {
  		addBehaviour(new CyclicBehaviour(this) {
  			public void action() {
  				ACLMessage msg = myAgent.receive();
  				if (msg != null) {
  					ACLMessage reply = msg.createReply();
  					reply.setPerformative(ACLMessage.INFORM);
  					myAgent.send(reply);
  				}
  				else {
  					block();
  				}
  			}
  		} );
  	}
  } // END of inner class PingAgent
  
  /**
     Inner class TestFilter
   */
  public static class TestFilter implements PersistentDeliveryFilter {
    public long delayBeforeExpiration(ACLMessage msg) {
    	if (MATCHING_ONTO.equals(msg.getOntology())) {
    		return NEVER;
    	}
    	else {
    		return NOW;
    	}
    }
  } // END of inner class TestFilter
}
