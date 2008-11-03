package test.content.tests;

import jade.content.onto.ConceptSlotFunction;
import jade.content.onto.basic.Action;
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
import examples.content.ecommerceOntology.Item;
import examples.content.ecommerceOntology.Sell;

public class TestCSF extends Test {
	private static final String CC_TYPE = "VISA";
	private static final long CC_NUMBER = 987453457;
	private static final Date now = new Date();
	private static final String EXPECTED_SL_CONTENT = "((= ("+ECommerceOntology.CREDIT_CARD_EXPIRATION_DATE+" ("+ECommerceOntology.CREDIT_CARD+" :"+ECommerceOntology.CREDIT_CARD_TYPE+" "+CC_TYPE+" :"+ECommerceOntology.CREDIT_CARD_NUMBER+" "+CC_NUMBER+"L)) "+ISO8601.toString(now)+"))";
	public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
		final Logger l = Logger.getLogger();

		try {
			final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
			String codecClass = (String) getGroupArgument(ContentTesterAgent.CODEC_CLASS_NAME);
			final boolean usingSL = ContentTesterAgent.CODEC_CLASS_DEFAULT.equals(codecClass);
			return new SuccessExpectedInitiator(a, ds, resultKey) {
				protected ACLMessage prepareMessage() throws Exception {
					msg.setPerformative(ACLMessage.INFORM);
					ConceptSlotFunction expDate = ECommerceOntology.getInstance().createConceptSlotFunction(ECommerceOntology.CREDIT_CARD_EXPIRATION_DATE, new CreditCard(CC_TYPE, CC_NUMBER, null));
					Equals eq = new Equals(expDate, now);
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
					ConceptSlotFunction expDate = (ConceptSlotFunction) eq.getLeft();
					expDate.fill(eq.getRight());
					Date d = ((CreditCard) expDate.getConcept()).getExpirationDate();
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
