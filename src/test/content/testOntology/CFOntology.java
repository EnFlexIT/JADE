package test.content.testOntology;

import jade.content.onto.*;
import jade.content.schema.*;

public class CFOntology extends Ontology {
	public static final String ONTOLOGY_NAME = "CF-ontology";

	// VOCABULARY
	public static final String CF_THING = "CFTHING";
	public static final String CF_THING_LIST = "list";
	public static final String CF_THING_OBJECT = "object";
	
	// The singleton instance of this ontology
	private static Ontology theInstance = new CFOntology();

	public static Ontology getInstance() {
		return theInstance;
	}

	/**
	 * Constructor
	 */
	private CFOntology() {
		super(ONTOLOGY_NAME, TestOntology.getInstance(), new CFReflectiveIntrospector());

		try {
			add(new ConceptSchema(CF_THING), CFThing.class);

			ConceptSchema cs = (ConceptSchema) getSchema(CF_THING);
			cs.add(CF_THING_LIST, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			cs.add(CF_THING_OBJECT, (TermSchema) TermSchema.getBaseSchema(), ObjectSchema.OPTIONAL);
		} 
		catch (OntologyException oe) {
			oe.printStackTrace();
		} 
	}

}
