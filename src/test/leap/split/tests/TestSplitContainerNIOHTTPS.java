package test.leap.split.tests;

import jade.core.Agent;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

public class TestSplitContainerNIOHTTPS extends TestSplitContainerBasic {
	JadeController jc1, jc;

	//create a container with BEManagementService using HTTPS
	//create a split-container using HTTPFESDispatcher 
	protected String createSplitContainer() throws TestException{
		log("Launching normal container with BEManagementService installed...");
		jc1 = TestUtility.launchJadeInstance("Container-2", null, new String("-container -host "+TestUtility.getLocalHostName()+ " -port "+String.valueOf(Test.DEFAULT_PORT)+" -services jade.imtp.leap.nio.BEManagementService -jade_imtp_leap_nio_BEManagementService_accept https"), null);
		log("Launching split-container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getContainerHostName(myAgent, jc1.getContainerName())+" -port 2099" + " -proto https"));
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
