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


package jade.domain.JADEAgentManagement;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.core.AID;
import jade.core.ContainerID;
import jade.content.onto.*;
import jade.content.schema.*;
import jade.domain.FIPAAgentManagement.*;

/**
   This class represents the ontology
   <code>jade-agent-management</code>, containing all JADE extensions
   related to agent management. There is only a single instance of
   this class.
   <p>
   The package contains one class for each element in the ontology.
   <p>
   @author Giovanni Caire -  TILAB
*/
public class JADEManagementOntology extends Ontology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "JADE-Agent-Management";

  public static final String AGENTIDENTIFIER = "agent-identifier";
  
  // Concepts
  public static final String CONTAINERID = "container-ID";
  public static final String CONTAINERID_NAME = "name";
  public static final String CONTAINERID_ADDRESS = "address";

  // Actions supported by the ams
  public static final String KILLCONTAINER = "kill-container";
  public static final String KILLCONTAINER_CONTAINER = "container";
  public static final String KILLCONTAINER_PASSWORD = "password";
  
	public static final String CREATEAGENT = "create-agent";
	public static final String CREATEAGENT_AGENT_NAME = "agent-name";
	public static final String CREATEAGENT_CLASS_NAME = "class-name";
	public static final String CREATEAGENT_ARGUMENTS = "arguments";
	public static final String CREATEAGENT_CONTAINER = "container";
	public static final String CREATEAGENT_DELEGATION = "delegation";
	public static final String CREATEAGENT_PASSWORD = "password";
	
  public static final String KILLAGENT = "kill-agent";
  public static final String KILLAGENT_AGENT = "agent";
  public static final String KILLAGENT_PASSWORD = "password";

  public static final String INSTALLMTP = "install-mtp";
  public static final String INSTALLMTP_ADDRESS = "address";
  public static final String INSTALLMTP_CONTAINER = "container";
  public static final String INSTALLMTP_CLASS_NAME = "class-name";

  public static final String UNINSTALLMTP = "uninstall-mtp";
  public static final String UNINSTALLMTP_ADDRESS = "address";
  public static final String UNINSTALLMTP_CONTAINER = "container";

  public static final String SNIFFON = "sniff-on";
  public static final String SNIFFON_SNIFFER = "sniffer";
  public static final String SNIFFON_SNIFFED_AGENTS = "sniffed-agents";
  public static final String SNIFFON_PASSWORD = "password";

  public static final String SNIFFOFF = "sniff-off";
  public static final String SNIFFOFF_SNIFFER = "sniffer";
  public static final String SNIFFOFF_SNIFFED_AGENTS = "sniffed-agents";
  public static final String SNIFFOFF_PASSWORD = "password";

  public static final String DEBUGON = "debug-on";
  public static final String DEBUGON_DEBUGGER = "debugger";
  public static final String DEBUGON_DEBUGGED_AGENTS = "debugged-agents";
  public static final String DEBUGON_PASSWORD = "password";

  public static final String DEBUGOFF = "debug-off";
  public static final String DEBUGOFF_DEBUGGER = "debugger";
  public static final String DEBUGOFF_DEBUGGED_AGENTS = "debugged-agents";
  public static final String DEBUGOFF_PASSWORD = "password";

  // actions supported by the DF
  public static final String SHOWGUI = "showgui";

  // Exception Predicates
  public static final String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NOTREGISTERED;
  
  public static final String INTERNALERROR = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.INTERNALERROR;
  public static final String INTERNALERROR_MESSAGE = "_0";

  public static final String UNSUPPORTEDVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNSUPPORTEDVALUE;
  public static final String UNSUPPORTEDVALUE_VALUE = "_0";

  public static final String UNRECOGNISEDVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNRECOGNISEDVALUE;
  public static final String UNRECOGNISEDVALUE_VALUE = "_0";

  public static final String UNSUPPORTEDFUNCTION = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNSUPPORTEDFUNCTION;
  public static final String UNSUPPORTEDFUNCTION_FUNCTION = "_0";

  public static final String MISSINGPARAMETER = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.MISSINGPARAMETER;
  public static final String MISSINGPARAMETER_OBJECT_NAME = "_0";
  public static final String MISSINGPARAMETER_PARAMETER_NAME = "_1";

  public static final String UNEXPECTEDPARAMETER = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNEXPECTEDPARAMETER;
  public static final String UNEXPECTEDPARAMETER_OBJECT_NAME = "_0";
  public static final String UNEXPECTEDPARAMETER_PARAMETER_NAME = "_1";

  public static final String UNRECOGNISEDPARAMETERVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNRECOGNISEDPARAMETERVALUE;
  public static final String UNRECOGNISEDPARAMETERVALUE_OBJECT_NAME = "_0";
  public static final String UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME = "_1";


  // The singleton instance of this ontology
	private static Ontology theInstance = new JADEManagementOntology();
	
  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  private JADEManagementOntology() {
  	super(NAME, BasicOntology.getInstance(), new BCReflectiveIntrospector());

    try {
    	// Concepts definitions
    	add(new ConceptSchema(CONTAINERID), ContainerID.class);
    	
    	// AgentActions definitions
    	add(new AgentActionSchema(KILLCONTAINER), KillContainer.class);
    	add(new AgentActionSchema(CREATEAGENT), CreateAgent.class);
    	add(new AgentActionSchema(KILLAGENT), KillAgent.class);
    	add(new AgentActionSchema(INSTALLMTP), InstallMTP.class);
    	add(new AgentActionSchema(UNINSTALLMTP), UninstallMTP.class);
    	add(new AgentActionSchema(SNIFFON), SniffOn.class);
    	add(new AgentActionSchema(SNIFFOFF), SniffOff.class);
    	add(new AgentActionSchema(DEBUGON), DebugOn.class);
    	add(new AgentActionSchema(DEBUGOFF), DebugOff.class);
    	add(new AgentActionSchema(SHOWGUI), ShowGui.class);

    	// Predicates definitions
    	add(new PredicateSchema(UNSUPPORTEDVALUE), UnsupportedValue.class);
    	add(new PredicateSchema(UNRECOGNISEDVALUE), UnrecognisedValue.class);
    	add(new PredicateSchema(UNSUPPORTEDFUNCTION), UnsupportedFunction.class);
    	add(new PredicateSchema(MISSINGPARAMETER), MissingParameter.class);
    	add(new PredicateSchema(UNEXPECTEDPARAMETER), UnexpectedParameter.class);
    	add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE), UnrecognisedParameterValue.class);
    	add(new PredicateSchema(NOTREGISTERED), NotRegistered.class);
    	add(new PredicateSchema(INTERNALERROR), jade.domain.FIPAAgentManagement.InternalError.class);
    	
    	// Slots definitions
    	ConceptSchema cs = (ConceptSchema) getSchema(CONTAINERID);
    	cs.add(CONTAINERID_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	cs.add(CONTAINERID_ADDRESS, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	AgentActionSchema as = (AgentActionSchema) getSchema(KILLCONTAINER);
    	as.add(KILLCONTAINER_CONTAINER, (ConceptSchema) getSchema(CONTAINERID)); 
    	as.add(KILLCONTAINER_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(CREATEAGENT);
    	as.add(CREATEAGENT_AGENT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	as.add(CREATEAGENT_CLASS_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	as.add(CREATEAGENT_ARGUMENTS, (TermSchema) TermSchema.getBaseSchema(), 0, ObjectSchema.UNLIMITED); 
    	as.add(CREATEAGENT_CONTAINER, (ConceptSchema) getSchema(CONTAINERID));
    	as.add(CREATEAGENT_DELEGATION, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	as.add(CREATEAGENT_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(KILLAGENT);
    	as.add(KILLAGENT_AGENT, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(KILLAGENT_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 

    	as = (AgentActionSchema) getSchema(INSTALLMTP);
    	as.add(INSTALLMTP_ADDRESS, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	as.add(INSTALLMTP_CONTAINER, (ConceptSchema) getSchema(CONTAINERID)); 
    	as.add(INSTALLMTP_CLASS_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
    	as = (AgentActionSchema) getSchema(UNINSTALLMTP);
    	as.add(UNINSTALLMTP_ADDRESS, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	as.add(UNINSTALLMTP_CONTAINER, (ConceptSchema) getSchema(CONTAINERID)); 
    	
    	as = (AgentActionSchema) getSchema(SNIFFON);
    	as.add(SNIFFON_SNIFFER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(SNIFFON_SNIFFED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(SNIFFON_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(SNIFFOFF);
    	as.add(SNIFFOFF_SNIFFER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(SNIFFOFF_SNIFFED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(SNIFFOFF_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(DEBUGON);
    	as.add(DEBUGON_DEBUGGER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(DEBUGON_DEBUGGED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(DEBUGON_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
    	as = (AgentActionSchema) getSchema(DEBUGOFF);
    	as.add(DEBUGOFF_DEBUGGER, (ConceptSchema) getSchema(BasicOntology.AID)); 
    	as.add(DEBUGOFF_DEBUGGED_AGENTS, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED); 
    	as.add(DEBUGOFF_PASSWORD, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 
    	
			PredicateSchema ps = (PredicateSchema) getSchema(UNSUPPORTEDVALUE);
    	ps.add(UNSUPPORTEDVALUE_VALUE, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
			ps = (PredicateSchema) getSchema(UNRECOGNISEDVALUE);
    	ps.add(UNRECOGNISEDVALUE_VALUE, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
			ps = (PredicateSchema) getSchema(UNSUPPORTEDFUNCTION);
    	ps.add(UNSUPPORTEDFUNCTION_FUNCTION, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
			ps = (PredicateSchema) getSchema(MISSINGPARAMETER);
    	ps.add(MISSINGPARAMETER_OBJECT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	ps.add(MISSINGPARAMETER_PARAMETER_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
			ps = (PredicateSchema) getSchema(UNEXPECTEDPARAMETER);
    	ps.add(UNEXPECTEDPARAMETER_OBJECT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	ps.add(UNEXPECTEDPARAMETER_PARAMETER_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
			ps = (PredicateSchema) getSchema(UNRECOGNISEDPARAMETERVALUE);
    	ps.add(UNRECOGNISEDPARAMETERVALUE_OBJECT_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	ps.add(UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING)); 
    	
			ps = (PredicateSchema) getSchema(INTERNALERROR);
    	ps.add(INTERNALERROR_MESSAGE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL); 

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } 
}
