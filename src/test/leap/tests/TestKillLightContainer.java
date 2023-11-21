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

package test.leap.tests;

import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.domain.JADEAgentManagement.KillContainer;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.util.leap.List;

import test.common.*;
import test.leap.LEAPTesterAgent;

/**
   Tests the KillContainer operation on a light container.
   @author Giovanni Caire - TILAB
 */
public class TestKillLightContainer extends Test {	  
	private String lightContainerName = "Container-1";
	private ContainerID lightContainer;

  public Behaviour load(Agent a) throws TestException {
  	setTimeout(90000);
  	
		// Light container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);
		lightContainer = new ContainerID(lightContainerName, null);
  	
  	SequentialBehaviour sb = new SequentialBehaviour(a);
  	// Step 1: Kill the light container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			log("1) Killing light container...");
  			try {
  				KillContainer kc = new KillContainer();
  				kc.setContainer(lightContainer);
  				TestUtility.requestAMSAction(myAgent, myAgent.getAMS(), kc);
	  			log("Light container correctly killed");
  			}
  			catch (TestException te) {
  				failed("Error killing light container. "+te.getMessage());
  				te.printStackTrace();
  			}  				
  		}
  	} );
  	  	
  	// Step 2: Check that the light container is no longer included 
  	// among the platform locations.
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
				log("2) Checking that the light conatiner is no longer included among the platform locations...");
  			try {
  				QueryPlatformLocationsAction qpl = new QueryPlatformLocationsAction();
  				List locations = (List) TestUtility.requestAMSAction(myAgent, myAgent.getAMS(), qpl);
	  			log("Platform locations retrieved");
	  			if (!locations.contains(lightContainer)) {
	  				passed("Light container correctly removed from platform locations.");
	  			}
	  			else {
	  				failed("Light container still present among platform locations.");
	  			}
  			}
  			catch (TestException te) {
  				failed("Error retrieving platform locations. "+te.getMessage());
  				te.printStackTrace();
  			}  				
  		}
  	} );
  	
  	return sb;
  }  
}
