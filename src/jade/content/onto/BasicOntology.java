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
import jade.content.schema.*;
import jade.core.AID;
import jade.util.leap.List;

/**
 * Ontology containing basic concepts.
 *
 * see jade.content.Ontology
 *
 * @author Federico Bergenti - Universita` di Parma
 */
public class BasicOntology extends Ontology {
	// The singleton instance of this ontology
  private static final BasicOntology theInstance = new BasicOntology();
  static {
  	theInstance.initialize();
  }
  
  // Primitive types names
 	public static final String         STRING = "BO_String";
  public static final String         FLOAT = "BO_Float";
  public static final String         INTEGER = "BO_Integer";
  public static final String         BOOLEAN = "BO_Boolean";
  public static final String         DATE = "BO_Date";
  public static final String         BYTE_SEQUENCE = "BO_Byte-sequence";
    
  // Aggregate types names
  public static final String         SEQUENCE = "sequence";
  public static final String         SET = "set";
    
  // Content element list 
  public static final String         CONTENT_ELEMENT_LIST = ContentElementListSchema.BASE_NAME;
  
  // Generic concepts: AID and ACLMessage
  public static final String         AID = "agent-identifier";
  public static final String         AID_NAME = "name";
  public static final String         AID_ADDRESSES = "addresses";
  public static final String         AID_RESOLVERS = "resolvers";

  public static final String         ACLMSG = "acl-message";
  public static final String         ACLMSG_PERFORMATIVE = "performative";
  public static final String         ACLMSG_SENDER = "sender";
  public static final String         ACLMSG_RECEIVERS = "receivers";
  public static final String         ACLMSG_REPLY_TO = "reply-to";
  public static final String         ACLMSG_LANGUAGE = "language";
  public static final String         ACLMSG_ONTOLOGY = "ontology";
  public static final String         ACLMSG_PROTOCOL = "protocol";
  public static final String         ACLMSG_IN_REPLY_TO = "in-reply-to";
  public static final String         ACLMSG_REPLY_WITH = "reply-with";
  public static final String         ACLMSG_CONVERSATION_ID = "conversation-id";
  public static final String         ACLMSG_REPLY_BY = "reply-by";
  public static final String         ACLMSG_CONTENT = "content";
  public static final String         ACLMSG_BYTE_SEQUENCE_CONTENT = "bs-content";
  public static final String         ACLMSG_ENCODING = "encoding";
  
  // Generic propositions: TRUE_PROP (i.e. the proposition that is true under whatever condition) 
  public static final String         TRUE_PROPOSITION = "TRUE";
  
  // Useful operators 
  public static final String         DONE = "DONE";
  public static final String         DONE_ACTION = "action";
    
  public static final String         RESULT = "RESULT";
  public static final String         RESULT_ACTION = "action";
  public static final String         RESULT_ITEMS = "items";
    
  public static final String         EQUALS = "EQUALS";
  public static final String         EQUALS_LEFT = "Left";
  public static final String         EQUALS_RIGHT = "Right";
  
  public static final String         ACTION = "ACTION";
  public static final String         ACTION_ACTOR = "Actor";
  public static final String         ACTION_ACTION = "Action";
  
  /**
   * Constructor
   */
  private BasicOntology() {
  	super("BASIC_ONTOLOGY", new BasicIntrospector());
  }
  
  private void initialize() {
    try {
    	// Schemas for primitives
      add(new PrimitiveSchema(STRING));
      add(new PrimitiveSchema(FLOAT));
      add(new PrimitiveSchema(INTEGER));
      add(new PrimitiveSchema(BOOLEAN));
      add(new PrimitiveSchema(DATE));
      add(new PrimitiveSchema(BYTE_SEQUENCE));
            
    	// Schemas for aggregates
      add(new AggregateSchema(SEQUENCE));
      add(new AggregateSchema(SET));


      // Note that the association between schemas and classes is not 
      // necessary for the elements of the BasicOntology as the
      // BasicIntrospector does not use schemas to translate between 
      // Java objects and abstract descriptors, but performs a hardcoded
      // translation
      
      // Content element list Schema
      add(ContentElementListSchema.getBaseSchema()); 
      
      // AID Schema
      ConceptSchema aidSchema = new ConceptSchema(AID);
      aidSchema.add(AID_NAME, (TermSchema) getSchema(STRING));
      aidSchema.add(AID_ADDRESSES, (TermSchema) getSchema(STRING), 0, ObjectSchema.UNLIMITED);
      aidSchema.add(AID_RESOLVERS, aidSchema, 0, ObjectSchema.UNLIMITED);
      add(aidSchema); 
      
      // ACLMessage Schema
      AgentActionSchema msgSchema = new AgentActionSchema(ACLMSG);
      msgSchema.add(ACLMSG_PERFORMATIVE, (PrimitiveSchema) getSchema(INTEGER));
      msgSchema.add(ACLMSG_SENDER, (ConceptSchema) getSchema(AID), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_RECEIVERS, (ConceptSchema) getSchema(AID), 0, ObjectSchema.UNLIMITED);
      msgSchema.add(ACLMSG_REPLY_TO, (ConceptSchema) getSchema(AID), 0, ObjectSchema.UNLIMITED);
      msgSchema.add(ACLMSG_LANGUAGE, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_ONTOLOGY, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_PROTOCOL, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_IN_REPLY_TO, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_REPLY_WITH, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_CONVERSATION_ID, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_REPLY_BY, (PrimitiveSchema) getSchema(DATE), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_CONTENT, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_BYTE_SEQUENCE_CONTENT, (PrimitiveSchema) getSchema(BYTE_SEQUENCE), ObjectSchema.OPTIONAL);
      msgSchema.add(ACLMSG_ENCODING, (PrimitiveSchema) getSchema(STRING), ObjectSchema.OPTIONAL);
      add(msgSchema); 
      
      // TRUE_PROPOSITION schema
      PredicateSchema truePropSchema = new PredicateSchema(TRUE_PROPOSITION);
      add(truePropSchema);

      // DONE Schema
      PredicateSchema doneSchema = new PredicateSchema(DONE);
      doneSchema.add(DONE_ACTION, AgentActionSchema.getBaseSchema());
      add(doneSchema); 
      
      // EQUALS Schema
      PredicateSchema resultSchema = new PredicateSchema(RESULT);
      resultSchema.add(RESULT_ACTION, (AgentActionSchema) AgentActionSchema.getBaseSchema());
      resultSchema.add(RESULT_ITEMS, (TermSchema) getSchema(SEQUENCE));
      add(resultSchema); 
      
      // EQUALS Schema
      PredicateSchema equalsSchema = new PredicateSchema(EQUALS);
      equalsSchema.add(EQUALS_LEFT, TermSchema.getBaseSchema());
      equalsSchema.add(EQUALS_RIGHT, TermSchema.getBaseSchema());
      add(equalsSchema); 

      // ACTION Schema
      AgentActionSchema actionSchema = new AgentActionSchema(ACTION);
      actionSchema.add(ACTION_ACTOR, (TermSchema) getSchema(AID));
      actionSchema.add(ACTION_ACTION, (TermSchema) ConceptSchema.getBaseSchema());
      add(actionSchema); 
    } 
    catch (OntologyException oe) {
      oe.printStackTrace();
    } 
  }

  /**
   * Returns the singleton instance of the <code>BasicOntology</code>.
   * @return the singleton instance of the <code>BasicOntology</code>
   */
  public static Ontology getInstance() {
    return theInstance;
  } 
}
