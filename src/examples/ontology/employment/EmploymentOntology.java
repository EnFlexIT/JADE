/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package examples.ontology.employment;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.SlotDescriptor;
import jade.onto.OntologyException;

import jade.onto.basic.*;

import jade.util.leap.*;

/**
   Javadoc documentation for the file EmploymentOntology
   @author Giovanni Caire - CSELT S.p.A.
   @version $Date$ $Revision$
*/

public class EmploymentOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "employment-ontology";

  // VOCABULARY
  // Concepts
  public static final String ADDRESS = "ADDRESS";
  public static final String PERSON = "PERSON";
  public static final String COMPANY = "COMPANY";
  // Actions
  public static final String ENGAGE = "ENGAGE";
  // Predicates
  public static final String WORKS_FOR = "WORKS-FOR";
	// Propositions
  public static final String ENGAGEMENT_ERROR = "ENGAGEMENT-ERROR";
  public static final String PERSON_TOO_OLD = "PERSON-TOO-OLD";
  
  private static Ontology theInstance = new DefaultOntology();


  /**
     This method grants access to the unique instance of the
     employment ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the employment ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private EmploymentOntology() {
  }

  static { 
    initInstance();
  }

  private static void initInstance() {
    try {
			// Adds the roles of the basic ontology (ACTION, AID,...)
    	theInstance.joinOntology(BasicOntology.instance());
    	
			// Adds ADDRESS role
    	                theInstance.addRole(
				ADDRESS, 
				new SlotDescriptor[] {
	  			new SlotDescriptor("street", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  			new SlotDescriptor("number", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.M),
	  			new SlotDescriptor("city", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
				}, Address.class);

			// Adds PERSON role
	                theInstance.addRole(
				PERSON, 
				new SlotDescriptor[] {
	  			new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  			new SlotDescriptor("age", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O),
	  			new SlotDescriptor("address", Ontology.FRAME_SLOT, ADDRESS, Ontology.O)
				}, Person.class); 
	
			// Adds COMPANY role
	                theInstance.addRole(
				COMPANY, 
				new SlotDescriptor[]{
	  			new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  			new SlotDescriptor("address", Ontology.FRAME_SLOT, ADDRESS, Ontology.M)
				},  Company.class);

			// Adds WORKS_FOR role
			theInstance.addRole(
				WORKS_FOR, 
				new SlotDescriptor[]{
	  			new SlotDescriptor(Ontology.FRAME_SLOT, PERSON, Ontology.M),
	  			new SlotDescriptor(Ontology.FRAME_SLOT, COMPANY, Ontology.M)
				}, WorksFor.class);

			// Adds ENGAGE role
			theInstance.addRole(
				ENGAGE, 
				new SlotDescriptor[]{
	  			new SlotDescriptor(Ontology.FRAME_SLOT, PERSON, Ontology.M),
	  			new SlotDescriptor(Ontology.FRAME_SLOT, COMPANY, Ontology.M)
				}, Engage.class);
	
			// Adds PERSON_TO_OLD role
			theInstance.addRole(
				EmploymentOntology.PERSON_TOO_OLD, 
				new SlotDescriptor[]{
				}, PersonTooOld.class);

			// Adds ENGAGEMENT_ERROR role
			theInstance.addRole(
				EmploymentOntology.ENGAGEMENT_ERROR, 
				new SlotDescriptor[]{
				}, EngagementError.class);

			// DEBUG: PRINT VOCABULARY
	  	//List voc = theInstance.getVocabulary();
	  	//Iterator i = voc.iterator();
	  	//while (i.hasNext())
	  	//	System.out.println((String) (i.next()));
	  	
    }	// End of try
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance



}
