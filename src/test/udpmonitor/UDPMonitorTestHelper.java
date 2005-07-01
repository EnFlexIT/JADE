package test.udpmonitor;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillContainer;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;
import test.common.JadeController;
import test.common.TestException;
import test.common.TestUtility;


/**
 * Helper class for the UDP Monitor tests
 * 
 * @author Roland Mungenast - Profactor
 */
public class UDPMonitorTestHelper {
  
  /**
   * Behaviour for sending a request, used by the method <code>killContainer</code>
   *
   * @author Roland Mungenast - Profactor
   */
  public static class RequestInitiator extends SimpleAchieveREInitiator {

    public RequestInitiator(Agent a, ACLMessage msg) {
      super(a, msg);
    }
    
    public boolean done() {
      myAgent.removeBehaviour(this); // remove itself from the agent
      return super.done();
    }
  }
  
  /**
   * Start a peripheral container with UDP monitoring
   * @param name Name of the container
   * @param port Port number where the main container to register with, is running.
   */
  public static JadeController startUDPMonitoredPeripheralContainer(String name, int port) throws TestException {
    return TestUtility.launchJadeInstance(name
      , null, "-container-name "+name+" -container -port "+port+" -host " + TestUtility.getLocalHostName() + " -services jade.core.nodeMonitoring.UDPNodeMonitoringService"
      , new String[] {});
  }
  
  /**
   * Start a peripheral container
   * @param name Name of the container
   * @param port Port number where the main container to register with, is running.
   */
  public static JadeController startPeripheralContainer(String name, int port) throws TestException {
    return TestUtility.launchJadeInstance(name
      , null, "-container-name "+name+" -container -port "+port+" -host " + TestUtility.getLocalHostName()
      , new String[] {});
  }
 
  
  /**
   * Kill a container in such a way that it is shutting down correctly
   * @param name Name of the container to kill
   */
  public static void killContainer(Agent sender, AID ams, String name) throws Exception {
    KillContainer kc = new KillContainer();
    kc.setContainer(new ContainerID(name, null));

    try {
      Action a = new Action();
      a.setActor(ams);
      a.setAction(kc);

      sender.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
      sender.getContentManager().registerOntology(JADEManagementOntology.getInstance());
      
      ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
      requestMsg.addReceiver(ams);
      requestMsg.setOntology(JADEManagementOntology.NAME);
      requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
      sender.getContentManager().fillContent(requestMsg, a);
      sender.addBehaviour(new RequestInitiator(sender, requestMsg));
      
      
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Exception("An error occured while trying to kill the container '"+name+"'. ["
          + e.toString() + "]", e);
    }
  }
}


