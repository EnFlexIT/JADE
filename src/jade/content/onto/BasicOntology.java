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
import jade.content.abs.*;
import jade.content.OntoAID;
import jade.content.OntoACLMessage;
import jade.content.ContentElementList;
import jade.content.onto.basic.*;
import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.lang.acl.ACLMessage;
import jade.util.leap.*;
import java.util.Date;

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
  	super("BASIC_ONTOLOGY", (Ontology) null);
  }
  
  private void initialize() {
    // Note that the association between schemas and classes is not 
    // necessary for the elements of the BasicOntology as the
    // BasicOntology does not use schemas to translate between 
    // Java objects and abstract descriptors, but performs a hardcoded
    // translation  
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
  
  /**
   * This method is redefined as BasicOntology does not use an
   * Introspector for performance reason
   * @see Ontology#toObject(AbsObject, Ontology)
   */
  protected Object toObject(AbsObject abs, String lcType, Ontology referenceOnto) throws UngroundedException, OntologyException {
    try {
      if (abs == null) {
        return null;
      } 

     	// PRIMITIVE
      if (abs instanceof AbsPrimitive) {
        return ((AbsPrimitive) abs).getObject();
      } 
      // AGGREGATES
     	if (abs instanceof AbsAggregate) {
        return AbsHelper.internaliseList((AbsAggregate) abs, referenceOnto);
      } 
			// CONTENT ELEMENT LIST
      if (abs instanceof AbsContentElementList) {
        return AbsHelper.internaliseContentElementList((AbsContentElementList) abs, referenceOnto);
      } 
			// AID
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.AID)) { 
				return AbsHelper.internaliseAID((AbsConcept) abs);
	    }
	    // TRUE_PROPOSITION
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.TRUE_PROPOSITION)) { 
				TrueProposition t = new TrueProposition();
				return t;
	    }
	    // DONE
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.DONE)) { 
				Done d = new Done();
  			d.setAction((AgentAction) referenceOnto.toObject(abs.getAbsObject(BasicOntology.DONE_ACTION))); 
				return d;
	    }
	    // RESULT
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.RESULT)) { 
				Result r = new Result();
  			r.setAction((AgentAction) referenceOnto.toObject(abs.getAbsObject(BasicOntology.RESULT_ACTION))); 
  			r.setItems((List) referenceOnto.toObject(abs.getAbsObject(BasicOntology.RESULT_ITEMS))); 
				return r;
	    }
	    // EQUALS
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.EQUALS)) { 
				Equals e = new Equals();
  			e.setLeft(referenceOnto.toObject(abs.getAbsObject(BasicOntology.EQUALS_LEFT))); 
  			e.setRight(referenceOnto.toObject(abs.getAbsObject(BasicOntology.EQUALS_RIGHT))); 
				return e;
	    }
	    // ACTION
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.ACTION)) { 
	    	Action a = new Action();
	    	a.internalise(abs, referenceOnto);
	    	return a;
	    }
			// ACLMESSAGE
	    if (CaseInsensitiveString.equalsIgnoreCase(abs.getTypeName(), BasicOntology.ACLMSG)) { 
				return AbsHelper.internaliseACLMessage((AbsAgentAction) abs, referenceOnto);
	    }
	    			
	    throw new UnknownSchemaException();
    } 
    catch (OntologyException oe) {
      // Forward the exception
      throw oe;
    } 
    catch (Throwable t) {
      throw new OntologyException("Unexpected error internalising "+abs+".", t);
    }
  }
		
  /**
   * This method is redefined as BasicOntology does not use an
   * Introspector for performance reason
   * @see Ontology#toObject(AbsObject, Ontology)
   */
  protected AbsObject fromObject(Object obj, Ontology referenceOnto) throws OntologyException{
    try {
      if (obj == null) {
        return null;
      } 

      if (obj instanceof String) {
        return AbsPrimitive.wrap((String) obj);
      } 
      if (obj instanceof Boolean) {
        return AbsPrimitive.wrap(((Boolean) obj).booleanValue());
      } 
      if (obj instanceof Integer) {
        return AbsPrimitive.wrap(((Integer) obj).intValue());
      } 
      if (obj instanceof Long) {
        return AbsPrimitive.wrap(((Long) obj).longValue());
      } 
      //__CLDC_UNSUPPORTED__BEGIN
      if (obj instanceof Float) {
        return AbsPrimitive.wrap(((Float) obj).floatValue());
      } 
      if (obj instanceof Double) {
        return AbsPrimitive.wrap(((Double) obj).doubleValue());
      } 
      //__CLDC_UNSUPPORTED__END
      if (obj instanceof Date) {
        return AbsPrimitive.wrap((Date) obj);
      } 
      if (obj instanceof byte[]) {
        return AbsPrimitive.wrap((byte[]) obj);
      } 


      if (obj instanceof List) {
        return AbsHelper.externaliseList((List) obj, referenceOnto);
      }

	    if (obj instanceof Iterator) {
				return AbsHelper.externaliseIterator((Iterator) obj, referenceOnto);
	    }
	    
	    if(obj instanceof AID) {
				return AbsHelper.externaliseAID((AID)obj);
	    }

      if (obj instanceof ContentElementList) {
        return AbsHelper.externaliseContentElementList((ContentElementList) obj, referenceOnto);
      } 
	    
	    if(obj instanceof TrueProposition) {
	    	AbsPredicate absTrueProp = new AbsPredicate(BasicOntology.TRUE_PROPOSITION);
				return absTrueProp;
	    }

	    if(obj instanceof Done) {
	    	AbsPredicate absDone = new AbsPredicate(BasicOntology.DONE);
  			absDone.set(BasicOntology.DONE_ACTION, (AbsAgentAction) referenceOnto.fromObject(((Done) obj).getAction()));
				return absDone;
	    }

	    if(obj instanceof Result) {
	    	AbsPredicate absResult = new AbsPredicate(BasicOntology.RESULT);
  			absResult.set(BasicOntology.RESULT_ACTION, (AbsAgentAction) referenceOnto.fromObject(((Result) obj).getAction()));
  			absResult.set(BasicOntology.RESULT_ITEMS, (AbsAggregate) referenceOnto.fromObject(((Result) obj).getItems()));
				return absResult;
	    }

	    if(obj instanceof Equals) {
	    	AbsPredicate absEquals = new AbsPredicate(BasicOntology.EQUALS);
  			absEquals.set(BasicOntology.EQUALS_LEFT, (AbsTerm) referenceOnto.fromObject(((Equals) obj).getLeft()));
  			absEquals.set(BasicOntology.EQUALS_RIGHT, (AbsTerm) referenceOnto.fromObject(((Equals) obj).getRight()));
				return absEquals;
	    }

	    if (obj instanceof Action) {
	    	AbsAgentAction absAction = new AbsAgentAction(BasicOntology.ACTION);
	    	((Action) obj).externalise(absAction, referenceOnto);
	    	return absAction;
	    }
	    			
	    if (obj instanceof ACLMessage) {
				return AbsHelper.externaliseACLMessage((ACLMessage)obj, referenceOnto);
	    }
	    			
      throw new UnknownSchemaException();
    } 
    catch (OntologyException oe) {
      // Forward the exception
      throw oe;
    } 
    catch (Throwable t) {
      throw new OntologyException("Unexpected error externalising "+obj+".", t);
    }
	}
  
}
