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

package jade.domain;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.net.InetAddress;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;

import jade.core.*;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.FipaRequestResponderBehaviour;

/**
  Standard <em>Agent Management System</em> agent. This class
  implements <em><b>FIPA</b></em> <em>AMS</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.

  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class ams extends Agent {

  private abstract class AMSBehaviour
      extends FipaRequestResponderBehaviour.Action
      implements FipaRequestResponderBehaviour.Factory {

    // This Action will be set by crackMessage()
    private AgentManagementOntology.AMSAction myAction;

    protected AgentManagementOntology myOntology;


    protected AMSBehaviour() {
      super(ams.this);
      myOntology = AgentManagementOntology.instance();
    }

    protected void checkMandatory(AgentManagementOntology.AMSAgentDescriptor amsd) throws FIPAException {
      // Make sure mandatory attributes for the current AMS
      // action are non-null

      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.NAME, amsd.getName());
      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.ADDRESS, amsd.getAddress());
      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.SIGNATURE, amsd.getSignature());
      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.APSTATE, amsd.getAPState());
      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.DELEGATE, amsd.getDelegateAgentName());
      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.FORWARD, amsd.getForwardAddress());
      checkAttribute(AgentManagementOntology.AMSAgentDescriptor.OWNERSHIP, amsd.getOwnership());

    }


    // This method throws a FIPAException if the attribute is
    // mandatory for the current AMS action but it is a null object
    // reference
    private void checkAttribute(String attributeName, String attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForAMS(myAction.getName(), attributeName) && (attributeValue == null))
	throw myOntology.getException(AgentManagementOntology.Exception.MISSINGATTRIBUTE + " " +attributeName);
    }

    // This method parses the message content and puts
    // 'FIPA-AMS-description' attribute values in instance
    // variables. If some error is found a FIPA exception is thrown
    private void crackMessage() throws FIPAException, NoSuchElementException {

      ACLMessage msg = getRequest();
      String content = msg.getContent();

      // Obtain an AMS action from message content
      try {
	myAction = AgentManagementOntology.AMSAction.fromText(new StringReader(content));
      }
      catch(ParseException pe) {
	// pe.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR+ " :content");
      }
      catch(TokenMgrError tme) {
	// tme.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR+ " :content");
      }

    }

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(AgentManagementOntology.AMSAction a) throws FIPAException;

    public void action() {

      try {
	// Convert message from untyped keyword/value list to a Java
	// object throwing a FIPAException in case of errors
	crackMessage();

	// Do real action, deferred to subclasses
	processAction(myAction);

      }
      catch(FIPAException fe) {
	// fe.printStackTrace();
	sendRefuse(fe.getMessage());
      }
      catch(NoSuchElementException nsee) {
	// nsee.printStackTrace();
	sendRefuse(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
      }

    }

    public boolean done() {
      return true;
    }

    public void reset() {
    }

  } // End of AMSBehaviour class


  // These four concrete classes serve both as a Factory and as an
  // Action: when seen as Factory they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Action they process their request and terminate.

  private class AuthBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new AuthBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      String agentName = amsd.getName();
      if(agentName == null)
	AMSDumpData();
      else {
	AMSDumpData();
      }
      throw myOntology.getException(AgentManagementOntology.Exception.UNWILLING); // FIXME: Not Implemented
    }

  } // End of AuthBehaviour class


  private class RegBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new RegBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      // This agent was created by some other, which is still
      // waiting for an 'inform' message. Recover the buffered
      // message from the Map and send it back.
      ACLMessage informCreator = (ACLMessage)pendingInforms.remove(amsd.getName());

      // Write new agent data in AMS Agent Table
      try {
	AMSNewData(amsd);
	sendAgree();
	sendInform();

	// Inform agent creator that registration was successful.
	if(informCreator !=  null) {
	informCreator.setPerformative(ACLMessage.INFORM);
	informCreator.setContent("( done ( " + a.getName() + " ) )");
	send(informCreator);
	}

      }
      catch(AgentAlreadyRegisteredException aare) {
	sendAgree();
	sendFailure(aare.getMessage());

	// Inform agent creator that registration failed.
	if(informCreator != null) {
	  informCreator.setPerformative(ACLMessage.FAILURE);
	  informCreator.setContent("( ( action " + getLocalName() + " " + a.getName() + " ) " + aare.getMessage() + ")");
	  send(informCreator);
	}
      }
    }

  } // End of RegBehaviour class


  private class DeregBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new DeregBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      // Remove the agent data from Global Descriptor Table
      AMSRemoveData(amsd);
      sendAgree();
      sendInform();

    }

  } // End of DeregBehaviour class


  private class ModBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new ModBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      // Modify agent data from Global Descriptor Table
      AMSChangeData(amsd);
      sendAgree();
      sendInform();
    }

  } // End of ModBehaviour class

  private class QueryPPBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new QueryPPBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      sendAgree();

      StringWriter profile = new StringWriter();
      theProfile.toText(profile);

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setContent(profile.toString());
      send(reply);
    }

  } // End of QueryPPBehaviour class

  // These Behaviours handle interactions with Remote Management Agent.

  private class RegisterRMABehaviour extends CyclicBehaviour {

    private MessageTemplate subscriptionTemplate;

    RegisterRMABehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage("SL");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("jade-agent-management");
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("RMA-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
      subscriptionTemplate = MessageTemplate.and(mt1, mt2);
      subscriptionTemplate = MessageTemplate.and(subscriptionTemplate, mt12);

    }

    public void action() {

      // Receive 'subscribe' ACL messages.
      ACLMessage current = receive(subscriptionTemplate);
      if(current != null) {
	// FIXME: Should parse 'iota ?x ...'

	// Get new RMA name from subscription message
	String newRMA = current.getSource();

	// Send back the whole container list.
	String[] names = myPlatform.containerNames();
	for(int i = 0; i < names.length; i++) {
	  String containerName = names[i];
	  AgentManagementOntology.AMSContainerEvent ev = new AgentManagementOntology.AMSContainerEvent();
	  ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWCONTAINER);
	  ev.setContainerName(containerName);
	  StringWriter w = new StringWriter();
	  ev.toText(w);

	  RMANotification.removeAllDests();
	  RMANotification.addDest(newRMA);
	  RMANotification.setContent(w.toString());
	  send(RMANotification);

	}

	// Send all agent names, along with their container name.
	names = myPlatform.agentNames();
	for(int i = 0; i < names.length; i++) {
          try {
	    String agentName = names[i];
	    String containerName = myPlatform.getContainerName(agentName);
	    String agentAddress = myPlatform.getAddress(agentName); // FIXME: Need to use AMSAgDesc directly
	    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();
	    amsd.setName(agentName);
	    amsd.setAddress(agentAddress);
	    amsd.setAPState(Agent.AP_ACTIVE);

	    AgentManagementOntology.AMSAgentEvent ev = new AgentManagementOntology.AMSAgentEvent();
	    ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWAGENT);
	    ev.setContainerName(containerName);
	    ev.setAgentDescriptor(amsd);
	    StringWriter w = new StringWriter();
	    ev.toText(w);

	    RMANotification.setContent(w.toString());
	    RMANotification.removeAllDests();
	    RMANotification.addDest(newRMA);
	    send(RMANotification);
	  }
	  catch(NotFoundException nfe) {
	    nfe.printStackTrace();
	  }
	}

	// Add the new RMA to RMAs agent group.
	RMAs.addMember(newRMA);

      }
      else
	block();

    }

  } // End of RegisterRMABehaviour class

  private class DeregisterRMABehaviour extends CyclicBehaviour {

    private MessageTemplate cancellationTemplate;

    DeregisterRMABehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage("SL");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("jade-agent-management");
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("RMA-cancellation");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
      cancellationTemplate = MessageTemplate.and(mt1, mt2);
      cancellationTemplate = MessageTemplate.and(cancellationTemplate, mt12);

    }

    public void action() {

      // Receive 'cancel' ACL messages.
      ACLMessage current = receive(cancellationTemplate);
      if(current != null) {
	// FIXME: Should parse 'iota ?x ...'

	// Remove this RMA to RMAs agent group.
	RMAs.removeMember(current.getSource());

      }
      else
	block();

    }

  } // End of DeregisterRMABehaviour class


  private class NotifyRMAsBehaviour extends CyclicBehaviour {

    private void processNewContainers() {
      Iterator it = newContainersBuffer.iterator();
      while(it.hasNext()) {
	ContDesc c  = (ContDesc)it.next();
	String name = c.name;
	InetAddress addr = c.addr;
	MobilityOntology.Location loc = new MobilityOntology.Location();
	loc.setName(name);
	loc.setTransportProtocol("JADE-IPMT");
	loc.setTransportAddress(getAddress() + "." + name);
	mobilityMgr.addLocation(name, loc);

	AgentManagementOntology.AMSContainerEvent ev = new AgentManagementOntology.AMSContainerEvent();
	ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWCONTAINER);
	ev.setContainerName(name);
	ev.setContainerAddr(addr.getHostName());
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());
	send(RMANotification, RMAs);
	it.remove();
      }
    }

    private void processDeadContainers() {
      Iterator it = deadContainersBuffer.iterator();
      while(it.hasNext()) {
	String name = (String)it.next();
	mobilityMgr.removeLocation(name);

	AgentManagementOntology.AMSContainerEvent ev = new AgentManagementOntology.AMSContainerEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.DEADCONTAINER);
	ev.setContainerName(name);
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());
	send(RMANotification, RMAs);
	it.remove();
      }
    }

    private void processNewAgents() {
      Iterator it = newAgentsBuffer.iterator();
      while(it.hasNext()) {
	AgDesc ad = (AgDesc)it.next();
	AgentManagementOntology.AMSAgentEvent ev = new AgentManagementOntology.AMSAgentEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.NEWAGENT);
	ev.setContainerName(ad.containerName);
	ev.setAgentDescriptor(ad.amsd);
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());
	send(RMANotification, RMAs);
	it.remove();
      }
    }

    private void processDeadAgents() {
      Iterator it = deadAgentsBuffer.iterator();
      while(it.hasNext()) {
	AgDesc ad = (AgDesc)it.next();
	AgentManagementOntology.AMSAgentEvent ev = new AgentManagementOntology.AMSAgentEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.DEADAGENT);
	ev.setContainerName(ad.containerName);
	ev.setAgentDescriptor(ad.amsd);

	// Remove Agent Descriptor from table
	String agentName = ad.amsd.getName();
	descrTable.remove(agentName.toLowerCase());

	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());

	send(RMANotification, RMAs);
	it.remove();
      }
    }

    private void processMovedAgents() {
      Iterator it = movedAgentsBuffer.iterator();
      while(it.hasNext()) {
	MotionDesc md = (MotionDesc)it.next();
	AgentManagementOntology.AMSMotionEvent ev = new AgentManagementOntology.AMSMotionEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.MOVEDAGENT);

	ev.setAgentDescriptor(md.desc);
	ev.setSrc(md.src);
	ev.setDest(md.dest);

	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());

	send(RMANotification, RMAs);
	it.remove();
      }
    }

    public void action() {
      // Look into the event buffers with AgentPlatform and send
      // appropriate ACL messages to registered RMAs

      // Mutual exclusion with postXXX() methods
      synchronized(ams.this) {
	processNewContainers();
	processDeadContainers();
	processNewAgents();
	processDeadAgents();
	processMovedAgents();
      }

      block();

    }

  } // End of NotifyRMAsBehaviour class

  private class KillContainerBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new KillContainerBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

      // Obtain container name and ask AgentPlatform to kill it
      AgentManagementOntology.KillContainerAction kca = (AgentManagementOntology.KillContainerAction)a;
      String containerName = kca.getContainerName();
      myPlatform.killContainer(containerName);
      sendAgree();
      sendInform();
    }

  } // End of KillContainerBehaviour class

  private class CreateBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new CreateBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

      AgentManagementOntology.CreateAgentAction caa = (AgentManagementOntology.CreateAgentAction)a;
      String className = caa.getClassName();
      String containerName = caa.getProperty(AgentManagementOntology.CreateAgentAction.CONTAINER);

      sendAgree();

      // Create a new agent
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();
      try {
	myPlatform.create(amsd.getName(), className, containerName);
	// An 'inform Done' message will be sent to the requester only
	// when the newly created agent will register itself with the
	// AMS. The new agent's name will be used as the key in the map.
	ACLMessage reply = getReply();
	reply = (ACLMessage)reply.clone();

	pendingInforms.put(amsd.getName(), reply);
      }
      catch(UnreachableException ue) {
	throw new NoCommunicationMeansException();
      }
    }

  } // End of CreateBehaviour class

  private class KillBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new KillBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

      // Kill an agent
      AgentManagementOntology.KillAgentAction kaa = (AgentManagementOntology.KillAgentAction)a;
      String agentName = kaa.getAgentName();
      String password = kaa.getPassword();
      try {
	myPlatform.kill(agentName, password);
	sendAgree();
	sendInform();
      }
      catch(UnreachableException ue) {
	throw new NoCommunicationMeansException();
      }
      catch(NotFoundException nfe) {
	throw new AgentNotRegisteredException();
      }

    }

  } // End of KillBehaviour class


  private class SniffAgentOnBehaviour extends AMSBehaviour { 

    public FipaRequestResponderBehaviour.Action create() {
      return new SniffAgentOnBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.SniffAgentOnAction saoa = (AgentManagementOntology.SniffAgentOnAction)a;
      try {
	myPlatform.sniffOn(saoa.getSnifferName(), saoa.getEntireList());
	sendAgree();
	sendInform();
      }
      catch(UnreachableException ue) {
	throw new NoCommunicationMeansException();
      }
    }

  } // End of SniffAgentOnBehaviour class

  private class SniffAgentOffBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new SniffAgentOffBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.SniffAgentOffAction saoa = (AgentManagementOntology.SniffAgentOffAction)a;
      try {
	myPlatform.sniffOff(saoa.getSnifferName(), saoa.getEntireList());
	sendAgree();
	sendInform();
      }
      catch(UnreachableException ue) {
	throw new NoCommunicationMeansException();
      }
    }

  } // End of SniffAgentOffBehaviour class


  private static class AgDesc {

    public AgDesc(String s, AgentManagementOntology.AMSAgentDescriptor a) {
      containerName = s;
      amsd = a;
    }

    public String containerName;
    public AgentManagementOntology.AMSAgentDescriptor amsd;

  }

  private static class ContDesc {

    public ContDesc(String s, InetAddress a) {
      name = s;
      addr = a;
    }

    public String name;
    public InetAddress addr;

  }

  private static class MotionDesc {

    public MotionDesc(AgentManagementOntology.AMSAgentDescriptor amsd, String s, String d) {
      desc = amsd;
      src = s;
      dest = d;
    }

    public AgentManagementOntology.AMSAgentDescriptor desc;
    public String src;
    public String dest;

  }

  // The AgentPlatform where information about agents is stored 
  private AgentManager myPlatform;

  // The table of 'AMS-Agent-Description' data for all the agents
  private Map descrTable;

  // Maintains an association between action names and behaviours
  private FipaRequestResponderBehaviour dispatcher;

  // Contains a main Behaviour and some utilities to handle JADE mobility
  private MobilityManager mobilityMgr;

  // Behaviour to listen to incoming 'subscribe' messages from Remote
  // Management Agents.
  private RegisterRMABehaviour registerRMA;

  // Behaviour to broadcats AgentPlatform notifications to each
  // registered Remote Management Agent.
  private NotifyRMAsBehaviour notifyRMAs;

  // Behaviour to listen to incoming 'cancel' messages from Remote
  // Management Agents.
  private DeregisterRMABehaviour deregisterRMA;

  // Group of Remote Management Agents registered with this AMS
  private AgentGroup RMAs;

  // ACL Message to use for RMA notification
  private ACLMessage RMANotification = new ACLMessage(ACLMessage.INFORM);

  // Buffers for AgentPlatform notifications
  private List newContainersBuffer = new ArrayList();
  private List deadContainersBuffer = new ArrayList();
  private List newAgentsBuffer = new ArrayList();
  private List deadAgentsBuffer = new ArrayList();
  private List movedAgentsBuffer = new ArrayList();

  private Map pendingInforms = new HashMap();

  private AgentManagementOntology.PlatformProfile theProfile = new AgentManagementOntology.PlatformProfile();

  /**
     This constructor creates a new <em>AMS</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>AMS</em> agent can be created
     beyond the default one.
  */
  public ams(AgentManager ap) {
    myPlatform = ap;
    descrTable = new HashMap();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));
    dispatcher = new FipaRequestResponderBehaviour(this, mt);
    mobilityMgr = new MobilityManager(this);
    registerRMA = new RegisterRMABehaviour();
    deregisterRMA = new DeregisterRMABehaviour();
    notifyRMAs = new NotifyRMAsBehaviour();

    RMAs = new AgentGroup();

    RMANotification.setSource(getLocalName());
    RMANotification.setLanguage("SL");
    RMANotification.setOntology("jade-agent-management");
    RMANotification.setReplyTo("RMA-subscription");

    // Associate each AMS action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(AgentManagementOntology.AMSAction.AUTHENTICATE, new AuthBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.REGISTERAGENT, new RegBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.DEREGISTERAGENT, new DeregBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.MODIFYAGENT, new ModBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.QUERYPLATFORMPROFILE, new QueryPPBehaviour());

    dispatcher.registerFactory(AgentManagementOntology.AMSAction.CREATEAGENT, new CreateBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.KILLAGENT, new KillBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.KILLCONTAINER, new KillContainerBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.SNIFFAGENTON, new SniffAgentOnBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.SNIFFAGENTOFF, new SniffAgentOffBehaviour());

  }

  /**
   This method starts the <em>AMS</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {

    // Fill Agent Platform Profile with data.
    theProfile.setPlatformName("cselt.UniPR.JADE");
    theProfile.setIiopURL(getAddress());
    theProfile.setDynReg("false");
    theProfile.setMobility("false");
    theProfile.setOwnership("CSELT.it");
    theProfile.setCertificationAuthority("None");
    theProfile.setFipaVersion("fipa97-part1-v2");

    // Add a dispatcher Behaviour for all ams actions following from a
    // 'fipa-request' interaction
    addBehaviour(dispatcher);

    // Add a main behaviour to manage mobility related messages
    addBehaviour(mobilityMgr.getMain());

    // Add a Behaviour to accept incoming RMA registrations and a
    // Behaviour to broadcast events to registered RMAs.
    addBehaviour(registerRMA);
    addBehaviour(deregisterRMA);
    addBehaviour(notifyRMAs);

  }

  // This one is called in response to a 'register-agent' action
  private void AMSNewData(AgentManagementOntology.AMSAgentDescriptor amsd) throws FIPAException, AgentAlreadyRegisteredException {

    String agentName = amsd.getName();
    AgentManagementOntology.AMSAgentDescriptor old = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());

    // FIXME: Should accept foreign agents by checking with the GADT

    if(old != null) {
      throw new AgentAlreadyRegisteredException();
    }

    descrTable.put(agentName.toLowerCase(), amsd);

  }

  // This one is called in response to a 'modify-agent' action
  private void AMSChangeData(AgentManagementOntology.AMSAgentDescriptor amsd) throws FIPAException {

    try {
      String agentName = amsd.getName();
      AgentManagementOntology.AMSAgentDescriptor toChange = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
      if(toChange == null)
	throw new AgentNotRegisteredException();

      String address = amsd.getAddress();
      if(address != null)
	toChange.setAddress(address);
      String signature = amsd.getSignature();
      if(signature != null)
	toChange.setSignature(signature);
      String delegateAgentName = amsd.getDelegateAgentName();
      if(delegateAgentName != null) {
	toChange.setDelegateAgentName(delegateAgentName);
	myPlatform.setDelegateAgent(agentName, delegateAgentName);
      }

      String forwardAddress = amsd.getForwardAddress();
      if(forwardAddress != null)
	toChange.setAddress(forwardAddress);

      String ownership = amsd.getOwnership();
      if(ownership != null)
	toChange.setOwnership(ownership);
      String APState = amsd.getAPState();
      if(APState != null) {
	AgentManagementOntology o = AgentManagementOntology.instance();
	int state = o.getAPStateByName(APState);
	int oldState = o.getAPStateByName(toChange.getAPState());
	switch(state) {
	case Agent.AP_SUSPENDED:
	  myPlatform.suspend(agentName, null);
	  break;
	case Agent.AP_WAITING:
	  myPlatform.wait(agentName, null);
	  break;
	case Agent.AP_ACTIVE:
	  if(oldState == Agent.AP_WAITING)
	    myPlatform.wake(agentName, null);
	  else
	    myPlatform.activate(agentName, null);
	  break;
	case Agent.AP_DELETED:
	  myPlatform.kill(agentName, null);
	  break;
	}

	toChange.setAPState(state);

      }
    }
    catch(NotFoundException nfe) {
      throw new AgentNotRegisteredException();
    }
    catch(UnreachableException ue) {
      throw new NoCommunicationMeansException();
    }

  }


  // This one is called in response to a 'deregister-agent' action
  private void AMSRemoveData(AgentManagementOntology.AMSAgentDescriptor amsd) throws FIPAException {
    String agentName = amsd.getName();
    AgentManagementOntology.AMSAgentDescriptor toRemove = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
    if(toRemove == null) {
      throw new jade.domain.UnableToDeregisterException();
    }
    toRemove.setAPState(Agent.AP_DELETED);
    // This descriptor will be removed from the table after the platform notification
  }

  private void AMSDumpData() {
    Iterator descriptors = descrTable.values().iterator();
    while(descriptors.hasNext()) {
      AgentManagementOntology.AMSAgentDescriptor amsd = (AgentManagementOntology.AMSAgentDescriptor)descriptors.next();
      amsd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    }
  }

  private void AMSDumpData(String agentName) {
    AgentManagementOntology.AMSAgentDescriptor amsd = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
    amsd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  }


  // This one is called in response to a 'move-agent' action
  void AMSMoveAgent(String agentName, Location where) throws FIPAException {
    try {
      int atPos = agentName.indexOf('@');
      if(atPos == -1)
        agentName = agentName.concat('@' + getAddress());
      myPlatform.move(agentName, where, "");
    }
    catch(UnreachableException ue) {
      ue.printStackTrace();
      throw new NoCommunicationMeansException();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      throw new AgentNotRegisteredException();
    }
  }

  // This one is called in response to a 'clone-agent' action
  void AMSCloneAgent(String agentName, Location where, String newName) throws FIPAException {
    try {
      int atPos = agentName.indexOf('@');
      if(atPos == -1)
        agentName = agentName.concat('@' + getAddress());
      myPlatform.copy(agentName, where, newName, "");
    }
    catch(UnreachableException ue) {
      throw new NoCommunicationMeansException();
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      throw new AgentNotRegisteredException();
    }
  }


  // This one is called in response to a 'where-is-agent' action
  MobilityOntology.Location AMSWhereIsAgent(String agentName) throws FIPAException {
    try {
      int atPos = agentName.indexOf('@');
      if(atPos == -1)
        agentName = agentName.concat('@' + getAddress());
      String containerName = myPlatform.getContainerName(agentName);
      return mobilityMgr.getLocation(containerName);
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      throw new AgentNotRegisteredException();
    }
  }

  // This one is called in response to a 'query-platform-locations' action
  MobilityOntology.Location[] AMSGetPlatformLocations() {
    return mobilityMgr.getLocations();
  }

  /**
   The AMS must have a special version for this method, or a deadlock will occur.
  */
  public void registerWithAMS(String signature, int APState, String delegateAgentName,
		       String forwardAddress, String ownership) {

    // Skip all fipa-request protocol and go straight to the target
    
    try { // FIXME: APState parameter is never used
      AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();
      amsd.setName(getName());
      amsd.setAddress(getAddress());
      amsd.setSignature(signature);
      amsd.setAPState(Agent.AP_ACTIVE);
      amsd.setDelegateAgentName(delegateAgentName);
      amsd.setForwardAddress(forwardAddress);
      amsd.setOwnership(ownership);

      AMSNewData(amsd);
    }
    // No exception should occur since this is a special case ...
    catch(AgentAlreadyRegisteredException aare) {
      aare.printStackTrace();
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  /** 
    The AMS must have a special version for this method, or a deadlock will occur.
  */
  public void deregisterWithAMS() throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();
      amsd.setName(getName());
      amsd.setAddress(getAddress());
      amsd.setAPState(Agent.AP_ACTIVE);

      AMSRemoveData(amsd);
  }

  // Methods to be called from AgentPlatform to notify AMS of special events

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postNewContainer(String name, InetAddress addr) {
    newContainersBuffer.add(new ContDesc(name, addr));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postDeadContainer(String name) {
    deadContainersBuffer.add(new String(name));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postNewAgent(String containerName, String agentName) {
    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();
    amsd.setName(agentName);
    amsd.setAddress(myPlatform.getAddress(agentName));
    newAgentsBuffer.add(new AgDesc(containerName, amsd));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postDeadAgent(String containerName, String agentName) {
    AgentManagementOntology.AMSAgentDescriptor amsd = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
    deadAgentsBuffer.add(new AgDesc(containerName, amsd));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postMovedAgent(String agentName, String src, String dest) {
    AgentManagementOntology.AMSAgentDescriptor amsd = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
    movedAgentsBuffer.add(new MotionDesc(amsd, src, dest));
    doWake();
  }

} // End of class ams
