package jade.proto;

import jade.core.SimpleBehaviour;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This abstract class implements the Fipa-Query interaction protocol.
 * The behaviour is cyclic so it remains active for ever.
 * Its usage is the following:
 * A class must be instantiated that extends this one. This new
 * class must implement the method <code>processQuery</code>. 
 * The instantiated class
 * must then be added to the Agent object by using the method 
 * <code>addBehaviour</code>
 */
public abstract class FipaQueryResponderBehaviour extends SimpleBehaviour {

private MessageTemplate template; 
private ACLMessage msg,reply;
private int state = 0;
  /**
   * This variable must be set to <code>true</code> in order to
   * finish the behaviour and remove it from the agent's behaviours.
   */
public boolean finished=false;

  /** 
   * Object Constructor.
   * @param a is the Agent that runs the behaviour
   */
public FipaQueryResponderBehaviour(Agent a){
  super(a);
  template = MessageTemplate.MatchProtocol("FIPA-Query");
}

  /**
   * Object Constructor.
   * @param a is the Agent that runs the behaviour
   * @param mt is the MessageTemplate to filter the messages to be consumed
   * by this behaviour
   */
public  FipaQueryResponderBehaviour(Agent a, MessageTemplate mt){
  this(a);
  template = MessageTemplate.and(template,mt);
}
  
  /**
   * This method allows to reset the behaviour.
   */
public void reset() {
  finished = false;
  state = 0;
}

public boolean done() {
  return finished;
}

  /**
   * This final method actually implements the procotol. It cannot be
   * re-implemented by sub-classes. 
   */
  final public void action() {
    switch (state) {
    case 0: {
      msg = myAgent.receive(template);
      if (msg == null) {
	block();
	return;
      }
      state ++;
      break;
    }
    case 1: {
      if (!  (msg.getType().equalsIgnoreCase("query-if") || 
	      msg.getType().equalsIgnoreCase("query-ref"))) {
	if (! msg.getType().equalsIgnoreCase("not-understood"))
	  SendNotUnderstood(msg, "unexpected Communicative Act");
	state = 0;
      } else { 
	reply = processQuery(msg.getContent());
	state++;
      }
      break;
    }
    case 2: {
      if (! ( reply.getType().equalsIgnoreCase("inform") ||
	      reply.getType().equalsIgnoreCase("not-understood") ||
	      reply.getType().equalsIgnoreCase("failure") ||
	      reply.getType().equalsIgnoreCase("refuse")))
	SendFailure(msg,"ill-formed return in processQuery");
      else 
	SendReply(msg,reply);
      state=0;
      break;
    }
    } // end of swith (state)
  }
    /**
     * This abstract method must be implemented by all sub-classes.
     * The method is called whenever a new query-if or query-ref message
     * arrives.
     * @param content is the String with the received message content
     * @return the method must return the ACLMessage to be sent as a reply. 
     * In particular, the ACLMessage must have valid type (i.e. it must be
     * "failure" or "inform" or "refuse" or "not-understood") and valid
     * message content.
     */
public abstract ACLMessage processQuery(String content);

private void SendFailure(ACLMessage msg, String reason) {
  String content = "(" + msg.toString() + " \""+reason+"\")"; 
  msg.setContent(content);
  msg.setType("failure");
  msg.removeAllDests();
  msg.addDest(msg.getSource());
  msg.setSource(myAgent.getName());
  msg.setReplyTo(msg.getReplyWith());
  msg.setConversationId(msg.getConversationId());
  myAgent.send(msg);
}

private void SendReply(ACLMessage msg, ACLMessage reply) {
  reply.removeAllDests();
  reply.addDest(msg.getSource());
  reply.setSource(myAgent.getName());
  reply.setReplyTo(msg.getReplyWith());
  reply.setConversationId(msg.getConversationId());
  reply.setProtocol("FIPA-Query");
  if (reply.getLanguage() == null)
    reply.setLanguage(msg.getLanguage());
  if (reply.getOntology() == null)
    reply.setOntology(msg.getOntology());
  myAgent.send(reply);
}

private void SendNotUnderstood(ACLMessage msg, String reason) {
  String content = "(" + msg.toString() + " \""+reason+"\")"; 
  msg.setContent(content);
  msg.setType("not-understood");
  msg.removeAllDests();
  msg.addDest(msg.getSource());
  msg.setSource(myAgent.getName());
  msg.setReplyTo(msg.getReplyWith());
  msg.setConversationId(msg.getConversationId());
  myAgent.send(msg);
}
  
}
