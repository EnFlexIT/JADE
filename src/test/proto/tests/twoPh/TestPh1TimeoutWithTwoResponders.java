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
import java.util.Iterator;
import java.util.Date;

/**
   Test the Two phase commit protocol support in the case that
   all responders reply successfully in all steps.
   @author Giovanni Caire - TILAB
   @author Elena Quarantotto - TILAB
 */
public class TestPh1TimeoutWithTwoResponders extends Test {

	private static final String R1 = "r1";
	private static final String R2 = "r2";
	private AID r1, r2;
	private boolean success = true;

  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
    final Logger l = Logger.getLogger();

    // Start the responders

    r1 = TestUtility.createAgent(a, R1, "test.proto.tests.twoPh.TestPh1TimeoutWithTwoResponders$Responder",
            new String[] {"don't timeout", ACLMessage.getPerformative(ACLMessage.CONFIRM)});
    r2 = TestUtility.createAgent(a, R2, "test.proto.tests.twoPh.TestPh1TimeoutWithTwoResponders$Responder",
            new String[] {"timeout", ACLMessage.getPerformative(ACLMessage.CONFIRM)});

    // Create the initial message
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setProtocol(TwoPhConstants.JADE_TWO_PHASE_COMMIT);
    cfp.addReceiver(r1);
    cfp.addReceiver(r2);

    // Create and return the behaviour that will actually perform the test
  	Behaviour b = new TwoPhInitiator(a, cfp) {
  		int cnt = 0;

  		protected void handlePropose(ACLMessage propose) {
              l.log("\n\nLOG - (TwoPhInitiator, handlePropose(), " + myAgent.getLocalName() +
                      ") - " + propose);
  		}

	    protected void handleAllPh0Responses(Vector responses, Vector proposes, Vector pendings, Vector nextPhMsgs) {
	    	boolean error = false;
	    	if (proposes.size() != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        proposes.size() + " proposes received while 2 was expected.");
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
                    l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        msg + " before setting timeout");
	    			cnt++;
                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                    l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        msg + " after setting timeout");
	    		}
	    	}
	    	if (cnt != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt + " query-if messages prepared while 2 was expected.");
	    		error = true;
	    	}
	    	if (error) {
	    		nextPhMsgs.clear();
                success = success && false;
	    	}
	    }



          protected void handleConfirm(ACLMessage confirm) {
              l.log("\n\nLOG - (TwoPhInitiator, handleConfirm(), " + myAgent.getLocalName() +
                      ") - " + confirm);
          }

          protected void handleAllPh1Responses(Vector responses, Vector confirms, Vector disconfirms,
                                               Vector informs, Vector pendings, Vector nextPhMsgs) {
              boolean error = false;
	    	if (confirms.size() != 1) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh1Responses(), " + myAgent.getLocalName() + ") - " +
                        confirms.size() + " confirms received while 1 was expected.");
	    		error = true;
	    	}
	    	if (disconfirms.size() != 0) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh1Responses(), " + myAgent.getLocalName() + ") - " +
                        disconfirms.size() + " disconfirms received while 0 was expected.");
	    		error = true;
	    	}
              if (informs.size() != 0) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh1Responses(), " + myAgent.getLocalName() + ") - " +
                        informs.size() + " informs received while 0 was expected.");
	    		error = true;
	    	}
	    	if (pendings.size() != 1) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh1Responses(), " + myAgent.getLocalName() + ") - " +
                    pendings.size() + " pendings still present while 1 were expected.");
	    		error = true;
	    	}
	    	Enumeration e = nextPhMsgs.elements();
	    	int cnt = 0;
	    	while (e.hasMoreElements()) {
	    		ACLMessage msg = (ACLMessage) e.nextElement();
	    		if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
	    			cnt++;
	    		}
	    	}
	    	if (cnt != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh1Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt + " reject-proposal messages prepared while 2 was expected.");
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

          protected void handleOldResponse(ACLMessage old) {
              l.log("\n\nLOG - (TwoPhInitiator, handleOldResponse(), " + myAgent.getLocalName() + ") - " +
                      "Old message received from responder "+old.getSender().getLocalName());
          }

		  protected void handleAllPh2Responses(Vector responses) {
	    	Enumeration e = responses.elements();
	    	int informCnt = 0;
	    	while (e.hasMoreElements()) {
	    		ACLMessage msg = (ACLMessage) e.nextElement();
	    		if (msg.getPerformative() == ACLMessage.INFORM)
	    			informCnt++;
	    	}
	    	if (informCnt != 2) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh2Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt+" informs received while 2 were expected.");

	    	} else
                success = success && true;
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
    private String ph1Response = null;
    private String timeout = null;

  	protected void setup() {
        Object[] args = getArguments();
        timeout = (String) args[0];
		ph1Response = (String) args[1];


  		addBehaviour(new TwoPhResponder(this, TwoPhResponder.createMessageTemplate()) {
  			protected ACLMessage preparePropose(ACLMessage cfp) {
                l.log("\n\nLOG - (Responder, preparePropose(), " + myAgent.getLocalName() +
                        ") - received --------------> " + cfp);
                ACLMessage response = null;
                try {
                    response = cfp.createReply();
                    response.setPerformative(ACLMessage.PROPOSE);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return response;
  			}

              protected ACLMessage handleQueryIf(ACLMessage queryIf) {
                  l.log("\n\nLOG - (Responder, handleQueryIf(), " + myAgent.getLocalName() +
                        ") - received --------------> " + queryIf);
                ACLMessage response = null;
                try {
                    response = queryIf.createReply();
                    response.setPerformative(ACLMessage.getInteger(ph1Response));
                } catch(Exception e) {
                    e.printStackTrace();
                }
                  if(timeout.equals("timeout")) {
                      l.log("\n\nLOG - (Responder, handleQueryIf(), " + myAgent.getLocalName() +
                          ") - SLEEP " + System.currentTimeMillis());
                      try {
                          Thread.sleep(60000);
                      } catch(InterruptedException e) {
                          l.log("\n\nLOG - (Responder, handleQueryIf(), " + myAgent.getLocalName() +
                              ") - WAKE UP " + System.currentTimeMillis());

                      }
                  }
                  l.log("\n\nLOG - (Responder, handleQueryIf(), " + myAgent.getLocalName() +
                          ") - send --------------> " + response);
                  return response;
              }

              protected ACLMessage handleRejectProposal(ACLMessage reject) {
                  l.log("\n\nLOG - (Responder, handleRejectProposal(), " + myAgent.getLocalName() +
                        ") - received --------------> " + reject);
                  ACLMessage inform = reject.createReply();
                  inform.setPerformative(ACLMessage.INFORM);
                  l.log("\n\nLOG - (Responder, handleRejectProposal(), " + myAgent.getLocalName() +
                        ") - send --------------> " + inform);
                  return inform;
              }

  		} );
  	}
  } // END of inner class Responder
}
