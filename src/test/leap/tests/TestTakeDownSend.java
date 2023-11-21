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
   This Test checks that messages sent within the agent takeDown() 
   method are correctly delivered.   
   @author Giovanni Caire - TILAB
 */
public class TestTakeDownSend extends Test {
	private String lightContainerName = "Container-1";
	private Vector participants = new Vector();
	private int nParticipants = 2;
	private int nMessages = 10;
	private MessageTemplate recvTemplate;
	private Expectation exp = new Expectation(null);
	
  public Behaviour load(final Agent a) throws TestException {
		// MIDP container name as group argument
		lightContainerName = (String) getGroupArgument(LEAPTesterAgent.LIGHT_CONTAINER_KEY);

		try {
			nParticipants = Integer.parseInt(getTestArgument("n-participants"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		try {
			nMessages = Integer.parseInt(getTestArgument("n-messages"));
		}
		catch (Exception e1) {
			// Ignore and keep default
		}
		
		// Create the participants on the light container
		try {
			String[] args = new String[] {String.valueOf(nMessages), a.getLocalName()};
			for (int i = 0; i < nParticipants; i++) {
				AID id = TestUtility.createAgent(a, "p"+i, "test.leap.midp.TakeDownSenderAgent", args, a.getAMS(), lightContainerName);
				log("Participant "+id.getLocalName()+" correctly created.");
				participants.addElement(id);
				if (recvTemplate == null) {
					recvTemplate = MessageTemplate.MatchSender(id);
				}
				else {
					recvTemplate = MessageTemplate.or(recvTemplate, MessageTemplate.MatchSender(id));
				}
				for (int j = 0; j < nMessages; ++j) {
					exp.addExpectedKey(id.getLocalName()+"#"+j);
				}
			}
		}
		catch (Exception e) {
			throw new TestException("Error creating participant agents", e);
		}
		
		setTimeout(3000 * nParticipants * nMessages);
		
		Behaviour b = new Behaviour() {
			private boolean finished = false;
			
			public void onStart() {
				// Kill all participants
				try {
					Enumeration e = participants.elements();
					while (e.hasMoreElements()) {
						AID id = (AID) e.nextElement();
						TestUtility.killAgent(a, id);
						log("Participant "+id.getLocalName()+" correctly killed.");
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					failed("Error killing participant agents");
				}
			}
				
			public void action() {
				// Wait for the replies
				ACLMessage msg = myAgent.receive(recvTemplate);
				if (msg != null) {
					String key = msg.getSender().getLocalName()+"#"+msg.getContent();
					if (exp.received(key)) {
						log("Received message # "+msg.getContent()+" from participant "+msg.getSender().getLocalName());
						if (exp.isCompleted()) {
							passed("All expected messages received");
							finished = true;
						}
					}
					else {
						failed("Unexpected message received from agent "+msg.getSender().getLocalName()+". Content is "+msg.getContent());
						finished = true;
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
  	return b;
  }
}

