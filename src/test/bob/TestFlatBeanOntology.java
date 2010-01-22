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

package test.bob;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;

import java.util.List;

import test.bob.beans.ClassOne;
import test.bob.beans.ClassThree;
import test.bob.beans.ClassTwo;
import test.bob.beans.ExtendedAction;
import test.bob.beans.SimpleAction;
import test.bob.beans.SimplePredicate;
import test.bob.beans.TestBean;
import test.bob.beans.TestBeanEx;
import test.bob.beans.TestBeanOther;
import test.bob.beans.VeryComplexBean;

public class TestFlatBeanOntology extends BeanOntology {
	private static final long serialVersionUID = 1L;

	private static final String ONTOLOGY_NAME = "Flat Test Ontology";
	private static TestFlatBeanOntology INSTANCE;

	public final static TestFlatBeanOntology getInstance() throws BeanOntologyException {
		if (INSTANCE == null) {
			INSTANCE = new TestFlatBeanOntology();
		}
		return INSTANCE;
	}

	private TestFlatBeanOntology() throws BeanOntologyException {
        super(ONTOLOGY_NAME);

		add(SimpleAction.class, false);
		add(ExtendedAction.class, false);

		add(ClassThree.class, false);

		add(TestBean.class, false);
		add(TestBeanEx.class, false);
		add(TestBeanOther.class, false);

		add(ClassTwo.class, false);
		add(ClassOne.class, false);

		add(VeryComplexBean.class, false);

		add(SimplePredicate.class, false);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TestFlatBeanOntology {");
		List names = getActionNames();
		sb.append("actions=");
		sb.append(names);
		sb.append(' ');
		names = getConceptNames();
		sb.append("concepts=");
		sb.append(names);
		sb.append(' ');
		names = getPredicateNames();
		sb.append("predicates=");
		sb.append(names);
		sb.append('}');
		return sb.toString();
	}
}
