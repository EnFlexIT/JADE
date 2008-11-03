package test.content.tests;

import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;
import jade.content.abs.AbsVariable;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.BasicOntology;
import jade.content.onto.ConceptSlotFunction;
import jade.content.onto.basic.Equals;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ISO8601;

import java.util.Date;

import test.common.Logger;
import test.common.Test;
import test.common.TestException;
import test.content.ContentTesterAgent;
import test.content.SuccessExpectedInitiator;
import examples.content.ecommerceOntology.CreditCard;
import examples.content.ecommerceOntology.ECommerceOntology;

public class TestIotaCSF extends Test {
	private static final String CC_TYPE = "VISA";
	private static final long CC_NUMBER = 987453457;
	private static final Date now = new Date();
	private static final String EXPECTED_SL_CONTENT = "((= ("+ECommerceOntology.CREDIT_CARD_EXPIRATION_DATE+" ("+ECommerceOntology.CREDIT_CARD+" :"+ECommerceOntology.CREDIT_CARD_TYPE+" "+CC_TYPE+" :"+ECommerceOntology.CREDIT_CARD_NUMBER+" "+CC_NUMBER+"L)) "+ISO8601.toString(now)+"))";
	public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
		final Logger l = Logger.getLogger();

		try {
			final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
			return new SuccessExpectedInitiator(a, ds, resultKey) {
				protected ACLMessage prepareMessage() throws Exception {
					msg.setPerformative(ACLMessage.QUERY_REF);
					ConceptSlotFunction expDate = ECommerceOntology.getInstance().createConceptSlotFunction(ECommerceOntology.CREDIT_CARD_EXPIRATION_DATE, new CreditCard(CC_TYPE, CC_NUMBER, null));
					AbsIRE iota = new AbsIRE(SLVocabulary.IOTA);
					AbsVariable x = new AbsVariable("x", null);
					iota.setVariable(x);
					Equals eq = new Equals(expDate, x);
					iota.setProposition((AbsPredicate) ECommerceOntology.getInstance().fromObject(eq));
					myAgent.getContentManager().fillContent(msg, iota);
					l.log("--- Content correctly encoded");
					l.log(msg.getContent());
					return msg;
				}

				protected boolean checkReply(ACLMessage reply) throws Exception {
					AbsIRE iota = (AbsIRE) myAgent.getContentManager().extractAbsContent(reply);
					l.log("--- Content correctly decoded");
					AbsPredicate absEquals = iota.getProposition();
					
					ConceptSlotFunction expDate = (ConceptSlotFunction) ECommerceOntology.getInstance().toObject(absEquals.getAbsTerm(SLVocabulary.EQUALS_LEFT));
					CreditCard cc = new CreditCard(CC_TYPE, CC_NUMBER, now);
					Date d = (Date) expDate.apply(cc);
					if (!now.equals(d)) {
						l.log("Wrong value for "+ECommerceOntology.CREDIT_CARD_EXPIRATION_DATE+" slot: found "+d+", expected "+now);
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