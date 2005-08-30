package test.leap.split.tests;

import jade.core.Agent;
import jade.imtp.leap.JICP.JICPProtocol;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

public class TestShutDownSplitContainerHTTP extends TestShutDownSplitContainerBasic {
	
	JadeController jc1 = null;

	//create a container with BEManagementService
	//create a split-container using NIOBEDispatcher
	protected JadeController createSplitContainer() throws TestException{
		
		log("Launching normal container with HTTPPeer...");
		jc1 = TestUtility.launchJadeInstance("Container-2", null, new String("-container -host "+TestUtility.getLocalHostName()+ " -port "+String.valueOf(Test.DEFAULT_PORT)+" -icps jade.imtp.leap.JICP.JICPPeer;jade.imtp.leap.http.HTTPPeer(3099)"), null);
		log("Launching split-container...");
		JadeController jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getContainerHostName(myAgent, jc1.getContainerName())+" -port 3099" + " -connection-manager jade.imtp.leap.http.HTTPFEDispatcher" + " -" + JICPProtocol.MAX_DISCONNECTION_TIME_KEY + " " +MAX_DISCONNECTION_TIME + " -" +JICPProtocol.KEEP_ALIVE_TIME_KEY + " 10000"));
		return jc;
	}
	
	public void clean(Agent a) {
  	try {
  		if(jc1 != null){
  			jc1.kill();
  		}
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  

}
