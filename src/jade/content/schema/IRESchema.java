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
public class IRESchema extends ContentElementSchema {
    public static final String BASE_NAME = "IRE";
    private static IRESchema   baseSchema = new IRESchema();
    
    public static final String VARIABLE = "Variable";
    public static final String PROPOSITION = "Proposition";

    /**
     * Construct a schema that vinculates an entity to be a generic
     * ire
     */
    private IRESchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>IRESchema</code> with a given type-name.
     * All ire-s have a variable and a proposition.
     *
     * @param typeName The name of this <code>IRESchema</code> 
     * (e.g. IOTA, ANY, ALL).
     */
    public IRESchema(String typeName) {
        super(typeName);

        // FIXME It should be possible to specify a set of variables
        add(VARIABLE, VariableSchema.getBaseSchema()); 
        add(PROPOSITION, PropositionSchema.getBaseSchema());
    }

    /**
     * Retrieve the generic base schema for all ire-s.
     *
     * @return the generic base schema for all ire-s.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold a ire of
     * the proper type.
     */
    public AbsObject newInstance() {
        return new AbsIRE(getTypeName());
    } 
}
