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


package jade.domain.introspection;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.core.AID;
import jade.core.AgentState;
import jade.core.BehaviourID;
import jade.core.ContainerID;
import jade.core.Channel;
import jade.core.event.*;

import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;

import jade.onto.basic.*;  // to import Done, Action, ...
import jade.onto.*;


/**
   This class represents the ontology <code>jade-introspection</code>,
   containing all JADE extensions related to agent and platform
   monitoring. There is only a single instance of this class.
   <p>
   The package contains one class for each Frame in the ontology.
   <p>

   @author Giovanni Rimassa -  Universita` di Parma
   @version $Date$ $Revision$

*/
public class JADEIntrospectionOntology {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "JADE-Introspection";

  private static Ontology theInstance = new DefaultOntology();

  // Concepts
  public static final String EVENTRECORD = "event-record";
  public static final String ADDEDCONTAINER = "added-container";
  public static final String REMOVEDCONTAINER = "removed-container";
  public static final String ADDEDMTP = "added-mtp";
  public static final String REMOVEDMTP = "removed-mtp";
  public static final String BORNAGENT = "born-agent";
  public static final String DEADAGENT = "dead-agent";
  public static final String SUSPENDEDAGENT = "suspended-agent";
  public static final String RESUMEDAGENT = "resumed-agent";
  public static final String CHANGEDAGENTOWNERSHIP = "changed-agent-ownership";
  public static final String MOVEDAGENT = "moved-agent";
  public static final String CHANGEDAGENTSTATE = "changed-agent-state";
  public static final String ADDEDBEHAVIOUR = "added-behaviour";
  public static final String REMOVEDBEHAVIOUR = "removed-behaviour";
  public static final String CHANGEDBEHAVIOURSTATE = "changed-behaviour-state";
  public static final String SENTMESSAGE = "sent-message";
  public static final String RECEIVEDMESSAGE = "received-message";
  public static final String POSTEDMESSAGE = "posted-message";
  public static final String ROUTEDMESSAGE = "routed-message";
  public static final String CONTAINERID = "container-ID";
  public static final String AGENTSTATE = "agent-state";
  public static final String BEHAVIOURID = "behaviour-ID";
  public static final String ACLMESSAGE = "acl-message";
  public static final String ENVELOPE = "envelope";
  public static final String RECEIVEDOBJECT = "received-object";
  public static final String CHANNEL = "channel";

  public static final String PLATFORMDESCRIPTION = "platform-description";
  
  // Actions
  public static final String STARTNOTIFY = "start-notify";
  public static final String STOPNOTIFY = "stop-notify";

  // Predicates
  public static final String OCCURRED = "occurred";




  /**
     This method grants access to the unique instance of the
     ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private JADEIntrospectionOntology() {
  }

  static { 
    initInstance();
  }

  private static void initInstance() {
    try {
	  // Adds the roles of the basic ontology (ACTION, AID,...)
    	theInstance.joinOntology(BasicOntology.instance());

	theInstance.addRole(EVENTRECORD, new SlotDescriptor[] {
	  new SlotDescriptor("what", Ontology.FRAME_SLOT, Ontology.ANY_TYPE, Ontology.M),
	  new SlotDescriptor("when", Ontology.PRIMITIVE_SLOT, Ontology.DATE_TYPE, Ontology.O),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.O)
	}, EventRecord.class);

	theInstance.addRole(ADDEDCONTAINER, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M),
	}, AddedContainer.class); 

	theInstance.addRole(REMOVEDCONTAINER, new SlotDescriptor[] {
	  new SlotDescriptor("container", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M)
	}, RemovedContainer.class); 

	theInstance.addRole(ADDEDMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M)
	}, AddedMTP.class);

	theInstance.addRole(REMOVEDMTP, new SlotDescriptor[] {
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M)
	}, RemovedMTP.class);

	theInstance.addRole(BORNAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.O)
	}, BornAgent.class); 

	theInstance.addRole(DEADAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.O)
	}, DeadAgent.class); 

	theInstance.addRole(SUSPENDEDAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.O)
	}, SuspendedAgent.class); 

	theInstance.addRole(RESUMEDAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.O)
	}, ResumedAgent.class); 

	theInstance.addRole(CHANGEDAGENTOWNERSHIP, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("from", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("to", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("where", Ontology.FRAME_SLOT, CONTAINERID, Ontology.O)
	}, ChangedAgentOwnership.class); 

	theInstance.addRole(MOVEDAGENT, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("from", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M),
	  new SlotDescriptor("to", Ontology.FRAME_SLOT, CONTAINERID, Ontology.M)
	}, MovedAgent.class);

	theInstance.addRole(CHANGEDAGENTSTATE, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("from", Ontology.FRAME_SLOT, AGENTSTATE, Ontology.M),
	  new SlotDescriptor("to", Ontology.FRAME_SLOT, AGENTSTATE, Ontology.M)
	}, ChangedAgentState.class);

	theInstance.addRole(ADDEDBEHAVIOUR, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("behaviour", Ontology.FRAME_SLOT, BEHAVIOURID, Ontology.M)
	}, AddedBehaviour.class);

	theInstance.addRole(REMOVEDBEHAVIOUR, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("behaviour", Ontology.FRAME_SLOT, BEHAVIOURID, Ontology.M)
	}, RemovedBehaviour.class);

	theInstance.addRole(CHANGEDBEHAVIOURSTATE, new SlotDescriptor[] {
	  new SlotDescriptor("agent", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("behaviour", Ontology.FRAME_SLOT, BEHAVIOURID, Ontology.M),
	  new SlotDescriptor("from", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("to", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, ChangedBehaviourState.class);

	theInstance.addRole(SENTMESSAGE, new SlotDescriptor[] {
	  new SlotDescriptor("sender", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("message", Ontology.FRAME_SLOT, ACLMESSAGE, Ontology.M)
	}, SentMessage.class);

	theInstance.addRole(RECEIVEDMESSAGE, new SlotDescriptor[] {
	  new SlotDescriptor("receiver", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("message", Ontology.FRAME_SLOT, ACLMESSAGE, Ontology.M)
	}, ReceivedMessage.class);

	theInstance.addRole(POSTEDMESSAGE, new SlotDescriptor[] {
	  new SlotDescriptor("receiver", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("message", Ontology.FRAME_SLOT, ACLMESSAGE, Ontology.M)
	}, PostedMessage.class);

	theInstance.addRole(ROUTEDMESSAGE, new SlotDescriptor[] {
	  new SlotDescriptor("from", Ontology.FRAME_SLOT, CHANNEL, Ontology.M),
	  new SlotDescriptor("to", Ontology.FRAME_SLOT, CHANNEL, Ontology.M),
	  new SlotDescriptor("message", Ontology.FRAME_SLOT, ACLMESSAGE, Ontology.M)
	}, RoutedMessage.class);

	theInstance.addRole(CONTAINERID, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, ContainerID.class);

	theInstance.addRole(AGENTSTATE, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, AgentState.class);

	theInstance.addRole(BEHAVIOURID, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  new SlotDescriptor("kind", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("parent", Ontology.FRAME_SLOT, BEHAVIOURID, Ontology.O)
	}, BehaviourID.class);

	theInstance.addRole(ACLMESSAGE, new SlotDescriptor[] {
	  new SlotDescriptor("envelope", Ontology.FRAME_SLOT, ENVELOPE, Ontology.O),
	  new SlotDescriptor("acl-representation", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("payload", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, ACLMessage.class);

	theInstance.addRole(ENVELOPE, new SlotDescriptor[] {
	  new SlotDescriptor("to", Ontology.SEQUENCE_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.O),
	  new SlotDescriptor("from", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.O),
	  new SlotDescriptor("comments", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("acl-representation", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("payload-length", Ontology.PRIMITIVE_SLOT, Ontology.LONG_TYPE, Ontology.O),
	  new SlotDescriptor("payload-encoding", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("date", Ontology.PRIMITIVE_SLOT, Ontology.DATE_TYPE, Ontology.O),
	  new SlotDescriptor("encrypted", Ontology.SEQUENCE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("intended-receiver", Ontology.SEQUENCE_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.O),
	  new SlotDescriptor("received", Ontology.FRAME_SLOT, RECEIVEDOBJECT, Ontology.O)
	  // new SlotDescriptor("transport-behaviour", Ontology.PRIMITIVE_SLOT, Ontology.BINARY_TYPE, Ontology.O)
	}, Envelope.class);

	theInstance.addRole(RECEIVEDOBJECT, new SlotDescriptor[] {
	  new SlotDescriptor("by", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("from", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("date", Ontology.PRIMITIVE_SLOT, Ontology.DATE_TYPE, Ontology.O),
	  new SlotDescriptor("id", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("via", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O)
	}, ReceivedObject.class);

	theInstance.addRole(CHANNEL, new SlotDescriptor[] {
	  new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("protocol", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  new SlotDescriptor("address", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, Channel.class);

	theInstance.addRole(STARTNOTIFY, new SlotDescriptor[] {
	  new SlotDescriptor("observer", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("events", Ontology.SEQUENCE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, StartNotify.class);

	theInstance.addRole(STOPNOTIFY, new SlotDescriptor[] {
	  new SlotDescriptor("observer", Ontology.FRAME_SLOT, BasicOntology.AGENTIDENTIFIER, Ontology.M),
	  new SlotDescriptor("events", Ontology.SEQUENCE_SLOT, Ontology.STRING_TYPE, Ontology.M)
	}, StopNotify.class);

	theInstance.addRole(OCCURRED, new SlotDescriptor[] {
	  new SlotDescriptor(Ontology.FRAME_SLOT, EVENTRECORD, Ontology.M)
	}, Occurred.class);

	theInstance.addRole(PLATFORMDESCRIPTION, new SlotDescriptor[]{
		new SlotDescriptor("platform",Ontology.FRAME_SLOT,BasicOntology.APDESCRIPTION,Ontology.M)
	}, PlatformDescription.class); 
    
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance


}
