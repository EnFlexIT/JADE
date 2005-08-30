package test.leap.split.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.imtp.leap.JICP.JICPProtocol;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;
import test.common.JadeController;

public class TestShutDownSplitContainerBasic extends Test {
	
	private JadeController jc;
	private String containerName;
	Agent myAgent;
	static final String MAX_DISCONNECTION_TIME ="30000";
	
	
	public Behaviour load(Agent a) throws TestException {
		
		myAgent = a;
		
		//Step 1: launch split-container
		SequentialBehaviour sb = new SequentialBehaviour(a);

		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					jc = createSplitContainer();
					containerName = jc.getContainerName();
  			}
  			catch (Exception e) {
  				failed("Error initilizing split-container. " + e);
  				e.printStackTrace();
  			}
  		}
  	} );
			
		//step 2: kill container process
		sb.addSubBehaviour(new WakerBehaviour(a, 20000){
			public void handleElapsedTimeout() {
				try{
					log("Shut down split-container process");
					if(jc != null){
						jc.kill();
					}
				}catch(Exception e){
					log("Exception in killing process " + e);
					e.printStackTrace();
				}
			}
		});
		
		//step 3: verify if max-disconnection time successfully end the container.
		sb.addSubBehaviour(new WakerBehaviour(a, 80000){
			public void handleElapsedTimeout() {
				//check if the container has been successfully ended.
				try{
					log("Try killing split-container..." + containerName);
					TestUtility.killContainer(myAgent, containerName);
					failed("The container does NOT exit as expected when disconnection time elapsed.");
				}catch(TestException te){
					log("Exception occured as expected. " + te);
					passed("Split-container exit after max disconnection time.");
				}
			}
		});
		
		return sb;
	}

	public void clean(Agent a) {

	}
	
	/**
	 * Override this method to initialize the environment for the test.
	 * @return name the name of the split-container created.
	 * @throws TestException
	 */
	protected JadeController createSplitContainer() throws TestException{
		log("Creating split container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT) +" -"+ JICPProtocol.MAX_DISCONNECTION_TIME_KEY + " " + MAX_DISCONNECTION_TIME));
		log("split-container created successfully !");
		return jc;
	}
}
