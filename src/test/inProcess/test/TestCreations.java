package test.inProcess.test;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
	private AgentController t;

	public Behaviour load(jade.core.Agent a) throws TestException {
		try {
			// get the number of current containers
			availableContainers = ((List)TestUtility.requestAMSAction(a, a.getAMS(), new QueryPlatformLocationsAction())).size();
		} catch (Exception e) {
			throw new TestException("Can't get number of currently active containers", e);
		}
		log("available Containers="+availableContainers);

		Behaviour b = new OneShotBehaviour(a) {
			public void action() {
				// create a new ThanksAgent
				try {
					t = myAgent.getContainerController().createNewAgent(AGENTNAME, "examples.thanksAgent.ThanksAgent", new Object[] {myAgent.getLocalName()});
					t.start();
				} catch (StaleProxyException e) {
					failed("exception in creating new agent. "+e);
					return;
				}
				// Wait for the notification from the ThanksAgent
				AID thank = null; 
				try {
					thank = new AID(t.getName(), AID.ISGUID);
				}
				catch (StaleProxyException spe) {
					spe.printStackTrace();
					failed("Error getting agent name from controller");
					return;
				}
				long start = System.currentTimeMillis();
				ACLMessage inform = myAgent.blockingReceive(MessageTemplate.MatchSender(thank), 10000);
				System.out.println("Elapsed time: "+(System.currentTimeMillis()-start));
				// check if the test went ok
				if (inform != null) {
					try {
						// Kill the ThanksAgent
						t.kill();
						// Wait a bit then check if the number of current containers is the same than before the test
						try {Thread.sleep(2000);} catch (Exception e) {}
						int newAvailableContainers = ((List)TestUtility.requestAMSAction(myAgent, myAgent.getAMS(), new QueryPlatformLocationsAction())).size();
						if (newAvailableContainers == availableContainers) {
							passed("TestCreations OK");
						} else {
							failed("There are "+newAvailableContainers+" containers while "+availableContainers+" were expected");
						}
					} 
					catch (StaleProxyException spe) {
						log(spe.toString());
						failed("Exception killing ThanksAgent "+spe);
					}
					catch (TestException te) {
						log(te.toString());
						failed("Exception in requestAMSAction "+te);
					}
				}
				else {
					failed("Timeout expired"); 
				}
			}
		};
		return b;
	}
}
