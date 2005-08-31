package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.*;

/**
 * This test checks whether a peripheral container gets removed <br />
 * immediately from a platform when it is killed.
 * <p>
 * The test sstarts a 
 * peripheral container and waits  for the appropriate event from the AMS. Next <br />
 * it kills the container and checks whether the container gets immediately removed <br />
 * from the platform, ignoring the maximum allowed time for staying in the state UNREACHABLE.
 * </p>
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestTerminatingContainer extends TestMonitoredContainer {
	private JadeController jc = null;

	public Behaviour loadSpecific(Agent a) throws TestException {
		expectedAddedContainer = 1;
		expectedRemovedContainer = 1;
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Setp 1: Start a monitored peripheral container 
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("Starting monitored peripheral container.");
				try {
					jc = startPeripheralContainer(myAgent, "-services jade.core.nodeMonitoring.UDPNodeMonitoringService");
				}
				catch (TestException te) {
					te.printStackTrace();
					failed("Error starting monitored peripheral container. "+te);
				}
			}
		} );
  
		// Setp 2: Kill the peripheral container 
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("Killing the peripheral container.");
				try {
					TestUtility.killContainer(myAgent, getRemoteAMS(), jc.getContainerName());
				}
				catch (TestException te) {
					te.printStackTrace();
					failed("Error killing peripheral container. "+te);
				}
			}
		} );
		
		// Setp 3: Wait for a little while. The container should have been removed immediately
		sb.addSubBehaviour(new WakerBehaviour(a, 2000) {
			public void onStart() {
				log("Wait for a little while. The container should have been removed immediately...");
				super.onStart();
			}

			public void onWake() {
			}
		} );
	  
		return sb;
	}
	
	public void clean(Agent a) {
		super.clean(a);
		
		if (jc != null) {
			// The container should have already been removed, but ...
			jc.kill();
		}
	}
}

