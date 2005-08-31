package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.*;

/**
 * This test checks whether a temporal unreachable container doesn't get removed from the platform.
 * <p>
 * The test starts an agent which subscribes at the AMS. Then it starts a peripheral container and waits 
 * for the appropriate event from the AMS. Next it kills the container, waits until the ping delay limit
 * has expired and then restarts the container with the same name.
 * </p>
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestReconnectingContainer extends TestBase {
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
					jc = startPeripheralContainer(myAgent, "-services test.udpmonitor.tests.InterruptableUDPNodeMonitoringService");
				}
				catch (TestException te) {
					te.printStackTrace();
					failed("Error starting monitored peripheral container. "+te);
				}
			}
		} );
  
		// Setp 2: Wait for a while  
		sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
			public void onStart() {
				log("Wait for a while...");
				super.onStart();
			}

			public void onWake() {
			}
		} );
	  
		// Setp 3: Make the peripheral container stop sending ping packets for a while  
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				try {
					log("Make the peripheral container stop sending ping packets for a while.");
					TestUtility.createAgent(myAgent, "udp-interruptor", TestReconnectingContainer.this.getClass().getName()+"$UDPInterruptorAgent", null, getRemoteAMS(), PERIPHERAL_CONTAINER_NAME);
				}
				catch (TestException te) {
					te.printStackTrace();
					failed("Error starting UDP-interruptor agent. "+te);
				}
			}
		} );
		
		// Setp 3: Wait for 3 times the unreachable limit  to be sure that no REMOVED-CONTAINER event is received  
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
	
	
	/**
	 * Inner class UDPInterruptorAgent
	 */
	public static class UDPInterruptorAgent extends Agent {
		protected void setup() {
			try {
				InterruptableUDPNodeMonitoringHelper helper = (InterruptableUDPNodeMonitoringHelper) getHelper(UDPNodeMonitoringService.NAME);
				if (helper != null) {
					helper.setPingDelay(60000);
					Thread.sleep(UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT - UDPNodeMonitoringService.DEFAULT_PING_DELAY);
					helper.setPingDelay(UDPNodeMonitoringService.DEFAULT_PING_DELAY);
					doDelete();
					return;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// Something went wrong --> Kill the container so that the test will fail
			try {
				getContainerController().kill();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	} // END of inner class UDPInterruptorAgent
}
