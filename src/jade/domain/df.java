/*
  $Log$
  Revision 1.18  1999/02/16 08:12:05  rimassa
  Removed some debugging printouts and fixed a bug in recursive search:
  a couple of addBehaviour() calls changed to addSubBehaviour() calls.

  Revision 1.17  1999/02/14 23:24:20  rimassa
  Changed addBehaviour() calls to addSubBehaviour() where appropriate.
  Added an automatic management of DF federeation: now a DF agent
  informs deregisters itself from parent DFs when it terminates
  Removed some debug printouts.

  Revision 1.16  1999/02/04 13:19:14  rimassa
  Fixed a bug in the content of the search result.
  The FIPA-request protocol now complies with the FIPA specs. Also the
  requested action is returned in the content of the agree and inform
  done messages.

  Revision 1.15  1999/02/03 11:50:18  rimassa
  Some 'private' instance variables made 'protected', to allow code
  compilation under jdk 1.2.
  Changed some FIPA exceptions thrown by DF agent.
  Added some missing parentheses and modified message content of several
  DF response messages.

  Revision 1.14  1998/12/08 00:21:09  rimassa
  Removed handmade parsing of message content. Now updated fromText()
  method from DFAction and DFSearchAction classes is used.
  Moved DFSearch() method from df class to SrchBehaviour inner
  class. Now DFSearch() performs a complete pattern matching among
  DFAgentDescriptor objects, ensures search constraints feasibility and
  can even spawn other behaviours for recursive searches.
  Added RecursiveSearchBehaviour inner class to support concurrently
  active recursive searches (when ':df-depth' search constraint is given
  and it is greater than 1).

  Revision 1.13  1998/11/30 00:24:34  rimassa
  Finished basic support for 'search' action: still missing search
  constraint management.

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
import java.io.StringWriter;

// FIXME: Just for debug
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
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
    protected ACLMessage myRequest;
    protected ACLMessage myReply;

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
	throw myOntology.getException(AgentManagementOntology.Exception.MISSINGATTRIBUTE+ " "+attributeName);
    }

    private void checkAttributeList(String attributeName, Enumeration attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForDF(myAction.getName(), attributeName) && (!attributeValue.hasMoreElements()))
	throw myOntology.getException(AgentManagementOntology.Exception.MISSINGATTRIBUTE+ " "+attributeName);
    }

    // This method parses the message content and puts
    // 'FIPA-DF-agent-description' attribute values in instance
    // variables. If some error is found a FIPA exception is thrown
    private void crackMessage() throws FIPAException, NoSuchElementException {

      String content = myRequest.getContent();

      // Obtain a DF action from message content
      try {
	myAction = AgentManagementOntology.DFAction.fromText(new StringReader(content));
      }
      catch(ParseException pe) {
	// pe.printStackTrace();
	// System.out.println("DF ParseException with: " + content);
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE+" :content");
      }
      catch(TokenMgrError tme) {
	// tme.printStackTrace();
	// System.out.println("DF TokenMgrError with: " + content);
	throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE+" :content");
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
      msg.setContent("(" + myRequest.getContent() + " (unknown reason))");
      send(msg);
    }
    
    // Send a 'refuse' message back to the requester
    protected void sendRefuse(ACLMessage msg, String reason) {
      msg.setType("refuse");
      // FIXME. Fipa97 version 2 is different
      msg.setContent("(" + myRequest.getContent() + " (:fipa-man-exception (" + reason + ")))");
      send(msg);
    }

    // Send a 'failure' message back to the requester
    protected void sendFailure(ACLMessage msg, String reason) {
    msg.setType("failure");
    msg.setContent("(" + myRequest.getContent() + " (:fipa-man-exception (" + reason + ")))");
    send(msg);
    }
    
    // Send an 'agree' message back to the requester
    protected void sendAgree(ACLMessage msg) {
      msg.setType("agree");
      msg.setContent("(" + myRequest.getContent() + " (true))"); 
      send(msg);
    }

    // Send an 'inform' message back to the requester
    protected void sendInform(ACLMessage msg) {
      msg.setType("inform");
      msg.setContent("( done " + myRequest.getContent() + ")");
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
      DFSearch(dfd, constraints, myReply);

    }

    private void DFSearch(AgentManagementOntology.DFAgentDescriptor dfd,
			  Enumeration constraints, ACLMessage myReply) throws FIPAException {

      AgentManagementOntology.DFSearchResult matchesFound = new AgentManagementOntology.DFSearchResult();

      // Final search depth, set such that search constraints are satisfied
      int dfDepth = -1;

      // Minimum search depth, according to constraints
      int dfDepthMin = -1;

      // Maximum search depth, according to constraints
      int dfDepthMax = -1;

      // Exact search depth, according to constrtaints
      int dfDepthExactly = -1;

      // Final response length, set such that search constraints are satisfied
      int respReq = -1;

      // Minimum response length, according to constraints
      int respReqMin = -1;

      // Maximum response length, according to constraints
      int respReqMax = -1;

      // Exact response length, according to constrtaints
      int respReqExactly = -1;

      /***********************************************************
       *
       *    Algorithm for search constraint processing:
       *
       *   for each constraint kind (':df-depth' or ':resp-req'),
       *   the following combination is the only correct one:
       *
       *    - Min N Max M Exactly P, N > 0 and M > 0 and P > 0 and N <= P <= M
       *
       *   Multiple clauses can be present, as long as they all are equivalent
       *   to some form of the combination above. For example:
       *
       *    - No constraint at all (using a default value)
       *    - Min 5 Min 10 (result is 10)
       *    - Max 5 Max 10 (result is 5)
       *    - Max 3 Exactly 2 (result is 2)
       *    - Min 4 Max 6 Exactly 5 (result is 5)
       *    - Min 2 Max 8 (result is 5, somewhat arbitrarily)
       *
       *  When some other constraint combination is detected an 
       *  'inconsistency' exception is reised.
       *  ========================================================
       *
       *  The following code scans the constraint list, keeping
       *  running values for 'Min', 'Exactly' and 'Max' constraint
       *  both for ':df-depth' and ':resp-req', raising an exception
       *  when an inconsistency occurs. Finally, if everything is OK,
       *  a couple of variables is set to the chosen value.
       *  In the code, '-1' is used as an out-of-band value.
       *
       ***********************************************************/



      while(constraints.hasMoreElements()) {
	AgentManagementOntology.Constraint c = (AgentManagementOntology.Constraint)constraints.nextElement();
	String name = c.getName();
	String fn = c.getFn();
	int arg = c.getArg();

	if(arg <= 0)
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);

	if(name.equalsIgnoreCase(AgentManagementOntology.Constraint.DFDEPTH)) {
	  if(fn.equalsIgnoreCase(AgentManagementOntology.Constraint.MIN)) {
	    // If 'Exactly' clause is already present and with a smaller value, it is an error
	    if((dfDepthExactly != -1) && (dfDepthExactly < arg))
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    dfDepthMin = Math.max(arg, dfDepthMin); // The larger 'Min' arg is the strongest clause
	    if((dfDepthMax != -1) && (dfDepthMax < dfDepthMin)) // Must be 'Min' <= 'Max' when both are set
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	  }
	  else if(fn.equalsIgnoreCase(AgentManagementOntology.Constraint.EXACTLY)) {
	    // If 'Min' or 'Max' clauses are present, it must be Min <= Exactly <= Max, or it is an error
	    if(((dfDepthMin != -1) && (dfDepthMin > arg)) || ((dfDepthMax != -1) && (dfDepthMax < arg)))
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    if((dfDepthExactly != -1) && (dfDepthExactly != arg)) // There must be a sole value for 'Exactly'
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    dfDepthExactly = arg;
	  }
	  else { // Max
	    // If 'Exactly' clause is already present and with a greater value, it is an error
	    if((dfDepthExactly != -1) && (dfDepthExactly > arg))
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    if(dfDepthMax != -1)
	      dfDepthMax = Math.min(arg, dfDepthMax); // The smaller 'Max' arg is the strongest clause
	    else
	      dfDepthMax = arg;
	    if((dfDepthMin != -1) && (dfDepthMin > dfDepthMax)) // Must be 'Min' <= 'Max' when both are set
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	  }
	}
	else { // :resp-req
	  if(fn.equalsIgnoreCase(AgentManagementOntology.Constraint.MIN)) {
	    // If 'Exactly' clause is already present and with a smaller value, it is an error
	    if((respReqExactly != -1) && (respReqExactly < arg))
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    respReqMin = Math.max(arg, respReqMin); // The larger 'Min' arg is the strongest clause
	    if((respReqMax != -1) && (respReqMax < respReqMin)) // Must be 'Min' <= 'Max' when both are set
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	  }
	  else if(fn.equalsIgnoreCase(AgentManagementOntology.Constraint.EXACTLY)) {
	    // If 'Min' or 'Max' clauses are present, it must be Min <= Exactly <= Max, or it is an error
	    if(((respReqMin != -1) && (respReqMin > arg)) || ((respReqMax != -1) && (respReqMax < arg)))
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    if((respReqExactly != -1) && (respReqExactly != arg)) // There must be a sole value for 'Exactly'
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    respReqExactly = arg;
	  }
	  else { // Max
	    // If 'Exactly' clause is already present and with a greater value, it is an error
	    if((respReqExactly != -1) && (respReqExactly > arg))
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    if(respReqMax != -1)
	      respReqMax = Math.min(arg, respReqMax); // The smaller 'Max' arg is the strongest clause
	    else
	      respReqMax = arg;
	    if((respReqMin != -1) && (respReqMin > respReqMax)) // Must be 'Min' <= 'Max' when both are set
	      throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	  }
	}
      }

      // Now, calculate dfDepth from dfDepthMin, dfDepthExactly and dfDepthMax
      if(dfDepthExactly != -1)
	dfDepth = dfDepthExactly;
      else {
	if(dfDepthMin != -1) {
	  if(dfDepthMax != -1)
	    dfDepth = (dfDepthMin + dfDepthMax) / 2;
	  else
	    dfDepth = dfDepthMin;
	}
	else {
	  if(dfDepthMax != -1)
	    dfDepth = dfDepthMax;
	  else // No constraints
	    dfDepth = 1;
	}
      }

      // Now, calculate respReq from respReqMin, respReqExactly and respReqMax
      if(respReqExactly != -1)
	respReq = respReqExactly;
      else {
	if(respReqMin != -1) {
	  if(respReqMax != -1)
	    respReq = (respReqMin + respReqMax) / 2;
	  else
	    respReq = respReqMin;
	}
	else {
	  if(respReqMax != -1)
	    respReq = respReqMax;
	  else // No constraints
	    respReq = 1;
	}
      }

      Enumeration e = descriptors.elements();

      while(e.hasMoreElements()) {
	Object obj = e.nextElement();
	AgentManagementOntology.DFAgentDescriptor current = (AgentManagementOntology.DFAgentDescriptor)obj;
	if(match(dfd, current)) {
	  matchesFound.put(current.getName(), current);
	}

      }

      sendAgree(myReply);

      if(dfDepth == 1) {

	StringWriter text = new StringWriter();
	matchesFound.toText(text);

	String content = "(result " + myRequest.getContent() + text.toString() + ")";
	// System.err.println("myRequest.getContent="+myRequest.getContent());
	// System.err.println("text.toString()="+text.toString());
	myReply.setContent(content);
	myReply.setType("inform");
	send(myReply);

      }
      else {
	addBehaviour(new RecursiveSearchBehaviour(dfd, myReply, matchesFound, dfDepth - 1, myRequest));
      }

    }


  } // End of SrchBehaviour class

  private class RecursiveSearchBehaviour extends SequentialBehaviour {

    ACLMessage reply;
    AgentManagementOntology.DFSearchResult result;
    ACLMessage originalRequestToSearchMsg;
    RecursiveSearchBehaviour(AgentManagementOntology.DFAgentDescriptor dfd, ACLMessage msg,
			     AgentManagementOntology.DFSearchResult res, int dfDepth, ACLMessage requestToSearchMsg) {

      reply = msg;
      result = res;
      originalRequestToSearchMsg = requestToSearchMsg;

      ComplexBehaviour searchThemAll = NonDeterministicBehaviour.createWhenAll(df.this);

      Vector constraints = new Vector();
      AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      c.setFn(AgentManagementOntology.Constraint.EXACTLY);
      c.setArg(dfDepth);
      constraints.addElement(c);

      String convID = getName() + "-recursive-search-" + dfDepth;
      ACLMessage request = new ACLMessage("request");
      request.setConversationId(convID);
      request.setSource(getName());

      Enumeration e = subDFs.keys();
      while(e.hasMoreElements()) {
	String subDF = (String)e.nextElement();
	ACLMessage copy = (ACLMessage)request.clone();
	copy.setDest(subDF);
	try {
	  searchThemAll.addSubBehaviour(new SearchDFBehaviour(df.this, copy, dfd, constraints, result));
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }

      addSubBehaviour(searchThemAll);

      addSubBehaviour(new OneShotBehaviour(df.this) {

	public void action() {
	  StringWriter text = new StringWriter();
	  try {
	    result.toText(text);
	    String content = "(result " + 
	      originalRequestToSearchMsg.getContent() + text.toString() + ")";
	    reply.setContent(content);
	    reply.setType("inform");
	    send(reply);
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
	  }

	}
      });

    }

  } // End of RecursiveSearchBehaviour


  private AgentManagementOntology myOntology;
  private FipaRequestServerBehaviour dispatcher;
  private Hashtable descriptors = new Hashtable();
  private Hashtable subDFs = new Hashtable();

  private AgentGroup parents = new AgentGroup();

  private DFGUI gui;

  protected Enumeration getDFAgentDescriptors() {
    return descriptors.elements();
  }

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
    // Show GUI
    gui = new DFGUI(this);
    gui.setVisible(true);
    // add a message dispatcher behaviour
    addBehaviour(dispatcher);
  }

  protected void takeDown() {
    AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
    dfd.setName(getName());
    Enumeration e = parents.getMembers();
    while(e.hasMoreElements()) {
      String parentName = (String)e.nextElement();
      try {
        deregisterWithDF(parentName, dfd);
      }
      catch(FIPAException fe) {
        fe.printStackTrace();
      }
    }
  }

  public void registerWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    super.registerWithDF(dfName, dfd);
    parents.addMember(dfName);
  }

  public void deregisterWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    super.deregisterWithDF(dfName, dfd);
    parents.removeMember(dfName);
  }

  protected void DFRegister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    if(descriptors.containsKey(dfd.getName()))
      throw myOntology.getException(AgentManagementOntology.Exception.AGENTALREADYREG);

    descriptors.put(dfd.getName(), dfd);
    
    // Update sub-DF table if needed
    Enumeration e = dfd.getAgentServices();
    while(e.hasMoreElements()) {
      AgentManagementOntology.ServiceDescriptor current = (AgentManagementOntology.ServiceDescriptor)e.nextElement();
      String type = current.getType();
      if(type == null)
	return;
      if(type.equalsIgnoreCase("fipa-df") || type.equalsIgnoreCase("df")) {
	subDFs.put(dfd.getName(), dfd);
      }
    }
  }

  protected void DFDeregister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    Object o = descriptors.remove(dfd.getName());
    AgentManagementOntology.DFAgentDescriptor toRemove = (AgentManagementOntology.DFAgentDescriptor)o;
    if(toRemove == null)
      throw myOntology.getException(AgentManagementOntology.Exception.UNABLETODEREG);

    // Update sub-DF table if needed
    subDFs.remove(dfd.getName());
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
      toChange.addAgentService((AgentManagementOntology.ServiceDescriptor)e.nextElement());

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

  
  // These two String arrays hold the field names of
  // AgentManagementOntology.DFAgentDescriptor class.

  private static final String[] stringFields = { "Name",
						 "Type",
						 "Ontology",
						 "Ownership",
						 "DFState"
  };

  private static final String[] vectorOfStringFields = { "Addresses",
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
       + Service Descriptors list of dfd matches Service Descriptor list of template

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

      for(int i = 0; i<vectorOfStringFields.length; i++) {
	methodName = "get" + vectorOfStringFields[i];
	m = dfdClass.getMethod(methodName, noClass);

	// This means: templateValues = template.get<vectorOfStringFields[i]>()
	templateValues = (Enumeration)m.invoke(template, noParams);
	while(templateValues.hasMoreElements()) {
	  // This means: dfdValues = dfd.get<vectorOfStringFields[i]>()
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

    /* Match Service Descriptors: the following algorithm is used:
      + FOR EACH ServiceDescriptor templSD contained in the template
        + EXISTS a ServiceDescriptor sd contained in dfd SUCH THAT
          + FOR EACH String-valued attribute A of the ServiceDescriptor
            - ( templSD.getA() == null ) OR ( templSD.getA() == sd.getA() )
    */
    Enumeration templateSDs = template.getAgentServices();
    while(templateSDs.hasMoreElements()) {
      java.lang.Object o = templateSDs.nextElement();
      AgentManagementOntology.ServiceDescriptor templSD = (AgentManagementOntology.ServiceDescriptor)o;
      if(noMatchingService(dfd, templSD))
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

  private static final String[] sdFields = {
    "Name",
    "Type",
    "Ontology",
    "FixedProps",
    "NegotiableProps",
    "CommunicationProps"
  };

  private boolean noMatchingService(
    AgentManagementOntology.DFAgentDescriptor dfd,
    AgentManagementOntology.ServiceDescriptor templSD) {

    Class sdClass = templSD.getClass();
    String methodName = null;
    Method m = null;
    String templSDValue = null;
    String sdValue = null;

    Enumeration services = dfd.getAgentServices();
    while(services.hasMoreElements()) {
      java.lang.Object o = services.nextElement();
      AgentManagementOntology.ServiceDescriptor sd = (AgentManagementOntology.ServiceDescriptor)o;
      try {
        boolean sdMatches = true;
        for(int i = 0; i<sdFields.length; i++) {
        	methodName = "get" + sdFields[i];
        	m = sdClass.getMethod(methodName, noClass);

        	// This means: templSDValue = templSD.get<sdFields[i]>()
  	      templSDValue = (String)m.invoke(templSD, noParams);
  	      if(templSDValue != null) {
        	  // This means: sdValue = sd.get<sdFields[i]>()
        	  sdValue = (String)m.invoke(sd, noParams);
        	  if(sdValue == null) {
        	    sdMatches = false;
              break; // Out of for loop
            }
        	  if(!sdValue.equalsIgnoreCase(templSDValue)) {
        	    sdMatches = false;
              break; // Out of for loop
            }
          }
        }

        if(sdMatches)
          return false;
      }

      catch(Exception e) {
        e.printStackTrace();
        return true;
      }

    }

    return true;
  }

}
