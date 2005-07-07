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

package test.inProcess.test;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import test.common.*;
import jade.wrapper.*;
/**
 * Tests the creation using the in-process interface of an agent having wildcards in its name.
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public class TestWildcardCreateAgent extends Test {
	
	private static final String CONTAINER_NAME= "dummy_container";
	private static final String PREFIX="prefix_";
	private static final String SUFFIX="_suffix";
	ContainerController controller;
	
	public Behaviour load(Agent a) throws TestException {
		
		Behaviour b = new OneShotBehaviour(a) {	
			public void action(){
				try{
					//creo il container
					ProfileImpl theProfile = new ProfileImpl(false);
					theProfile.setParameter("host", TestUtility.getLocalHostName());
					theProfile.setParameter("port", String.valueOf(Test.DEFAULT_PORT));
					theProfile.setParameter("container-name", CONTAINER_NAME);
					controller = Runtime.instance().createAgentContainer(theProfile);
				  
					if(controller != null){
						//creo l'agente con la in-process interface	
						log("Creating agent with wildcards in container " + controller.getContainerName()+ " ...");
						AgentController ac = controller.createNewAgent(PREFIX+"#C"+SUFFIX, Agent.class.getName(), null);
						ac.start();
						log("Agent created successfully.");
					}
					
					String containerName = controller.getContainerName();
					AID wildcardAgent = new AID(PREFIX+containerName+SUFFIX, AID.ISLOCALNAME);
					
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
				catch(Exception e){
					failed("Error executing test. "+e);
  				e.printStackTrace();
				}
			}
		};
		return b;
		
	}
	
	public void clean(Agent a) {
		if(controller != null){
			try{
				controller.kill();
			}
			catch(StaleProxyException spe){
				spe.printStackTrace();
			}
		}
		
	}
}
