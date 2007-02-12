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


import java.util.ArrayList;
import java.util.List;

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
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;


public class TestDFUnderStress extends Test {

	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_PROPS = 5;
	private static final int NUMBER_OF_SUBSCRIBERS = 10;
	private static final int NUMBER_OF_CONTAINERS = 3;
	private static final int NUMBER_OF_AGENTS = 100;
	protected static final String AGENT_START_CHAR = "a";
	protected static final String CONTAINER_START_CHAR = "c";
	public List<String> times = new ArrayList<String>();

	public Behaviour load(Agent a) throws TestException {
		//Creating Container with n (= class variable NUMBER_OF_SUBSCRIBERS)  agents subsribing to df
		TestUtility.launchJadeInstance("Container", null,
				"-container-name Subscribers -container -host "
				+ TestUtility.getContainerHostName(a, null) + " -port "
				+ Test.DEFAULT_PORT, new String[] {});
		try {
			for (int i = 0; i < NUMBER_OF_SUBSCRIBERS; ++i) {
				String agentName = new String("s" + i);
				// Create Agents
				TestUtility.createAgent(a,agentName,"test.domain.df.tests.TestDFUnderStress$SubscriberAgent",
						null, a.getAMS(), "Subscribers");
			}

		} catch (TestException te) {
			failed("Error creating agents: " + te);
			te.printStackTrace();
		}

		//tester agent registers to the df
		final DFAgentDescription template = new DFAgentDescription();
		try {
			DFService.register(a, template);
		} catch (FIPAException e) {
			failed("Error in registering tester agent to df: "+e);
			e.printStackTrace();
		}

		//tester agent subscribes itself to the df
		final DFAgentDescription subsTemplate = new DFAgentDescription();
		ACLMessage message = DFService.createSubscriptionMessage(a, a.getDefaultDF(), subsTemplate, null);
		final SubscriptionInitiator initiator = new SubscriptionInitiator(a, message) {
			private static final long serialVersionUID = 1L;
			public int counter = 0;
			private long startTime = 0;
			private long elapsedTime = 0;

			public void onStart() {
				System.out.println("Subscribing to the DF. A notification should immediately be received");
				super.onStart();
			}

			protected void handleInform(ACLMessage inform) {
				//System.out.println("Tester Agent: Notification received from DF");
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					if (results.length > 0) {
						//System.out.println("Length of result "+results.length);
						for (int i = 0; i < results.length; ++i) {
							DFAgentDescription dfd = results[i];
							//System.out.println("Dfd for TesterAgent "+dfd.getName());
							if(dfd.getName().getLocalName().startsWith(AGENT_START_CHAR)){
								if(counter == 0){
									startTime = System.currentTimeMillis();
									times.add(String.valueOf(startTime));
								}
								counter++; 
							}
						}
					}
					//Evaluating time of handling NUMBER_OF_CONTAINERS*NUMBER_OF_AGENTS INFORM messages from df) 
					if(counter == NUMBER_OF_CONTAINERS*NUMBER_OF_AGENTS){
						long endTime = System.currentTimeMillis();
						times.add(String.valueOf(endTime));
						elapsedTime = endTime - startTime;
						times.add(String.valueOf(elapsedTime));
						//Tester agent unsubscribes itself from df
						cancel(myAgent.getDefaultDF(), true);
					}						
				} catch (FIPAException fe) {
					failed("Error in handling INFORM messages incoming from df to tester agent "+fe);
					fe.printStackTrace();
				} 
			}
		};

		//Main behaviour... will be returned
		SequentialBehaviour sb = new SequentialBehaviour();
		
		//First parallel behaviour: on first side there is the behaviour for the tester agent subscribing to the df
		//on second side there is the creation of n containers (n=class variable NUMBER_OF_CONTAINERS) with m
		//agents each (m=class variable NUMBER_OF_AGENTS)
		ParallelBehaviour pb = new ParallelBehaviour(a,	ParallelBehaviour.WHEN_ALL);
		pb.addSubBehaviour(initiator);

		//Creating n containers (n=class variable NUMBER_OF_CONTAINERS) with m agents each (m=class variable NUMBER_OF_AGENTS)
		pb.addSubBehaviour(new OneShotBehaviour() {
			private static final long serialVersionUID = 1L;

			public void action() {
				try {
					for (int j = 0; j < NUMBER_OF_CONTAINERS; j++) {
						TestUtility.launchJadeInstance("Container", null,
								"-container-name "+CONTAINER_START_CHAR + j + " -container -host "+ TestUtility.getContainerHostName(myAgent, null)
								+ " -port " + Test.DEFAULT_PORT,new String[] {});
						for (int i = 0; i < NUMBER_OF_AGENTS; ++i) {
							String agentName = new String(AGENT_START_CHAR + j + i);
							TestUtility.createAgent(myAgent,agentName,"test.domain.df.tests.TestDFUnderStress$NormalAgent",
									null, myAgent.getAMS(), CONTAINER_START_CHAR + j);
						}
					}
				} catch (TestException e) {
					failed("Error creating containers and agents "+e);
					e.printStackTrace();
				}

			}

			public int onEnd() {
				//Sending message to each agent. Each agent in his startup has a blockingReceive() method
				for (int j = 0; j < NUMBER_OF_CONTAINERS; j++) {						
					for (int i = 0; i < NUMBER_OF_AGENTS; ++i) {
						String agentName = new String(AGENT_START_CHAR + j + i);
						AID aid = new AID(agentName,AID.ISLOCALNAME);
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(aid);
						myAgent.send(msg);
					}
				}
				return super.onEnd();
			}

		});

		sb.addSubBehaviour(pb);

		//Secoind parallel behaviour: on first side there is the behaviour for the tester agent subscribing againt 
		//to the df on second side there is the kill of n containers (n=class variable NUMBER_OF_CONTAINERS) with m
		//agents each (m=class variable NUMBER_OF_AGENTS). In the handleInform method of SubscribeInitiator behaviour
		//we will handle the only message informing tester agent of previous registrations and the 
		//NUMBER_OF_CONTAINERS*NUMBER_OF_AGENTS messages of the deregistration
		ParallelBehaviour pb1 = new ParallelBehaviour(a,ParallelBehaviour.WHEN_ALL);
		final DFAgentDescription secondSubsTemplate = new DFAgentDescription();
		ACLMessage secondMessage = DFService.createSubscriptionMessage(a, a.getDefaultDF(), secondSubsTemplate, null);
		pb1.addSubBehaviour(new SubscriptionInitiator(a,secondMessage){
			private static final long serialVersionUID = 1L;
			public int counter = 0;
			private long startTime = 0;
			private long elapsedTime = 0;
			private long endTime = 0;


			public void onStart() {
				System.out.println("Subscribing to the DF. A notification should immediately be received");
				super.onStart();
			}

			protected void handleInform(ACLMessage inform) {
				//System.out.println("Tester Agent: Notification received from DF Second registration ");
				int counterLocal = 0;
				long startLocal = 0 ,endLocal = 0, elapsedLocal = 0;
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					if (results != null && results.length > 0 && checkForServices(results)) {
						//One single message: registration phase
						for (int i = 0; i < results.length; ++i) {
							DFAgentDescription dfd = results[i];
							//System.out.println("Dfd for TesterAgent "+ dfd.getName());
							if (dfd.getName().getLocalName().startsWith(AGENT_START_CHAR)) {
								if (counterLocal == 0) {
									startLocal = System.currentTimeMillis();
									times.add(String.valueOf(startLocal));
								}
								counterLocal++;
							}
						}
						if (counterLocal == NUMBER_OF_CONTAINERS * NUMBER_OF_AGENTS) {
							endLocal =  System.currentTimeMillis();
							times.add(String.valueOf(endLocal));
							elapsedLocal = endLocal - startLocal;
							times.add(String.valueOf(elapsedLocal));
						}
					}else{
						//n different messagges; deregistration phase
						//System.out.println("Length of result " + results.length);
						for (int i = 0; i < results.length; ++i) {
							DFAgentDescription dfd = results[i];
							//System.out.println("Dfd for TesterAgent "+ dfd.getName());
							if (dfd.getName().getLocalName().startsWith(AGENT_START_CHAR)) {
								if (counter == 0) {
									startTime = System.currentTimeMillis();
									times.add(String.valueOf(startTime));
								}
								counter++;
							}
						}
						if (counter == NUMBER_OF_CONTAINERS * NUMBER_OF_AGENTS) {
							endTime = System.currentTimeMillis();
							times.add(String.valueOf(endTime));
							elapsedTime =  endTime - startTime;
							times.add(String.valueOf(elapsedTime));
							printTimes();
							passed("OK");
						}
						
					}					
				} catch (FIPAException fe) {
					failed("Error in handling INFORM messages incoming from df to tester agent "+fe);
					fe.printStackTrace();
				} 
			}

			//Print times taken during the test
			private void printTimes() {
				for(int i = 0; i<times.size(); i=i+3){
					System.out.println("---------TIMES"+i/3+"-----------");
					System.out.println("START:"+times.get(i));
					System.out.println("END:"+times.get(i+1));
					System.out.println("ELAPSED:"+times.get(i+2));
					System.out.println("-------------------------");
					System.out.println("-------------------------");
				}
				
			}

			//if dFAgentDescription doesn't have services we are during agent deregistration from df  
			private boolean checkForServices(DFAgentDescription[] results) {
				//System.out.println("Result size in checkForService : "+ results.length);
				for (int i = 0; i < results.length; ++i) {
					Iterator allServices = results[i].getAllServices();
					if (allServices.hasNext()) {
						return true;
					}
				}
				return false;
			}


		});

		//Kill containers with NUMBER_OF_AGENTS each
		pb1.addSubBehaviour(new WakerBehaviour(a,10000){
			private static final long serialVersionUID = 1L;

			protected void onWake() {
				try {
					for (int j = 0 ; j < NUMBER_OF_CONTAINERS ; j++){
						TestUtility.killContainer(myAgent, "c"+j);
					}
				} catch (TestException e) {
					failed("Error in kill Container");
					e.printStackTrace();
				}
				super.onWake();
			}

		});

		sb.addSubBehaviour(pb1);

		return sb;
	}

	//in clean method we kill container with subscribers agents.
	public void clean(Agent a) {
		try {
			TestUtility.killContainer(a, "Subscribers");
		} catch (TestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static class SubscriberAgent extends Agent {

		private static final long serialVersionUID = 1L;

		private Codec codec;

		protected void setup() {

			codec = new SLCodec();
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(
					FIPAManagementOntology.getInstance());
			try {
				DFService.register(this, getDefaultDF(), getDescription());
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			// Prepare the subscription message
			DFAgentDescription template = new DFAgentDescription();
			ACLMessage message = DFService.createSubscriptionMessage(this, getDefaultDF(), template, null);
			addBehaviour(new SubscriptionInitiator(this, message) {
				private static final long serialVersionUID = 1L;

				protected void handleInform(ACLMessage inform) {
					/*System.out.println("Agent " + getLocalName()
							+ ": Notification received from DF");*/
					try {
						DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
						if (results.length > 0) {
							for (int i = 0; i < results.length; ++i) {
								DFAgentDescription dfd = results[i];
								/*System.out.println("dfd for Agent "+getLocalName()+
										"dfd "+dfd.getName());*/
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

	public static class NormalAgent extends Agent {

		private static final long serialVersionUID = 1L;
		protected static final int NUMBER_OF_SEARCH = 2;

		private Codec codec;

		protected void setup() {
			
			blockingReceive();
			// Register language and ontology
			codec = new SLCodec();
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(
					FIPAManagementOntology.getInstance());
			try {
				DFService.register(this, getDefaultDF(), getDescription());
			} catch (FIPAException e) {
				System.out.println("Error in registering agents: "+e);
				e.printStackTrace();
			}
			addBehaviour(new OneShotBehaviour(this){

				private static final long serialVersionUID = 1L;

				public void action() {
					DFAgentDescription dfAgent = new DFAgentDescription();
					for (int i = 0; i < NUMBER_OF_SEARCH; i++) {
						try {
							DFService.search(myAgent, dfAgent);
						} catch (FIPAException e) {
							System.out.println("Error in searching in the df: "+e);
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
			sd.setType("DFStressNormalAgent");
			sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
			sd.addOntologies("fipa-agent-management");
			sd.setOwnership("JADE");
			for (int i = 0; i < NUMBER_OF_PROPS ; i++) {
				sd.addProperties(new Property("a"+i,"b"+i));
			}
			dfd.addServices(sd);
			return dfd;
		}
	}
}
