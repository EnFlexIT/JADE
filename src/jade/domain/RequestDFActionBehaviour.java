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

import java.io.StringReader;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Vector;

import jade.core.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.sl.SL0Codec;

import jade.proto.FipaRequestInitiatorBehaviour;

/** 
  This class extends the <code>FipaRequestIntiatorBehaviour</code> in order to request a <em>DF</em> 
  to perform a specific action.
  This class implements all the abstract method of the super classes. 
  The behaviour can be added to an agent directly, or the class can be extended to override the methods 
  to react to the received messages in a specific manner.
  The class has two constructor. The first generic constructor can be use for all the action a DF can perform.
  if this constructor is used to perform a search action the constraint will be the default one.
  The second constructor provided is specific for a search action.
  
  @see jade.domain.AgentManagementOntology.DFAction
  
  @author Tiziana Trucco
  @version $Date$ $Revision$
*/
public class RequestDFActionBehaviour extends FipaRequestInitiatorBehaviour {

  private static final MessageTemplate mt = 
    MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));

  /**
  @serial
  */
	private AgentManagementOntology.DFSearchResult result = null;
  /**
  @serial
  */
	private AgentManagementOntology.DFAction  action = null;
  
  private static ACLMessage msgtemp = new ACLMessage(ACLMessage.REQUEST); 
  
    /**
  *  Create a behaviour to request a DF to perform a specific action. 
  *  Using this constructor, is possible to pass all information necessary to
  *  set up a <em>Directory Facilitator</em> search. 
  *  @param a The agent this behaviour belongs to, i.e. the agent who is
  *  interested in the search result.
  *  @param dfName The df who will perform the action.
  *  @param dfd An agent descriptor used according to the action required.
  *  @param constraint A <code>Vector</code> that will be filled with
  *  desired search constraints. When no particular constraint is
  *  needed, a <code>null</code> object reference can be passed.
  *  @param r This object will contain the results of the search. At the
  *  end of this <code>Behaviour</code>, this object can be accessed to
  *  retrieve all agent descriptors matching search criteria. If
  *  something goes wrong during the search, <code>DFSearchResult</code>
  *  object will throw a suitable <code>FipaException</code> as soon as
  *  a data access is attempted.
  *  @see jade.domain.AgentManagementOntology.Constraint
  */
   public RequestDFActionBehaviour(Agent a, String dfName, String dfAction, AgentManagementOntology.DFAgentDescriptor dfd,
			   Vector constraints, AgentManagementOntology.DFSearchResult r) throws FIPAException {
    	
		super(a, msgtemp, mt);

	  ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	  
	  if(r != null)
    	result = r;
    else
    	result = new AgentManagementOntology.DFSearchResult();
    
    StringWriter textOut = new StringWriter();
    
    String sender = myAgent.getLocalName();
    AgentManagementOntology o = AgentManagementOntology.instance();
    if(dfName == null)
      
      	throw o.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
    
    msg.setLanguage(SL0Codec.NAME);
    msg.setOntology(FIPAAgentManagementOntology.NAME);
    msg.addDest(dfName);
    
    if(AgentManagementOntology.DFAction.SEARCH.equalsIgnoreCase(dfAction))
    {
    	AgentManagementOntology.DFSearchAction dfsa = new AgentManagementOntology.DFSearchAction();
    	dfsa.setActor(dfName);
    	dfsa.setArg(dfd);
    	if(constraints != null) 
    	{
    		Enumeration e = constraints.elements();
    		while(e.hasMoreElements()) 
    		{
    			AgentManagementOntology.Constraint c = (AgentManagementOntology.Constraint)e.nextElement();
    			dfsa.addConstraint(c);
    		}
    	}
      else 
      { // default constraints
      	AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      	c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      	c.setFn(AgentManagementOntology.Constraint.EXACTLY);
      	c.setArg(1);
      	dfsa.addConstraint(c);
      }
      action = dfsa;
      dfsa.toText(textOut);
    }
    else //not a search action
    {
    	AgentManagementOntology.DFAction dfa = new AgentManagementOntology.DFAction();
    	dfa.setActor(dfName);
    	dfa.setArg(dfd);
    	// no check is made of the correctness of the action
    	dfa.setName(dfAction);
    	action = dfa;
    	dfa.toText(textOut);
    }
   
    msg.setContent(textOut.toString());
    //reset is necessary to change the message sent by FipaRequestInitiatorBehaviour
    reset(msg);

  }

  /**
  * Create a behaviour to request a DF to perform a specific action.
  *
  * @param a The agent this behaviour belongs to, i.e the agent who is interested in the action.
  * @param dfName The DF who will perform the action.
  * @param dfAction The action requested to the DF.
  * @param dfd An agent descriptor that will be use according to the action required to the DF.
  */
  public RequestDFActionBehaviour(Agent a, String dfName, String dfAction, AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException
  {
  	this(a,dfName,dfAction,dfd,null,null);
  	
  }

  /**
    Method to handle <code>not-understood</code> replies.
    @param reply The actual ACL message received. It is of
    <code>not-understood</code> type and matches the conversation
    template.
  */
  protected void handleNotUnderstood(ACLMessage reply) {
    if(result != null)
    {
  	AgentManagementOntology myOntology = AgentManagementOntology.instance();
    result.setException(myOntology.getException(AgentManagementOntology.Exception.FAILEDMANACTION));
    }
  }

  /**
    Method to handle <code>refuse</code> replies.
    @param reply The actual ACL message received. It is of
    <code>refuse</code> type and matches the conversation
    template.
  */
  protected void handleRefuse(ACLMessage reply) {
  	if (result != null)
  	{
    String content = reply.getContent();
    StringReader text = new StringReader(content);
    result.setException(FIPAException.fromText(text));
  	}
  }

  /**
    Method to handle <code>agree</code> replies.
    @param reply The actual ACL message received. It is of
    <code>agree</code> type and matches the conversation
    template.
  */
  protected void handleAgree(ACLMessage reply) {
  //	System.out.println("Agent " + myAgent + "received the following message");
  //	System.out.println(reply.toString());
  }

  /**
    Method to handle <code>failure</code> replies.
    @param reply The actual ACL message received. It is of
    <code>failure</code> type and matches the conversation
    template.
  */
  protected void handleFailure(ACLMessage reply) {
  	if (result != null)
    {
    String content = reply.getContent();
    StringReader text = new StringReader(content);
    result.setException(FIPAException.fromText(text));
  	}
  }

  /**
    Method to handle <code>inform</code> replies.
    @param reply The actual ACL message received. It is of
    <code>inform</code> type and matches the conversation
    template.
  */
  protected void handleInform(ACLMessage reply) {

    if (AgentManagementOntology.DFAction.SEARCH.equalsIgnoreCase(action.getName()))
    {
  	  // Extract agent descriptors from reply message
      String content = reply.getContent();
      StringReader textIn = new StringReader(content);
      try {
        AgentManagementOntology.DFSearchResult found = AgentManagementOntology.DFSearchResult.fromText(textIn);
        try {
	        Enumeration e = found.elements();
	        while(e.hasMoreElements()) {
	          AgentManagementOntology.DFAgentDescriptor current = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
	          result.put(current.getName(), current);
	        }
      }
      catch(FIPAException fe) {
	      result.setException(fe);}
    }
    catch(ParseException pe) {
      pe.printStackTrace();}
    catch(TokenMgrError tme) {
      tme.printStackTrace();}
    }
  }


}
