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

import jade.core.AID;
import jade.onto.basic.*;  // to import Done, Action, ...
import jade.onto.*;

import jade.lang.Codec;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.TermDescriptor;
import jade.onto.RoleEntityFactory;
import jade.onto.OntologyException;

/**
   Javadoc documentation for the file
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

  // Actions
  public static final String KILLCONTAINER = "kill-container";
  public static final String CREATEAGENT = "create-agent";
  public static final String KILLAGENT = "kill-agent";
  public static final String SNIFFON = "sniff-on";
  public static final String SNIFFOFF = "sniff-off";

  // Predicates
  public static final String EVENTOCCURRED = "event-occurred";
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

  private JADEAgentManagementOntology() {
  }

  private static void initInstance() {
    try {
	  // Adds the roles of the basic ontology (ACTION, AID,...)
    	theInstance.joinOntology(BasicOntologyManager.instance());

	theInstance.addRole(KILLCONTAINER, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new KillContainer(); } 
	     public Class getClassForRole() { return KillContainer.class; }
	   });

	theInstance.addRole(CREATEAGENT, new SlotDescriptor[] {	  
	  new SlotDescriptor("agent-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("class-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("container-name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new CreateAgent(); } 
	     public Class getClassForRole() { return CreateAgent.class; }
	   });

	theInstance.addRole(KILLAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new KillAgent(); }
	     public Class getClassForRole() { return KillAgent.class; }
	   });

	theInstance.addRole(SNIFFON, new SlotDescriptor[] {
	  new SlotDescriptor("sniffer", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("sniffed-agents", Ontology.SEQUENCE_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new SniffOn(); }
	     public Class getClassForRole() { return SniffOn.class; }
	   });

	theInstance.addRole(SNIFFOFF, new SlotDescriptor[] {
	  new SlotDescriptor("sniffer", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("sniffed-agents", Ontology.SEQUENCE_SLOT, AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("password", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new SniffOff(); }
	     public Class getClassForRole() { return SniffOff.class; }
	   });

	theInstance.addRole(EVENTOCCURRED, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.FRAME_SLOT, DefaultOntology.ANY_TYPE, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) {return new EventOccurred(); }
	     public Class getClassForRole() {return EventOccurred.class;}
	   });

	theInstance.addRole(CONTAINERBORN, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("host", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new ContainerBorn(); }
	     public Class getClassForRole() { return ContainerBorn.class; }
	   });

	theInstance.addRole(CONTAINERDEAD, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new ContainerDead(); }
	     public Class getClassForRole() { return ContainerDead.class; }
	   });

	theInstance.addRole(AGENTBORN, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new AgentBorn(); }
	     public Class getClassForRole() { return AgentBorn.class; }
	   });

	theInstance.addRole(AGENTDEAD, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new AgentDead(); }
	     public Class getClassForRole() { return AgentDead.class; }
	   });

	theInstance.addRole(AGENTMOVED, new SlotDescriptor[] {
	  new SlotDescriptor("from", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("to", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, AGENTIDENTIFIER, Ontology.M)
	}, new RoleEntityFactory() {
	     public Object create(Frame f) { return new AgentMoved(); }
	     public Class getClassForRole() { return AgentMoved.class; }
	   });

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
