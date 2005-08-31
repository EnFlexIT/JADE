package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.*;

/**
 * This test checks whether a peripheral container with deactivated 
 * UDP monitoring gets automatically removed from the platform.
 * 
 * The test starts an agent which subscribes at the AMS. Then it starts a peripheral container and waits 
 * for the appropriate event from the AMS. Next it it waits the maximum time a node can be unreachable + the 
 * maximum time the server waits for a single ping and checks whether the container has been removed from the platform.
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestUnmonitoredContainer extends TestBase {
	private JadeController jc = null;

	public Behaviour loadSpecific(Agent a) throws TestException {
		expectedAddedContainer = 1;
		expectedRemovedContainer = 1;
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Setp 1: Start an unmonitored peripheral container 
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("Starting unmonitored peripheral container.");
				try {
					jc = startPeripheralContainer(myAgent, "");
				}
				catch (TestException te) {
					te.printStackTrace();
					failed("Error starting unmonitored peripheral container. "+te);
				}
			}
		} );
  
		// Setp 2: Wait for ~ ping-delay-limit + unreachable-limit. The container should have been removed
		sb.addSubBehaviour(new WakerBehaviour(a, UDPNodeMonitoringService.DEFAULT_PING_DELAY_LIMIT + UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT + 2000) {
			public void onStart() {
				log("Wait for ~ ping-delay-limit + unreachable-limit. The container should have been removed...");
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
			// The container should have already been removed, but the prosess should be still running
			jc.kill();
		}
	}
  
}

