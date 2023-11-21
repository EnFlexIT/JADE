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

package test.mobility.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.Location;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.common.*;
import test.mobility.MobilityTesterAgent;

import java.util.Vector;

/**
   Test agent cloning and the transfer of an agent whose status includes a big amount 
   of data.
   @author Giovanni Caire - TILAB
 */
public class TestCloneBigAgent extends Test {
	private static final int BIG_VECTOR_SIZE = 100000;
  private static final String CONV_ID = "conv-id";
  
  public Behaviour load(Agent a) throws TestException {
  	
		// Create the test behaviour
  	Behaviour b = new SimpleBehaviour(a) {
  		private AID parentID;
  		private Location remote;
  		private int step = 0;
  		private Vector bigVector = new Vector();
  		
  		public void onStart() {
  			parentID = (AID) myAgent.getAID().clone();
				String c1 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER1_KEY);
				remote = new ContainerID(c1, null);
				
				// Initialize the Big Vector
  			for (int i = 0; i < BIG_VECTOR_SIZE; ++i) {
  				bigVector.addElement(new byte[10]);
  			}
  		}
  		
  		public void action() {
  			if (step == 0) {
  				// Clone to the remote container
  				log("Agent "+myAgent.getName()+" cloning to container "+remote.getName());
  				myAgent.doClone(remote, myAgent.getLocalName()+"-cloned");
  				step = 1;
  			}
  			else {
  				if (myAgent.getAID().equals(parentID)) {
  					// This is the parent agent.
  					// Wait for the notification message from the cloned agent
  					ACLMessage msg = myAgent.receive(MessageTemplate.MatchConversationId(CONV_ID));
  					if (msg != null) {
  						if (msg.getPerformative() == ACLMessage.INFORM) {
  							passed("Agent cloning successful");
  						}
  						else {
  							failed(msg.getContent());
  						}
  						step = 2;
  					}
  					else {
  						block();
  					}
  				}
  				else {
  					// This is the cloned agent.
  					System.out.println("Cloned agent "+myAgent.getName()+" alive");
  					ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);
  					msg.setConversationId(CONV_ID);
  					msg.addReceiver(parentID);
	  				// Check if we have successfully cloned to the remote container
	  				if (myAgent.here().equals(remote)) {
	  					// Check the big vector
	  					if (bigVector != null && bigVector.size() == BIG_VECTOR_SIZE) {
		  					msg.setPerformative(ACLMessage.INFORM);
	  					}
	  					else {
	  						msg.setContent("Big Vector transfer error");
	  					}
	  				}
	  				else {
	  					msg.setContent("Agent cloning error");
	  				}
	  				myAgent.send(msg);
	  				myAgent.doDelete();
	  				step = 2;
  				}
  			}
  		}
  		
  		public boolean done() {
  			return step >= 2;
  		}
  		
  		public int onEnd() {
  			bigVector.clear();
  			bigVector = null;
  			return 0;
  		}
  	};
  			
  	return b;
  }
}
