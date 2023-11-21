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

package test.leap.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import test.common.*;
import test.leap.LEAPTesterAgent;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
   This test addresses the preservation of message order in a MIDP 
   environment.
   
   The test works as follows:
   An agent (the "light agent") on a MIDP container carries out "conversations" 
   with N participants agents in the fixed network. Conversation with 
   each participant Pi consists of sending K messages to Pi and 
   receiving K messages from Pi. Both the light agent and the participants 
   waits a fixed time T after sending each message.
   
   The test passes if all conversations are carried out (including message 
   order) successfully.
   
   Arguments:
   1) N: the number of participants (default 10)
   2) K: the number of messages in each conversation (default 10)
   3) T: the time interval in ms after sending each message (default 1000)
   4) The name of the midp container (default "Container-1")
   
   @author Giovanni Caire - TILAB
 */
public class TestMessageOrder extends Test {
	private static final String LIGHT_AGENT = "light";
	
	private int nParticipants = 10;
	private int nMessages = 10;
	private long period = 1000;
	private String lightContainerName = "Container-1";
	
	private Vector participants = new Vector();
	private boolean lightDone = false;
	
  public Behaviour load(Agent a) throws TestException {
		// Get the number of participants as test argument
		try {
			nParticipants = Integer.parseInt(getTestArgument("n-participants"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		// Get the number of messages as test argument
		try {
			nMessages = Integer.parseInt(getTestArgument("n-messages"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		// Get the time interval as test argument
		try {
			period = Long.parseLong(getTestArgument("period"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		// Get the light container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);

		// Create the light agent on the light container
		TestUtility.createAgent(a, LIGHT_AGENT, "test.leap.midp.MessageOrderAgent", new String[] {a.getLocalName(), String.valueOf(period)}, a.getAMS(), lightContainerName);

		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ALL);
		
		// At each tick launch a new participant
		long t = period / nParticipants;
		if (t < 10) {
			t = 10;
		}
		pb.addSubBehaviour(new TickerBehaviour(a, t) {
			protected void onTick() {
				int index = getTickCount();
				launchParticipant(index);
				if (index == nParticipants) {
					stop();
				}
			}
			
			private void launchParticipant(int index) {
				String p = "p"+index;
				try {						
					AID id = TestUtility.createAgent(myAgent, p, "test.leap.tests.ParticipantAgent", new String[] {myAgent.getLocalName(), LIGHT_AGENT, String.valueOf(nMessages), String.valueOf(period)});
					participants.add(id);
				}
				catch (Exception e) {
					e.printStackTrace();
					failed("Error launching participant "+p);
				}
			}	
		} );
		
		// Wait for the termination message from both the light agent 
		// and all the participants
		pb.addSubBehaviour(new SimpleBehaviour(a) {
			
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					AID sender = msg.getSender();
					boolean b = ("true".equalsIgnoreCase(msg.getContent()));
					if (sender.getLocalName().equals(LIGHT_AGENT)) {
						// Termination notification from the Light agent
						if (!b) {
							failed("Error notification received from light agent");
						}
						else {
							lightDone = true;
						}
					}
					else if (participants.contains(sender)) {
						// Termination notification from a participant
						if (!b) {
							failed("Error notification received from participant "+sender.getLocalName());
						}
						else {
							participants.remove(sender);
						}
					}
					else {
						// Unexpected message
						log(myAgent.getLocalName()+": Unexpected message received from "+sender.getLocalName());
					}
				}
				else {
					block();
				}
			}
			
			public boolean done() {
				if (lightDone && participants.size() == 0) {
					passed("Termination notification received from light agent and all participants.");
					return true;
				}
				return false;
			}			
		} );
		
		return pb;
	}

	
	public void clean(Agent a) {
		// Kill light agent and all participants
		try {
			TestUtility.killAgent(a, new AID(LIGHT_AGENT, AID.ISLOCALNAME));
		}
		catch (Exception e) {
			log(a.getLocalName()+": exception killing light agent ");
		}
		
		for (int i = 1; i <= nParticipants; ++i) {
			String name = "p"+i;
			try {
				TestUtility.killAgent(a, new AID(name, AID.ISLOCALNAME));
			}
			catch (Exception e) {
				log(a.getLocalName()+": exception killing participant "+name);
			}
		}
	}
}

