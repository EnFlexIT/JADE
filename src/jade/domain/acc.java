/*
  $Log$
  Revision 1.10  1999/03/14 17:49:33  rimassa
  Changed acc class to take advantage of new
  FipaRequestResponderBehaviour class.

  Revision 1.9  1999/02/16 08:09:34  rimassa
  Removed a fixed FIXME.

  Revision 1.8  1999/02/03 10:54:28  rimassa
  Added some missing parentheses to ACC reply messages.

  Revision 1.7  1998/12/08 00:08:41  rimassa
  Removed handmade content message parsing. Now updated
  ACCAction.fromText() method is used.

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

import jade.proto.FipaRequestResponderBehaviour;

public class acc extends Agent {

  private class ACCBehaviour
    extends FipaRequestResponderBehaviour.Action
    implements FipaRequestResponderBehaviour.Factory {

    private AgentManagementOntology.ACCAction myAction;

    protected ACCBehaviour() {
      super(acc.this);
    }

    public FipaRequestResponderBehaviour.Action create() {
      return new ACCBehaviour();
    }

    public void action() {

      try {
	ACLMessage msg = getRequest();
	String content = msg.getContent();

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

	// Forward message
	send(toForward);

	// Acknowledge caller
	sendAgree();
	sendInform();
      }
      catch(FIPAException fe) {
	sendRefuse(fe.getMessage());
      }

    }

    public boolean done() {
      return true;
    }

    public void reset() {
    }

  } // End of ACCBehaviour class


  private AgentPlatformImpl myPlatform;
  private AgentManagementOntology myOntology;
  private FipaRequestResponderBehaviour dispatcher;

  public acc(AgentPlatformImpl ap) {

    myPlatform = ap;
    myOntology = AgentManagementOntology.instance();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));

    dispatcher = new FipaRequestResponderBehaviour(this, mt);

    // Associate each ACC action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message
    dispatcher.registerFactory(AgentManagementOntology.ACCAction.FORWARD, new ACCBehaviour());

  }


  protected void setup() {
    addBehaviour(dispatcher);
  }

}
