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
import java.util.Map;
import java.util.HashMap;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.domain.FIPAAgentManagement.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.NotRegistered;
import jade.domain.FIPAAgentManagement.InternalError;

import jade.domain.FIPAAgentManagement.Register;
import jade.domain.FIPAAgentManagement.Deregister;
import jade.domain.FIPAAgentManagement.Modify;
import jade.domain.FIPAAgentManagement.Search;
import jade.domain.FIPAAgentManagement.SearchConstraints;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.onto.Action;

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

    protected AMSBehaviour() {
      super(ams.this);
    }

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(Action a) throws FIPAException;

    public void action() {

      try {

	ACLMessage msg = getRequest();
        
       	// Extract the Action object from the message content
	List l = extractContent(msg);
	Action a = (Action)l.get(0);

	// Do real action, deferred to subclasses
	processAction(a);

      }
      catch(FIPAException fe) {
	sendRefuse(fe.getMessage());
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

  private class RegBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new RegBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Register r = (Register)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)r.get_0();

      // This agent was created by some other, which is still
      // waiting for an 'inform' message. Recover the buffered
      // message from the Map and send it back.
      ACLMessage informCreator = (ACLMessage)pendingInforms.remove(amsd.getName());

      try {
	// Write new agent data in AMS Agent Table
	AMSRegister(amsd);
	sendAgree();
	sendInform();

	// Inform agent creator that registration was successful.
	if(informCreator !=  null) {
	  informCreator.setPerformative(ACLMessage.INFORM);
	  // informCreator.setContent("( done ( " + a.getName() + " ) )");
	  send(informCreator);
	}
      }
      catch(AlreadyRegistered are) {
	sendAgree();
	sendFailure(are.getMessage());

	// Inform agent creator that registration failed.
	if(informCreator != null) {
	  informCreator.setPerformative(ACLMessage.FAILURE);
	  // informCreator.setContent("( ( action " + getLocalName() + " " + a.getName() + " ) " + aare.getMessage() + ")");
	  send(informCreator);
	}
      }
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new DeregBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Deregister d = (Deregister)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)d.get_0();
      AMSDeregister(amsd);
      sendAgree();
      sendInform();
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new ModBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Modify m = (Modify)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)m.get_0();
      AMSModify(amsd);
      sendAgree();
      sendInform();
    }

  } // End of ModBehaviour class

  private class SrchBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new SrchBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Search s = (Search)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)s.get_0();
      SearchConstraints constraints = s.get_1();
      AMSSearch(amsd, constraints, getReply());

    }

  } // End of SrchBehaviour class

  private class GetDescriptionBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new GetDescriptionBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {

      sendAgree();

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      List l = new ArrayList(1);
      l.add(theProfile);
      fillContent(reply, l);
      send(reply);
    }

  } // End of GetDescriptionBehaviour class


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
	AID newRMA = current.getSender();

	// Send back the whole container list.
	String[] names = myPlatform.containerNames();
	for(int i = 0; i < names.length; i++) {
	  String containerName = names[i];

	  // Create an ontological object corresponding to a sequence<string> ...

	  RMANotification.clearAllReceiver();
	  RMANotification.addReceiver(newRMA);
	  RMANotification.setContent("");
	  send(RMANotification);

	}

	// Send all agent names, along with their container name.
	names = myPlatform.agentNames();
	for(int i = 0; i < names.length; i++) {
          try {
	    String agentName = names[i];
	    String containerName = myPlatform.getContainerName(agentName);

	    AMSAgentDescription amsd = new AMSAgentDescription();
	    List l = new ArrayList(1);
	    l.add(amsd);

	    fillContent(RMANotification, l);
	    RMANotification.clearAllReceiver();
	    RMANotification.addReceiver(newRMA);
	    send(RMANotification);
	  }
	  catch(NotFoundException nfe) {
	    nfe.printStackTrace();
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
	  }
	}

	// Add the new RMA to RMAs list.
	RMAs.add(newRMA);

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
	RMAs.remove(current.getSender());

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
	loc.setTransportAddress(getHap() + "." + name);
	mobilityMgr.addLocation(name, loc);

	// Fill the content with a suitable ontological object
	RMANotification.setContent("");
	RMANotification.clearAllReceiver();
	Iterator rmaIt = RMAs.iterator();
	while(rmaIt.hasNext())
	  RMANotification.addReceiver((AID)rmaIt.next());

	send(RMANotification);

	it.remove();
      }
    }

    private void processDeadContainers() {
      Iterator it = deadContainersBuffer.iterator();
      while(it.hasNext()) {
	String name = (String)it.next();
	mobilityMgr.removeLocation(name);

	// Fill the content with a suitable ontological object
	RMANotification.setContent("");
	RMANotification.clearAllReceiver();
	Iterator rmaIt = RMAs.iterator();
	while(rmaIt.hasNext())
	  RMANotification.addReceiver((AID)rmaIt.next());

	send(RMANotification);

	it.remove();
      }
    }

    private void processNewAgents() {
      Iterator it = newAgentsBuffer.iterator();
      while(it.hasNext()) {
	AgDesc ad = (AgDesc)it.next();

	// Fill the content with a suitable ontological object
	RMANotification.setContent("");
	RMANotification.clearAllReceiver();
	Iterator rmaIt = RMAs.iterator();
	while(rmaIt.hasNext())
	  RMANotification.addReceiver((AID)rmaIt.next());

	send(RMANotification);

	it.remove();
      }
    }

    private void processDeadAgents() {
      Iterator it = deadAgentsBuffer.iterator();
      while(it.hasNext()) {
	AgDesc ad = (AgDesc)it.next();

	// Remove Agent Descriptor from table

	// Fill the content with a suitable ontological object
	RMANotification.setContent("");
	RMANotification.clearAllReceiver();
	Iterator rmaIt = RMAs.iterator();
	while(rmaIt.hasNext())
	  RMANotification.addReceiver((AID)rmaIt.next());

	send(RMANotification);
	it.remove();
      }
    }

    private void processMovedAgents() {
      Iterator it = movedAgentsBuffer.iterator();
      while(it.hasNext()) {
	MotionDesc md = (MotionDesc)it.next();

	// Fill the content with a suitable ontological object
	RMANotification.setContent("");
	RMANotification.clearAllReceiver();
	Iterator rmaIt = RMAs.iterator();
	while(rmaIt.hasNext())
	  RMANotification.addReceiver((AID)rmaIt.next());

	send(RMANotification);
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

    protected void processAction(Action a) throws FIPAException {

	/*
      // Obtain container name and ask AgentPlatform to kill it
      AgentManagementOntology.KillContainerAction kca = (AgentManagementOntology.KillContainerAction)a;
      String containerName = kca.getContainerName();
      myPlatform.killContainer(containerName);
      sendAgree();
      sendInform();
	*/
    }

  } // End of KillContainerBehaviour class

  private class CreateBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new CreateBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
	/*
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
	*/
    }

  } // End of CreateBehaviour class

  private class KillBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new KillBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
	/*
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
	*/
    }

  } // End of KillBehaviour class


  private class SniffAgentOnBehaviour extends AMSBehaviour { 

    public FipaRequestResponderBehaviour.Action create() {
      return new SniffAgentOnBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
	/*
      AgentManagementOntology.SniffAgentOnAction saoa = (AgentManagementOntology.SniffAgentOnAction)a;
      try {
	myPlatform.sniffOn(saoa.getSnifferName(), saoa.getEntireList());
	sendAgree();
	sendInform();
      }
      catch(UnreachableException ue) {
	throw new NoCommunicationMeansException();
      }
	*/
    }

  } // End of SniffAgentOnBehaviour class

  private class SniffAgentOffBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new SniffAgentOffBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
	/*
      AgentManagementOntology.SniffAgentOffAction saoa = (AgentManagementOntology.SniffAgentOffAction)a;
      try {
	myPlatform.sniffOff(saoa.getSnifferName(), saoa.getEntireList());
	sendAgree();
	sendInform();
      }
      catch(UnreachableException ue) {
	throw new NoCommunicationMeansException();
      }
	*/
    }

  } // End of SniffAgentOffBehaviour class


  private static class AgDesc {

    public AgDesc(String s, AMSAgentDescription a) {
      containerName = s;
      amsd = a;
    }

    public String containerName;
    public AMSAgentDescription amsd;

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

    public MotionDesc(AMSAgentDescription amsd, String s, String d) {
      desc = amsd;
      src = s;
      dest = d;
    }

    public AMSAgentDescription desc;
    public String src;
    public String dest;

  }

  // The AgentPlatform where information about agents is stored 
  /**
  @serial
  */
  private AgentManager myPlatform;


  // Maintains an association between action names and behaviours
  /**
  @serial
  */
  private FipaRequestResponderBehaviour dispatcher;

  // Contains a main Behaviour and some utilities to handle JADE mobility
  /**
  @serial
  */
  private MobilityManager mobilityMgr;

  // Behaviour to listen to incoming 'subscribe' messages from Remote
  // Management Agents.
  /**
  @serial
  */
  private RegisterRMABehaviour registerRMA;

  // Behaviour to broadcats AgentPlatform notifications to each
  // registered Remote Management Agent.
  /**
  @serial
  */
  private NotifyRMAsBehaviour notifyRMAs;

  // Behaviour to listen to incoming 'cancel' messages from Remote
  // Management Agents.
  /**
  @serial
  */
  private DeregisterRMABehaviour deregisterRMA;

  // Group of Remote Management Agents registered with this AMS
  /**
  @serial
  */
  private List RMAs;

  // ACL Message to use for RMA notification
  /**
  @serial
  */
  private ACLMessage RMANotification = new ACLMessage(ACLMessage.INFORM);

  // Buffers for AgentPlatform notifications
  /**
  @serial
  */
  private List newContainersBuffer = new ArrayList();
  /**
  @serial
  */
  private List deadContainersBuffer = new ArrayList();
  /**
  @serial
  */
  private List newAgentsBuffer = new ArrayList();
  /**
  @serial
  */
  private List deadAgentsBuffer = new ArrayList();
  /**
  @serial
  */
  private List movedAgentsBuffer = new ArrayList();

  /**
  @serial
  */
  private Map pendingInforms = new HashMap();

  /**
  @serial
  */
  private APDescription theProfile = new APDescription();

  /**
     This constructor creates a new <em>AMS</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>AMS</em> agent can be created
     beyond the default one.
  */
  public ams(AgentManager ap) {
    myPlatform = ap;

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));
    dispatcher = new FipaRequestResponderBehaviour(this, mt);
    mobilityMgr = new MobilityManager(this);
    registerRMA = new RegisterRMABehaviour();
    deregisterRMA = new DeregisterRMABehaviour();
    notifyRMAs = new NotifyRMAsBehaviour();

    RMAs = new ArrayList();

    RMANotification.setSender(new AID());
    RMANotification.setLanguage("SL");
    RMANotification.setOntology("jade-agent-management");
    RMANotification.setInReplyTo("RMA-subscription");

    // Associate each AMS action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(FIPAAgentManagementOntology.REGISTER, new RegBehaviour());
    dispatcher.registerFactory(FIPAAgentManagementOntology.DEREGISTER, new DeregBehaviour());
    dispatcher.registerFactory(FIPAAgentManagementOntology.MODIFY, new ModBehaviour());
    dispatcher.registerFactory(FIPAAgentManagementOntology.SEARCH, new SrchBehaviour());

    dispatcher.registerFactory(FIPAAgentManagementOntology.GETDESCRIPTION, new GetDescriptionBehaviour());
    /*
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.CREATEAGENT, new CreateBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.KILLAGENT, new KillBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.KILLCONTAINER, new KillContainerBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.SNIFFAGENTON, new SniffAgentOnBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.SNIFFAGENTOFF, new SniffAgentOffBehaviour());
    */
  }

  /**
   This method starts the <em>AMS</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {

    // Fill Agent Platform Profile with data.
    theProfile.setName("JADE");
    theProfile.setDynamic(new Boolean(false));
    theProfile.setMobility(new Boolean(false));
    theProfile.setTransportProfile(null);

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

  private void AMSRegister(AMSAgentDescription amsd) throws FIPAException {
    System.out.println("ams::AMSRegister() called");
  }

  private void AMSDeregister(AMSAgentDescription amsd) throws FIPAException {
    System.out.println("ams::AMSDeregister() called");
  }

  private void AMSModify(AMSAgentDescription amsd) throws FIPAException {
    System.out.println("ams::AMSModify() called");
  }

  private void AMSSearch(AMSAgentDescription amsd, SearchConstraints constraints, ACLMessage reply) throws FIPAException {
    System.out.println("ams::AMSSearch() called");
  }

  // This one is called in response to a 'move-agent' action
  void AMSMoveAgent(String agentName, Location where) throws FIPAException {
    try {
      int atPos = agentName.indexOf('@');
      if(atPos == -1)
        agentName = agentName.concat('@' + getHap());
      myPlatform.move(agentName, where, "");
    }
    catch(UnreachableException ue) {
      throw new InternalError("The container is not reachable");
    }
    catch(NotFoundException nfe) {
      throw new NotRegistered();
    }
  }

  // This one is called in response to a 'clone-agent' action
  void AMSCloneAgent(String agentName, Location where, String newName) throws FIPAException {
    try {
      int atPos = agentName.indexOf('@');
      if(atPos == -1)
        agentName = agentName.concat('@' + getHap());
      myPlatform.copy(agentName, where, newName, "");
    }
    catch(UnreachableException ue) {
      throw new InternalError("The container is not reachable");
    }
    catch(NotFoundException nfe) {
      throw new NotRegistered();
    }
  }


  // This one is called in response to a 'where-is-agent' action
  MobilityOntology.Location AMSWhereIsAgent(String agentName) throws FIPAException {
    try {
      int atPos = agentName.indexOf('@');
      if(atPos == -1)
        agentName = agentName.concat('@' + getHap());
      String containerName = myPlatform.getContainerName(agentName);
      return mobilityMgr.getLocation(containerName);
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      throw new NotRegistered();
    }
  }

  // This one is called in response to a 'query-platform-locations' action
  MobilityOntology.PlatformLocations AMSGetPlatformLocations() {
    return mobilityMgr.getLocations();
  }

  /**
   The AMS must have a special version for this method, or a deadlock will occur.
  */
  public void registerWithAMS(AMSAgentDescription amsd) {

    // Skip all fipa-request protocol and go straight to the target
    
    try {
      AMSRegister(amsd);
    }
    // No exception should occur since this is a special case ...
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  /** 
    The AMS must have a special version for this method, or a deadlock will occur.
  */
  public void deregisterWithAMS() throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription(); // Get the standard AMS AID.
    AMSDeregister(amsd);
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
    AMSAgentDescription amsd = new AMSAgentDescription();
    newAgentsBuffer.add(new AgDesc(containerName, amsd));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postDeadAgent(String containerName, String agentName) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    //(AMSAgentDescription)descrTable.get(agentName.toLowerCase());
    deadAgentsBuffer.add(new AgDesc(containerName, amsd));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postMovedAgent(String agentName, String src, String dest) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    //(AMSAgentDescription)descrTable.get(agentName.toLowerCase());
    movedAgentsBuffer.add(new MotionDesc(amsd, src, dest));
    doWake();
  }

} // End of class ams
