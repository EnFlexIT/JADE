package test.udpmonitor.tests;




import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.nodeMonitoring.UDPNodeMonitoringService;
import jade.domain.introspection.AddedContainer;
import jade.domain.introspection.RemovedContainer;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;

import test.udpmonitor.EventReceiverAgent;
import test.udpmonitor.UDPMonitorTestHelper;
import test.udpmonitor.UDPMonitorTesterAgent;

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
public class TestMonitoredContainer extends Test {

  /** number of expected ADDED CONTAINER events **/
  protected int expAddedCont;
  
  /** number of expected REMOVED CONTAINER events **/
  protected int expRemovedCont;
  
  /** time to wait after the peripheral container has been started **/
  protected int delay;
  
  /** specifies whether the created simple container has to be killed at the end of the test **/
  protected boolean killContainer;
  
  /**
   * Default constructor - initializes all test parameters
   */
  public TestMonitoredContainer() {
    expAddedCont = 1;
    expRemovedCont = 0;
    delay = UDPNodeMonitoringService.DEFAULT_UNREACHABLE_LIMIT * 3;
    killContainer = true;
  }
  
  /**
   * Starts a peripheral container with activated UDP monitoring
   */
  protected JadeController startPeripheralContainer(int port) throws TestException {
    // start container with activated UDP monitoring
    return UDPMonitorTestHelper.startUDPMonitoredPeripheralContainer("udp-peripheral-container", port);
  }
  
  // Evaluates the test results and calls either the method passed or failed
  private void evaluateResults(int addedCont, int removedCont) {
    if (addedCont == expAddedCont && removedCont == expRemovedCont) {
      passed("AMS has fired "+expAddedCont+" ADDED CONTAINER and "+expRemovedCont+" REMOVED CONTAINER ... OK");
      
    } else {
      if (addedCont != expAddedCont) 
        failed("AMS fired " + addedCont + " ADDED CONTAINER event(s). Expected: "+expAddedCont+".");
      if (removedCont != expRemovedCont)
        failed("AMS fired " + removedCont + " REMOVED CONTAINER event(s). Expected: "+expRemovedCont+".");
    }
  }

  
  public Behaviour load(Agent a) {
  
    return new OneShotBehaviour(a) {
      
      public void action() {       
        
        JadeController per = null;
        
        try {  
          EventReceiverAgent.clear();

          // start a new peripheral container
          log("(1) Starting a new peripheral container.");
          int port = ((Integer)getGroupArgument(UDPMonitorTesterAgent.MAIN_CONTAINER_PORT_KEY)).intValue();
          per = startPeripheralContainer(port);
          
          int added = EventReceiverAgent.getEventCnt(AddedContainer.NAME);
          
          // wait some time
          log("(2) Wait "+delay+" milliseconds.");
          try {
            Thread.sleep(delay); 
          } catch (InterruptedException e) {
            failed(e.toString());
          }          
          
          int removed = EventReceiverAgent.getEventCnt(RemovedContainer.NAME);
          evaluateResults(added, removed); 
          
          if (killContainer) {
            log("(3) Killing container " + per.getContainerName());
            per.kill();
            EventReceiverAgent.waitForEvent(); // skip next REMOVED container event
            EventReceiverAgent.clear();
          }
          
        } catch (TestException e) {
          failed(e.toString());
        
        } finally {
          per.kill();
        }
      } 
    };
  }
  
  public void clean(Agent a) {
    // nothing to do
  }
 
}
