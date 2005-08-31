package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.*;

/**
 * This tests checks whether a peripheral container with activated <br />
 * UDP monitoring is sending ping messages in time.
 * 
 * The test starts an agent which subscribes at the AMS. Then it starts a peripheral container and waits 
 * for the appropriate event from the AMS. Next it waits (3 * maximum time a node can be unreachable) 
 * and checks whether the container is still alive.
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestMonitoredContainer extends TestBase {
	private JadeController jc = null;

	public Behaviour loadSpecific(Agent a) throws TestException {
		expectedAddedContainer = 1;
		expectedRemovedContainer = 0;
		
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
  
		// Setp 2: Wait for 3 times the unreachable limit  to be sure that no REMOVED-CONTAINER event is received  
		sb.addSubBehaviour(new WakerBehaviour(a, UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT * 3) {
			public void onStart() {
				log("Wait for 3 times the unreachable limit to be sure that the peripheral container is not removed...");
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
			// We kill the peripheral container smoothly to avoid waiting the unreachable limit for its actual removal
			try {
				TestUtility.killContainer(a, getRemoteAMS(), jc.getContainerName());
			} 
			catch (TestException te) {
				te.printStackTrace();
				jc.kill();
			}
		}
	}
}
