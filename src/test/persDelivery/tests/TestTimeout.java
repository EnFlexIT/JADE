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
   Tests the case of a message that is stored for a limited
   amount of time.   
   @author Giovanni Caire - TILAB
 */
public class TestTimeout extends Test {
	private static final String FORWARDER_NAME = "forwarder";
	private static final String NOBODY_NAME = "nobody";
	
	private static final String CONV_ID = "conv-id";
	private static final String MATCHING_ONTO = "onto1";
	
	private JadeController jc;
	private AID forwarder;
  
  public Behaviour load(Agent a) throws TestException {
  	enablePause(false);
  	
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 1: Start a container with the Persistent delivery service
		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					log("1) Starting container with the Persistent Delivery service...");
		  		String mainHost = TestUtility.getContainerHostName(myAgent, TestSuiteAgent.mainController.getContainerName());
					jc = TestUtility.launchJadeInstance("Pers-delivery", null, " -container -services jade.core.messaging.PersistentDeliveryService;jade.core.event.NotificationService -persistent-delivery-filter test.persDelivery.tests.TestTimeout$TestFilter -persistent-delivery-sendfailureperiod 10000 -host "+mainHost+" -port "+Test.DEFAULT_PORT, null);
					log("Container with the Persistent Delivery service correctly started");
					pause();
  			}
  			catch (Exception e) {
  				failed("Error starting container with the Persistent Delivery service");
  			}
  		}
  	} );
  	
		// Step 2: Start the forwarder agent
		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					log("2) Starting Forwarder agent...");
					forwarder = TestUtility.createAgent(myAgent, FORWARDER_NAME, "test.persDelivery.tests.ForwarderAgent", new String[]{myAgent.getLocalName(), NOBODY_NAME}, null, jc.getContainerName());
					log("Forwarder agent correctly started");
					pause();
  			}
  			catch (Exception e) {
  				failed("Error starting Forwarder agent");
  			}
  		}
  	} );
  	
		// Step 3: Send a "persistent" message (to nobody) through the Forwarder agent
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
	  			pause();
  			}
  			else {
  				failed("\"Persistent\" message not stored. Unexpected reply received");
  			}
  		}
  	} );
  	
		// Step 4: Wait some more time: a FAILURE should be received when the storage timeout expires
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+60000);
				log("4) Wait some more time...");
  		}
  		
  		protected void handleMessage(ACLMessage msg) {
  			if (msg != null) {
  				if (msg.getPerformative() == ACLMessage.FAILURE) {
	  				passed("FAILURE notification received after the storage timeout as expected");
  				}
  				else {
  					failed("Unexpected reply received"+msg);
  				}
  			}
  			else {
  				failed("\"Persistent\" message handling error. No failure notification received after storage timeout");
  			}
  		}
  	} );
  
  	return sb;
  }
  
  public void clean(Agent a) {
  	try {
  		jc.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  	
  
  /**
     Inner class TestFilter
   */
  public static class TestFilter implements PersistentDeliveryFilter {
    public long delayBeforeExpiration(ACLMessage msg) {
    	if (MATCHING_ONTO.equals(msg.getOntology())) {
    		System.out.println("\n\nTime is "+System.currentTimeMillis()+" Storing message "+msg);
    		return 20000;
    	}
    	else {
    		return NOW;
    	}
    }
  } // END of inner class TestFilter
}
