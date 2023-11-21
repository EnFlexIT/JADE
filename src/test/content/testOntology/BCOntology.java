package test.content.testOntology;

import jade.content.onto.BCReflectiveIntrospector;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;

public class BCOntology extends Ontology {
	public static final String ONTOLOGY_NAME = "BC-ontology";

	// VOCABULARY
	public static final String BC_THING = "BCTHING";
	public static final String BC_THING_LLL = "lll";
	
	// The singleton instance of this ontology
	private static Ontology theInstance = new BCOntology();

	public static Ontology getInstance() {
		return theInstance;
	}

	/**
	 * Constructor
	 */
	private BCOntology() {
		super(ONTOLOGY_NAME, LEAPOntology.getInstance(), new BCReflectiveIntrospector());

		try {
			add(new ConceptSchema(BC_THING), BCThing.class);

			ConceptSchema cs = (ConceptSchema) getSchema(BC_THING);
			cs.addSuperSchema((ConceptSchema) getSchema(CFOntology.CF_THING));
			cs.add(BC_THING_LLL, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
	    	useConceptSlotsAsFunctions();
		} 
		catch (OntologyException oe) {
			oe.printStackTrace();
		} 
	}

}
