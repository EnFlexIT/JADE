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
public class ActionPredicateSchema extends PropositionSchema {
    public static final String         BASE_NAME = "ActionPredicate";
    private static ActionPredicateSchema baseSchema = new ActionPredicateSchema();

    /**
     * Construct a schema that vinculates an entity to be a generic
     * action predicate
     */
    private ActionPredicateSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates an <code>ActionPredicateSchema</code> with a given type-name.
     *
     * @param typeName The name of this <code>ActionPredicateSchema</code>.
     */
    public ActionPredicateSchema(String typeName) {
        super(typeName);
    }

    /**
     * Retrieve the generic base schema for all action predicates.
     *
     * @return the generic base schema for all action predicates.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 
    
    /**
     * Add a mandatory slot to the schema. The schema for this slot must 
     * be a <code>GenericActionSchema</code>.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     */
    public void add(String name, GenericActionSchema slotSchema) {
        super.add(name, slotSchema);
    } 

    /**
     * Add a slot to the schema. The schema for this slot must 
     * be a <code>GenericActionSchema</code>.
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     * @param cardinality The cardinality, i.e., <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    public void add(String name, GenericActionSchema slotSchema, int cardinality) {
        super.add(name, slotSchema, cardinality);
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
     * Creates an Abstract descriptor to hold an action predicate of
     * the proper type.
     */
    public AbsObject newInstance() {
        return new AbsActionPredicate(getTypeName());
    } 
}
