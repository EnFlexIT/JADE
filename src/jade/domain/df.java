/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

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

import java.lang.reflect.Method;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.NoSuchElementException;

import jade.core.*;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.FipaRequestResponderBehaviour;
import jade.gui.DFGUI;
import jade.gui.GUI2DFCommunicatorInterface;

/**
  Standard <em>Directory Facilitator</em> agent. This class implements
  <em><b>FIPA</b></em> <em>DF</em> agent. <b>JADE</b> applications
  cannot use this class directly, but interact with it through
  <em>ACL</em> message passing. More <em>DF</em> agents can be created
  by application programmers to divide a platform into many
  <em><b>Agent Domains</b></em>.

  Each DF has a GUI but, by default, it is not visible. The GUI of the
  agent platform includes a menu item that allows to show the GUI of the
  default DF. 

  In order to show the GUI, you should simply send the following message
  to each DF agent: <code>(request :content (action DFName (SHOWGUI))
  :ontology jade-extensions :protocol fipa-request)</code>
  
  Javadoc documentation for the file
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class df extends Agent implements GUI2DFCommunicatorInterface {

  private abstract class DFBehaviour
    extends FipaRequestResponderBehaviour.Action 
    implements FipaRequestResponderBehaviour.Factory {

    // This will be set by subclasses
    private AgentManagementOntology.DFAction myAction;

    protected DFBehaviour() {
      super(df.this);
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

      ACLMessage msg = getRequest();
      String content = msg.getContent();

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
	sendRefuse(fe.getMessage());
      }
      catch(NoSuchElementException nsee) {
	sendRefuse(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
      }

    }

      public boolean done() {
	return true;
      }

      public void reset() {
      }

  } // End of DFBehaviour class


  // These four concrete classes serve both as a Factory and as
  // Action: when seen as Factory they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Action they process their request and terminate.

  private class RegBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new RegBehaviour();
    }

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
      DFRegister(dfd);
      sendAgree();
      sendInform();
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new DeregBehaviour();
    }

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
      DFDeregister(dfd);
      sendAgree();
      sendInform();
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new ModBehaviour();
    }

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
      DFModify(dfd);
      sendAgree();
      sendInform();
    }

  } // End of ModBehaviour class

  private class SrchBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.Action create() {
      return new SrchBehaviour();
    }

    protected void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException {
      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
      AgentManagementOntology.DFSearchAction dfsa = (AgentManagementOntology.DFSearchAction)dfa;

      Enumeration constraints = dfsa.getConstraints();
      DFSearch(dfd, constraints, getReply());

    }

    private void DFSearch(AgentManagementOntology.DFAgentDescriptor dfd,
			  Enumeration constraints, ACLMessage reply) throws FIPAException {

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

      sendAgree();

      if(dfDepth == 1) {

	StringWriter text = new StringWriter();
	matchesFound.toText(text);

	ACLMessage req = getRequest();
	String content = "(result " + req.getContent() + text.toString() + ")";
	reply.setContent(content);
	reply.setType("inform");
	send(reply);

      }
      else {
	addBehaviour(new RecursiveSearchBehaviour(dfd, reply, matchesFound, dfDepth - 1, getRequest()));
      }

    }


  } // End of SrchBehaviour class

  private class ShowGUIBehaviour 
   extends FipaRequestResponderBehaviour.Action 
   implements FipaRequestResponderBehaviour.Factory {

    protected ShowGUIBehaviour() {
      super(df.this);
    }

    public FipaRequestResponderBehaviour.Action create() {
      return new ShowGUIBehaviour();
    }

   public void action () { 
      //      AgentManagementOntology.DFAgentDescriptor dfd = dfa.getArg();
      //      DFRegister(dfd);
      sendAgree();
      if (gui == null) {
	//GUI2DFCommunicator dfc = new GUI2DFCommunicator(myAgent);
	gui = new DFGUI(df.this);
	gui.setVisible(true);
	sendInform();
      } else
	sendFailure("Gui_is_being_shown_already");
    }
      
      public boolean done() {
	return true;
      }

      public void reset() {
      }
  } // End of ShowGUIBehaviour class


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
	copy.removeAllDests();
	copy.addDest(subDF);
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
  private FipaRequestResponderBehaviour dispatcher;
  private FipaRequestResponderBehaviour guiActivator;
  private Hashtable descriptors = new Hashtable();
  private Hashtable subDFs = new Hashtable();

  private AgentGroup parents = new AgentGroup();

  private DFGUI gui;
  private Vector eventQueue = new Vector();

  public Enumeration getDFAgentDescriptors() {
    return descriptors.elements();
  }

  /**
    This constructor creates a new <em>DF</em> agent. This can be used
    to create additional <em>DF</em> agents, beyond the default one
    created by <em><b>JADE</b></em> on platform startup.
  */
  public df() {

    myOntology = AgentManagementOntology.instance();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));

    dispatcher = new FipaRequestResponderBehaviour(this, mt);

    // Associate each DF action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(AgentManagementOntology.DFAction.REGISTER, new RegBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.DFAction.DEREGISTER, new DeregBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.DFAction.MODIFY, new ModBehaviour());
    dispatcher.registerFactory(AgentManagementOntology.DFAction.SEARCH, new SrchBehaviour());

    // Behaviour to deal with the GUI
    // FIXME. Fabio. This is only necessary because AgentManagementParser.jj
    // parses also the action names. Asap AgentManagementParser.jj is
    // fixed, we can register SHOWGUI with dispatcher
    MessageTemplate mt1 = MessageTemplate.MatchOntology("jade-extensions");
    guiActivator = new FipaRequestResponderBehaviour(this, mt1);
    guiActivator.registerFactory("SHOWGUI", new ShowGUIBehaviour());
  }

  /**
    This method starts all behaviours needed by <em>DF</em> agent to
    perform its role within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    // Show GUI
    //    gui = new DFGUI(this);
    //gui.setVisible(true);
    // Add a message dispatcher behaviour
    addBehaviour(dispatcher);
    addBehaviour(guiActivator);

    // Add an event listener behaviour
    addBehaviour(new CyclicBehaviour() {
      public void action() {
        if(!eventQueue.isEmpty()) {
	  try {
	    GUIEvent re = (GUIEvent)eventQueue.remove(0);
	    switch(re.getKind()) {
	    case GUIEvent.EV_REG:
	      if(re.dfName.equalsIgnoreCase(getName()) || re.dfName.equalsIgnoreCase(getLocalName())) {
		// Register with yourself directly, avoiding deadlock
		DFRegister(re.dfd);
	      }
	      else {
		// Follow ordinary 'fipa-request' protocol
		registerWithDF(re.dfName, re.dfd);
	      }
	      break;
	    case GUIEvent.EV_DEREG:
	      if(re.dfName.equalsIgnoreCase(getName()) || re.dfName.equalsIgnoreCase(getLocalName())) {
		// Deregister with yourself directly, avoiding deadlock
		DFDeregister(re.dfd);
	      }
	      else {
		// Follow ordinary 'fipa-request' protocol
		deregisterWithDF(re.dfName, re.dfd);
	      }
	      break;
	    }
	  }
	  catch(ArrayIndexOutOfBoundsException aioobe) { // Cannot happen
	    aioobe.printStackTrace();
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
	  }
	}
	else {
	  block();
	}
      }
    });
  }

  /**
    Cleanup <em>DF</em> on exit. This method performs all necessary
    cleanup operations during agent shutdown.
  */
  protected void takeDown() {

    if(gui != null)
	gui.disposeAsync();
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

  /**
    A <em>DF</em> agent has a special version of this method in order
    to remember its parents.
  */
  public void registerWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    super.registerWithDF(dfName, dfd);
    parents.addMember(dfName);
  }

  /**
    A <em>DF</em> agent has a special version of this method in order
    to remember its parents.
  */
  public void deregisterWithDF(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    super.deregisterWithDF(dfName, dfd);
    parents.removeMember(dfName);
  }

    // PP
  private void DFRegister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
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

    // PP
  private void DFDeregister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    Object o = descriptors.remove(dfd.getName());
    AgentManagementOntology.DFAgentDescriptor toRemove = (AgentManagementOntology.DFAgentDescriptor)o;
    if(toRemove == null)
      throw myOntology.getException(AgentManagementOntology.Exception.UNABLETODEREG);

    // Update sub-DF table if needed
    subDFs.remove(dfd.getName());
  }

    // PP
  private void DFModify(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
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

  private class GUIEvent {

    public static final int EV_REG = 1;
    public static final int EV_DEREG = 2;

    private int myKind;
    public String dfName;
    public AgentManagementOntology.DFAgentDescriptor dfd;

    public GUIEvent(int kind, String n, AgentManagementOntology.DFAgentDescriptor ad) {
      myKind = kind;
      dfName = n;
      dfd = ad;
    }

    public int getKind() {
      return myKind;
    }

  }

  public void postRegisterEvent(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {
    eventQueue.addElement(new GUIEvent(GUIEvent.EV_REG, dfName, dfd));
    doWake();
  }

  public void postDeregisterEvent(String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {
    eventQueue.addElement(new GUIEvent(GUIEvent.EV_DEREG, dfName, dfd));
    doWake();
  }

}


