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

package test.domain.df.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import test.common.*;
import test.domain.df.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestDFCleanUp extends Test {
	private AID id;
	
  public Behaviour load(Agent a) throws TestException {
  	// Starts an agent that registers with the DF
  	id = TestUtility.createAgent(a, "r", "test.domain.df.tests.TestDFCleanUp$RegistererAgent", null);
  	
  	Behaviour b = new OneShotBehaviour(a) {
  		public void action() {
  			// Wait a bit to be sure the registsrer agent has registered
  			try {
  				Thread.sleep(2000);
  			}
  			catch (Exception e) {}
  			
  			// Check the registration 
  			log("Checking registration...");
  			DFAgentDescription template = new DFAgentDescription();
  			template.setName(id);
  			DFAgentDescription[] result = null;
  			try {
	  			result = DFService.search(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints());
  			}
  			catch (FIPAException fe) {
  				fe.printStackTrace();
  				failed("DF search failed. "+fe);
  				return;
  			}	
  			if (result.length != 1 || (!id.equals(result[0].getName()))) {
  				failed("Registration NOT OK");
  				return;
  			}
				log("Registration OK");
  			
  			// Kill the registerer agent.
				try {
					log("Killing registerer agent...");
					TestUtility.killAgent(myAgent, id);
					log("Registerer agent killed");
				}
				catch (Exception e) {
					e.printStackTrace();
					failed("Error killing registerer agent. "+e);
				}
				
  			// Wait a bit to be sure the DF has cleaned up
  			try {
  				Thread.sleep(2000);
  			}
  			catch (Exception e) {}
				
  			// Check if the registration has been cleaned up
  			log("Check if the registration has been cleaned up...");
  			try {
	  			result = DFService.search(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints());
  			}
  			catch (FIPAException fe) {
  				fe.printStackTrace();
  				failed("DF search-2 failed");
  				return;
  			}	
  			if (result.length != 0) {
  				failed(result.length+" items found while 0 were expected");
  				return;
  			}
  			passed("Registration correctly cleaned up");
  		}
  	};
  	
  	return b;
  }
  
  public static class RegistererAgent extends Agent {
  	protected void setup() {
  		try {
  			System.out.println("Agent "+getLocalName()+" registering with DF.");
  			DFAgentDescription dfd = new DFAgentDescription();
  			dfd.setName(getAID());
  			DFService.register(this, dfd);
  			System.out.println("Agent "+getLocalName()+" registerer.");
  		}
  		catch (Exception e) {
  			e.printStackTrace();
  		}
  	}
  }
}
