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
import jade.util.leap.*;
import test.common.*;
import test.common.remote.*;
import test.roundTripTime.RoundTripTimeTesterAgent;

/**
   @author Giovanni Caire - TILAB
 */
public class TestRTT2Platforms2HostsIIOP extends Test {
	public static final String TEST_NAME = "Test Round Trip Time - Two Platforms, Two hosts, Sun ORB";
	
	public static final String REMOTE_PLATFORM_NAME = "Remote-platform";
	public static final String REMOTE_PLATFORM_PORT = "9003";
	
	private int nCouples;
	private int nIterations;
	private JadeController jc1, jc2;
	private RemoteManager originalRm;
	
  public String getName() {
  	return TEST_NAME;
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("TBD\n");
  	sb.append("NOTE: This test requires the TSDaemon running on the indicated remote host");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException { 
  	try {
  		final DataStore store = ds;
  		final String key = resultKey;
  		
  		// Save original default RemoteManager
  		originalRm = TestUtility.getDefaultRemoteManager();
  		TestUtility.setDefaultRemoteManager(null);
  		
    	// Get arguments
    	nCouples = Integer.parseInt((String) getGroupArgument(RoundTripTimeTesterAgent.N_COUPLES_KEY));
    	nIterations = Integer.parseInt((String) getGroupArgument(RoundTripTimeTesterAgent.N_ITERATIONS_KEY));
			String remoteHost = (String) getGroupArgument(RoundTripTimeTesterAgent.REMOTE_HOST_KEY);
			String addClasspath1 = (String) getGroupArgument(RoundTripTimeTesterAgent.BENCHMARK_CLASSPATH1_KEY);
			String addClasspath2 = (String) getGroupArgument(RoundTripTimeTesterAgent.BENCHMARK_CLASSPATH2_KEY);

			// Launch a peripheral container with the proper classpath for the senders
			// on the local host. Activate an MTP on it.
			if (addClasspath1 != null) {
				addClasspath1 = new String("+"+addClasspath1);
			}
	    jc1 = TestUtility.launchJadeInstance("C-senders", addClasspath1, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -mtp "+Test.DEFAULT_MTP, new String[] {});
    	String containerName = jc1.getContainerName();
  
    	// Launch a new platform with the proper classpath for the receivers
    	// on a remote host
			if (addClasspath2 != null) {
				addClasspath2 = new String("+"+addClasspath2);
			}
			RemoteManager rm = TestUtility.createRemoteManager(remoteHost, TSDaemon.DEFAULT_PORT, TSDaemon.DEFAULT_NAME);
			jc2 = TestUtility.launchJadeInstance(rm, REMOTE_PLATFORM_NAME, addClasspath2, "-name "+REMOTE_PLATFORM_NAME+" -port "+REMOTE_PLATFORM_PORT+" -mtp "+Test.DEFAULT_MTP, new String[]{Test.DEFAULT_PROTO}); 

			// Construct the AID of the AMS of the remote platform 
			String address = null;
			AID remoteAMS = new AID("ams@"+REMOTE_PLATFORM_NAME, AID.ISGUID);
			Iterator it = jc2.getAddresses().iterator();
			if (it.hasNext()) {
				address = (String) it.next();
				remoteAMS.addAddresses(address);
			}
			else {
				throw new TestException("Remote platform does not have any address");
			}
			System.out.println("Remote AMS is "+remoteAMS);
			  	
	    // Launch receivers on the remote platform (receivers must be launched first)
  	  for (int i = 0; i < nCouples; ++i) {
  	  	String name = new String("r"+i);
    		TestUtility.createAgent(a, name, RoundTripTimeTesterAgent.RECEIVER_CLASS, null, remoteAMS, null); 
  	  }
			System.out.println("Receivers created");
  	  
	    // Launch senders on peripheral container
  	  for (int i = 0; i < nCouples; ++i) {
  	  	String senderName = new String("s"+i);
  	  	String receiverName = new String("r"+i+"@"+REMOTE_PLATFORM_NAME);
    		String[] args = new String[] {receiverName, String.valueOf(nIterations), address, String.valueOf(nCouples), a.getLocalName()}; 
    		TestUtility.createAgent(a, senderName, RoundTripTimeTesterAgent.ROUNDTRIPPER_CLASS, args, null, containerName); 
  	  }
			System.out.println("Senders created");
    
    	// Create the behaviour to return  	  
    	Behaviour b = new SimpleBehaviour(a) {
    		private boolean finished = false;
    		private MessageTemplate template = MessageTemplate.or(
    			MessageTemplate.MatchPerformative(ACLMessage.INFORM),
    			MessageTemplate.MatchPerformative(ACLMessage.FAILURE));
    		
    		public void action() {
    			ACLMessage msg = myAgent.receive(template);
    			if (msg != null) {
	    			Logger l = Logger.getLogger();
	    			if (msg.getPerformative() == ACLMessage.INFORM) {
	    				try {
  	  					double rtt = Double.parseDouble(msg.getContent());
  		  				l.log("---------------------------------------------------------------------");
    						l.log("Average RTT ["+nCouples+" couples, "+nIterations+" iterations] = "+rtt);
    						l.log("---------------------------------------------------------------------");
								store.put(key, new Integer(Test.TEST_PASSED));
	    				}
	    				catch (Exception e) {
	    					l.log("Unexpected information received. Message is:\n"+msg);
								store.put(key, new Integer(Test.TEST_FAILED));
	    				}
	    			}
	    			else {
	    				// It must be a FAILURE
	    				l.log("FAILURE message received:\n"+msg);
							store.put(key, new Integer(Test.TEST_FAILED));
	    			}
    				finished = true;
    			}
    			else {
	    			block();
    			}
    		}
    	
    		public boolean done() {
    	 		return finished;
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
  	  // but just kill the container and platform processes.
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
  	  
  	  // Restore the original default RemoteManager
  		TestUtility.setDefaultRemoteManager(originalRm);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  	
}

