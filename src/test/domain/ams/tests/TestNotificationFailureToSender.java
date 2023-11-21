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
import jade.content.lang.sl.SL0Vocabulary;
import jade.content.lang.sl.SimpleSLTokenizer;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import test.common.*;

/**
   Test the "notification failure to sender" mechanism.
   More in details in this test we send a message to a number 
   of agents that do not exist. Some of them look like local agents
   while others look like living on remote platforms and have fake
   or no address.
   @author Giovanni Caire - TILAB
 */
public class TestNotificationFailureToSender extends Test {
	
  private String[] receivers = new String[] {"r1", "r2", "r3", "l1", "l2", "l3"};
  private String[] fakeAddresses = new String[] {"IOR:000", "http://fake"};
	private Expectation exp;
	
  public Behaviour load(Agent a) throws TestException {
  	
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
			
			public void onStart() {
				exp = new Expectation(receivers);
				super.onStart();
			}

			protected void handleFailure(ACLMessage failure) {
				if (myAgent.getAMS().equals(failure.getSender())) {
					try {
						AID id = getIntendedReceiver(failure.getContent());
						String name = id.getName();
						if (!name.startsWith("r")) {
							name = id.getLocalName();
						}
						if (exp.received(name)) {
							log("FAILURE message for agent "+id.getName()+" received as expected.");
						}
						else {
							failed("FAILURE message for agent "+id.getName()+" already received.");
						}
					}
					catch (Exception e) {
						failed("Error extracting agent name from FAILURE message ["+failure.getContent()+"]");
						e.printStackTrace();
					}
				}
				else {
					failed("Unexpected FAILURE message received from agent "+failure.getSender());
				}
			}
			
  		public int onEnd() {
  			if (exp.isCompleted()) {
  				passed("All FAILURE messages received as expected.");
  				return 1;
  			}
  			else {
  				failed("Only "+exp.size()+" FAILURE messages received while "+exp.expectedSize()+" where expected.");
  				return -1;
  			}
  		}	
  	};
  		
  	Behaviour b2 = new WakerBehaviour(a, 10000) {
  		protected void handleElapsedTimeout() {
				failed("Timeout expired: only "+exp.size()+" FAILURE messages received while "+exp.expectedSize()+" where expected.");
  		}
  	};
  	
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
  	pb.addSubBehaviour(b1);
  	pb.addSubBehaviour(b2);
  	return pb;
  }
  
  private AID getIntendedReceiver(String content) throws Exception {
  	int start = content.indexOf("MTS-error");
  	SimpleSLTokenizer parser = new SimpleSLTokenizer(content.substring(start+9));
  	parser.consumeChar('(');
  	return parseAID(parser);
  }
  
  /**
     The parser content has the form:
     agent-identifier ......) <possibly something else>
   */
  private static AID parseAID(SimpleSLTokenizer parser) throws Exception {
  	AID id = new AID("", AID.ISGUID); // Dummy temporary name
 		// Skip "agent-identifier"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(SL0Vocabulary.AID_NAME)) {
				id.setName(parser.getElement());
			}
			// Addresses
			else if (slotName.equals(SL0Vocabulary.AID_ADDRESSES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					id.addAddresses((String) it.next());
				}
			}
			// Resolvers
			else if (slotName.equals(SL0Vocabulary.AID_RESOLVERS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					id.addResolvers((AID) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return id;
  }
  
  /**
     The parser content has the form:
     (sequence <val> <val> ......) <possibly something else>
     or 
     (set <val> <val> ......) <possibly something else>
   */
  private static List parseAggregate(SimpleSLTokenizer parser) throws Exception {
  	List l = new ArrayList();
		// Skip first (
  	parser.consumeChar('(');
  	// Skip "sequence" or "set" (no matter)
		parser.getElement();
		String next = parser.nextToken();
		while (!next.startsWith(")")) {
			if (!next.startsWith("(")) {
				l.add(parser.getElement());
			}
			else {
				parser.consumeChar('(');
				next = parser.nextToken();
				if (next.equals(SL0Vocabulary.AID)) {
					l.add(parseAID(parser));
				}
			}
			next = parser.nextToken();
		}
		parser.consumeChar(')');
		return l;
  }
}
