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
package jade.content.lang.sl;

import jade.content.*;
import jade.content.onto.*;
import jade.content.schema.*;
import jade.core.CaseInsensitiveString;

/**
 * Ontology containing schemas for the SL2 language operators.
 * see jade.content.Ontology
 * @author Giovanni Caire - TILAB
 */
class SL2Ontology extends SL1Ontology implements SL2Vocabulary {
	// NAME
  public static final String ONTOLOGY_NAME = "SL2-ONTOLOGY";
	
  // The singleton instance of this ontology
	private static Ontology theInstance = new SL2Ontology(ONTOLOGY_NAME, SL1Ontology.getInstance());
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  protected SL2Ontology(String name, Ontology base) {
  	super(name, base);
  	
  	try {
    	add(VariableSchema.getBaseSchema(), absVariableClass);
  		add(new IRESchema(IOTA), absIREClass);
  		add(new IRESchema(ANY), absIREClass);
  		add(new IRESchema(ALL), absIREClass);
	  	add(new PredicateSchema(FORALL), absPredicateClass);
	  	add(new PredicateSchema(EXISTS), absPredicateClass);
	  	add(new PredicateSchema(BELIEF), absPredicateClass);
	  	add(new PredicateSchema(UNCERTAINTY), absPredicateClass);
	  	add(new PredicateSchema(PERSISTENT_GOAL), absPredicateClass);
	  	add(new PredicateSchema(INTENTION), absPredicateClass);
	  	add(new PredicateSchema(FEASIBLE), absPredicateClass);
  	
  		PredicateSchema ps = (PredicateSchema) getSchema(EXISTS);
  		ps.add(EXISTS_WHAT, (VariableSchema) VariableSchema.getBaseSchema());
	  	ps.add(EXISTS_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(FORALL);
  		ps.add(FORALL_WHAT, (VariableSchema) VariableSchema.getBaseSchema());
	  	ps.add(FORALL_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(BELIEF);
  		ps.add(BELIEF_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(BELIEF_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(UNCERTAINTY);
  		ps.add(UNCERTAINTY_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(UNCERTAINTY_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(PERSISTENT_GOAL);
  		ps.add(PERSISTENT_GOAL_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(PERSISTENT_GOAL_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(INTENTION);
  		ps.add(INTENTION_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(INTENTION_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(FEASIBLE);
  		ps.add(FEASIBLE_ACTION, (VariableSchema) VariableSchema.getBaseSchema());
	  	ps.add(FEASIBLE_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema(), ObjectSchema.OPTIONAL);
  	
    } 
    catch (OntologyException oe) {
      oe.printStackTrace();
    }   	
	}

	boolean isQuantifier(String symbol) {
		return (CaseInsensitiveString.equalsIgnoreCase(EXISTS, symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(FORALL, symbol));
	}
	
	boolean isModalOp(String symbol) {
		return (CaseInsensitiveString.equalsIgnoreCase(BELIEF, symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(UNCERTAINTY, symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(PERSISTENT_GOAL, symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(INTENTION, symbol));
	}
	
	boolean isActionOp(String symbol) {
		return (super.isActionOp(symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(FEASIBLE, symbol));
	}
	
	boolean isSLFunctionWithoutSlotNames(String symbol) {
		return (super.isSLFunctionWithoutSlotNames(symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(ACTION_SEQUENCE, symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(ACTION_ALTERNATIVE, symbol));
	}
}
