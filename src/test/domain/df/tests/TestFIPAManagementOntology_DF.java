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
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.proto.AchieveREInitiator;
import test.common.*;
import test.domain.df.*;

import java.util.Vector;

/**
   @author Filippo Quarta - TILAB
   @author Giovanni Caire - TILAB
 */
public class TestFIPAManagementOntology_DF extends Test {
	
  public String getName() {
  	return "Test FIPAManagementOntology-DF";
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
 		final Codec codec = new SLCodec();
 		final Ontology ontology = FIPAManagementOntology.getInstance();
    final Logger l = Logger.getLogger();
 	
  	SequentialBehaviour sb = new SequentialBehaviour(a) {
  		public void onStart() {
 				myAgent.getContentManager().registerLanguage(codec, "FIPA-SL0");
 				myAgent.getContentManager().registerOntology(ontology);
  			store.put(key, new Integer(Test.TEST_PASSED));
  		}
  	};
  	
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  	msg.addReceiver(a.getDefaultDF());
		msg.setLanguage("FIPA-SL0");
		msg.setOntology(ontology.getName());
		
		// Registration step
 		sb.addSubBehaviour(new AchieveREInitiator(a, msg) {
 			int ret = Test.TEST_FAILED;
 			protected Vector prepareRequests(ACLMessage request) {
 				// Prepare registration message
 				Vector v = new Vector();
 				try {
  				DFAgentDescription dfd = TestDFHelper.getSampleDFD(myAgent.getAID());
  				Register r = new Register();
  				r.setDescription(dfd);
  				Action act = new Action(myAgent.getDefaultDF(), r);
  				myAgent.getContentManager().fillContent(request, act);
  				v.add(request);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				return v;
 			}
 			protected void handleInform(ACLMessage inform) {
 				// Decode registration confirmation. If this succeeds set OK return code
 				try {
 					Done d = (Done) myAgent.getContentManager().extractContent(inform);
 					ret = Test.TEST_PASSED;
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			public int onEnd() {
 				if (ret == Test.TEST_FAILED) {
  				store.put(key, new Integer(ret));
  				((SequentialBehaviour) parent).skipNext();
  				l.log("DF registration failed");
 				}
 				else {
  				l.log("DF registration done");
 				}
  			return 0;
 			}
 		} );
 		
		// Search 1 step
 		sb.addSubBehaviour(new AchieveREInitiator(a, msg) {
 			int ret = Test.TEST_FAILED;
 			protected Vector prepareRequests(ACLMessage request) {
 				// Prepare search message
 				Vector v = new Vector();
  			try {
  				DFAgentDescription template = TestDFHelper.getSampleTemplate1();
  				Search s = new Search();
  				s.setDescription(template);
  				s.setConstraints(new SearchConstraints());
  				Action act = new Action(myAgent.getDefaultDF(), s);
  				myAgent.getContentManager().fillContent(request, act);
  				v.add(request);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				return v;
 			}
 			protected void handleInform(ACLMessage inform) {
 				// Decode search result. If 1 agent was found set OK return code
 				try {
 					Result r = (Result) myAgent.getContentManager().extractContent(inform);
 					if (r.getItems().size() == 1) {
	 					ret = Test.TEST_PASSED;
 					}
 					else {
  					l.log(r.getItems().size()+" items found while 1 was expected.");
 					}	
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			public int onEnd() {
 				if (ret == Test.TEST_FAILED) {
  				store.put(key, new Integer(ret));
  				((SequentialBehaviour) parent).skipNext();
  				l.log("DF search-1 failed");
 				}
 				else {
  				l.log("DF search-1 OK");
 				}
  			return 0;
 			}
 		} );
 		
		// Deregistration step
 		sb.addSubBehaviour(new AchieveREInitiator(a, msg) {
 			int ret = Test.TEST_FAILED;
 			protected Vector prepareRequests(ACLMessage request) {
 				// Prepare deregistration message
 				Vector v = new Vector();
 				try {
  				DFAgentDescription dfd = new DFAgentDescription();
  				dfd.setName(myAgent.getAID());
  				Deregister d = new Deregister();
  				d.setDescription(dfd);
  				Action act = new Action(myAgent.getDefaultDF(), d);
  				myAgent.getContentManager().fillContent(request, act);
  				v.add(request);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				return v;
 			}
 			protected void handleInform(ACLMessage inform) {
 				// Decode deregistration confirmation. If this succeeds set OK return code
 				try {
 					Done d = (Done) myAgent.getContentManager().extractContent(inform);
 					ret = Test.TEST_PASSED;
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			public int onEnd() {
 				if (ret == Test.TEST_FAILED) {
  				store.put(key, new Integer(ret));
  				((SequentialBehaviour) parent).skipNext();
  				l.log("DF deregistration failed");
 				}
 				else {
  				l.log("DF deregistration done");
 				}
  			return 0;
 			}
 		} );
 		
		// Search 2 step
 		sb.addSubBehaviour(new AchieveREInitiator(a, msg) {
 			int ret = Test.TEST_FAILED;
 			protected Vector prepareRequests(ACLMessage request) {
 				// Prepare search message
 				Vector v = new Vector();
  			try {
  				DFAgentDescription template = TestDFHelper.getSampleTemplate1();
  				Search s = new Search();
  				s.setDescription(template);
  				s.setConstraints(new SearchConstraints());
  				Action act = new Action(myAgent.getDefaultDF(), s);
  				myAgent.getContentManager().fillContent(request, act);
  				v.add(request);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				return v;
 			}
 			protected void handleInform(ACLMessage inform) {
 				// Decode search result. If NO agent was found set OK return code
 				try {
 					Result r = (Result) myAgent.getContentManager().extractContent(inform);
 					if (r.getItems().size() == 0) {
	 					ret = Test.TEST_PASSED;
 					}
 					else {
  					l.log(r.getItems().size()+" items found while 0 was expected.");
 					}	
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			public int onEnd() {
 				if (ret == Test.TEST_FAILED) {
  				store.put(key, new Integer(ret));
  				((SequentialBehaviour) parent).skipNext();
  				l.log("DF search-2 failed");
 				}
 				else {
  				l.log("DF search-2 OK");
 				}
  			return 0;
 			}
 		} );
 		
  	return sb;
  }
}
