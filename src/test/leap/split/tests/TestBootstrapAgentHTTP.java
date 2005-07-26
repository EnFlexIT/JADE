package test.leap.split.tests;

import jade.core.Agent;
import jade.domain.JADEAgentManagement.JADEManagementVocabulary;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

public class TestBootstrapAgentHTTP extends TestBootstrapAgentBasic {
	JadeController jc1, jc;

	//create a container with BEManagementService
	//create a split-container using NIOBEDispatcher
	protected String createSplitContainerWithBootstrapAgent() throws TestException{
		
		log("Launching normal container with HTTPPeer...");
		jc1 = TestUtility.launchJadeInstance("Container-2", null, new String("-container -host "+TestUtility.getLocalHostName()+ " -port "+String.valueOf(Test.DEFAULT_PORT)+" -icps jade.imtp.leap.JICP.JICPPeer;jade.imtp.leap.http.HTTPPeer(3099)"), null);
		log("Launching split-container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getContainerHostName(myAgent, jc1.getContainerName())+" -port 3099" + " -connection-manager jade.imtp.leap.http.HTTPFEDispatcher" + " "+ TestBootstrapAgentBasic.PREFIX+JADEManagementVocabulary.CONTAINER_WILDCARD+TestBootstrapAgentBasic.SUFFIX+":jade.core.Agent"));
		return jc.getContainerName();
	}
	
	public void clean(Agent a) {
  	try {
  		jc1.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  	try {
  		jc.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  


}
