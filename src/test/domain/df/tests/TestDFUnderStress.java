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

package test.domain.df.tests;


import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.List;

import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;


public class TestDFUnderStress extends Test {

	private static final long serialVersionUID = 1L;
	
	private static final int NUMBER_OF_PROPS = 5;
	private static final int NUMBER_OF_SUBSCRIBERS = 10;
	private static final int NUMBER_OF_CONTAINERS = 3;
	private static final int NUMBER_OF_AGENTS = 100;
	private static final String AGENT_PREFIX = "a-";
	private static final String CONTAINER_PREFIX = "c-";
	private static final String SUBSCRIBERS_CONTAINER = "Subscribers-Container";
	private static final String AGENT_NAME_PROP =  "agent-name";
	
	
	private long startTime;
	private int totAgents = NUMBER_OF_CONTAINERS * NUMBER_OF_AGENTS;
	private List summary = new ArrayList();

	public Behaviour load(Agent a) throws TestException {
		String mainHost = TestUtility.getContainerHostName(a, null);
		// Create a container with some agents subscribing to the DF
		log("--- Starting container for subscriber agents...");
		TestUtility.launchJadeInstance(SUBSCRIBERS_CONTAINER, null, "-container-name "+SUBSCRIBERS_CONTAINER+" -container -host " + mainHost + " -port " + Test.DEFAULT_PORT, new String[] {});
		log("--- Starting "+NUMBER_OF_SUBSCRIBERS+" subscriber agents...");
		for (int i = 0; i < NUMBER_OF_SUBSCRIBERS; ++i) {
			String agentName = "s-" + i;
			TestUtility.createAgent(a,agentName,"test.domain.df.tests.TestDFUnderStress$SubscriberAgent", null, a.getAMS(), SUBSCRIBERS_CONTAINER);
		}

		for (int j = 0; j < NUMBER_OF_CONTAINERS; j++) {
			String containerName = CONTAINER_PREFIX+j;
			log("--- Starting container "+containerName+"...");
			TestUtility.launchJadeInstance(containerName, null, "-container-name "+containerName + " -container -host "+ mainHost + " -port " + Test.DEFAULT_PORT, new String[] {});
			log("--- Starting "+NUMBER_OF_AGENTS+" registering agents...");
			for (int i = 0; i < NUMBER_OF_AGENTS; ++i) {
				String agentName = AGENT_PREFIX + j +"-"+ i;
				TestUtility.createAgent(a, agentName, "test.domain.df.tests.TestDFUnderStress$RegisteringAgent", null, a.getAMS(), containerName);
			}
		}
		
		// Main test behaviour
		SequentialBehaviour main = new SequentialBehaviour();
		

		// Step 1) Subscribe to the DF with a null template and collect notifications about all NEW registrations. In parallel, wait a bit to give all 
		// registering agents enough time to startup and then send them the startup message
		ParallelBehaviour step1 = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ALL) {
			public void onStart() {
				log("--- Step 1:");
			}
		};
		
		DFAgentDescription subsTemplate = new DFAgentDescription();
		ACLMessage subs1 = DFService.createSubscriptionMessage(a, a.getDefaultDF(), subsTemplate, null);
		step1.addSubBehaviour(new SubscriptionInitiator(a, subs1) {
			private static final long serialVersionUID = 1L;
			public int counter = 0;

			public void onStart() {
				log("--- Subscribing to the DF");
				super.onStart();
			}

			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
						if(dfd.getName().getLocalName().startsWith(AGENT_PREFIX) && isRegistration(dfd)){
							counter++; 
							if (counter % 10 == 0) {
								log("--- # "+counter+" registration notifications received");
							}
						}
						else {
							failed("--- Unexpected notification received: agent = "+dfd.getName().getLocalName()+", is-registration = "+isRegistration(dfd));
						}
					}
					
					if (counter == totAgents){
						long elapsedTime = System.currentTimeMillis() - startTime;
						log("--- All expected registration notifications received in "+elapsedTime+" ms");
						summary.add("Step1 time = "+elapsedTime+" ms");
						cancel(myAgent.getDefaultDF(), true);
					}						
				} 
				catch (FIPAException fe) {
					failed("--- Error handling INFORM messages from df. "+fe);
					fe.printStackTrace();
				} 
			}
		} );

		step1.addSubBehaviour(new WakerBehaviour(a, 10000) {
			public void onStart() {
				log("--- Wait a bit before sending the startup message...");
				super.onStart();
			}
			
			public void onWake() {
				log("--- Issuing startup message");
				startTime = System.currentTimeMillis();
				for (int j = 0; j < NUMBER_OF_CONTAINERS; j++) {						
					for (int i = 0; i < NUMBER_OF_AGENTS; ++i) {
						String agentName = AGENT_PREFIX + j +"-"+ i;
						AID aid = new AID(agentName,AID.ISLOCALNAME);
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(aid);
						myAgent.send(msg);
					}
				}
			}
		});
		
		main.addSubBehaviour(step1);
		
		
		// Step 2) Wait a bit then subscribe again with a null template and collect notifications about all OLD registrations
		SequentialBehaviour step2 = new SequentialBehaviour(a) {
			public void onStart() {
				log("--- Step 2:");
			}
		};
		
		step2.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("--- Wait a bit before subscribing again...");
				super.onStart();
			}
			
			public void onWake() {
				// Just do nothing
			}
		});
		
		ACLMessage subs2 = DFService.createSubscriptionMessage(a, a.getDefaultDF(), subsTemplate, null);
		step2.addSubBehaviour(new SubscriptionInitiator(a, subs2) {
			private static final long serialVersionUID = 2L;
			public int counter = 0;

			public void onStart() {
				log("--- Subscribing to the DF");
				startTime = System.currentTimeMillis();
				super.onStart();
			}

			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
						if(dfd.getName().getLocalName().startsWith(AGENT_PREFIX) && isRegistration(dfd)){
							counter++; 
							if (counter % 10 == 0) {
								log("--- # "+counter+" registration notifications received");
							}
						}
						else {
							failed("--- Unexpected notification received: agent = "+dfd.getName().getLocalName()+", is-registration = "+isRegistration(dfd));
						}
					}
					
					if (counter == totAgents){
						long elapsedTime = System.currentTimeMillis() - startTime;
						log("--- All expected registration notifications received in "+elapsedTime+" ms");
						summary.add("Step2 time = "+elapsedTime+" ms");
						cancel(myAgent.getDefaultDF(), true);
					}						
				} 
				catch (FIPAException fe) {
					failed("--- Error handling INFORM messages from df. "+fe);
					fe.printStackTrace();
				} 
			}
		} );
		
		main.addSubBehaviour(step2);
		
		
		// Step 3) Subscribe once more to the DF with a null template and collect notifications about de-registrations. 
		// In parallel, wait a bit and then kill all containers with registering agents
		ParallelBehaviour step3 = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ALL) {
			public void onStart() {
				log("--- Step 3:");
			}			
		};
		
		ACLMessage subs3 = DFService.createSubscriptionMessage(a, a.getDefaultDF(), subsTemplate, null);
		step3.addSubBehaviour(new SubscriptionInitiator(a, subs3) {
			private static final long serialVersionUID = 3L;
			public int counter = 0;

			public void onStart() {
				log("--- Subscribing to the DF");
				super.onStart();
			}

			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
						if(dfd.getName().getLocalName().startsWith(AGENT_PREFIX) && !isRegistration(dfd)){
							counter++; 
							if (counter % 10 == 0) {
								log("--- # "+counter+" de-registration notifications received ");
							}
						}
						else {
							// Just do nothing: Here we receive notifications about all current registrations again
						}
					}
					
					if (counter == totAgents){
						long elapsedTime = System.currentTimeMillis() - startTime;
						log("--- All expected de-registration notifications received in "+elapsedTime+" ms");
						summary.add("Step3 time = "+elapsedTime+" ms");
						cancel(myAgent.getDefaultDF(), true);
						passed("--- Test successful. Summary: "+summary);
					}						
				} 
				catch (FIPAException fe) {
					failed("--- Error handling INFORM messages from df. "+fe);
					fe.printStackTrace();
				} 
			}
		} );

		step3.addSubBehaviour(new WakerBehaviour(a, 10000) {
			public void onStart() {
				log("--- Wait a bit before killing containers with registering agents...");
				super.onStart();
			}
			
			protected void onWake() {
				log("--- Killing containers with registering agents");
				startTime = System.currentTimeMillis();
				for (int j = 0 ; j < NUMBER_OF_CONTAINERS ; j++){
					String containerName = CONTAINER_PREFIX+j;
					try {
						TestUtility.killContainer(myAgent, containerName);
						log("--- Container "+containerName+" killed");
					} catch (TestException e) {
						failed("--- Error killing container "+containerName);
						e.printStackTrace();
					}
				}
			}
		});
		
		main.addSubBehaviour(step3);

		return main;
	}

	//in clean method we kill container with subscribers agents.
	public void clean(Agent a) {
		try {
			TestUtility.killContainer(a, SUBSCRIBERS_CONTAINER);
		} catch (TestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isRegistration(DFAgentDescription dfd) {
		Iterator it = dfd.getAllServices();
		return it.hasNext();
	}


	public static class SubscriberAgent extends Agent {

		private static final long serialVersionUID = 1L;

		private Codec codec;

		protected void setup() {

			codec = new SLCodec();
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(FIPAManagementOntology.getInstance());
			
			// Prepare the subscription message
			DFAgentDescription template = new DFAgentDescription();
			ACLMessage message = DFService.createSubscriptionMessage(this, getDefaultDF(), template, null);
			addBehaviour(new SubscriptionInitiator(this, message) {
				private static final long serialVersionUID = 1L;

				protected void handleInform(ACLMessage inform) {
					try {
						DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
						if (results.length > 0) {
							for (int i = 0; i < results.length; ++i) {
								DFAgentDescription dfd = results[i];
							}
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			});
		}

		private DFAgentDescription getDescription() {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName(getLocalName() + "-Service");
			sd.setType("DFStressSubscriberAgent");
			sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
			sd.addOntologies("fipa-agent-management");
			sd.setOwnership("JADE");
			dfd.addServices(sd);
			return dfd;
		}
	}

	public static class RegisteringAgent extends Agent {

		private static final long serialVersionUID = 1L;
		protected static final int NUMBER_OF_SEARCH = 2;

		private Codec codec;

		protected void setup() {
			// Register language and ontology
			codec = new SLCodec();
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(FIPAManagementOntology.getInstance());
			
			blockingReceive();
			System.out.println("Agent "+getLocalName()+" - startup message received");
			try {
				DFService.register(this, getDefaultDF(), getDescription());
			} catch (FIPAException e) {
				System.out.println("Error in registering agents: "+e);
				e.printStackTrace();
			}
			addBehaviour(new OneShotBehaviour(this){

				private static final long serialVersionUID = 1L;

				public void action() {
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.addProperties(new Property(AGENT_NAME_PROP, myAgent.getLocalName()));
					template.addServices(sd);
					
					for (int i = 0; i < NUMBER_OF_SEARCH; i++) {
						try {
							DFAgentDescription[] dfds = DFService.search(myAgent, template);
							if (dfds.length != 1) {
								System.out.println("Agent "+getLocalName()+" - unexpected DF search result: found "+dfds.length+" items(s) while 1 was expected");
							}
							else if (!dfds[0].getName().equals(myAgent.getAID())) {
								System.out.println("Agent "+getLocalName()+" - unexpected DF search result: found agent "+dfds[0].getName().getLocalName()+" while "+myAgent.getAID().getLocalName()+" was expected");
							}				 
						} catch (FIPAException e) {
							System.out.println("Agent "+getLocalName()+" - error searching in the df: "+e);
							e.printStackTrace();
						}
					}
				}

			});
			//System.out.println("Agent "+this.getName()+" correctly started");

		}

		private DFAgentDescription getDescription() {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName(getLocalName() + "-Service");
			sd.setType("DFStressRegisteringAgent");
			sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
			sd.addOntologies("fipa-agent-management");
			sd.setOwnership("JADE");
			sd.addProperties(new Property(AGENT_NAME_PROP, getLocalName()));
			for (int i = 0; i < NUMBER_OF_PROPS ; i++) {
				sd.addProperties(new Property("a"+i,"b"+i));
			}
			dfd.addServices(sd);
			return dfd;
		}
	}
}
