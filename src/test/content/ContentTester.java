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

package test.content;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.*;
import jade.content.lang.leap.*;

import test.content.testOntology.TestOntology;

public class ContentTester extends Agent {
	private static final String TEST_CONVERSATION = "_Test_";
	private static final String TEST_RESPONSE_ID = "_Response_";
	
  private ContentManager manager  = (ContentManager)getContentManager();
  // This agent by default speaks a language called "LEAP"
  private Codec          codec    = new LEAPCodec();
  // This agent complies with the MusicShop ontology
  private Ontology   ontology = TestOntology.getInstance();

  private boolean verbose = true;
  private int passedCnt = 0;
  private int failedCnt = 0;
  
  protected void setup() {
    // Get the codec for the language to speack (use LEAP codec by default)
    Object[] args = getArguments();
    if (args != null && args.length > 0) {
    	String codecClassName = (String) args[0];
    	try {
    		codec = (Codec) Class.forName(codecClassName).newInstance();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    System.out.println("Test performed using the "+codec.getName()+" language");
		manager.registerLanguage(codec);
		manager.registerOntology(ontology);
	
		addBehaviour(new ResponderBehaviour(this));
		addBehaviour(new TesterBehaviour(this));      
  }
    
  protected void takeDown() {
    System.out.println("\nTest summary:");
    System.out.println(passedCnt+" tests PASSED");
    System.out.println(failedCnt+" tests FAILED");
  }

  // RESPONDER BEHAVIOUR
  class ResponderBehaviour extends CyclicBehaviour {
  	private MessageTemplate mt = MessageTemplate.and(
  		MessageTemplate.MatchConversationId(TEST_CONVERSATION),
  		MessageTemplate.MatchReplyWith(TEST_RESPONSE_ID));
  		
  	public ResponderBehaviour(Agent a) {
  		super(a);
  	}
  	
  	public void action() {
  		ACLMessage msg = receive(mt);
  		if (msg != null) {
  			ACLMessage reply = msg.createReply();
  			try {
  				handleContent(msg);
  				// Condent handling OK --> reply with an empty INFORM message
  				reply.setPerformative(ACLMessage.INFORM);
  			}
  			catch (Throwable t) {
  				// Content handling FAILED --> reply with an empty FAILURE message
  				if (verbose) {
	  				t.printStackTrace();
  				}
  				reply.setPerformative(ACLMessage.FAILURE);
  			}
  			send(reply);
  		}
  		else {
  			block();
  		}
  	}
  	
  	private void handleContent(ACLMessage msg) throws Throwable {
  		if (verbose) {
  			System.out.println("Received content is:");
  		}
  		try {
  			ContentElement ce = manager.extractContent(msg);
  			if (verbose) {
  				System.out.println(ce);
  			}
  		}
  		catch (UngroundedException ue) {
  			AbsContentElement ace = manager.extractAbsContent(msg);
  			if (verbose) {
  				System.out.println(ace);
  			}
  		}
  	}
  }  // END of ResponderBehaviour class
  			
 
  // TESTER BEHAVIOUR
  class TesterBehaviour extends FSMBehaviour {
  	private static final String EXECUTE_TEST = "Execute-test";
  	private static final String GET_RESPONSE = "Get-rsponse";
  	private static final String CHECK_FINISHED = "Check-finished";
  	private static final String EXIT = "Exit";
  	
  	private ACLMessage testMsg;
  	private MessageTemplate mt = MessageTemplate.and(
  		MessageTemplate.MatchConversationId(TEST_CONVERSATION),
  		MessageTemplate.MatchInReplyTo(TEST_RESPONSE_ID));

  	private TestManager myTestManager = new TestManager();
  	private Test currentTest = null;
  	
  	public TesterBehaviour(Agent a) {
  		super(a);
  		  		
  		testMsg = new ACLMessage(ACLMessage.INFORM);
  		testMsg.addReceiver(myAgent.getAID());
  		testMsg.setLanguage(codec.getName());
  		testMsg.setOntology(ontology.getName());
  		testMsg.setConversationId(TEST_CONVERSATION);
  		testMsg.setReplyWith(TEST_RESPONSE_ID);
  		
  		registerTransition(EXECUTE_TEST, GET_RESPONSE, Test.SEND_MSG);
  		registerDefaultTransition(EXECUTE_TEST, CHECK_FINISHED);
  		registerDefaultTransition(GET_RESPONSE, CHECK_FINISHED);
  		registerTransition(CHECK_FINISHED, EXECUTE_TEST, TestManager.TEST_IN_PROGRESS);
  		registerTransition(CHECK_FINISHED, EXIT, TestManager.TEST_COMPLETED);
  	
  		// EXECUTE_TEST state
  		registerFirstState(new OneShotBehaviour() {
  			private int ret;
  			
  			public void action() {
  				currentTest = myTestManager.next();
  				System.out.println("\nExecuting TEST: "+currentTest.getName());
  				System.out.println(currentTest.getDescription());
  				ret = currentTest.execute(testMsg, myAgent, verbose);
  				if (ret == Test.SEND_MSG) {
  					myAgent.send(testMsg);
  				}
  				else {
  					printTestResult(ret == Test.DONE_PASSED);
  				}
  			}
  			
  			public int onEnd() {
  				// Be ready for next round
  				reset();
  				return ret;
  			}	
  		}, EXECUTE_TEST);
  		
  		// GET_RESPONSE state
  		registerState(new SimpleBehaviour() {
  			private boolean received = false;
  			
  			public void action() {
  				ACLMessage response = receive(mt);
  				if (response != null) {
  					printTestResult(response.getPerformative() == ACLMessage.INFORM);
  					received = true;
  				}
  				else {
  					block();
  				}
  			}
  			
  			public boolean done() {
  				return received;
  			}
  			
  			public int onEnd() {
  				// Be ready for next round
  				reset();
  				return 0;
  			}
  			
  			public void reset() {
  				super.reset();
  				received = false;
  			}
  		}, GET_RESPONSE);
  		
  		// CHECK_FINISHED state
  		registerState(new OneShotBehaviour() {
  			public void action() {
  				// do nothing
  			}
  			
  			public int onEnd() {
  				// Be ready for next round
  				reset();
  				return myTestManager.getStatus();
  			}
  		}, CHECK_FINISHED);  		
  		
  		// EXIT state
  		registerLastState(new OneShotBehaviour() {
  			public void action() {
  				myAgent.doDelete();
  			}
  		}, EXIT);  		
  	}
  	
  }  // END of TesterBehaviour class
 
  public void printTestResult(boolean ok) {
  	if (ok) {
  		System.out.println("Test PASSED");
  		passedCnt++;
  	}
  	else {
  		System.out.println("Test FAILED");
  		failedCnt++;
  	}
  }
  
  class TestManager {
  	public static final int TEST_IN_PROGRESS = 0;
  	public static final int TEST_COMPLETED = 1;

  	private String[] tests = new String[] {
  		"test.content.tests.TestInt",
  		"test.content.tests.TestLong",
  		"test.content.tests.TestFloat",
  		"test.content.tests.TestDouble",
  		"test.content.tests.TestDate",
  		"test.content.tests.TestUnknownSchema",
  		"test.content.tests.TestOntoAID",
  		"test.content.tests.TestSequence",
  		"test.content.tests.TestMissingOptional",
  		"test.content.tests.TestMissingMandatory",
  		"test.content.tests.TestContentElementList",
  		"test.content.tests.TestAggregateAsConcept",
  		"test.content.tests.TestTypedAggregate",
  		"test.content.tests.TestCardinality",
  		"test.content.tests.TestCorrectIRE"
  	};
  	private int cnt = 0;
  	
  	public Test next() {
  		Test t = null;
  		try {
	  		// Create the Test object
  			String className = tests[cnt];
  			t = (Test) Class.forName(className).newInstance();
  		}
  		catch (Exception e) {
  			e.printStackTrace();
  		}
  		cnt++;
  		return t;
  	}
  	
  	public int getStatus() {
  		if (cnt < tests.length) {
  			return TEST_IN_PROGRESS;
  		}
  		else {
  			return TEST_COMPLETED;
  		}
  	}
  }
  
}