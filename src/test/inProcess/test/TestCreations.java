package test.inProcess.test;

import jade.core.*;
import jade.core.behaviours.*;
import jade.wrapper.*;
import jade.content.onto.basic.*;
import jade.domain.JADEAgentManagement.*;
import jade.util.leap.*;
import test.common.*;

/**
 * Test some functionalities of the in-process interface (jade.wrapper package)
 * by running the ThanksAgent example.
 * <br> In particular, the following APIs are tested:
 * <ul> <code>jade.core.Agent.getContainerController()</code>
 * <li> <code>jade.wrapper.AgentContainer.createNewAgent</code> with null as third argument
 * <li> <code>jade.wrapper.AgentContainer.kill</code> with null as third argument
 * <li> <code>jade.wrapper.AgentController.start()</code> with null as third argument
 * <li> <code>new jade.core.ProfileImpl(false)</code> for creating a default container 
 * <li> <code>Runtime.instance().createAgentContainer(profile)</code> for creating a new container 
 * <li> message communication between the 3 created agents
 * </ul>
 * @author Fabio Bellifemine - TILAB
 **/
public class TestCreations extends Test {
		private final static String AGENTNAME = "ThanksAgent";
		private int availableContainers = 0;

		public Behaviour load(Agent a) throws TestException {
				try {
						// get the number of current containers
						availableContainers = ((List)TestUtility.requestAMSAction(a, a.getAMS(), new QueryPlatformLocationsAction())).size();
				} catch (Exception e) {
						e.printStackTrace();
				}
				log("available Containers="+availableContainers);
				// The test must complete in 5 sec
				setTimeout(5000);

				Behaviour b = new OneShotBehaviour(a) {
								public void action() {
										// create a new ThanksAgent
										try {
												myAgent.getContainerController().createNewAgent(AGENTNAME, "examples.thanksAgent.ThanksAgent", null).start();
										} catch (StaleProxyException e) {
												failed("exception in creating new agent"+e);
												return;
										}
										// block for 3 seconds
										myAgent.blockingReceive(3000);
										// check if the test went ok
										if (examples.thanksAgent.ThanksAgent.terminated == 2) {
												try {
														// check if the number of current containers is the same than before the test
														if (availableContainers == ((List)TestUtility.requestAMSAction(myAgent, myAgent.getAMS(), new QueryPlatformLocationsAction())).size()) {
																passed("TestCreations OK");
														} else {
																failed("There are more containers than the expected "+availableContainers);
														}
												} catch (TestException e) {
														log(e.toString());
														failed("Exception in requestAMSAction "+e);
												}
										}	else {
												failed("ThanksAgent did not terminate both 2 agents"); 
										}
								}
						};
				return b;
		}

		public void clean(Agent a) {
				try {
						TestUtility.killAgent(a, new AID(AGENTNAME, AID.ISLOCALNAME));
						TestUtility.killAgent(a, new AID(AGENTNAME+"t1", AID.ISLOCALNAME));
				} catch (TestException any) {
				}
				examples.thanksAgent.ThanksAgent.terminated = 0;
				examples.thanksAgent.ThanksAgent.IAmTheCreator = true; 
				
		}
}
