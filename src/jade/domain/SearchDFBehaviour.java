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

import java.io.StringReader;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Vector;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.FipaRequestInitiatorBehaviour;

/** 
  Search a <em>DF</em> agent for information. This behaviour allows
  applications to access <em>DF</em> agents with an object oriented
  interface, without resorting to direct <em>FIPA ACL</em> message
  passing and reply content parsing. The full <em><b>FIPA</b></em>
  capabilities are supported, such as search constraints and recursive
  searches through <em>DF</em> federations.
  
  Javadoc documentation for the file
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public class SearchDFBehaviour extends FipaRequestInitiatorBehaviour {

  private static final MessageTemplate mt = 
    MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			MessageTemplate.MatchOntology("fipa-agent-management"));

  private AgentManagementOntology.DFSearchResult result = null;

  /**
   Create a behaviour to search a <em>DF</em> agent for
   informations. Using this constructor, all information necessary to
   set up a <em>Directory Facilitator</em> search must be passed as
   parameter before starting the <code>SearchDFBehaviour</code>.
   @param a The agent this behaviour belongs to, i.e. the agent who is
   interested in the search result.

   @param msg The ACL <code>request</code> message to send. Its
   <code>:receiver</code> slot must contain the name of the
   <em>DF</em> agent to search, and its <code>:sender</code> slot must
   hold the name of the agent performing the search.
   @param dfd An agent descriptor that will be used as a template
   against which to match the descriptors contained inside the
   <em>DF</em> agent to search.
   @param constraint A <code>Vector</code> that will be filled with
   desired search constraints. When no particular constraint is
   needed, a <code>null</code> object reference can be passed.
   @param r This object will contain the results of the search. At the
   end of this <code>Behaviour</code>, this object can be accessed to
   retrieve all agent descriptors matching search criteria. If
   something goes wrong during the search, <code>DFSearchResult</code>
   object will throw a suitable <code>FipaException</code> as soon as
   a data access is attempted.
   @see jade.domain.AgentManagementOntology.Constraint
   */
  public SearchDFBehaviour(Agent a, ACLMessage msg, AgentManagementOntology.DFAgentDescriptor dfd,
			   Vector constraints, AgentManagementOntology.DFSearchResult r) throws FIPAException {
    super(a, msg, mt);

    result = r;

    String dfName = msg.getFirstDest();
    String sender = msg.getSource();
    AgentManagementOntology o = AgentManagementOntology.instance();
    if((dfName == null)||(sender == null))
      throw o.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
    if(!sender.equalsIgnoreCase(a.getLocalName()))
      throw o.getException(AgentManagementOntology.Exception.UNAUTHORISED);

    msg.setLanguage("SL0");
    msg.setOntology("fipa-agent-management");

    AgentManagementOntology.DFSearchAction dfsa = new AgentManagementOntology.DFSearchAction();
    dfsa.setActor(dfName);
    dfsa.setArg(dfd);
    if(constraints != null) {
      Enumeration e = constraints.elements();
      while(e.hasMoreElements()) {
	AgentManagementOntology.Constraint c = (AgentManagementOntology.Constraint)e.nextElement();
	dfsa.addConstraint(c);
      }
    }
    else {
      AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      c.setFn(AgentManagementOntology.Constraint.EXACTLY);
      c.setArg(1);
      dfsa.addConstraint(c);
    }

    StringWriter textOut = new StringWriter();
    dfsa.toText(textOut);
    msg.setContent(textOut.toString());

  }

  /**
    Method to handle <code>not-understood</code> replies.
    @param reply The actual ACL message received. It is of
    <code>not-understood</code> type and matches the conversation
    template.
  */
  protected void handleNotUnderstood(ACLMessage reply) {
    AgentManagementOntology myOntology = AgentManagementOntology.instance();
    result.setException(myOntology.getException(AgentManagementOntology.Exception.FAILEDMANACTION));
  }

  /**
    Method to handle <code>refuse</code> replies.
    @param reply The actual ACL message received. It is of
    <code>refuse</code> type and matches the conversation
    template.
  */
  protected void handleRefuse(ACLMessage reply) {
    String content = reply.getContent();
    StringReader text = new StringReader(content);
    result.setException(FIPAException.fromText(text));
  }

  /**
    Method to handle <code>agree</code> replies.
    @param reply The actual ACL message received. It is of
    <code>agree</code> type and matches the conversation
    template.
  */
  protected void handleAgree(ACLMessage reply) {
  }

  /**
    Method to handle <code>failure</code> replies.
    @param reply The actual ACL message received. It is of
    <code>failure</code> type and matches the conversation
    template.
  */
  protected void handleFailure(ACLMessage reply) {
    String content = reply.getContent();
    StringReader text = new StringReader(content);
    result.setException(FIPAException.fromText(text));
  }

  /**
    Method to handle <code>inform</code> replies.
    @param reply The actual ACL message received. It is of
    <code>inform</code> type and matches the conversation
    template.
  */
  protected void handleInform(ACLMessage reply) {

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
	result.setException(fe);
      }
    }
    catch(ParseException pe) {
      pe.printStackTrace();
    }
    catch(TokenMgrError tme) {
      tme.printStackTrace();
    }
    
  }


}
