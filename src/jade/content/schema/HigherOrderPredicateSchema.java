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
import java.util.Vector;
import java.util.Enumeration;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class HigherOrderPredicateSchema extends PropositionSchema {

    /**
     * Constructor
     *
     * @param name
     *
     */
    public HigherOrderPredicateSchema(String name) {
        super(name);
    }

    /**
     * Adds an argument schema.
     *
     * @param name name of the argument.
     * @param slotSchema schema to add.
     *
     */
    public void add(String name, PropositionSchema slotSchema) {
        addElement(name, slotSchema);
    } 

    /**
     * Adds an argument schema.
     *
     * @param name name of the argument.
     * @param slotSchema schema to add.
     * @param cardinality cardinality of the argument, i.e., optional or
     *        mandatory.
     *
     */
    public void add(String name, PropositionSchema slotSchema, int cardinality) {
        addElement(name, slotSchema, cardinality);
    } 

    /**
     * Adds an argument schema.
     *
     * @param name name of the argument.
     * @param slotSchema schema to add.
     *
     */
    public void add(String name, TermSchema slotSchema) {
        addElement(name, slotSchema);
    } 

    /**
     * Adds an argument schema.
     *
     * @param name name of the argument.
     * @param slotSchema schema to add.
     * @param cardinality cardinality of the argument, i.e., mandatory or
     *        optional.
     *
     */
    public void add(String name, TermSchema slotSchema, int cardinality) {
        addElement(name, slotSchema, cardinality);
    } 

    /**
     * Creates a new instance.
     *
     * @return the new instance.
     *
     */
    public AbsObject newInstance() {
        return new AbsHigherOrderPredicate(getTypeName());
    } 
}
