/*
  $Log$
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

import java.util.NoSuchElementException;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


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

  private abstract class AMSBehaviour extends OneShotBehaviour implements BehaviourPrototype {

    // This Action will be set by crackMessage()
    private AgentManagementOntology.AMSAction myAction;

    private String myActionName;
    private ACLMessage myRequest;
    private ACLMessage myReply;

    protected AgentManagementOntology myOntology;


    protected AMSBehaviour(String name) {
      super(ams.this);
      myActionName = name;
      myRequest = null;
      myReply = null;
      myOntology = AgentManagementOntology.instance();
    }

    protected AMSBehaviour(String name, ACLMessage request, ACLMessage reply) {
      super(ams.this);
      myActionName = name;
      myRequest = request;
      myReply = reply;
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
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
    }

    // This method parses the message content and puts
    // 'FIPA-AMS-description' attribute values in instance
    // variables. If some error is found a FIPA exception is thrown
    private void crackMessage() throws FIPAException, NoSuchElementException {

      String content = myRequest.getContent();

      // Remove 'action ams' from content string
      content = content.substring(content.indexOf("ams") + 3); // FIXME: AMS could crash for a bad msg

      // Obtain an AMS action from message content
      try {
	myAction = AgentManagementOntology.AMSAction.fromText(new StringReader(content));
      }
      catch(ParseException pe) {
	//	pe.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
      }
      catch(TokenMgrError tme) {
	//      tme.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
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
	sendRefuse(myReply, fe.getMessage());
      }
      catch(NoSuchElementException nsee) {
	sendRefuse(myReply, AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
      }

    }


    // The following methods handle the various possibilities arising in
    // AMS <-> Agent interaction. They all receive an ACL message as an
    // argument, most of whose fields have already been set. Only the
    // message type and message content have to be filled in.

    // Send a 'not-understood' message back to the requester
    protected void sendNotUnderstood(ACLMessage msg) {
      msg.setType("not-understood");
      msg.setContent("");
      send(msg);
    }

    // Send a 'refuse' message back to the requester
    protected void sendRefuse(ACLMessage msg, String reason) {
      msg.setType("refuse");
      msg.setContent("( action ams " + myActionName + " ) " + reason);
      send(msg);
    }

    // Send a 'failure' message back to the requester
    protected void sendFailure(ACLMessage msg, String reason) {
      msg.setType("failure");
      msg.setContent("( action ams " + myActionName + " ) " + reason);
      send(msg);
    }

    // Send an 'agree' message back to the requester
    protected void sendAgree(ACLMessage msg) {
      msg.setType("agree");
      msg.setContent("( action ams " + myActionName + " )");
      send(msg);
    }

    // Send an 'inform' message back to the requester
    protected void sendInform(ACLMessage msg) {
      msg.setType("inform");
      msg.setContent("( done ( " + myActionName + " ) )");
      send(msg);
    }


  } // End of AMSBehaviour class


  // These four concrete classes serve both as a Prototype and as an
  // Instance: when seen as BehaviourPrototype they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Behaviour they process their request and terminate.

  private class AuthBehaviour extends AMSBehaviour {

    public AuthBehaviour() {
      super(AgentManagementOntology.AMSAction.AUTHENTICATE);
    }

    public AuthBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.AMSAction.AUTHENTICATE, request, reply);
    }

    public Behaviour instance(ACLMessage msg, ACLMessage reply) {
      return new AuthBehaviour(msg, reply);
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

    public RegBehaviour() {
      super(AgentManagementOntology.AMSAction.REGISTERAGENT);
    }

    public RegBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.AMSAction.REGISTERAGENT, request, reply);
    }

    public Behaviour instance(ACLMessage msg, ACLMessage reply) {
      return new RegBehaviour(msg, reply);
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      // Write new agent data in Global Agent Descriptor Table
      try {
	myPlatform.AMSNewData(amsd.getName(), amsd.getAddress(), amsd.getSignature(),amsd.getAPState(),
			      amsd.getDelegateAgentName(), amsd.getForwardAddress(), amsd.getOwnership());
	sendAgree(myReply);
	sendInform(myReply);
      }
      catch(AgentAlreadyRegisteredException aare) {
	sendAgree(myReply);
	sendFailure(myReply, aare.getMessage());
      }

    }

  } // End of RegBehaviour class


  private class DeregBehaviour extends AMSBehaviour {

    public DeregBehaviour() {
      super(AgentManagementOntology.AMSAction.DEREGISTERAGENT);
    }

    public DeregBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.AMSAction.DEREGISTERAGENT, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new DeregBehaviour(request, reply);
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      // Remove the agent data from Global Descriptor Table
      myPlatform.AMSRemoveData(amsd.getName(), amsd.getAddress(), amsd.getSignature(), amsd.getAPState(),
			       amsd.getDelegateAgentName(), amsd.getForwardAddress(), amsd.getOwnership());
      sendAgree(myReply);
      sendInform(myReply);
    }

  } // End of DeregBehaviour class


  private class ModBehaviour extends AMSBehaviour {

    public ModBehaviour() {
      super(AgentManagementOntology.AMSAction.MODIFYAGENT);
    }

    public ModBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.AMSAction.MODIFYAGENT, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new ModBehaviour(request, reply);
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();

      checkMandatory(amsd);

      // Modify agent data from Global Descriptor Table
      myPlatform.AMSChangeData(amsd.getName(), amsd.getAddress(), amsd.getSignature(), amsd.getAPState(),
			       amsd.getDelegateAgentName(), amsd.getForwardAddress(), amsd.getOwnership());
      sendAgree(myReply);
      sendInform(myReply);
    }

  } // End of ModBehaviour class


  private class CreateBehaviour extends AMSBehaviour {

    CreateBehaviour() {
      super(AgentManagementOntology.AMSAction.CREATEAGENT);
    }

    CreateBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.AMSAction.CREATEAGENT, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new CreateBehaviour(request, reply);
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

      // Make sure it is RMA that's calling
      String peerName = myRequest.getSource();
      if(!peerName.equalsIgnoreCase("RMA"))
	 throw myOntology.getException(AgentManagementOntology.Exception.UNAUTHORISED);

      AgentManagementOntology.CreateAgentAction caa = (AgentManagementOntology.CreateAgentAction)a;
      String className = caa.getClassName();
      int containerID = caa.getContainerID();

      // Create a new agent
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();
      myPlatform.AMSCreateAgent(amsd.getName(), className, containerID);

      sendAgree(myReply);
      sendInform(myReply);

    }

  } // End of CreateBehaviour class

  private class KillBehaviour extends AMSBehaviour {

    KillBehaviour() {
      super(AgentManagementOntology.AMSAction.KILLAGENT);
    }

    KillBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.AMSAction.KILLAGENT, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new KillBehaviour(request, reply);
    }

    protected void processAction(AgentManagementOntology.AMSAction a) throws FIPAException {

    // Make sure it is RMA that's calling
    String peerName = myRequest.getSource();
    if(!peerName.equalsIgnoreCase("RMA"))
       throw myOntology.getException(AgentManagementOntology.Exception.UNAUTHORISED);

      // Create a new agent
      AgentManagementOntology.AMSAgentDescriptor amsd = a.getArg();
      myPlatform.AMSKillAgent(amsd.getName());

      sendAgree(myReply);
      sendInform(myReply);

    }

  } // End of KillBehaviour class


  // The AgentPlatform where information about agents is stored 
  private AgentPlatformImpl myPlatform;

  // Maintains an association between action names and behaviours
  private FipaRequestServerBehaviour dispatcher;

  public ams(AgentPlatformImpl ap, String name) {
    myPlatform = ap;
    myName = name;

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));
    dispatcher = new FipaRequestServerBehaviour(this, mt);


    // Associate each AMS action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerPrototype(AgentManagementOntology.AMSAction.AUTHENTICATE, new AuthBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSAction.REGISTERAGENT, new RegBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSAction.DEREGISTERAGENT, new DeregBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSAction.MODIFYAGENT, new ModBehaviour());

    dispatcher.registerPrototype(AgentManagementOntology.AMSAction.CREATEAGENT, new CreateBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSAction.KILLAGENT, new KillBehaviour());

  }

  protected void setup() {

    // Add a dispatcher behaviour
    addBehaviour(dispatcher);

  }


  // The AMS must have a special version for this method, or a deadlock will occur...
  public void registerWithAMS(String signature, int APState, String delegateAgentName,
		       String forwardAddress, String ownership) {

    // Skip all fipa-request protocol and go straight to the target
    
    try { // FIXME: APState parameter is never used
      myPlatform.AMSNewData(myName + '@' + myAddress, myAddress, signature, "active", delegateAgentName,
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



} // End of class ams
