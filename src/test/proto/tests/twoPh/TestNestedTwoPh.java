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

package test.proto.tests.twoPh;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.*;
import test.common.*;
import test.common.Logger;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

/**
   @author Giovanni Caire - TILAB
   @author Elena Quarantotto - TILAB
 */
public class TestNestedTwoPh extends Test {
	  
	private static final String INITIATOR = "initiator";
	private static final String RESPONDER = "responder";
	private static final String CONV_ID = "id";
	private static final String NESTED_CONV_ID = "nested-id";
	private static final String CONTROL_CONV_ID = "control-id";
	
	private AID initiator, responder;
	private boolean success = true;
	
  private static final String DUMMY_OUTPUT = "Dummy-output";
  private boolean firstTime;
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
    final Logger l = Logger.getLogger();
    
    // Start the initiator and the responder
    initiator = TestUtility.createAgent(a, INITIATOR, "test.proto.tests.twoPh.TestNestedTwoPh$Initiator", null);
    responder = TestUtility.createAgent(a, RESPONDER, "test.proto.tests.twoPh.TestNestedTwoPh$Responder", null);
    firstTime = true;
    
    // Create and return the behaviour that will actually perform the test
  	final TwoPhResponder tpr = new TwoPhResponder(a, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {
  		public void onStart() {
  			if (firstTime) {
	  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	  			msg.addReceiver(initiator);
	  			msg.setConversationId(CONTROL_CONV_ID);
	  			myAgent.send(msg);
	  			l.log("Startup message sent");
	  			firstTime = false;
  			}
  			super.onStart();
  		}
  	};

  	// Register a nested TwoPh0 initiator in the PREPARE_PROPOSE state
  	tpr.registerHandleCfp(new TwoPh0Initiator(a, null, DUMMY_OUTPUT) {
  		public void onStart() {
  			l.log("CFP received from initiator");
  			super.onStart();
  		}
  		
  		protected Vector prepareCfps(ACLMessage cfp) {
  			// Retrieve the CFP message received from the real initiator
  			cfp = (ACLMessage) getDataStore().get(tpr.RECEIVED_KEY);
  			// Create the CFP to send to the responder
  			ACLMessage nestedCfp = (ACLMessage) cfp.clone();
  			nestedCfp.setSender(myAgent.getAID());
  			nestedCfp.clearAllReceiver();
  			nestedCfp.addReceiver(responder);
  			nestedCfp.setConversationId(NESTED_CONV_ID);
  			return super.prepareCfps(nestedCfp);
  		}
  		
      protected void handlePropose(ACLMessage nestedPropose) {
    		l.log("PROPOSE received from responder");
    		// Create and store the PROPOSE to send back to the initiator
    		ACLMessage propose = (ACLMessage) nestedPropose.clone();
    		propose.setSender(myAgent.getAID());
    		propose.clearAllReceiver();
    		// All other protocol fields should be automatically set
    		getDataStore().put(tpr.REPLY_KEY, propose);
      }
  	} );
  	
  	// Register a nested TwoPh1 initiator in the HANDLE_QUERY_IF state
  	tpr.registerHandleQueryIf(new TwoPh1Initiator(a, null, DUMMY_OUTPUT) {
  		public void onStart() {
  			l.log("QUERY_IF received from initiator");
  			super.onStart();
  		}
  		
  		protected Vector prepareQueryIfs(ACLMessage queryIf) {
  			// Retrieve the QUERY_IF message received from the real initiator
  			queryIf = (ACLMessage) getDataStore().get(tpr.RECEIVED_KEY);
  			// Create the QUERY_IF to send to the responder
  			ACLMessage nestedQueryIf = (ACLMessage) queryIf.clone();
  			nestedQueryIf.setSender(myAgent.getAID());
  			nestedQueryIf.clearAllReceiver();
  			nestedQueryIf.addReceiver(responder);
  			nestedQueryIf.setConversationId(NESTED_CONV_ID);
  			return super.prepareQueryIfs(nestedQueryIf);
  		}
  		
      protected void handleConfirm(ACLMessage nestedConfirm) {
    		l.log("CONFIRM received from responder");
    		// Create and store the CONFIRM to send back to the initiator
    		ACLMessage confirm = (ACLMessage) nestedConfirm.clone();
    		confirm.setSender(myAgent.getAID());
    		confirm.clearAllReceiver();
    		// All other protocol fields should be automatically set
    		getDataStore().put(tpr.REPLY_KEY, confirm);
      }
  	} );
  	
  	// Register a nested TwoPh2 initiator in the HANDLE_ACCEPT_PROPOSAL state
  	tpr.registerHandleAcceptProposal(new TwoPh2Initiator(a, null) {
  		public void onStart() {
  			l.log("ACCEPT_PROPOSAL received from initiator");
  			super.onStart();
  		}
  		
  		protected Vector prepareAcceptances(ACLMessage acceptance) {
  			// Retrieve the ACCEPT/REJECT_PROPOSAL message received from the real initiator
  			acceptance = (ACLMessage) getDataStore().get(tpr.RECEIVED_KEY);
  			// Create the ACCEPT/REJECT_PROPOSAL to send to the responder
  			ACLMessage nestedAcceptance = (ACLMessage) acceptance.clone();
  			nestedAcceptance.setSender(myAgent.getAID());
  			nestedAcceptance.clearAllReceiver();
  			nestedAcceptance.addReceiver(responder);
  			nestedAcceptance.setConversationId(NESTED_CONV_ID);
  			return super.prepareAcceptances(nestedAcceptance);
  		}
  		
      protected void handleInform(ACLMessage nestedInform) {
    		l.log("INFORM received from responder");
    		// Create and store the INFORM to send back to the initiator
    		ACLMessage inform = (ACLMessage) nestedInform.clone();
    		inform.setSender(myAgent.getAID());
    		inform.clearAllReceiver();
    		// All other protocol fields should be automatically set
    		getDataStore().put(tpr.REPLY_KEY, inform);
      }
  	} );
  	
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
  	pb.addSubBehaviour(tpr);
  	
  	// The behaviour waiting for the termination notification 
  	// from the initiator
  	pb.addSubBehaviour(new SimpleBehaviour(a) {
  		private boolean finished = false;
  		private MessageTemplate template = MessageTemplate.MatchConversationId(CONTROL_CONV_ID);
  		
  		public void action() {
  			ACLMessage msg = myAgent.receive(template);
  			if (msg != null) {
  				if (msg.getPerformative() == ACLMessage.INFORM) {
  					store.put(key, new Integer(Test.TEST_PASSED));
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
  	
  	// The behaviour that checks the maximum execution time
  	pb.addSubBehaviour(new WakerBehaviour(a, 10000) {
  		protected void handleElapsedTimeout() {
  			l.log("Timeout expired.");
  			store.put(key, new Integer(Test.TEST_FAILED));
  		}
  	} );
  	
  	return pb;
  }
  
  public void clean(Agent a) {
  	try {
  		TestUtility.killAgent(a, initiator);
  		TestUtility.killAgent(a, responder);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}	
  }  
  
  /**
     Inner class Initiator
   */
  public static class Initiator extends Agent {
  	ACLMessage msg;
  	
  	protected void setup() {
  		msg = blockingReceive(MessageTemplate.MatchConversationId(CONTROL_CONV_ID));
  		Logger.getLogger().log(getLocalName()+": Startup message received.");
  		ACLMessage cfp = msg.createReply();
  		cfp.setConversationId(CONV_ID);
  		cfp.setPerformative(ACLMessage.CFP);
  		addBehaviour(new TwoPhInitiator(this, cfp) {
  			protected void handlePropose(ACLMessage propose) {
  				Logger.getLogger().log(getLocalName()+": PROPOSE message received from tester.");
  			}
  			
  			protected void handlePh2Inform(ACLMessage inform) {
  				Logger.getLogger().log(getLocalName()+": INFORM message received from tester.");
  				ACLMessage notification = msg.createReply();
  				notification.setPerformative(ACLMessage.INFORM);
  				myAgent.send(notification);
  			}
  		} );
  	}			
  } // END of inner class Initiator
  
  /**
     Inner class Responder
   */
  public static class Responder extends Agent {
  	protected void setup() {
  		addBehaviour(new TwoPhResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {
  			protected ACLMessage handleCfp(ACLMessage cfp) {
  				ACLMessage propose = cfp.createReply();
  				propose.setPerformative(ACLMessage.PROPOSE);
  				return propose;
  			}
  	
  			protected ACLMessage handleQueryIf(ACLMessage queryIf) {
  				ACLMessage confirm = queryIf.createReply();
  				confirm.setPerformative(ACLMessage.CONFIRM);
  				return confirm;
  			}
  	
  			protected ACLMessage handleAcceptProposal(ACLMessage acceptProposal) {
  				ACLMessage inform = acceptProposal.createReply();
  				inform.setPerformative(ACLMessage.INFORM);
  				return inform;
  			}
  		} );
  	}
  } // END of inner class Responder
}
