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
import jade.content.lang.*;
import jade.content.lang.leap.*;

import examples.content.musicShopOntology.*;
import examples.content.ecommerceOntology.*;

import java.util.Date;

public class CDTrader extends Agent {
    private ContentManager manager  = (ContentManager)getContentManager();
    // This agent by default speaks a language called "LEAP"
    private Codec          codec    = new LEAPCodec();
    // This agent complies with the MusicShop ontology
    private Ontology   ontology = MusicShopOntology.getInstance();

    protected void setup() {
    	// Get the codec for the language to speack (use LEAP codec by default)
    	Object[] args = getArguments();
    	if (args != null && args.length > 0) {
    		String codecClassName = (String) args[0];
    		try {
    			codec = (Codec) Class.forName(codecClassName).newInstance();
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	System.out.println("Conversation based on the "+codec.getName()+" language\n");
			manager.registerLanguage(codec);
			manager.registerOntology(ontology);
	
			// BUYER PART
			addBehaviour(new HandleInformBehaviour(this));
			
			// SELLER PART
			addBehaviour(new HandleQueryBehaviour(this));      
			addBehaviour(new HandleRequestBehaviour(this)); 
			
			CD myCd = new CD();
			myCd.setSerialID(11111);
			myCd.setTitle("Synchronicity");
			List tracks = new ArrayList();
			Track t = new Track();
			t.setName("Synchronicity");
			tracks.add(t);
			t = new Track();
			t.setName("Every breath you take");
			tracks.add(t);
			t = new Track();
			t.setName("King of pain");
			t.setDuration(new Integer(240));
			tracks.add(t);
			
			myCd.setTracks(tracks);
					
			addBehaviour(new InformOwnsBehaviour(this, myCd));      
    }
    
    protected void takeDown() {
    	System.out.println(getName()+" exiting ...");
    }
    
    // SELLER informs BUYER that he owns a given Item
    class InformOwnsBehaviour extends OneShotBehaviour {
			private Item it;
			
			public InformOwnsBehaviour(Agent a, Item it) { 
				super(a); 
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println("\nSELLER: Inform BUYER that I own "+it);

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
    class HandleInformBehaviour extends CyclicBehaviour {
    	
			public HandleInformBehaviour(Agent a) { 
				super(a); 
			}
	
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				if (msg != null) {
					System.out.println("\nBUYER: Information received from SELLER. Message is");
					System.out.println(msg);
	    		try {
						ContentElement ce = manager.extractContent(msg);
						if (ce instanceof Owns) {
							Owns owns = (Owns) ce;
							AID owner = owns.getOwner();
							System.out.println("Owner is: "+owner);
							Item it = owns.getItem();
							System.out.println("Item is: "+it);
							
	    				addBehaviour(new QueryPriceBehaviour(myAgent, it));
	    			}
	    			else if (ce instanceof Costs) {
	    				Costs c = (Costs) ce;
	    				Item it = c.getItem();
	    				Price p = c.getPrice();
	    				System.out.println("Item ");
	    				System.out.println(it);
	    				System.out.println("costs "+p);
	    				
							addBehaviour(new RequestSellBehaviour(myAgent, it));
	    			}
	    			else if (ce instanceof Done) {
	    				Done d = (Done) ce;
	    				AgentAction aa = (AgentAction) d.getAction();
	    				Sell s = null;
	    				if (aa instanceof Sell) {
	    					s = (Sell) aa;
	    				}
	    				else {
	    					Action a = (Action) aa;
	    					s = (Sell) a.getAction();
	    				}
							System.out.println("OK! Now I own Item "+s.getItem());
							myAgent.doDelete();
	    			}
	    			else {
	    				System.out.println("Unknown predicate "+ce.getClass().getName());
	    			}
	    		}
	    		catch (UngroundedException ue) {
	    			try {
							AbsContentElement ce = manager.extractAbsContent(msg);
							if (ce.getTypeName().equals(BasicOntology.EQUALS)) {
								AbsConcept p = (AbsConcept) ce.getAbsObject(BasicOntology.EQUALS_RIGHT);
								Price price = (Price) MusicShopOntology.getInstance().toObject(p);
								System.out.println("Price is "+price);
								
								AbsIRE iota = (AbsIRE) ce.getAbsObject(BasicOntology.EQUALS_LEFT);
								AbsPredicate costs = iota.getProposition();
								AbsConcept i = (AbsConcept) costs.getAbsObject(ECommerceOntology.COSTS_ITEM);
								Item item = (Item) MusicShopOntology.getInstance().toObject(i);
								addBehaviour(new RequestSellBehaviour(myAgent, item));
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
    class QueryPriceBehaviour extends OneShotBehaviour {
			Item it;
			
			public QueryPriceBehaviour(Agent a, Item it) { 
				super(a);
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println("\nBUYER: Query price of "+it);

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					Ontology onto = MusicShopOntology.getInstance();
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
    class HandleQueryBehaviour extends CyclicBehaviour {
    	
			public HandleQueryBehaviour(Agent a) { 
				super(a); 
			}
	
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF));
				if (msg != null) {
	    		try {
						System.out.println("\nSELLER: Received query from BUYER. Message is");
						System.out.println(msg);
						// The content of a QUERY_REF is certainly an abstract descriptor
						// representing an IRE
						AbsIRE ire = (AbsIRE) manager.extractAbsContent(msg);
						if (ire.getTypeName().equals(LEAPCodec.IOTA)) {
							AbsPredicate p = (AbsPredicate) ire.getProposition();
							if (p.getTypeName().equals(ECommerceOntology.COSTS) &&
								  p.getAbsTerm(ECommerceOntology.COSTS_PRICE) instanceof AbsVariable) { 
	    					AbsConcept absItem = (AbsConcept) p.getAbsTerm(ECommerceOntology.COSTS_ITEM);
	    					Item it = (Item) MusicShopOntology.getInstance().toObject(absItem);
	    					
								addBehaviour(new InformCostsBehaviour(myAgent, it));
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
    class InformCostsBehaviour extends OneShotBehaviour {
			private Item it;
			
			public InformCostsBehaviour(Agent a, Item it) { 
				super(a); 
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println("\nSELLER: Inform Buyer about price of item "+it);

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					Ontology onto = MusicShopOntology.getInstance();
					AbsVariable x = new AbsVariable("x", ECommerceOntology.PRICE);
					
					AbsPredicate costs = new AbsPredicate(ECommerceOntology.COSTS);
					costs.set(ECommerceOntology.COSTS_ITEM, (AbsTerm) onto.fromObject(it));
					costs.set(ECommerceOntology.COSTS_PRICE, x);
					
					AbsIRE iota = new AbsIRE(LEAPCodec.IOTA);
					iota.setVariable(x);
					iota.setProposition(costs);
					
					AbsPredicate equals = new AbsPredicate(BasicOntology.EQUALS);
					equals.set(BasicOntology.EQUALS_LEFT, iota);
					AbsConcept price = (AbsConcept) onto.fromObject(new Price(20.5F, "EURO"));
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
    class RequestSellBehaviour extends OneShotBehaviour {
	
    	private Item it = null;
    	
			public RequestSellBehaviour(Agent a, Item it) { 
				super(a);
				this.it = it;
			}
	
			public void action() {
	    	try {
					System.out.println("\nBUYER: Request seller to sell item "+it);

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
					sell.setItem(it);
					sell.setCreditCard(new CreditCard("VISA", 3378892003L, new Date()));
					
					// Some CLs (e.g. SL) require actions to be included into the
					// ACTION construct --> Enable the following 2 lines when using such a CL 
					Action a = new Action(getAID(), sell);
					manager.fillContent(msg, a);
					// Other CLs (e.g. LEAP) allows putting actions directly in the 
					// content --> Enable the following line when using such a CL
					//manager.fillContent(msg, sell);
					
					send(msg);
	    	} 
	    	catch(Exception e) { 
	    		e.printStackTrace(); 
	    	}

			}
    }
    
    // SELLER handles requests from BUYER
    class HandleRequestBehaviour extends CyclicBehaviour {
    	
			public HandleRequestBehaviour(Agent a) { 
				super(a); 
			}
	
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				if (msg != null) {
	    		try {
						System.out.println("\nSELLER: Received request from BUYER. Message is");
						System.out.println(msg);
						ContentElement ce = manager.extractContent(msg);
						Sell sell = null;
						AgentAction toNotify = null;
						if (ce instanceof Sell) {
							sell = (Sell) ce;
							toNotify = sell;
						}
						else if (ce instanceof Action) {
							Action a = (Action) ce;
							sell = (Sell) a.getAction();
							toNotify = a;
						}
	    			else {
	    				System.out.println("Unknown action");
	    				return;
	    			}
	    			
						System.out.println("Buyer is: "+sell.getBuyer());
						System.out.println("Item is: "+sell.getItem());
						System.out.println("Credit Card is: "+sell.getCreditCard());
							
						addBehaviour(new InformDoneBehaviour(myAgent, toNotify));
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
        	
    // SELLER informs BUYER that a given action has been completed
    class InformDoneBehaviour extends OneShotBehaviour {
			private AgentAction act;
			
			public InformDoneBehaviour(Agent a, AgentAction act) { 
				super(a); 
				this.act = act;
			}
	
			public void action() {
	    	try {
					System.out.println("\nSELLER: Inform Buyer that the requested operation has been completed");

					// Prepare the message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					AID receiver = getAID(); // Send the message to myself
			       				
					msg.setSender(getAID());
					msg.addReceiver(receiver);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());

					// Fill the content
					Done d = new Done(act);
					manager.fillContent(msg, d);
					send(msg);
	    	} 
	    	catch(Exception e) { 
	    		e.printStackTrace(); 
	    	}

			}
    }
     
}
