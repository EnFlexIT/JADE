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
  public static final String AGENTIDENTIFIER = "agent-identifier";
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
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String DONE = "done";
  public static final String RESULT = "result";

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
	theInstance.addFrame(Ontology.NAME_OF_ACTION_FRAME, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.ANY_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new jade.onto.Action(); }
	     public Class getClassForRole() { return jade.onto.Action.class; }
	   });

	theInstance.addFrame(AGENTIDENTIFIER, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("addresses", Ontology.SEQUENCE_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("resolvers", Ontology.SEQUENCE_TERM, AGENTIDENTIFIER, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AID(); }
	     public Class getClassForRole() { return AID.class; }
	   });

	theInstance.addFrame(DFAGENTDESCRIPTION, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
          new TermDescriptor("services", Ontology.SET_TERM, SERVICEDESCRIPTION, Ontology.O),
	  new TermDescriptor("protocols", Ontology.SET_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("ontologies", Ontology.SET_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("languages", Ontology.SET_TERM, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new DFAgentDescription(); }
	     public Class getClassForRole() { return DFAgentDescription.class; }
	   });

	theInstance.addFrame(SERVICEDESCRIPTION, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("type", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("ontologies", Ontology.SET_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("languages", Ontology.SET_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("protocols", Ontology.SET_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("ownership", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("properties", Ontology.SET_TERM, PROPERTY, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new ServiceDescription(); }
	     public Class getClassForRole() { return ServiceDescription.class;}
	   });

	theInstance.addFrame(SEARCHCONSTRAINTS, new TermDescriptor[] {
	  new TermDescriptor("max-depth", Ontology.CONSTANT_TERM, Ontology.LONG_TYPE, Ontology.O),
	  new TermDescriptor("max-results", Ontology.CONSTANT_TERM, Ontology.LONG_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new SearchConstraints(); }
	     public Class getClassForRole() { return SearchConstraints.class; }
	   });

	theInstance.addFrame(AMSAGENTDESCRIPTION, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor("ownership", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("state", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AMSAgentDescription();}
	     public Class getClassForRole() { return AMSAgentDescription.class; }
	   });

	theInstance.addFrame(APDESCRIPTION, new TermDescriptor[] {
	    new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	    new TermDescriptor("dynamic", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O),
	    new TermDescriptor("mobility", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O),
            new TermDescriptor("transport-profile", Ontology.FRAME_TERM, APTRANSPORTDESCRIPTION, Ontology.O),
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AgentPlatformDescription(); } 
	     public Class getClassForRole() { return AgentPlatformDescription.class; }
	   });

	theInstance.addFrame(APTRANSPORTDESCRIPTION, new TermDescriptor[] {
	    new TermDescriptor("available-mtps", Ontology.SET_TERM, MTPDESCRIPTION, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new APTransportDescription(); } 
	     public Class getClassForRole() { return APTransportDescription.class; }
	   });

	theInstance.addFrame(MTPDESCRIPTION, new TermDescriptor[] {
	    new TermDescriptor("profile", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O),
	    new TermDescriptor("mtp-name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O),
	    new TermDescriptor("addresses", Ontology.SEQUENCE_TERM, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MTPDescription(); } 
	     public Class getClassForRole() { return MTPDescription.class; }
	   });

	theInstance.addFrame(PROPERTY, new TermDescriptor[] {
	    new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	    new TermDescriptor("value", Ontology.ANY_TERM, Ontology.ANY_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Property(); } 
	     public Class getClassForRole() { return Property.class; }
	   });

	theInstance.addFrame(REGISTER, new TermDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.ANY_TYPE,Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Register(); } 
	     public Class getClassForRole() { return Register.class; }
	   });

	theInstance.addFrame(DEREGISTER, new TermDescriptor[] {	  
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.ANY_TYPE,Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Deregister(); } 
	     public Class getClassForRole() { return Deregister.class; }
	   });

	theInstance.addFrame(MODIFY, new TermDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.ANY_TYPE,Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Modify(); } 
	     public Class getClassForRole() { return Modify.class; }
	   });

	theInstance.addFrame(SEARCH, new TermDescriptor[] {
	  // This can both be a DFAgentDescription and an AMSAgentDescription
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.ANY_TYPE,Ontology.M), 
	  new TermDescriptor(Ontology.FRAME_TERM, SEARCHCONSTRAINTS, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Search(); } 
	     public Class getClassForRole() { return Search.class; }
	   });

	theInstance.addFrame(GETDESCRIPTION, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new GetDescription(); } 
	     public Class getClassForRole() { return GetDescription.class; }
	   });

	theInstance.addFrame(QUIT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Quit(); } 
	     public Class getClassForRole() { return Quit.class; }
	   });


	theInstance.addFrame(UNSUPPORTEDACT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnsupportedAct(); } 
	     public Class getClassForRole() { return UnsupportedAct.class; }
	   });

	theInstance.addFrame(UNEXPECTEDACT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnexpectedAct(); } 
	     public Class getClassForRole() { return UnexpectedAct.class; }
	   });

	theInstance.addFrame(UNSUPPORTEDVALUE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnsupportedValue(); } 
	     public Class getClassForRole() { return UnsupportedValue.class; }
	   });

	theInstance.addFrame(UNRECOGNISEDVALUE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnrecognisedValue(); } 
	     public Class getClassForRole() { return UnrecognisedValue.class; }
	   });

	theInstance.addFrame(UNAUTHORISED, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new Unauthorised(); } 
	     public Class getClassForRole() { return Unauthorised.class; }
	   });

	theInstance.addFrame(UNSUPPORTEDFUNCTION, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnsupportedFunction(); } 
	     public Class getClassForRole() { return UnsupportedFunction.class; }
	   });

	theInstance.addFrame(MISSINGARGUMENT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MissingArgument(); } 
	     public Class getClassForRole() { return MissingArgument.class; }
	   });

	theInstance.addFrame(UNEXPECTEDARGUMENT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnexpectedArgument(); }
	     public Class getClassForRole() { return UnexpectedArgument.class;}
	   });

	theInstance.addFrame(UNEXPECTEDARGUMENTCOUNT, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new UnexpectedArgumentCount(); }
	     public Class getClassForRole() { return UnexpectedArgumentCount.class;}
	   });

	theInstance.addFrame(MISSINGPARAMETER, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M), 
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new MissingParameter(); }
	     public Class getClassForRole() { return MissingParameter.class;}
	   });

	theInstance.addFrame(UNEXPECTEDPARAMETER, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M), 
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) {return new UnexpectedParameter(); }
	     public Class getClassForRole() {return UnexpectedParameter.class;}
	   });


	theInstance.addFrame(UNRECOGNISEDPARAMETERVALUE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M), 
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M) 
	}, new RoleFactory() {
	     public Object create(Frame f) {return new UnrecognisedParameterValue(); }
	     public Class getClassForRole() {return UnrecognisedParameterValue.class;}
	   });

	theInstance.addFrame(ALREADYREGISTERED, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AlreadyRegistered(); } 
	     public Class getClassForRole() { return AlreadyRegistered.class; }
	   });

	theInstance.addFrame(NOTREGISTERED, new TermDescriptor[] {
	}, new RoleFactory() {
	     public Object create(Frame f) { return new NotRegistered(); } 
	     public Class getClassForRole() { return NotRegistered.class; }
	   });

	theInstance.addFrame(INTERNALERROR, new TermDescriptor[] {
	  new TermDescriptor(Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O) 
	}, new RoleFactory() {
	     public Object create(Frame f) { return new InternalError(); } 
	     public Class getClassForRole() { return InternalError.class; }
	   });

	theInstance.addFrame(TRUE, new TermDescriptor[]{
	}, new RoleFactory() {
             public Object create(Frame f) { return new TrueProposition(); } 
	     public Class getClassForRole() { return TrueProposition.class; }
	});

	theInstance.addFrame(FALSE, new TermDescriptor[]{
	}, new RoleFactory() {
             public Object create(Frame f) { return new FalseProposition(); } 
	     public Class getClassForRole() { return FalseProposition.class; }
	});

	theInstance.addFrame(DONE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.NAME_OF_ACTION_FRAME, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) {return new DonePredicate(); }
	     public Class getClassForRole() {return DonePredicate.class;}
	   });

	theInstance.addFrame(RESULT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.NAME_OF_ACTION_FRAME, Ontology.M),
	  new TermDescriptor(Ontology.ANY_TERM, Ontology.ANY_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) {return new ResultPredicate(); }
	     public Class getClassForRole() {return ResultPredicate.class;}
	   });

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
