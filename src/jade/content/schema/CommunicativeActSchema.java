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

import jade.content.onto.*;
import jade.content.abs.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class CommunicativeActSchema extends GenericActionSchema {
    public static final String            BASE_NAME = "CommunicativeAct";
    private static CommunicativeActSchema baseSchema = new CommunicativeActSchema();
    
    public static final String            SENDER = "Sender";
    public static final String            RECEIVERS = "Receiver";

    /**
     * Construct a schema that vinculates an entity to be a generic
     * communicative act
     */
    private CommunicativeActSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>CommunicativeActSchema</code> with a given type-name.
     * All communicative acts have a sender (AID) a list of receivers and
     * a variable number of slots of type content element. These (not being 
     * fixed) have to be added depending
     * on the actual type of communicative act (i.e. INFORM, REQUEST...)
     *
     * @param typeName The name of this <code>CommunicativeActSchema</code>.
     */
    public CommunicativeActSchema(String typeName) {
        super(typeName);

        try {
            add(SENDER, BasicOntology.getInstance().getSchema(BasicOntology.AID));
            add(RECEIVERS, BasicOntology.getInstance().getSchema(BasicOntology.SET));
        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Retrieve the generic base schema for all communicative acts.
     *
     * @return the generic base schema for all communicative acts.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Add a mandatory slot to the schema. The schema for this slot must 
     * be a <code>ContentElementSchema</code>.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     */
    public void add(String name, ContentElementSchema slotSchema) {
        super.add(name, slotSchema);
    } 

    /**
     * Add a slot to the schema. The schema for this slot must 
     * be a <code>ContentElementSchema</code>.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     * @param cardinality The cardinality, i.e., <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    public void add(String name, ContentElementSchema slotSchema, int cardinality) {
        super.add(name, slotSchema, cardinality);
    } 

    /**
     * Creates an Abstract descriptor to hold an agent action of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsCommunicativeAct(getTypeName());
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
  		if (s.equals(getBaseSchema())) {
	  		return true;
  		}
  		return super.descendsFrom(s);
  	}
}
