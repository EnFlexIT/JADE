package test.content.testOntology;

import jade.content.onto.BasicOntology;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.PredicateSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import jade.content.schema.TermSchema;

public class LEAPOntology extends Ontology {
	public static final String ONTOLOGY_NAME = "LEAP-ontology";

	// VOCABULARY
	public static final String LEAP_PREDICATE = "LEAPPREDICATE";
	public static final String LEAP_PREDICATE_LIST = "list";
	public static final String LEAP_PREDICATE_OBJECT = "object";
	
	// The singleton instance of this ontology
	private static Ontology theInstance = new LEAPOntology();

	public static Ontology getInstance() {
		return theInstance;
	}

	/**
	 * Constructor
	 */
	private LEAPOntology() {
		super(ONTOLOGY_NAME, new Ontology[]{BasicOntology.getInstance(), CFOntology.getInstance()}, new ReflectiveIntrospector());

		try {
			add(new PredicateSchema(LEAP_PREDICATE), LEAPPredicate.class);

			PredicateSchema ps = (PredicateSchema) getSchema(LEAP_PREDICATE);
			ps.add(LEAP_PREDICATE_LIST, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			ps.add(LEAP_PREDICATE_OBJECT, (TermSchema) TermSchema.getBaseSchema(), ObjectSchema.OPTIONAL);
		} 
		catch (OntologyException oe) {
			oe.printStackTrace();
		} 
	}
}
