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

package test.domain.ams.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.proto.AchieveREInitiator;
import test.common.*;

/**
   Test the notification failure to sender mechanism.
   @author Giovanni Caire - TILAB
 */
public class TestNotificationFailureToSender extends Test {
	
  private int ret;
  private Logger l = Logger.getLogger();
  private String[] receivers = new String[] {"r1", "r2", "r3", "l1", "l2", "l3"};
  private String[] fakeAddresses = new String[] {"IOR:000", "http://fake"};
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		for (int i = 0; i < receivers.length; ++i) {
			String rec = receivers[i];
			AID id = null;
			if (rec.startsWith("r")) {
				// Remote receiver
				id = new AID(rec, AID.ISGUID);
				if (i < fakeAddresses.length) {
					id.addAddresses(fakeAddresses[i]);
				}
			}
			else {
				// Local receiver
				id = new AID(rec, AID.ISLOCALNAME);
			}
			request.addReceiver(id);
		}
				
		Behaviour b1 = new AchieveREInitiator(a, request) {
			private boolean aborted = false;
			private Expectation exp = new Expectation(receivers);
			
			protected void handleFailure(ACLMessage failure) {
				if (myAgent.getAMS().equals(failure.getSender())) {
					AID id = getIntendedReceiver(failure.getContent());
					if (id != null) {
						String name = id.getName();
						if (!name.startsWith("r")) {
							name = id.getLocalName();
						}
						if (exp.received(name)) {
							l.log("FAILURE message for agent "+id.getName()+" already received.");
							aborted = true;
						}
						else {
							l.log("FAILURE message for agent "+id.getName()+" received as expected.");
						}
					}
					else {
						l.log("Error extracting agent name from FAILURE message ["+failure.getContent()+"]");
						aborted = true;
					}
				}
				else {
					l.log("Unexpected FAILURE message received from agent "+failure.getSender());
					aborted = true;
				}
			}
			
  		public int onEnd() {
  			if (exp.isCompleted() && (!aborted)) {
	  			store.put(key, new Integer(Test.TEST_PASSED));
  			}
  			else {
	  			store.put(key, new Integer(Test.TEST_FAILED));
  			}
  			return 0;
  		}	
  	};
  		
  	Behaviour b2 = new WakerBehaviour(a, 10000) {
  		protected void handleElapsedTimeout() {
  			l.log("Timeout expired.");
	  		store.put(key, new Integer(Test.TEST_FAILED));
  		}
  	};
  	
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
  	pb.addSubBehaviour(b1);
  	pb.addSubBehaviour(b2);
  	return pb;
  }
  
  private AID getIntendedReceiver(String content) {
  	try {
	  	int start = content.indexOf("MTS-error");
	  	start = content.indexOf(":name", start);
	  	int end = content.indexOf(":addresses", start);
	  	String name = content.substring(start+5, end).trim();
	  	return new AID(name, AID.ISGUID);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  		return null;
  	}
  }
}
