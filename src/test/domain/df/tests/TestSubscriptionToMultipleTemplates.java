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
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.basic.*;

import jade.proto.SubscriptionInitiator;

import test.common.*;
import test.domain.df.*;

/**
 * Test the case in wich an agent subscribes to the df to receive notifications 
 * about registrations matching two different templates
 * @author Giovanni Caire - TILAB
 */
public class TestSubscriptionToMultipleTemplates extends Test {
	private static final String A1 = "a1";
	private static final String A2 = "a2";
	
	public Behaviour load(Agent a) throws TestException {
		setTimeout(60000);
		
		try {
			// Register language and ontology
			final Codec codec = new SLCodec();
			a.getContentManager().registerLanguage(codec);
			a.getContentManager().registerOntology(FIPAManagementOntology.getInstance());
	
			// The behaviour that subscribes for template 1
			DFAgentDescription template = TestDFHelper.getSampleTemplate1();
			ACLMessage subscriptionMsg = DFService.createSubscriptionMessage(a, a.getDefaultDF(), template, null);
			Behaviour si1 = new SubsInitiator(a, subscriptionMsg);
			
			// The behaviour that subscribes for template 2
			template = TestDFHelper.getSampleTemplate2();
			subscriptionMsg = DFService.createSubscriptionMessage(a, a.getDefaultDF(), template, null);
			Behaviour si2 = new SubsInitiator(a, subscriptionMsg);
			
			// The behaviour that perform registrations and deregistrations
			SequentialBehaviour sb = new SequentialBehaviour(a);
			// Step1: Wait a bit then register a description matching template 1
			sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
				public void onStart() {
					log("--- Wait a bit then register a DFD matching template 1...");
					super.onStart();
				}
				
				public void onWake() {
					DFAgentDescription dfd = new DFAgentDescription();
					dfd.setName(new AID(A1, AID.ISLOCALNAME));
					dfd.addServices(TestDFHelper.getSampleSD1());
					try {
						DFService.register(myAgent, myAgent.getDefaultDF(), dfd);
						log("--- DFD matching template 1 correctly registered");
					}
					catch (FIPAException fe) {
						failed("--- Error registering DFD matching template 1");
					}						
				}
			} );
			// Step2: Deregister the DFD
			sb.addSubBehaviour(new OneShotBehaviour(a) {
				public void action() {
					DFAgentDescription dfd = new DFAgentDescription();
					dfd.setName(new AID(A1, AID.ISLOCALNAME));
					try {
						log("--- Deregistering DFD matching template 1");
						DFService.deregister(myAgent, myAgent.getDefaultDF(), dfd);
						log("--- DFD matching template 1 correctly deregistered");
					}
					catch (FIPAException fe) {
						failed("--- Error deregistering DFD matching template 1");
					}						
				}
			} );
			// Step3: Wait a bit then register a description matching template 2
			sb.addSubBehaviour(new WakerBehaviour(a, 5000) {
				public void onStart() {
					log("--- Wait a bit then register a DFD matching template 2...");
					super.onStart();
				}
				
				public void onWake() {
					DFAgentDescription dfd = new DFAgentDescription();
					dfd.setName(new AID(A2, AID.ISLOCALNAME));
					dfd.addServices(TestDFHelper.getSampleSD2());
					try {
						DFService.register(myAgent, myAgent.getDefaultDF(), dfd);
						log("--- DFD matching template 2 correctly registered");
					}
					catch (FIPAException fe) {
						failed("--- Error registering DFD matching template 1");
					}						
				}
			} );
			// Step4: Deregister the DFD
			sb.addSubBehaviour(new OneShotBehaviour(a) {
				public void action() {
					DFAgentDescription dfd = new DFAgentDescription();
					dfd.setName(new AID(A2, AID.ISLOCALNAME));
					try {
						log("--- Deregistering DFD matching template 2");
						DFService.deregister(myAgent, myAgent.getDefaultDF(), dfd);
						log("--- DFD matching template 2 correctly deregistered");
					}
					catch (FIPAException fe) {
						failed("--- Error deregistering DFD matching template 2");
					}						
				}
			} );
			
			ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ALL) {
				public int onEnd() {
					if (!TestSubscriptionToMultipleTemplates.this.isFailed()) {
						passed("Test OK");
					}
					return 0;
				}
			};
			pb.addSubBehaviour(si1);
			pb.addSubBehaviour(si2);
			pb.addSubBehaviour(sb);
			
			return pb;
		}
		catch (Exception e) {
			throw new TestException("Error in test initialization", e);
		}
	}

	
	/** 
     * Inner class SubsInitiator
     * This behaviour subscribes to the DF and terminates when receiving the second notification 
	 */
	private class SubsInitiator extends SubscriptionInitiator {
		private int notificationCnt = 0;
		
		public SubsInitiator(Agent a, ACLMessage sub) {
			super(a, sub);
		}

		public void onStart() {
			super.onStart();
			log("--- Agent "+myAgent.getLocalName()+": Subscribing to the DF");
		}

		protected void handleInform(ACLMessage inform) {
			log("--- Agent "+myAgent.getLocalName()+": Notification received from DF.");
		
			notificationCnt++;
			if (notificationCnt == 2) {
				// Cancel the subscription
				cancel(myAgent.getDefaultDF(), true);
			}
		}

		protected void fillCancelContent(ACLMessage subscription, ACLMessage cancel) {
			try {
				Action act = new Action((AID) cancel.getAllReceiver().next(), OntoACLMessage.wrap(subscription));
				myAgent.getContentManager().fillContent(cancel, act);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
