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


package jade.domain.FIPAAgentManagement;

import java.util.List;
import java.util.LinkedList;

import jade.lang.Codec;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.SlotDescriptor;
import jade.onto.RoleEntityFactory;
import jade.onto.OntologyException;

import jade.onto.basic.*;
import java.util.Iterator;

/**
   Javadoc documentation for the file
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the ontology defined by FIPA Agent Management 
   specifications (document no. 23). There is
   only a single instance of this class.
   <p>
   The package contains one class for each Frame in the ontology.
   <p>
   Notice that userDefinedslots will be parsed but ignored and not
   returned in the Java object. In order to get a userDefined Slot, a new
   Termdescriptor must be added to the Frame
   of this
   ontology and a new couple of set/get methods to the Java class representing
   that frame.
 */
public class FIPAAgentManagementOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "FIPA-Agent-Management";

  private static Ontology theInstance = new DefaultOntology();

  // Concepts
  //public static final String AGENTIDENTIFIER = "agent-identifier";
  public static final String DFAGENTDESCRIPTION = "df-agent-description";
  public static final String SERVICEDESCRIPTION = "service-description";
  public static final String SEARCHCONSTRAINTS = "search-constraints";
  public static final String AMSAGENTDESCRIPTION = "ams-agent-description";
  public static final String APDESCRIPTION = "ap-description";
  public static final String APTRANSPORTDESCRIPTION = "ap-transport-description";
  public static final String MTPDESCRIPTION = "mtp-description";
  public static final String PROPERTY = "property";

  // Actions
  public static final String REGISTER = "register";
  public static final String DEREGISTER = "deregister";
  public static final String MODIFY = "modify";
  public static final String SEARCH = "search";
  public static final String GETDESCRIPTION = "get-description";
  public static final String QUIT = "quit";

  // Not-understood Exception Propositions
  public static final String UNSUPPORTEDACT = "unsupported-act";
  public static final String UNEXPECTEDACT = "unexpected-act";
  public static final String UNSUPPORTEDVALUE = "unsupported-value";
  public static final String UNRECOGNISEDVALUE = "unrecognised-value";
  // Refusal Exception Propositions
  public static final String UNAUTHORISED = "unauthorised";
  public static final String UNSUPPORTEDFUNCTION = "unsupported-function";
  public static final String MISSINGARGUMENT = "missing-argument";
  public static final String UNEXPECTEDARGUMENT = "unexpected-argument";
  public static final String UNEXPECTEDARGUMENTCOUNT = "unexpected-argument-count";
  public static final String MISSINGPARAMETER = "missing-parameter";
  public static final String UNEXPECTEDPARAMETER = "unexpected-parameter";
	public static final String UNRECOGNISEDPARAMETERVALUE = "unrecognised-parameter-value";

  // Failure Exception Propositions
  public static final String ALREADYREGISTERED = "already-registered";
  public static final String NOTREGISTERED = "not-registered";
  public static final String INTERNALERROR = "internal-error";  

  // Other Propositions
  //public static final String TRUE = "true";
  //public static final String FALSE = "false";
  //public static final String DONE = "done";
  //public static final String RESULT = "result";

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

  private FIPAAgentManagementOntology() {
  }

  private static void initInstance() {
    try {
			// Adds the roles of the basic ontology (ACTION, AID,...)
    	theInstance.joinOntology(BasicOntologyManager.instance());
    	
	theInstance.addRole(DFAGENTDESCRIPTION, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.M),
          new SlotDescriptor("services", Ontology.SET_SLOT, SERVICEDESCRIPTION, Ontology.O),
	  new SlotDescriptor("protocols", Ontology.SET_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("ontologies", Ontology.SET_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("languages", Ontology.SET_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new DFAgentDescription(); }
	     public Class getClassForRole() { return DFAgentDescription.class; }
	   });

	theInstance.addRole(SERVICEDESCRIPTION, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("type", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("ontologies", Ontology.SET_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("languages", Ontology.SET_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("protocols", Ontology.SET_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("ownership", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("properties", Ontology.SET_SLOT, PROPERTY, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new ServiceDescription(); }
	     public Class getClassForRole() { return ServiceDescription.class;}
	   });

	theInstance.addRole(SEARCHCONSTRAINTS, new SlotDescriptor[] {
	  new SlotDescriptor("max-depth", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O),
	  new SlotDescriptor("max-results", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new SearchConstraints(); }
	     public Class getClassForRole() { return SearchConstraints.class; }
	   });

	theInstance.addRole(AMSAGENTDESCRIPTION, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("ownership", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("state", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new AMSAgentDescription();}
	     public Class getClassForRole() { return AMSAgentDescription.class; }
	   });

	theInstance.addRole(APDESCRIPTION, new SlotDescriptor[] {
	    new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	    new SlotDescriptor("dynamic", Ontology.PRIMITIVE_SLOT, Ontology.BOOLEAN_TYPE, Ontology.O),
	    new SlotDescriptor("mobility", Ontology.PRIMITIVE_SLOT, Ontology.BOOLEAN_TYPE, Ontology.O),
            new SlotDescriptor("transport-profile", Ontology.FRAME_SLOT, APTRANSPORTDESCRIPTION, Ontology.O),
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new APDescription(); } 
	     public Class getClassForRole() { return APDescription.class; }
	   });

	theInstance.addRole(APTRANSPORTDESCRIPTION, new SlotDescriptor[] {
	    new SlotDescriptor("available-mtps", Ontology.SET_SLOT, MTPDESCRIPTION, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new APTransportDescription(); } 
	     public Class getClassForRole() { return APTransportDescription.class; }
	   });

	theInstance.addRole(MTPDESCRIPTION, new SlotDescriptor[] {
	    new SlotDescriptor("profile", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	    new SlotDescriptor("mtp-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	    new SlotDescriptor("addresses", Ontology.SEQUENCE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new MTPDescription(); } 
	     public Class getClassForRole() { return MTPDescription.class; }
	   });

	theInstance.addRole(PROPERTY, new SlotDescriptor[] {
	    new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	    new SlotDescriptor("value", Ontology.ANY_SLOT, Ontology.ANY_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Property(); } 
	     public Class getClassForRole() { return Property.class; }
	   });

	theInstance.addRole(REGISTER, new SlotDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new SlotDescriptor(Ontology.FRAME_SLOT, Ontology.ANY_TYPE,Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Register(); } 
	     public Class getClassForRole() { return Register.class; }
	   });

	theInstance.addRole(DEREGISTER, new SlotDescriptor[] {	  
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new SlotDescriptor(Ontology.FRAME_SLOT, Ontology.ANY_TYPE,Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Deregister(); } 
	     public Class getClassForRole() { return Deregister.class; }
	   });

	theInstance.addRole(MODIFY, new SlotDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new SlotDescriptor(Ontology.FRAME_SLOT, Ontology.ANY_TYPE,Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Modify(); } 
	     public Class getClassForRole() { return Modify.class; }
	   });

	theInstance.addRole(SEARCH, new SlotDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new SlotDescriptor(Ontology.FRAME_SLOT, Ontology.ANY_TYPE,Ontology.M), 
	  new SlotDescriptor(Ontology.FRAME_SLOT, SEARCHCONSTRAINTS, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Search(); } 
	     public Class getClassForRole() { return Search.class; }
	   });

	theInstance.addRole(GETDESCRIPTION, new SlotDescriptor[] {
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new GetDescription(); } 
	     public Class getClassForRole() { return GetDescription.class; }
	   });

	theInstance.addRole(QUIT, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Quit(); } 
	     public Class getClassForRole() { return Quit.class; }
	   });


	theInstance.addRole(UNSUPPORTEDACT, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnsupportedAct(); } 
	     public Class getClassForRole() { return UnsupportedAct.class; }
	   });

	theInstance.addRole(UNEXPECTEDACT, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnexpectedAct(); } 
	     public Class getClassForRole() { return UnexpectedAct.class; }
	   });

	theInstance.addRole(UNSUPPORTEDVALUE, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnsupportedValue(); } 
	     public Class getClassForRole() { return UnsupportedValue.class; }
	   });

	theInstance.addRole(UNRECOGNISEDVALUE, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnrecognisedValue(); } 
	     public Class getClassForRole() { return UnrecognisedValue.class; }
	   });

	theInstance.addRole(UNAUTHORISED, new SlotDescriptor[] {
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new Unauthorised(); } 
	     public Class getClassForRole() { return Unauthorised.class; }
	   });

	theInstance.addRole(UNSUPPORTEDFUNCTION, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnsupportedFunction(); } 
	     public Class getClassForRole() { return UnsupportedFunction.class; }
	   });

	theInstance.addRole(MISSINGARGUMENT, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new MissingArgument(); } 
	     public Class getClassForRole() { return MissingArgument.class; }
	   });

	theInstance.addRole(UNEXPECTEDARGUMENT, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnexpectedArgument(); }
	     public Class getClassForRole() { return UnexpectedArgument.class;}
	   });

	theInstance.addRole(UNEXPECTEDARGUMENTCOUNT, new SlotDescriptor[] {
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new UnexpectedArgumentCount(); }
	     public Class getClassForRole() { return UnexpectedArgumentCount.class;}
	   });

	theInstance.addRole(MISSINGPARAMETER, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M), 
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new MissingParameter(); }
	     public Class getClassForRole() { return MissingParameter.class;}
	   });

	theInstance.addRole(UNEXPECTEDPARAMETER, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M), 
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) {return new UnexpectedParameter(); }
	     public Class getClassForRole() {return UnexpectedParameter.class;}
	   });


	theInstance.addRole(UNRECOGNISEDPARAMETERVALUE, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M), 
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) {return new UnrecognisedParameterValue(); }
	     public Class getClassForRole() {return UnrecognisedParameterValue.class;}
	   });

	theInstance.addRole(ALREADYREGISTERED, new SlotDescriptor[] {
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new AlreadyRegistered(); } 
	     public Class getClassForRole() { return AlreadyRegistered.class; }
	   });

	theInstance.addRole(NOTREGISTERED, new SlotDescriptor[] {
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new NotRegistered(); } 
	     public Class getClassForRole() { return NotRegistered.class; }
	   });

	theInstance.addRole(INTERNALERROR, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O) 
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new InternalError(); } 
	     public Class getClassForRole() { return InternalError.class; }
	   });

	   	   
	/** DEBUG: PRINT VOCABULARY
	  List voc = theInstance.getVocabulary();
	  Iterator i = voc.iterator();
	  while (i.hasNext())
	  	System.out.println((String) (i.next()));
		**/	
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
