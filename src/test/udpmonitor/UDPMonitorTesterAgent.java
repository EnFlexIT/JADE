/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package test.udpmonitor;

import java.util.Iterator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import test.common.*;


/**
 * Tester agent for UDP Monitor tests.
 * 
 * It starts a new platform and connects it to the platform where the test suite is running
 * over a new simple container with an HTTP MTP.
 *  
 * @author Roland Mungenast - Profactor
 */
public class UDPMonitorTesterAgent extends TesterAgent {

  private final static int PORT = 2000;
  private final static String LISTENER_AGENT_NAME = "AMSEventListener";
  private final static String MAIN_CONTAINER_NAME = "main-container";

  public final static String AMS_LISTENER_AGENT_AID_KEY = "ams-listener-agent-key";
  public final static String MAIN_CONTAINER_KEY = "main-container-key";
  public final static String MAIN_CONTAINER_PORT_KEY = "main-container-port-key";
  public final static String REMOTE_AMS_AID_KEY = "remote-ams-aid-key";
  
  private JadeController main, mtpCont;
  private AID receiverAgent, listenerAgent;
  
  protected TestGroup getTestGroup() {
    return new TestGroup("test/udpmonitor/UDPMonitorTestsList.xml") {
      
      protected void initialize(Agent a) throws TestException {
        
        // start main container with UDP monitoring as a new platform
        String mtp = "jade.mtp.http.MessageTransportProtocol";
        String proto = "http";
        main = TestUtility.launchJadeInstance(MAIN_CONTAINER_NAME, null, 
            new String("-services jade.core.nodeMonitoring.UDPNodeMonitoringService -name "+MAIN_CONTAINER_NAME+" -port "+PORT+" -mtp "+mtp), new String[]{proto});
      
        // Construct the AID of the AMS of the remote platform a
        AID remoteAMS = new AID("ams@"+MAIN_CONTAINER_NAME, AID.ISGUID);
        Iterator iter = main.getAddresses().iterator();
        while (iter.hasNext()) {
          remoteAMS.addAddresses((String) iter.next());
        }
        
        // Start a local container with an MTP
        mtpCont = TestUtility.launchJadeInstance("Container-mtp", null, 
            new String("-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -mtp "+mtp+"()"), null);
        
        // start event receiver agent at local container
        receiverAgent = TestUtility.createAgent(a, "EventReceiver", "test.udpmonitor.EventReceiverAgent", null);

        // start AMS event listener agent at remote platform
        String addr = receiverAgent.getAddressesArray()[0];
        String aidStr = receiverAgent.getName();
        listenerAgent = TestUtility.createAgent(a, LISTENER_AGENT_NAME, "test.udpmonitor.AMSEventListenerAgent", new String[] {aidStr, addr}, 
            remoteAMS, main.getContainerName());
        
        EventReceiverAgent.waitForEvent(); // skip next REMOVED container event
        EventReceiverAgent.clear();
        
        setArgument(AMS_LISTENER_AGENT_AID_KEY, listenerAgent);
        setArgument(MAIN_CONTAINER_PORT_KEY, new Integer(PORT));
        setArgument(MAIN_CONTAINER_KEY, main);
        setArgument(REMOTE_AMS_AID_KEY, remoteAMS);
      }
      
      protected void shutdown(Agent a) {
        mtpCont.kill();
        main.kill();
        try {
          TestUtility.killAgent(a, receiverAgent);
        } catch (TestException e) {
          e.printStackTrace();
        }
      }
    };
  }
        
  // Main method that allows launching this test as a stand-alone program 
  public static void main(String[] args) {
    try {
      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Exit the JVM when there are no more containers around
      rt.setCloseVM(true);

      Profile pMain = new ProfileImpl(null, Test.DEFAULT_PORT, null);

      AgentContainer mc = rt.createMainContainer(pMain);
      
      AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

      AgentController tester = mc.createNewAgent("tester", "test.udpmonitor.UDPMonitorTesterAgent", args);
      tester.start();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
 
}
