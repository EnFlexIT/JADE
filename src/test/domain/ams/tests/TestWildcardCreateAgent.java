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

package test.domain.ams.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import test.common.*;
import jade.domain.JADEAgentManagement.JADEManagementVocabulary;

/**
 * The test asks the AMS to create an agent that has wildcards in its name, and verify if it's
 * correctly created. 
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */


public class TestWildcardCreateAgent extends Test {
	
	private static final String PREFIX = "prefix_";
	private static final String SUFFIX = "_suffix";
	private static final String CONTAINER_NAME = "dummy_container";
	
	private JadeController jc = null;
	
	public Behaviour load(Agent a) throws TestException {  	
		
		log("Creating container...");
		jc = TestUtility.launchJadeInstance("Container-1", null, new String("-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -container-name "+CONTAINER_NAME), null);
		
		return new OneShotBehaviour(a) {
  		public void action() {
  			try {
	  			log("Creating agent with wildcards...");
	  			TestUtility.createAgent(myAgent, PREFIX+JADEManagementVocabulary.CONTAINER_WILDCARD+SUFFIX, Agent.class.getName(), null, null, jc.getContainerName());
	  			log("Agent correctly created.");
	  			String containerName = jc.getContainerName();
	  			AID wildcardAgent = new AID(PREFIX + containerName + SUFFIX, AID.ISLOCALNAME);
	  			try {
		  			log("Killing agent "+wildcardAgent.getName()+"...");
	  				TestUtility.killAgent(myAgent, wildcardAgent);
	  				passed("Agent "+wildcardAgent.getName()+" found and killed as expected.");
	  			}
	  			catch (Exception e) {
	  				failed("Cannot kill agent "+wildcardAgent.getName()+". "+e);
	  				e.printStackTrace();
	  			}
  			}
  			catch (TestException te) {
  				failed("Error starting container with wildcarded bootstrap agent. "+te);
  				te.printStackTrace();
  			}
  		}
  	};
	}
	
	public void clean(Agent a) {
  	if (jc != null) {
	  	jc.kill();
  	}
  }
	
}
