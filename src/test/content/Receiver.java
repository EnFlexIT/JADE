// Example agent that receives two messages

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.lang.*;
import jade.content.lang.j.*;

public class Receiver extends Agent {
	private ContentManager manager  = getContentManager();
	private Codec          codec    = new JCodec();
	private Ontology       ontology = PeopleOntology.getInstance();

	class ReceiverBehaviour extends SimpleBehaviour {
		private boolean finished = false;

		public ReceiverBehaviour(Agent a) { super(a); }

		public boolean done() { return finished; }

		public void action() {
			try {
				System.out.println( "[" + getLocalName() + "] Waiting for a message...");

			    	ACLMessage msg = blockingReceive();
    	
				if (msg!= null){
					// Got first message
					// Extract its content using the content manager.
					AbsContentElement abs = manager.extractAbsContent(msg);
				
					System.out.println("[" + getLocalName() + "] Received: ");
					abs.dump();
				}

				System.out.println( "[" + getLocalName() + "] Waiting for a message...");

			    	msg = blockingReceive();
    	
				if (msg!= null){
					AbsContentElement ace = manager.extractAbsContent(msg);

					System.out.println("[" + getLocalName() + "] Received: ");
					ace.dump();
				}
			} catch(Exception e) { e.printStackTrace(); }

			finished = true;
		}
	}
     
	protected void setup() {
		manager.registerLanguage(codec);
		manager.registerOntology(ontology);

		addBehaviour(new ReceiverBehaviour(this));		
	}
}
