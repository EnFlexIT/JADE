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
     * Creates a schema with a name.
     *
     * @param name
     *
     */
    public ConceptSchema(String name) {
        super(name);

        addBaseSchema(baseSchema);
    }

    /**
     * Constructor
     *
     */
    private ConceptSchema() {
        super(BASE_NAME);
    }

    /**
     * Retrieves the base schema of this schema.
     *
     * @return the base schema.
     *
     */
    public static TermSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Adds a slot to the schema.
     *
     * @param name name of the slot.
     * @param slotSchema schema of the slot.
     *
     */
    public void addSlot(String name, TermSchema slotSchema) {
        addAttribute(name, slotSchema);
    } 

    /**
     * Adds a slot to the schema.
     *
     * @param name name of the slot.
     * @param slotSchema schema of the slot.
     * @param cardinality cardinality of the slot, i.e., optional or mandatory.
     *
     */
    public void addSlot(String name, TermSchema slotSchema, int cardinality) {
        addAttribute(name, slotSchema, cardinality);
    } 

    /**
     * Adds a superclass to this schema.
     *
     * @param superClassSchema
     *
     */
    public void addSuperClass(ConceptSchema superClassSchema) {
        addBaseSchema(superClassSchema);
    } 

    /**
     * Creates a new instance.
     *
     * @return the new instance.
     *
     */
    public AbsObject newInstance() {
        return new AbsConcept(getTypeName());
    } 

}

