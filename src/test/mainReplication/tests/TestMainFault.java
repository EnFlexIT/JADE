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
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.*;
import test.common.testSuite.TestSuiteAgent;

/**
   Test the recovery capability from a fault of the main container. 
   @author Giovanni Caire - TILAB
 */
public class TestMainFault extends Test {
	private static final int BACKUPMAIN_PORT = 1234;
	private static final int PERIPHERAL_PORT = 5678;
	
	private static final String PING_NAME = "ping";
	private static final String PING_ID = "ping-id";
	
	private JadeController backupMain, peripheral;
	private String backupMainHost;
  
  public Behaviour load(Agent a) throws TestException {
  	if (TestSuiteAgent.mainController == null) {
  		throw new TestException("This test can only be executed from the JADE TestSuite");
  	}
  	
  	SequentialBehaviour sb = new SequentialBehaviour(a);
  	// Step 1: Start a backup main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
	  			// Start a backup main container
					log("1) Starting backup main container ...");
					backupMain = TestUtility.launchJadeInstance("Backup-Main", null, "-backupmain -nomtp -services "+TestSuiteAgent.MAIN_SERVICES+" -local-port "+BACKUPMAIN_PORT+" -host "+TestUtility.getLocalHostName()+" -port "+Test.DEFAULT_PORT, null);
					log("Backup main container correctly started");
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
					peripheral = TestUtility.launchJadeInstance("Peripheral", null, "-container -services jade.core.replication.AddressNotificationService -local-port "+PERIPHERAL_PORT+" -host "+backupMainHost+" -port "+BACKUPMAIN_PORT+" rma1:jade.tools.rma.rma", null);
					log("Peripheral container correctly connected to the backup main");
  			}
  			catch (Exception e) {
  				failed("Error attaching a peripheral container to the backup main. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 3: Start a platform events listener agent
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  		}
  	} );
  	
  	// Step 4: Kill the master main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			log("3) Killing master main container...");
  			TestSuiteAgent.mainController.kill();
  			log("Master main container killed. Wait a bit to enable platform recovery...");
  			try {
  				Thread.sleep(5000);
  			}
  			catch (Exception e) {
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 5: Check that things still work
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
				log("4) Checking platform activity ...");
  			try {
	  			check(myAgent, 1);
	  			log("The platform works properly after master main container fault.");
  			}
  			catch (Exception e) {
  				failed("The platform does not work properly after master main container fault. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 6: Restore the old main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
				log("5) Restoring old master (now backup) main container ...");
  			try {
					TestSuiteAgent.mainController = TestUtility.launchJadeInstance("Main", null, "-backupmain -gui -nomtp -host "+backupMainHost+" -port "+BACKUPMAIN_PORT+" -local-port "+Test.DEFAULT_PORT+" -services "+TestSuiteAgent.MAIN_SERVICES, null);
					log("Old master (now backup) main container correctly restored");
  			}
  			catch (Exception e) {
  				failed("Error restoring old master main container. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 7: Kill the new master main container
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			log("6) Killing new master main container...");
  			backupMain.kill();
  			log("New master main container killed. Wait a bit to enable platform recovery...");
  			try {
  				Thread.sleep(5000);
  			}
  			catch (Exception e) {
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 8: Check again that things still work
  	sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
				log("7) Checking platform activity ...");
  			try {
	  			check(myAgent, 2);
	  			passed("The platform works properly after new master main container fault.");
  			}
  			catch (Exception e) {
  				failed("The platform does not work properly after new master main container fault. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	return sb;
  }
  
  public void clean(Agent a) {
  	// Kill the peripheral container
  	if (peripheral != null) {
			peripheral.kill();
  	}
  }  	
  
  private void check(Agent a, int step) throws TestException {
  	AID ping = TestUtility.createAgent(a, PING_NAME+String.valueOf(step), "test.mainReplication.tests.TestMainFault$PingAgent", null, null, peripheral.getContainerName());
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  	msg.addReceiver(ping);
  	msg.setConversationId(PING_ID);
  	a.send(msg);
  	msg = a.blockingReceive(MessageTemplate.MatchConversationId(PING_ID), 10000);
  	TestUtility.killAgent(a, ping);
  	if (msg != null) {
  		if (msg.getPerformative() != ACLMessage.INFORM) {
  			throw new TestException("FAILURE");
  		}
  	}
  	else {
  		throw new TestException("Timeout expired");
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
