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
/*__J2ME_COMPATIBILITY__BEGIN
import jade.content.abs.*;
__J2ME_COMPATIBILITY__END*/

/**
 * Ontology containing schemas for the SL language operators.
 * see jade.content.Ontology
 * @author Giovanni Caire - TILAB
 */
class SLOntology 
//__CLDC_UNSUPPORTED__BEGIN
	extends SL2Ontology 
//__CLDC_UNSUPPORTED__END
/*__J2ME_COMPATIBILITY__BEGIN
	extends Ontology
__J2ME_COMPATIBILITY__END*/
	implements SLVocabulary {
		
	// NAME
  public static final String ONTOLOGY_NAME = "SL-ONTOLOGY";
	
  // The singleton instance of this ontology
//__CLDC_UNSUPPORTED__BEGIN
	private static Ontology theInstance = new SLOntology(ONTOLOGY_NAME, SL2Ontology.getInstance(), null);
//__CLDC_UNSUPPORTED__END
/*__J2ME_COMPATIBILITY__BEGIN
	private static Ontology theInstance = new SLOntology(ONTOLOGY_NAME, BasicOntology.getInstance(), null);
__J2ME_COMPATIBILITY__END*/
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  protected SLOntology(String name, Ontology base, Introspector intro) {
  	super(name, base, intro);

/*__J2ME_COMPATIBILITY__BEGIN
  	try {
			// Schemas for the SL1 operators
  		add(new PredicateSchema(AND), AbsPredicate.getJavaClass());
  		add(new PredicateSchema(OR), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(NOT), AbsPredicate.getJavaClass());
  	
  		PredicateSchema ps = (PredicateSchema) getSchema(AND);
  		ps.add(AND_LEFT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps.add(AND_RIGHT, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(OR);
  		ps.add(OR_LEFT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps.add(OR_RIGHT, (PredicateSchema) PredicateSchema.getBaseSchema());
  		
  		ps = (PredicateSchema) getSchema(NOT);
	  	ps.add(NOT_WHAT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  
	  
			// Schemas for the SL2 operators
    	add(VariableSchema.getBaseSchema(), AbsVariable.getJavaClass());
  		add(new IRESchema(IOTA), AbsIRE.getJavaClass());
  		add(new IRESchema(ANY), AbsIRE.getJavaClass());
  		add(new IRESchema(ALL), AbsIRE.getJavaClass());
	  	add(new PredicateSchema(FORALL), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(EXISTS), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(BELIEF), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(UNCERTAINTY), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(PERSISTENT_GOAL), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(INTENTION), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(FEASIBLE), AbsPredicate.getJavaClass());
	  	add(new AgentActionSchema(ACTION_SEQUENCE), AbsAgentAction.getJavaClass());
	  	add(new AgentActionSchema(ACTION_ALTERNATIVE), AbsAgentAction.getJavaClass());
  	
  		ps = (PredicateSchema) getSchema(EXISTS);
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
  	
  		AgentActionSchema as = (AgentActionSchema) getSchema(ACTION_SEQUENCE);
  		as.add(ACTION_SEQUENCE_FIRST, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.add(ACTION_SEQUENCE_SECOND, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.setEncodingByOrder(true);
  	
  		as = (AgentActionSchema) getSchema(ACTION_ALTERNATIVE);
  		as.add(ACTION_ALTERNATIVE_FIRST, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.add(ACTION_ALTERNATIVE_SECOND, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.setEncodingByOrder(true);
    } 
    catch (OntologyException oe) {
      oe.printStackTrace();
    } 
__J2ME_COMPATIBILITY__END*/
	}

}
