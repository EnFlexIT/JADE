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
package test.content.testOntology;

import jade.content.onto.*;
import jade.content.schema.*;
import examples.content.musicShopOntology.*;

/**
 * Ontology containing concepts and predicates used for testing 
 * the CL and ontology support.
 * @author Giovanni Caire - TILAB
 */
public class TestOntology extends Ontology {
	// NAME
  public static final String ONTOLOGY_NAME = "Test-ontology";
	
	// VOCABULARY
  public static final String EXISTS = "TEST_EXISTS";
  public static final String EXISTS_WHAT = "what";

  public static final String POSITION = "POSITION";
  public static final String POSITION_X = "x";
  public static final String POSITION_Y = "y";
  
  public static final String MOVE = "MOVE";
  public static final String MOVE_DESTINATION = "destination";
  
  public static final String ROUTE = "ROUTE";
  public static final String ROUTE_ELEMENTS = "elements";
  public static final String ROUTE_EST_TIME = "estimated-time";
  
  // The singleton instance of this ontology
	private static Ontology theInstance = new TestOntology(MusicShopOntology.getInstance());
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  private TestOntology(Ontology base) {
  	super(ONTOLOGY_NAME, base, new BCReflectiveIntrospector());

    try {
    	add(new PredicateSchema(EXISTS), Exists.class);
    	add(new ConceptSchema(POSITION), Position.class);
    	add(new ConceptSchema(MOVE), Move.class);
    	add(new ConceptSchema(ROUTE), Route.class);
    	
    	PredicateSchema ps = (PredicateSchema) getSchema(EXISTS);
    	ps.add(EXISTS_WHAT, (ConceptSchema) ConceptSchema.getBaseSchema());

    	ConceptSchema cs = (ConceptSchema) getSchema(POSITION);
    	cs.add(POSITION_X, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
    	cs.add(POSITION_Y, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));

    	cs = (ConceptSchema) getSchema(MOVE);
    	cs.add(MOVE_DESTINATION, (ConceptSchema) getSchema(POSITION));
    	
    	cs = (ConceptSchema) getSchema(ROUTE);
    	cs.add(ROUTE_ELEMENTS, (ConceptSchema) getSchema(POSITION), 2, ObjectSchema.UNLIMITED);
    	cs.add(ROUTE_EST_TIME, (PrimitiveSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    } 
    catch (OntologyException oe) {
    	oe.printStackTrace();
    } 
	}

}
