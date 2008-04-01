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

package test.mobility.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.mobility.*;
import jade.domain.JADEAgentManagement.*;
import jade.util.leap.*;
import test.common.*;

/**
 @author Giovanni Caire - TILAB
 */
public class TestMigration extends Test {
	private static final String REMOTE_PLATFORM_NAME = "Remote-platform";
	private static final int REMOTE_PLATFORM_PORT = 9003;
	private static final String MOBILE_AGENT_NAME = "ma";
	
	private AID ma;
	private JadeController jcp, jcc;
	
	public Behaviour load(Agent a) throws TestException {
		// Launch the MobileAgent
		ma = TestUtility.createAgent(a, MOBILE_AGENT_NAME, "examples.mobile.MobileAgent", null, a.getAMS(), a.here().getName());
		
		// Launch a remote platform
		jcp = TestUtility.launchJadeInstance(REMOTE_PLATFORM_NAME, "+"+TestUtility.HTTP_MTP_CLASSPATH, new String("-name "+REMOTE_PLATFORM_NAME+" -port "+REMOTE_PLATFORM_PORT+" -mtp "+Test.DEFAULT_MTP+" "+TestUtility.HTTP_MTP_ARG), new String[]{Test.DEFAULT_PROTO}); 
		
		// Construct the AID of the AMS of the remote platform 
		final AID remoteAMS = new AID("ams@"+REMOTE_PLATFORM_NAME, AID.ISGUID);
		Iterator it = jcp.getAddresses().iterator();
		while (it.hasNext()) {
			remoteAMS.addAddresses((String) it.next());
		}
		
		final PlatformID remotePlatformLocation = new PlatformID(remoteAMS);
		
		
		// Launch another container with an MTP to communicate with the 
		// remote platform
		jcc = TestUtility.launchJadeInstance("Container-1", "+"+TestUtility.HTTP_MTP_CLASSPATH, new String("-container -host "+TestUtility.getContainerHostName(a, null)+" -port "+Test.DEFAULT_PORT+" -mtp "+Test.DEFAULT_MTP+" "+TestUtility.HTTP_MTP_ARG), null); 
		
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		// Step 1) Move the MA to the remote platform
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("--- Make the MobileAgent migrate to the remote platform");
				try {
					MobileAgentDescription dsc = new MobileAgentDescription();
					dsc.setName(ma);
					dsc.setDestination(remotePlatformLocation);
					MoveAction moveAct = new MoveAction();
					moveAct.setMobileAgentDescription(dsc);
					TestUtility.requestAMSAction(myAgent, null, moveAct, MobilityOntology.NAME, 10000);
					log("--- MobileAgent correctly migrated");
				}
				catch (Exception e) {
					failed("--- Error migrating MobileAgent. "+e);
					e.printStackTrace();
				}
			}
		});
		
		// Step 2) check the position of ma
		sb.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("--- Check the current position of the MobileAgent");
				try {
					WhereIsAgentAction wia = new WhereIsAgentAction();
					wia.setAgentIdentifier(ma);
					ContainerID location = (ContainerID) TestUtility.requestAMSAction(myAgent, remoteAMS, wia);
					if (location != null) {
						passed("--- MobileAgent correctly migrated to the remote platform");
					}
					else {
						failed("--- MobileAgent not found in the remote platform");
					}
				}
				catch (Exception e) {
					failed("--- Error requesting the cutrrent position of the MobileAgent. "+e);
					e.printStackTrace();
				}
			}
		});
		
		return sb;
	}
	
	public void clean(Agent a) {
		try {
			jcp.kill();
			jcc.kill();
			try {
				TestUtility.killAgent(a, ma);
			}
			catch (Exception e) {}
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	
}
