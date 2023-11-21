package test.leap.midp;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.util.Logger;

/**
   This agent just sends some messages in its takeDown()
   method.
   @author Giovanni Caire - TILAB
 */
public class TakeDownSenderAgent extends Agent {
	private int nMessages;
	private String receiver;
	
	protected void setup() {
		Object[] args = getArguments();
		try {
			nMessages = Integer.parseInt((String) args[0]);
			receiver = (String) args[1];
		}
		catch (Exception e) {
			System.out.println("Wrong arguments");
		}
	}
	
	protected void takeDown() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		for (int i = 0; i < nMessages; i++) {
			msg.setContent(String.valueOf(i));
			send(msg);
		}
	}
}

