/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;


import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;

import jade.proto.FipaRequestResponderBehaviour;

/**
  Standard <em>Agent Management System</em> agent. This class
  implements <em><b>FIPA</b></em> <em>AMS</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.

  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$


*/
public class ams extends Agent implements AgentManager.Listener {

  private abstract class AMSBehaviour
      extends FipaRequestResponderBehaviour.ActionHandler
      implements FipaRequestResponderBehaviour.Factory {

	
    protected AMSBehaviour(ACLMessage req) {
      super(ams.this,req);
    }

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(Action a) throws FIPAException;

    public void action() {

      try {

	ACLMessage msg = getRequest();

	List l = myAgent.extractContent(msg);
	Action a = (Action)l.get(0);

	// Do real action, deferred to subclasses
	processAction(a);

      }
      catch(FIPAException fe) {
	sendReply(ACLMessage.REFUSE,"("+fe.getMessage()+")");
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

    public RegBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new RegBehaviour(msg);
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
	//sendReply(ACLMessage.AGREE,"( true )");
	sendReply(ACLMessage.INFORM, "FIXME");

	// Inform agent creator that registration was successful.
	if(informCreator !=  null) {
	  informCreator.setPerformative(ACLMessage.INFORM);
	  // informCreator.setContent("( done ( " + a.getName() + " ) )");
	  send(informCreator);
	}
      }
      catch(AlreadyRegistered are) {
	//sendReply(ACLMessage.AGREE, "( true )");
	sendReply(ACLMessage.FAILURE,"("+are.getMessage()+")");

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
    public DeregBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new DeregBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Deregister d = (Deregister)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)d.get_0();
      AMSDeregister(amsd);
      //sendReply(ACLMessage.AGREE, "( true )");
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends AMSBehaviour {
    public ModBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new ModBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Modify m = (Modify)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)m.get_0();
      AMSModify(amsd);
      //sendReply(ACLMessage.AGREE, "( true)");
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  } // End of ModBehaviour class

  private class SrchBehaviour extends AMSBehaviour {
    public SrchBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SrchBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Search s = (Search)a.getAction();
      AMSAgentDescription amsd = (AMSAgentDescription)s.get_0();
      SearchConstraints constraints = s.get_1();
      List l = AMSSearch(amsd, constraints, getReply());
      //sendReply(ACLMessage.AGREE,"( true)");
      ACLMessage msg = getRequest().createReply();
      msg.setPerformative(ACLMessage.INFORM);
      ResultPredicate r = new ResultPredicate();
      r.set_0(a);
      for (int i=0; i<l.size(); i++)
      	r.add_1(l.get(i));
      l.clear();
      l.add(r);
      fillContent(msg,l); 
      send(msg);
    }

  } // End of SrchBehaviour class

  private class GetDescriptionBehaviour extends AMSBehaviour {
    public GetDescriptionBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new GetDescriptionBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {

      //sendReply(ACLMessage.AGREE, "( true )");

      ACLMessage reply = getReply();
      reply.setPerformative(ACLMessage.INFORM);
      List l = new ArrayList(1);
      l.add(theProfile);
      fillContent(reply, l);
      send(reply);
    }

  } // End of GetDescriptionBehaviour class


  // These Behaviours handle interactions with platform tools.

  private class RegisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate subscriptionTemplate;

    RegisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("tool-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
      subscriptionTemplate = MessageTemplate.and(mt1, mt2);
      subscriptionTemplate = MessageTemplate.and(subscriptionTemplate, mt12);

    }

    public void action() {

      // Receive 'subscribe' ACL messages.
      ACLMessage current = receive(subscriptionTemplate);
      if(current != null) {
	// FIXME: Should parse 'iota ?x ...'

	// Get new tool name from subscription message
	AID newTool = current.getSender();

	try {

	  // Send back the whole container list.
	  String[] names = myPlatform.containerNames();
	  for(int i = 0; i < names.length; i++) {

	    String containerName = names[i];

	    // FIXME: Need to retrieve the real host from the platform
	    InetAddress addr = null;
	    try {
	      addr = InetAddress.getLocalHost();
	    }
	    catch(java.net.UnknownHostException jnuhe) {
	      jnuhe.printStackTrace();
	    }

	    String containerHost = addr.getHostName();

	    ContainerBorn cb = new ContainerBorn();
	    cb.setName(containerName);
	    cb.setHost(containerHost);
	    EventOccurred eo = new EventOccurred();
	    eo.setEvent(cb);

	    List l = new ArrayList(1);
	    l.add(eo);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillContent(toolNotification, l);

	    send(toolNotification);

	  }

	  // Send all agent names, along with their container name.
	  AID[] agents = myPlatform.agentNames();
	  for(int i = 0; i < agents.length; i++) {

	    AID agentName = agents[i];
	    String containerName = myPlatform.getContainerName(agentName);

	    AgentBorn ab = new AgentBorn();
	    ab.setContainer(containerName);
	    ab.setAgent(agentName);
	    EventOccurred eo = new EventOccurred();
	    eo.setEvent(ab);

	    List l = new ArrayList(1);
	    l.add(eo);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillContent(toolNotification, l);

	    send(toolNotification);
	  }

	  // Add the new tool to tools list.
	  tools.add(newTool);

	}
	catch(NotFoundException nfe) {
	  nfe.printStackTrace();
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
      else
	block();

    }

  } // End of RegisterToolBehaviour class


  private class DeregisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate cancellationTemplate;

    DeregisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("tool-cancellation");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
      cancellationTemplate = MessageTemplate.and(mt1, mt2);
      cancellationTemplate = MessageTemplate.and(cancellationTemplate, mt12);

    }

    public void action() {

      // Receive 'cancel' ACL messages.
      ACLMessage current = receive(cancellationTemplate);
      if(current != null) {
	// FIXME: Should parse the content

	// Remove this tool to tools agent group.
	tools.remove(current.getSender());

      }
      else
	block();

    }

  } // End of DeregisterToolBehaviour class


  private class NotifyToolsBehaviour extends CyclicBehaviour {

    public void action() {

      synchronized(ams.this) { // Mutual exclusion with handleXXX() methods

	// Look into the event buffer
	Iterator it = eventQueue.iterator();
	EventOccurred eo = new EventOccurred();

	while(it.hasNext()) {

	  // Write the event into the notification message
	  AMSEvent ev = (AMSEvent)it.next();
	  List l = new ArrayList(1);
	  eo.setEvent(ev);
	  l.add(eo);
	  try {
	    fillContent(toolNotification, l);
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
	  }

	  // Put all tools in the receiver list
	  toolNotification.clearAllReceiver();
	  Iterator toolIt = tools.iterator();
	  while(toolIt.hasNext()) {
	    AID tool = (AID)toolIt.next();
	    toolNotification.addReceiver(tool);
	  }

	  send(toolNotification);
	  it.remove();
	}
      }

      block();
    }

  } // End of NotifyToolsBehaviour class

  private class KillContainerBehaviour extends AMSBehaviour {
    public KillContainerBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new KillContainerBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {

      KillContainer kc = (KillContainer)a.get_1();
      String containerName = kc.getName();
      myPlatform.killContainer(containerName);
      //sendReply(ACLMessage.AGREE, " (true)");
      sendReply(ACLMessage.INFORM,"FIXME");

    }

  } // End of KillContainerBehaviour class

  private class CreateBehaviour extends AMSBehaviour {
    public CreateBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new CreateBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      CreateAgent ca = (CreateAgent)a.get_1();

      String agentName = ca.getAgentName();
      String className = ca.getClassName();
      String containerName = ca.getContainerName();

      //sendReply(ACLMessage.AGREE, "(true)");

      try {
	myPlatform.create(agentName, className, containerName);
	// An 'inform Done' message will be sent to the requester only
	// when the newly created agent will register itself with the
	// AMS. The new agent's name will be used as the key in the map.
	ACLMessage reply = getReply();
	reply = (ACLMessage)reply.clone();

	pendingInforms.put(agentName, reply);
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }

    }

  } // End of CreateBehaviour class

  private class KillBehaviour extends AMSBehaviour {
    public KillBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new KillBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      // Kill an agent
      KillAgent ka = (KillAgent)a.get_1();
      AID agentID = ka.getAgent();
      String password = ka.getPassword();

      try {
	myPlatform.kill(agentID, password);
	//sendReply(ACLMessage.AGREE, "FIXME");
	sendReply(ACLMessage.INFORM,"FIXME");
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch(NotFoundException nfe) {
	throw new NotRegistered();
      }

    }

  } // End of KillBehaviour class


  private class SniffAgentOnBehaviour extends AMSBehaviour { 
    public SniffAgentOnBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SniffAgentOnBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      SniffOn so = (SniffOn)a.get_1();
      try {
	myPlatform.sniffOn(so.getSniffer(), so.getAllSniffedAgents());
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
    }

  } // End of SniffAgentOnBehaviour class

  private class SniffAgentOffBehaviour extends AMSBehaviour {
    public SniffAgentOffBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SniffAgentOffBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      SniffOff so = (SniffOff)a.get_1();
      try {
	myPlatform.sniffOff(so.getSniffer(), so.getAllSniffedAgents());
      }
      catch(UnreachableException ue) {
	throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
    }

  } // End of SniffAgentOffBehaviour class




  // The AgentPlatform where information about agents is stored 
  /**
  @serial
  */
  private AgentManager myPlatform;

  // Maintains an association between action names and behaviours to
  // handle 'fipa-agent-management' actions
  /**
  @serial
  */
  private FipaRequestResponderBehaviour dispatcher;

  // Maintains an association between action names and behaviours to
  // handle 'jade-agent-management' actions
  /**
  @serial
  */
  private FipaRequestResponderBehaviour extensionsDispatcher;

  // Contains a main Behaviour and some utilities to handle JADE mobility
  /**
  @serial
  */
  private MobilityManager mobilityMgr;

  // Behaviour to listen to incoming 'subscribe' messages from tools.
  /**
  @serial
  */
  private RegisterToolBehaviour registerTool;

  // Behaviour to broadcats AgentPlatform notifications to each
  // registered tool.
  /**
  @serial
  */
  private NotifyToolsBehaviour notifyTools;

  // Behaviour to listen to incoming 'cancel' messages from tools.
  /**
  @serial
  */
  private DeregisterToolBehaviour deregisterTool;

  // Group of tools registered with this AMS
  /**
  @serial
  */
  private List tools;

  // ACL Message to use for tool notification
  /**
  @serial
  */
  private ACLMessage toolNotification = new ACLMessage(ACLMessage.INFORM);

  // Buffer for AgentPlatform notifications
  /**
  @serial
  */
  private List eventQueue = new ArrayList(10);

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
    myPlatform.addListener(this);

    MessageTemplate mtFIPA = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));
    dispatcher = new FipaRequestResponderBehaviour(this, mtFIPA);

    MessageTemplate mtJADE = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME));
    extensionsDispatcher = new FipaRequestResponderBehaviour(this, mtJADE);

    mobilityMgr = new MobilityManager(this);
    registerTool = new RegisterToolBehaviour();
    deregisterTool = new DeregisterToolBehaviour();
    notifyTools = new NotifyToolsBehaviour();

    tools = new ArrayList();

    toolNotification.setSender(new AID());
    toolNotification.setLanguage(SL0Codec.NAME);
    toolNotification.setOntology("jade-agent-management");
    toolNotification.setInReplyTo("tool-subscription");

    // Associate each AMS action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(FIPAAgentManagementOntology.REGISTER, new RegBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.DEREGISTER, new DeregBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.MODIFY, new ModBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.SEARCH, new SrchBehaviour(null));

    dispatcher.registerFactory(FIPAAgentManagementOntology.GETDESCRIPTION, new GetDescriptionBehaviour(null));

    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.CREATEAGENT, new CreateBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.KILLAGENT, new KillBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.KILLCONTAINER, new KillContainerBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.SNIFFON, new SniffAgentOnBehaviour(null));
    extensionsDispatcher.registerFactory(JADEAgentManagementOntology.SNIFFOFF, new SniffAgentOffBehaviour(null));

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


    // Register the supported ontologies 
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
    registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());
    registerOntology(MobilityOntology.NAME, MobilityOntology.instance());
    
    // register the supported languages
    registerLanguage(SL0Codec.NAME, new SL0Codec());	

    // Add a dispatcher Behaviour for all ams actions following from a
    // 'fipa-request' interaction with 'fipa-agent-management' ontology.
    addBehaviour(dispatcher);

    // Add a dispatcher Behaviour for all ams actions following from a
    // 'fipa-request' interaction with 'jade-agent-management' ontology.
    addBehaviour(extensionsDispatcher);

    // Add a main behaviour to manage mobility related messages
    addBehaviour(mobilityMgr.getMain());

    // Add a Behaviour to accept incoming tool registrations and a
    // Behaviour to broadcast events to registered tools.
    addBehaviour(registerTool);
    addBehaviour(deregisterTool);
    addBehaviour(notifyTools);

  }

  
  /**
  * checks that all the mandatory slots for a register/modify/deregister action
  * are present.
  * @param actionName is the name of the action (one of 
  * <code>FIPAAgentManagementOntology.REGISTER</code>,
  * <code>FIPAAgentManagementOntology.MODIFY</code>,
  * <code>FIPAAgentManagementOntology.DEREGISTER</code>)
  * @param amsd is the AMSAgentDescription to be checked for
  * @throws MissingParameter if one of the mandatory slots is missing
  **/
  private void checkMandatorySlots(String actionName, AMSAgentDescription amsd) throws MissingParameter {
    try {
      AID name = amsd.getName();
      if ((name == null)||(name.getName().length() == 0))
	throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    }
    if (!actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
      try {
	String state = amsd.getState();
	if((state == null)||(state.length() == 0))
	  throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
      } catch (Exception e) {
	e.printStackTrace();
	throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
      }
  }
  
  /**
     @serial
   */
  private KB agentDescriptions = new KBAbstractImpl() {
      protected boolean match(Object template, Object fact) {
	try {
	  AMSAgentDescription templateDesc = (AMSAgentDescription)template;
	  AMSAgentDescription factDesc = (AMSAgentDescription)fact;

	  String o1 = templateDesc.getOwnership();
	  if(o1 != null) {
	    String o2 = factDesc.getOwnership();
	    if((o2 == null) || (!o1.equalsIgnoreCase(o2)))
	      return false;
	  }

	  String s1 = templateDesc.getState();
	  if(s1 != null) {
	    String s2 = factDesc.getState();
	    if((s2 == null) || (!s1.equalsIgnoreCase(s2)))
	      return false;
	  }

	  AID id1 = templateDesc.getName();
	  if(id1 != null) {
	    AID id2 = factDesc.getName();
	    if((id2 == null) || (!matchAID(id1, id2)))
	      return false;
	  }

	  return true;
	}
	catch(ClassCastException cce) {
	  return false;
	}
      }
    };

  /** it is called also by Agent.java **/
  public void AMSRegister(AMSAgentDescription amsd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, amsd);
    Object old = agentDescriptions.register(amsd.getName(), amsd);
    if(old != null)
      throw new AlreadyRegistered();
  }

  /** it is called also by Agent.java **/
  public void AMSDeregister(AMSAgentDescription amsd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, amsd);
    Object old = agentDescriptions.deregister(amsd.getName());
    if(old == null)
      throw new NotRegistered();
  }

  private void AMSModify(AMSAgentDescription amsd) throws FIPAException {
    checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, amsd);
    Object old = agentDescriptions.deregister(amsd.getName());
    if(old == null)
      throw new NotRegistered();
    agentDescriptions.register(amsd.getName(), amsd);
  }

  private List AMSSearch(AMSAgentDescription amsd, SearchConstraints constraints, ACLMessage reply) throws FIPAException {
    // Search has no mandatory slots
    return agentDescriptions.search(amsd);
  }

  // This one is called in response to a 'move-agent' action
  void AMSMoveAgent(AID agentID, Location where) throws FIPAException {
    try {
      myPlatform.move(agentID, where, "");
    }
    catch(UnreachableException ue) {
      throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
    }
    catch(NotFoundException nfe) {
      throw new NotRegistered();
    }
  }

  // This one is called in response to a 'clone-agent' action
  void AMSCloneAgent(AID agentID, Location where, String newName) throws FIPAException {
    try {
      myPlatform.copy(agentID, where, newName, "");
    }
    catch(UnreachableException ue) {
      throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
    }
    catch(NotFoundException nfe) {
      throw new NotRegistered();
    }
  }


  // This one is called in response to a 'where-is-agent' action
  MobilityOntology.Location AMSWhereIsAgent(AID agentID) throws FIPAException {
    try {
      String containerName = myPlatform.getContainerName(agentID);
      return mobilityMgr.getLocation(containerName);
    }
    catch(NotFoundException nfe) {
      nfe.printStackTrace();
      throw new NotRegistered();
    }
  }

  // This one is called in response to a 'query-platform-locations' action
  Iterator AMSGetPlatformLocations() {
    return mobilityMgr.getLocations();
  }


  // Methods to be called from AgentPlatform to notify AMS of special events

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void handleNewContainer(String name, InetAddress addr) {

    // Add a new location to the locations list
    MobilityOntology.Location loc = new MobilityOntology.Location();
    loc.setName(name);
    loc.setTransportProtocol("JADE-IPMT");
    loc.setTransportAddress(getHap() + "." + name);
    mobilityMgr.addLocation(name, loc);

    // Fire a 'container is born' event
    ContainerBorn cb = new ContainerBorn();
    cb.setName(name);
    cb.setHost(addr.getHostName());
    eventQueue.add(cb);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void handleDeadContainer(String name) {
    // Remove the location from the location list
    mobilityMgr.removeLocation(name);

    // Fire a 'container is dead' event
    ContainerDead cd = new ContainerDead();
    cd.setName(name);
    eventQueue.add(cd);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void handleNewAgent(String containerName, AID agentID) {
    AgentBorn ab = new AgentBorn();
    ab.setAgent(agentID);
    ab.setContainer(containerName);
    eventQueue.add(ab);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void handleDeadAgent(String containerName, AID agentID) {
    AgentDead ad = new AgentDead();
    ad.setAgent(agentID);
    ad.setContainer(containerName);
    eventQueue.add(ad);
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void handleMovedAgent(String fromContainer, String toContainer, AID agentID) {
    AgentMoved am = new AgentMoved();
    am.setFrom(fromContainer);
    am.setTo(toContainer);
    am.setAgent(agentID);
    eventQueue.add(am);
    doWake();
  }

} // End of class ams
