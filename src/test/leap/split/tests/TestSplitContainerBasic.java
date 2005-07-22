package test.leap.split.tests;

/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import test.common.*;

/**
 * The test start a Leap split-container, then starts a PingAgent on it and 
 * verifies if messages are successfully sent and received by the agent.
 * The test is successfully extecuted if the agent is exists and can be be properly killed,
 * and the split-container is successfully killed.
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */

public class TestSplitContainerBasic extends Test{

	private JadeController jc;
	static final String PING_NAME = "ping";
	String containerName;
	AID pingAgent;
	Agent myAgent;
	
	private static final String CONV_ID = "conv-id";

	public Behaviour load(Agent a) throws TestException {
		
		myAgent = a;
		
		SequentialBehaviour sb = new SequentialBehaviour(a);

		//Step 1: Initialization phase
		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					containerName = createSplitContainer();
  			}
  			catch (Exception e) {
  				failed("Error initilizing split-container. " + e);
  				e.printStackTrace();
  			}
  		}
  	} );
		
		//Step 2: create PingAgent via AMS.
		sb.addSubBehaviour(new OneShotBehaviour(a){
			public void action(){
				try{
					log("Starting ping agent on " + containerName);
					pingAgent = TestUtility.createAgent(myAgent, PING_NAME, "test.leap.split.tests.TestSplitContainerBasic$PingAgent", null, null, containerName);
					log("Ping agent correctly started");
				}
				catch(TestException e){
					failed("Error starting Ping agent. " + e);
					e.printStackTrace();
				}
			}
		});
		
		
		//Step 3: Send a message (to the PingAgent) 
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+10000);
				log("Sending message to ping agent...");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(pingAgent);
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
  		}
  		
  		protected void handleMessage(ACLMessage msg) {
  			if (msg != null) {
  				if (msg.getPerformative() == ACLMessage.INFORM) {
	  				log("Response received from ping agent: " + msg);
  				}
  				else {
  					failed("FAILURE notification received. " + msg);
  				}
  			}
  			else {
  				failed("No response received from ping agent");
  			}
  		}
  	});
		
		//Step 4: kill ping agent
		sb.addSubBehaviour(new OneShotBehaviour(){
			public void action(){
				log("Killing ping agent...");
				try{
					TestUtility.killAgent(myAgent, pingAgent);
				}catch(Exception e){
					failed("Error in killing ping agent. " + e);
					e.printStackTrace();
				}
			}
		});
		
		
		//Step 5: Send a message to the PingAgent. 
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+10000);
				log("Sending message to ping agent...");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(pingAgent);
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
  		}
  		
  		protected void handleMessage(ACLMessage msg) {
  			if (msg != null) {
  				if (msg.getPerformative() == ACLMessage.INFORM) {
  					//the ping agent is dead. No response must be received.
	  				failed("Response received from ping agent. The ping agent is not dead as expected. " + msg);
  				}
  				else {
  					log("Failure notification received as expected. Ping agent successfully killed.");
  				}
  			}
  			else {
  				log("No response received from ping agent");
  			}
  		}
  	});
		
		
		//Step 6: kill split container.
		sb.addSubBehaviour(new OneShotBehaviour(){
			public void action(){
				try{
					log("Killing split-container");
					TestUtility.killContainer(myAgent, containerName);
				}catch(TestException te){
					failed("Error in killing split-container.");
					te.printStackTrace();
				}
				//check if the container has been successfully ended.
				try{
					log("Retry killing split-container...");
					TestUtility.killContainer(myAgent, containerName);
				}catch(TestException te){
					log("Exception occured as expected. " + te);
					passed("Split-container successfully killed the first time.");
				}
			}
		});
		
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
	 * Override this method to initialize the environment for the test.
	 * @return name the name of the split-container created.
	 * @throws TestException
	 */
	protected String createSplitContainer() throws TestException{
		log("Creating split container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)));
		log("split-container created successfully !");
		return jc.getContainerName();
	}
	
	
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
}//END of TestSplitContainerBasic
