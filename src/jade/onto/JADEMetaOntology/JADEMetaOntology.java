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


package jade.onto.JADEMetaOntology;

import jade.core.AID;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.SlotDescriptor;
import jade.onto.OntologyException;


import java.util.Map;
import java.util.HashMap;
/**
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the meta-ontology used by JADE. 
   There is only a single instance of this class.
   <p>
   The <code>jade.onto.JadeMetaOntology</code> package contains one class for 
   each role in this ontology.
   <p>
 */
public class JADEMetaOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "JADE-meta-ontology";

  private static Ontology theInstance = new DefaultOntology();

   // Concepts 
  public static final String ANONTOLOGY = "AnOntology";
  public static final String ROLE = "Role";
  public static final String SLOT = "Slot";
	

  /**
     This method grants access to the unique instance of the
     basic ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the basic ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private JADEMetaOntology() {
  }

  static { 
    initInstance();
  }

  private static void initInstance() {
    try {
      theInstance.addRole(ANONTOLOGY,
	new SlotDescriptor[] { //the slots must have no name otherwise SL-0 complains
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor(Ontology.SET_SLOT, ROLE, Ontology.M)
	    }, AnOntology.class);

      theInstance.addRole(ROLE,
	new SlotDescriptor[] {
  	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("className", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("slots", Ontology.SET_SLOT, SLOT, Ontology.O)
	    }, Role.class);

      theInstance.addRole(SLOT,
	new SlotDescriptor[] {
  	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("category", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.M),
	  new SlotDescriptor("type", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("presence", Ontology.PRIMITIVE_SLOT, Ontology.BOOLEAN_TYPE, Ontology.M)
	    }, Slot.class);
    }	// End of try
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
