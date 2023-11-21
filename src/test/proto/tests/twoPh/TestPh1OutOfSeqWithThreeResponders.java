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
import jade.lang.acl.*;
import jade.proto.*;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
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
public class TestPh1OutOfSeqWithThreeResponders extends Test {

	private static final String R1 = "r1";
	private static final String R2 = "r2";
    private static final String R3 = "r3";
	private static AID r1, r2, r3;
	private boolean success = true;

    public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
    final DataStore store = ds;
    final String key = resultKey;
    final Logger l = Logger.getLogger();

    // Start the responders

    r1 = TestUtility.createAgent(a, R1, "test.proto.tests.twoPh.TestPh1OutOfSeqWithThreeResponders$Responder", null);
    r2 = TestUtility.createAgent(a, R2, "test.proto.tests.twoPh.TestPh1OutOfSeqWithThreeResponders$Responder", null);
    r3 = TestUtility.createAgent(a, R3, "test.proto.tests.twoPh.TestPh1OutOfSeqWithThreeResponders$SimpleResponder", null);

    // Create the initial message
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setProtocol(TwoPhConstants.JADE_TWO_PHASE_COMMIT);
    cfp.addReceiver(r1);
    cfp.addReceiver(r2);
    cfp.addReceiver(r3);

    // Create and return the behaviour that will actually perform the test
    Behaviour b = new TwoPhInitiator(a, cfp) {
        int cnt = 0;

        /* PHASE 0 */
        protected void handlePropose(ACLMessage propose) {
              l.log("\n\nLOG - (TwoPhInitiator, handlePropose(), " + myAgent.getLocalName() +
                      ") - " + propose);
              if(!propose.getSender().getLocalName().equals(R1) &&
                      !propose.getSender().getLocalName().equals(R2) &&
                      !propose.getSender().getLocalName().equals(R3)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handlePropose(), " + myAgent.getLocalName() + ") - " +
                    propose.getSender().getLocalName() + " sender while r1 or r2 or r3 were expected.");
                    success = success && false;
              }
        }

        protected void handleAllPh0Responses(Vector responses, Vector proposes, Vector pendings, Vector nextPhMsgs) {
            boolean error = false;
            if (proposes.size() != 3) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        proposes.size() + " proposes received while 3 was expected.");
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
            if (cnt != 3) {
                l.log("\n\nLOG - (TwoPhInitiator, handleAllPh0Responses(), " + myAgent.getLocalName() + ") - " +
                        cnt + " query-if messages prepared while 3 was expected.");
                error = true;
            }
            if (error) {
                nextPhMsgs.clear();
            }
        }

        /* PHASE 1 */
        protected void handleConfirm(ACLMessage confirm) {
              l.log("\n\nLOG - (TwoPhInitiator, handleConfirm(), " + myAgent.getLocalName() +
                      ") - " + confirm);
              if(!confirm.getSender().getLocalName().equals(R1) &&
                      !confirm.getSender().getLocalName().equals(R2)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleConfirm(), " + myAgent.getLocalName() + ") - " +
                    confirm.getSender().getLocalName() + " sender while r1 or r2 were expected.");
                    success = success && false;
              }
        }

        protected void handleDisconfirm(ACLMessage disconfirm) {
              l.log("\n\nLOG - (TwoPhInitiator, handleDisconfirm(), " + myAgent.getLocalName() +
                      ") - " + disconfirm);
              if(!disconfirm.getSender().getLocalName().equals(R3)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleDisconfirm(), " + myAgent.getLocalName() + ") - " +
                    disconfirm.getSender().getLocalName() + " sender while r3 was expected.");
                    success = success && false;
              }
        }


          protected void handleNotUnderstood(ACLMessage notUnderstood) {
              l.log("\n\nLOG - (TwoPhInitiator, handleNotUnderstood(), " + myAgent.getLocalName() +
                      ") - " + notUnderstood);
              if(!notUnderstood.getSender().getLocalName().equals(R3)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleNotUnderstood(), " + myAgent.getLocalName() + ") - " +
                        notUnderstood.getSender().getLocalName() + " sender while r3 was expected.");
                    success = success && false;
              }
              if(!getCurrentPhase().equals(TwoPhInitiator.PH1_STATE)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleNotUnderstood(), " + myAgent.getLocalName() + ") - " +
                        getCurrentPhase() + " while ph1 was expected.");
                    success = success && false;
              }
          }

        protected void handleFailure(ACLMessage failure) {
            l.log("\n\nLOG - (TwoPhInitiator, handleFailure(), " + myAgent.getLocalName() +
                    ") - " + failure);
            if(!failure.getSender().getLocalName().equals(R3)) {
                  l.log("\n\nLOG - (TwoPhInitiator, handleFailure(), " + myAgent.getLocalName() + ") - " +
                      failure.getSender().getLocalName() + " sender while r3 was expected.");
                  success = success && false;
            }
            if(!getCurrentPhase().equals(TwoPhInitiator.PH1_STATE)) {
                  l.log("\n\nLOG - (TwoPhInitiator, handleFailure(), " + myAgent.getLocalName() + ") - " +
                      getCurrentPhase() + " while ph1 was expected.");
                  success = success && false;
            }
        }

          protected void handleOutOfSequence(ACLMessage msg) {
          	l.log(myAgent.getLocalName()+"["+getCurrentPhase()+"]: Out-of-sequence "+ACLMessage.getPerformative(msg.getPerformative())+" message received.");
              /*l.log("\n\nLOG - (TwoPhInitiator, handleOutOfSequence(), " + myAgent.getLocalName() +
                      ") - " + msg);

              if(!msg.getSender().getLocalName().equals(R3)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleOutOfSequence(), " + myAgent.getLocalName() + ") - " +
                        msg.getSender().getLocalName() + " sender while r3 was expected.");
                    success = success && false;
              }
              if(msg.getPerformative() != ACLMessage.PROPAGATE) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleOutOfSequence(), " + myAgent.getLocalName() + ") - " +
                        ACLMessage.getPerformative(msg.getPerformative()) + " message while PROPAGATE was expected.");
                    success = success && false;
              }
              if(!getCurrentPhase().equals(TwoPhInitiator.PH1_STATE)) {
                    l.log("\n\nLOG - (TwoPhInitiator, handleOutOfSequence(), " + myAgent.getLocalName() + ") - " +
                        getCurrentPhase() + " while ph1 was expected.");
                    success = success && false;
              }*/
          }


        protected void handlePh2Inform(ACLMessage inform) {
            l.log("\n\nLOG - (TwoPhInitiator, handlePh2Inform(), " + myAgent.getLocalName() + ") - " +
                "Inform message received from responder "+inform.getSender().getLocalName());
            if(!inform.getSender().getLocalName().equals(R1) && !inform.getSender().getLocalName().equals(R2)) {
                l.log("\n\nLOG - (TwoPhInitiator, handlePh2Inform(), " + myAgent.getLocalName() + ") - " +
                    inform.getSender().getLocalName() + " sender while r1 or r2 were expected.");
                success = success && false;
            }
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
                        cnt+" informs received while 2 was expected.");
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
            TestUtility.killAgent(a, r3);
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
                          ") - received --------------> " + cfp);
                  ACLMessage response = null;
                  try {
                      response = cfp.createReply();
                      response.setPerformative(ACLMessage.PROPOSE);
                  } catch(Exception e) {
                      e.printStackTrace();
                  }
                  l.log("\n\nLOG - (Responder, handleCfp(), " + myAgent.getLocalName() +
                          ") - send --------------> " + response);
                  return response;
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

    public static class SimpleResponder extends Agent {
        final Logger l = Logger.getLogger();

        protected void setup() {
            addBehaviour(new SimpleBehaviour(this) {
                private boolean finished = false;
                private ACLMessage msg;

                public void action() {
                    msg = myAgent.receive();
                    if (msg != null) {
                        l.log("\n\nLOG - (SimpleResponder, prepareResponse(), " + myAgent.getLocalName() + ") - " +
                                            "received --------------> " + msg);
                        if(msg.getPerformative() == ACLMessage.CFP) {
                            ACLMessage propose = msg.createReply();
                            propose.setPerformative(ACLMessage.PROPOSE);
                            l.log("\n\nLOG - (SimpleResponder, prepareResponse(), " + myAgent.getLocalName() + ") - " +
                                 "send --------------> " + propose);
                            myAgent.send(propose);
                        } else {
                            myAgent.addBehaviour(new TickerBehaviour(myAgent, 1000) {
                                public void onTick() {
                                   switch(getTickCount()) {
                                       case 1:
                                           ACLMessage propagate = msg.createReply();
                                           propagate.setPerformative(ACLMessage.PROPAGATE);
                                           l.log("\n\nLOG - (SimpleResponder, prepareResponse(), " + myAgent.getLocalName() + ") - " +
                                                "send --------------> " + propagate);
                                           myAgent.send(propagate);
                                           break;
                                       case 2:
                                           ACLMessage failure = msg.createReply();
                                           failure.setPerformative(ACLMessage.FAILURE);
                                           l.log("\n\nLOG - (SimpleResponder, prepareResponse(), " + myAgent.getLocalName() + ") - " +
                                                "send --------------> " + failure);
                                           myAgent.send(failure);
                                           break;
                                       case 3:
                                           ACLMessage notUnderstood = msg.createReply();
                                           notUnderstood.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                                           l.log("\n\nLOG - (SimpleResponder, prepareResponse(), " + myAgent.getLocalName() + ") - " +
                                                "send --------------> " + notUnderstood);
                                           myAgent.send(notUnderstood);
                                           break;
                                       case 4:
                                           ACLMessage disconfirm = msg.createReply();
                                           disconfirm.setPerformative(ACLMessage.DISCONFIRM);
                                           l.log("\n\nLOG - (SimpleResponder, prepareResponse(), " + myAgent.getLocalName() + ") - " +
                                                "send --------------> " + disconfirm);
                                           myAgent.send(disconfirm);
                                           break;
                                   }
                                }
                            });
                            finished = true;
                        }
                    }
                }

                public boolean done() {
                    return finished;
                }
            });
        }
    }
}



