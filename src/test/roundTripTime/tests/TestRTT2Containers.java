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

package test.roundTripTime.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import test.common.*;
import test.roundTripTime.RoundTripTimeTesterAgent;

/**
   @author Giovanni Caire - TILAB
 */
public class TestRTT2Containers extends Test {
	public static final String TEST_NAME = "Test Round Trip Time - Two Containers";
	
	private int nCouples;
	private int nIterations;
	private JadeController jc1, jc2;
	
  public String getName() {
  	return TEST_NAME;
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("TBD");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException { 
  	try {
  		final DataStore store = ds;
  		final String key = resultKey;
  		
    	// Get arguments
    	nCouples = Integer.parseInt((String) getGroupArgument(RoundTripTimeTesterAgent.N_COUPLES_KEY));
    	nIterations = Integer.parseInt((String) getGroupArgument(RoundTripTimeTesterAgent.N_ITERATIONS_KEY));
			String addClasspath1 = (String) getGroupArgument(RoundTripTimeTesterAgent.BENCHMARK_CLASSPATH1_KEY);
			String addClasspath2 = (String) getGroupArgument(RoundTripTimeTesterAgent.BENCHMARK_CLASSPATH2_KEY);

			// Launch a peripheral container with the proper classpath for the senders
			if (addClasspath1 != null) {
				addClasspath1 = new String("+"+addClasspath1);
			}
	    jc1 = TestUtility.launchJadeInstance("C-senders", addClasspath1, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT), new String[] {});
    	String containerName1 = jc1.getContainerName();
    	
			// Launch a peripheral container with the proper classpath for the receivers
			if (addClasspath2 != null) {
				addClasspath2 = new String("+"+addClasspath2);
			}
	    jc2 = TestUtility.launchJadeInstance("C-receivers", addClasspath2, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT), new String[] {});
    	String containerName2 = jc2.getContainerName();
    	
	    // Launch receivers on container 2 (receivers must be launched first)
  	  for (int i = 0; i < nCouples; ++i) {
  	  	String name = new String("r"+i);
    		TestUtility.createAgent(a, name, RoundTripTimeTesterAgent.RECEIVER_CLASS, null, null, containerName2); 
  	  }
    
	    // Launch senders on container 1
  	  for (int i = 0; i < nCouples; ++i) {
  	  	String senderName = new String("s"+i);
  	  	String receiverName = new String("r"+i);
    		String[] args = new String[] {receiverName, String.valueOf(nIterations), "", String.valueOf(nCouples), a.getLocalName()}; 
    		TestUtility.createAgent(a, senderName, RoundTripTimeTesterAgent.ROUNDTRIPPER_CLASS, args, null, containerName1); 
  	  }
    
    	// Create the behaviour to return  	  
    	Behaviour b = new SimpleBehaviour(a) {
    		private boolean finished = false;
    		
    		public void action() {
    			ACLMessage msg = myAgent.receive();
    			if (msg != null) {
    				Logger l = Logger.getLogger();
    				l.log("---------------------------------------------------------------------");
    				l.log("Average RTT ["+nCouples+" couples, "+nIterations+" iterations] = "+msg.getContent());
    				l.log("---------------------------------------------------------------------");
    				finished = true;
    			}
    			else {
	    			block();
    			}
    		}
    	
    		public boolean done() {
    	 		return finished;
    		}
    	
    		public int onEnd() {
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
  	  // Don't waste time killing senders and receivers one by one,
  	  // but just kill the container processes.
  	  jc1.kill();
  	  jc2.kill();
  	  
  	  // Wait a bit to allow the AMS to perform all clean-up operations 
  	  // properly.
  	  try {
  	  	Thread.sleep(5000);
  	  }
  	  catch (InterruptedException ie) {
  	  	ie.printStackTrace();
  	  }
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  	
}

