package test.content.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import test.common.Test;
import test.common.Logger;
import test.common.TestException;
import test.content.ContentTesterAgent;
import test.content.SuccessExpectedInitiator;
import test.content.testOntology.Exists;
import test.content.testOntology.UntypedConcept;

/**
 * Test that usage of long/int and double/float in un-typed (Object) slot is handled correctly 
 */
public class TestJavaTypesPreservation extends Test {
	public static final Long LONG_VALUE = new Long(10L);
	public static final Integer INTEGER_VALUE = new Integer(10);
	public static final Double DOUBLE_VALUE = new Double(10.0d);
	public static final Float FLOAT_VALUE = new Float(10.0f);
	public static final String STRING_VALUE = "a < \"5\"";

	public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
		final Logger l = Logger.getLogger();

		try {
			final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
			return new SuccessExpectedInitiator(a, ds, resultKey) {
				protected ACLMessage prepareMessage() throws Exception {
					UntypedConcept uc = new UntypedConcept();
					uc.setAttr1(LONG_VALUE);
					uc.setAttr2(INTEGER_VALUE);
					uc.setAttr3(DOUBLE_VALUE);
					uc.setAttr4(FLOAT_VALUE);
					uc.setAttr5(STRING_VALUE);
					uc.addListElements(LONG_VALUE);
					uc.addListElements(INTEGER_VALUE);
					uc.addListElements(DOUBLE_VALUE);
					uc.addListElements(FLOAT_VALUE);
					uc.addListElements(STRING_VALUE);
					
					Exists e = new Exists(uc);
					myAgent.getContentManager().fillContent(msg, e);
					l.log("Content correctly encoded");
					l.log(msg.getContent());
					return msg;
				}

				protected boolean checkReply(ACLMessage reply) throws Exception {
					Exists e = (Exists) myAgent.getContentManager().extractContent(reply);
					l.log("Content correctly decoded");
					UntypedConcept uc = (UntypedConcept) e.getWhat();
					boolean ret = checkLong(uc.getAttr1());
					ret = ret && checkInteger(uc.getAttr2());
					ret = ret && checkDouble(uc.getAttr3());
					ret = ret && checkFloat(uc.getAttr4());
					ret = ret && checkString(uc.getAttr5());
					if (ret) {
						l.log("Attribute values OK");
					}
					Iterator it = uc.getAllListElements();
					ret = ret && checkLong(it.next());
					ret = ret && checkInteger(it.next());
					ret = ret && checkDouble(it.next());
					ret = ret && checkFloat(it.next());
					ret = ret && checkString(it.next());
					if (ret) {
						l.log("List values OK");
					}
					// If at least one check failed the whole test will fail
					return ret;
				}
				
				private boolean checkLong(Object val) {
					if (val == null || !(val instanceof Long) || !val.equals(LONG_VALUE)) {
						
						l.log("--- Wrong Long value: expected "+LONG_VALUE+" (Long) found "+stringify(val));
						return false;
					}
					else {
						return true;
					}
				}
				private boolean checkInteger(Object val) {
					if (val == null || !(val instanceof Integer) || !val.equals(INTEGER_VALUE)) {
						l.log("--- Wrong Integer value: expected "+INTEGER_VALUE+" (Integer) found "+stringify(val));
						return false;
					}
					else {
						return true;
					}
				}
				private boolean checkDouble(Object val) {
					if (val == null || !(val instanceof Double) || !val.equals(DOUBLE_VALUE)) {
						l.log("--- Wrong Double value: expected "+DOUBLE_VALUE+" (Double) found "+stringify(val));
						return false;
					}
					else {
						return true;
					}
				}
				private boolean checkFloat(Object val) {
					if (val == null || !(val instanceof Float) || !val.equals(FLOAT_VALUE)) {
						l.log("--- Wrong Float value: expected "+FLOAT_VALUE+" (Float) found "+stringify(val));
						return false;
					}
					else {
						return true;
					}
				}
				private boolean checkString(Object val) {
					if (val == null || !(val instanceof String) || !val.equals(STRING_VALUE)) {
						l.log("--- Wrong String value: expected "+STRING_VALUE+" (String) found "+stringify(val));
						return false;
					}
					else {
						return true;
					}
				}
				
				private String stringify(Object obj) {
					if (obj != null) {
						return obj.toString() + " (" + obj.getClass().getSimpleName() +")";
					}
					else {
						return "null";
					}
				}
			};
		}
		catch (Exception e) {
			throw new TestException("Wrong group argument", e);
		}
	}

}
