package jade.content.onto;

import static org.junit.Assert.*;

import org.junit.Test;

import jade.content.schema.*;
import java.util.List;

public class OntologyTest {
	private Ontology basic = BasicOntology.getInstance();
	
	private static final String C1 = "C1";
	private static final String C2 = "C2";
	private static final String C3 = "C3";
	private static final String A1 = "A1";
	private static final String P1 = "P1";
	
	@Test
	public void testGetAllConcepts() throws Exception {
		Ontology o1 = new Ontology("O1", basic);
		o1.add(new ConceptSchema(C1));
		o1.add(new AgentActionSchema(A1));
		
		Ontology o2 = new Ontology("O2", SerializableOntology.getInstance());
		o2.add(new ConceptSchema(C2));
		o2.add(new PredicateSchema(P1));
		
		Ontology o3 = new Ontology("O3", new Ontology[]{o1, o2}, new ReflectiveIntrospector());
		o3.add(new ConceptSchema(C3));
		
		// Getting concept names from O1 we expect: (C1, A1)
		List names = o1.getConceptNames(); 
		System.out.println(names);
		assertEquals(names.size(), 2);
		assertTrue(names.contains(C1));
		assertTrue(names.contains(A1));
		
		// Getting concept names from O2 we expect: (C2)
		names = o2.getConceptNames(); 
		System.out.println(names);
		assertEquals(names.size(), 1);
		assertEquals(names.get(0), C2);
		
		// Getting concept names from O3 we expect: (C1, A1, C2, C3)
		names = o3.getConceptNames(); 
		System.out.println(names);
		assertEquals(names.size(), 4);
		assertTrue(names.contains(C1));
		assertTrue(names.contains(A1));
		assertTrue(names.contains(C2));
		assertTrue(names.contains(C3));
	}

}
