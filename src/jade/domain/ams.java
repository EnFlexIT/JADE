/*
  $Log$
  Revision 1.39  1999/11/08 15:26:16  rimassaJade
  Added AMS support for the message sniffer.

  Revision 1.38  1999/08/31 17:28:55  rimassa
  Added automatic notification to registered RMAs whenever an agent
  moves across containers.

  Revision 1.37  1999/08/10 15:39:00  rimassa
  Changed method name in an invocation to the agent platform to reflect
  AgentManager changes.

  Revision 1.36  1999/07/19 00:05:34  rimassa
  Added support for storing and querying a FIPA 98 compliant platform profile.

  Revision 1.35  1999/07/13 19:54:23  rimassa
  Changed AMS implementation to hold inside all the AMS Agent
  Descriptors for the agents of the platform.

  Revision 1.34  1999/06/06 21:52:28  rimassa
  Modified 'create-agent' action to notify an agent's creator when a
  subsequent AMS registration fails.

  Revision 1.33  1999/06/04 07:58:23  rimassa
  Replaced direct AgentPlatformImpl usage with a more restricted
  AgentManager interface, to tighten jade.core encapsulation.

  Revision 1.32  1999/05/20 13:43:18  rimassa
  Moved all behaviour classes in their own subpackage.

  Revision 1.31  1999/05/19 18:20:44  rimassa
  Removed fake RMA authentication. Now every agent can ask the AMS to
  perform Life Cycle Management tasks.

  Revision 1.30  1999/04/06 00:09:55  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.29  1999/03/24 12:20:49  rimassa
  Ported most data structures to newer Java 2 Collection framework.

  Revision 1.28  1999/03/17 13:06:12  rimassa
  A small change to use complete agent GUID in addressing the platform
  Global Descriptor Table.

  Revision 1.27  1999/03/14 17:50:27  rimassa
  Changed acc class to take advantage of new
  FipaRequestResponderBehaviour class.

  Revision 1.26  1999/03/09 13:18:32  rimassa
  Minor changes.

  Revision 1.25  1999/02/25 08:34:57  rimassa
  Changed direct access to 'myName' and 'myAddress' instance variables
  to accessor methods call.
  Changed ams constructor not to require a name anymore.
  Added a customized 'deregisterWithAMS() method to avoid deadlocking on
  agent shutdown.

  Revision 1.24  1999/02/15 11:45:56  rimassa
  Removed a fixed FIXME.

  Revision 1.23  1999/02/04 12:53:37  rimassa
  Modified some error messages for 'fipa-man-exeption' objects.

  Revision 1.22  1999/02/03 10:56:31  rimassa
  Some 'private' instance variables made 'protected', to allow code
  compilation under jdk 1.2.
  Added some missing parentheses to AMS reply messages.

  Revision 1.21  1998/12/08 00:10:20  rimassa
  Removed handmade parsing of message content; now updated
  AMSAction.fromText() method is used.

  Revision 1.20  1998/11/15 23:08:24  rimassa
  Added a new KillContainerBehaviour to support 'kill-container' AMS
  action.

  Revision 1.19  1998/11/09 00:24:29  rimassa
  Replaced older container ID with newer container name.
  Added code to send a snapshot of Agent Platform state (active agent
  containers and agent list on every container) to each newly registered
  Remote Management Agent.

  Revision 1.18  1998/11/05 23:36:31  rimassa
  Added a deregisterRMABehaviour to listen to 'cancel' messages from
  registered Remote Management Agents.

  Revision 1.17  1998/11/03 00:37:33  rimassa
  Added AMS event notification to the Remote Management Agent. Now the
  AMS picks up AgentPlatform events from synchronized buffers and
  forwards them to RMA agent for GUI update.

  Revision 1.16  1998/11/02 02:04:29  rimassa
  Added two new Behaviours to support AMS <-> RMA interactions. The
  first Behaviour listens for incoming 'subscribe' messages from RMA
  agents and adds them to a sequence of listeners. The second one
  forwards to registered listeners each notification the AgentPlatforrm
  sends to the AMS (for example, the AgentPlatform informs AMS whenever
  a new agent container is added or removed to tha AgentPlatform).

  Revision 1.15  1998/11/01 14:59:56  rimassa
  Added a new Behaviour to support Remote Management Agent registration.

  Revision 1.14  1998/10/31 16:43:10  rimassa
  Implemented 'kill-agent' action through a suitable Behaviour; now both
  an agent name and a password are recognized in action
  content. Currently the password is ignored.

  Revision 1.13  1998/10/26 22:38:44  Giovanni
  Modified AMS Behaviour for action 'create-agent': now the syntax of
  the "Proposal for an extension of the Agent Management specifications"
  is used in 'create-agent' action content.

  Revision 1.12  1998/10/26 00:08:47  rimassa
  Added two new Behaviours to support new 'create-agent' and
  'kill-agent' actions. Some modifications to AMSBehaviour abstract base
  class to use it with the two new subclasses, which have different
  parameters. Now the AMS is just like the DF, which has 'search' action
  with different parameters.

  Revision 1.11  1998/10/04 18:01:36  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

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
	informCreator.setType("inform");
	informCreator.setContent("( done ( " + a.getName() + " ) )");
	send(informCreator);
	}

      }
      catch(AgentAlreadyRegisteredException aare) {
	sendAgree();
	sendFailure(aare.getMessage());

	// Inform agent creator that registration failed.
	if(informCreator != null) {
	  informCreator.setType("failure");
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
      reply.setType("inform");
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
      mt2 = MessageTemplate.MatchType("subscribe");
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
	Set s = myPlatform.containerNames();
	Iterator i = s.iterator();
	while(i.hasNext()) {
	  String containerName = (String)i.next();
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
	s = myPlatform.agentNames();
	i = s.iterator();
	while(i.hasNext()) {
          try {
	    String agentName = (String)i.next();
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
      mt2 = MessageTemplate.MatchType("cancel");
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
      Enumeration e = newContainersBuffer.elements();
      while(e.hasMoreElements()) {
	String name = (String)e.nextElement();
	AgentManagementOntology.AMSContainerEvent ev = new AgentManagementOntology.AMSContainerEvent();
	ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWCONTAINER);
	ev.setContainerName(name);
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());
	send(RMANotification, RMAs);
	newContainersBuffer.removeElement(name);
      }
    }

    private void processDeadContainers() {
      Enumeration e = deadContainersBuffer.elements();
      while(e.hasMoreElements()) {
	String name = (String)e.nextElement();
	AgentManagementOntology.AMSContainerEvent ev = new AgentManagementOntology.AMSContainerEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.DEADCONTAINER);
	ev.setContainerName(name);
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());
	send(RMANotification, RMAs);
	deadContainersBuffer.removeElement(name);
      }
    }

    private void processNewAgents() {
      Enumeration e = newAgentsBuffer.elements();
      while(e.hasMoreElements()) {
	AgDesc ad = (AgDesc)e.nextElement();
	AgentManagementOntology.AMSAgentEvent ev = new AgentManagementOntology.AMSAgentEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.NEWAGENT);
	ev.setContainerName(ad.containerName);
	ev.setAgentDescriptor(ad.amsd);
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());
	send(RMANotification, RMAs);
	newAgentsBuffer.removeElement(ad);
      }
    }

    private void processDeadAgents() {
      Enumeration e = deadAgentsBuffer.elements();
      while(e.hasMoreElements()) {
	AgDesc ad = (AgDesc)e.nextElement();
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
	deadAgentsBuffer.removeElement(ad);
      }
    }

    private void processMovedAgents() {
      Enumeration e = movedAgentsBuffer.elements();
      while(e.hasMoreElements()) {
	MotionDesc md = (MotionDesc)e.nextElement();
	AgentManagementOntology.AMSMotionEvent ev = new AgentManagementOntology.AMSMotionEvent();
	ev.setKind(AgentManagementOntology.AMSEvent.MOVEDAGENT);

	ev.setAgentDescriptor(md.desc);
	ev.setSrc(md.src);
	ev.setDest(md.dest);

	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());

	send(RMANotification, RMAs);
	movedAgentsBuffer.removeElement(md);
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

    }
    
    
    public void action() {

      try {

	ACLMessage msg = getRequest();
	AgentManagementOntology.SniffAgentOnAction myAction;
	String content = msg.getContent();
	try {
	  myAction = (AgentManagementOntology.SniffAgentOnAction)
	    AgentManagementOntology.SniffAgentOnAction.fromText(new StringReader(content));			
	  sendAgree();
	  myPlatform.sniffOn(myAction.getSnifferName(),myAction.getEntireList());		
	}
	catch (ParseException e) {
	  sendRefuse(e.getMessage());       		
	}
	catch (TokenMgrError et) {
	  sendRefuse(et.getMessage());       	       		
	}
      }
      catch (UnreachableException ue) {
	sendRefuse(ue.getMessage()); 
      }
    }

  } // End of SniffAgentOnBehaviour class

  private class SniffAgentOffBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new SniffAgentOffBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
    }

    public void action() {
      try {
	ACLMessage msg = getRequest();
	AgentManagementOntology.SniffAgentOffAction myAction;
	String content = msg.getContent();
	try {
	  myAction = (AgentManagementOntology.SniffAgentOffAction)
	    AgentManagementOntology.SniffAgentOffAction.fromText(new StringReader(content));
	  sendAgree();
	  myPlatform.sniffOff(myAction.getSnifferName(),myAction.getEntireList());
      	}
	catch (ParseException e) {
	  sendRefuse(e.getMessage());
      	}
	catch (TokenMgrError et) {
	  sendRefuse(et.getMessage());
      	}
      }
      catch (UnreachableException ue) {
	sendRefuse(ue.getMessage());
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
  private ACLMessage RMANotification = new ACLMessage("inform");

  // Buffers for AgentPlatform notifications
  private Vector newContainersBuffer = new Vector();
  private Vector deadContainersBuffer = new Vector();
  private Vector newAgentsBuffer = new Vector();
  private Vector deadAgentsBuffer = new Vector();
  private Vector movedAgentsBuffer = new Vector();

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
      if(delegateAgentName != null)
	toChange.setDelegateAgentName(delegateAgentName);
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
  public synchronized void postNewContainer(String name) {
    newContainersBuffer.addElement(new String(name));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postDeadContainer(String name) {
    deadContainersBuffer.addElement(new String(name));
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
    newAgentsBuffer.addElement(new AgDesc(containerName, amsd));
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void postDeadAgent(String containerName, String agentName) {
    AgentManagementOntology.AMSAgentDescriptor amsd = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
    deadAgentsBuffer.addElement(new AgDesc(containerName, amsd));
    doWake();
  }

  public synchronized void postMovedAgent(String agentName, String src, String dest) {
    AgentManagementOntology.AMSAgentDescriptor amsd = (AgentManagementOntology.AMSAgentDescriptor)descrTable.get(agentName.toLowerCase());
    movedAgentsBuffer.addElement(new MotionDesc(amsd, src, dest));
    doWake();
  }

} // End of class ams
