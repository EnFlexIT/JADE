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
import jade.content.abs.*;
import examples.content.musicShopOntology.*;

/**
 * Ontology containing concepts and predicates used for testing 
 * the CL and ontology support.
 * @author Giovanni Caire - TILAB
 */
public class MultipleInheritanceOntology extends Ontology {
	// NAME
  public static final String ONTOLOGY_NAME = "Inheritance-ontology";
	
	// VOCABULARY
  public static final String HOUSE = "HOUSE";
  public static final String HOUSE_ROOMS = "rooms";
  
  public static final String VEHICLE = "VEHICLE";
  public static final String VEHICLE_MAXSPEED = "max-speed";
  
  public static final String MOTORVEHICLE = "MOTORVEHICLE";
  public static final String MOTORVEHICLE_MECPIECES = "mec-pieces";
  
  public static final String CAMPER = "CAMPER";
  
  // The singleton instance of this ontology
	private static Ontology theInstance = new MultipleInheritanceOntology();
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  private MultipleInheritanceOntology() {
  	super(ONTOLOGY_NAME, TestOntology.getInstance(), new ReflectiveIntrospector());

    try {
    	add(new ConceptSchema(HOUSE), House.class);
    	add(new ConceptSchema(VEHICLE), Vehicle.class);
    	add(new ConceptSchema(MOTORVEHICLE), MotorVehicle.class);
    	add(new ConceptSchema(CAMPER), Camper.class);
    	
    	ConceptSchema cs = (ConceptSchema) getSchema(HOUSE);
    	cs.add(HOUSE_ROOMS, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
    	
    	cs = (ConceptSchema) getSchema(VEHICLE);
    	cs.add(VEHICLE_MAXSPEED, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
    	
    	cs = (ConceptSchema) getSchema(MOTORVEHICLE);
    	cs.addSuperSchema((ConceptSchema) getSchema(VEHICLE));
    	cs.add(MOTORVEHICLE_MECPIECES, (AggregateSchema) getSchema(BasicOntology.SET));
    	
    	cs = (ConceptSchema) getSchema(CAMPER);
    	cs.addSuperSchema((ConceptSchema) getSchema(HOUSE));
    	cs.addSuperSchema((ConceptSchema) getSchema(MOTORVEHICLE));
    } 
    catch (OntologyException oe) {
    	oe.printStackTrace();
    } 
	}

}
