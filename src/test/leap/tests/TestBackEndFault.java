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

package test.leap.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import test.common.*;
import test.common.testSuite.TestSuiteAgent;
import test.leap.LEAPTesterAgent;

/**
   Test the recovery capability from a fault of the container hosting
   back-ends. 
   @author Giovanni Caire - TILAB
 */
public class TestBackEndFault extends Test {
	private static final int BACKUPMAIN_PORT = 1234;
	
	private static final String PING_AGENT = "ping";
	private static final String SENDER_AGENT = "sender";
	private static final String PING_CONV_ID = "__conv-id__";
	private static final int N_MESSAGES = 10;
	
	private JadeController backupMain, peripheral;
	private String masterMainHost;
	private String backupMainHost;
	  
	private String lightContainerName = "Container-1";
	private AID ping;
	private AID sender;

	private boolean pingCompleted = false;
	private boolean senderCompleted = false;
	
  public Behaviour load(Agent a) throws TestException {
  	if (TestSuiteAgent.mainController == null) {
  		throw new TestException("This test can only be executed from the JADE TestSuite");
  	}
  	 
  	setTimeout(60000);
  	
		// MIDP container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);

		// Start a backup main container
		log("Starting backup main container...");
		masterMainHost = TestUtility.getContainerHostName(a, TestSuiteAgent.mainController.getContainerName());
		backupMain = TestUtility.launchJadeInstance("Backup-Main", null, "-backupmain -nomtp -name "+TestSuiteAgent.TEST_PLATFORM_NAME+" -services "+TestSuiteAgent.MAIN_SERVICES+" -local-port "+BACKUPMAIN_PORT+" -host "+masterMainHost+" -port "+Test.DEFAULT_PORT, null);
		log("Backup main container correctly started");
  	
		// Start a PingAgent on the light container
		log("Creating a PingAgent on the light container...");
		ping = TestUtility.createAgent(a, PING_AGENT, "test.leap.midp.PingAgent", null, a.getAMS(), lightContainerName);
		log("Ping agent correctly created.");
		
		// Start a SenderAgent on the light container
		log("Creating a SenderAgent on the light container...");
		String[] args = new String[] {a.getLocalName(), String.valueOf(N_MESSAGES), "2000"}; // 10 messages one each 2 secs
		sender = TestUtility.createAgent(a, SENDER_AGENT, "test.leap.midp.SenderAgent", args, a.getAMS(), lightContainerName);
		log("Sender agent correctly created.");
		
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ALL);
  	
  	// The behaviour receiving messages from the sender agent
  	pb.addSubBehaviour(new CyclicBehaviour(a) {
  		private int cnt = 0;
  		public void action() {
  			ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(sender));
  			if (msg != null) {
  				try {
	  				int i = Integer.parseInt(msg.getContent());
	  				if (i == cnt) {
	  					log("Message # "+i+" received.");
	  					cnt++;
	  					if (cnt >= N_MESSAGES) {
	  						senderCompleted = true;
	  						if (pingCompleted) {
		  						passed("Test completed successfully");
	  						}
	  					}
	  				}
	  				else {
	  					failed("Received message # "+i+" while # "+cnt+" was expected");
	  				}
  				}
  				catch (Exception e) {
  					failed("Unexpected message "+msg.getContent());
  				}
  			}
  			else {
  				block();
  			}
  		}
  	} );
  	
  	// The behaviour that kills the master main container and then restores it
  	SequentialBehaviour sb = new SequentialBehaviour(a);
  	// Step 1: Kill the master main container after 5 secs
  	sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
  		protected void handleElapsedTimeout() {
  			log("1) Killing master main container...");
  			TestSuiteAgent.mainController.kill();
  			log("Master main container killed.");
  		}
  	} );
  	  	
  	// Step 2: Restore the old main container after 5 more secs
  	sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
  		public void handleElapsedTimeout() {
				log("2) Restoring old master (now backup) main container ...");
  			try {
					backupMainHost = TestUtility.getContainerHostName(myAgent, backupMain.getContainerName());
					TestSuiteAgent.mainController = TestUtility.launchJadeInstance("Main", null, "-backupmain -gui -nomtp -host "+backupMainHost+" -port "+BACKUPMAIN_PORT+" -local-port "+Test.DEFAULT_PORT+" -services "+TestSuiteAgent.MAIN_SERVICES+" -container-name Main-Container -name "+TestSuiteAgent.TEST_PLATFORM_NAME, null);
					log("Old master (now backup) main container correctly restored");
  			}
  			catch (Exception e) {
  				failed("Error restoring old master main container. "+e.getMessage());
  				e.printStackTrace();
  			}
  		}
  	} );
  	
  	// Step 3: Kill the new master main container aftre 5 more secs
  	sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
  		public void handleElapsedTimeout() {
  			log("3) Killing new master main container...");
  			backupMain.kill();
  			log("New master main container killed.");
  		}
  	} );
	
  	// Step 4: Ping the PingAgent
  	sb.addSubBehaviour(new Behaviour(a) {
  		private boolean finished = false;
  		
  		public void onStart() {
  			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  			msg.addReceiver(ping);
  			msg.setConversationId(PING_CONV_ID);
  			myAgent.send(msg);
  			log("Waiting for reply from ping agent.");
  		}
  		
  		public void action() {
  			ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(PING_CONV_ID));
  			if (msg != null) {
  				if (msg.getSender().equals(ping)) {
  					log("Reply from ping agent correctly received");
  					pingCompleted = true;
  					if (senderCompleted) {
  						passed("Test completed successfully");
  					}
  				}
  				else {
  					failed("Wrong message received. "+msg);
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
  	} );
  	
  	pb.addSubBehaviour(sb);
  	
  	return pb;
  }
  
  public void clean(Agent a) {
  	try {
  		TestUtility.killAgent(a, ping);
  		TestUtility.killAgent(a, sender);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  	
}
