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

package test.leap;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.AID;
import jade.wrapper.*;
import test.common.*;

/**
   @author Givanni Caire - TILAB
 */
public class LEAPTesterAgent extends TesterAgent {

	// Names and default values for group arguments
	public static final String LIGHT_CONTAINER_KEY = "light-container";
	public static final String LIGHT_CONTAINER_DEFAULT = "Container-1";
		
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/leap/LEAPTestsList.xml"){		
			
			public void initialize(Agent a) throws TestException {
				Logger.getLogger().log("************ NOTE *************");
				Logger.getLogger().log("You should manually start a JADE container on a wireless device using the testleap.jar (MIDP) or testleappjava.jar (PJAVA) jar file for this group of tests to be executed.");
				Logger.getLogger().log("The name of this container must be set as group argument to this group of tests (default \"Container-1\").");
				Logger.getLogger().log("*******************************\n");
			}			
		};
		
		tg.specifyArgument(LIGHT_CONTAINER_KEY, "Light container name", LIGHT_CONTAINER_DEFAULT);
		return tg;
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

      AgentController tester = mc.createNewAgent("tester", "test.leap.LEAPTesterAgent", args);
      tester.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
