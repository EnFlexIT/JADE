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

package demo.MeetingScheduler.Ontology;

import jade.onto.*;
import jade.onto.basic.*;

public class MSOntology {
  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "Meeting-Scheduling-Ontology";

  public static final String PERSON = "Person";
  public static final String APPOINTMENT = "Appointment";

  private static Ontology theInstance = new DefaultOntology();


  static {
    initInstance();
  }


  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private static void initInstance() {
    try {
      // Adds the roles of the basic ontology (ACTION, AID,...)
      theInstance.joinOntology(BasicOntologyManager.instance());
      theInstance.addRole(PERSON, new SlotDescriptor[] {
	new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	new SlotDescriptor("AID", Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.O),
	new SlotDescriptor("DFName", Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.O)
	  }, new RoleEntityFactory() {
             public Object create(Frame f) { return new Person(); }
	     public Class getClassForRole() { return Person.class; }
	  });
      theInstance.addRole(APPOINTMENT, new SlotDescriptor[] {
	new SlotDescriptor("inviter", Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.M),
	new SlotDescriptor("description", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	new SlotDescriptor("starting-on", Ontology.PRIMITIVE_SLOT, Ontology.DATE_TYPE, Ontology.O),
	new SlotDescriptor("ending-with", Ontology.PRIMITIVE_SLOT, Ontology.DATE_TYPE, Ontology.O),
	new SlotDescriptor("fixed-date", Ontology.PRIMITIVE_SLOT, Ontology.DATE_TYPE, Ontology.O),
	new SlotDescriptor("invited-persons", Ontology.SET_SLOT, PERSON, Ontology.O),
	new SlotDescriptor("possible-dates", Ontology.SET_SLOT, Ontology.DATE_TYPE, Ontology.O)
	  }, new RoleEntityFactory() {
             public Object create(Frame f) { return new Appointment(); }
	     public Class getClassForRole() { return Appointment.class; }
	  });
    } catch (OntologyException oe) {
      oe.printStackTrace();
    }
  }

}




