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
import jade.onto.TermDescriptor;
import jade.onto.RoleFactory;
import jade.onto.OntologyException;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A.
@version $Date$ $Revision$
*/

/**
   This class represents the ontology defined by FIPA Agent Management 
   specifications (document no. 23). There is
   only a single instance of this class.
 */
public class FIPAAgentManagementOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "FIPA-Agent-Management";

  private static Ontology theInstance = new DefaultOntology();

  // Concepts
  public static final String AGENTIDENTIFIER = "AID";
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
  public static final String MISSINGATTRIBUTE = "missing-attribute";
  public static final String UNEXPECTEDATTRIBUTE = "unexpected-attribute";
  public static final String UNRECOGNISEDATTRIBUTEVALUE = "unrecognised-attribute-value";
  // Failure Exception Propositions
  public static final String ALREADYREGISTERED = "already-registered";
  public static final String NOTREGISTERED = "not-registered";
  public static final String INTERNALERROR = "internal-error";  

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
	theInstance.addFrame(AGENTIDENTIFIER, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("addresses", Ontology.SEQUENCE_TYPE, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("resolvers", Ontology.SEQUENCE_TYPE, AGENTIDENTIFIER, Ontology.O)
	    //FIXME How can we deal with userDefinedSlots? Should we mind?
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AID(); }
	     public Class getClassForRole() { return AID.class; }
	   });

	theInstance.addFrame(DFAGENTDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.STRING_TYPE, Ontology.M),
          new TermDescriptor("services", Ontology.SET_TYPE, SERVICEDESCRIPTION, Ontology.O),
	  new TermDescriptor("protocols", Ontology.SET_TYPE, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("ontology", Ontology.SET_TYPE, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("language", Ontology.SET_TYPE, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new DFAgentDescription(); }
	     public Class getClassForRole() { return DFAgentDescription.class; }
	   });

	theInstance.addFrame(SERVICEDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("type", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("ontology", Ontology.SET_TYPE, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("protocols", Ontology.SET_TYPE, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("ownership", Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("properties", Ontology.SET_TYPE, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new ServiceDescription(); }
	     public Class getClassForRole() { return ServiceDescription.class;}
	   });

	theInstance.addFrame(SEARCHCONSTRAINTS, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("max-depth", Ontology.LONG_TYPE, Ontology.O),
	  new TermDescriptor("max-results", Ontology.LONG_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new SearchConstraints(); }
	     public Class getClassForRole() { return SearchConstraints.class; }
	   });

	theInstance.addFrame(AMSAGENTDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("ownership", Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("state", Ontology.STRING_TYPE, Ontology.M),
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AMSAgentDescription();}
	     public Class getClassForRole() { return AMSAgentDescription.class; }
	   });

	theInstance.addFrame(APDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	    new TermDescriptor("name", Ontology.STRING_TYPE, Ontology.M),
	    new TermDescriptor("dynamic", Ontology.STRING_TYPE, Ontology.O),
	    new TermDescriptor("mobility", Ontology.STRING_TYPE, Ontology.O),
            new TermDescriptor("transport-profile", Ontology.CONCEPT_TYPE, APTRANSPORTDESCRIPTION, Ontology.O),
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AgentPlatformDescription(); } 
	     public Class getClassForRole() { return AgentPlatformDescription.class; }
	   });

	theInstance.addFrame(APTRANSPORTDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	    new TermDescriptor("available-mtps", Ontology.SET_TYPE, MTPDESCRIPTION, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new APTransportDescription(); } 
	     public Class getClassForRole() { return APTransportDescription.class; }
	   });

	theInstance.addFrame(MTPDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	    new TermDescriptor("profile", Ontology.STRING_TYPE, Ontology.O),
	    new TermDescriptor("mtp-name", Ontology.STRING_TYPE, Ontology.O),
	    new TermDescriptor("addresses", Ontology.SEQUENCE_TYPE, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MTPDescription(); } 
	     public Class getClassForRole() { return MTPDescription.class; }
	   });

	theInstance.addFrame(PROPERTY, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	    new TermDescriptor("name", Ontology.STRING_TYPE, Ontology.M),
	    new TermDescriptor("value", Ontology.STRING_TYPE, Ontology.M) //FIXME it should be a Term and not a String
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Property(); } 
	     public Class getClassForRole() { return Property.class; }
	   });

	theInstance.addFrame(REGISTER, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.CONCEPT_TYPE, AMSAGENTDESCRIPTION, Ontology.O), 
	  new TermDescriptor(Ontology.CONCEPT_TYPE, DFAGENTDESCRIPTION, Ontology.O) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Register(); } 
	     public Class getClassForRole() { return Register.class; }
	   });

	theInstance.addFrame(DEREGISTER, Ontology.CONCEPT_TYPE, new TermDescriptor[] {	  
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.CONCEPT_TYPE, AMSAGENTDESCRIPTION, Ontology.O), 
	  new TermDescriptor(Ontology.CONCEPT_TYPE, DFAGENTDESCRIPTION, Ontology.O) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Deregister(); } 
	     public Class getClassForRole() { return Deregister.class; }
	   });

	theInstance.addFrame(MODIFY, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.CONCEPT_TYPE, AMSAGENTDESCRIPTION, Ontology.O), 
	  new TermDescriptor(Ontology.CONCEPT_TYPE, DFAGENTDESCRIPTION, Ontology.O) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Modify(); } 
	     public Class getClassForRole() { return Modify.class; }
	   });

	theInstance.addFrame(SEARCH, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.CONCEPT_TYPE, AMSAGENTDESCRIPTION, Ontology.O), 
	  new TermDescriptor(Ontology.CONCEPT_TYPE, DFAGENTDESCRIPTION, Ontology.O), 
	  new TermDescriptor(Ontology.CONCEPT_TYPE, SEARCHCONSTRAINTS, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Search(); } 
	     public Class getClassForRole() { return Search.class; }
	   });

	theInstance.addFrame(GETDESCRIPTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new GetDescription(); } 
	     public Class getClassForRole() { return GetDescription.class; }
	   });

	theInstance.addFrame(QUIT, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONCEPT_TYPE, AGENTIDENTIFIER, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Quit(); } 
	     public Class getClassForRole() { return Quit.class; }
	   });


	theInstance.addFrame(UNSUPPORTEDACT, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnsupportedAct(); } 
	     public Class getClassForRole() { return UnsupportedAct.class; }
	   });

	theInstance.addFrame(UNEXPECTEDACT, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnexpectedAct(); } 
	     public Class getClassForRole() { return UnexpectedAct.class; }
	   });

	theInstance.addFrame(UNSUPPORTEDVALUE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnsupportedValue(); } 
	     public Class getClassForRole() { return UnsupportedValue.class; }
	   });

	theInstance.addFrame(UNRECOGNISEDVALUE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnrecognisedValue(); } 
	     public Class getClassForRole() { return UnrecognisedValue.class; }
	   });

	theInstance.addFrame(UNAUTHORISED, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Unauthorised(); } 
	     public Class getClassForRole() { return Unauthorised.class; }
	   });

	theInstance.addFrame(UNSUPPORTEDFUNCTION, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnsupportedFunction(); } 
	     public Class getClassForRole() { return UnsupportedFunction.class; }
	   });

	theInstance.addFrame(MISSINGARGUMENT, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MissingArgument(); } 
	     public Class getClassForRole() { return MissingArgument.class; }
	   });

	theInstance.addFrame(UNEXPECTEDARGUMENT, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnexpectedArgument(); }
	     public Class getClassForRole() { return UnexpectedArgument.class;}
	   });

	theInstance.addFrame(UNEXPECTEDARGUMENTCOUNT, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnexpectedArgumentCount(); }
	     public Class getClassForRole() { return UnexpectedArgumentCount.class;}
	   });

	theInstance.addFrame(MISSINGATTRIBUTE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M), 
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MissingAttribute(); }
	     public Class getClassForRole() { return MissingAttribute.class;}
	   });

	theInstance.addFrame(UNEXPECTEDATTRIBUTE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M), 
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) {return new UnexpectedAttribute(); }
	     public Class getClassForRole() {return UnexpectedAttribute.class;}
	   });

	theInstance.addFrame(UNRECOGNISEDATTRIBUTEVALUE, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M), 
	  new TermDescriptor(Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) {return new UnrecognisedAttributeValue(); }
	     public Class getClassForRole() {return UnrecognisedAttributeValue.class;}
	   });

	theInstance.addFrame(ALREADYREGISTERED, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AlreadyRegistered(); } 
	     public Class getClassForRole() { return AlreadyRegistered.class; }
	   });

	theInstance.addFrame(NOTREGISTERED, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new NotRegistered(); } 
	     public Class getClassForRole() { return NotRegistered.class; }
	   });

	theInstance.addFrame(INTERNALERROR, Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new InternalError(); } 
	     public Class getClassForRole() { return InternalError.class; }
	   });

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
