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

package test.proto.tests.contractNet;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;
import test.common.*;

import java.util.Vector;

/**
   @author Giovanni Caire - TILAB
 */
public class TestInitiatorNestedInResponder extends Test {
	private static final String RESPONDER_NAME = "r";
  private AID responder;
  
  public Behaviour load(Agent a) throws TestException {
  	// Create the responder with a ContractNetResponder whose HANDLE_CFP state 
  	// is a nested ContractNetInitiator
  	log("Creating responder agent ...");
    responder = TestUtility.createAgent(a, RESPONDER_NAME, getClass().getName()+"$ResponderAgent", null);
  	log("Responder agent successfullhy created.");

  	ACLMessage msg = new ACLMessage(ACLMessage.CFP);
  	msg.addReceiver(responder);
  	msg.setContent(a.getLocalName());
  	
  	ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
  	
  	Behaviour b = new BasicContractNetInitiator(a, msg, null, null, 10000, new int[] {1, 0, 0, 1, 0, 0}) { // 1 PROPOSE and 1 INFORM
  		protected void handleAllResultNotifications(Vector resultNotifications) {
  			if (check()) {
  				passed("Protocol successfully completed.");
  			}
  			else {
  				failed(getDetails());
  			}
  		}
  	};
  	pb.addSubBehaviour(b);
  	
  	b = new ContractNetResponder(a, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {
  		protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
  			log("Aux-CN-responder: CFP received.");
  			ACLMessage reply = cfp.createReply();
  			reply.setPerformative(ACLMessage.PROPOSE);
  			return reply;
  		}
  		
  		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
  			log("Aux-CN-responder: ACCEPT_PROPOSAL received.");
  			ACLMessage reply = accept.createReply();
  			reply.setPerformative(ACLMessage.INFORM);
  			return reply;
  		}
  		
  		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
  			failed("Aux-CN-responder: Unexpected REJECT_PROPOSAL received.");
  		}
  	};
  	pb.addSubBehaviour(b);
  	
  	return pb;
  }  
  
  
  public void clean(Agent a) {
  	try {
  		TestUtility.killAgent(a, responder);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  
	/**
	   Inner class ResponderAgent.
	   This is an agent that executes a ContractNetResponder with a nested
	   ContractNetInitiator registered in the HANDLE_CFP state
	 */
	public static class ResponderAgent extends Agent {
		protected void setup() {
			ContractNetResponder cnresp = new ContractNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {
	  		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
	  			System.out.println("Responder: ACCEPT_PROPOSAL received.");
	  			ACLMessage reply = accept.createReply();
	  			reply.setPerformative(ACLMessage.INFORM);
	  			return reply;
	  		}
	  		
	  		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
	  			System.out.println("Responder: Unexpected REJECT_PROPOSAL received.");
	  		}
			};
			
			cnresp.registerHandleCfp(new ContractNetInitiator(this, null) {
				protected Vector prepareCfps(ACLMessage innerCfp) {
	  			System.out.println("Inner-initiator: prepareCfps()");
					ContractNetResponder parent = (ContractNetResponder) getParent();
					ACLMessage cfp = (ACLMessage) getDataStore().get(parent.CFP_KEY);
					AID innerResponder = new AID(cfp.getContent(), AID.ISLOCALNAME);
	  			System.out.println("Inner-initiator: responder is "+innerResponder.getName());
					
					innerCfp = new ACLMessage(ACLMessage.CFP);
					innerCfp.addReceiver(innerResponder);
					Vector v = new Vector();
					v.add(innerCfp);
					return v;
				}
				
			  protected void handlePropose(ACLMessage innerPropose, Vector acceptances) {
	  			System.out.println("Inner-initiator: handlePropose()");
	  			ACLMessage innerAccept = innerPropose.createReply();
	  			innerAccept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
	  			acceptances.add(innerAccept);
			  }
				
			  protected void handleInform(ACLMessage innerInform) {
	  			System.out.println("Inner-initiator: handleInform()");
					ContractNetResponder parent = (ContractNetResponder) getParent();
					ACLMessage cfp = (ACLMessage) getDataStore().get(parent.CFP_KEY);
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					getDataStore().put(parent.REPLY_KEY, propose);
			  }

			  protected void handleAllResultNotifications(Vector resultNotifications) {
			  	if (resultNotifications.size() != 1) {
		  			System.out.println("Inner-initiator: handleResultNotifications() "+resultNotifications.size()+" notification messages received while 1 was expected");
			  	}
			  	else {
			  		ACLMessage notif = (ACLMessage) resultNotifications.get(0);
			  		if (notif.getPerformative() != ACLMessage.INFORM) {
			  			System.out.println("Inner-initiator: handleResultNotifications() "+ACLMessage.getPerformative(notif.getPerformative())+" notification received while INFORM was expected");
			  		}
			  	}
			  }
			} );
			
			addBehaviour(cnresp);
		}
	} // END Of inner class ResponderAgent
}

