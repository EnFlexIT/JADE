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

package test.mobility.separate;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.ContractNetInitiator;
import jade.util.leap.*;

import java.util.StringTokenizer;
import java.util.Vector;

/**
   @author Giovanni Caire - TILAB
 */
public class MobileInitiatorAgent extends Agent {
	private AID tester;
	private List visitedLocations = new ArrayList();
	private List expectedLocations = new LinkedList();
	
	protected void setup() {
		// Wait for the startup message from the tester agent
		ACLMessage msg = blockingReceive();
		System.out.println(getLocalName()+": Startup message received");
		tester = msg.getSender();
		
		// Extract the responders and fill the CFP message
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		StringTokenizer st = new StringTokenizer(msg.getContent(), " ");
		int i = 1;
		while (st.hasMoreTokens()) {
			String resp = st.nextToken();
			cfp.addReceiver(new AID(resp, AID.ISLOCALNAME));
			i++;
		}
		
		ContractNetInitiator cni = new ContractNetInitiator(this, cfp) {
			protected void handlePropose(ACLMessage propose, Vector acceptances) {
				// Accept the proposal and move on the indicated container
				String cName = propose.getContent();
				expectedLocations.add(cName);
				if (cName.equals(myAgent.here().getName())) {
					visitedLocations.add(cName);
				}
				System.out.println(getLocalName()+": Propose received from "+propose.getSender().getLocalName()+". Container is "+cName);
				ACLMessage accept = propose.createReply();
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				acceptances.addElement(accept);
				doMove(new ContainerID(propose.getContent(), null));
			}
			
			protected void handleInform(ACLMessage inform) {
				// Move on the indicated container
				String cName = inform.getContent();
				expectedLocations.add(cName);
				if (cName.equals(myAgent.here().getName())) {
					visitedLocations.add(cName);
				}
				System.out.println(getLocalName()+": Notification received from "+inform.getSender().getLocalName()+". Container is "+cName);
				doMove(new ContainerID(inform.getContent(), null));
			}
		};
		
		// Register a handler in the HANDLE_ALL_RESPONSES state
		cni.registerHandleAllResponses(new Informer(this, "responses"));
		// Register a handler in the HANDLE_ALL_RESULT_NOTIFICATIONS state
		cni.registerHandleAllResultNotifications(new Informer(this, "notifications"));

		addBehaviour(cni);
	}
	
	protected void beforeMove() {
		System.out.println(getLocalName()+": Leaving location "+here().getName());
	}
	
	protected void afterMove() {
		String cName = here().getName();
		visitedLocations.add(cName);
		System.out.println(getLocalName()+": Entering location "+cName);
	}
	
	class Informer extends OneShotBehaviour {
		String type;
		Informer(Agent a, String type) {
			super(a);
			this.type = type;
		}
		
		public void action() {
			// Check that we visited all the expected locations
			Iterator it = expectedLocations.iterator();
			int i = 0;
			while (it.hasNext()) {
				String expectedName = (String) it.next();
				try {
					String visitedName = (String) visitedLocations.get(i++);
					if (!visitedName.equals(expectedName)) {
						type = "error";
						break;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					type = "error";
					break;
				}
			}
			expectedLocations.clear();
			visitedLocations.clear();
			
			// Inform the tester
			System.out.println(getLocalName()+": Informing tester about "+type);
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(tester);
			inform.setContent(type);
			myAgent.send(inform);
		}
	}
}
