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
import jade.onto.basic.*;  // to import Done, Action, ...
import jade.onto.*;

import jade.lang.Codec;

/**
   
   @author Giovanni Rimassa -  Universita` di Parma
   @version $Date$ $Revision$
*/

/**
   This class represents the ontology
   <code>jade-agent-management</code>, containing all JADE extensions
   related to agent management. There is only a single instance of
   this class.
   <p>
   The package contains one class for each Frame in the ontology.
   <p>
*/
public class JADEAgentManagementOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "JADE-Agent-Management";

  private static Ontology theInstance = new DefaultOntology();

  // Concepts
  public static final String AGENTIDENTIFIER = "agent-identifier";
  public static final String CONTAINERID = "container-ID";

  // Actions supported by the ams
  public static final String KILLCONTAINER = "kill-container";
  public static final String CREATEAGENT = "create-agent";
  public static final String KILLAGENT = "kill-agent";
  public static final String INSTALLMTP = "install-mtp";
  public static final String UNINSTALLMTP = "uninstall-mtp";
  public static final String SNIFFON = "sniff-on";
  public static final String SNIFFOFF = "sniff-off";
  public static final String DEBUGON = "debug-on";
  public static final String DEBUGOFF = "debug-off";

  //actions supported by the DF
  public static final String SHOWGUI = "showgui";

  // Exception Predicates
  public static final String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.NOTREGISTERED;
  public static final String INTERNALERROR = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.INTERNALERROR;
  public static final String UNSUPPORTEDVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNSUPPORTEDVALUE;
  public static final String UNRECOGNISEDVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNRECOGNISEDVALUE;
  public static final String UNSUPPORTEDFUNCTION = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNSUPPORTEDFUNCTION;
  public static final String MISSINGPARAMETER = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.MISSINGPARAMETER;
  public static final String UNEXPECTEDPARAMETER = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNEXPECTEDPARAMETER;
  public static final String UNRECOGNISEDPARAMETERVALUE = jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology.UNRECOGNISEDPARAMETERVALUE;


  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private JADEAgentManagementOntology() {
  }

  static { 
    initInstance();
  }

  private static void initInstance() {
    try {
	  // Adds the roles of the basic ontology (ACTION, AID,...)
    	theInstance.joinOntology(BasicOntology.instance());

	theInstance.addRole(KILLCONTAINER, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, KillContainer.class);

	theInstance.addRole(CREATEAGENT, new SlotDescriptor[] {	  
	  new SlotDescriptor("agent-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("class-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("arguments",Ontology.SET_SLOT,Ontology.ANY_TYPE, Ontology.O),	
	  new SlotDescriptor("container", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M),
	  new SlotDescriptor("delegation", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, CreateAgent.class);

	theInstance.addRole(KILLAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, KillAgent.class);

	theInstance.addRole(INSTALLMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("container", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M),
	  new SlotDescriptor("class-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, InstallMTP.class);

	theInstance.addRole(UNINSTALLMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("container", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M),
	}, UninstallMTP.class);

	theInstance.addRole(SNIFFON, new SlotDescriptor[] {
	  new SlotDescriptor("sniffer", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("sniffed-agents", Ontology.SEQUENCE_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, SniffOn.class);

	theInstance.addRole(SNIFFOFF, new SlotDescriptor[] {
	  new SlotDescriptor("sniffer", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("sniffed-agents", Ontology.SEQUENCE_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, SniffOff.class);

	theInstance.addRole(DEBUGON, new SlotDescriptor[] {
	  new SlotDescriptor("debugger", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("debugged-agents", Ontology.SEQUENCE_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, DebugOn.class);

	theInstance.addRole(DEBUGOFF, new SlotDescriptor[] {
	  new SlotDescriptor("debugger", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("debugged-agents", Ontology.SEQUENCE_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, DebugOff.class);

	theInstance.addRole(SHOWGUI, new SlotDescriptor[] {
	}, ShowGui.class);

	theInstance.addRole(CONTAINERID, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, ContainerID.class);

	theInstance.addRole(UNSUPPORTEDVALUE, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, jade.domain.FIPAAgentManagement.UnsupportedValue.class); 

	theInstance.addRole(UNRECOGNISEDVALUE, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, jade.domain.FIPAAgentManagement.UnrecognisedValue.class); 

	theInstance.addRole(UNSUPPORTEDFUNCTION, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, jade.domain.FIPAAgentManagement.UnsupportedFunction.class);

	theInstance.addRole(MISSINGPARAMETER, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M), 
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, jade.domain.FIPAAgentManagement.MissingParameter.class); 

	theInstance.addRole(UNEXPECTEDPARAMETER, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M), 
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, jade.domain.FIPAAgentManagement.UnexpectedParameter.class); 

	theInstance.addRole(UNRECOGNISEDPARAMETERVALUE, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M), 
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M) 
	}, jade.domain.FIPAAgentManagement.UnrecognisedParameterValue.class); 

	theInstance.addRole(NOTREGISTERED, new SlotDescriptor[] {
	}, jade.domain.FIPAAgentManagement.NotRegistered.class); 

	theInstance.addRole(INTERNALERROR, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O) 
	}, jade.domain.FIPAAgentManagement.InternalError.class); 
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance



}
