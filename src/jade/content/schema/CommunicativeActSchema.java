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
    private static CommunicativeActSchema baseSchema = 
        new CommunicativeActSchema();
    public static final String            BASE_NAME = "CommunicativeAct";
    public static final String            SENDER = ACLOntology.SENDER;
    public static final String            RECEIVERS = ACLOntology.RECEIVERS;

    /**
     * Constructor
     *
     */
    private CommunicativeActSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a new schema with a name.
     *
     * @param name the name of the schema.
     *
     */
    public CommunicativeActSchema(String name) {
        super(name);

        FullOntology basicOntology = (FullOntology)BasicOntology.getInstance();

        try {
            addElement(SENDER, 
                       basicOntology.getSchema(BasicOntology.AID));
            addElement(RECEIVERS, 
                       basicOntology.getSchema(BasicOntology.SET));
        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Add a parameter to the schema.
     *
     * @param name name of the paramter.
     * @param elementSchema schema of the parameter.
     *
     */
    public void add(String name, ContentElementSchema elementSchema) {
        addElement(name, elementSchema);
    } 

    /**
     * Add a parameter to the schema.
     *
     * @param name name of the paramter.
     * @param elementSchema schema of the parameter.
     * @param cardinality cardinality of the paramter, i.e., 
     *        optional or mandatory
     *
     */
    public void add(String name, ContentElementSchema elementSchema, int cardinality) {
        add(name, elementSchema, cardinality);
    } 

    /**
     * Gets the base schema of this schema.
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
     * @return teh new instance.
     *
     */
    public AbsObject newInstance() {
        return new AbsCommunicativeAct(getTypeName());
    } 
}
