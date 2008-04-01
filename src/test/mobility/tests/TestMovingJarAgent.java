package test.mobility.tests;

import test.common.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.mobility.MobilityOntology;
import jade.domain.mobility.MobileAgentDescription;
import jade.domain.mobility.MoveAction;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TestMovingJarAgent extends Test {
	private AID movingJarAgent, movingJarAgent2;
	
	public Behaviour load(Agent a) throws TestException {
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 1) Create an agent from a separate Jar file using the agent-class --> jar name convention
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- 1) Create an agent from a separate Jar file using the agent-class --> jar-name convention");
					movingJarAgent = TestUtility.createAgent(myAgent, "dummy", "test.mobility.separate.dummy.DummyAgent", new Object[]{myAgent.getAID()});
				}
				catch (TestException te) {
					failed("--- ERROR creating an agent from a separate Jar file. "+te);
					te.printStackTrace();					
				}
			}
		});
		
		// Step 2) Wait for the notification about the correct loading of a dummy resource from the separate jar file
		sb.addSubBehaviour(new SimpleBehaviour(a) {
			private boolean finished = false;
			public void onStart() {
				log("--- 2) Wait for the notification about the correct loading of a dummy resource from the separate jar file");
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(movingJarAgent));
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						log("--- Dummy resource correctly loaded from separate jar file");
					}
					else {
						failed("--- "+msg.getContent());
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
		
		// Step 3) Move the jar agent to the Main Container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- 3) Move the jar agent to the Main Container");
					MobileAgentDescription dsc = new MobileAgentDescription();
					dsc.setName(movingJarAgent);
					dsc.setDestination(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
					MoveAction moveAct = new MoveAction();
					moveAct.setMobileAgentDescription(dsc);
					TestUtility.requestAMSAction(myAgent, null, moveAct, MobilityOntology.NAME);
					log("--- Jar agent successfully moved");
				}
				catch (TestException te) {
					failed("--- ERROR moving the jar agent to the Main Container. "+te);
					te.printStackTrace();					
				}
			}
		});
		
		// Step 4) Create a second agent from a separate Jar file specifying an explicit jar-name  
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- 4) Create a second agent from a separate Jar file specifying an explicit jar-name");
					movingJarAgent2 = TestUtility.createAgent(myAgent, "dummy2", "test.mobility.separate.jar.JarAgent[code=testJarAgent.jar]", null);
				}
				catch (TestException te) {
					failed("--- ERROR creating a second agent from a separate Jar file. "+te);
					te.printStackTrace();					
				}
			}
		});
		
		// Step 5) Move the second jar agent to the Main Container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- 5) Move the second jar agent to the Main Container");
					MobileAgentDescription dsc = new MobileAgentDescription();
					dsc.setName(movingJarAgent2);
					dsc.setDestination(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
					MoveAction moveAct = new MoveAction();
					moveAct.setMobileAgentDescription(dsc);
					TestUtility.requestAMSAction(myAgent, null, moveAct, MobilityOntology.NAME);
					passed("--- Second jar agent successfully moved");
				}
				catch (TestException te) {
					failed("--- ERROR moving the second jar agent to the Main Container. "+te);
					te.printStackTrace();					
				}
			}
		});
		
		return sb;
	}
	
	public void clean(Agent a) {
		if (movingJarAgent != null) {
			try {
				TestUtility.killAgent(a, movingJarAgent);
			}
			catch (TestException te) {
				te.printStackTrace();
			}
		}
		if (movingJarAgent2 != null) {
			try {
				TestUtility.killAgent(a, movingJarAgent2);
			}
			catch (TestException te) {
				te.printStackTrace();
			}
		}
	}
}
