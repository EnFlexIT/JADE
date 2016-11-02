package examples.ams;

import jade.core.Agent;
import jade.core.behaviours.ActionExecutor;
import jade.core.behaviours.OutcomeManager;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;

import java.util.Iterator;

public class ContainersRetrieverAgent extends Agent {

	@Override
	public void setup() {
		// Wait a bit and then create an agent of class jade.core.Agent in the indicated container
		System.out.println("Hello. I'm going to get the list of all containers in the platform every 10 seconds...");
		addBehaviour(new TickerBehaviour(this, 10000) {
			@Override
			public void onTick() {
				// Request the AMS to perform the QueryPlatformLocationsAction action of the JADEManagementOntology
				// To do this use an ActionExecutor behaviour requesting the QueryPlatformLocationsAction action and expecting a result of type jade.util.leap.List 
				System.out.println("Retrieving containers");
				QueryPlatformLocationsAction qa = new QueryPlatformLocationsAction();
				ActionExecutor<QueryPlatformLocationsAction, jade.util.leap.List> ae = new ActionExecutor<QueryPlatformLocationsAction, jade.util.leap.List>(qa, JADEManagementOntology.getInstance(), getAMS()) {
					@Override
					public int onEnd() {
						int ret = super.onEnd();
						if (getExitCode() == OutcomeManager.OK) {
							// Creation successful
							System.out.println("Containers successfully retrieved");
							jade.util.leap.List containers = getResult();
							Iterator it = containers.iterator();
							while (it.hasNext()) {
								System.out.println("- "+it.next());
							}
						}
						else {
							// Something went wrong
							System.out.println("Error retrieving containers. "+getErrorMsg());
						}
						return ret;
					}
				};
				addBehaviour(ae);
			}
		});
	}
}
