
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

import java.util.Hashtable;
import java.util.StringTokenizer;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;


/** 
  This behaviour plays the <em>Responder</em> role in
  <code>fipa-request</code> protocol. This is an abstract class,
  defining an abstract method for each message type expected from a
  <code>fipa-request</code> interaction.

  This behaviour works as a dispatcher, reading the
  <code>:content</code> slot of received <code>request</code> messages
  and spawning different behaviours according to the action
  requested. To be able to handle requests, user defined behaviours
  must extend <code>Action</code> inner class; when a
  <code>request</code> message arrives, an implementation of
  <code>Factory</code> inner interface is retrieved from registered
  action factories, using the <code>action</code> name as key. Then an
  <code>Action</code> is created and spawned with
  <code>Agent.addBehaviour()</code>; this new action object will
  handle the specific request.  Therefore, three steps must be
  accomplished to add a new action to a
  <code>FipaRequestResponderBehaviour</code> object:

  <ol>
  <li> Write a suitable handler for the new action
  extending <code>Action</code> inner class.
  <li> Write an implementation of <code>Factory</code> inner interface
  to create handler behaviours on demand.
  <li> Register your <code>Factory</code> object calling
  <code>registerFactory()</code> and use the name of the action you
  want to handle as key.
  </ol>

  @see jade.proto.FipaRequestInitiatorBehaviour
  
  Javadoc documentation for the file  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class FipaRequestResponderBehaviour extends CyclicBehaviour {

  /**
   This interface must be implemented by users to create on demand a
   new instance of a suitable <code>Action</code> according to the
   action name.
  */
  public interface Factory {
    /**
       Creates a new object, implementing the <code>Action</code>
       interface.
       @return A new <code>Action</code> object.
     */
    Action create();
  }

  /**
    This class must be extended by users to handle a specific request
    action. It provides convenience protected methods to send back
    specific <code>inform</code>, <code>refuse</code> or
    <code>failure</code> messages. Besides, it holds request and reply
    ACL messages.
  */
  public static abstract class Action extends Behaviour {

    private String myActionName;
    private ACLMessage myRequest;
    private ACLMessage myReply;

    /**
      Constructor for <code>Action</code>objects.
      @param a The agent this <code>Action</code> belongs to.
     */
    protected Action(Agent a) {
      super(a);
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

    /**
      Reads action name slot.
      @return The name of this action as will appear in content of
      <code>request</code> ACL messages.
    */
    protected final String getActionName() {
      return myActionName;
    }

    /**
       Reads <code>request</code> message slot.
       @return The <code>ACLMessage</code> that was received by the
       dispatcher and that this <code>Action</code> has to handle.
    */
    protected final ACLMessage getRequest() {
      return myRequest;
    }

    /**
       Reads the reply message slot.
       @return The <code>ACLMessage</code> that will be sent back to
       peer agent. This ACL message can be modified by application
       specific code, but should not change <code>:in-reply-to</code>,
       <code>:conversation-id</code>, <code>:receiver</code> and all
       the message slots that are automatically handled by
       <code>FipaRequestResponderBehaviour</code>. For typical cases,
       just the <code>:content</code> slot should be changed and
       suitable methods should be called to send back the reply
       message with an appropriate message type.
       @see jade.proto.FipaRequestResponderBehaviour.Action#sendNotUnderstood()
       @see jade.proto.FipaRequestResponderBehaviour.Action#sendRefuse(String reason)
       @see jade.proto.FipaRequestResponderBehaviour.Action#sendFailure(String reason)
       @see jade.proto.FipaRequestResponderBehaviour.Action#sendAgree()
       @see jade.proto.FipaRequestResponderBehaviour.Action#sendInform()
    */
    protected final ACLMessage getReply() {
      return myReply;
    }

    /**
      Send a <code>not-understood</code> message back to the
      requester. This method sends the ACL message object returned by
      <code>getReply()</code> method, after changing its message type
      to <code>not-understood</code>.
      @see jade.proto.FipaRequestResponderBehaviour.Action#getReply()
    */
    protected void sendNotUnderstood() {
      myReply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
      myAgent.send(myReply);
    }

    /**
      Send a <code>refuse</code> message back to the requester. This
      method sends the ACL message object returned by
      <code>getReply()</code> method, after changing its message type
      to <code>refuse</code>.
      @param reason A string containing the reason for this
      <code>refuse</code> message. It will be put in
      <code>:content</code> slot of the reply message.
      @see jade.proto.FipaRequestResponderBehaviour.Action#getReply()
    */
    protected void sendRefuse(String reason) {
      myReply.setPerformative(ACLMessage.REFUSE);
      myReply.setContent("( ( action " + myAgent.getLocalName() + " " + myActionName + " ) " + reason + ")");
      myAgent.send(myReply);
    }

    /**
      Send a <code>failure</code> message back to the requester. This
      method sends the ACL message object returned by
      <code>getReply()</code> method, after changing its message type
      to <code>failure</code>.
      @param reason A string containing the reason for this
      <code>failure</code> message. It will be put in
      <code>:content</code> slot of the reply message.
      @see jade.proto.FipaRequestResponderBehaviour.Action#getReply()
    */
    protected void sendFailure(String reason) {
        myReply.setPerformative(ACLMessage.FAILURE);
	myReply.setContent("( ( action " + myAgent.getLocalName() + " " + myActionName + " ) " + reason + ")");
	myAgent.send(myReply);
    }

    /**
      Send a <code>agree</code> message back to the requester. This
      method sends the ACL message object returned by
      <code>getReply()</code> method, after changing its message type
      to <code>agree</code>.
      @see jade.proto.FipaRequestResponderBehaviour.Action#getReply()
    */
    protected void sendAgree() {
      myReply.setPerformative(ACLMessage.AGREE);
      myReply.setContent("( action " + myAgent.getLocalName() + " " + myActionName + " )");
      myAgent.send(myReply);
    }

    /**
      Send a <code>inform</code> message back to the requester. This
      method sends the ACL message object returned by
      <code>getReply()</code> method, after changing its message type
      to <code>inform</code>.
      @see jade.proto.FipaRequestResponderBehaviour.Action#getReply()
    */
    protected void sendInform() {
      myReply.setPerformative(ACLMessage.INFORM);
      myReply.setContent("( done ( " + myActionName + " ) )");
      myAgent.send(myReply);
    }

  } // End of Action class


  private MessageTemplate requestTemplate;
  private Hashtable actions;

  /**
    Public constructor for this behaviour.
    @param a The agent this behaviour belongs to.
   */
  public FipaRequestResponderBehaviour(Agent a) {

    myAgent = a;
    actions = new Hashtable();

    requestTemplate = MessageTemplate.and(
			  MessageTemplate.MatchProtocol("fipa-request"),
			  MessageTemplate.MatchType("request"));

   }

  
  /** 
    This constructor allows to specify a message pattern to be matched
    by the received message.
    @param a the agent that adds the behaviour
    @param match the <code>MessageTemplate</code> to be matched
  */
  public FipaRequestResponderBehaviour(Agent a, MessageTemplate match) {
    this(a);
    requestTemplate = MessageTemplate.and( requestTemplate, match);
  }

  public void action() {
    ACLMessage msg = myAgent.receive(requestTemplate);
    if(msg != null) {
      ACLMessage reply = new ACLMessage(ACLMessage.INFORM);

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

      String content = msg.getContent();
      if(content == null) {
	sendNotUnderstood(reply);
	return;
      }

      StringTokenizer st = new StringTokenizer(content," \t\n\r()",false);

      String token = st.nextToken();
      if(token.equalsIgnoreCase("action")) {
	token = st.nextToken(); // Now 'token' is the agent name

	// Commented out, since it is SL-specific...
	/*
	  if(!(token.equalsIgnoreCase(myAgent.getName()) || token.equalsIgnoreCase(myAgent.getLocalName()))) {
	  sendNotUnderstood(reply);
	  return;
	}
	*/ 

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


  /**
    Associate a <code>Factory</code> object with an action name. This
    method registers an object to be used to create behaviours to
    handle the specified action when some <code>request</code> for it
    is received.
    @param actionName The name of the action the <code>Factory</code>
    creates handlers for.
    @param f The actual <code>Factory</code> object; it will be used
    to create action handlers on demand.
  */
  public void registerFactory(String actionName, Factory f) {
    actions.put(actionName, f);
  }

  /**
    Remove a action name - <code>Factory</code> object
    association. This method deregisters a <code>Factory</code> object
    with the dispatcher. It can be used to suspend service for a
    specific action for a while; during that time, the agent will
    answer with <code>not-understood</code> messages to requests for
    the suspended action.
    @param actionName The name of the action to remove from supported actions.
  */
  public void unregisterFactory(String actionName) {
    actions.remove(actionName);
  }

  // Send a 'not-understood' message back to the requester

  void sendNotUnderstood(ACLMessage msg) {
    msg.setPerformative(ACLMessage.NOT_UNDERSTOOD);
    myAgent.send(msg);
  }

} // End of FipaRequestResponderBehaviour class

