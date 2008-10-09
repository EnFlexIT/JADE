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

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.Iterator;

import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestGroup;
import test.common.TestUtility;
import test.common.TesterAgent;

/**
 * Tester agent for UDP Monitor tests.
 * 
 * It starts a new platform and connects it to the platform where the test suite is running
 * over a new simple container with an HTTP MTP.
 *  
 * @author Roland Mungenast - Profactor
 */
public class UDPMonitorTesterAgent extends TesterAgent {

	public final static String REMOTE_PLATFORM_NAME = "Remote-platform";
	public final static String REMOTE_PLATFORM_MAIN_CONTAINER_NAME = "Main-Container";
	public final static int REMOTE_PLATFORM_PORT = 2000;

	public final static String REMOTE_AMS_AID_KEY = "remote-ams-aid-key";

	private JadeController main, mtpCont;

	protected TestGroup getTestGroup() {
		return new TestGroup("test/udpmonitor/UDPMonitorTestsList.xml") {

			protected void initialize(Agent a) throws TestException {
				// Start a new platform (Main Container) with UDP monitoring
				String mtp = Test.DEFAULT_MTP;
				String proto = Test.DEFAULT_PROTO;
				main = TestUtility.launchJadeInstance(REMOTE_PLATFORM_NAME, "+" + TestUtility.HTTP_MTP_CLASSPATH,
						"-services jade.core.nodeMonitoring.UDPNodeMonitoringService -name " + REMOTE_PLATFORM_NAME +
						" -port " + REMOTE_PLATFORM_PORT + " -mtp " + mtp + " " + TestUtility.HTTP_MTP_ARG, new String[] {proto});

				// Construct the AID of the AMS of the remote platform
				AID remoteAMS = new AID("ams@" + REMOTE_PLATFORM_NAME, AID.ISGUID);
				Iterator iter = main.getAddresses().iterator();
				while (iter.hasNext()) {
					remoteAMS.addAddresses((String) iter.next());
				}

				// Start a local container with an MTP to communicate with te remote platform
				mtpCont = TestUtility.launchJadeInstance("Container-mtp", "+" + TestUtility.HTTP_MTP_CLASSPATH,
						"-container -host " + TestUtility.getLocalHostName() + " -port " + String.valueOf(Test.DEFAULT_PORT) +
						" -mtp " + mtp + " " + TestUtility.HTTP_MTP_ARG, null);

				// Store the remote platform AMS AID as a group argument
				setArgument(REMOTE_AMS_AID_KEY, remoteAMS);
			}

			protected void shutdown(Agent a) {
				mtpCont.kill();
				main.kill();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
