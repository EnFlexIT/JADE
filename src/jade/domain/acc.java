/*
  $Log$
  Revision 1.12  1999/06/04 07:49:47  rimassa
  Changed class code to avoid any relation with jade.core package.

  Revision 1.11  1999/04/06 00:09:54  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

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

/**
  Standard <em>Agent Communication Channel</em> agent. This class
  implements <em><b>FIPA</b></em> <em>ACC</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
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


  private AgentManagementOntology myOntology;
  private FipaRequestResponderBehaviour dispatcher;

  /**
     This constructor creates a new <em>ACC</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>ACC</em> agent can be created
     beyond the default one.
  */
  public acc() {

    myOntology = AgentManagementOntology.instance();

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));

    dispatcher = new FipaRequestResponderBehaviour(this, mt);

    // Associate each ACC action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message
    dispatcher.registerFactory(AgentManagementOntology.ACCAction.FORWARD, new ACCBehaviour());

  }

  /**
   This method starts the <em>ACC</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    addBehaviour(dispatcher);
  }

}
