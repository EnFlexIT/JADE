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
import jade.util.leap.Iterator;
import test.common.*;
import test.domain.ams.*;

/**
   Test the getFailedReceiver() method of the AMSService class.
   A message is sent to a non-existing agent, the AMS FAILURE
   is received and the AMSService.getFailedReceiver() method is 
   applied to it.
   The the passes if the returned value is actually the receiver AID.
   @author Giovanni Caire - TILAB
 */
public class TestFailedReceiver extends Test {
	private static final String ADDR1 = "dummy-address-1";
	private static final String ADDR2 = "dummy-address-2";
	private static final AID dummyResolver = new AID("dummy-resolver", AID.ISLOCALNAME);
	private static final AID dummy = new AID("dummy", AID.ISLOCALNAME);
	
  public Behaviour load(Agent a) throws TestException {
  	setTimeout(5000);
  	
  	return new SimpleBehaviour(a) {
  		private boolean finished = false;
  		
  		public void onStart() {
  			dummy.addAddresses(ADDR1);
  			dummy.addAddresses(ADDR2);
  			dummy.addResolvers(dummyResolver);
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(dummy);
  			myAgent.send(msg);
  		}
  		
  		public void action() {
  			ACLMessage reply = myAgent.receive();
  			if (reply != null) {
  				finished = true;
  				log("Reply received");
  				if (reply.getPerformative() == ACLMessage.FAILURE && reply.getSender().equals(myAgent.getAMS())) {
						try {
  						AID failedReceiver = AMSService.getFailedReceiver(myAgent, reply);
  						if (failedReceiver.equals(dummy)) {
  							Iterator it = failedReceiver.getAllAddresses();
  							if (it.next().equals(ADDR1) && it.next().equals(ADDR2) &&(!it.hasNext())) {
  								Iterator it1 = failedReceiver.getAllResolvers();
  								if (it1.next().equals(dummyResolver) && (!it1.hasNext())) {
  									passed("Failed receiver correct.");
  								}
  								else {
  									failed("Wrong resolver.");
  								}
  							}
  							else {
  								failed("Wrong addresses.");
  							}
  						}
  						else {
  							failed("Wrong name.");
  						}
						}
						catch (FIPAException fe) {
							failed("Error in getFailedReceiver(). "+fe.getMessage());
						}
						catch (Exception e) {
							failed("Unexpected error. "+e);
						}
  				}
  				else {
  					failed("Unexpected message received. "+reply);
  				}
  			}
  			else {
  				block();
  			}
  		}
  		
  		public boolean done() {
  			return finished;
  		}
  	};  			
  }
}
