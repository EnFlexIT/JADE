/*
  $Log$
  Revision 1.6  1999/04/06 00:09:53  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.5  1999/03/14 17:47:21  rimassa
  Fixed a bug: a getName() call should have been getLocalName() instead.

  Revision 1.4  1999/03/10 06:56:20  rimassa
  Changed superclass name from 'FipaRequestClientBehaviour' to
  'FipaRequestInitiatorBehaviour'.

  Revision 1.3  1999/03/09 13:05:06  rimassa
  A minor change to avoid using deprecated 'ACLMessage.getDest()' calls.

  Revision 1.2  1998/12/08 00:07:17  rimassa
  Removed handcrafted content generation for request message; now using
  updated DFSearchBehaviour.toText() method.
  Removed debugging printouts.

  Revision 1.1  1998/12/01 23:45:51  rimassa
  A Behaviour to search a DF for information. Will be used by a DF itself to
  perform recursive searches concurrently when a :df-depth greater than 1 is
  given.

*/

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
