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

import java.util.LinkedList;

import test.bob.beans.ClassOne;
import test.bob.beans.ClassZero;
import test.bob.beans.VeryComplexBean;
import test.bob.beans.ClassZero.EnumNumber;

public abstract class AbstractBaseTest extends AbstractCheckSendAndReceiveTest {
	private static final long serialVersionUID = 1L;

	protected static final String NORMALSLOT_VALUE = "aValue";
	protected static final String SUPPRESSEDSTRING_VALUE = "aSuppressedString";
	protected java.util.List<ClassZero> javaList_model;
	protected java.util.List<ClassZero> jadeList_model;
	protected java.util.Set<ClassOne> javaSet_model;
	protected java.util.Set<ClassZero> jadeSet_model;
	protected jade.util.leap.List jadeList_value;
	protected java.util.List<ClassZero> javaList_value;
	protected jade.util.leap.Set jadeSet_value;
	protected java.util.Set<ClassOne> javaSet_value;

	private int counter;

	private ClassZero generateClassZero() {
		ClassZero cz = new ClassZero();
		cz.setFieldZeroZero(counter++);
		cz.setFieldZeroOne(counter++);
		EnumNumber[] values = EnumNumber.values();
		EnumNumber fieldZeroEnum = values[counter++ % values.length];
		cz.setFieldZeroEnum(fieldZeroEnum);
		return cz;
	}

	private ClassOne generateClassOne() {
		ClassOne co = new ClassOne();
		co.setFieldZeroZero(counter++);
		co.setFieldZeroOne(counter++);
		co.setFieldOneZero("s10-"+counter++);
		co.setFieldOneOne("s11-"+counter++);
		co.setFieldOneTwo("s12-"+counter++);
		return co;
	}

	@Override
	protected Concept getConcept() {
		counter = 0;
		javaList_model = new LinkedList<ClassZero>();
		javaList_model.add(generateClassZero());
		javaList_model.add(generateClassZero());

		jadeList_model = new LinkedList<ClassZero>();
		jadeList_model.add(generateClassZero());
		jadeList_model.add(generateClassZero());
		jadeList_model.add(generateClassZero());

		javaSet_model = new java.util.HashSet<ClassOne>();
		javaSet_model.add(generateClassOne());
		javaSet_model.add(generateClassOne());
		javaSet_model.add(generateClassOne());
		javaSet_model.add(generateClassOne());

		jadeSet_model = new java.util.HashSet<ClassZero>();
		jadeSet_model.add(generateClassZero());

		VeryComplexBean vcb = new VeryComplexBean();

		jadeList_value = new jade.util.leap.ArrayList();
		for (ClassZero cz: jadeList_model) {
			jadeList_value.add(cz);
		}

		javaList_value = new java.util.ArrayList<ClassZero>();
		for (ClassZero cz: javaList_model) {
			javaList_value.add(cz);
		}

		jadeSet_value = new jade.util.leap.HashSet();
		for (ClassZero cz: jadeSet_model) {
			jadeSet_value.add(cz);
		}

		javaSet_value = new java.util.HashSet<ClassOne>();
		for (ClassOne co: javaSet_model) {
			javaSet_value.add(co);
		}

		vcb.setAJadeList(jadeList_value);
		vcb.setAJavaList(javaList_value);
		vcb.setAJadeSet(jadeSet_value);
		vcb.setAJavaSet(javaSet_value);
		vcb.setNormalSlot(NORMALSLOT_VALUE);
		vcb.setSuppressedString(SUPPRESSEDSTRING_VALUE);
		return vcb;
	}
}
