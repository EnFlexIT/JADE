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

    /**
     * Constructor
     *
     * @param name name of the schema.
     *
     */
    public ActionPredicateSchema(String name) {
        super(name);
    }

    /**
     * Add parameters to the schema.
     *
     * @param name name of the parameter.
     * @param actionSchema the schema of the parameter.
     *
     */
    public void addParameter(String name, GenericActionSchema actionSchema) {
        addAttribute(name, actionSchema);
    } 

    /**
     * Add parameters to the schema.
     *
     * @param name name of the parameter.
     * @param actionSchema the schema of the parameter.
     * @param cardinality the cardinality, i.e., optional or mandatory.
     *
     */
    public void addParameter(String name, GenericActionSchema actionSchema, 
                             int cardinality) {
        addAttribute(name, actionSchema, cardinality);
    } 

    /**
     * Add parameters to the schema.
     *
     * @param name name of the parameter.
     * @param termSchema schema of the parameter.
     *
     */
    public void addParameter(String name, TermSchema termSchema) {
        addAttribute(name, termSchema);
    } 

   /**
     * Add parameters to the schema.
     *
     * @param name name of the parameter.
     * @param actionSchema the schema of the parameter.
     * @param cardinality the cardinality, i.e., optional or mandatory.
     *
     */
    public void addParameter(String name, TermSchema termSchema, 
                             int cardinality) {
        addAttribute(name, termSchema, cardinality);
    } 

    /**
     * Creates a new instance. 
     *
     * @return teh new instance.
     *
     */
    public AbsObject newInstance() {
        return new AbsActionPredicate(getTypeName());
    } 

}

