// Example agent sending two messages

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.lang.*;
import jade.content.lang.j.*;

public class Sender extends Agent {
	// We handle contents
	private ContentManager manager  = getContentManager();
	// This agent speaks a language called "J"
	private Codec          codec    = new JCodec();
	// This agent complies with the People ontology
	private Ontology       ontology = PeopleOntology.getInstance();

	class SenderBehaviour extends SimpleBehaviour {
		private boolean finished = false;

		public SenderBehaviour(Agent a) { super(a); }

		public boolean done() { return finished; }

		public void action() {
			try {
				// Preparing the first message
				System.out.println( "[" + getLocalName() + "] Creating the message...");

			    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				AID        receiver = new AID("receiver");
				
				msg.setSender(getAID());
				msg.addReceiver(receiver);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());

				// The message informs that:
				// fatherOf(man :name "John" :address "Ipswich", [man :name "Ronnie" :address "Paris"])

				// Create the concept for filling the attribute :address
				AbsConcept absAddress = new AbsConcept(PeopleOntology.ADDRESS);
				absAddress.setSlot(PeopleOntology.CITY, "London");

				// Create the concept "John"
				AbsConcept absJohn = new AbsConcept(PeopleOntology.MAN);
				absJohn.setSlot(PeopleOntology.NAME,    "John");
				absJohn.setSlot(PeopleOntology.ADDRESS, absAddress);

				// Create the concept for filling the attribute :address
				absAddress = new AbsConcept(PeopleOntology.ADDRESS);
				absAddress.setSlot(PeopleOntology.CITY, "Paris");

				// Create the concept "Ronnie"
				AbsConcept absRonnie = new AbsConcept(PeopleOntology.MAN);
				absRonnie.setSlot(PeopleOntology.NAME,    "Ronnie");
				absRonnie.setSlot(PeopleOntology.ADDRESS, absAddress);

				// Create the list of children
				AbsAggregate absChildren = new AbsAggregate(BasicOntology.SET);
				absChildren.addElement(absRonnie);

				// Create the predicate
				AbsPredicate absFatherOf = new AbsPredicate(PeopleOntology.FATHER_OF);
				absFatherOf.setArgument(PeopleOntology.FATHER,   absJohn);
				absFatherOf.setArgument(PeopleOntology.CHILDREN, absChildren);

				// Fill the message content
				manager.fillContent(msg, absFatherOf);

				// Send the message
				System.out.println( "[" + getLocalName() + "] Sending first message...");
				send(msg);

				// The message asks for any ?FATHER such that:
				// fatherOf(?FATHER, [man :name "Ronnie" :address "Paris"])
				AbsVariable absX = new AbsVariable("?FATHER", PeopleOntology.MAN);

				absFatherOf.setArgument(PeopleOntology.FATHER, absX);

				// Create the IRE
				AbsIRE absIRE = new AbsIRE();
				absIRE.setKind(ACLOntology.ANY);
				absIRE.setVariable(absX);
				absIRE.setProposition(absFatherOf);

				msg.setPerformative(ACLMessage.QUERY_REF);

				manager.fillContent(msg, absIRE);

				// Send the message
				System.out.println( "[" + getLocalName() + "] Sending second message...");
				send(msg);

				finished = true;
			} catch(Exception e) { e.printStackTrace(); }

			finished = true;
		}
	}
     
	protected void setup() {
		manager.registerLanguage(codec);
		manager.registerOntology(ontology);

		addBehaviour(new SenderBehaviour(this));		
	}
}
