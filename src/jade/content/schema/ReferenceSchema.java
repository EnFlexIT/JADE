package jade.content.schema;

import jade.content.abs.AbsObject;
import jade.content.abs.AbsReference;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;

/**
 * The schema of a reference to an object or an attribute of an object
 * @author Caire
 */
public class ReferenceSchema extends TermSchema {
	public static final String    BASE_NAME = "Reference";
	private static ReferenceSchema baseSchema = new ReferenceSchema();

	public static final String    NAME = "Name";
	public static final String    OBJECT_TYPE = "ObjectType";

	/**
	 * Construct a schema that binds an entity to be a reference to an object or to
	 * an attribute of an object.
	 */
	private ReferenceSchema() {
		super(BASE_NAME);

		try {
			add(NAME, BasicOntology.getInstance().getSchema(BasicOntology.STRING));
			add(OBJECT_TYPE, BasicOntology.getInstance().getSchema(BasicOntology.STRING));
		} 
		catch (OntologyException oe) {
			oe.printStackTrace();
		} 
	}

	/**
	 * Retrieve the generic base schema for all references.
	 *
	 * @return the generic base schema for all references.
	 */
	public static ObjectSchema getBaseSchema() {
		return baseSchema;
	} 

		
	/**
	 * Creates an Abstract descriptor to hold a reference
	 */
	public AbsObject newInstance() throws OntologyException {
		return new AbsReference();
	} 

	/**
	     Check whether a given abstract descriptor complies with this schema.
	     @param abs The abstract descriptor to be checked
	     @throws OntologyException If the abstract descriptor does not 
	     complies with this schema
	 */
	public void validate(AbsObject abs, Ontology onto) throws OntologyException {
		// Check the type of the abstract descriptor
		if (!(abs instanceof AbsReference)) {
			throw new OntologyException(abs+" is not an AbsReference");
		}

		// Check the slots
		validateSlots(abs, onto);
	}

	/**
  	   A reference can be put wherever a term of whatever type is
  	   required --> A ReferenceSchema is
  	   compatible with s if s descends from TermSchema.getBaseSchema()
	 */
	public boolean isCompatibleWith(ObjectSchema s) {
		if (s != null) {
			return s.descendsFrom(TermSchema.getBaseSchema());
		}
		else {
			return false;
		}
	}

	/**
  	   Return true if 
  	   - s is the base schema for the XXXSchema class this schema is
  	     an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
  	     schema is an instance of ConceptSchema)
  	   - s is the base schema for a super-class of the XXXSchema class
  	     this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
  	     and this schema is an instance of ConceptSchema)
	 */
	protected boolean descendsFrom(ObjectSchema s) {
		if (s != null) {
			if (s.equals(getBaseSchema())) {
				return true;
			}
			return super.descendsFrom(s);
		}
		else {
			return false;
		}
	}
}
