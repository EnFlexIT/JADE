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
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Iterator;

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
 * @author Fabio Bellifemine - CSELT
 * @version $Date$ $Revision$
*/
public abstract class FipaQueryResponderBehaviour extends SimpleBehaviour {

/**
@serial
*/
private MessageTemplate template; 

/**
@serial
*/
private ACLMessage msg,reply;

/**
@serial
*/
private int state = 0;
  
/**
* This variable must be set to <code>true</code> in order to finish
* the behaviour and remove it from the agent's behaviours.
* @serial
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
      if (!  (ACLMessage.QUERY_IF ==msg.getPerformative()|| 
	      ACLMessage.QUERY_REF == msg.getPerformative())) {
	if (! (ACLMessage.NOT_UNDERSTOOD == msg.getPerformative()))
	  SendNotUnderstood(msg, "((unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+"))");
	state = 0;
      } else { 
	reply = handleQueryMessage(msg);
	state++;
      }
      break;
    }
    case 2: {
      if (! ( ACLMessage.INFORM == reply.getPerformative() ||
	      ACLMessage.NOT_UNDERSTOOD == reply.getPerformative() ||
	      ACLMessage.FAILURE == reply.getPerformative()||
	      ACLMessage.REFUSE == reply.getPerformative()))
	SendFailure(msg,"((unexpected-act "+ACLMessage.getPerformative(reply.getPerformative())+"))");
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
  ACLMessage reply=msg.createReply();
  String content = "(" + msg.toString() + " \""+reason+"\")"; 
  reply.setContent(content);
  reply.setPerformative(ACLMessage.FAILURE);
  myAgent.send(reply);
}

  /**
   * @param msg is the QUERY received
   * @param rep is the message to be sent
   */
private void SendReply(ACLMessage msg, ACLMessage rep) {
	// The :receiver, :sender, :protocol and :conversation-id slots are set 
	// by default regardless of how they are set in the rep message.
  ACLMessage reply=msg.createReply();
  
  // The :performative, :content, :language, :ontology, :encoding and 
  // :reply-by slots are maintained as in the rep message.
  reply.setPerformative(rep.getPerformative());
  reply.setContent(rep.getContent());
  reply.setLanguage(rep.getLanguage());
  reply.setOntology(rep.getOntology());
  reply.setEncoding(rep.getEncoding());
  reply.setReplyByDate(rep.getReplyByDate());
  
  // The :reply-to, and :reply-with slots are set by default unless
  // they are explicitly set in the rep message.
  Iterator i = rep.getAllReplyTo();
  if (i.hasNext()){ // If some replyTo is set in rep --> preserve them
  	reply.clearAllReplyTo();
  	while (i.hasNext())
  		reply.addReplyTo((AID) i.next());
  }
  if (rep.getReplyWith() != null)
  	reply.setReplyWith(rep.getReplyWith());
  
  myAgent.send(reply);
}

private void SendNotUnderstood(ACLMessage msg, String reason) {
  ACLMessage reply=msg.createReply();
  String content = "(" + msg.toString() + " \""+reason+"\")"; 
  reply.setContent(content);
  reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  myAgent.send(reply);
}
  
}



