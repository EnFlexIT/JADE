/*
  $Id$
*/

package jade.domain;

import java.util.Hashtable;
import java.util.StringTokenizer;

import jade.core.*;
import jade.lang.acl.*;


// This behaviour receives incoming 'fipa-request' request messages
// and starts specific sub-behaviours according to the kind of
// action requested.
class FipaRequestServerBehaviour extends CyclicBehaviour {

  private Agent myAgent;

  private MessageTemplate requestTemplate;
  private Hashtable actions;

  public FipaRequestServerBehaviour(Agent a) {

    myAgent = a;
    actions = new Hashtable();

    MessageTemplate mt1 = 
      MessageTemplate.and(MessageTemplate.MatchProtocol("fipa-request"),
			  MessageTemplate.MatchType("request"));
    MessageTemplate mt2 = 
      MessageTemplate.and(MessageTemplate.MatchLanguage("SL0"),
			  MessageTemplate.MatchOntology("fipa-agent-management"));
    requestTemplate = MessageTemplate.and(mt1, mt2);
  }

  public void action() {
    ACLMessage msg = myAgent.receive(requestTemplate);
    if(msg != null) {

      ACLMessage reply = new ACLMessage();

      // Write content-independent fields of reply message

      reply.setDest(msg.getSource());
      reply.setSource(msg.getDest());
      reply.setProtocol("fipa-request");
      reply.setOntology("fipa-agent-management");
      reply.setLanguage("SL0");

      String s = msg.getReplyWith();
      if(s != null)
	reply.setReplyTo(s);
      s =msg.getConversationId();
      if(s != null)
	reply.setConversationId(s);


      // Start reading message content and spawn a suitable
      // Behaviour according to action kind

      StringTokenizer st = new StringTokenizer(msg.getContent()," \t\n\r()",false);

      String token = st.nextToken();
      if(token.equalsIgnoreCase("action")) {
	token = st.nextToken(); // Now 'token' is the name of the AMS agent
	token = st.nextToken(); // Now 'token' is the action name

	BehaviourPrototype action = (BehaviourPrototype)actions.get(token);
	if(action == null) {
	  sendNotUnderstood(reply);
	  return;
	}
	else
	  myAgent.addBehaviour(action.instance(reply, st));
      }
      else
	sendNotUnderstood(reply);
    }
    else block();

  }


  // These two methods allow to add and remove prototype Behaviours to
  // be associated to action names
  public void registerPrototype(String actionName, BehaviourPrototype bp) {
    actions.put(actionName, bp);
  }

  public void unregisterPrototype(String actionName) {
    actions.remove(actionName);
  }

  // Send a 'not-understood' message back to the requester
  private void sendNotUnderstood(ACLMessage msg) {
    msg.setType("not-understood");
    msg.setContent("");
    myAgent.send(msg);
  }

} // End of FipaRequestServerBehaviour class

