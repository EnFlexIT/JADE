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

package test.domain.persistence.df.tests;

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
import test.domain.persistence.df.*;

/**
 * @author Elisabetta Cortese - TILab
 *
 */
public class TestDFSubscriptionKill extends Test {
  private int informCnt = 0;
	private JadeController jc;
	private AID df1;
	  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
    final Logger l = Logger.getLogger();
  	
    // Read group arguments
  	String url = (String) getGroupArgument(PersistenceDFTesterAgent.DB_URL_KEY);
  	String driver = (String) getGroupArgument(PersistenceDFTesterAgent.DB_DRIVER_KEY);
  	String pippo = null;
  	
  	// Start a new "persistent" DF on a separated container
	jc = TestUtility.launchJadeInstance("Persistent", null, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -jade.domain.df.verbosity 2 -jade.domain.df.db-url "+url+" -jade.domain.df.db-driver "+driver, new String[] {});
  	df1 = TestUtility.createAgent(a, "df1", "jade.domain.df", null, a.getAMS(), jc.getContainerName());
	  
  	// Register a DFD on df1
  	DFAgentDescription dfd = TestDFHelper.getSampleDFD(new AID("a1", AID.ISLOCALNAME));
  	try {
	  	DFService.register(a, df1, dfd);
  	}
  	catch (FIPAException fe) {
  		throw new TestException("Error registering a DFD with DF1.", fe);
  	}
  	
  	// Register language and ontology
  	final Codec codec = new SLCodec();
  	a.getContentManager().registerLanguage(codec);
  	a.getContentManager().registerOntology(FIPAManagementOntology.getInstance());

  	// Prepare the subscription message
  	ACLMessage subscriptionMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
  	subscriptionMsg.addReceiver(df1);
  	subscriptionMsg.setLanguage(codec.getName());
  	subscriptionMsg.setOntology(FIPAManagementOntology.getInstance().getName());
  	try {
			DFAgentDescription template = TestDFHelper.getSampleTemplate1();
			Search s = new Search();
			s.setDescription(template);
			s.setConstraints(new SearchConstraints());
			Action aa = new Action(df1, s);
			AbsPredicate result = new AbsPredicate(BasicOntology.RESULT);
			AbsVariable x = new AbsVariable("x", null);
			result.set(BasicOntology.RESULT_ACTION, (AbsAgentAction) FIPAManagementOntology.getInstance().fromObject(aa));
			result.set(BasicOntology.RESULT_ITEMS, x);
	  	AbsIRE iota = new AbsIRE(SLVocabulary.IOTA);
	  	iota.setVariable(x);
	  	iota.setProposition(result);
			a.getContentManager().fillContent(subscriptionMsg, iota);
  	}
  	catch (Exception e) {
  		throw new TestException("Error preparing SUBSCRIBE message.", e);
  	}
  	
  	// The behaviour that subscribes to the DF and handles notifications
  	final SubscriptionInitiator si = new SubscriptionInitiator(a, subscriptionMsg) {
  		
  		public void onStart() {
  			super.onStart();
  			l.log("Subscribing to DF1. A notification should immediately be received");
  		}
  		
  		protected void handleInform(ACLMessage inform) {
  			l.log("Notification received.");
  			//l.log(inform.toString());
  			informCnt++;
  		}
  	};
  	
  	// The test behaviour is a ParallelBehaviour (WHEN_ANY) with 2 children
  	// i) A SubscriptionInitiator that subscribes to DF1 and manages
  	//    notifications from it. 
  	// ii) A TickerBehaviour (with period 5 sec) that simulates other 
  	//     agents registering/deregistering/modifying with DF1 as follows:
  	// - tick 1 - registers a second DFD (matching)
  	// - tick 2 - Kills df1 and makes it start again
  	// - tick 3 - registers a third DFD (not matching)
  	// - tick 4 - modifies the second DFD (still matching)
  	// - tick 5 - modifies the third DFD (now matching)
  	// - tick 6 - terminates.
  	// When the TickerBehaviour terminates the whole parallel behaviour 
  	// terminates too. If 4 notifications were received --> TEST_PASSED
  	// otherwise TEST_FAILED.
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY) {
  		public int onEnd() {
  			if (informCnt == 4) {
  				store.put(key, new Integer(Test.TEST_PASSED));
  			}
  			else {
  				l.log(informCnt+" notifications received from DF while 4 were expected");
  				store.put(key, new Integer(Test.TEST_FAILED));
  			}
  			
  			// Cancel the subscription
				ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
				cancel.addReceiver(df1);
				cancel.setLanguage(codec.getName());
				cancel.setOntology(FIPAManagementOntology.getInstance().getName());
				ACLMessage subscriptionMsg = (ACLMessage) si.getDataStore().get(df1);				
				Action act = new Action(df1, OntoACLMessage.wrap(subscriptionMsg));
				try {
					myAgent.getContentManager().fillContent(cancel, act);
				}
				catch (Exception e) {
					e.printStackTrace();
		  	}
				myAgent.send(cancel);
  			return 0;
  		}
  	};	

  	pb.addSubBehaviour(si);
		
  	// The behaviour that simulates other agents that 
  	// register/deregister/modify descriptions with the DF
  	pb.addSubBehaviour(new TickerBehaviour(a, 5000) {
  		
  		protected void onTick() {
				DFAgentDescription dfd = null; 			
				switch (getTickCount()) {
  			case 1:
  				// Register 2nd DFD (matching)
  				dfd = new DFAgentDescription();
  				dfd.setName(new AID("a2", AID.ISLOCALNAME));
  				dfd.addServices(TestDFHelper.getSampleSD1());
  				try {
  					l.log("Registering a matching DFD. A notification should be received.");
	  				DFService.register(myAgent, df1, dfd);
  				}
  				catch (FIPAException fe) {
  					System.out.println("Error registering 2nd DFD with the DF.");
  					fe.printStackTrace();
  					stop();
  				}
  				break;
  			case 2:
  				// Kill df1 and restart it
  				try{
						l.log("Killing and restarting DF1");
						TestUtility.killAgent(myAgent, df1);
				//		Thread.sleep(5000);
					  TestUtility.createAgent(myAgent, "df1", "jade.domain.df", null, null, jc.getContainerName());
						l.log("DF1 killed and restarted correctly");
					}
					catch(Exception ex){
						System.out.println("Error killing/restarting DF1.");
						ex.printStackTrace();
						stop();
  				}
  				break;
  			case 3:
  				// Register 3rd DFD (not matching)
  				dfd = new DFAgentDescription();
  				dfd.setName(new AID("a3", AID.ISLOCALNAME));
  				dfd.addServices(TestDFHelper.getSampleSD2());
  				try {
  					l.log("Registering a NON-matching DFD. No notification should be received");
	  				DFService.register(myAgent, df1, dfd);
  				}
  				catch (FIPAException fe) {
  					System.out.println("Error registering 3rd DFD with the DF.");
  					fe.printStackTrace();
  					stop();
  				}
  				break;
  			case 4:
  				// Modify 2nd DFD (still matching)
  				dfd = TestDFHelper.getSampleDFD(new AID("a2", AID.ISLOCALNAME));
  				try {
  					l.log("Modifying a matching DFD into another matching DFD. A notification should be received");
	  				DFService.modify(myAgent, df1, dfd);
  				}
  				catch (FIPAException fe) {
  					System.out.println("Error modifying 2nd DFD with the DF.");
  					fe.printStackTrace();
  					stop();
  				}
  				break;
  			case 5:
  				// Modify 3nd DFD (now matching)
  				dfd = TestDFHelper.getSampleDFD(new AID("a3", AID.ISLOCALNAME));
  				try {
  					l.log("Modifying a NON-matching DFD into a matching DFD. A notification should be received");
	  				DFService.modify(myAgent, df1, dfd);
  				}
  				catch (FIPAException fe) {
  					System.out.println("Error modifying 3rd DFD with the DF.");
  					fe.printStackTrace();
  					stop();
  				}
  				break;
  			case 6:
  				stop();
  			}
  		}
  	} );
  	
  	return pb;
  }
  
  public void clean(Agent a) {
  	// Deregister all descriptions
  	try {
  		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(new AID("a1", AID.ISLOCALNAME));
	  	DFService.deregister(a, df1, dfd);
  		dfd.setName(new AID("a2", AID.ISLOCALNAME));
	  	DFService.deregister(a, df1, dfd);
  		dfd.setName(new AID("a3", AID.ISLOCALNAME));
	  	DFService.deregister(a, df1, dfd);
  		dfd.setName(new AID("df1", AID.ISLOCALNAME));
	  	
	  	jc.kill();
//		try{ TestUtility.killAgent(a, df1);}catch(TestException te){}
  	}
  	catch (FIPAException fe) {
  		fe.printStackTrace();
  	}
  }
  	
}


