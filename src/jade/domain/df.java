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

  Name: df

  Responsibility and Collaborations:

  + Serves as Directory Facilitator for the Agent Platform, according
    to FIPA 97 specification.

****************************************************************/
public class df extends Agent {

  private abstract class DFBehaviour extends OneShotBehaviour implements BehaviourPrototype {

    // This String will be set by subclasses
    private String myActionName;

    private ACLMessage myReply;
    private StringTokenizer myTokenizer;
    private DFDescriptionParser myParser = DFDescriptionParser.create();

    protected AgentManagementOntology myOntology;

    // These variables are set by crackMessage() method to the
    // attribute values of message content
    private String agentName;
    private String agentServices;
    private String agentType;
    private String interactionProtocols;
    private String ontology;
    private String address;
    private String ownership;
    private String DFState;

    protected DFBehaviour(String actionName) {
      super(df.this);
      myActionName = actionName;
      myReply = null;
      myTokenizer = null;
      myOntology = AgentManagementOntology.instance();
    }

    protected DFBehaviour(String actionName, ACLMessage msg, StringTokenizer st) {
      super(df.this);
      myActionName = actionName;
      myReply = msg;
      myTokenizer = st;
      myOntology = AgentManagementOntology.instance();
    }

    // This method throws a FIPAException if the attribute is
    // mandatory for the current DF action but it is a null object
    // reference
    private void checkAttribute(String attributeName, String attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForDF(myActionName, attributeName) && (attributeValue == null))
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
    }

    // This method parses the message content and puts
    // 'fipa-df-agent-description' attribute values in instance
    // variables. If some error is found a FIPA exception is thrown
    private void crackMessage() throws FIPAException, NoSuchElementException {

      Hashtable attributes = new Hashtable();

      while(myTokenizer.hasMoreTokens()) {

	String name = myTokenizer.nextToken();

	// Make sure that keyword is defined in
	// 'fipa-man-ams-agent-description'
	if(!myOntology.isValidDFADKeyword(name))
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);

	String value = myTokenizer.nextToken();

	Object old = attributes.put(name, value);
	// Check if attribute exists already. If so, raise an
	// exception
	if(old != null)
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
      }


      // Finally, assign each attribute value to an instance variable,
      // making sure mandatory attributes for the current DF action
      // are non-null

      agentName = (String)attributes.get(AgentManagementOntology.DFAgentDescription.NAME);
      checkAttribute(AgentManagementOntology.DFAgentDescription.NAME, agentName);

      agentServices = (String)attributes.get(AgentManagementOntology.DFAgentDescription.SERVICES);
      checkAttribute(AgentManagementOntology.DFAgentDescription.SERVICES, agentServices);

      agentType = (String)attributes.get(AgentManagementOntology.DFAgentDescription.TYPE);
      checkAttribute(AgentManagementOntology.DFAgentDescription.TYPE, agentType);

      interactionProtocols = (String)attributes.get(AgentManagementOntology.DFAgentDescription.PROTOCOLS);
      checkAttribute(AgentManagementOntology.DFAgentDescription.PROTOCOLS, interactionProtocols);

      ontology = (String)attributes.get(AgentManagementOntology.DFAgentDescription.ONTOLOGY);
      checkAttribute(AgentManagementOntology.DFAgentDescription.ONTOLOGY, ontology);

      address = (String)attributes.get(AgentManagementOntology.DFAgentDescription.ADDRESS);
      checkAttribute(AgentManagementOntology.DFAgentDescription.ADDRESS, address);

      ownership = (String)attributes.get(AgentManagementOntology.DFAgentDescription.OWNERSHIP);
      checkAttribute(AgentManagementOntology.DFAgentDescription.OWNERSHIP, ownership);

      DFState = (String)attributes.get(AgentManagementOntology.DFAgentDescription.DFSTATE);
      checkAttribute(AgentManagementOntology.DFAgentDescription.DFSTATE, DFState);

    }

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAttributes(String agentName, String agentServices,
					      String agentType, String interactionProtocols,
					      String ontology, String address,
					      String ownership, String DFState);

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
      processAttributes(agentName, agentServices, agentType, interactionProtocols, ontology, address, ownership, DFState);

    }


    // The following methods handle the various possibilities arising in
    // DF <-> Agent interaction. They all receive an ACL message as an
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


  } // End of DFBehaviour class


  // These four concrete classes serve both as a Prototype and as an
  // Instance: when seen as BehaviourPrototype they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Behaviour they process their request and terminate.

  private class RegBehaviour extends DFBehaviour {

    public RegBehaviour() {
      super(AgentManagementOntology.DFActions.REGISTER);
    }

    public RegBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.DFActions.REGISTER);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new RegBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String agentServices,
				     String agentType, String interactionProtocols,
				     String ontology, String address,
				     String ownership, String DFState) {
      try {
	DFRegister(agentName, agentServices, agentType, interactionProtocols, ontology, address, ownership, DFState);
      }
      catch(Exception e) {
	e.printStackTrace();
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  }

  private class DeregBehaviour extends DFBehaviour {

    public DeregBehaviour() {
      super(AgentManagementOntology.DFActions.DEREGISTER);
    }

    public DeregBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.DFActions.DEREGISTER);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new DeregBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String agentServices,
				     String agentType, String interactionProtocols,
				     String ontology, String address,
				     String ownership, String DFState) {
      try {
	DFDeregister(agentName, agentServices, agentType, interactionProtocols, ontology, address, ownership, DFState);
      }
      catch(Exception e) {
	e.printStackTrace();
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  }

  private class ModBehaviour extends DFBehaviour {

    public ModBehaviour() {
      super(AgentManagementOntology.DFActions.MODIFY);
    }

    public ModBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.DFActions.MODIFY);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new ModBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String agentServices,
				     String agentType, String interactionProtocols,
				     String ontology, String address,
				     String ownership, String DFState) {
      try {
	DFModify(agentName, agentServices, agentType, interactionProtocols, ontology, address, ownership, DFState);
      }
      catch(Exception e) {
	e.printStackTrace();
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  }

  private class SrchBehaviour extends DFBehaviour {

    public SrchBehaviour() {
      super(AgentManagementOntology.DFActions.SEARCH);
    }

    public SrchBehaviour(ACLMessage msg, StringTokenizer st) {
      super(AgentManagementOntology.DFActions.SEARCH);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new SrchBehaviour(msg, st);
    }

    protected void processAttributes(String agentName, String agentServices,
				     String agentType, String interactionProtocols,
				     String ontology, String address,
				     String ownership, String DFState) {
      try {
	DFSearch(agentName, agentServices, agentType, interactionProtocols, ontology, address, ownership, DFState);
      }
      catch(Exception e) {
	e.printStackTrace();
      }

      sendAgree(myReply);
      sendInform(myReply);

    }

  }


  private FipaRequestServerBehaviour dispatcher;

  public df() {

    dispatcher = new FipaRequestServerBehaviour(this);

    // Associate each DF action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerPrototype(AgentManagementOntology.DFActions.REGISTER, new RegBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.DFActions.DEREGISTER, new DeregBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.DFActions.MODIFY, new ModBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.DFActions.SEARCH, new SrchBehaviour());

  }

  protected void setup() {

    // add a message dispatcher behaviour
    addBehaviour(dispatcher);
  }

  private void DFRegister(String agentName, String agentServices,
			  String agentType, String interactionProtocols,
			  String ontology, String address,
			  String ownership, String DFState) {
  }

  private void DFDeregister(String agentName, String agentServices,
			    String agentType, String interactionProtocols,
			    String ontology, String address,
			    String ownership, String DFState) {
  }

  private void DFModify(String agentName, String agentServices,
			String agentType, String interactionProtocols,
			String ontology, String address,
			String ownership, String DFState) {
  }

  private void DFSearch(String agentName, String agentServices,
			String agentType, String interactionProtocols,
			String ontology, String address,
			String ownership, String DFState) {
  }

}
