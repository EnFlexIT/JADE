/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.schema;

import jade.content.abs.*;
import jade.content.onto.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class ConceptSchema extends TermSchema {
    public static final String   BASE_NAME = "Concept";
    private static ConceptSchema baseSchema = new ConceptSchema();

    /**
     * Construct a schema that vinculates an entity to be a generic
     * concept
     */
    private ConceptSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>ConceptSchema</code> with a given type-name,
     * e.g. PERSON, ADDRESS...
     * @param typeName The name of this <code>ConceptSchema</code>.
     */
    public ConceptSchema(String typeName) {
        super(typeName);
    }

    /**
     * Retrieve the generic base schema for all concepts.
     * @return the generic base schema for all concepts.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Add a mandatory slot to the schema. The schema for this slot must 
     * be a <code>TermSchema</code>.
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     */
    public void add(String name, TermSchema slotSchema) {
        super.add(name, slotSchema);
    } 

    /**
     * Add a slot to the schema. The schema for this slot must 
     * be a <code>TermSchema</code>.
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     * @param optionality The optionality, i.e., <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    public void add(String name, TermSchema slotSchema, int optionality) {
        super.add(name, slotSchema, optionality);
    } 

    /**
     * Add a slot with cardinality between <code>cardMin</code>
     * and <code>cardMax</code> to this schema. 
     * Adding such a slot is equivalent to add a slot
     * of type Aggregate and then to add proper facets (constraints)
     * to check that the type of the elements in the aggregate are
     * compatible with <code>elementsSchema</code> and that the 
     * aggregate contains at least <code>cardMin</code> elements and
     * at most <code>cardMax</code> elements.
     * @param name The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin This slot must get at least <code>cardMin</code>
     * values
     * @param cardMax This slot can get at most <code>cardMax</code>
     * values
     */
    public void add(String name, TermSchema elementsSchema, int cardMin, int cardMax) {
      int optionality = (cardMin == 0 ? OPTIONAL : MANDATORY);
    	try {
	      super.add(name, BasicOntology.getInstance().getSchema(BasicOntology.SEQUENCE), optionality);
    	}
    	catch (OntologyException oe) {
    		// Should never happen
    		oe.printStackTrace();
    	}
    	// Add proper facets
    	// FIXME: to be done
    } 

    /**
     * Adds a super-schema to this schema. This allows defining 
     * inheritance relationships between ontological concepts.
     * It must be noted that a concept always inherits from another 
     * concept --> A super-schema of a <code>ConceptSchema</code>
     * must be a <code>ConceptSchema</code> too.
     *
     * @param superClassSchema The super-schema to be added.
     */
    public void addSuperSchema(ConceptSchema superClassSchema) {
        super.addSuperSchema(superClassSchema);
    } 

    /**
     * Creates an Abstract descriptor to hold a concept of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsConcept(getTypeName());
    } 

		/**
	     Check whether a given abstract descriptor complies with this 
	     schema.
	     @param abs The abstract descriptor to be checked
	     @throws OntologyException If the abstract descriptor does not 
	     complies with this schema
	   */
  	public void validate(AbsObject abs, Ontology onto) throws OntologyException {
			// Check the type of the abstract descriptor
  		if (!(abs instanceof AbsConcept)) {
				throw new OntologyException(abs+" is not an AbsConcept");
			}
			
			// Check the slots
			validateSlots(abs, onto);
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
