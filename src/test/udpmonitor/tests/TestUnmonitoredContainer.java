package test.udpmonitor.tests;

import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import test.common.JadeController;
import test.common.TestException;
import test.udpmonitor.UDPMonitorTestHelper;

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
public class TestUnmonitoredContainer extends 
  TestMonitoredContainer {
  
  public TestUnmonitoredContainer() {
    expAddedCont = 1;
    expRemovedCont = 1;
    delay = UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT + UDPNodeMonitoringService.DEFAULT_PING_DELAY_LIMIT;
    killContainer = false;
  }
  
  /**
   * Starts a peripheral container with DEactivated UDP monitoring
   */
  protected JadeController startPeripheralContainer(int port) throws TestException {
    // start container without UDP monitoring
    return UDPMonitorTestHelper.startPeripheralContainer("simple-peripheral-container", port);
  }
  
}

