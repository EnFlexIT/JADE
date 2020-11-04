package examples.ams;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.behaviours.ActionExecutor;
import jade.core.behaviours.OutcomeManager;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;

public class CreatorAgent extends Agent {
	private static final long serialVersionUID = 875534367892872L;

	private String containerName;
	
	@Override
	public void setup() {
		// Get the name of the container where to create an agent as first argument (default: Main Container)
		containerName = AgentContainer.MAIN_CONTAINER_NAME;
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			containerName = (String) args[0];
		}
		
		// Wait a bit and then create an agent of class jade.core.Agent in the indicated container
		System.out.println("Hello. I'm going to create a new agent in container "+containerName+" in 5 seconds...");
		addBehaviour(new WakerBehaviour(this, 5000) {
			@Override
			public void onWake() {
				// Request the AMS to perform the CreateAgent action of the JADEManagementOntology
				// To do this use an ActionExecutor behaviour requesting the CreateAgent action and expecting no result (Void) 
				System.out.println("Creating agent!");
				CreateAgent ca = new CreateAgent();
				ca.setAgentName(getLocalName()+"-child");
				ca.setClassName("jade.core.Agent");
				ca.setContainer(new ContainerID(containerName, null));
				ActionExecutor<CreateAgent, Void> ae = new ActionExecutor<CreateAgent, Void>(ca, JADEManagementOntology.getInstance(), getAMS()) {
					@Override
					public int onEnd() {
						int ret = super.onEnd();
						if (getExitCode() == OutcomeManager.OK) {
							// Creation successful
							System.out.println("Agent successfully created");
						}
						else {
							// Something went wrong
							System.out.println("Agent creation error. "+getErrorMsg());
						}
						return ret;
					}
				};
				addBehaviour(ae);
			}
		});
	}
}
