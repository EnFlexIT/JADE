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

package test.mainReplication.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.introspection.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import test.common.*;
import test.common.testSuite.TestSuiteAgent;

import java.util.Map;
import java.util.HashMap;

/**
   Test the recovery capability from a fault of the main container. 
   @author Giovanni Caire - TILAB
 */
public class TestMainFault extends Test {
	private static final int BACKUPMAIN_PORT = 1234;
	private static final int PERIPHERAL_PORT = 5678;
	
	private static final String PING_NAME = "ping";
	private static final String PING_ID = "ping-id";
	
	private static final String RMA = "rma1";
	
	private JadeController backupMain, peripheral;
	private String masterMainHost;
	private String backupMainHost;
	
	private HashMap agents = new HashMap();
	private AMSSubscriber subscriber;
  
  public Behaviour load(Agent a) throws TestException {
  	if (TestSuiteAgent.mainController == null) {
  		throw new TestException("This test can only be executed from the JADE TestSuite");
  	}
  	
  	//enablePause(true);
  	
  	SequentialBehaviour sb = new SequentialBehaviour(a);
  	// Step 1: Start a backup main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
	  			// Start a backup main container
					log("1) Starting backup main container ...");
	  			masterMainHost = TestUtility.getContainerHostName(myAgent, TestSuiteAgent.mainController.getContainerName());
					backupMain = TestUtility.launchJadeInstance("Backup-Main", null, "-backupmain -nomtp -name "+TestSuiteAgent.TEST_PLATFORM_NAME+" -services "+TestSuiteAgent.MAIN_SERVICES+" -local-port "+BACKUPMAIN_PORT+" -host "+masterMainHost+" -port "+Test.DEFAULT_PORT, null);
					log("Backup main container correctly started");
					pause();
  			}
  			catch (Exception e) {
  				failed("Error creating backup main container. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 2: Attach a peripheral container to the backup main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
	  			// Get the host the backup main container is running on 
					log("2) Attaching a peripheral container to the backup main container ...");
	  			backupMainHost = TestUtility.getContainerHostName(myAgent, backupMain.getContainerName());
					log("Backup main is running on host "+backupMainHost);
					// Start a peripheral container connecting to the backup main
					String smaddrs = masterMainHost+":"+Test.DEFAULT_PORT+";"+backupMainHost+":"+BACKUPMAIN_PORT;
					peripheral = TestUtility.launchJadeInstance("Peripheral", null, "-container -smaddrs "+smaddrs+" -local-port "+PERIPHERAL_PORT+" -host "+backupMainHost+" -port "+BACKUPMAIN_PORT+" "+RMA+":jade.tools.rma.rma", null);
					log("Peripheral container correctly connected to the backup main");
					pause();
  			}
  			catch (Exception e) {
  				failed("Error attaching a peripheral container to the backup main. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 3: Kill the master main container
  	// We give some time to the RMA to register as a tool 
  	sb.addSubBehaviour(new WakerBehaviour(a, 10000) {
  		protected void handleElapsedTimeout() {
  			log("3) Killing master main container...");
  			TestSuiteAgent.mainController.kill();
  			log("Master main container killed.");
				pause();
  		}
  	} );
  	
  	// Step 4: Check that things still work
  	sb.addSubBehaviour(new WakerBehaviour(a, 10000) {
  		public void onStart() {
  			log("Wait a bit to enable platform recovery...");
  			super.onStart();
  		}
  		
  		protected void handleElapsedTimeout() {
				log("4) Checking platform activity ...");
  			try {
	  			check(myAgent, 1, backupMain.getContainerName());
	  			log("The platform works properly after master main container fault.");
					pause();
  			}
  			catch (Exception e) {
  				failed("The platform does not work properly after master main container fault. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 5: Restore the old main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
				log("5) Restoring old master (now backup) main container ...");
  			try {
					TestSuiteAgent.mainController = TestUtility.launchJadeInstance("Main", null, "-backupmain -gui -nomtp -host "+backupMainHost+" -port "+BACKUPMAIN_PORT+" -local-port "+Test.DEFAULT_PORT+" -services "+TestSuiteAgent.MAIN_SERVICES+" -container-name Main-Container -name "+TestSuiteAgent.TEST_PLATFORM_NAME, null);
					log("Old master (now backup) main container correctly restored");
					pause();
  			}
  			catch (Exception e) {
  				failed("Error restoring old master main container. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 6: Kill the new master main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			log("6) Killing new master main container...");
  			backupMain.kill();
  			log("New master main container killed.");
				pause();
  		}
  	} );
  	
  	// Step 7: Check again that things still work
  	sb.addSubBehaviour(new WakerBehaviour(a, 10000) {
  		public void onStart() {
  			log("Wait a bit to enable platform recovery...");
  			super.onStart();
  		}
  		
  		protected void handleElapsedTimeout() {
				log("7) Checking platform activity ...");
  			try {
	  			check(myAgent, 2, TestSuiteAgent.mainController.getContainerName());
					pause();
	  			passed("The platform works properly after new master main container fault.");
  			}
  			catch (Exception e) {
  				failed("The platform does not work properly after new master main container fault. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
		// The AMSSubscriber used to check if AMS notifications are correctly
  	// sent when a fault occurs
  	subscriber = new AMSSubscriber() {
  		protected void installHandlers(Map handlers) {
	      handlers.put(IntrospectionOntology.ADDEDCONTAINER, new EventHandler() {
	      	public void handle(Event ev) {
		    		AddedContainer ac = (AddedContainer)ev;
		    		ContainerID cid = ac.getContainer();
		    		if (agents.containsKey(cid)) {
	    				failed("Received ADDED_CONTAINER event for container "+cid.getName()+" that already exists");
		    		}
		    		else {
			    		List l = new ArrayList();
			    		agents.put(cid, l);
		    		}
		  		}
	      });
	      
	      handlers.put(IntrospectionOntology.REMOVEDCONTAINER, new EventHandler() {
	      	public void handle(Event ev) {
		    		RemovedContainer rc = (RemovedContainer)ev;
		    		ContainerID cid = rc.getContainer();
		    		if (!agents.containsKey(cid)) {
	    				failed("Received REMOVED_CONTAINER event for unknown container "+cid.getName());
		    		}
		    		else {
			    		agents.remove(cid);
		    		}
		  		}
	      });
	      
	      handlers.put(IntrospectionOntology.BORNAGENT, new EventHandler() {
	      	public void handle(Event ev) {
		    		BornAgent ba = (BornAgent)ev;
		    		AID born = ba.getAgent();
		    		ContainerID cid = ba.getWhere();
		    		List l = (List) agents.get(cid);
		    		if (l != null) {
		    			if (!l.contains(born)) {
		    				l.add(born);
		    			}
		    			else {
		    				failed("Received BORN_AGENT event for agent "+born.getName()+" that already exists");
		    			}
		    		}
		    		else {
		    			failed("Received BORN_AGENT event for agent "+born.getName()+" on unknown container "+cid.getName());
		    		}
		  		}
	      });

	      handlers.put(IntrospectionOntology.DEADAGENT, new EventHandler() {
	      	public void handle(Event ev) {
		    		DeadAgent da = (DeadAgent)ev;
		    		AID dead = da.getAgent();
		    		ContainerID cid = da.getWhere();
		    		List l = (List) agents.get(cid);
		    		if (l != null) {
		    			if (!l.remove(dead)) {
		    				log("WARNING: Received DEAD_AGENT event for unknown agent "+dead.getName());
		    			}
		    		}
		    		else {
		    			log("WARNING: Received DEAD_AGENT event for agent "+dead.getName()+" on unknown container "+cid.getName());
		    		}
		  		}
	      });

	      handlers.put(IntrospectionOntology.META_RESETEVENTS, new EventHandler() {
	      	public void handle(Event ev) {
	      		agents.clear();
		  		}
	      });
  		}
  	};
  	
  	a.addBehaviour(subscriber);
  	
  	return sb;
  }
  
  public void clean(Agent a) {
  	// Remove the AMSSubscriber and unsubscribe from the AMS
  	a.removeBehaviour(subscriber);
  	a.send(subscriber.getCancel());
  	agents.clear();
  	
  	// Kill the peripheral container
  	if (peripheral != null) {
			peripheral.kill();
  	}
  }  	
  
  private void check(Agent a, int step, String mainName) throws TestException {
  	// Check that agent management and communication works properly
  	AID ping = TestUtility.createAgent(a, PING_NAME+String.valueOf(step), "test.mainReplication.tests.TestMainFault$PingAgent", null, null, peripheral.getContainerName());
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  	msg.addReceiver(ping);
  	msg.setConversationId(PING_ID);
  	a.send(msg);
  	msg = a.blockingReceive(MessageTemplate.MatchConversationId(PING_ID), 10000);
  	if (msg != null) {
  		if (msg.getPerformative() != ACLMessage.INFORM) {
  			throw new TestException("FAILURE");
  		}
  	}
  	else {
  		throw new TestException("Timeout expired");
  	}
  	TestUtility.killAgent(a, ping);
  	
  	// Check that AMS subscribers have been correctly notified
  	// a) Check that my container contains me
  	List l = (List) agents.get(a.here());
  	if (l != null) {
  		if (!l.contains(a.getAID())) {
  			throw new TestException("Agent "+a.getName()+" not found on container "+a.here().getName());
  		}
  	}
  	else {
  		throw new TestException("Container "+a.here().getName()+" not found");
  	}
  	// b) Check that the peripheral container contains the RMA
  	l = (List) agents.get(new ContainerID(peripheral.getContainerName(), null));
  	if (l != null) {
  		if (!l.contains(new AID(RMA, AID.ISLOCALNAME))) {
  			throw new TestException("Agent "+RMA+" not found on container "+peripheral.getContainerName());
  		}
  	}
  	else {
  		throw new TestException("Container "+peripheral.getContainerName()+" not found");
  	}
  	// c) Check that the AMS is on the current master main
  	l = (List) agents.get(new ContainerID(mainName, null));
  	if (l != null) {
  		if (!l.contains(a.getAMS())) {
  			throw new TestException("Agent ams not found on master main container "+mainName);
  		}
  	}
  	else {
  		throw new TestException("Master main container "+mainName+" not found");
  	}
  }
  
  /**
     Inner class PingAgent
   */
  public static class PingAgent extends Agent {
  	protected void setup() {
  		addBehaviour(new CyclicBehaviour(this) {
  			public void action() {
  				ACLMessage msg = myAgent.receive();
  				if (msg != null) {
  					ACLMessage reply = msg.createReply();
  					reply.setPerformative(ACLMessage.INFORM);
  					myAgent.send(reply);
  				}
  				else {
  					block();
  				}
  			}
  		} );
  	}
  } // END of inner class PingAgent
}
