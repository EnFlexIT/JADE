/*
 * $Id$
 */

package jade.domain;

import java.io.StringReader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**************************************************************

  Name: df

  Responsibility and Collaborations:

  + Serves as Directory Facilitator for the Agent Platform, according
    to FIPA 98 specification.

****************************************************************/
public class df extends Agent {

  private abstract class DFBehaviour extends OneShotBehaviour implements BehaviourPrototype {

    // This String will be set by subclasses
    private AgentManagementOntology.DFAction myAction;

    private String myActionName;
    private ACLMessage myRequest;
    private ACLMessage myReply;

    protected DFBehaviour(String name) {
      super(df.this);
      myActionName = name;
      myRequest = null;
      myReply = null;
    }

    protected DFBehaviour(String name, ACLMessage request, ACLMessage reply) {
      super(df.this);
      myActionName = name;
      myRequest = request;
      myReply = reply;
    }

    // This method throws a FIPAException if the attribute is
    // mandatory for the current DF action but it is a null object
    // reference
    private void checkAttribute(String attributeName, String attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForDF(myAction.getName(), attributeName) && (attributeValue == null))
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
    }

    private void checkAttributeList(String attributeName, Enumeration attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForDF(myAction.getName(), attributeName) && (!attributeValue.hasMoreElements()))
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
    }

    // This method parses the message content and puts
    // 'FIPA-DF-agent-description' attribute values in instance
    // variables. If some error is found a FIPA exception is thrown
    private void crackMessage() throws FIPAException, NoSuchElementException {

      String content = myRequest.getContent();

      // Remove 'action df' from content string
      content = content.substring(content.indexOf("df") + 2); // FIXME: DF could crash for a bad msg

      // Obtain a DF action from message content
      try {
	myAction = AgentManagementOntology.DFAction.fromText(new StringReader(content));
      }
      catch(ParseException pe) {
	//      pe.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
      }
      catch(TokenMgrError tme) {
	//      tme.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
      }

      // Finally, assign each attribute value to an instance variable,
      // making sure mandatory attributes for the current DF action
      // are non-null
      AgentManagementOntology.DFAgentDescriptor dfd = myAction.getArg();

      checkAttribute(AgentManagementOntology.DFAgentDescriptor.NAME, dfd.getName());
      checkAttributeList(AgentManagementOntology.DFAgentDescriptor.SERVICES, dfd.getAgentServices());
      checkAttribute(AgentManagementOntology.DFAgentDescriptor.TYPE, dfd.getType());
      checkAttributeList(AgentManagementOntology.DFAgentDescriptor.PROTOCOLS, dfd.getInteractionProtocols());
      checkAttribute(AgentManagementOntology.DFAgentDescriptor.ONTOLOGY, dfd.getOntology());
      checkAttributeList(AgentManagementOntology.DFAgentDescriptor.ADDRESS, dfd.getAddresses());
      checkAttribute(AgentManagementOntology.DFAgentDescriptor.OWNERSHIP, dfd.getOwnership());
      checkAttribute(AgentManagementOntology.DFAgentDescriptor.DFSTATE, dfd.getDFState());

    }

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processArgs(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException;

    public void action() {

      try {
	// Convert message from untyped keyword/value list to ordinary
	// typed variables, throwing a FIPAException in case of errors
	crackMessage();

	// Do real action, deferred to subclasses
	processArgs(myAction.getArg());

      }
      catch(FIPAException fe) {
	sendRefuse(myReply, fe.getMessage());
      }
      catch(NoSuchElementException nsee) {
	sendRefuse(myReply, AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
      }

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
      msg.setContent("( action df " + myActionName + " ) " + reason);
      send(msg);
    }
    
    // Send a 'failure' message back to the requester
    protected void sendFailure(ACLMessage msg, String reason) {
    msg.setType("failure");
    msg.setContent("( action df " + myActionName + " ) " + reason);
    send(msg);
    }
    
    // Send an 'agree' message back to the requester
    protected void sendAgree(ACLMessage msg) {
      msg.setType("agree");
      msg.setContent("( action df " + myActionName + " )");
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
      super(AgentManagementOntology.DFAction.REGISTER);
    }

    public RegBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.DFAction.REGISTER, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new RegBehaviour(request, reply);
    }

    protected void processArgs(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
      DFRegister(dfd);
      sendAgree(myReply);
      sendInform(myReply);
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends DFBehaviour {

    public DeregBehaviour() {
      super(AgentManagementOntology.DFAction.DEREGISTER);
    }

    public DeregBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.DFAction.DEREGISTER, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new DeregBehaviour(request, reply);
    }

    protected void processArgs(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
      DFDeregister(dfd);
      sendAgree(myReply);
      sendInform(myReply);
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends DFBehaviour {

    public ModBehaviour() {
      super(AgentManagementOntology.DFAction.MODIFY);
    }

    public ModBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.DFAction.MODIFY, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new ModBehaviour(request, reply);
    }

    protected void processArgs(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
      DFModify(dfd);
      sendAgree(myReply);
      sendInform(myReply);
    }

  } // End of ModBehaviour class

  private class SrchBehaviour extends DFBehaviour {

    public SrchBehaviour() {
      super(AgentManagementOntology.DFAction.SEARCH);
    }

    public SrchBehaviour(ACLMessage request, ACLMessage reply) {
      super(AgentManagementOntology.DFAction.SEARCH, request, reply);
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new SrchBehaviour(request, reply);
    }

    protected void processArgs(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
      DFSearch(dfd);
      sendAgree(myReply);
      sendInform(myReply);
    }

  } // End of SrchBehaviour class

  private AgentManagementOntology myOntology;
  private FipaRequestServerBehaviour dispatcher;
  private Hashtable descriptors = new Hashtable();

  public df() {

    myOntology = AgentManagementOntology.instance();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));
    dispatcher = new FipaRequestServerBehaviour(this, mt);

    // Associate each DF action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerPrototype(AgentManagementOntology.DFAction.REGISTER, new RegBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.DFAction.DEREGISTER, new DeregBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.DFAction.MODIFY, new ModBehaviour());
    dispatcher.registerPrototype(AgentManagementOntology.DFAction.SEARCH, new SrchBehaviour());

  }

  protected void setup() {

    // add a message dispatcher behaviour
    addBehaviour(dispatcher);
  }

  protected void DFRegister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    if(descriptors.containsKey(dfd.getName()))
      throw myOntology.getException(AgentManagementOntology.Exception.AGENTALREADYREG);

    descriptors.put(dfd.getName(), dfd);
    System.out.println("");

  }

  protected void DFDeregister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    Object o = descriptors.remove(dfd.getName());
    AgentManagementOntology.DFAgentDescriptor toRemove = (AgentManagementOntology.DFAgentDescriptor)o;
    if(toRemove == null)
      throw myOntology.getException(AgentManagementOntology.Exception.UNABLETODEREG);
  }

  protected void DFModify(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    Object o = descriptors.get(dfd.getName());
    if(o == null)
      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);

    AgentManagementOntology.DFAgentDescriptor toChange = (AgentManagementOntology.DFAgentDescriptor)o;

    Enumeration e = dfd.getAddresses();
    if(e.hasMoreElements())
      toChange.removeAddresses();
    while(e.hasMoreElements())
      toChange.addAddress((String)e.nextElement());

    e = dfd.getAgentServices();
    if(e.hasMoreElements())
      toChange.removeAgentServices();
    while(e.hasMoreElements())
      toChange.addService((AgentManagementOntology.ServiceDescriptor)e.nextElement());

    String s = dfd.getType();
    if(s != null)
      toChange.setType(s);

    e = dfd.getInteractionProtocols();
    if(e.hasMoreElements())
      toChange.removeInteractionProtocols();
    while(e.hasMoreElements())
      toChange.addInteractionProtocol((String)e.nextElement());

    s = dfd.getOntology();
    if(s != null)
      toChange.setOntology(s);

    s = dfd.getOwnership();
    if(s != null)
      toChange.setOwnership(s);

    s = dfd.getDFState();
    if(s != null)
      toChange.setDFState(s);
    
  }

  private void DFSearch(AgentManagementOntology.DFAgentDescriptor dfd) {

    // FIXME: To be implemented
  }

}
