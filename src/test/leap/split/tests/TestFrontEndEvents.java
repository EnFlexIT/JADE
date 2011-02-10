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

import java.util.StringTokenizer;

import jade.core.Agent;
import jade.core.AID;
import jade.core.FEListener;
import jade.core.FEService;
import jade.core.MicroRuntime;
import jade.core.ServiceHelper;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.event.ContainerEvent;
import jade.core.event.JADEEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import test.common.*;

/**
 */
public class TestFrontEndEvents extends Test{
	private static final String HELPER_AGENT_NAME = "helper";
	private static final String DUMMY_AGENT_NAME = "dummy";
	
	private static final String CONV_ID = "XXXX";
	
	private String splitContainerName;
	private AID dummyAgent;
	

	public Behaviour load(Agent a) throws TestException {
		SequentialBehaviour sb = new SequentialBehaviour(a);

		//Step 1: Create a split container with 
		// - a service registering as FEListener and making the list of intercepted events available through its helper
		// - a helper agent responsible to inform the Tester about intercepted events
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- Creating split container...");
					JadeController jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -services test.leap.split.tests.TestFrontEndEvents$FEEventListenerService -agents "+HELPER_AGENT_NAME+":test.leap.split.tests.TestFrontEndEvents$HelperAgent"));
					log("--- Split-container created successfully !");
					splitContainerName = jc.getContainerName();
				}
				catch (Exception e) {
					failed("--- Error creating split-container. " + e);
					e.printStackTrace();
				}
			}
		} );

		//Step 2: Create a new Agent on the split container
		sb.addSubBehaviour(new WakerBehaviour(a, 2000){
			public void onWake(){
				try{
					log("--- Starting dummy agent on container " + splitContainerName);
					dummyAgent = TestUtility.createAgent(myAgent, DUMMY_AGENT_NAME, "jade.core.Agent", null, null, splitContainerName);
					log("--- Dummy agent correctly started");
				}
				catch(TestException e){
					failed("--- Error starting dummy agent. " + e);
					e.printStackTrace();
				}
			}
		});


		//Step 3: Kill the created agent 
		sb.addSubBehaviour(new WakerBehaviour(a, 2000){
			public void onWake(){
				try{
					log("--- Killing dummy agent");
					TestUtility.killAgent(myAgent, dummyAgent);
					log("--- Dummy agent correctly killed");
				}
				catch(TestException e){
					failed("--- Error killing dummy agent. " + e);
					e.printStackTrace();
				}
			}
		});

		//Step 4: Request the helper agent the counters of intercepted events: We expect 2 BORN_AGENTs (helper and dummy) and 1 DEAD_AGENT (dummy)
		sb.addSubBehaviour(new MsgReceiver(a, MessageTemplate.MatchConversationId(CONV_ID), -1, null, null) {
			public void onStart() {
				setDeadline(System.currentTimeMillis()+10000);
				log("--- Retrieving counters of BORN-AGENT and DEAD-AGENT events from the helper agent");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(new AID(HELPER_AGENT_NAME, AID.ISLOCALNAME));
				msg.setConversationId(CONV_ID);
				myAgent.send(msg);
			}

			protected void handleMessage(ACLMessage msg) {
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						log("--- Response received from helper agent. Content = "+msg.getContent());
						try {
							// We expect a content of the form "<n-born-agent-s> <n-dead-agent-s>"
							StringTokenizer st = new StringTokenizer(msg.getContent(), " ");
							String str = st.nextToken();
							int nBornAgents = Integer.parseInt(str);
							if (nBornAgents != 2) {
								failed("--- Wrong number of BORN-AGENT events. Found "+nBornAgents+" while 2 was expected");
								return;
							}
							str = st.nextToken();
							int nDeadAgents = Integer.parseInt(str);
							if (nDeadAgents != 1) {
								failed("--- Wrong number of DEAD-AGENT events. Found "+nDeadAgents+" while 1 was expected");
								return;
							}
							passed("--- Received 2 BORN-AGENT and 1 DEAD-AGENT events as expected");
						}
						catch (Exception e) {
							e.printStackTrace();
							failed("--- Unexpected error parsing helper agent response content: "+msg.getContent()+". "+e);
						}
					}
					else {
						failed("--- Unexpected message received. "+msg);
					}
				}
				else {
					failed("--- No response received from helper agent");
				}
			}
		});

		return sb;
	}


	public void clean(Agent a) {
		try {
			// Kill the split container
			TestUtility.killContainer(a, splitContainerName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}  


	/**
	 * Inner class HelperAgent
	 */
	public static class HelperAgent extends Agent {
		protected void setup() {
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					ACLMessage msg = myAgent.receive();
					if (msg != null) {
						ACLMessage reply = msg.createReply();
						try {
							FEEventListenerService.Helper h = (FEEventListenerService.Helper) getHelper("FE-Event-Listener");
							reply.setContent(h.getBornAgentCnt()+" "+h.getDeadAgentCnt());
							reply.setPerformative(ACLMessage.INFORM);
						}
						catch (Exception e) {
							reply.setPerformative(ACLMessage.FAILURE);
							reply.setContent(e.toString());
						}
						myAgent.send(reply);
					}
					else {
						block();
					}
				}
			} );
		}
	} // END of inner class HelperAgent
	
	
	/**
	 * Inner class FEEventListenerService
	 */
	public static class FEEventListenerService extends FEService implements FEListener {
		private int bornAgentCnt = 0;
		private int deadAgentCnt = 0;
		
		public FEEventListenerService() {
			// Register as FEListener
			System.out.println("Registering as FE-Listener");
			MicroRuntime.addListener(this);
		}
		
		@Override
		public ServiceHelper getHelper(Agent a) {
			return new Helper();
		}

		@Override
		public String getName() {
			return "FE-Event-Listener";
		}

		public void handleEvent(JADEEvent ev) {
			System.out.println("Handling event "+ev+" of type "+ev.getType());
			if (ev.getType() == ContainerEvent.BORN_AGENT) {
				bornAgentCnt++;
			}
			else if (ev.getType() == ContainerEvent.DEAD_AGENT) {
				deadAgentCnt++;
			}
		}
		
		
		/**
		 * Inner class Helper 
		 */
		private class Helper implements ServiceHelper {

			public void init(Agent a) {
			}
			
			public int getBornAgentCnt() {
				return bornAgentCnt;
			}
			
			public int getDeadAgentCnt() {
				return deadAgentCnt;
			}
		}  // END of inner class Helper
		
	}  // END of inner class FEEventListenerService
	
}
