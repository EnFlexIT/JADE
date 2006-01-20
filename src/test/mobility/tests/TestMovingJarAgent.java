package test.mobility.tests;

import test.common.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.mobility.MobilityOntology;
import jade.domain.mobility.MobileAgentDescription;
import jade.domain.mobility.MoveAction;

public class TestMovingJarAgent extends Test {
	private AID movingJarAgent;
	
	public Behaviour load(Agent a) throws TestException {
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Step 1) Create an agent from a separate Jar file
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("1) Create an agent from a separate Jar file");
					movingJarAgent = TestUtility.createAgent(myAgent, "dummy", "test.mobility.separate.dummy.DummyAgent", null);
				}
				catch (TestException te) {
					failed("ERROR creating an agent from a separate Jar file. "+te);
					te.printStackTrace();					
				}
			}
		});
		
		// Step 2) Move the jar agent to the Main Container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("2) Move the jar agent to the Main Container");
					MobileAgentDescription dsc = new MobileAgentDescription();
					dsc.setName(movingJarAgent);
					dsc.setDestination(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
					MoveAction moveAct = new MoveAction();
					moveAct.setMobileAgentDescription(dsc);
					TestUtility.requestAMSAction(myAgent, null, moveAct, MobilityOntology.NAME);
					passed("Jar agent successfully moved");
				}
				catch (TestException te) {
					failed("ERROR moving the jar agent to the Main Container. "+te);
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
	}
}
