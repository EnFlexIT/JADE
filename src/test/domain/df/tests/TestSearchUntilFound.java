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
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import test.common.*;
import test.domain.df.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestSearchUntilFound extends Test {
  private AID r = null;
	private DFAgentDescription dfd1 = null;
	static private DFAgentDescription dfd2 = null;
  private Logger l = Logger.getLogger();
	
  public String getName() {
  	return "Test searchUntilFound()";
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests the searchUntilFound() method of the DFService class\n");
  	sb.append("More in details \n");
  	sb.append("- register DFD1 with the DF\n");
  	sb.append("- launch another agent that will register a DFD2 after 5 sec and DFD12 after other 10 sec.\n");
  	sb.append("- call searchUntilFound(template1 matching DFD1) (this should immediately return DFD1)\n");
  	sb.append("- call searchUntilFound(template2 matching DFD2) (this should return DFD2 as soos as the other agent registers DFD2)\n");
  	sb.append("- call searchUntilFound(template3 not matching, timeout == 5 sec) (this should return null when the timeout expires)\n");
  	sb.append("- wait some time to check that no further notifications are received from the DF\n");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	// Register DFD1
  	dfd1 = new DFAgentDescription();
  	dfd1.setName(new AID("a1", AID.ISLOCALNAME));
  	dfd1.addServices(TestDFHelper.getSampleSD1());
  	try {
	  	DFService.register(a, a.getDefaultDF(), dfd1);
  	}
  	catch (FIPAException fe) {
  		throw new TestException("Error registering a DFD with the DF.", fe);
  	}
  	
  	// Initialize DFD2
  	dfd2 = new DFAgentDescription();
  	dfd2.setName(new AID("a2", AID.ISLOCALNAME));
  	dfd2.addServices(TestDFHelper.getSampleSD2());
  	
  	// Create the DFRegisterer agent that will register DFD2
	  r = TestUtility.createAgent(a, "r", "test.domain.df.tests.TestSearchUntilFound$DFRegisterer", null, a.getAMS(), a.here().getName());
  	
	  Behaviour b = new OneShotBehaviour(a) {
  		private int ret;
  		
  		public void action() {
  			ret = Test.TEST_FAILED;
  		
  			// Make the DFRegisterer agent start 
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(r);
  			myAgent.send(msg);
  			
  			try {
	  			DFAgentDescription[] result = null;  			
  			
  				// First search
  				result = DFService.searchUntilFound(myAgent, myAgent.getDefaultDF(), TestDFHelper.getSampleTemplate1(), new SearchConstraints(), 10000);
  				if (!check(1, result, dfd1)) {
  					return;
  				}
  			
	  			// Second search
	  			result = DFService.searchUntilFound(myAgent, myAgent.getDefaultDF(), TestDFHelper.getSampleTemplate2(), new SearchConstraints(), 10000);
	  			if (!check(2, result, dfd2)) {
	  				return;
	  			}
	  			
	  			// Third search
	  			DFAgentDescription template = new DFAgentDescription();
	  			template.addOntologies("Non-matching-ontology");
	  			result = DFService.searchUntilFound(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints(), 5000);
	  			if (result == null) {
	  				l.log("Search 3 successful (No result found)");
	  			}
	  			else {
	  				l.log("Search 3 found "+result.length+", while 0 were expected");
	  				return;
	  			}
	  			
	  			// Wait to see if other messages arrive. This would mean that the 
	  			// CANCEL mechanism does not work
	  			msg = myAgent.blockingReceive(10000);
	  			if (msg != null) {
	  				l.log("Unexpected message received "+msg);
	  				return;
	  			}
	  			
	  			ret = Test.TEST_PASSED;
  			}
  			catch (FIPAException fe) {
  				l.log("Some error occurred during one of the search operation. "+fe);
  			}
  		}
  		
  		public int onEnd() {
  			store.put(key, new Integer(ret));
  			return 0;
  		}	
  		
  		private boolean check(int step, DFAgentDescription[] result, DFAgentDescription expectedDfd) {
  			if (result != null) {
  				if (result.length == 1) {
  					if (TestDFHelper.compare(result[0], expectedDfd)) {
  						l.log("Search "+step+" successful.");
  						return true;
  					}
  					else {
  						l.log("Error in search "+step+". The DFD found is different from the expected one");
  					}
  				}
  				else {
  					l.log("Error in search "+step+". "+result.length+" items found while 1 was expected.");
  				}
  			}
  			else {
  				l.log("Error in search "+step+". Timeout expired.");
  			}
  			return false;
  		}	
  	};
  	
  	return b;
  }
  
  public void clean(Agent a) {
  	try {
  		TestUtility.killTarget(a, r);
  		
  		// Deregister all descriptions
  		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(new AID("a1", AID.ISLOCALNAME));
	  	DFService.deregister(a, a.getDefaultDF(), dfd);
  		dfd.setName(new AID("a2", AID.ISLOCALNAME));
	  	DFService.deregister(a, a.getDefaultDF(), dfd);
  		dfd.setName(new AID("a3", AID.ISLOCALNAME));
	  	DFService.deregister(a, a.getDefaultDF(), dfd);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  public static class DFRegisterer extends Agent {
  	protected void setup() {
  		blockingReceive();
  		
  		addBehaviour(new TickerBehaviour(this, 5000) {
  			private int cnt = 0;
  			
  			protected void onTick() {
  				cnt++;
  				switch (cnt) {
  				case 1:
  					try {
  						Logger.getLogger().log("Registering DFD2");
  						DFService.register(myAgent, dfd2);
  					}
  					catch (Exception e) {
  						e.printStackTrace();
  					}	
  					break;
  				case 2:
  					break;
  				case 3:
  					try {
  						Logger.getLogger().log("Registering DFD3");
  						dfd2.setName(new AID("a3",AID.ISLOCALNAME));
  						DFService.register(myAgent, dfd2);
  					}
  					catch (Exception e) {
  						e.printStackTrace();
  					}	
  					stop();
  					break;
  				}
  			}
  		} );			
  	}
  }
}
