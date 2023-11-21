package test.leap.split.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.MicroRuntime;
import test.common.*;

/**
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public class TestSplitContainerNaturalDeathBasic extends Test {

	private static final String KILLER_NAME = "killer";
	String containerName;
	JadeController jc = null;
	Agent myAgent;

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

		//Step 2: create KillerAgent via AMS.
		sb.addSubBehaviour(new WakerBehaviour(a, 2000){
			public void onWake(){
				try{
					log("Starting killer agent on " + containerName);
					TestUtility.createAgent(myAgent, KILLER_NAME, "test.leap.split.tests.TestSplitContainerNaturalDeathBasic$KillerAgent", null, null, containerName);
					log("Killer agent correctly started");
				}
				catch(TestException e){
					failed("Error starting Ping agent. " + e);
					e.printStackTrace();
				}
			}
		});

		//3. verify if the container is already alive.
		sb.addSubBehaviour(new WakerBehaviour(a, 20000){
			public void handleElapsedTimeout() {
				//check if the container has been successfully ended.
				try{
					log("Try killing split-container...");
					TestUtility.killContainer(myAgent, containerName);
					failed("Split-container not killer by killer agent.");
				}catch(TestException te){
					log("Exception occured as expected. " + te);
					passed("Split-container successfully killed by killer agent.");
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

	public static class KillerAgent extends Agent{
		protected void setup() {
			addBehaviour(new WakerBehaviour(this, 5000) {
				public void handleElapsedTimeout() {
					//kill the container.
					MicroRuntime.stopJADE();
				}
			});
		}
	}// END of inner class KillAgent
}

