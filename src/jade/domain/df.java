
/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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
import java.util.StringTokenizer;
import java.net.InetAddress;
import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.FipaRequestResponderBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.gui.DFGUI;
import jade.gui.GUI2DFCommunicatorInterface;
import jade.proto.FipaRequestInitiatorBehaviour;

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
 
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class df extends GuiAgent implements GUI2DFCommunicatorInterface {

  private abstract class DFBehaviour
    extends FipaRequestResponderBehaviour.Action 
    implements FipaRequestResponderBehaviour.Factory {

    // This will be set by subclasses
    private AgentManagementOntology.DFAction myAction;

    protected DFBehaviour() {
      super(df.this);
    }
  

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(AgentManagementOntology.DFAction dfa) throws FIPAException;

    public void action() {

      try {
      	// Convert message from untyped keyword/value list to ordinary
      	// typed variables, throwing a FIPAException in case of errors
      	
      	ACLMessage msg = getRequest();
      	String content = msg.getContent();
      	// Obtain a DF action from message content
      	try {
      		myAction = AgentManagementOntology.DFAction.fromText(new StringReader(content));
      	}catch(ParseException pe) {
      		//pe.printStackTrace();
      		//System.out.println("DF ParseException with: " + content);
      		throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE+" :content");
      	}
      	catch(TokenMgrError tme) {
      		//tme.printStackTrace();
      		//System.out.println("DF TokenMgrError with: " + content);
      		throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE+" :content");
      	}

      	// Do real action, deferred to subclasses
      	processAction(myAction);
      
      }catch(FIPAException fe) {
      	
      	sendRefuse(fe.getMessage());
      }
       catch(NoSuchElementException nsee) {	
       
       	sendRefuse(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
      }

    }

      public boolean done() {
      	return true;
      }

      public void reset() {}

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
	  else // No constraints: use a default value.
	    respReq = NUMBER_OF_AGENT_FOUND;
	}
      }

      Enumeration e = descriptors.elements();
      
      while(e.hasMoreElements() && (matchesFound.size() < respReq)) {
	Object obj = e.nextElement();
	AgentManagementOntology.DFAgentDescriptor current = (AgentManagementOntology.DFAgentDescriptor)obj;
	if(match(dfd, current)) {
	  matchesFound.put(current.getName(), current);
	}

      }

      sendAgree();

      if((dfDepth == 1) || (matchesFound.size() >= respReq)) {

	StringWriter text = new StringWriter();
	matchesFound.toText(text);

	ACLMessage req = getRequest();
	String content = "(result " + req.getContent() + text.toString() + ")";
	reply.setContent(content);
	reply.setPerformative(ACLMessage.INFORM);
	send(reply);

      }
      else {
	addBehaviour(new RecursiveSearchBehaviour(dfd, reply, matchesFound, dfDepth - 1, getRequest()));
      }

    }

  } // End of SrchBehaviour class


  private class ShowGUIBehaviour extends FipaRequestResponderBehaviour.Action 
                                 implements FipaRequestResponderBehaviour.Factory 
  {
  	protected ShowGUIBehaviour() 
	{
      	super(df.this);
  	}

  	public FipaRequestResponderBehaviour.Action create() 
  	{
      	return new ShowGUIBehaviour();
  	}

  	public void action () 
  	{ 
      	sendAgree();
			if (((df)myAgent).showGui())
			  sendInform();
  		else
			sendFailure("Gui_is_being_shown_already");
  	}
      
      public boolean done() 
  	{
		return true;
      }

      public void reset() 
  	{
      }
  } // End of ShowGUIBehaviour class

  //This behaviour will be use to respond to request from the applet to know the parent with which  this df is federated.
  private class GetParentsBehaviour extends FipaRequestResponderBehaviour.Action implements FipaRequestResponderBehaviour.Factory
  {
  	protected GetParentsBehaviour()
  	{
  		super(df.this);
  	}
  	
  	public FipaRequestResponderBehaviour.Action create()
  	{
  		return new GetParentsBehaviour();
  	}
  	public void action ()
  	{
  		sendAgree();
  		
  		//Construct the reply
  		AgentManagementOntology.DFSearchResult out = new AgentManagementOntology.DFSearchResult();
  		Enumeration p = parents.getMembers();
      while(p.hasMoreElements())
      {
      	AgentManagementOntology.DFAgentDescriptor dfd= new AgentManagementOntology.DFAgentDescriptor();
      	String name = (String)p.nextElement();
      	dfd.setName(name);
      	out.put(name,dfd);
      }
  		
      try{
      	StringWriter text = new StringWriter();
  		  out.toText(text);
  		  ACLMessage req = getRequest();
  		  ACLMessage inform = req.createReply();
  		  //inform.setSource(myAgent.getName());
  		  inform.setPerformative(ACLMessage.INFORM);
  		  //no information about the action inserted in the content otherwise parser exception
  		  String content = "(result "  + text.toString() + ")";
  		  inform.setContent(content);
  		  send(inform);
      }catch(FIPAException e){
      	sendFailure("Impossible to provide the needed information");
      }
  	}
  	public boolean done()
  	{
  		return true;
  	}
  	public void reset()
  	{
  		
  	}
  	
  }
  
  //This Behaviour returns the description of this df used  to federate with another df 
  //It is used to reply to a request from the applet 
  private class GetDescriptionOfThisDFBehaviour extends FipaRequestResponderBehaviour.Action implements FipaRequestResponderBehaviour.Factory
  {
  	protected GetDescriptionOfThisDFBehaviour()
  	{
  		super(df.this);
  	}
  	
  	public FipaRequestResponderBehaviour.Action create()
  	{
  		return new GetDescriptionOfThisDFBehaviour();

  	}
  	
  	public void action()
  	{ 
  		
  		sendAgree();
  		ACLMessage req = getRequest();
  		ACLMessage inform = req.createReply();
  		inform.setPerformative(ACLMessage.INFORM);
  		//inform.setSource(myAgent.getName());
  		StringWriter text = new StringWriter();
  	  thisDF.toText(text);	
  		String content = "(:df-description " + text.toString() + ")";
  		inform.setContent(content);
  		send(inform);
  	}
  	
  	public boolean done()
  	{
  		return true;
  	}
  	public void reset()
  	{
  		
  	}

  }//End GetDescriptionOfThisDFBehaviour
  
  // This behaviour allows the federation of this df with another df required by the APPLET
  private class FederateWithBehaviour extends FipaRequestResponderBehaviour.Action implements FipaRequestResponderBehaviour.Factory 
  {
  	private class mySequentialBehaviour extends SequentialBehaviour
    {
  	  String parentAgentName = null;
  	  ACLMessage request;
      String token = "FEDERATE_WITH";
  	
  	
  	//This behaviour send the agree message to the dfproxy for the request of a federation
  	private class FirstStep extends SimpleBehaviour
  	{
  	  boolean finished = false;
  		
  		FirstStep(Agent a,ACLMessage msg)
  		{
  			super(a);
  			request = (ACLMessage)msg.clone();
  		}
  		
  		public void action()
  		{
  			ACLMessage reply = request.createReply();
  			reply.setPerformative(ACLMessage.AGREE);
  			//reply.setSource(myAgent.getName());
  			send(reply);
        finished = true;
  		}
  		
  		public boolean done()
  		{
  			return finished;
  		}
  	}
  	
  	// this behaviour send the reply to the dfdproxy: inform if the federation occur failure otherwise
  	private class ThirdStep extends SimpleBehaviour
  	{
  		private boolean finished = false;
  		private myRegisterWithDFBehaviour previousStep;
  		
  		ThirdStep(myRegisterWithDFBehaviour b)
  		{
  			super(df.this);
  			previousStep = b;
  		}
  		
  		public void action()
  		{
  			ACLMessage reply = request.createReply();
  			//reply.setSource(myAgent.getName());
  			if(previousStep.correctly == true)
  			{
  				reply.setPerformative(ACLMessage.INFORM);
  			  reply.setContent("( done ( " + token + " ) )");
  			  }
  			else
  			{
  			  reply.setPerformative(ACLMessage.FAILURE);
  			  reply.setContent("( ( action " + myAgent.getLocalName() + " " + token + " ) " + "federation not possible" + ")");
  			}
  			send(reply);
  			finished = true;
  		}
  		
  		public boolean done()
  		{
  			return finished;
  		}
  	}
  	
  	mySequentialBehaviour(ACLMessage msg)
  	{
  		addSubBehaviour(new FirstStep(df.this,msg));
  		
  		//parse the content to find the name of the parent
  	  StringTokenizer st = new StringTokenizer(msg.getContent()," \t\n\r()",false);
	    while (!token.equalsIgnoreCase(st.nextToken())) {}
      parentAgentName = st.nextToken();
      
  		try
  		{ 
  		  myRegisterWithDFBehaviour secondStep = new myRegisterWithDFBehaviour(df.this,parentAgentName,thisDF);
  		  addSubBehaviour(secondStep);
  		  addSubBehaviour(new ThirdStep(secondStep));
  		}catch(FIPAException e){
  		 System.err.println(e.getMessage());
  		}
  		
  	}
  } //End mySequentialBehaviour

  	protected FederateWithBehaviour()
  	{
  		super(df.this);
  	}
  	
  	public FipaRequestResponderBehaviour.Action create()
  	{
  		return new FederateWithBehaviour();
  	}
  	
  	public void action()
  	{
     
  		addBehaviour(new mySequentialBehaviour(getRequest()));
  	}
  	
  	public boolean done()
  	{
  		return true;
  	}
  	
  	public void reset(){}
  }
  

  
  //This behaviour allow the applet to required the df to deregister itself form a parent of the federation
  private class DeregisterFromBehaviour extends FipaRequestResponderBehaviour.Action implements FipaRequestResponderBehaviour.Factory
  {
 	 private class mySequentialBehaviourForDereg extends SequentialBehaviour 
  	{
  	  String parentAgentName = null;
  	  ACLMessage request;
  	  String token = "DEREGISTER_FROM";
  	  
  	  private class FirstStep extends SimpleBehaviour
  	  {
  	  	boolean finished = false;
  	  	FirstStep(Agent a,ACLMessage m)
  	  	{
  	  		super(a);
  	  		request=(ACLMessage)m.clone();
  	  	}
  	  	
  	  	public void action()
  	  	{
  	  		ACLMessage reply = request.createReply();
  	  		reply.setPerformative(ACLMessage.AGREE);
  	  		send(reply);
  	  		finished = true;
  	  	}
  	  	public boolean done()
  	  	{
  	  		return finished;
  	  	}
  	  	
  	  }
  	  
  	  private class ThirdStep extends SimpleBehaviour
  	  {
  	  	private boolean finished = false;
  	  	private myDeregisterWithDFBehaviour previousStep;
  	  	
  	  	ThirdStep(myDeregisterWithDFBehaviour b)
  	  	{
  	  		super(df.this);
  	  		previousStep = b;
  	  	}
  	  	
  	  	public void action()
  	  	{
  	  		ACLMessage reply = request.createReply();
  	  		if(previousStep.correctly == true)
  	  		{
  	  			reply.setPerformative(ACLMessage.INFORM);
  	  			reply.setContent("(done ( "+token+" ) )");
  	  		}
  	  		else
  	  		{
  	  			reply.setPerformative(ACLMessage.FAILURE);
  	  			reply.setContent("( ( action " + myAgent.getLocalName() + " "+ token+ " ) " + "deregister not possible "+ ")");
  	  		}
  	  		send(reply);
  	  		finished = true;
  	  	}
  	  	
  	  	public boolean done()
  	  	{
  	  		return finished;
  	  	}
  	  }
  	  
  	  mySequentialBehaviourForDereg(ACLMessage msg)
  	  {
  	  	addSubBehaviour(new FirstStep(df.this, msg));
  	  	
  	  	StringTokenizer st = new StringTokenizer(msg.getContent(), " \t\n\r()", false);
  	  	while(!token.equalsIgnoreCase(st.nextToken())){}
  	  	parentAgentName = st.nextToken();
  	  	try
  	  	{
  	  		myDeregisterWithDFBehaviour secondStep = new 	myDeregisterWithDFBehaviour(df.this,parentAgentName, thisDF);
  	  		addSubBehaviour(secondStep);
  	  		addSubBehaviour(new ThirdStep(secondStep));
  	  	}catch(FIPAException e){System.err.println(e.getMessage());}
  	  }
  	}//End mySequentialBehaviourForDereg
  	
  	protected DeregisterFromBehaviour()
  	{
  		super(df.this);
  	}
  	
  	public FipaRequestResponderBehaviour.Action create()
  	{
  		return new DeregisterFromBehaviour();
  	}
  	
  	public void action()
  	{
  		addBehaviour(new mySequentialBehaviourForDereg(getRequest()));
  	}
  	
  	public boolean done()
  	{
  		return true;
  	}
  	public void reset(){}
  	
  }//End DeregisterFromBehaviour
  
 

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
      ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
      request.setConversationId(convID);
      request.setSource(getLocalName());

      
      Enumeration e = subDFs.elements();
      while (e.hasMoreElements()){
      String subDF = (String)e.nextElement();
      //ACLMessage copy = (ACLMessage)request.clone();
      //copy.removeAllDests();
      //copy.addDest(subDF);
      try {
      	//searchThemAll.addSubBehaviour(new SearchDFBehaviour(df.this, copy, dfd, constraints, result));
      	searchThemAll.addSubBehaviour(new RequestDFActionBehaviour(df.this,subDF,AgentManagementOntology.DFAction.SEARCH,dfd,constraints,result)); 
      }catch(FIPAException fe) {
      		fe.printStackTrace();}
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
      			reply.setPerformative(ACLMessage.INFORM);
      			send(reply);
      		}
      		catch(FIPAException fe) {
      			fe.printStackTrace();
      		}
      	}
      });
			     }

  } // End of RecursiveSearchBehaviour

  
 
 // This private class extends the RequestDFActionBehaviour to make the search of agents with a Df. 
 private class mySearchWithDFBehaviour extends RequestDFActionBehaviour
 {
 	 mySearchWithDFBehaviour(Agent a,String dfName,AgentManagementOntology.DFAgentDescriptor dfd,Vector constraints,AgentManagementOntology.DFSearchResult r) throws FIPAException
 	 {
 	 	super(a,dfName,AgentManagementOntology.DFAction.SEARCH,dfd,constraints,r);
 	 }
 	 
 	 //Ovveride the method handleInform to update the found variable with the result of the search.
 	 protected void handleInform(ACLMessage reply)
 	 {
 	 	//System.out.println("\nResponder has just informed me that the action has been carried out.");
    
    //System.out.println(reply.toString());

    StringReader textIn = new StringReader(reply.getContent());

    try{  
    	found = AgentManagementOntology.DFSearchResult.fromText(textIn);
      if (gui != null)	
	  	{
	  		if((found.elements()).hasMoreElements())
	  		  gui.showStatusMsg("Request processed.Ready for new request.");
	  		else
	  		  gui.showStatusMsg("Request processed. No agent found.");
	    	gui.refreshLastSearch(found.elements());
	  	}
	
    } catch(jade.domain.ParseException e){}
    catch (jade.domain.FIPAException e1){}
 	 }
 }
 
 // This private class extends the RequestDFActionBehaviour to make the registration of an agent with a DF.
 // In particular it will be use to manage the federate action with a df.
 // The methos handle Inform has been override to update the parent and to refresh the gui if showed.
 
 private class myRegisterWithDFBehaviour extends RequestDFActionBehaviour 
 {
 	 String dfName;
 	 boolean correctly = false; // used to verify if the protocol finish correctly
 	 
 	 myRegisterWithDFBehaviour(Agent a, String dfName,AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException
 	 {
 	 	 super(a, dfName,AgentManagementOntology.DFAction.REGISTER,dfd);
 	 	 this.dfName = dfName;
 	 
 	 }
 	 
 	 protected void handleInform(ACLMessage msg)
 	 {
 	 
 	 	correctly = true; 
 	 	parents.addMember(dfName); //remember the parents
 	  //refresh the gui
 	 	if(gui != null)
 	  	{
 	  		gui.refreshFederation();
 	  		gui.showStatusMsg("Request processed. Ready for new request.");
 	  	}
 	 	
 	 }
 	 protected void handleRefuse(ACLMessage msg)
 	 {
 	 	if (gui != null)
    	gui.showStatusMsg("Request refused.");
 	 }
 	 
 	 
 	
 }
 
 
 // This private class extends the RequestDFActionBehavior in order to provide the deregistration 
 // of this df with another df.
 private class myDeregisterWithDFBehaviour extends RequestDFActionBehaviour 
 {
 	String dfName;
 	boolean correctly = false; // used to verify if the protocol finish correctly

 	myDeregisterWithDFBehaviour(Agent a,String dfName,AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException
 	{
 		super(a, dfName,AgentManagementOntology.DFAction.DEREGISTER,dfd);
 		this.dfName = dfName;
 	}
 	 protected void handleInform(ACLMessage reply)
 	 {
 	 	correctly = true;
 	 	parents.removeMember(dfName);
 	 	
 	 	if(gui != null)
 	 		{
 	 			gui.refreshFederation();
 	 		  gui.showStatusMsg("Request processed. Ready for new request.");
 	 		}
 	 }
 	 protected void handleRefuse(ACLMessage msg)
 	 {
 	 	if (gui != null)
 	 		gui.showStatusMsg("Request refused.");
 	 }
   
 }
 
  private static int NUMBER_OF_AGENT_FOUND = 1000;
  /**
  @serial
  */
  private AgentManagementOntology myOntology;
  /**
  @serial
  */
  private FipaRequestResponderBehaviour dispatcher;
  /**
  @serial
  */
  private FipaRequestResponderBehaviour jadeExtensionDispatcher;
  /**
  @serial
  */
  private Hashtable descriptors = new Hashtable();
  /**
  @serial
  */
  private AgentManagementOntology.DFSearchResult found;
  	
  //OLD Version: private Hashtable subDFs = new Hashtable();
  /**
  @serial
  */
  Vector subDFs = new Vector();
  /**
  @serial
  */
  private AgentGroup parents = new AgentGroup();
  /**
  @serial
  */
  private DFGUI gui;
  // Current description of the df
  /**
  @serial
  */
  private AgentManagementOntology.DFAgentDescriptor thisDF = null;
  
  //Vector to maintain the constrait for the search inserted by the user.
  /**
  @serial
  */
  private Vector searchConstraint = null;
  
  
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
    jadeExtensionDispatcher = new FipaRequestResponderBehaviour(this, mt1);
    jadeExtensionDispatcher.registerFactory("SHOWGUI", new ShowGUIBehaviour());
    // The following three actions are used only by the DFApplet	
    jadeExtensionDispatcher.registerFactory("GETPARENTS", new GetParentsBehaviour()); 
    jadeExtensionDispatcher.registerFactory("FEDERATE_WITH", new FederateWithBehaviour());
    jadeExtensionDispatcher.registerFactory("DEREGISTER_FROM", new DeregisterFromBehaviour());
    jadeExtensionDispatcher.registerFactory("GETDEFAULTDESCRIPTION", new GetDescriptionOfThisDFBehaviour()); 
  }

  /**
    This method starts all behaviours needed by <em>DF</em> agent to
    perform its role within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    
    // Add a message dispatcher behaviour
    addBehaviour(dispatcher);
    addBehaviour(jadeExtensionDispatcher);
    setDescriptionOfThisDF(getDefaultDescription());
    setConstraints();
  }  // End of method setup()

	/**
	  This method make visible the GUI of the DF.
	  @return true if the GUI was not visible already, false otherwise.
	*/
  public boolean showGui() {
   if (gui == null) 
  		{
			gui = new DFGUI((GUI2DFCommunicatorInterface)df.this, false);
			gui.refresh();
			
			gui.setVisible(true);
			return true;
  		}
  	
   return false;
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

  
    // This method throws a FIPAException if the attribute is
    // mandatory for the current DF action but it is a null object
    // reference

  private void checkAttribute(String dfAction, String attributeName, String attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForDF(dfAction, attributeName) && (attributeValue == null))
	throw myOntology.getException(AgentManagementOntology.Exception.MISSINGATTRIBUTE+ " "+attributeName);
    }

  private void checkAttributeList(String dfAction, String attributeName, Enumeration attributeValue) throws FIPAException {
      if(myOntology.isMandatoryForDF(dfAction, attributeName) && (!attributeValue.hasMoreElements()))
	throw myOntology.getException(AgentManagementOntology.Exception.MISSINGATTRIBUTE+ " "+attributeName);
    }

  private void checkAllAttributes(String action,AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
  
  		checkAttribute(action, AgentManagementOntology.DFAgentDescriptor.NAME, dfd.getName());
      checkAttributeList(action, AgentManagementOntology.DFAgentDescriptor.SERVICES, dfd.getAgentServices());
      checkAttribute(action, AgentManagementOntology.DFAgentDescriptor.TYPE, dfd.getType());
      checkAttributeList(action,AgentManagementOntology.DFAgentDescriptor.PROTOCOLS, dfd.getInteractionProtocols());
      checkAttribute(action, AgentManagementOntology.DFAgentDescriptor.ONTOLOGY, dfd.getOntology());
      checkAttributeList(action, AgentManagementOntology.DFAgentDescriptor.ADDRESS, dfd.getAddresses());
      checkAttribute(action, AgentManagementOntology.DFAgentDescriptor.OWNERSHIP, dfd.getOwnership());
      checkAttribute(action, AgentManagementOntology.DFAgentDescriptor.DFSTATE, dfd.getDFState());

  
  }  
    // PP
  private void DFRegister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    
  	checkAllAttributes(AgentManagementOntology.DFAction.REGISTER,dfd);
  
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
      if(type.equalsIgnoreCase("fipa-df") || type.equalsIgnoreCase("df")) 
      	subDFs.addElement(dfd.getName());
    }

    // UPDATE GUI IF OPENED
    if (gui != null)
      gui.refresh();
  }

    // PP
  private void DFDeregister(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    
  	checkAllAttributes(AgentManagementOntology.DFAction.DEREGISTER,dfd);
  	
  	Object o = descriptors.remove(dfd.getName());
    AgentManagementOntology.DFAgentDescriptor toRemove = (AgentManagementOntology.DFAgentDescriptor)o;
    
    if(toRemove == null)
      throw myOntology.getException(AgentManagementOntology.Exception.UNABLETODEREG);

    // Update sub-DF table if needed
    // OldVersion: subDFs.remove(dfd.getName());
       subDFs.removeElement(dfd.getName());
       
    // UPDATE GUI IF OPENED
    if (gui != null)
      gui.refresh();
  }

    // PP
  private void DFModify(AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException {
    
  	checkAllAttributes(AgentManagementOntology.DFAction.MODIFY, dfd);
  	
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

    // UPDATE GUI IF OPENED
    if (gui != null)
      gui.refresh();
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

  
	/////////////////////////////////
	// GUI HANDLING

	// DF GUI EVENT 
	private class DFGuiEvent extends GuiEvent
	{
		public static final int REGISTER = 1001;
		public static final int DEREGISTER = 1002;
		public static final int MODIFY = 1003;
		public static final int SEARCH = 1004;
		public static final int FEDERATE = 1005;
		public static final int SEARCH_WITH_CONSTRAINT = 1006;
	
		public String dfName;
		public AgentManagementOntology.DFAgentDescriptor dfd;
		public Vector constraints = null;

		public DFGuiEvent(Object source, int type, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
		{
			super(source, type);
			this.dfName = dfName;
			this.dfd =dfd;
		}
		
		public DFGuiEvent(Object source, int type, String dfName, AgentManagementOntology.DFAgentDescriptor dfd, Vector constraints)
		{
			super(source,type);
      this.dfName = dfName;
      this.dfd = dfd;
		  this.constraints = constraints; 
		}
	}
		
	// METHODS PROVIDED TO THE GUI TO POST EVENTS REQUIRING AGENT DATA MODIFICATIONS 	
	public void postRegisterEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.REGISTER, dfName, dfd);
		postGuiEvent(ev);
	}
	
	public void postDeregisterEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.DEREGISTER, dfName, dfd);
		postGuiEvent(ev);
	}

	public void postModifyEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.MODIFY, dfName, dfd);
		postGuiEvent(ev);
	}

	public void postSearchEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
	{
	 DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.SEARCH, dfName, dfd);
	 postGuiEvent(ev);
	}	
	
	public void postFederateEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source,DFGuiEvent.FEDERATE, dfName, dfd);
		postGuiEvent(ev);
	
	}
	
	public void postSearchWithConstraintEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd, Vector constraint)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.SEARCH_WITH_CONSTRAINT,dfName,dfd,constraint);
		postGuiEvent(ev);
	}
	
	// AGENT DATA MODIFICATIONS FOLLOWING GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
		try
		{
			DFGuiEvent e;
			switch(ev.getType()) 
			{
			case DFGuiEvent.EXIT:
				gui.disposeAsync();
				gui = null;
				doDelete();
				break;
			case DFGuiEvent.CLOSEGUI:
				gui.disposeAsync();
				gui = null;
				break;
			case DFGuiEvent.REGISTER:
				e = (DFGuiEvent) ev;
				if (e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())) 
				{
					// Register an agent with this DF
						DFRegister(e.dfd);
				}
				else 
				{
					// Register an agent with another DF. 
					// The agent to be registered should be this DF --> FEDERATE
					addBehaviour(new myRegisterWithDFBehaviour(this,e.dfName, e.dfd));
				}
				break;
			case DFGuiEvent.DEREGISTER:
				e = (DFGuiEvent) ev;
				if(e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())) 
				{
					// Deregister an agent with this DF
					DFDeregister(e.dfd);
				}
				else 
				{
					// Deregister an agent with another DF. 
					// The agent to be deregistered should be this DF --> DEFEDERATE
					addBehaviour(new myDeregisterWithDFBehaviour(this,e.dfName,e.dfd));  
				}
				break;
			case DFGuiEvent.MODIFY:
				e = (DFGuiEvent) ev;
				if(e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())) 
				{
					// Modify the description of an agent with this DF
					DFModify(e.dfd);
				}
				else 
				{
					// Modify the description of an agent with another DF
					// The agent whose description has to be modified should be this DF --> MODIFY FEDERATION
					modifyDFData(e.dfName, e.dfd);
				}
				break;
		  case DFGuiEvent.SEARCH:
		  	e = (DFGuiEvent) ev;
		  	searchDFAgentDescriptor(e.dfd,e.dfName,null);	 
		  	break;
		 	case DFGuiEvent.FEDERATE:
		 		e = (DFGuiEvent) ev;
		 	  //registerWithDF(e.dfName, e.dfd);
		 	  if (!(e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())))
		 	  	{
		 	  		if(gui != null)
		 	  			gui.showStatusMsg("Process your request & waiting for result.");
		 	  		addBehaviour(new myRegisterWithDFBehaviour(this,e.dfName,e.dfd));
		 	  	}
		 	  else
		 	  { 
		 	  	if (gui != null)
		 	  		gui.showStatusMsg("Self Federation not allowed");
		 	  	throw new FIPAException("Self Federation not allowed");
		 	  }
		 		break;
		 	case DFGuiEvent.SEARCH_WITH_CONSTRAINT:
		 		e = (DFGuiEvent)ev;
		 		searchConstraint = (Vector)(e.constraints).clone();
		 		searchDFAgentDescriptor(e.dfd,e.dfName,searchConstraint);
		 		break;
		 
			} // END of switch
		} // END of try
		catch(FIPAException fe) 
		{
			fe.printStackTrace();
		}
	}

	////////////////////////////////////////////////
	// Methods used by the DF GUI to get the DF data
	public Enumeration getAllDFAgentDsc() 
	{
		return descriptors.elements();
	}

	// This method returns the descriptor of an agent registered with the df.
	public AgentManagementOntology.DFAgentDescriptor getDFAgentDsc(String name) throws FIPAException
	{
		AgentManagementOntology.DFAgentDescriptor dsc = (AgentManagementOntology.DFAgentDescriptor) descriptors.get(name);
		if(dsc == null)
			throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
		return(dsc);
	}
	
	// returns the description of the agents result of the search. 
	public AgentManagementOntology.DFAgentDescriptor getDFAgentSearchDsc(String name) throws FIPAException
	{
		AgentManagementOntology.DFAgentDescriptor out = null;
	
		Enumeration e = found.elements();
		while (e.hasMoreElements())
		{
			 AgentManagementOntology.DFAgentDescriptor dfd = (AgentManagementOntology.DFAgentDescriptor) e.nextElement();
			 if (dfd.getName().equalsIgnoreCase(name))
			  out = dfd;	
		}
		if (out == null) throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
		
		return out;
	}
	//This method implements the search feature. 
	
	public void searchDFAgentDescriptor(AgentManagementOntology.DFAgentDescriptor dfd, String responder,Vector constraints)
	{
	  	gui.showStatusMsg("Process your request & waiting for result...");
	  	try{
	  		addBehaviour(new mySearchWithDFBehaviour(this,responder,dfd,constraints,null));
	  	}catch(FIPAException fe){
	  	  fe.printStackTrace();
	  	}
	}
	
	/**
  * This method creates the DFAgent descriptor for this df used to federate with other df.
	*/
	private AgentManagementOntology.DFAgentDescriptor getDefaultDescription()
	{
	  	AgentManagementOntology.DFAgentDescriptor out = new AgentManagementOntology.DFAgentDescriptor();
	  	//System.out.println("Initialization of the description of thisDF");
			out.setName(getName());
		  out.addAddress(getAddress());
		  try{
		  	out.setOwnership(InetAddress.getLocalHost().getHostName());
		  }catch (java.net.UnknownHostException uhe){
		  	out.setOwnership("unknown");}
		  out.setType("fipa-df");
		  out.setDFState("active");
		  out.setOntology("fipa-agent-management");
		  //thisDF.setLanguage("SL0"); not exist method setLanguage() for dfd in Fipa97
		  out.addInteractionProtocol("fipa-request");
		  AgentManagementOntology.ServiceDescriptor sd = new 	AgentManagementOntology.ServiceDescriptor();
	    sd.setType("fipa-df");
	    sd.setName("federate-df");
	    sd.setOntology("fipa-agent-management");
		  out.addAgentService(sd);
		  return out;
	}

	//Method to know the parent of this df. DFs with which the current DF is federated.
	public Enumeration getParents()
	{
		return (parents.getMembers());
	
	}
	
	//Method to know the df registered with this df
	public Enumeration getChildren()
	{
		return (subDFs.elements());
	}
	
	/*
	* This methos set the description of the df according to the DFAgentManagementOntology.DFAgentAgentDescriptor passed.
	* The programmers can call this method to provide a different initialization of the description of the df they are implemented.
	* The method is called inside the setup of the agent and set the df description using a default description.
	*/
	public void setDescriptionOfThisDF(AgentManagementOntology.DFAgentDescriptor dfd)
	{
		thisDF = (AgentManagementOntology.DFAgentDescriptor)dfd.clone();
	}
	/**
	* This method returns the current description of this DF
	*/
	public AgentManagementOntology.DFAgentDescriptor getDescriptionOfThisDF()
	{
		return thisDF;
	}
	
	// This method is used to get the constraint manitained by the df to perform a search with constraint
	public Vector getConstraints()
	{
		return searchConstraint;
	}
	
	/*
	This method can be used to add a parent (a DF with which the this DF is federated). 
	*/
	public void addParent(String dfName)
	{
		parents.addMember(dfName);
	}
	
	
	//This method is called in the setup method to initialize the constraint vector for the search operation.
	private void setConstraints()
	{
		AgentManagementOntology.Constraint c = new 	AgentManagementOntology.Constraint();
		c.setName(	AgentManagementOntology.Constraint.DFDEPTH);
		c.setFn(AgentManagementOntology.Constraint.MAX);
		c.setArg(1);
		searchConstraint = new Vector();
		searchConstraint.add(c);
	}
	
	// Method for refresh the gui of an applet of the df.
	public void postRefreshAppletGuiEvent(Object o){}
}