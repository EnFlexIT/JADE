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
public class AIDSchema extends ConceptSchema {
    public static final String BASE_NAME = "AID";
    private static AIDSchema  baseSchema = new AIDSchema();

    /**
     * Construct a schema that vinculates an entity to be an AID
     * Note that there are no different types of AID as it 
     * happens for concepts (e.g. Person, Address...), IREs (e.g. IOTA,
     * ANY, ALL...) and the like. Therefore there is no AIDSchema
     * constructor that takes a String parameter.
     */
    private AIDSchema() {
        super(BASE_NAME);
        
        try {
        	add(BasicOntology.AID_NAME, BasicOntology.getInstance().getSchema(BasicOntology.STRING));
        	add(BasicOntology.AID_ADDRESSES, BasicOntology.getInstance().getSchema(BasicOntology.SET));
        	add(BasicOntology.AID_RESOLVERS, BasicOntology.getInstance().getSchema(BasicOntology.SET));
        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Retrieve the generic base schema for AIDs.
     *
     * @return the generic base schema for AIDs.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold an AID      
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsAID();
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
  		if (!(abs instanceof AbsAID)) {
				throw new OntologyException(abs+" is not an AbsAID");
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

