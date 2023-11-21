package test.leap.split.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;
import test.common.JadeController;

public class TestExitWhenEmptyOnSplitContainerBasic extends Test{

	Agent myAgent;
	private String containerName;
	private JadeController jc;
	private static final String KILLER_NAME="killer";

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
		});

		//step 2: creating agent on split-container
		sb.addSubBehaviour(new WakerBehaviour(a, 2000){
			public void onWake(){
				try{
					log("Starting killer agent on " + containerName);
					TestUtility.createAgent(myAgent, KILLER_NAME, "test.leap.split.tests.TestExitWhenEmptyOnSplitContainerBasic$KillerAgent", null, null, containerName);
					log("Killer agent correctly started");
				}
				catch(TestException e){
					failed("Error starting Ping agent. " + e);
					e.printStackTrace();
				}
			}
		});

		//step 3: verify if container successfully ended.
		sb.addSubBehaviour(new WakerBehaviour(a, 20000){
			public void handleElapsedTimeout() {
				//check if the container has been successfully ended.
				try{
					log("Try killing split-container...");
					TestUtility.killContainer(myAgent, containerName);
					failed("Split-container does NOT exit as expected after max disconnection time out elapsed.");
				}catch(TestException te){
					log("Exception occured as expected. " + te);
					passed("Split-container successfully killed by killer agent.");
				}
			}
		});
		return sb;

	}

	public void clean(Agent a) {
		try{
			if(jc != null){
				jc.kill();
			}
		}
		catch(Exception e){
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
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT) + " -exitwhenempty true"));
		log("split-container created successfully !");
		return jc.getContainerName();
	}

	public static class KillerAgent extends Agent{
		protected void setup() {
			addBehaviour(new WakerBehaviour(this, 5000) {
				public void handleElapsedTimeout() {
					System.out.println("Agent " +getName() + " is killing itself...");
					doDelete();
				}
			});
		}
	}// END of inner class KillAgent
}
