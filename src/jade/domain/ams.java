/*
 * $Id$
 */

package jade.domain;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import jade.core.*;
import jade.lang.acl.ACLMessage;


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
public class ams extends Agent { // FIXME: Must become a Singleton

  private abstract class AMSBehaviour extends OneShotBehaviour implements BehaviourPrototype {

    // This String will be set by subclasses
    private String myActionName;

    private ACLMessage myReply;
    private StringTokenizer myTokenizer;

    protected AgentManagementOntology myOntology;

    // These variables are set by crackMessage() method to the
    // attribute values of message content
    private String agentName;
    private String address;
    private String signature;
    private String delegateAgent;
    private String forwardAddress;
    private String APState;

    protected AMSBehaviour(String actionName) {
      super(ams.this);
      myActionName = actionName;
      myReply = null;
      myTokenizer = null;
      myOntology = AgentManagementOntology.instance();
    }

    protected AMSBehaviour(String actionName, ACLMessage msg, StringTokenizer st) {
      super(ams.this);
      myActionName = actionName;
      myReply = msg;
      myTokenizer = st;
      myOntology = AgentManagementOntology.instance();
    }

    // This method throws a FIPAException if the attribute is
    // mandatory for the current AMS action but it is a null object
    // reference
    private void checkAttribute(String attributeName, String attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForAMS(myActionName, attributeName) && (attributeValue == null))
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
    }

    // This method parses the message content and puts
    // 'fipa-ams-agent-description' attribute values in instance
    // variables. If some error is found a FIPA exception is thrown
    private void crackMessage() throws FIPAException, NoSuchElementException {

      Hashtable attributes = new Hashtable();

      while(myTokenizer.hasMoreTokens()) {

	String name = myTokenizer.nextToken();

	// Make sure that keyword is defined in
	// 'fipa-man-ams-agent-description'
	if(!myOntology.isValidAMSADKeyword(name))
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);

	String value = myTokenizer.nextToken();

	Object old = attributes.put(name, value);
	// Check if attribute exists already. If so, raise an
	// exception
	if(old != null)
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
      }


      // Finally, assign each attribute value to an instance variable,
      // making sure mandatory attributes for the current AMS action
      // are non-null

      agentName = (String)attributes.get(AgentManagementOntology.AMSAgentDescription.NAME);
      checkAttribute(AgentManagementOntology.AMSAgentDescription.NAME, agentName);

      address = (String)attributes.get(AgentManagementOntology.AMSAgentDescription.ADDRESS);
      checkAttribute(AgentManagementOntology.AMSAgentDescription.ADDRESS, address);

      signature = (String)attributes.get(AgentManagementOntology.AMSAgentDescription.SIGNATURE);
      checkAttribute(AgentManagementOntology.AMSAgentDescription.SIGNATURE, signature);

      delegateAgent = (String)attributes.get(AgentManagementOntology.AMSAgentDescription.DELEGATE);
      checkAttribute(AgentManagementOntology.AMSAgentDescription.DELEGATE, delegateAgent);

      forwardAddress = (String)attributes.get(AgentManagementOntology.AMSAgentDescription.FORWARD);
      checkAttribute(AgentManagementOntology.AMSAgentDescription.FORWARD, forwardAddress);

      APState = (String)attributes.get(AgentManagementOntology.AMSAgentDescription.APSTATE);
      checkAttribute(AgentManagementOntology.AMSAgentDescription.APSTATE, APState);

    }

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAttributes(String agentName, String address,
					      String signature, String delegateAgent,
					      String forwardAddress, String APState);

    public void action() {

      try {
	// Convert message from untyped keyword/value list to ordinary
	// typed variables, throwing a FIPAException in case of errors
	crackMessage();
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, fe.getMessage());
	return;
      }
      catch(NoSuchElementException nsee) {
	sendRefuse(myReply, AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
	return;
      }

      // Do real action, deferred to subclasses
      processAttributes(agentName, address, signature, delegateAgent, forwardAddress, APState);

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
      msg.setContent("( ams action " + myActionName + " ) " + reason);
      send(msg);
    }

    // Send a 'failure' message back to the requester
    protected void sendFailure(ACLMessage msg, String reason) {
      msg.setType("failure");
      msg.setContent("( ams action " + myActionName + " ) " + reason);
      send(msg);
    }

    // Send an 'agree' message back to the requester
    protected void sendAgree(ACLMessage msg) {
      msg.setType("agree");
      msg.setContent("( ams action " + myActionName + " )");
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
      super(AgentManagementOntology.AMSActions.AUTHENTICATE);
    }

    public AuthBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.AMSActions.AUTHENTICATE, msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new AuthBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String address, String signature,
				     String delegateAgent, String forwardAddress, String APState) {

      sendRefuse(myReply, AgentManagementOntology.Exception.UNWILLING); // FIXME: Not Implemented

      if(agentName != null)
	myPlatform.AMSDumpData(agentName);
      else
	myPlatform.AMSDumpData();

    }

  } // End of AuthBehaviour class


  private class RegBehaviour extends AMSBehaviour {

    public RegBehaviour() {
      super(AgentManagementOntology.AMSActions.REGISTERAGENT);
    }

    public RegBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.AMSActions.REGISTERAGENT, msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new RegBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String address, String signature,
				     String delegateAgent, String forwardAddress, String APState) {

      // Write new agent data in Global Agent Descriptor Table
      try {
	myPlatform.AMSNewData(agentName, address, signature, delegateAgent,
			      forwardAddress, APState);
      }
      catch(AgentAlreadyRegisteredException aare) {
	sendAgree(myReply);
	sendFailure(myReply, aare.getMessage());
	return;
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, fe.getMessage());
	return;
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  } // End of RegBehaviour class


  private class DeregBehaviour extends AMSBehaviour {

    public DeregBehaviour() {
      super(AgentManagementOntology.AMSActions.DEREGISTERAGENT);
    }

    public DeregBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.AMSActions.DEREGISTERAGENT, msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new DeregBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String address, String signature,
				     String delegateAgent, String forwardAddress, String APState) {
      try {
	// Remove the agent data from Global Descriptor Table
	myPlatform.AMSRemoveData(agentName, address, signature, delegateAgent,
				 forwardAddress, APState);
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, fe.getMessage());
	return;
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  } // End of DeregBehaviour class


  private class ModBehaviour extends AMSBehaviour {

    public ModBehaviour() {
      super(AgentManagementOntology.AMSActions.MODIFYAGENT);
    }

    public ModBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.AMSActions.MODIFYAGENT, msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new ModBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String address, String signature,
				     String delegateAgent, String forwardAddress, String APState) {

      try {
	// Modify agent data from Global Descriptor Table
	myPlatform.AMSChangeData(agentName, address, signature, delegateAgent,
				 forwardAddress, APState);
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, fe.getMessage());
	return;
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  } // End of ModBehaviour class


  // The AgentPlatform where information about agents is stored 
  private AgentPlatformImpl myPlatform;

  // Maintains an association between action names and behaviours
  private FipaRequestServerBehaviour dispatcher;

  public ams(AgentPlatformImpl ap, String name) {
    myPlatform = ap;
    myName = name;

    dispatcher = new FipaRequestServerBehaviour(this);

    // Associate each AMS action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerPrototype(AgentManagementOntology.AMSActions.AUTHENTICATE, new AuthBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSActions.REGISTERAGENT, new RegBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSActions.DEREGISTERAGENT, new DeregBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.AMSActions.MODIFYAGENT, new ModBehaviour());

  }

  protected void setup() {

    // Add a dispatcher behaviour
    addBehaviour(dispatcher);

  }


  // The AMS must have a special version for this method, or a deadlock will occur...
  public void registerWithAMS(String signature, String delegateAgent,
		       String forwardAddress, int APState) {

    // Skip all fipa-request protocol and go straight to the target
    
    try {
      myPlatform.AMSNewData(myName, myAddress, signature, delegateAgent,
			    forwardAddress, "active");
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
