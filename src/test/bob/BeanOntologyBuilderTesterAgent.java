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

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import test.common.TestGroup;
import test.common.TesterAgent;

/**
   @author Paolo Cancedda
 */
public class BeanOntologyBuilderTesterAgent extends TesterAgent {
	private static final long serialVersionUID = 1L;

	private Codec codec;
	private Ontology flatOntology;
	private Ontology hierarchicalOntology;

	@Override
	protected void setup() {
		super.setup();
		codec = new SLCodec();
		try {
			flatOntology = TestFlatBeanOntology.getInstance();
			hierarchicalOntology = TestHierarchicalBeanOntology.getInstance();
		} catch (BeanOntologyException e) {
			e.printStackTrace();
			doDelete();
		}
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(flatOntology);
		getContentManager().registerOntology(hierarchicalOntology);
	}

	public Codec getCodec() {
		return codec;
	}

	public Ontology getFlatOntology() {
		return flatOntology;
	}

	public Ontology getHierarchicalOntology() {
		return hierarchicalOntology;
	}

	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/bob/beanOntologyBuilderTestsList.xml");
		return tg;
	}
}
