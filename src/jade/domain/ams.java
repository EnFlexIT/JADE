/*
 * $Id$
 */

package jade.domain;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import jade.core.*;
import jade.lang.acl.*;


public class ams extends Agent { // FIXME: Must become a Singleton


  // These String constants are the keywords in
  // 'fipa-man-ams-agent-description' objects
  private static final String NAME_ATTR = ":agent-name";
  private static final String ADDR_ATTR = ":address";
  private static final String SIGN_ATTR = ":signature";
  private static final String DELE_ATTR = ":delegate-agent";
  private static final String FORW_ATTR = ":forward-address";
  private static final String APST_ATTR = ":ap-state";

  private static final String DUPLICATE_MSG = "Duplicate attribute name";
  private static final String UNRECOGNIZED_MSG = "Unrecognized attribute name";
  private static final String UNKNOWN_MSG = "???";


  private abstract class AMSBehaviour extends SimpleBehaviour {

    private ACLMessage myReply;
    private StringTokenizer myTokenizer;

    protected AMSBehaviour() {
      super(ams.this);
      myReply = null;
      myTokenizer = null;
    }

    protected AMSBehaviour(ACLMessage msg, StringTokenizer st) {
      super(ams.this);
      myReply = msg;
      myTokenizer = st;
    }

    // This method creates a copy of an AMS behaviour, passing the
    // reply message and a StringTokenizer that is reading the content
    // of a received message.
    public abstract Behaviour instance(ACLMessage msg, StringTokenizer st);


    // This method parses the message content and builds an Hashtable
    // of attribute/value pairs.
    protected Hashtable crackMessage() throws NoSuchElementException {

      Hashtable result = new Hashtable();

      while(myTokenizer.hasMoreTokens()) {

	String name = myTokenizer.nextToken();

	// Make sure that keyword is defined in
	// 'fipa-man-ams-agent-description'
	if(!attributeNames.containsKey(name))
	  throw new NoSuchElementException(UNRECOGNIZED_MSG);

	String value = myTokenizer.nextToken();

	Object old = result.put(name, value);
	// Check if attribute exists already. If so, raise an
	// exception
	if(old != null)
	  throw new NoSuchElementException(DUPLICATE_MSG);
      }

      return result;
    }

    protected void handleNSEE(NoSuchElementException nsee, String action) {
	String why = nsee.getMessage();
	if(why.equals(UNRECOGNIZED_MSG))
	   sendRefuse(myReply, action, "unrecognised-attribute");
	else if(why.equals(DUPLICATE_MSG))
	  sendRefuse(myReply, action, "unrecognised-attribute-value");
	else
	  sendRefuse(myReply, action, UNKNOWN_MSG);
    }

  } // End of AMSBehaviour class


  // These four concrete classes serve both as a Prototype and as an
  // Instance: when seen as amsBehaviourPrototype they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Behaviour they process their request and terminate.

  private class AuthBehaviour extends AMSBehaviour {

    public AuthBehaviour() {
    }

    public AuthBehaviour(ACLMessage msg, StringTokenizer st) {
      super(msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new AuthBehaviour(msg, st);
    }

    public void action() {
      Hashtable attributes = null;
      try {
	attributes = crackMessage();
      }
      catch(NoSuchElementException nsee) {
	handleNSEE(nsee, "authenticate");
      }

      sendRefuse(myReply, "authenticate", "unwilling-to-perform"); // FIXME: Not Implemented

      myPlatform.AMSDumpData();
      // Mandatory attributes:
      // :agent-name

      // Optional attributes:
      // :address
      // :signature
      // :ap-state
      // :delegate-agent-name
      // :forward-address

    }

  } // End of AuthBehaviour class

  private class RegBehaviour extends AMSBehaviour {

    public RegBehaviour() {
    }

    public RegBehaviour(ACLMessage msg, StringTokenizer st) {
      super(msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new RegBehaviour(msg, st);
    }

    public void action() {
      Hashtable attributes = null;
      try {
	attributes = crackMessage();
      }
      catch(NoSuchElementException nsee) {
	handleNSEE(nsee, "register-agent");
      }

      // Mandatory attributes:
      // :agent-name
      // :address
      // :ap-state
      String agentName = (String)attributes.get(NAME_ATTR);
      String address = (String)attributes.get(ADDR_ATTR);
      String APState = (String)attributes.get(APST_ATTR);
      if((agentName == null)||(address == null)||(APState == null)) {
	sendRefuse(myReply, "register-agent", "unrecognised-attribute");
	return;
      }

      // Optional attributes:
      // :signature
      // :delegate-agent-name
      // :forward-address
      String signature = (String)attributes.get(SIGN_ATTR);
      String delegateAgent = (String)attributes.get(DELE_ATTR);
      String forwardAddress = (String)attributes.get(FORW_ATTR);

      // Write new agent data in Global Agent Descriptor Table
      try {
	myPlatform.AMSNewData(agentName, address, signature, delegateAgent,
			      forwardAddress, APState);
      }
      catch(AgentAlreadyRegisteredException aare) {
	sendAgree(myReply, "register-agent");
	sendFailure(myReply, "register-agent", aare.getMessage());
	return;
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, "register-agent", fe.getMessage());
	return;
      }

      sendAgree(myReply, "register-agent");
      sendInform(myReply, "register-agent");

    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends AMSBehaviour {

    public DeregBehaviour() {
    }

    public DeregBehaviour(ACLMessage msg, StringTokenizer st) {
      super(msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new DeregBehaviour(msg, st);
    }

    public void action() {

      Hashtable attributes = null;
      try {
	attributes = crackMessage();
      }
      catch(NoSuchElementException nsee) {
	handleNSEE(nsee, "deregister-agent");
      }

      // Mandatory attributes:
      // :agent-name
      String agentName = (String)attributes.get(NAME_ATTR);

      if(agentName == null) {
	sendRefuse(myReply, "deregister-agent", "unrecognised-attribute");
	return;
      }

      // Optional attributes:
      // :address
      // :signature
      // :delegate-agent-name
      // :forward-address
      // :ap-state
      String address = (String)attributes.get(ADDR_ATTR);
      String signature = (String)attributes.get(SIGN_ATTR);
      String delegateAgent = (String)attributes.get(DELE_ATTR);
      String forwardAddress = (String)attributes.get(FORW_ATTR);
      String APState = (String)attributes.get(APST_ATTR);

      try {
	// Remove the agent data from Global Descriptor Table
	myPlatform.AMSRemoveData(agentName, address, signature, delegateAgent,
				 forwardAddress, APState);
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, "deregister-agent", fe.getMessage());
	return;
      }

      sendAgree(myReply, "deregister-agent");
      sendInform(myReply, "deregister-agent");

    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends AMSBehaviour {

    public ModBehaviour() {
    }

    public ModBehaviour(ACLMessage msg, StringTokenizer st) {
      super(msg, st);
    }

    public Behaviour instance(ACLMessage msg, StringTokenizer st) {
      return new ModBehaviour(msg, st);
    }

    public void action() {

      Hashtable attributes = null;
      try {
	attributes = crackMessage();
      }
      catch(NoSuchElementException nsee) {
	handleNSEE(nsee, "modify-agent");
      }
      // Mandatory attributes:
      // :agent-name
      String agentName = (String)attributes.get(NAME_ATTR);

      if(agentName == null) {
	sendRefuse(myReply, "modify-agent", "unrecognised-attribute");
	return;
      }

      // Optional attributes:
      // :address
      // :signature
      // :delegate-agent-name
      // :forward-address
      // :ap-state
      String address = (String)attributes.get(ADDR_ATTR);
      String signature = (String)attributes.get(SIGN_ATTR);
      String delegateAgent = (String)attributes.get(DELE_ATTR);
      String forwardAddress = (String)attributes.get(FORW_ATTR);
      String APState = (String)attributes.get(APST_ATTR);

      try {
	// Modify agent data from Global Descriptor Table
	myPlatform.AMSChangeData(agentName, address, signature, delegateAgent,
				 forwardAddress, APState);
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, "modify-agent", fe.getMessage());
	return;
      }

      sendAgree(myReply, "modify-agent");
      sendInform(myReply, "modify-agent");

    }

  } // End of ModBehaviour class


  // This behaviour receives incoming request messages and starts
  // specific sub-behaviours according to the kind of action
  // requested.
  private class DispatcherBehaviour implements Behaviour {

    private MessageTemplate requestTemplate;

    public DispatcherBehaviour() {
      MessageTemplate mt1 = 
	MessageTemplate.and(MessageTemplate.MatchProtocol("fipa-request"),
			    MessageTemplate.MatchType("request"));
      MessageTemplate mt2 = 
	MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			    MessageTemplate.MatchOntology("fipa-agent-management"));
      requestTemplate = MessageTemplate.and(mt1, mt2);
    }

    public void execute() {
      ACLMessage msg = receive(requestTemplate);
      if(msg != null) {

	ACLMessage reply = new ACLMessage();


	// Write content-independent fields of reply message

	reply.setDest(msg.getSource());
	reply.setSource(msg.getDest());
	reply.setProtocol("fipa-request");
	reply.setOntology("fipa-agent-management");
	reply.setLanguage("SL0");

	String s = msg.getReplyWith();
	if(s != null)
	  reply.setReplyTo(s);
	s =msg.getConversationId();
	if(s != null)
	  reply.setConversationId(s);


	// Start reading message content and spawn a suitable
	// Behaviour according to action kind

	StringTokenizer st = new StringTokenizer(msg.getContent()," \t\n\r()",false);

	String token = st.nextToken();
	if(token.equalsIgnoreCase("action")) {
	  token = st.nextToken(); // Now 'token' is the name of the AMS agent
	  token = st.nextToken(); // Now 'token' is the action name

	  AMSBehaviour action = (AMSBehaviour)actions.get(token);
	  if(action == null) {
	    sendNotUnderstood(reply);
	    return;
	  }
	  else
	    addBehaviour(action.instance(reply, st));
	}
	else
	  sendNotUnderstood(reply);
      }

    }

    public boolean done() {
      return false;
    }

  } // End of DispatcherBehaviour class


  // The AgentPlatform where information about agents is stored 
  private AgentPlatformImpl myPlatform;

  // Maintains an association between action names and behaviours
  private Hashtable actions;

  // Holds all the valid names of attributes in
  // 'fipa-man-ams-agent-description' objects
  private Hashtable attributeNames;

  public ams(AgentPlatformImpl ap, String name) {
    myPlatform = ap;
    myName = name;

    actions = new Hashtable(4, 1.0f);
    actions.put("authenticate", new AuthBehaviour());
    actions.put("register-agent", new RegBehaviour());
    actions.put("deregister-agent", new DeregBehaviour());
    actions.put("modify-agent", new ModBehaviour());


    attributeNames = new Hashtable(6, 1.0f);
    // When key == value an Hashtable is used like a Set
    attributeNames.put(NAME_ATTR, NAME_ATTR);
    attributeNames.put(ADDR_ATTR, ADDR_ATTR);
    attributeNames.put(SIGN_ATTR, SIGN_ATTR);
    attributeNames.put(DELE_ATTR, DELE_ATTR);
    attributeNames.put(FORW_ATTR, FORW_ATTR);
    attributeNames.put(APST_ATTR, APST_ATTR);
  }

  protected void setup() {

    // Add a dispatcher behaviour
    addBehaviour(new DispatcherBehaviour());

  }


  // The following methods handle the various possibilities arising in
  // AMS <-> Agent interaction. They all receive an ACL message as an
  // argument, most of whose fields have already been set. Only the
  // message type and message content have to be filled in.

  // Send a 'not-understood' message back to the requester
  private void sendNotUnderstood(ACLMessage msg) {
    msg.setType("not-understood");
    msg.setContent("");
    send(msg);
  }

  // Send a 'refuse' message back to the requester
  private void sendRefuse(ACLMessage msg, String action, String reason) {
    msg.setType("refuse");
    msg.setContent("( action " + action + " ) " + reason);
    send(msg);
  }

  // Send a 'failure' message back to the requester
  private void sendFailure(ACLMessage msg, String action, String reason) {
    msg.setType("failure");
    msg.setContent("( action " + action + " ) " + reason);
    send(msg);
  }

  // Send an 'agree' message back to the requester
  private void sendAgree(ACLMessage msg, String action) {
    msg.setType("agree");
    msg.setContent("( action " + action + " )");
    send(msg);
  }

  // Send an 'inform' message back to the requester
  private void sendInform(ACLMessage msg, String action) {
    msg.setType("inform");
    msg.setContent("( done ( " + action + " ) )");
    send(msg);
  }


  // The AMS must have a special version for this method, or a deadlock will occur...
  public void registerWithAMS(String signature, String delegateAgent,
		       String forwardAddress, int APState) {

    // Skip all fipa-request protocol and go straight to the target
    
    try {
      myPlatform.AMSNewData(myName, myAddress, signature, delegateAgent,
			    forwardAddress, "initiated");
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
