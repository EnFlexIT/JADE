package test.leap.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import test.common.*;
import test.leap.LEAPTesterAgent;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
   This Test addresses the basic operations of life-cycle management (agent creation/killing) 
   and communication (message exchange) in a wireless device.   
   @author Giovanni Caire - TILAB
 */
public class TestLCCommunication extends Test {
	private static final String PING_AGENT = "ping";
	private static final String CONV_ID = "ping_conv";
	
	private String lightContainerName = "Container-1";
	private AID ping;
	
  public Behaviour load(final Agent a) throws TestException {
		// MIDP container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);

		final SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 1: Create the ping agent on the light container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					ping = TestUtility.createAgent(a, PING_AGENT, "test.leap.midp.PingAgent", null, a.getAMS(), lightContainerName);
					log("Ping agent correctly created.");
				}
				catch (Exception e) {
					e.printStackTrace();
					failed("Error creating Ping agent.");
				}
			}
		} );
		// Step 2: Send a message to the ping agent and gets the reply
		sb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(ping);
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
				log("Message 1 sent to Ping agent.");
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (msg != null) {
					if (msg.getSender().equals(ping)) {
						log("Reply from Ping agent correctly received.");
					}
					else {
						failed("Unexpected reply received: "+msg);
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
		} );
		// Step 3: Kill the ping agent on the light container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					TestUtility.killAgent(a, ping);
					log("Ping agent correctly killed.");
				}
				catch (Exception e) {
					e.printStackTrace();
					failed("Error killing Ping agent.");
				}
			}
		} );
		// Step 4: Send a message to the ping agent and gets the FAILURE from the AMS
		sb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			
			public void onStart() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(ping);
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
				log("Message 2 sent to Ping agent.");
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
				if (msg != null) {
					if (msg.getSender().equals(myAgent.getAMS()) && msg.getPerformative() == ACLMessage.FAILURE) {
						passed("FAILURE notification from AMS correctly received.");
					}
					else {
						failed("Unexpected reply received: "+msg);
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
		} );

  	return sb;
  }
}

