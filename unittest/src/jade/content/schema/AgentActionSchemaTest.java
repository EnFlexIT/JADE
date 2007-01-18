package jade.content.schema;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

import jade.content.abs.AbsObject;
import jade.content.onto.*;

public class AgentActionSchemaTest {
	private Ontology basic = BasicOntology.getInstance();
	
	@Test
	public void testSetResultTermSchema() throws Exception {	
		TermSchema s1 = (TermSchema) basic.getSchema(BasicOntology.STRING);
		TermSchema s2 = (TermSchema) basic.getSchema(BasicOntology.INTEGER);
		
		AgentActionSchema a1 = new AgentActionSchema("A1");
		a1.add("SLOT1", basic.getSchema(BasicOntology.STRING));
		a1.setResult(s1);
		
		AgentActionSchema a2 = new AgentActionSchema("A2");
		a2.addSuperSchema(a1);
		
		AgentActionSchema a3 = new AgentActionSchema("A3");
		a3.addSuperSchema(a2);
		a3.setResult(s2);
		
		// Reading result schema from a1 we expect s1
		TermSchema resultSchema = a1.getResultSchema();
		assertEquals(resultSchema, s1);
		// Reading slot names from a1 we expect "SLOT1" only
		String[] names = a1.getNames();
		assertEquals(names.length, 1);
		assertEquals(names[0], "SLOT1");
		
		// Reading result schema from a1 we expect s1
		resultSchema = a2.getResultSchema();
		assertEquals(resultSchema, s1);
		
		// Reading result schema from a3 we expect s2
		resultSchema = a3.getResultSchema();
		assertEquals(resultSchema, s2);
	}

}
