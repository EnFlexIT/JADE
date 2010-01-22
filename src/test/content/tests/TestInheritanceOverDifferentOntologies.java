package test.content.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import test.common.Test;
import test.content.ContentTesterAgent;
import test.content.testOntology.BCOntology;
import test.content.testOntology.BCThing;
import test.content.testOntology.CFThing;
import test.content.testOntology.LEAPOntology;
import test.content.testOntology.LEAPPredicate;
import test.content.testOntology.Position;
import test.content.testOntology.Route;

public class TestInheritanceOverDifferentOntologies  extends Test {
	public Behaviour load(Agent a) {
		
		a.getContentManager().registerOntology(BCOntology.getInstance());
		
		return new SimpleBehaviour(a) {
			private boolean finished = false;
			private MessageTemplate template;
			
			public void onStart() {
				// Prepare the request. Use a clone of the message to avoid interfering with other tests
				ACLMessage tmp = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
				ACLMessage msg = (ACLMessage) tmp.clone();
				msg.setOntology(BCOntology.getInstance().getName());
				
				BCThing t = new BCThing();
				
				t.addLll("A");
				t.addLll("B");
				
				Set s = new HashSet();
				s.add("ONE");
				s.add("TWO");
				t.setList(s);

				Route r = new Route();
				r.addElements(new Position(1.0, 2.0));
				r.addElements(new Position(2.0, 3.0));
				
				List l = new ArrayList();
				l.add(r);
				t.setObject(l);
				
				LEAPPredicate p = new LEAPPredicate();
				jade.util.leap.List ll = new jade.util.leap.ArrayList();
				ll.add("THREE");
				ll.add("FOUR");
				p.setList(ll);
				p.setObject(t);
				
				try {
					log("--- Filling message content");
					myAgent.getContentManager().fillContent(msg, p);
					log("--- Message content correctly filled: "+msg.getContent());
					myAgent.send(msg);
					template = MessageTemplate.MatchConversationId(msg.getConversationId());
				}
				catch (Exception e) {
					e.printStackTrace();
					failed("--- Error filling message content. "+e);
				}
			}
			
			public void action() {
				ACLMessage reply = myAgent.receive(template);
				if (reply != null) {
					// Check the response
					try {
						log("--- Extracting message content");
						LEAPPredicate p = (LEAPPredicate) myAgent.getContentManager().extractContent(reply);
						log("--- Message content correctly extracted");
						if (p.getList().size() != 2) {
							failed("Wrong LEAPPredicate list length: found "+p.getList().size()+" while 2 was expected");
							return;
						}
						BCThing t = (BCThing) p.getObject();
						// Check the "lll": must be an iterator containing 2 items
						Iterator it = t.getAllLll();
						int k = 0;
						while (it.hasNext()) {
							it.next();
							k++;
						}
						if (k != 2) {
							failed("Wrong BCThing lll length: found "+k+" while 2 was expected");
							return;
						}
						
						// Check "list": must be a Set containing 2 items	
						if (!(t.getList() instanceof Set)) {
							failed("Wrong aggregate type: found "+t.getList().getClass().getName()+" while an instance of java.util.Set was expected");
							return;
						}
						if (t.getList().size() != 2) {
							failed("Wrong BCThing list length: found "+t.getList().size()+" while 2 was expected");
							return;
						}
						
						// Check Object: must be a List containing a Route with 2 positions
						List l = (List) t.getObject();
						if (l.size() != 1) {
							failed("Wrong BCThing object-list length: found "+l.size()+" while 1 was expected");
							return;
						}
						Route r = (Route) l.get(0);
						it  = r.getAllElements();
						k = 0;
						while (it.hasNext()) {
							it.next();
							k++;
						}
						if (k != 2) {
							failed("Wrong Route position-list length: found "+k+" while 2 was expected");
							return;
						}
						passed("The extracted content is consistent with the original one");
					}
					catch (Exception e) {
						e.printStackTrace();
						failed("--- Error extracting message content. "+e);
					}
					finished = true;
				}
				else {
					block();
				}
				
			}
			
			public boolean done() {
				return finished;
			}
		};
	}

}
