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

import jade.domain.FIPAAgentManagement.DonePredicate;
import jade.domain.FIPAAgentManagement.ResultPredicate;

import jade.lang.Codec;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.TermDescriptor;
import jade.onto.RoleFactory;
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

  private JADEAgentManagementOntology() {
  }

  private static void initInstance() {
    try {
	theInstance.addFrame(DefaultOntology.NAME_OF_ACTION_FRAME, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor(Ontology.FRAME_TERM, Ontology.ANY_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new jade.onto.Action(); }
	     public Class getClassForRole() { return jade.onto.Action.class; }
	   });

	theInstance.addFrame(DONE, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, DefaultOntology.NAME_OF_ACTION_FRAME, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) {return new DonePredicate(); }
	     public Class getClassForRole() {return DonePredicate.class;}
	   });

	theInstance.addFrame(RESULT, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, DefaultOntology.NAME_OF_ACTION_FRAME, Ontology.M),
	  new TermDescriptor(Ontology.ANY_TERM, Ontology.ANY_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) {return new ResultPredicate(); }
	     public Class getClassForRole() {return ResultPredicate.class;}
	   });

	theInstance.addFrame(AGENTIDENTIFIER, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("addresses", Ontology.SEQUENCE_TERM, Ontology.STRING_TYPE, Ontology.O),
	  new TermDescriptor("resolvers", Ontology.SEQUENCE_TERM, AGENTIDENTIFIER, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AID(); }
	     public Class getClassForRole() { return AID.class; }
	   });

	theInstance.addFrame(KILLCONTAINER, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("password", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new KillContainer(); } 
	     public Class getClassForRole() { return KillContainer.class; }
	   });

	theInstance.addFrame(CREATEAGENT, new TermDescriptor[] {	  
	  new TermDescriptor("agent-name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("class-name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("container-name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("password", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new CreateAgent(); } 
	     public Class getClassForRole() { return CreateAgent.class; }
	   });

	theInstance.addFrame(KILLAGENT, new TermDescriptor[] {
	  new TermDescriptor("agent", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor("password", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new KillAgent(); }
	     public Class getClassForRole() { return KillAgent.class; }
	   });

	theInstance.addFrame(SNIFFON, new TermDescriptor[] {
	  new TermDescriptor("sniffer", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor("sniffed-agents", Ontology.SEQUENCE_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor("password", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new SniffOn(); }
	     public Class getClassForRole() { return SniffOn.class; }
	   });

	theInstance.addFrame(SNIFFOFF, new TermDescriptor[] {
	  new TermDescriptor("sniffer", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor("sniffed-agents", Ontology.SEQUENCE_TERM, AGENTIDENTIFIER, Ontology.M),
	  new TermDescriptor("password", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.O)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new SniffOff(); }
	     public Class getClassForRole() { return SniffOff.class; }
	   });

	theInstance.addFrame(EVENTOCCURRED, new TermDescriptor[] {
	  new TermDescriptor(Ontology.FRAME_TERM, DefaultOntology.ANY_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) {return new EventOccurred(); }
	     public Class getClassForRole() {return EventOccurred.class;}
	   });

	theInstance.addFrame(CONTAINERBORN, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("host", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new ContainerBorn(); }
	     public Class getClassForRole() { return ContainerBorn.class; }
	   });

	theInstance.addFrame(CONTAINERDEAD, new TermDescriptor[] {
	  new TermDescriptor("name", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new ContainerDead(); }
	     public Class getClassForRole() { return ContainerDead.class; }
	   });

	theInstance.addFrame(AGENTBORN, new TermDescriptor[] {
	  new TermDescriptor("container", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("agent", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AgentBorn(); }
	     public Class getClassForRole() { return AgentBorn.class; }
	   });

	theInstance.addFrame(AGENTDEAD, new TermDescriptor[] {
	  new TermDescriptor("container", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("agent", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AgentDead(); }
	     public Class getClassForRole() { return AgentDead.class; }
	   });

	theInstance.addFrame(AGENTMOVED, new TermDescriptor[] {
	  new TermDescriptor("from", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("to", Ontology.CONSTANT_TERM, Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("agent", Ontology.FRAME_TERM, AGENTIDENTIFIER, Ontology.M)
	}, new RoleFactory() {
	     public Object create(Frame f) { return new AgentMoved(); }
	     public Class getClassForRole() { return AgentMoved.class; }
	   });

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
