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
package examples.content.ecommerceOntology;

import jade.content.onto.*;
import jade.content.schema.*;

/**
 * Ontology containing concepts related to buying/selling musical items.
 * @author Giovanni Caire - TILAB
 */
public class ECommerceOntology extends Ontology {
	// NAME
  public static final String ONTOLOGY_NAME = "E-Commerce-ontology";
	
	// VOCABULARY
  public static final String ITEM = "ITEM";
  public static final String ITEM_SERIALID = "serialID";
  
  public static final String OWNS = "OWNS";
  public static final String OWNS_OWNER = "owner";
  public static final String OWNS_ITEM = "item";
  
  public static final String SELL = "SELL";
  public static final String SELL_BUYER = "buyer";
  public static final String SELL_ITEM = "item";
  public static final String SELL_CARDNUMBER = "cardnumber";
  
  public static final String PRICE = "PRICE";
  public static final String PRICE_VALUE = "value";
  
  public static final String COSTS = "COSTS";
  public static final String COSTS_ITEM = "item";
  public static final String COSTS_PRICE = "price";
  
  // The singleton instance of this ontology
	private static Ontology theInstance = new ECommerceOntology(ACLOntology.getInstance());
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  private ECommerceOntology(Ontology base) {
  	super(ONTOLOGY_NAME, base, new ReflectiveIntrospector());

    try {
    	ConceptSchema itemSchema = new ConceptSchema(ITEM);
    	itemSchema.add(ITEM_SERIALID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL); 
    	add(itemSchema, Item.class);
    	
    	PredicateSchema ownsSchema = new PredicateSchema(OWNS);
    	ownsSchema.add(OWNS_OWNER, (TermSchema) getSchema(BasicOntology.AID));
    	ownsSchema.add(OWNS_ITEM, itemSchema);
    	add(ownsSchema, Owns.class);
    	
    	AgentActionSchema sellSchema = new AgentActionSchema(SELL);
    	sellSchema.add(SELL_BUYER, (TermSchema) getSchema(BasicOntology.AID));
    	sellSchema.add(SELL_ITEM, itemSchema); 
    	sellSchema.add(SELL_CARDNUMBER, (TermSchema) getSchema(BasicOntology.STRING)); 
    	add(sellSchema, Sell.class);
    	
    	ConceptSchema priceSchema = new ConceptSchema(PRICE);
    	priceSchema.add(PRICE_VALUE, (TermSchema) getSchema(BasicOntology.INTEGER));
    	add(priceSchema, Price.class);
    	
    	PredicateSchema costsSchema = new PredicateSchema(COSTS);
    	costsSchema.add(COSTS_ITEM, itemSchema);
    	costsSchema.add(COSTS_PRICE, priceSchema);
    	add(costsSchema, Costs.class);
    } 
    catch (OntologyException oe) {
    	oe.printStackTrace();
    } 
	}

}
