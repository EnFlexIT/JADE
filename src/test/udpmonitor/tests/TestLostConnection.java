package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.*;

/**
 * This test checks whether a peripheral container gets removed from a platform <br />
 * when the container doesn't send ping messages anymore.
 * <p>
 * The test starts a peripheral 
 * container and waits for the appropriate event from the AMS. Next it kills the whole <br />
 * process of the container and checks whether the container gets removed from the platform <br />
 * after the expiration of the ping delay limit and the unreachable limit.
 * </p>
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestLostConnection extends TestBase {
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
				log("Crashing the peripheral container.");
				jc.kill();
			}
		} );
		
		// Setp 3: Wait for ~ ping-delay-limit + unreachable-limit. The container should have been removed
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
}

