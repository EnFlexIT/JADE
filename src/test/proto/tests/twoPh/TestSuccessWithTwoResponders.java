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

package test.proto.tests.twoPh;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.proto.*;
import test.common.*;
import test.common.Logger;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

/**
   Test the Two phase commit protocol support in the case that 
   all responders reply successfully in all steps.
   @author Giovanni Caire - TILAB
   @author Elena Quarantotto - TILAB
 */
public class TestSuccessWithTwoResponders extends Test {
	  
	private static final String R1 = "r1";
	private static final String R2 = "r2";
	private AID r1, r2;
	private boolean success = true;
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
    final Logger l = Logger.getLogger();
    
    // Start the responders
    r1 = TestUtility.createAgent(a, R1, "test.proto.tests.twoPh.TestSuccessWithTwoResponders$Responder", null);
    r2 = TestUtility.createAgent(a, R2, "test.proto.tests.twoPh.TestSuccessWithTwoResponders$Responder", null);
    
    // Create the initial message
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setProtocol(TwoPhConstants.JADE_TWO_PHASE_COMMIT);
    cfp.addReceiver(r1);
    cfp.addReceiver(r2);
    cfp.setReplyByDate(new Date(System.currentTimeMillis() + 600000)); // 30 sec timeout
    
    // Create and return the behaviour that will actually perform the test
  	Behaviour b = new TwoPhInitiator(a, cfp) {
  		int cnt = 0;

  		protected void handlePropose(ACLMessage propose) {
              l.log("\n\nLOG - (TwoPhInitiator, handlePropose(), " + myAgent.getLocalName() + ") - " + propose);
  		}
  		
	    protected void handleAllPh0Responses(Vector responses, Vector proposes, Vector pendings, Vector nextPhMsgs) {
	    	boolean error = false;
	    	if (proposes.size() != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        proposes.size() + " proposes received while 2 were expected.");
	    		error = true;
	    	}
	    	if (pendings.size() != 0) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                    pendings.size() + " pendings still present while 0 were expected.");
	    		error = true;
	    	}
	    	Enumeration e = nextPhMsgs.elements();
	    	int cnt = 0;
	    	while (e.hasMoreElements()) {
	    		ACLMessage msg = (ACLMessage) e.nextElement();
	    		if (msg.getPerformative() == ACLMessage.QUERY_IF) {
	    			cnt++;
	    		}
	    	}
	    	if (cnt != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt + " query-if messages prepared while 2 were expected.");
	    		error = true;
	    	}
	    	if (error) {
	    		nextPhMsgs.clear();
                success = success && false;
	    	}
	    }
  		
  		protected void handleConfirm(ACLMessage confirm) {
              l.log("\n\nLOG - (TwoPhInitiator, handleConfirm(), " + myAgent.getLocalName() + ") - " +
                      "Confirm message received from responder " + confirm.getSender().getLocalName());
  		}
  		
	    protected void handleAllPh1Responses(Vector confirms, Vector disconfirms, Vector informs, Vector pendings, Vector nextPhMsgs) {
	    	boolean error = false;
	    	if (confirms.size() != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        confirms.size()+" confirms received while 2 were expected.");
	    		error = true;
	    	}
	    	if (disconfirms.size() != 0) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        disconfirms.size()+" disconfirms received while 0 were expected.");
	    		error = true;
	    	}
	    	if (informs.size() != 0) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        informs.size()+" informs received while 0 were expected.");
	    		error = true;
	    	}
	    	if (pendings.size() != 0) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        pendings.size()+" pendings still present while 0 were expected.");
	    		error = true;
	    	}
	    	Enumeration e = nextPhMsgs.elements();
	    	int cnt = 0;
	    	while (e.hasMoreElements()) {
	    		ACLMessage msg = (ACLMessage) e.nextElement();
	    		if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
	    			cnt++;
	    		}
	    	}
	    	if (cnt != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt+" accept_proposal messages prepared while 2 were expected.");
	    		error = true;
	    	}
	    	if (error) {
	    		nextPhMsgs.clear();
                success = success && false;
	    	}
	    }
	    
  		protected void handlePh2Inform(ACLMessage inform) {
              l.log("\n\nLOG - (TwoPhInitiator, handlePh2Inform(), " + myAgent.getLocalName() + ") - " +
                      "Inform message received from responder "+inform.getSender().getLocalName());
  		}
  		
		  protected void handleAllPh2Responses(Vector responses) {
	    	Enumeration e = responses.elements();
	    	int cnt = 0;
	    	while (e.hasMoreElements()) {
	    		ACLMessage msg = (ACLMessage) e.nextElement();
	    		if (msg.getPerformative() == ACLMessage.INFORM) {
	    			cnt++;
	    		}
	    	}
	    	if (cnt != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh2Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt+" informs received while 2 were expected.");
	    	}
	    	else {
                success = success && true;
	    	}
		  }
		  
  		public int onEnd() {
              int ret;
              if(!success)
                ret = Test.TEST_FAILED;
              else
                ret = Test.TEST_PASSED;
  			store.put(key, new Integer(ret));
  			return 0;
  		}	
  	};
  	
  	return b;
  }
  
  public void clean(Agent a) {
  	try {
  		TestUtility.killAgent(a, r1);
  		TestUtility.killAgent(a, r2);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}	
  }  
  
  /**
     Inner class Responder
   */
  public static class Responder extends Agent {
    final Logger l = Logger.getLogger();
  	protected void setup() {
  		addBehaviour(new TwoPhResponder(this, TwoPhResponder.createMessageTemplate()) {
  			protected ACLMessage handleCfp(ACLMessage cfp) {
                l.log("\n\nLOG - (Responder, handleCfp(), " + myAgent.getLocalName() +
                        ") - received -------------> " + cfp);
                ACLMessage propose = null;
                try {
                    propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                l.log("\n\nLOG - (Responder, handleCfp(), " + myAgent.getLocalName() +
                        ") - send -------------> " + propose);
                return propose;
  			}
  			
  			protected ACLMessage handleQueryIf(ACLMessage queryIf) {
                  l.log("\n\nLOG - (Responder, handleQueryIf(), " + myAgent.getLocalName() +
                          ") - received --------------> " + queryIf);
  				ACLMessage confirm = queryIf.createReply();
  				confirm.setPerformative(ACLMessage.CONFIRM);
                l.log("\n\nLOG - (Responder, handleQueryIf(), " + myAgent.getLocalName() +
                        ") - send --------------> " + confirm);
  				return confirm;
  			}
  			
  			protected ACLMessage handleAcceptProposal(ACLMessage accept) {
                  l.log("\n\nLOG - (Responder, handleAcceptProposal(), " + myAgent.getLocalName() +
                          ") - received --------------> " + accept);
  				ACLMessage inform = accept.createReply();
  				inform.setPerformative(ACLMessage.INFORM);
                l.log("\n\nLOG - (Responder, handleAcceptProposal(), " + myAgent.getLocalName() +
                        ") - send --------------> " + inform);
  				return inform;
  			}
  		} );
  	}
  } // END of inner class Responder
}
