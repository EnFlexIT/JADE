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

package examples.content;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.acl.*;
import jade.content.lang.*;
import jade.content.lang.leap.*;

import examples.content.musicOntology.*;
import examples.content.ecommerceOntology.*;

public class CDOwner extends Agent {
    // We handle contents
    private ContentManager manager  = (ContentManager)getContentManager();
    // This agent speaks a language called "LEAP"
    private Codec          codec    = new LEAPCodec();
    // This agent complies with the People ontology
    private Ontology   ontology = MusicOntology.getInstance();

    protected void setup() {
			manager.registerLanguage(codec);
			manager.registerOntology(ontology);
	
			addBehaviour(new InformManager(this));      
			addBehaviour(new QueryManager(this));      
			addBehaviour(new RequestManager(this)); 
			
			CD myCd = new CD();
			myCd.setTitle("Synchronicity");
			List tracks = new ArrayList();
			Track t1 = new Track();
			t1.setName("Every breath you take");
			tracks.add(t1);
			Track t2 = new Track();
			t2.setName("King-of-pain");
			t2.setDuration(new Integer(240000));
			tracks.add(t2);
			
			/*CD cd = new CD();
			cd.setTitle("Pippo");
			cd.setTracks(new ArrayList());
			tracks.add(cd);
			*/
			myCd.setTracks(tracks);
					
			addBehaviour(new ItemInformSender(this, myCd));      
    }
    
    // SELLER informs BUYER that he owns a given Item
    class ItemInformSender extends OneShotBehaviour {
			private Item it;
			
			public ItemInformSender(Agent a, Item it) { 
				super(a); 
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println(getLocalName()+": Send INFORM");

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					Owns owns = new Owns();
					owns.setOwner(getAID());
					owns.setItem(it);
					
					manager.fillContent(msg, owns);
					send(msg);
	    	} 
	    	catch(Exception e) { 
	    		e.printStackTrace(); 
	    	}

			}
    }
     
    // BUYER handles informations received from the SELLER
    class InformManager extends CyclicBehaviour {
    	
			public InformManager(Agent a) { 
				super(a); 
			}
	
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				if (msg != null) {
					System.out.println(getLocalName()+": INFORM received");
	    		try {
						ContentElement ce = manager.extractContent(msg);
						if (ce instanceof Owns) {
							Owns owns = (Owns) ce;
							AID owner = owns.getOwner();
							System.out.println("Owner is: "+owner);
							Item it = owns.getItem();
							System.out.println("Item is:");
							System.out.println(it);
							
	    				addBehaviour(new QuerySender(myAgent, it));
	    			}
	    			else if (ce instanceof Costs) {
	    				Costs c = (Costs) ce;
	    				Item it = c.getItem();
	    				Price p = c.getPrice();
	    				System.out.println("Item ");
	    				System.out.println(it);
	    				System.out.println("costs "+p.getValue());
	    				
							addBehaviour(new RequestSender(myAgent, it));
	    			}
	    			else {
	    				System.out.println("Unknown predicate "+ce.getClass().getName());
	    			}
	    		}
	    		catch (UngroundedException ue) {
	    			try {
							AbsContentElement ce = manager.extractAbsContent(msg);
							if (ce.getTypeName().equals(BasicOntology.EQUALS)) {
								AbsConcept price = (AbsConcept) ce.getAbsObject(BasicOntology.EQUALS_RIGHT);
								System.out.println("Price is "+price.getInteger(ECommerceOntology.PRICE_VALUE));
								
								AbsIRE iota = (AbsIRE) ce.getAbsObject(BasicOntology.EQUALS_LEFT);
								AbsProposition costs = iota.getProposition();
								AbsConcept i = (AbsConcept) costs.getAbsObject(ECommerceOntology.COSTS_ITEM);
								Item item = (Item) MusicOntology.getInstance().toObject(i);
								addBehaviour(new RequestSender(myAgent, item));
							}
							else {
								System.out.println("Unknown predicate "+ce.getTypeName());
							}
	    			}
	    			catch (Exception e) {
	    				e.printStackTrace();
	    			}
	    		}	
	    		catch(Exception e) { 
	    			e.printStackTrace(); 
	    		}
	    	}
	    	else {
	    		block();
	    	}
			}
			
    }
    
    // BUYER queries the SELLER how much a given item costs 
    class QuerySender extends OneShotBehaviour {
			Item it;
			
			public QuerySender(Agent a, Item it) { 
				super(a);
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println(getLocalName()+": Send QUERY_REF");

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					Ontology onto = MusicOntology.getInstance();
					AbsVariable x = new AbsVariable("x", ECommerceOntology.PRICE);
					
					AbsPredicate costs = new AbsPredicate(ECommerceOntology.COSTS);
					costs.set(ECommerceOntology.COSTS_ITEM, (AbsTerm) onto.fromObject(it));
					costs.set(ECommerceOntology.COSTS_PRICE, x);
					
					AbsIRE iota = new AbsIRE(LEAPCodec.IOTA);
					iota.setVariable(x);
					iota.setProposition(costs);
					
					manager.fillContent(msg, iota);
					send(msg);
					
	    	} 
	    	catch(Exception e) { 
	    		e.printStackTrace(); 
	    	}

			}
    }
     
    // SELLER handles queries received from BUYER
    class QueryManager extends CyclicBehaviour {
    	
			public QueryManager(Agent a) { 
				super(a); 
			}
	
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF));
				if (msg != null) {
	    		try {
						System.out.println(getLocalName()+": QUERY_REF received");
						// The content of a QUERY_REF is definitely an abstract descriptor
						// representing an IRE
						AbsIRE ire = (AbsIRE) manager.extractAbsContent(msg);
						if (ire.getTypeName().equals(LEAPCodec.IOTA)) {
							AbsPredicate p = (AbsPredicate) ire.getProposition();
							if (p.getTypeName().equals(ECommerceOntology.COSTS) &&
								  p.getAbsTerm(ECommerceOntology.COSTS_PRICE) instanceof AbsVariable) { 
	    					AbsConcept absItem = (AbsConcept) p.getAbsTerm(ECommerceOntology.COSTS_ITEM);
	    					Item it = (Item) MusicOntology.getInstance().toObject(absItem);
	    					
								addBehaviour(new PriceInformSender(myAgent, it));
							}
							else {
								System.out.println("Can't answer to query!!");
							}
	    			}
	    			else {
	    				System.out.println("Unknown IRE type");
	    			}
	    		}
	    		catch(Exception e) { 
	    			e.printStackTrace(); 
	    		}
	    	}
	    	else {
	    		block();
	    	}
			}
			
    }
    
    // SELLER informs BUYER about the cost of a given Item
    class PriceInformSender extends OneShotBehaviour {
			private Item it;
			
			public PriceInformSender(Agent a, Item it) { 
				super(a); 
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println(getLocalName()+": Send INFORM");

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					/*Costs costs = new Costs();
					costs.setItem(it);
					costs.setPrice(new Price(new Integer(40000)));
					
					manager.fillContent(msg, costs);*/
					Ontology onto = MusicOntology.getInstance();
					AbsVariable x = new AbsVariable("x", ECommerceOntology.PRICE);
					
					AbsPredicate costs = new AbsPredicate(ECommerceOntology.COSTS);
					costs.set(ECommerceOntology.COSTS_ITEM, (AbsTerm) onto.fromObject(it));
					costs.set(ECommerceOntology.COSTS_PRICE, x);
					
					AbsIRE iota = new AbsIRE(LEAPCodec.IOTA);
					iota.setVariable(x);
					iota.setProposition(costs);
					
					AbsPredicate equals = new AbsPredicate(BasicOntology.EQUALS);
					equals.set(BasicOntology.EQUALS_LEFT, iota);
					AbsConcept price = new AbsConcept(ECommerceOntology.PRICE);
					price.set(ECommerceOntology.PRICE_VALUE, 40000);
					equals.set(BasicOntology.EQUALS_RIGHT, price);
					
					manager.fillContent(msg, equals);
					send(msg);
	    	} 
	    	catch(Exception e) { 
	    		e.printStackTrace(); 
	    	}

			}
    }
     
    // BUYER requests SELLER to sell a given Item
    class RequestSender extends OneShotBehaviour {
	
    	private Item item = null;
    	
			public RequestSender(Agent a, Item item) { 
				super(a);
				this.item = item;
			}
	
			public void action() {
	    	try {
					System.out.println(getLocalName()+": Send REQUEST");

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					Sell sell = new Sell();
					sell.setBuyer(getAID());
					sell.setItem(item);
					sell.setCardNumber("3475660018");
					
					manager.fillContent(msg, sell);
					send(msg);
					
	    	} 
	    	catch(Exception e) { 
	    		e.printStackTrace(); 
	    	}

			}
    }
    
    // SELLER handles requests from BUYER
    class RequestManager extends CyclicBehaviour {
    	
			public RequestManager(Agent a) { 
				super(a); 
			}
	
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				if (msg != null) {
	    		try {
						System.out.println(getLocalName()+": REQUEST received");
						ContentElement ce = manager.extractContent(msg);
						if (ce instanceof Sell) {
							Sell sell = (Sell) ce;
							System.out.println("Buyer is:");
							System.out.println(sell.getBuyer());
							System.out.println("Item is:");
							System.out.println(sell.getItem());
							System.out.println("Card number is:");
							System.out.println(sell.getCardNumber());
	    			}
	    			else {
	    				System.out.println("Unknown action");
	    			}
	    		}
	    		catch(Exception e) { 
	    			e.printStackTrace(); 
	    		}
	    	}
	    	else {
	    		block();
	    	}
			}
    }
        	
}
