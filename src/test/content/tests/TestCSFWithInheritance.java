package test.content.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jade.content.onto.ConceptSlotFunction;
import jade.content.onto.basic.Equals;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import test.common.Logger;
import test.common.Test;
import test.common.TestException;
import test.content.ContentTesterAgent;
import test.content.SuccessExpectedInitiator;
import test.content.testOntology.BCOntology;
import test.content.testOntology.BCThing;
import test.content.testOntology.LEAPPredicate;
import test.content.testOntology.Position;
import test.content.testOntology.Route;

public class TestCSFWithInheritance extends Test {
	private static final String EXPECTED_SL_CONTENT = "((= (list (BCTHING)) (sequence A B)))";
	public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
		final Logger l = Logger.getLogger();

		try {
			// Prepare the request. Use a clone of the message to avoid interfering with other tests
			ACLMessage tmp = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
			final ACLMessage msg = (ACLMessage) tmp.clone();
			msg.setOntology(BCOntology.getInstance().getName());
			a.getContentManager().registerOntology(BCOntology.getInstance());
			
			String codecClass = (String) getGroupArgument(ContentTesterAgent.CODEC_CLASS_NAME);
			final boolean usingSL = ContentTesterAgent.CODEC_CLASS_DEFAULT.equals(codecClass);
			return new SuccessExpectedInitiator(a, ds, resultKey) {
				protected ACLMessage prepareMessage() throws Exception {
					msg.setPerformative(ACLMessage.INFORM);
					jade.util.leap.List ls = new jade.util.leap.ArrayList();
					ls.add("A");
					ls.add("B");
					ConceptSlotFunction listCsf = BCOntology.getInstance().createConceptSlotFunction("list", new BCThing());
					Equals eq = new Equals(listCsf, ls);
					myAgent.getContentManager().fillContent(msg, eq);
					if (usingSL && (!EXPECTED_SL_CONTENT.equals(msg.getContent()))) {
						throw new Exception("--- Wrong encodec content: Found "+msg.getContent()+" expected "+EXPECTED_SL_CONTENT);
					}
					l.log("--- Content correctly encoded");
					l.log(msg.getContent());
					return msg;
				}

				protected boolean checkReply(ACLMessage reply) throws Exception {
					Equals eq = (Equals) myAgent.getContentManager().extractContent(reply);
					l.log("--- Content correctly decoded");
					ConceptSlotFunction listCsf = (ConceptSlotFunction) eq.getLeft();
					jade.util.leap.ArrayList ls = (jade.util.leap.ArrayList) eq.getRight();
					listCsf.fill(ls.toList());
					BCThing t = (BCThing) listCsf.getConcept();
					java.util.List javaLs = (java.util.List) t.getList();
					if (javaLs == null || javaLs.size() != 2) {
						l.log("Wrong value for attribute list of BCThing. Found "+javaLs+" while a java.util.List of 2 items was expected");
						return false;
					}
					return true;
				}
			};
		}
		catch (Exception e) {
			throw new TestException("Wrong group argument", e);
		}
	}
}
