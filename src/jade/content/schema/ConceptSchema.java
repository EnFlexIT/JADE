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
     * Creates a <code>ConceptSchema</code> with a given type-name.
     *
     * @param typeName The name of this <code>ConceptSchema</code>.
     */
    public ConceptSchema(String typeName) {
        super(typeName);
    }

    /**
     * Retrieve the generic base schema for all concepts.
     *
     * @return the generic base schema for all concepts.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Add a mandatory slot to the schema. The schema for this slot must 
     * be a <code>TermSchema</code>.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     */
    public void add(String name, TermSchema slotSchema) {
        super.add(name, slotSchema);
    } 

    /**
     * Add a slot to the schema. The schema for this slot must 
     * be a <code>TermSchema</code>.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     * @param cardinality The cardinality, i.e., <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    public void add(String name, TermSchema slotSchema, int cardinality) {
        super.add(name, slotSchema, cardinality);
    } 

    /**
     * Adds a super-schema to this schema. This allows defining 
     * inheritance relationships between ontological concepts.
     * It must be noted that a concept always inherits from another 
     * concept --> A super-schemas of a <code>ConceptSchema</code>
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
    public AbsObject newInstance() {
        return new AbsConcept(getTypeName());
    } 
}
