/*
  $Log$
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

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import jade.core.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.FipaRequestResponderBehaviour;

/**************************************************************

  Name: ams

  Responsibility and Collaborations:

  + Serves as Agent Management System for the Agent Platform,
    according to FIPA 97 specification.

  + Handles platform internal data to describe all the agents present
    on the platform.
    (AgentPlatformImpl)

  + Manages AP Life-Cycle of Agent objects.
    (Agent)

****************************************************************/
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
      if(agentName != null)
	myPlatform.AMSDumpData(agentName);
      else
	myPlatform.AMSDumpData();

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

      // Write new agent data in Global Agent Descriptor Table
      try {
	myPlatform.AMSNewData(amsd.getName(), amsd.getAddress(), amsd.getSignature(),amsd.getAPState(),
			      amsd.getDelegateAgentName(), amsd.getForwardAddress(), amsd.getOwnership());
	sendAgree();
	sendInform();
      }
      catch(AgentAlreadyRegisteredException aare) {
	sendAgree();
	sendFailure(aare.getMessage());
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
      myPlatform.AMSRemoveData(amsd.getName(), amsd.getAddress(), amsd.getSignature(), amsd.getAPState(),
			       amsd.getDelegateAgentName(), amsd.getForwardAddress(), amsd.getOwnership());
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
      myPlatform.AMSChangeData(amsd.getName(), amsd.getAddress(), amsd.getSignature(), amsd.getAPState(),
			       amsd.getDelegateAgentName(), amsd.getForwardAddress(), amsd.getOwnership());
      sendAgree();
      sendInform();
    }

  } // End of ModBehaviour class


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
	Enumeration e = myPlatform.AMSContainerNames();
	while(e.hasMoreElements()) {
	  String containerName = (String)e.nextElement();
	  AgentManagementOntology.AMSContainerEvent ev = new AgentManagementOntology.AMSContainerEvent();
	  ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWCONTAINER);
	  ev.setContainerName(containerName);
	  StringWriter w = new StringWriter();
	  ev.toText(w);

	  RMANotification.setDest(newRMA);
	  RMANotification.setContent(w.toString());
	  send(RMANotification);

	}

	// Send all agent names, along with their container name.
	e = myPlatform.AMSAgentNames();
	while(e.hasMoreElements()) {
          try {
	    String agentName = (String)e.nextElement();
	    String containerName = myPlatform.AMSGetContainerName(agentName);
	    String agentAddress = myPlatform.AMSGetAddress(agentName);
	    AgentManagementOntology.AMSAgentDescriptor amsd = new AgentManagementOntology.AMSAgentDescriptor();
	    amsd.setName(agentName + '@' + agentAddress); // FIXME: 'agentName' should contain the address, too.
	    amsd.setAddress(agentAddress);
	    amsd.setAPState(Agent.AP_ACTIVE);

	    AgentManagementOntology.AMSAgentEvent ev = new AgentManagementOntology.AMSAgentEvent();
	    ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWAGENT);
	    ev.setContainerName(containerName);
	    ev.setAgentDescriptor(amsd);
	    StringWriter w = new StringWriter();
	    ev.toText(w);

	    RMANotification.setContent(w.toString());
	    RMANotification.setDest(newRMA);
	    send(RMANotification);
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
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
	ev.setKind(AgentManagementOntology.AMSContainerEvent.DEADCONTAINER);
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
	ev.setKind(AgentManagementOntology.AMSContainerEvent.NEWAGENT);
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
	ev.setKind(AgentManagementOntology.AMSContainerEvent.DEADAGENT);
	ev.setContainerName(ad.containerName);
	ev.setAgentDescriptor(ad.amsd);
	StringWriter w = new StringWriter();
	ev.toText(w);
	RMANotification.setContent(w.toString());

	send(RMANotification, RMAs);
	deadAgentsBuffer.removeElement(ad);
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
      }

      block();

    }

  } // End of NotifyRMAsBehaviour class

  private class KillContainerBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new KillContainerBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

      // Make sure it is RMA that's calling
      ACLMessage msg = getRequest();
      String peerName = msg.getSource();
      if(!peerName.equalsIgnoreCase("RMA"))
	 throw myOntology.getException(AgentManagementOntology.Exception.UNAUTHORISED);

      // Obtain container name and ask AgentPlatform to kill it
      AgentManagementOntology.KillContainerAction kca = (AgentManagementOntology.KillContainerAction)a;
      String containerName = kca.getContainerName();
      myPlatform.AMSKillContainer(containerName);
      sendAgree();
      sendInform();
    }

  } // End of KillContainerBehaviour class

  private class CreateBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new CreateBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

      // Make sure it is RMA that's calling
      ACLMessage msg = getRequest();
      String peerName = msg.getSource();
      if(!peerName.equalsIgnoreCase("RMA"))
	 throw myOntology.getException(AgentManagementOntology.Exception.UNAUTHORISED);

      AgentManagementOntology.CreateAgentAction caa = (AgentManagementOntology.CreateAgentAction)a;
      String className = caa.getClassName();
      String containerName = caa.getProperty(AgentManagementOntology.CreateAgentAction.CONTAINER);

      // Create a new agent
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();
      myPlatform.AMSCreateAgent(amsd.getName(), className, containerName);

      sendAgree();
      sendInform();

    }

  } // End of CreateBehaviour class

  private class KillBehaviour extends AMSBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new KillBehaviour();
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

    // Make sure it is RMA that's calling
    ACLMessage msg = getRequest();
    String peerName = msg.getSource();
    if(!peerName.equalsIgnoreCase("RMA"))
       throw myOntology.getException(AgentManagementOntology.Exception.UNAUTHORISED);

      // Kill an agent
      AgentManagementOntology.KillAgentAction kaa = (AgentManagementOntology.KillAgentAction)a;
      String agentName = kaa.getAgentName();
      String password = kaa.getPassword();
      myPlatform.AMSKillAgent(agentName, password);

      sendAgree();
      sendInform();

    }

  } // End of KillBehaviour class


  private static class AgDesc {

    public AgDesc(String s, AgentManagementOntology.AMSAgentDescriptor a) {
      containerName = s;
      amsd = a;
    }

    public String containerName;
    public AgentManagementOntology.AMSAgentDescriptor amsd;

  }

  // The AgentPlatform where information about agents is stored 
  private AgentPlatformImpl myPlatform;

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

  public ams(AgentPlatformImpl ap) {
    myPlatform = ap;

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

    dispatcher.registerFactory(AgentManagementOntology.AMSAction.CREATEAGENT, new CreateBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.KILLAGENT, new KillBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.AMSAction.KILLCONTAINER, new KillContainerBehaviour());

  }

  protected void setup() {

    // Add a dispatcher Behaviour for all ams actions following from a
    // 'fipa-request' interaction
    addBehaviour(dispatcher);

    // Add a Behaviour to accept incoming RMA registrations and a
    // Behaviour to broadcast events to registered RMAs.
    addBehaviour(registerRMA);
    addBehaviour(deregisterRMA);
    addBehaviour(notifyRMAs);

  }


  // The AMS must have a special version for this method, or a deadlock will occur...
  public void registerWithAMS(String signature, int APState, String delegateAgentName,
		       String forwardAddress, String ownership) {

    // Skip all fipa-request protocol and go straight to the target
    
    try { // FIXME: APState parameter is never used
      myPlatform.AMSNewData(getName(), getAddress(), signature, "active", delegateAgentName,
			    forwardAddress, ownership);
    }
    // No exception should occur since this is a special case ...
    catch(AgentAlreadyRegisteredException aare) {
      aare.printStackTrace();
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  // The AMS must have a special version for this method, or a deadlock will occur...
  public void deregisterWithAMS() throws FIPAException {
    myPlatform.AMSRemoveData(getName(), getAddress(), null, "active", null, null, null);
  }

  // Methods to be called from AgentPlatform to notify AMS of special events

  public synchronized void postNewContainer(String name) {
    newContainersBuffer.addElement(new String(name));
    doWake();
  }

  public synchronized void postDeadContainer(String name) {
    deadContainersBuffer.addElement(new String(name));
    doWake();
  }

  public synchronized void postNewAgent(String containerName, AgentManagementOntology.AMSAgentDescriptor amsd) {
    newAgentsBuffer.addElement(new AgDesc(containerName, amsd));
    doWake();
  }

  public synchronized void postDeadAgent(String containerName, AgentManagementOntology.AMSAgentDescriptor amsd) {
    deadAgentsBuffer.addElement(new AgDesc(containerName, amsd));
    doWake();
  }

} // End of class ams
