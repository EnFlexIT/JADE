package examples.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.*;

/**
 * Test of the ReceiverBehaviour and at the same time example of usage
 * of ReceiverBehaviour, WakerBehaviour, and SimpleBehaviour.
 * Just run
 * java jade.Boot x:examples.behaviours.TestReceiverBehaviourAgent
 * and after about 1 minute it will say if the test passed or not.
 * @author Fabio Bellifemine, TILab
 */

public class TestReceiverBehaviourAgent extends Agent {

    private ReceiverBehaviour be1, be2;
    private ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);

    public void setup() {
	System.out.println("TestReceiverBehaviourAgent started. This agent tests the ReceiverBehaviour.");

	// this behaviour waits until a message arrives
	be1 = new ReceiverBehaviour(this, -1, null); 
	addBehaviour(be1);

	// this behaviour waits for 40 seconds because no INFORM_REF message 
	// will arrive.
	be2 = new ReceiverBehaviour(this, 40000, MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));
	addBehaviour(be2);

	// After 10 seconds the behaviour sends to itself the INFORM message
	addBehaviour(new WakerBehaviour(this, 10000) {
		protected void handleElapsedTimeout() {
		    msg1.setContent("prova");
		    msg1.addReceiver(myAgent.getAID());
		    myAgent.send(msg1);
		    System.out.println("Message Sent by the WakerBehaviour");
		}
	    });

	addBehaviour(new SimpleBehaviour(this) {

		boolean finished = false;

		public void action() {
		    ACLMessage rx;
		    if (be1.done()) {
			try {
			    rx = be1.getMessage();
			    if (!msg1.getContent().equals(rx.getContent())) { 
				System.out.println("TEST NOT PASSED. WRONG MESSAGE RECEIVED:\n"+rx+"\nINSTEAD OF:\n"+msg1);
				myAgent.doDelete();
			    }
			} catch (Exception e1) {
			    System.out.println("TEST NOT PASSED. UNEXPECTED EXCEPTION THROWN"); 
			    e1.printStackTrace();
			    myAgent.doDelete();
			}
			System.out.println("First ReceiverBehaviour was terminated.");
		    } else {
			try {
			    rx = be1.getMessage();
			} catch (ReceiverBehaviour.NotYetReady e2) {
			} catch (Exception e3) {
			    System.out.println("TEST NOT PASSED. UNEXPECTED EXCEPTION THROWN"); 
			    e3.printStackTrace();			     
			    myAgent.doDelete();
			}
			System.out.println("First ReceiverBehaviour was not terminated.");
		    }
		    
		    if (be2.done()) {
			try {
			    rx = be2.getMessage();
			    System.out.println("TEST NOT PASSED. UNEXPECTED MESSAGE RECEIVED:\n"+rx);
			    myAgent.doDelete();
			} catch (ReceiverBehaviour.TimedOut e3) {
			} catch (Exception e4) {
			    System.out.println("TEST NOT PASSED. UNEXPECTED EXCEPTION THROWN"); 
			    e4.printStackTrace();
			    myAgent.doDelete();
			}
			System.out.println("Second ReceiverBehaviour was terminated.");
		    } else {
			try {
			    rx = be2.getMessage();
			} catch (ReceiverBehaviour.NotYetReady e5) {
			} catch (Exception e6) {
			    System.out.println("TEST NOT PASSED. UNEXPECTED EXCEPTION THROWN"); 
			    e6.printStackTrace();
			    myAgent.doDelete();
			}
			System.out.println("Second ReceiverBehaviour was not terminated.");
		    }

		    finished = (be1.done() && be2.done());
		    if (finished) 
			doDelete();
		    else
			block(2000); // block 2 seconds
		}
		
		public boolean done() {
		    return finished;
		}
	    });
    }

    protected void takeDown() {
	System.out.println("TEST TERMINATED. IF NO TEST NOT PASSED MESSAGE WAS PRINTED OUT, THEN THE TEST HAS BEEN PASSED.");
    }
}
