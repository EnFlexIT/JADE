package test.udpmonitor.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import jade.domain.introspection.RemovedContainer;
import test.common.JadeController;
import test.udpmonitor.EventReceiverAgent;
import test.udpmonitor.UDPMonitorTestHelper;
import test.udpmonitor.UDPMonitorTesterAgent;

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
public class TestReconnectingContainer extends TestMonitoredContainer {
  
  public Behaviour load(Agent a) {
    
      return new OneShotBehaviour(a) {

        public void action() {
          
          JadeController container = null;
          
          try {  
            EventReceiverAgent.clear();

             // start a new peripheral container
            log("(1) Starting a new peripheral container.");
            int port = ((Integer)getGroupArgument(UDPMonitorTesterAgent.MAIN_CONTAINER_PORT_KEY)).intValue();
            container = startPeripheralContainer(port);
            
            // kill the container
            log("(2) Killing the peripheral container.");
            AID ams = (AID)getGroupArgument(UDPMonitorTesterAgent.REMOTE_AMS_AID_KEY);
            UDPMonitorTestHelper.killContainer(myAgent, ams, container.getContainerName());
            container.kill(); // kill also the process
            
            // wait for ping delay limit
            log("(3) Waiting for expiration of the ping delay limit.");
            Thread.sleep(UDPNodeMonitoringService.DEFAULT_PING_DELAY_LIMIT);
            
            // restart the container
            log("(4) Restarting the container.");
            container = startPeripheralContainer(port);
            
            // container shouldn't be removed
            int removed = EventReceiverAgent.getEventCnt(RemovedContainer.NAME); 
          
            if (removed == 0) {
              passed("Container hasn't been removed ... OK");
            } else {
              failed("AMS has fired "+removed+" REMOVED CONTAINER event(s). Expected was no event.");
            }
            
          } catch (Exception e) {
            failed(e.toString());
          
          } finally {
            container.kill();
            try {
              Thread.sleep(3000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        } 
      };
    }
}

