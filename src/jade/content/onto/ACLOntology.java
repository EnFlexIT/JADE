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
public class ACLOntology extends FullOntology {
    public static final String       INFORM = "Inform";
    public static final String       PROPOSITION = "Proposition";
    public static final String       QUERY_REF = "QueryRef";
    public static final String       IRE = "IRE";
    public static final String       REQUEST = "Request";
    public static final String       ACTION = "Action";
    public static final String       SENDER = "Sender";
    public static final String       RECEIVERS = "Receivers";
    public static final String       ANY = "Any";
    public static final String       IOTA = "Iota";
    public static final String       ALL = "All";
    public static final String       VARIABLES = "Variables";
    public static final String       CONCEPT = "Concept";
    public static final String       EQUALS = "Equals";
    private static final ACLOntology theInstance = new ACLOntology();

    /**
     * Constructor
     *
     */
    private ACLOntology() {
        super("ACL_ONTOLOGY", BasicOntology.getInstance());

        try {
            add(IRESchema.getBaseSchema());
            add(ContentElementSchema.getBaseSchema());
            add(CommunicativeActSchema.getBaseSchema());
	    add(EqualsSchema.getBaseSchema());

            CommunicativeActSchema informSchema = 
                new CommunicativeActSchema(INFORM);

            informSchema.add(PROPOSITION, 
                             PropositionSchema.getBaseSchema());
            add(informSchema, Inform.class);

            CommunicativeActSchema queryRefSchema = 
                new CommunicativeActSchema(QUERY_REF);

            queryRefSchema.add(IRE, IRESchema.getBaseSchema());
            add(queryRefSchema, QueryRef.class);

            CommunicativeActSchema requestSchema = 
                new CommunicativeActSchema(REQUEST);

            requestSchema.add(ACTION, new GenericActionSchema());
            add(requestSchema, Request.class);
        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Returns the singleton instance of the <code>BasicOntology</code>.
     *
     * @return the <code>BasicOntology</code>
     *
     */
    public static ACLOntology getInstance() {
        return theInstance;
    } 
}
