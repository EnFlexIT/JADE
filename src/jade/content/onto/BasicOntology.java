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
  private static final BasicOntology theInstance;
  static {
  	theInstance = new BasicOntology();
  	theInstance.initialize();
  }
  
  // Primitive types names
 	public static final String         STRING = "String";
  public static final String         FLOAT = "Float";
  public static final String         INTEGER = "Integer";
  public static final String         BOOLEAN = "Boolean";
    
  // Aggregate types names
  public static final String         SEQUENCE = "Sequence";
  public static final String         SET = "Set";
    
  // Content element list 
  public static final String         CONTENT_ELEMENT_LIST = ContentElementListSchema.BASE_NAME;
  
  // Generic concepts: AID 
  public static final String         AID = AIDSchema.BASE_NAME;
  public static final String         AID_NAME = "Name";
  public static final String         AID_ADDRESSES = "Addresses";
  public static final String         AID_RESOLVERS = "Resolvers";

  // Generic propositions: TRUE_PROP (i.e. the proposition that is true under whatever condition) 
  public static final String         TRUE_PROPOSITION = "TRUEPROPOSITION";
  
  // Always required operators propositions
  public static final String         DONE = "DONE";
  public static final String         DONE_ACTION = "action";
    
  public static final String         RESULT = "RESULT";
  public static final String         RESULT_ACTION = "action";
  public static final String         RESULT_ITEMS = "items";
    
  public static final String         EQUALS = "EQUALS";
  public static final String         EQUALS_LEFT = "Left";
  public static final String         EQUALS_RIGHT = "Right";
  
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
      add(AIDSchema.getBaseSchema()); 
      
      // TRUE_PROPOSITION schema
      PredicateSchema truePropSchema = new PredicateSchema(TRUE_PROPOSITION);
      add(truePropSchema);

      // DONE Schema
      ActionPredicateSchema doneSchema = new ActionPredicateSchema(DONE);
      doneSchema.add(DONE_ACTION, (GenericActionSchema) GenericActionSchema.getBaseSchema());
      add(doneSchema); 
      
      // EQUALS Schema
      ActionPredicateSchema resultSchema = new ActionPredicateSchema(RESULT);
      resultSchema.add(RESULT_ACTION, (GenericActionSchema) GenericActionSchema.getBaseSchema());
      resultSchema.add(RESULT_ITEMS, (TermSchema) getSchema(SEQUENCE));
      add(resultSchema); 
      
      // EQUALS Schema
      PredicateSchema equalsSchema = new PredicateSchema(EQUALS);
      equalsSchema.add(EQUALS_LEFT, (TermSchema) TermSchema.getBaseSchema());
      equalsSchema.add(EQUALS_RIGHT, (TermSchema) TermSchema.getBaseSchema());
      add(equalsSchema); 
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
