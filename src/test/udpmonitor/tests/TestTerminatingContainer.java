package test.udpmonitor.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.introspection.RemovedContainer;
import test.common.JadeController;
import test.udpmonitor.EventReceiverAgent;
import test.udpmonitor.UDPMonitorTestHelper;
import test.udpmonitor.UDPMonitorTesterAgent;

/**
 * This test checks whether a peripheral container gets removed <br />
 * immediately from a platform when it is killed.
 * <p>
 * The test starts an agent which subscribes at the AMS. Then it starts a <br />
 * peripheral container and waits  for the appropriate event from the AMS. Next <br />
 * it kills the container and checks whether the container gets immediately removed <br />
 * from the platform, ignoring the maximum allowed time for staying in the state UNREACHABLE.
 * </p>
 * 
 * @author Roland Mungenast - Profactor
 */
public class TestTerminatingContainer extends TestMonitoredContainer {
  
  public Behaviour load(Agent a) {
    
      return new OneShotBehaviour(a) {
        
        JadeController container = null;
        
        public void action() {
          
          try {  
            EventReceiverAgent.clear();

             // start a new peripheral container
            log("(1) Starting new peripheral container.");
            int port = ((Integer)getGroupArgument(UDPMonitorTesterAgent.MAIN_CONTAINER_PORT_KEY)).intValue();
            container = startPeripheralContainer(port);
            
            // kill the container
            log("(2) Killing container.");
            AID ams = (AID)getGroupArgument(UDPMonitorTesterAgent.REMOTE_AMS_AID_KEY);
            UDPMonitorTestHelper.killContainer(myAgent, ams, container.getContainerName());
 
            // container has to be removed immediately
            int removed = EventReceiverAgent.getEventCnt(RemovedContainer.NAME); 
          
            if (removed == 1) {
              passed("Container has been removed immediately ... OK");
            } else {
              failed("AMS hasn't fired a REMOVED CONTAINER event as expected.");
            }
            
          } catch (Exception e) {
            failed(e.toString());
          
          } finally {
            container.kill();
          }
        } 
      };
    }
}

