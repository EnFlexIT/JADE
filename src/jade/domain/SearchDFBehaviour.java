/*
  $Log$
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

public class SearchDFBehaviour extends FipaRequestClientBehaviour {

  private static final MessageTemplate mt = 
    MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			MessageTemplate.MatchOntology("fipa-agent-management"));

  private AgentManagementOntology.DFSearchResult result = null;

  public SearchDFBehaviour(Agent a, ACLMessage msg, AgentManagementOntology.DFAgentDescriptor dfd,
			   Vector constraints, AgentManagementOntology.DFSearchResult r) throws FIPAException {
    super(a, msg, mt);

    result = r;

    String dfName = msg.getDest();
    String sender = msg.getSource();
    AgentManagementOntology o = AgentManagementOntology.instance();
    if((dfName == null)||(sender == null))
      throw o.getException(AgentManagementOntology.Exception.UNRECOGNIZEDVALUE);
    if(!sender.equalsIgnoreCase(a.getName()))
      throw o.getException(AgentManagementOntology.Exception.UNAUTHORISED);

    msg.setLanguage("SL0");
    msg.setOntology("fipa-agent-management");

    AgentManagementOntology.DFSearchAction dfsa = new AgentManagementOntology.DFSearchAction();
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
    msg.setContent("( action " + dfName + textOut + " )");

  }

  protected void handleNotUnderstood(ACLMessage reply) {
    System.out.println("'not-understood' received.");
    AgentManagementOntology myOntology = AgentManagementOntology.instance();
    result.setException(myOntology.getException(AgentManagementOntology.Exception.FAILEDMANACTION));
  }

  protected void handleRefuse(ACLMessage reply) {
    System.out.println("'refuse' received.");
    String content = reply.getContent();
    StringReader text = new StringReader(content);
    result.setException(FIPAException.fromText(text));
  }

  protected void handleAgree(ACLMessage reply) {
    System.out.println("'agree' received.");
  }

  protected void handleFailure(ACLMessage reply) {
    System.out.println("'failure' received.");
    String content = reply.getContent();
    StringReader text = new StringReader(content);
    result.setException(FIPAException.fromText(text));
  }

  protected void handleInform(ACLMessage reply) {
    System.out.println("'inform' received.");

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
