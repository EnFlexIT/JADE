package test.persDelivery.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.Envelope;

/**
   ForwarderAgent
 */
public class ForwarderAgent extends Agent {
	AID tester = null;
	AID receiver = null;
	
	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length > 1) {
			tester = new AID((String) args[0], AID.ISLOCALNAME);
			receiver = new AID((String) args[1], AID.ISLOCALNAME);
			
  		addBehaviour(new CyclicBehaviour(this) {
  			public void action() {
  				ACLMessage msg = myAgent.receive();
  				if (msg != null) {
            Envelope env = msg.getEnvelope();
            AID forwardee = (msg.getSender().equals(tester))?receiver:tester;
            if (env==null){
              msg.clearAllReceiver();
              msg.addReceiver(forwardee);
            } else {
              env.clearAllTo();
              env.addTo(forwardee);
            }
						//msg.setSender(myAgent.getAID());
						myAgent.send(msg);
  				}
  				else {
  					block();
  				}
  			}
  		} );
		}
		else {
			System.out.println("No tester/receiver name specified");
		}
	}
}
  
