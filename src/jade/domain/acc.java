/*
  $Log$
  Revision 1.6  1998/10/23 21:43:26  Giovanni
  Activated some instrumentation code. Now both ParseException and
  TokenMgrError are dumped when catched.

  Revision 1.5  1998/10/04 18:01:35  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

import java.io.StringReader;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class acc extends Agent {

  private class ACCBehaviour extends OneShotBehaviour implements BehaviourPrototype {

    private AgentManagementOntology.ACCAction myAction;
    private final String myActionName = AgentManagementOntology.ACCAction.FORWARD;
    private ACLMessage myRequest;
    private ACLMessage myReply;

    protected ACCBehaviour() {
      super(acc.this);
      myRequest = null;
      myReply = null;
    }

    protected ACCBehaviour(ACLMessage request, ACLMessage reply) {
      super(acc.this);
      myRequest = request;
      myReply = reply;
    }

    public Behaviour instance(ACLMessage request, ACLMessage reply) {
      return new ACCBehaviour(request, reply);
    }

    public void action() {

      try {
	String content = myRequest.getContent();
	// Remove 'action acc' from content string
	content = content.substring(content.indexOf("acc") + 3); // FIXME: ACC could crash for a bad msg

	// Obtain an ACC action from message content
	try {
	  myAction = AgentManagementOntology.ACCAction.fromText(new StringReader(content));
	}
	catch(ParseException pe) {
	  pe.printStackTrace();
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
	}
	catch(TokenMgrError tme) {
	  tme.printStackTrace();
	  throw myOntology.getException(AgentManagementOntology.Exception.UNRECOGNIZEDATTR);
	}

	ACLMessage toForward = myAction.getArg();

	// Make sure destination agent is registered with platform AMS
	String destName = toForward.getDest();
	// FIXME: Add existence check ...

	// Forward message
	send(toForward);

	// Acknowledge caller
	sendAgree(myReply);
	sendInform(myReply);
      }
      catch(FIPAException fe) {
	sendRefuse(myReply, fe.getMessage());
      }

    }


    // Send a 'refuse' message back to the requester
    protected void sendRefuse(ACLMessage msg, String reason) {
      msg.setType("refuse");
      msg.setContent("( action acc " + myActionName + " ) " + reason);
      send(msg);
    }
    
    // Send a 'failure' message back to the requester
    protected void sendFailure(ACLMessage msg, String reason) {
    msg.setType("failure");
    msg.setContent("( action acc " + myActionName + " ) " + reason);
    send(msg);
    }
    
    // Send an 'agree' message back to the requester
    protected void sendAgree(ACLMessage msg) {
      msg.setType("agree");
      msg.setContent("( action acc " + myActionName + " )");
      send(msg);
    }
    
    // Send an 'inform' message back to the requester
    protected void sendInform(ACLMessage msg) {
      msg.setType("inform");
      msg.setContent("( done ( " + myActionName + " ) )");
      send(msg);
    }


  } // End of ACCBehaviour class


  private AgentPlatformImpl myPlatform;
  private AgentManagementOntology myOntology;
  private FipaRequestServerBehaviour dispatcher;

  public acc(AgentPlatformImpl ap) {

    myPlatform = ap;
    myOntology = AgentManagementOntology.instance();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));

    dispatcher = new FipaRequestServerBehaviour(this, mt);

    // Associate each ACC action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message
    dispatcher.registerPrototype(AgentManagementOntology.ACCAction.FORWARD, new ACCBehaviour());

  }


  protected void setup() {
    addBehaviour(dispatcher);
  }

}
