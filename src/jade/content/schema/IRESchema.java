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
    public static final String ANY = ACLOntology.ANY;
    public static final String IOTA = ACLOntology.IOTA;
    public static final String ALL = ACLOntology.ALL;
    public static final String BASE_NAME = "IRE";
    public static final String QUANTIFIED_VARIABLE = 
        BasicOntology.QUANTIFIED_VARIABLE;
    public static final String KIND = BasicOntology.KIND;
    public static final String VARIABLE = BasicOntology.VARIABLE;
    public static final String PROPOSITION = BasicOntology.PROPOSITION;
    private static IRESchema   baseSchema = new IRESchema();

    /**
     * Constructor
     *
     */
    private IRESchema() {
        super(BASE_NAME);

        BasicOntology basicOntology = BasicOntology.getInstance();

        try {
            addElement(KIND, 
                basicOntology.getSchema(BasicOntology.STRING));
            addElement(VARIABLE, 
                basicOntology.getSchema(BasicOntology.SET));
            addElement(PROPOSITION, PropositionSchema.getBaseSchema());
        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Retrieves the base schema.
     *
     * @return the base schema.
     *
     */
    public static ContentElementSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates a new instance.
     *
     * @return the new instance.
     *
     */
    public AbsObject newInstance() {
        return new AbsIRE();
    } 
}
