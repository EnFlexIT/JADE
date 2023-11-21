package test.leap.split.tests;

import jade.core.Agent;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

public class TestRemoteCommunicationNIO extends TestRemoteCommunicationBasic {

	JadeController jc1, jc;

	//create a container with BEManagementService
	//create a split-container using NIOBEDispatcher
	protected String createSplitContainer() throws TestException{
		log("Launching normal container with BEManagementService installed...");
		jc1 = TestUtility.launchJadeInstance("Container-2", null, new String("-container -host "+TestUtility.getLocalHostName()+ " -port "+String.valueOf(Test.DEFAULT_PORT)+" -services jade.imtp.leap.nio.BEManagementService"), null);
		log("Launching split-container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getContainerHostName(myAgent, jc1.getContainerName())+" -port 2099" + " -mediator-class jade.imtp.leap.nio.NIOBEDispatcher"));
		return jc.getContainerName();

	}

	protected void killSplitContainer(Agent a){
		try{
			TestUtility.killContainer(a, jc.getContainerName());
		}catch(TestException te){
			te.printStackTrace();
		}
		if(jc != null) {
			jc.kill();
		}
		if(jc1 != null){
			jc1.kill();
		}
	}

}
