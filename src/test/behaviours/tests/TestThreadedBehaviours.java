package test.behaviours.tests;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import test.common.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestThreadedBehaviours extends Test {
	private static final String CONV_ID = "__conv-id__";
	
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	private ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	private MessageTemplate msgTemplate = MessageTemplate.and(
		MessageTemplate.MatchConversationId(CONV_ID),
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
	private MessageTemplate rplTemplate = MessageTemplate.and(
		MessageTemplate.MatchConversationId(CONV_ID),
		MessageTemplate.MatchPerformative(ACLMessage.INFORM) );
		
  public Behaviour load(Agent a) throws TestException { 
  	msg.setConversationId(CONV_ID);
  	msg.addReceiver(a.getAID());
  	
  	Behaviour b = new OneShotBehaviour(a) {
  				
  		public void onStart() {
				log("Adding threaded behaviour"); 
  			myAgent.addBehaviour(tbf.wrap(new Behaviour() {
  				private boolean aborted = false;
  				private int status = 0;
  				private ACLMessage rcv;
  				private long t0;
  				
  				public void action() {
  					switch (status) {
  					case 0:
  						// Check the root 
  						if (root() != this) {
  							failed("Unexpected root for threaded behaviour");
  							aborted = true;
  						}
  						else {
  							status++;
  						}
  						break;
  					case 1:
  						// Test receiving messages in blocking mode
  						myAgent.blockingReceive(msgTemplate);
  						log(Thread.currentThread().getName()+": Message 1 received");
  						status++;
  						break;
  					case 2:
  						rcv = myAgent.receive(msgTemplate);
  						if (rcv != null) {
		  					log(Thread.currentThread().getName()+": Message 2 received");
	  						status++;
  						}
  						else {
	  						// Test blocking until a message arrives
	  						block();
  						}
  						break;
  					case 3:
  						// Test blocking for a given amount of time
  						block(5000);
  						t0 = System.currentTimeMillis();
  						status++;
  						break;
  					case 4:
  						long t1 = System.currentTimeMillis();
  						if ((t1 - t0) < 4000) {
  							failed("Blocked for "+(t1-t0)+" ms only while 5000 were expected");
  							aborted = true;
  						}
  						else {
	  						// Send back the reply
	  						ACLMessage reply = rcv.createReply();
	  						reply.setPerformative(ACLMessage.INFORM);
	  						myAgent.send(reply);
		  					log(Thread.currentThread().getName()+": Reply sent");
	  						status++;
  						}
  					}
  				}
  				
  				public boolean done() {
  					return aborted || status == 5;
  				}
  			} ) );
  		}

  		public void action() {
  			// Check the root
  			if (!(root() instanceof TestGroupExecutor)) {
  				failed("Unexpected root for threaded behaviour");
  				return;
  			}
  			try {
	  			log("Wait a bit");
	  			Thread.sleep(5000);
  				log("Sending message 1");
	  			myAgent.send(msg);
	  			log("Wait a bit");
	  			Thread.sleep(5000);
	  			log("Sending message 2");
	  			myAgent.send(msg);
	  			log("Wait for the reply");
	  			ACLMessage reply = myAgent.blockingReceive(rplTemplate, 10000);
	  			if (reply != null) {
	  				passed("Reply received");
	  			}
	  			else {
	  				failed("Timeout expired");
	  			}
  			}
  			catch (Exception e) {
  				failed("Unexpected Exception");
  				e.printStackTrace();
  			}
  		}
  	};
  	
  	return tbf.wrap(b);
  }					
  
  public void clean() {
  	tbf.interrupt();
  }
}

