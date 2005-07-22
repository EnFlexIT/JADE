package test.leap.split.tests;

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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementVocabulary;
import test.common.*;


/**
 * The test start a leap split-container specifying on the command line an agent 
 * including the #C wildcard in its name.
 * The test is successfully extecuted if the agent exists and can be be properly killed. 
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public class TestBootstrapAgentBasic extends Test{
	
	private JadeController jc = null;
	private static String PREFIX = "prefix_";
	private static String SUFFIX = "_suffix";

	
	public Behaviour load(Agent a) throws TestException {  
		
		log("Creating split container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT) + " " + PREFIX+JADEManagementVocabulary.CONTAINER_WILDCARD+SUFFIX+":jade.core.Agent"));
		
		return new OneShotBehaviour(a) {
			public void action() {
  			String containerName = jc.getContainerName();
  			AID wildcardAgent = new AID(PREFIX + containerName + SUFFIX, AID.ISLOCALNAME);
  			try {
	  			log("Killing agent "+wildcardAgent.getName()+"...");
	  			//to be sure that the agent has been created.
	  			Thread.sleep(1000); 
  				TestUtility.killAgent(myAgent, wildcardAgent);
  				passed("Agent "+wildcardAgent.getName()+" found and killed as expected.");
  			}
  			catch (Exception e) {
  				failed("Cannot kill agent "+wildcardAgent.getName()+". "+e);
  				e.printStackTrace();
  			}
	  	}
  	};
	}
	
	public void clean(Agent a) {
		
		try{
			TestUtility.killContainer(a, jc.getContainerName());
		}catch(TestException te){
			te.printStackTrace();
		}
		if (jc != null) {
	  	jc.kill();
  	}
	}

}
