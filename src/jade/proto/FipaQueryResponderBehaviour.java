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


package jade.proto;

import jade.core.behaviours.SimpleBehaviour;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Behaviour class for <code>fipa-query</code> <em>Responder</em>
 * role.  This abstract class implements the <code>fipa-query</code> interaction
 * protocol.  The behaviour is cyclic so it remains active forever.
 * Its usage is the following: A class must be instantiated that
 * extends this one. This new class must implement the method
 * <code>handleQueryMessage()</code>.  The instantiated class must then be
 * added to the <code>Agent</code> object by using the method
 * <code>Agent.addBehaviour()</code>
 *
 * Javadoc documentation for the file
 * @author Fabio Bellifemine - CSELT
 * @version $Date$ $Revision$
*/
public abstract class FipaQueryResponderBehaviour extends SimpleBehaviour {

private MessageTemplate template; 
private ACLMessage msg,reply;
private int state = 0;
  /**
   * This variable must be set to <code>true</code> in order to finish
   * the behaviour and remove it from the agent's behaviours.
   */
public boolean finished=false;

  /** 
   * Constructor.
   * @param a is the <code>Agent</code> that runs the behaviour.
   */
public FipaQueryResponderBehaviour(Agent a){
  super(a);
  template = MessageTemplate.MatchProtocol("FIPA-Query");
}

  /**
   * Constructor.
   * @param a is the <code>Agent</code> that runs the behaviour.
   * @param mt is the <code>MessageTemplate</code> to filter the messages to be consumed
   * by this behaviour.
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

  /**
   * This method checks whether this behaviour has finished or not.
   * @return <code>true</code> if this behaviour has completed its
   * task, <code>false</code> otherwise.
   */
public boolean done() {
  return finished;
}

  /**
   * This <code>final</code> method actually implements the procotol. It cannot be
   * overridden by subclasses. 
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
	reply = handleQueryMessage(msg);
	state++;
      }
      break;
    }
    case 2: {
      if (! ( reply.getType().equalsIgnoreCase("inform") ||
	      reply.getType().equalsIgnoreCase("not-understood") ||
	      reply.getType().equalsIgnoreCase("failure") ||
	      reply.getType().equalsIgnoreCase("refuse")))
	SendFailure(msg,"ill-formed return in handleQueryMessage");
      else 
	SendReply(msg,reply);
      state=0;
      break;
    }
    } // end of swith (state)
  }
    /**
     * This abstract method must be implemented by all sub-classes.
     * The method is called whenever a new <code>query-if</code> or
     * <code>query-ref</code> message arrives.
     * @param msg is the received message.
     * @return the method must return the <code>ACLMessage</code> to
     * be sent as a reply.  In particular, the <code>ACLMessage</code>
     * must have valid type (i.e. it must be <code>failure</code> or
     * <code>inform</code> or <code>refuse</code> or
     * <code>not-understood</code>) and valid message content.
     */
public abstract ACLMessage handleQueryMessage(ACLMessage msg);

private void SendFailure(ACLMessage msg, String reason) {
  String content = "(" + msg.toString() + " \""+reason+"\")"; 
  msg.setContent(content);
  msg.setPerformative(ACLMessage.FAILURE);
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
  if (reply.getLanguage().length()<1)
    reply.setLanguage(msg.getLanguage());
  if (reply.getOntology().length()<1)
    reply.setOntology(msg.getOntology());
  myAgent.send(reply);
}

private void SendNotUnderstood(ACLMessage msg, String reason) {
  String content = "(" + msg.toString() + " \""+reason+"\")"; 
  msg.setContent(content);
  msg.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  msg.removeAllDests();
  msg.addDest(msg.getSource());
  msg.setSource(myAgent.getName());
  msg.setReplyTo(msg.getReplyWith());
  msg.setConversationId(msg.getConversationId());
  myAgent.send(msg);
}
  
}
