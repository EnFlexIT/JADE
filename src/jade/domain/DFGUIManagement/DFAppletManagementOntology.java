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


package jade.domain.DFGUIManagement;

import java.util.List;
import java.util.LinkedList;


import jade.onto.basic.*;  // to import Done, Action, ...
import jade.onto.*;
import jade.domain.FIPAAgentManagement.*;

/**
   
   @author Tiziana Trucco - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the ontology
   <code>DFApplet-management</code>, containing all JADE extensions
   related to applet management. There is only a single instance of
   this class.
   <p>
   The package contains one class for each Frame in the ontology.
   <p>
 
*/
public class DFAppletManagementOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "DFApplet-Management";

  private static Ontology theInstance = new DefaultOntology();

  //action supported by the df for the applet
  public static final String GETDEFAULTDESCRIPTION= "getdefaultdescription";
  public static final String FEDERATEWITH = "federatewith";
  public static final String GETPARENT = "getparent";
  public static final String GETDESCRIPTIONUSED = "getdescriptionused";
  public static final String DEREGISTERFROM = "deregisterfrom";
  public static final String REGISTERWITH = "registerwith";
  public static final String SEARCHON = "searchon";
  public static final String MODIFYON = "modifyon";

  
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

  private DFAppletManagementOntology() {
  }

  private static void initInstance() {
    try {
	  // Adds the roles of the basic ontology (ACTION, AID,...) 
    	theInstance.joinOntology(FIPAAgentManagementOntology.instance());

	     
	theInstance.addRole(GETDEFAULTDESCRIPTION, new SlotDescriptor[]{
	},GetDefaultDescription.class);  
	
	theInstance.addRole(FEDERATEWITH, new SlotDescriptor[]{
		new SlotDescriptor("parentdf", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
		new SlotDescriptor("childrendf", Ontology.FRAME_SLOT, FIPAAgentManagementOntology.DFAGENTDESCRIPTION,Ontology.M)	
	},Federate.class);
	
	theInstance.addRole(DEREGISTERFROM, new SlotDescriptor[]{
		new SlotDescriptor("parentdf", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
  	new SlotDescriptor("childrendf", Ontology.FRAME_SLOT, FIPAAgentManagementOntology.DFAGENTDESCRIPTION,Ontology.M)
	},DeregisterFrom.class);

	theInstance.addRole(REGISTERWITH, new SlotDescriptor[]{
		new SlotDescriptor("df", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
  	new SlotDescriptor("description", Ontology.FRAME_SLOT, FIPAAgentManagementOntology.DFAGENTDESCRIPTION,Ontology.M)
	},RegisterWith.class);

	theInstance.addRole(SEARCHON, new SlotDescriptor[]{
		new SlotDescriptor("df", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
  	new SlotDescriptor("description", Ontology.FRAME_SLOT, FIPAAgentManagementOntology.DFAGENTDESCRIPTION,Ontology.M),
  	new SlotDescriptor("constraints", Ontology.FRAME_SLOT, FIPAAgentManagementOntology.SEARCHCONSTRAINTS,Ontology.M)	

	},SearchOn.class);

	theInstance.addRole(GETPARENT, new SlotDescriptor[] {
	}, GetParent.class);
	   
  theInstance.addRole(GETDESCRIPTIONUSED, new SlotDescriptor[]{
		new SlotDescriptor("parentdf", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	},GetDescriptionUsed.class);
	
	theInstance.addRole(MODIFYON, new SlotDescriptor[]{
		new SlotDescriptor("df", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
  	new SlotDescriptor("description", Ontology.FRAME_SLOT, FIPAAgentManagementOntology.DFAGENTDESCRIPTION,Ontology.M)
	},ModifyOn.class);
	
  }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  
  } //end of initInstance

}