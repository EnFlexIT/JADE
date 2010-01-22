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

import test.bob.beans.VeryComplexBean;

public class SuppressSlotAnnotationFlatOntoTest extends AbstractBaseTest {
	private static final long serialVersionUID = 1L;

	@Override
	protected Ontology getOntology() {
		return testerAgent.getFlatOntology();
	}

	@Override
	protected boolean isConceptCorrectlyFilled(Concept c) {
		VeryComplexBean vcb = (VeryComplexBean)c;
		String s = VeryComplexBean.getSuppressedStringDefaultValue();
		return s.equals(vcb.getSuppressedString());
	}

	@Override
	protected boolean isSchemaCorrect(ObjectSchema os) {
		Set<String> effectiveSlotNames = getSlotNameSet(os);
		return !effectiveSlotNames.contains("suppressedString");
	}
}
