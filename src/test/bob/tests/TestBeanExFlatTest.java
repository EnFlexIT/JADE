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

package test.bob.tests;

import jade.content.Concept;
import jade.content.onto.Ontology;
import jade.content.schema.ObjectSchema;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import test.bob.beans.TestBeanEx;
import test.bob.beans.TestSubBean;

public class TestBeanExFlatTest extends AbstractCheckSendAndReceiveTest {
	private static final long serialVersionUID = 1L;

	private static final String S1_VALUE = "901";
	private static final String S2_VALUE = "902";
	private static final String S3_VALUE = "903";
	private static final String S4_VALUE = "904";

	private static final String SF_VALUE = "sf";
	private static final int I_VALUE = 1;
	private static final long L_VALUE = 2L;
	private static final float F_VALUE = 3.0F;
	private static final double D_VALUE = 4.0D;
	private static final Integer CI_VALUE = Integer.valueOf(5);
	private static final Long CL_VALUE = Long.valueOf(6L);
	private static final Float CF_VALUE = new Float(7.0F);
	private static final Double CD_VALUE = new Double(8.0D);
	private static final Double SB_DF_VALUE = new Double(11.0D);
	private static final Double SB2_DF_VALUE = new Double(22.0D);

	private TestSubBean sb_value = null;
	private jade.util.leap.List sb_list_value = null;
	private List<String> sbl_model = null; 
	private TestSubBean sb2_value = null;
	private List<String> sbl2_model = null; 
	private jade.util.leap.List sb2_list_value = null;

	public TestBeanExFlatTest() {
		sbl_model = new LinkedList<String>();
		sbl_model.add("sbl_1");
		sbl_model.add("sbl_2");
		sbl_model.add("sbl_3");

		sb_list_value = new jade.util.leap.ArrayList();
		for (String s: sbl_model) {
			sb_list_value.add(s);
		}
		sb_value = new TestSubBean();
		sb_value.setCapitalDoubleField(SB_DF_VALUE);
		sb_value.setListField(sb_list_value);

		sbl2_model = new LinkedList<String>();
		sbl2_model.add("sbl2_a");
		sbl2_model.add("sbl2_b");
		sbl2_model.add("sbl2_c");
		sbl2_model.add("sbl2_d");

		sb2_list_value = new jade.util.leap.LinkedList();
		for (String s: sbl2_model) {
			sb2_list_value.add(s);
		}
		sb2_value = new TestSubBean();
		sb2_value.setCapitalDoubleField(SB2_DF_VALUE);
		sb2_value.setListField(sb2_list_value);
	}

	@Override
	protected Concept getConcept() {
		TestBeanEx tbe = new TestBeanEx();
		tbe.setStringOne(S1_VALUE);
		tbe.setStringTwo(S2_VALUE);
		tbe.setStringThree(S3_VALUE);
		tbe.setStringFour(S4_VALUE);
		tbe.setStringField(SF_VALUE);
		tbe.setIntField(I_VALUE);
		tbe.setLongField(L_VALUE);
		tbe.setFloatField(F_VALUE);
		tbe.setDoubleField(D_VALUE);
		tbe.setCapitalIntegerField(CI_VALUE);
		tbe.setCapitalLongField(CL_VALUE);
		tbe.setCapitalFloatField(CF_VALUE);
		tbe.setCapitalDoubleField(CD_VALUE);
		tbe.setTestSubBeanField(sb_value);
		tbe.setTestSubBeanTwoField(sb2_value);
		return tbe;
	}

	@Override
	protected Ontology getOntology() {
		return testerAgent.getFlatOntology();
	}

//	private static boolean stringsAreEqual(String s1, String s2) {
//		if (s1 == null) {
//			return s2 == null;
//		} else {
//			return s1.equals(s2);
//		}
//	}
//
//	private String iterToString(java.util.Iterator<String> iterator) {
//		StringBuilder sb = new StringBuilder();
//		while (iterator.hasNext()) {
//			sb.append('"');
//			sb.append((String)iterator.next());
//			sb.append("\", ");
//		}
//		if (sb.length() > 2) {
//			sb.setLength(sb.length()-2);
//		}
//		return sb.toString();
//	}
//
//	private boolean isListCorrectlyFilled(List<String> expected, jade.util.leap.List effective) {
//		boolean result = true;
//		Iterator iter = effective.iterator();
//		int i = 0;
//		String s2;
//		for (String s: expected) {
//			if (!iter.hasNext()) {
//				log("unexpected end of effective list");
//				result = false;
//				break;
//			}
//			s2 = (String)iter.next();
//			if (!stringsAreEqual(s, s2)) {
//				log("wrong value for element #"+i+": expected \""+s+"\", found \""+s2+"+\"");
//				result = false;
//				break;
//			}
//			i++;
//		}
//		if (iter.hasNext()) {
//			log("effective list is longer than expected");
//			result = false;
//		}
//		if (!result) {
//			log("expected list: "+iterToString(expected.iterator()));
//			log("effective list: "+iterToString(effective.iterator()));
//		}
//		return result;
//	}

	@Override
	protected boolean isConceptCorrectlyFilled(Concept c) {
		TestBeanEx tbe = (TestBeanEx)c;
		boolean result =
			S1_VALUE.equals(tbe.getStringOne()) &&
			S2_VALUE.equals(tbe.getStringTwo()) &&
			S3_VALUE.equals(tbe.getStringThree()) &&
			S4_VALUE.equals(tbe.getStringFour()) &&
			SF_VALUE.equals(tbe.getStringField()) &&
			I_VALUE == tbe.getIntField() &&
			L_VALUE == tbe.getLongField() &&
			F_VALUE == tbe.getFloatField() &&
			D_VALUE == tbe.getDoubleField() &&
			CI_VALUE.equals(tbe.getCapitalIntegerField()) &&
			CL_VALUE.equals(tbe.getCapitalLongField()) &&
			CF_VALUE.equals(tbe.getCapitalFloatField()) &&
			CD_VALUE.equals(tbe.getCapitalDoubleField()) &&
			sb_value.equals(tbe.getTestSubBeanField()) &&
			sb2_value.equals(tbe.getTestSubBeanTwoField());
		return result;
	}

	@Override
	protected boolean isSchemaCorrect(ObjectSchema os) {
		boolean result = true;

		String expectedSchemaName = TestBeanEx.class.getSimpleName();
		String realSchemaName = os.getTypeName(); 
		if (!expectedSchemaName.equals(realSchemaName)) {
			result = false; 
			log("wrong schema name \""+realSchemaName+"\" (expected \""+expectedSchemaName+"\")");
		}
		Set<String> slotNames = getSlotNameSet(os);
		String[] expectedSlotNames = new String[] {"slotForStringOne", "stringTwo", "stringThree", "stringFour", "stringField", "intField", "longField", "floatField", "doubleField", "capitalIntegerField", "capitalLongField", "capitalFloatField", "capitalDoubleField", "testSubBeanField", "testSubBeanTwoField"};
		result &= compareFieldSets(expectedSlotNames, slotNames);
		return result;
	}
}
