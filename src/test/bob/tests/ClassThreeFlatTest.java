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

import java.util.Set;

import test.bob.beans.ClassThree;
import test.bob.beans.ClassZero.EnumNumber;

public class ClassThreeFlatTest extends AbstractCheckSendAndReceiveTest {
	private static final long serialVersionUID = 1L;

	private static final int F00_VALUE = 900;
	private static final int F01_VALUE = 901;
	private static final EnumNumber F0E_VALUE = EnumNumber.One; 
	private static final String F10_VALUE = "10";
	private static final String F11_VALUE = "11";
	private static final String F12_VALUE = "12";
	private static final long F20_VALUE = 20L;
	private static final long F21_VALUE = 21L;
	private static final long F22_VALUE = 22L;
	private static final long F23_VALUE = 23L;
	private static final double F30_VALUE = 30.0D;
	private static final double F31_VALUE = 31.0D;
	private static final double F32_VALUE = 32.0D;
	private static final double F33_VALUE = 33.0D;
	private static final double F34_VALUE = 34.0D;

	@Override
	protected Concept getConcept() {
		ClassThree cz = new ClassThree();
		cz.setFieldZeroZero(F00_VALUE);
		cz.setFieldZeroOne(F01_VALUE);
		cz.setFieldZeroEnum(F0E_VALUE);
		cz.setFieldOneZero(F10_VALUE);
		cz.setFieldOneOne(F11_VALUE);
		cz.setFieldOneTwo(F12_VALUE);
		cz.setFieldTwoZero(F20_VALUE);
		cz.setFieldTwoOne(F21_VALUE);
		cz.setFieldTwoTwo(F22_VALUE);
		cz.setFieldTwoThree(F23_VALUE);
		cz.setFieldThreeZero(F30_VALUE);
		cz.setFieldThreeOne(F31_VALUE);
		cz.setFieldThreeTwo(F32_VALUE);
		cz.setFieldThreeThree(F33_VALUE);
		cz.setFieldThreeFour(F34_VALUE);
		return cz;
	}

	@Override
	protected boolean isConceptCorrectlyFilled(Concept c) {
		ClassThree cz = (ClassThree)c;
		boolean result =
			cz.getFieldZeroZero() == F00_VALUE &&
			cz.getFieldZeroOne() == F01_VALUE &&
			cz.getFieldZeroEnum() == F0E_VALUE &&
			F10_VALUE.equals(cz.getFieldOneZero()) &&
			F11_VALUE.equals(cz.getFieldOneOne()) &&
			F12_VALUE.equals(cz.getFieldOneTwo()) &&
			cz.getFieldTwoOne() == F21_VALUE &&
			cz.getFieldTwoTwo() == F22_VALUE &&
			cz.getFieldTwoThree() == F23_VALUE &&
			cz.getFieldThreeZero() == F30_VALUE &&
			cz.getFieldThreeOne() == F31_VALUE &&
			cz.getFieldThreeTwo() == F32_VALUE &&
			cz.getFieldThreeThree() == F33_VALUE &&
			cz.getFieldThreeFour() == F34_VALUE;
		return result;
	}

	@Override
	protected Ontology getOntology() {
		return testerAgent.getFlatOntology();
	}

	@Override
	protected boolean isSchemaCorrect(ObjectSchema os) {
		boolean result = true;

		String expectedSchemaName = ClassThree.class.getSimpleName();
		String realSchemaName = os.getTypeName(); 
		if (!expectedSchemaName.equals(realSchemaName)) {
			result = false; 
			log("wrong schema name \""+realSchemaName+"\" (expected \""+expectedSchemaName+"\")");
		}
		Set<String> slotNames = getSlotNameSet(os);
		String[] expectedSlotNames = new String[] {"fieldZeroZero", "fieldZeroOne", "fieldZeroEnum", "fieldOneZero", "fieldOneOne", "fieldOneTwo", "fieldTwoZero", "fieldTwoOne", "fieldTwoTwo", "fieldTwoThree", "fieldThreeZero", "fieldThreeOne", "fieldThreeTwo", "fieldThreeThree", "fieldThreeFour", "listOfStrings", "arrayOfStrings"};
		for (String expectedSlotName: expectedSlotNames) {
			if (!slotNames.remove(expectedSlotName)) {
				log("missing field "+expectedSlotName);
				result = false;
			}
		}
		if (slotNames.size() > 0) {
			result = false;
			for (String name: slotNames) {
				log("unexpected field "+name);
			}
		}
		return result;
	}
}
