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
package jade.content.onto;

import jade.content.*;
import jade.content.acl.*;
import jade.content.schema.*;

/**
 * Ontology containing the concepts that ACL mandates.
 *
 * see jade.content.Ontology
 *
 * @author Federico Bergenti - Universita` di Parma
 */
public class ACLOntology extends Ontology {
    public static final String       INFORM = "INFORM";
    public static final String       INFORM_PROPOSITION = "proposition";
    
    public static final String       REQUEST = "REQUEST";
    public static final String       REQUEST_ACTION = "action";
    
    public static final String       QUERYREF = "QUERYREF";
    public static final String       QUERYREF_IRE = "ire";
    
    private static final ACLOntology theInstance = new ACLOntology();

    /**
     * Constructor
     */
    private ACLOntology() {
        super("ACL_ONTOLOGY", BasicOntology.getInstance(), new ReflectiveIntrospector());

        try {
            CommunicativeActSchema informSchema = new CommunicativeActSchema(INFORM);
            informSchema.add(INFORM_PROPOSITION, (PropositionSchema) PropositionSchema.getBaseSchema());
            add(informSchema, Inform.class);

            CommunicativeActSchema requestSchema = new CommunicativeActSchema(REQUEST);
            requestSchema.add(REQUEST_ACTION, (GenericActionSchema) GenericActionSchema.getBaseSchema());
            add(requestSchema, Request.class);
            
            CommunicativeActSchema queryRefSchema = new CommunicativeActSchema(QUERYREF);
            queryRefSchema.add(QUERYREF_IRE, (IRESchema) IRESchema.getBaseSchema());
            add(queryRefSchema); // As the content of a QUERYREF is an IRE a concrete QueryRef class makes no sense

        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Returns the singleton instance of the <code>ACLOntology</code>.
     * @return the <code>ACLOntology</code>
     */
    public static Ontology getInstance() {
        return theInstance;
    } 
}
