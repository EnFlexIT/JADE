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

package test.behaviours.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import test.common.*;
import test.behaviours.PerformanceTesterAgent;

/**
   @author Giovanni Caire - TILAB
 */
public class TestPerformance extends Test {
	public static final String TEST_NAME = "Test behaviour performance";
	
	private static final String HELPER_CLASS = "test.behaviours.PerformanceHelperAgent";
	private static final String HELPER_NAME = "helper";
	private static final String HELPER_CONV_ID = "helper-conv-id";

	private int nIterations;
  private ACLMessage startMsg = new ACLMessage(ACLMessage.REQUEST);
  private AID helper;
	
  public String getName() {
  	return TEST_NAME;
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException { 
  	try {
  		final DataStore store = ds;
  		final String key = resultKey;
  		
    	// Get arguments
    	nIterations = Integer.parseInt((String) getGroupArgument(PerformanceTesterAgent.N_ITERATIONS_KEY));
    	
	    // Launch the helper agent that actually runs all the behaviours
    	helper = TestUtility.createAgent(a, HELPER_NAME, HELPER_CLASS, null, a.getAMS(), null); 
    
    	// Prepare the message to tell the helper to start
    	startMsg.addReceiver(helper);
    	startMsg.setConversationId(HELPER_CONV_ID);
    	
    	// Create the behaviour to return
    	Behaviour b = new SimpleBehaviour(a) {
    		private int cnt = 0;
    		private long initTime;
    		MessageTemplate template = MessageTemplate.MatchConversationId(HELPER_CONV_ID);
    		
    		public void onStart() {
    			initTime = System.currentTimeMillis();
    			myAgent.send(startMsg);
    		}
    			
    		public void action() {
    			ACLMessage msg = myAgent.receive(template);
    			if (msg != null) {
    				myAgent.send(startMsg);
    				cnt++;
    			}
    			else {
	    			block();
    			}
    		}
    	
    		public boolean done() {
    	 		return (cnt >= nIterations);
    		}
    	
    		public int onEnd() {
    			long finalTime =  System.currentTimeMillis();
    			long avgTime = (finalTime - initTime) / nIterations;
    			Logger l = Logger.getLogger();
    			l.log("------------------------------");
    			l.log("Average time = "+avgTime+" ms");
    			l.log("------------------------------");
					store.put(key, new Integer(Test.TEST_PASSED));
    			return 0;
    		}
    	};
    	
  		return b;
  	}
  	catch (TestException te) {
  		throw te;
  	}
  	catch (Exception e) {
  		throw new TestException("Error loading test", e);
  	}
  }
					
  public void clean(Agent a) {
  	try {
  		// Kill the helper
    		TestUtility.killAgent(a, helper); 
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  	
}

