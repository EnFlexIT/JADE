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

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import jade.core.AID;
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
  public static final String CONTAINERBORN = "container-born";
  public static final String CONTAINERDEAD = "container-dead";
  public static final String AGENTBORN = "agent-born";
  public static final String AGENTDEAD = "agent-dead";
  public static final String AGENTMOVED = "agent-moved";
  public static final String NEWMTP = "new-mtp";
  public static final String DEADMTP = "dead-mtp";
  
  // Actions supported by the ams
  public static final String KILLCONTAINER = "kill-container";
  public static final String CREATEAGENT = "create-agent";
  public static final String KILLAGENT = "kill-agent";
  public static final String INSTALLMTP = "install-mtp";
  public static final String UNINSTALLMTP = "uninstall-mtp";
  public static final String SNIFFON = "sniff-on";
  public static final String SNIFFOFF = "sniff-off";

  //actions supported by the DF
  public static final String SHOWGUI = "showgui";

  // Predicates
  public static final String EVENTOCCURRED = "event-occurred";
  //public static final String DONE = "done";
  //public static final String RESULT = "result";

  
  
  

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
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, KillContainer.class);

	theInstance.addRole(CREATEAGENT, new SlotDescriptor[] {	  
	  new SlotDescriptor("agent-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("class-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("arguments",Ontology.SET_SLOT,Ontology.ANY_TYPE, Ontology.O),	
	  new SlotDescriptor("container-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, CreateAgent.class);

	theInstance.addRole(KILLAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, KillAgent.class);

	theInstance.addRole(INSTALLMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("container", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("class-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, InstallMTP.class);

	theInstance.addRole(UNINSTALLMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("container", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
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
	   
	theInstance.addRole(SHOWGUI, new SlotDescriptor[] {
	}, ShowGui.class);
	   
	theInstance.addRole(EVENTOCCURRED, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.FRAME_SLOT, DefaultOntology.ANY_TYPE, Ontology.M)
	}, EventOccurred.class);

	theInstance.addRole(CONTAINERBORN, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("host", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, ContainerBorn.class); 

	theInstance.addRole(CONTAINERDEAD, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, ContainerDead.class); 

	theInstance.addRole(AGENTBORN, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M)
	}, AgentBorn.class); 

	theInstance.addRole(AGENTDEAD, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M)
	}, AgentDead.class); 

	theInstance.addRole(AGENTMOVED, new SlotDescriptor[] {
	  new SlotDescriptor("from", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("to", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M)
	}, AgentMoved.class);

	theInstance.addRole(NEWMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("where", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, NewMTP.class);

	theInstance.addRole(DEADMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("where", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, DeadMTP.class);

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance



}
