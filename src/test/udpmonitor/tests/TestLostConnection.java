package test.udpmonitor.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import jade.domain.introspection.RemovedContainer;
import test.common.JadeController;
import test.common.TestException;
import test.udpmonitor.EventReceiverAgent;
import test.udpmonitor.UDPMonitorTesterAgent;

/**
 * This test checks whether a peripheral container gets removed from a platform <br />
 * when the container doesn't send ping messages anymore.
 * <p>
 * The test starts an agent which subscribes at the AMS. Then it starts a peripheral <br />
 * container and waits for the appropriate event from the AMS. Next it kills the whole <br />
 * process of the container and checks whether the container gets removed from the platform <br />
 * after the expiration of the ping delay limit and the unreachable limit.
 * </p>
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestLostConnection extends TestMonitoredContainer {
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
            
            log("(2) Killing the process of the container.");
            container.kill(); 
            /* NOTE: the method call above actually kills the whole process not the container! 
               This means that the container is not shutting down correctly. This should have the
               same effect than a system crash or a connection problem. */
            
            log("(3) Waiting until the ping delay limit has expired.");
            try {
              Thread.sleep(UDPNodeMonitoringService.DEFAULT_PING_DELAY_LIMIT);
            } catch (InterruptedException e) {
              failed(e.toString());
            }
            
            // container has to be removed after the expiration of the PING DELAY and UNREACHABLE limit
            
            if (EventReceiverAgent.getEventCnt(RemovedContainer.NAME) > 0) {
              failed("Container has been removed too early!");
            }
            
            log("(4) Waiting until the unreachable limit has expired.");
            try {
              Thread.sleep(UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT);
            } catch (InterruptedException e) {
              failed(e.toString());
            }
          
            int removed = EventReceiverAgent.getEventCnt(RemovedContainer.NAME); 
          
            if (removed == 1) {
              passed("Container has been removed after the expiration of the ping delay and unreachable limit ... OK");
            } else {
              failed("AMS hasn't fired a REMOVED CONTAINER event as expected.");
            }
            
          } catch (TestException e) {
            failed(e.toString());
          
          } finally {
            container.kill();
          }
        } 
      };
    }
}

