package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.*;

/**
 * This test checks that a peripheral container with deactivated 
 * UDP monitoring is not automatically removed from the platform.
 * 
 * The test starts a peripheral container WITHOUT the UDPNodeMonitoringService and waits 
 * for the appropriate event from the AMS. Next it it waits (3 * maximum time a node can be unreachable)
 * and checks whether the container is still alive.
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
  
		// Setp 2: Wait for 3 times the unreachable limit  to be sure that no REMOVED-CONTAINER event is received  
		sb.addSubBehaviour(new WakerBehaviour(a, UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT * 3) {
			public void onStart() {
				log("Wait for 3 times the unreachable limit to be sure that the peripheral container is not removed...");
				super.onStart();
			}

			public void onWake() {
			}
		} );
	  
		// Setp 3: Check that no REMOVED-CONTAINER event was received then kill the unmonitored container 
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				if (getRemovedContainerCnt() > 0) {
					failed("Unexpected REMOVED-CONTAINER event received");
				}
				else {
					try {
						log("Up to now the un-monitored container is correctly alive. Now kill it");
						// This returns when the killed container is actually removed --> There is no need to wait an extra time 
						// due to the fact that we are killing an un-monitored container.
						TestUtility.killContainer(myAgent, getRemoteAMS(), jc.getContainerName());
					} 
					catch (Exception e) {
						e.printStackTrace();
						failed("Error killing un-monitored container."+e);
					}					
				}
			}
		} );
		
		// Step 4: Just wait a bit to be sure the REMOVED-CONTAINER notification is received
		sb.addSubBehaviour(new WakerBehaviour(a, 2000) {
			public void onWake() {
			}
		} );
	  
		return sb;
	}	
	
	public void clean(Agent a) {
		super.clean(a);
		
		if (jc != null) {
			// The container should have already been removed but ...
			jc.kill();
		}
	}
  
}

