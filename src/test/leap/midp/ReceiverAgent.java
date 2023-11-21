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

/**
   This agent is used by the TestMessageReception test. It 
   receives a number of messages and checks that they are in order.
   Each message includes its index (first = 1) as the content.
   @author Giovanni Caire - TILAB
 */
public class ReceiverAgent extends Agent {
	private int cnt = 0;
	private int nMessages = 100;
	private int period = 60000;
	
	protected void setup() {
		Object[] args = getArguments();
		try {
			nMessages = Integer.parseInt((String) args[0]);
		}
		catch (Exception e1) {
			// Keep default
		}
		try {
			period = Integer.parseInt((String) args[1]);
		}
		catch (Exception e1) {
			// Keep default
		}
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					int index = -1;
					try {
						// Check message order
						index = Integer.parseInt(msg.getContent());
						if (index == cnt+1) {
							cnt = index;
							
							// Check test completion
							if (cnt == nMessages) {
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.INFORM);
								myAgent.send(reply);
							}
						}
						else {
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.FAILURE);
							reply.setContent("Expected #"+(cnt+1)+" received #"+index);
							myAgent.send(reply);
						}
					}
					catch (Exception e) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("Unexpected message content: "+msg.getContent());
						myAgent.send(reply);
					}
				}
				else { 
					block();
				}
			}
		} );
	}
}

