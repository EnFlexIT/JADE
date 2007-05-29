package test.content.tests;

import test.common.*;
import test.content.ContentTesterAgent;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import test.content.testOntology.*;

import java.util.*;

public class TestCFReflectiveIntrospector extends Test {
	
	public Behaviour load(Agent a) {
		
		a.getContentManager().registerOntology(LEAPOntology.getInstance());
		
		return new SimpleBehaviour(a) {
			private boolean finished = false;
			private MessageTemplate template;
			
			public void onStart() {
				// Prepare the request
				ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
				msg.setOntology(LEAPOntology.getInstance().getName());
				
				CFThing t = new CFThing();
				
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
					log("--- Message content correctly filled");
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
						CFThing t = (CFThing) p.getObject();
						if (!(t.getList() instanceof Set)) {
							failed("Wrong aggregate type: found "+t.getList().getClass().getName()+" while an instance of java.util.Set was expected");
							return;
						}
						if (t.getList().size() != 2) {
							failed("Wrong CFThing list length: found "+t.getList().size()+" while 2 was expected");
							return;
						}
						List l = (List) t.getObject();
						if (l.size() != 1) {
							failed("Wrong CFThing object-list length: found "+l.size()+" while 1 was expected");
							return;
						}
						Route r = (Route) l.get(0);
						Iterator it = r.getAllElements();
						l.clear();
						while (it.hasNext()) {
							l.add(it.next());
						}
						if (l.size() != 2) {
							failed("Wrong Route position-list length: found "+l.size()+" while 2 was expected");
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
