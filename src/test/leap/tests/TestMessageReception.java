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
   This Test addresses the basic operations of life-cycle management (agent creation/killing) 
   and communication (message exchange) in a wireless device.   
   @author Giovanni Caire - TILAB
 */
public class TestMessageReception extends Test {
	private static final String RECEIVER_AGENT = "receiver";
	
	private String lightContainerName = "Container-1";
	private long period = 60000;
	private int nMessages = 100;
	private int size = 1000;
	private AID receiver;
	private ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	
  public Behaviour load(final Agent a) throws TestException {
  	
		// MIDP container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);

		// Get the time interval between 2 messages
		try {
			period = Integer.parseInt(getTestArgument("period"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		// Get the number of messages to be received
		try {
			nMessages = Integer.parseInt(getTestArgument("n-messages"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		// Get the size of the content
		try {
			size = Integer.parseInt(getTestArgument("size"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		receiver = TestUtility.createAgent(a, RECEIVER_AGENT, "test.leap.midp.ReceiverAgent", new String[] {String.valueOf(nMessages), String.valueOf(period)}, a.getAMS(), lightContainerName);
		log("Receiver agent correctly created.");
		
		msg.addReceiver(receiver); 
		msg.setByteSequenceContent(new byte[size]);
			
		TickerBehaviour b = new TickerBehaviour(a, period) {
			protected void onTick() {
				if (getTickCount() <= nMessages) {
					myAgent.send(msg);
					log("Message "+getTickCount()+" sent.");
				}
				else {
					msg = myAgent.receive();
					if (msg != null && msg.getPerformative() == ACLMessage.INFORM && msg.getSender().equals(receiver)) {
						passed("Completion notification received from "+RECEIVER_AGENT);
					}
					else {
						failed("Missing completion notification");
					}
				}
			}
		};
		return b;
  }
  
  public void clean(Agent a) {
  	try {
			TestUtility.killAgent(a, receiver);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
}