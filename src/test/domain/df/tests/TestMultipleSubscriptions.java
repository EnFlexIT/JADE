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

import jade.content.*;
import jade.content.abs.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import jade.proto.SubscriptionInitiator;

import test.common.*;
import test.domain.df.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestMultipleSubscriptions extends Test {
  private Expectation expectedInform;
	  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
    final Logger l = Logger.getLogger();
  	
		// Launches 3 subscriber agents
    TestUtility.createAgent(a, "s1", "test.domain.df.tests.TestMultipleSubscriptions$SubscriberAgent", new String[] {a.getLocalName()}, a.getAMS(), null);
    TestUtility.createAgent(a, "s2", "test.domain.df.tests.TestMultipleSubscriptions$SubscriberAgent", new String[] {a.getLocalName()}, a.getAMS(), null);
    TestUtility.createAgent(a, "s3", "test.domain.df.tests.TestMultipleSubscriptions$SubscriberAgent", new String[] {a.getLocalName()}, a.getAMS(), null);
	  expectedInform = new Expectation(new String[] {"s1", "s2", "s3"}); 
    
  	// Register language and ontology
  	final Codec codec = new SLCodec();
  	a.getContentManager().registerLanguage(codec);
  	a.getContentManager().registerOntology(FIPAManagementOntology.getInstance());

  	// Registers a DFD after 5 sec. Kill the test (FAILED) after 10 sec
  	Behaviour helper = new TickerBehaviour(a, 5000) {
  		protected void onTick() {
  			if (getTickCount() == 1) {
  				// First tick --> Register a DFD
	  			DFAgentDescription dfd = TestDFHelper.getSampleDFD(myAgent.getAID());
	  			try {
		  			DFService.register(myAgent, myAgent.getDefaultDF(), dfd);
	  				l.log("DF registration done");
	  			}
	  			catch (FIPAException fe) {
	  				l.log("DF registration failed");
	  				fe.printStackTrace();
	  				store.put(key, new Integer(Test.TEST_FAILED));
	  				stop();
	  			}	
  			}
  			else {
  				// Second tick --> Timeout expired
  				l.log("Timeout expired");
	  			store.put(key, new Integer(Test.TEST_FAILED));
	  			stop();
  			}
  		}
  	};
  				
  	// Collects INFORM messages from the subscribers
  	Behaviour collector = new SimpleBehaviour(a) {
  		private boolean finished = false;
  		
  		public void action() {
  			ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM)); 
  			if (msg != null) {
  				String sender = msg.getSender().getLocalName();
  				if (expectedInform.received(sender)) {
  					l.log("INFORM received from "+sender);
  				}
  				if (expectedInform.isCompleted()) {
  					// All expected INFORM messages received
  					store.put(key, new Integer(Test.TEST_PASSED));
  					finished = true;
  				}
  			}
  			else {
  				block();
  			}
  		}
  		
  		public boolean done() {
  			return finished;
  		}
  	};
  			
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
  	pb.addSubBehaviour(helper);
  	pb.addSubBehaviour(collector);
  	return pb;
  }
  
  public void clean(Agent a) {
  	try {
  		// Deregister the DFD
  		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(a.getAID());
	  	DFService.deregister(a, a.getDefaultDF(), dfd);
	  	// No need to kill the subscribers as they kill themselves
  	}
  	catch (FIPAException fe) {
  		fe.printStackTrace();
  	}
  }
  
  /** 
     Inner class SubscriberAgent
   */
  public static class SubscriberAgent extends Agent {
  	private AID myTester;
  	private Logger l = Logger.getLogger();
  	private Codec codec;
  	
  	protected void setup() {
  		// Get the tester agent to notify
  		Object[] args = getArguments();
  		if (args != null && args.length == 1) {
  			myTester = new AID((String) args[0], AID.ISLOCALNAME);
  		}
  		
	  	// Register language and ontology
	  	codec = new SLCodec();
	  	getContentManager().registerLanguage(codec);
	  	getContentManager().registerOntology(FIPAManagementOntology.getInstance());
	  	
	  	// Prepare the subscription message
	  	ACLMessage subscriptionMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
	  	subscriptionMsg.addReceiver(getDefaultDF());
	  	subscriptionMsg.setLanguage(codec.getName());
	  	subscriptionMsg.setOntology(FIPAManagementOntology.getInstance().getName());
	  	try {
				DFAgentDescription template = TestDFHelper.getSampleTemplate1();
				Search s = new Search();
				s.setDescription(template);
				s.setConstraints(new SearchConstraints());
				Action aa = new Action(getDefaultDF(), s);
				AbsPredicate result = new AbsPredicate(BasicOntology.RESULT);
				AbsVariable x = new AbsVariable("x", null);
				result.set(BasicOntology.RESULT_ACTION, (AbsAgentAction) FIPAManagementOntology.getInstance().fromObject(aa));
				result.set(BasicOntology.RESULT_ITEMS, x);
	  		AbsIRE iota = new AbsIRE(SLVocabulary.IOTA);
	  		iota.setVariable(x);
	  		iota.setProposition(result);
				getContentManager().fillContent(subscriptionMsg, iota);
	  	}
	  	catch (Exception e) {
	  		l.log("Agent "+getLocalName()+": Error preparing SUBSCRIBE message.");
	  		e.printStackTrace();
	  		doDelete();
	  		return;
	  	}
	  	
	  	// The behaviour that subscribes to the DF and handles notifications
	  	SubscriptionInitiator si = new SubscriptionInitiator(this, subscriptionMsg) {
	  		
	  		public void onStart() {
	  			super.onStart();
	  			l.log("Agent "+getLocalName()+": Subscribing to the DF");
	  		}
	  		
	  		protected void handleInform(ACLMessage inform) {
	  			l.log("Agent "+getLocalName()+": Notification received from DF.");
	  			// Forward the message to the tester
	  			inform.setSender(getAID());
	  			inform.clearAllReceiver();
	  			inform.addReceiver(myTester);
	  			send(inform);
	  			
	  			// Cancel the subscription
					ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
					cancel.addReceiver(myAgent.getDefaultDF());
					cancel.setLanguage(codec.getName());
					cancel.setOntology(FIPAManagementOntology.getInstance().getName());
					ACLMessage subscriptionMsg = (ACLMessage) getDataStore().get(myAgent.getDefaultDF());				
					Action act = new Action(myAgent.getDefaultDF(), OntoACLMessage.wrap(subscriptionMsg));
					try {
						myAgent.getContentManager().fillContent(cancel, act);
					}
					catch (Exception e) {
						e.printStackTrace();
			  	}
					myAgent.send(cancel);
					
					// Terminate 
					myAgent.doDelete();
	  		}
	  	};
  	
	  	addBehaviour(si);
  	}
  }
}
