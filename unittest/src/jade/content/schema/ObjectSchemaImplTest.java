package jade.content.schema;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

import jade.content.abs.AbsObject;
import jade.content.onto.*;

public class ObjectSchemaImplTest {
	private Ontology basic = BasicOntology.getInstance();
	
	private class TestFacet1 implements Facet {
		public void validate(AbsObject value, Ontology onto) throws OntologyException {
		}
	}

	private class TestFacet2 implements Facet {
		public void validate(AbsObject value, Ontology onto) throws OntologyException {
		}
	}

	@Test
	public void testAddFacet() throws Exception {
		Facet f1 = new TestFacet1();
		Facet f2 = new TestFacet2();
		
		ObjectSchemaImpl s1 = new ObjectSchemaImpl("S1");
		s1.add("slot1", basic.getSchema(BasicOntology.STRING));
		s1.addFacet("slot1", f1);
		s1.add("slot2", basic.getSchema(BasicOntology.STRING));
		s1.addFacet("slot2", f1);
		s1.add("slot3", basic.getSchema(BasicOntology.STRING));
		
		ObjectSchemaImpl s2 = new ObjectSchemaImpl("S2");
		s2.addSuperSchema(s1);
		s2.addFacet("slot1", f2);
		s2.addFacet("slot3", f2);
		
		// Reading facets on S1 we expect:
		// slot1 --> (f1)
		// slot2 --> (f1)
		// slot3 --> ()
		Vector facets = s1.getAllFacets("slot1");
		assertEquals(facets.size(), 1);
		assertEquals(facets.get(0), f1);
		facets = s1.getAllFacets("slot2");
		assertEquals(facets.size(), 1);
		assertEquals(facets.get(0), f1);
		facets = s1.getAllFacets("slot3");
		assertEquals(facets.size(), 0);
				
		// Reading facets on S2 we expect:
		// slot1 --> (f1, f2)
		// slot2 --> (f1)
		// slot3 --> (f2)
		facets = s2.getAllFacets("slot1");
		assertEquals(facets.size(), 2);
		assertTrue(facets.contains(f1));
		assertTrue(facets.contains(f2));
		facets = s2.getAllFacets("slot2");
		assertEquals(facets.size(), 1);
		assertEquals(facets.get(0), f1);
		facets = s2.getAllFacets("slot3");
		assertEquals(facets.size(), 1);
		assertEquals(facets.get(0), f2);
	}

}
