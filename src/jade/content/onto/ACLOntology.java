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
		public static final String       COMMUNICATIVE_ACT = "COMMUNICATIVEACT";
		public static final String       COMMUNICATIVE_ACT_SENDER = "sender";
		public static final String       COMMUNICATIVE_ACT_RECEIVERS = "receivers";
		
    public static final String       INFORM = "INFORM";
    public static final String       INFORM_PREDICATE = "predicate";
    
    public static final String       REQUEST = "REQUEST";
    public static final String       REQUEST_ACTION = "action";
    
    public static final String       QUERY_REF = "QUERYREF";
    public static final String       QUERY_REF_IRE = "ire";
    
    private static final ACLOntology theInstance = new ACLOntology();

    /**
     * Constructor
     */
    private ACLOntology() {
        super("ACL_ONTOLOGY", BasicOntology.getInstance(), new ReflectiveIntrospector());

        try {
            AgentActionSchema baseSchema = new AgentActionSchema(COMMUNICATIVE_ACT);
            baseSchema.add(COMMUNICATIVE_ACT_SENDER, (ConceptSchema) getSchema(BasicOntology.AID));
            baseSchema.add(COMMUNICATIVE_ACT_RECEIVERS, (AggregateSchema) getSchema(BasicOntology.SEQUENCE));
            add(baseSchema, CommunicativeActBase.class);

            AgentActionSchema informSchema = new AgentActionSchema(INFORM);
            informSchema.addSuperSchema(baseSchema);
            informSchema.add(INFORM_PREDICATE, (PredicateSchema) PredicateSchema.getBaseSchema());
            add(informSchema, Inform.class);

            AgentActionSchema requestSchema = new AgentActionSchema(REQUEST);
            requestSchema.addSuperSchema(baseSchema);
            requestSchema.add(REQUEST_ACTION, (AgentActionSchema) AgentActionSchema.getBaseSchema());
            add(requestSchema, Request.class);

            AgentActionSchema queryrefSchema = new AgentActionSchema(QUERY_REF);
            queryrefSchema.addSuperSchema(baseSchema);
            queryrefSchema.add(QUERY_REF_IRE, (IRESchema) IRESchema.getBaseSchema());
            add(queryrefSchema); // As the content of a QUERYREF is an IRE a concrete QueryRef class makes no sense

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
