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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementVocabulary;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

/**
 * Test the creation of bootstrap agent having wildcards in its name.
 */
public class TestWildcardBootstrapAgent extends Test {
	
	private static final String PREFIX = "prefix_";
	private static final String SUFFIX = "_suffix";
	private static final String CONTAINER_NAME = "dummy_container";
	
	private JadeController jc;
	
  public Behaviour load(Agent a) throws TestException {  	
  	return new OneShotBehaviour(a) {
  		public void action() {
  			try {
	  			log("Creating container with wildcarded bootstrap agent...");
	  			jc = TestUtility.launchJadeInstance("Container-1", null, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -container-name "+CONTAINER_NAME+" "+PREFIX+JADEManagementVocabulary.CONTAINER_WILDCARD+SUFFIX+":jade.core.Agent", null); 
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
