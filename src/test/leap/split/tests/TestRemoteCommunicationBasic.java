package test.leap.split.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementVocabulary;
import test.common.*;

/**
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public class TestRemoteCommunicationBasic extends Test{
	
	private JadeController jc = null;
	private static String PREFIX = "prefix_";
	private static String SUFFIX = "_suffix";

	
	public Behaviour load(Agent a) throws TestException {  
		
		log("Creating split container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)));
		
		return new OneShotBehaviour(a) {
			public void action() {
				try{
					String containerName = jc.getContainerName();
					log("Creating agent with wildcards via AMS...");
					TestUtility.createAgent(myAgent, PREFIX + JADEManagementVocabulary.CONTAINER_WILDCARD + SUFFIX, Agent.class.getName(), null, null, containerName);
					log("Agent correctly created.");

					AID wildcardAgent = new AID(PREFIX + containerName + SUFFIX, AID.ISLOCALNAME);
					try{
						//to be sure that the agent has been created.
						Thread.sleep(1000);
						TestUtility.killAgent(myAgent, wildcardAgent);
						log("Killing agent " + wildcardAgent.getName() + "...");
						passed("Agent " + wildcardAgent.getName() + " found and killed as expected.");
					}
					catch(Exception te){
						failed("Cannot kill agent "+wildcardAgent.getName()+". "+te);
	  				te.printStackTrace();
					}
  			}
  			catch (Exception e) {
  				failed("Error starting container with wildcarded bootstrap agent. " + e);
  				e.printStackTrace();
  			}
	  	}
  	};
	}
	
	public void clean(Agent a) {
		try{
			TestUtility.killContainer(a, jc.getContainerName());
		}catch(TestException te){
			te.printStackTrace();
		}
		if (jc != null) {
	  	jc.kill();
  	}
	}

}
