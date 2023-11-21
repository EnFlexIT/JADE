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

package test.leap.midp;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.util.Logger;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
   @author Giovanni Caire - TILAB
 */
public class MessageOrderAgent extends Agent {
	private AID testerAgent;
	private long period;
	
	// Maps a participant AID to the counters of incoming, outgoing and
	// total messages to be exchanged with that participant
	private Hashtable participants = new Hashtable();
	
	private boolean testOK = false;
	
	protected void setup() {
		// Get the tester agent name and period as arguments
		Object[] args = getArguments();
		testerAgent = new AID((String) args[0], AID.ISLOCALNAME);
		period = Long.parseLong((String) args[1]);
		
		// Each time send a message to each participant
		addBehaviour(new TickerBehaviour(this, period + 10) {
			private ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			
			protected void onTick() {
				if (participants.size() > 0) {
					Enumeration e = participants.keys();
					while (e.hasMoreElements()) {
						AID recv = (AID) e.nextElement();
						int[] cnt = (int[]) participants.get(recv);
						if (cnt[1] < cnt[2]) {
							msg.clearAllReceiver();
							msg.addReceiver(recv);
							msg.setContent(String.valueOf(++cnt[1]));
							myAgent.send(msg);
							checkFinished(recv, cnt);
						}
					}
				}
			}
		} );
		
		// Receive messages from participants
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					AID sender = msg.getSender();
					int val = Integer.parseInt(msg.getContent());
					int[] cnt = (int[]) participants.get(sender);
					
					if (cnt == null) {
						// First message from sender --> Val is the number of messages 
						Logger.println("Conversation with participant "+sender.getLocalName()+" started");
						cnt = new int[] {0, 0, val};
						participants.put(sender, cnt);
					}
					else {
						// Normal message from sender
						if (val == cnt[0]+1) {
							cnt[0] = val;
							checkFinished(sender, cnt);
						}
						else {
							// Out of sequence
							Logger.println("Error receiving message from participant "+sender.getLocalName()+". Expected # "+String.valueOf(cnt[0]+1)+", found # "+val);
							notifyTester();
						}
					}
				}
				else {
					block();
				}
			}
		} );
	}

	private void notifyTester() {
		// Notify the tester agent
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(testerAgent);
		msg.setContent(String.valueOf(testOK));
		send(msg);
	}
		
	/**
	   When all messages (cnt[2]) have been received (cnt[0]) and sent 
	   (cnt[1]) from/to agent id, remove id from the list of participants
	 */
	private void checkFinished(AID id, int[] cnt) {
		if (cnt[0] >= cnt[2] && cnt[1] >= cnt[2]) {
			participants.remove(id);
			Logger.println("Conversation with participant "+id.getLocalName()+" completed");
			if (participants.size() == 0) {
				Logger.println("Test successfully completed");
				testOK = true;
				notifyTester();
			}
		}
	}
}

