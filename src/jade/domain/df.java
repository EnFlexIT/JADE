/*
  $Log$
  Revision 1.12  1998/11/23 00:14:17  rimassa
  Added a match() method to aid in 'search' DF action. Now a complete
  match is performed on every attribute of a 'df-agent-descriptor'
  ontology object.

  Revision 1.11  1998/11/18 23:00:52  Giovanni
  Written 'search' action implementation; now a simple linear scan of DF
  agent descriptor table is used. Still missing is a non trivial match
  function, support for action constraints and result packaging as an
  'inform' message.

  Revision 1.10  1998/10/18 17:37:34  rimassa
  Minor changes towards 'search' action implementation.

  Revision 1.9  1998/10/04 18:01:37  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

import java.lang.reflect.Method;

import java.io.StringReader;

// FIXME: Just for debug
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

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
	pe.printStackTrace();
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
      }
      catch(TokenMgrError tme) {
	tme.printStackTrace();
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
    protected abstract void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException;

    public void action() {

      try {
	// Convert message from untyped keyword/value list to ordinary
	// typed variables, throwing a FIPAException in case of errors
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

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
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

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
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

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
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

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
      AgentManagementOntology.DFSearchAction dfsa = (AgentManagementOntology.DFSearchAction)dfa;
      Enumeration constraints = dfsa.getConstraints();
      DFSearch(dfd, constraints);
      sendAgree(myReply);
      sendInform(myReply); // FIXME: To change, since it must send a result instead of Done(action)
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

  private void DFSearch(AgentManagementOntology.DFAgentDescriptor dfd, Enumeration constraints) {

    Vector matchesFound = new Vector();
    Enumeration e = descriptors.elements();

    while(e.hasMoreElements()) {
      Object obj = e.nextElement();
      AgentManagementOntology.DFAgentDescriptor current = (AgentManagementOntology.DFAgentDescriptor)obj;
      if(match(dfd, current)) {
	matchesFound.addElement(current);
      }

    }

    e = matchesFound.elements();
    while(e.hasMoreElements()) {
      Object obj = e.nextElement();
      AgentManagementOntology.DFAgentDescriptor current = (AgentManagementOntology.DFAgentDescriptor)obj;

      current.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

    }
    // FIXME: To be completed
  }

  
  // These two String arrays hold the field names of
  // AgentManagementOntology.DFAgentDescriptor class.

  private static final String[] stringFields = { "Name",
						 "Type",
						 "Ontology",
						 "Ownership",
						 "DFState"
  };

  private static final String[] vectorFields = { "Addresses",
						 "AgentServices",
						 "InteractionProtocols"
  };

  private static final Class[] noClass = new Class[0];
  private static final Object[] noParams = new Object[0];

  private boolean match(AgentManagementOntology.DFAgentDescriptor template,
			AgentManagementOntology.DFAgentDescriptor dfd) {

    /* To have a match, the following clauses must be true:

       + FOR EACH String-valued attribute A of the template
         - ( template.getA() == null ) OR ( template.getA() == dfd.getA() )
       + FOR EACH Vector-valued attribute V of the template
         + FOR EACH element E of template.getV()
	   - dfd.getV().contains(E)

      Now we will use Reflection API to code the algorithm above.
      This method returns false as soon as a mismatch is detected.

    */

    try {

      Class dfdClass = dfd.getClass();
      String methodName = null;
      Method m = null;
      String templateValue = null;
      String dfdValue = null;

      for(int i = 0; i<stringFields.length; i++) {
	methodName = "get" + stringFields[i];
	m = dfdClass.getMethod(methodName, noClass);

	// This means: templateValue = template.get<stringFields[i]>()
	templateValue = (String)m.invoke(template, noParams);
	if(templateValue != null) {
	  // This means: dfdValue = dfd.get<stringFields[i]>()
	  dfdValue = (String)m.invoke(dfd, noParams);
	  if(dfdValue == null)
	    return false;
	  if(!dfdValue.equalsIgnoreCase(templateValue))
	    return false;
	}

      }

      // If we reach here, then no mismatch occurred in comparing
      // String-valued attributes.

      Enumeration templateValues = null;
      Enumeration dfdValues = null;

      for(int i = 0; i<vectorFields.length; i++) {
	methodName = "get" + vectorFields[i];
	m = dfdClass.getMethod(methodName, noClass);

	// This means: templateValues = template.get<vectorFields[i]>()
	templateValues = (Enumeration)m.invoke(template, noParams);
	while(templateValues.hasMoreElements()) {
	  // This means: dfdValues = dfd.get<vectorFields[i]>()
	  dfdValues = (Enumeration)m.invoke(dfd, noParams);
	  templateValue = (String)templateValues.nextElement();
	  if(!contains(dfdValues, templateValue))
	    return false;
	}

      }

    }
    catch(Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  private boolean contains(Enumeration list, String value) {
    while(list.hasMoreElements()) {
      String current = (String)list.nextElement();
      if(current.equalsIgnoreCase(value))
	return true;
    }
    return false;
  }

}
