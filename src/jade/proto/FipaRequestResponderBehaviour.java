/*
  $Log$
  Revision 1.1  1999/03/14 17:54:45  rimassa
  This class models the Responder role within standard 'fipa-request' interaction protocol.

*/

package jade.proto;

import java.util.Hashtable;
import java.util.StringTokenizer;

import jade.core.*;
import jade.lang.acl.*;


/**
  This behaviour receives incoming 'fipa-request' request messages
  and starts specific sub-behaviours according to the kind of
  action requested.
*/
public class FipaRequestResponderBehaviour extends CyclicBehaviour {

  /**
   This interface must be implemented by users to create on demand a
   new instance of a suitable Action according to the action
   name.
  */
  public interface Factory {
    Action create();
  }

  /**
   This class must be extended by users to handle a specific request
   action.
   It provides convenience protected methods to send back specific
   'inform', 'refuse' or 'failure' messages. Besides, it holds request
   and reply ACL messages.
  */
  public static abstract class Action extends Behaviour {

    private Agent myAgent;
    private String myActionName;
    private ACLMessage myRequest;
    private ACLMessage myReply;


    protected Action(Agent a) {
      myAgent = a;
    }

    final void setActionName(String an) {
      myActionName = an;
    }

    final void setRequest(ACLMessage request) { 
	myRequest = request;
    }

    final void setReply(ACLMessage reply) {
      myReply = reply;
    }


    protected final String getActionName() {
      return myActionName;
    }

    protected final ACLMessage getRequest() {
      return myRequest;
    }

    protected final ACLMessage getReply() {
      return myReply;
    }

    // Send a 'not-understood' message back to the requester
    protected void sendNotUnderstood() {
      myReply.setType("not-understood");
      myAgent.send(myReply);
    }

    // Send a 'refuse' message back to the requester
    protected void sendRefuse(String reason) {
      myReply.setType("refuse");
      myReply.setContent("( ( action " + myAgent.getLocalName() + " " + myActionName + " ) " + reason + ")");
      myAgent.send(myReply);
    }

    // Send a 'failure' message back to the requester
    protected void sendFailure(String reason) {
        myReply.setType("failure");
	myReply.setContent("( ( action " + myAgent.getLocalName() + " " + myActionName + " ) " + reason + ")");
	myAgent.send(myReply);
    }

    // Send an 'agree' message back to the requester
    protected void sendAgree() {
      myReply.setType("agree");
      myReply.setContent("( action " + myAgent.getLocalName() + " " + myActionName + " )");
      myAgent.send(myReply);
    }

    // Send an 'inform' message back to the requester
    protected void sendInform() {
      myReply.setType("inform");
      myReply.setContent("( done ( " + myActionName + " ) )");
      myAgent.send(myReply);
    }

  }

  private Agent myAgent;

  private MessageTemplate requestTemplate;
  private Hashtable actions;

  public FipaRequestResponderBehaviour(Agent a) {

    myAgent = a;
    actions = new Hashtable();

    requestTemplate = MessageTemplate.and(
			  MessageTemplate.MatchProtocol("fipa-request"),
			  MessageTemplate.MatchType("request"));

   }

  
  /** 
   * This constructor allows to specify a message pattern to be matched
   * by the received message.
   *
   * @param a the agent that adds the behaviour
   * @param match the MessageTemplate to be matched
   */
  public FipaRequestResponderBehaviour(Agent a, MessageTemplate match) {
    this(a);
    requestTemplate = MessageTemplate.and( requestTemplate, match);
  }

  public void action() {
    ACLMessage msg = myAgent.receive(requestTemplate);
    if(msg != null) {
      ACLMessage reply = new ACLMessage("inform");

      // Write content-independent fields of reply message

      reply.removeAllDests();
      reply.addDest(msg.getSource());
      reply.setSource(msg.getFirstDest());
      reply.setProtocol("fipa-request");
      reply.setOntology(msg.getOntology());
      reply.setLanguage(msg.getLanguage());

      String s = msg.getReplyWith();
      if(s != null)
	reply.setReplyTo(s);
      s = msg.getConversationId();
      if(s != null)
	reply.setConversationId(s);


      // Start reading message content and spawn a suitable
      // Behaviour according to action kind

      StringTokenizer st = new StringTokenizer(msg.getContent()," \t\n\r()",false);

      String token = st.nextToken();
      if(token.equalsIgnoreCase("action")) {
	token = st.nextToken(); // Now 'token' is the agent name
	if(!(token.equalsIgnoreCase(myAgent.getName()) || token.equalsIgnoreCase(myAgent.getLocalName()))) {
	  sendNotUnderstood(reply);
	  return;
	}

	token = st.nextToken(); // Now 'token' is the action name

	Factory action = (Factory)actions.get(token);

	if(action == null) {
	  sendNotUnderstood(reply);
	  return;
	}
	else {
	  Action ab = action.create();
	  ab.setActionName(token);
	  ab.setRequest(msg);
	  ab.setReply(reply);
	  myAgent.addBehaviour(ab);
	}
      }
      else
	sendNotUnderstood(reply);
    }
    else block();

  }


  // These two methods allow to add and remove prototype Behaviours to
  // be associated to action names
  public void registerFactory(String actionName, Factory f) {
    actions.put(actionName, f);
  }

  public void unregisterFactory(String actionName) {
    actions.remove(actionName);
  }

  // Send a 'not-understood' message back to the requester
  protected void sendNotUnderstood(ACLMessage msg) {
    msg.setType("not-understood");
    myAgent.send(msg);
  }

} // End of FipaRequestResponderBehaviour class

