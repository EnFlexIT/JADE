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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAException;

import jade.onto.*
import jade.onto.basic.Action;

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
       @param a An ontological object representing the action to perform.
       @return A new <code>ActionHandler</code> object.
     */
    ActionHandler create();
  }

  /**
    This class must be extended by users to handle a specific request
    action. It provides convenience protected methods to send back
    specific <code>inform</code>, <code>refuse</code> or
    <code>failure</code> messages. Besides, it holds request and reply
    ACL messages.
  */
  public static abstract class ActionHandler extends Behaviour {

    /**
    @serial
    */
    private ACLMessage myRequest;

    /**
    @serial
    */
    private ACLMessage myReply;

    /**
      Constructor for <code>ActionHandler</code>objects.
      @param a The agent this <code>ActionHandler</code> belongs to.
     */
    protected ActionHandler(Agent ag) {
      super(ag);
    }

    final void setRequest(ACLMessage request) { 
	myRequest = request;
    }

    final void setReply(ACLMessage reply) {
      myReply = reply;
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
      myReply.setContent("STUB");
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
	myReply.setContent("STUB");
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
      myReply.setContent("STUB");
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
      myReply.setContent("STUB");
      myAgent.send(myReply);
    }

  } // End of ActionHandler class

  /**
  @serial
  */
  private MessageTemplate requestTemplate;
  /**
  @serial
  */
  private Map actions;

  /**
    Public constructor for this behaviour.
    @param a The agent this behaviour belongs to.
   */
  public FipaRequestResponderBehaviour(Agent a) {

    myAgent = a;
    actions = new HashMap();

    requestTemplate = MessageTemplate.and(
			  MessageTemplate.MatchProtocol("fipa-request"),
			  MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

   }

  
  /** 
    This constructor allows to specify a message pattern to be matched
    by the received message.
    @param a the agent that adds the behaviour
    @param match the <code>MessageTemplate</code> to be matched
  */
  public FipaRequestResponderBehaviour(Agent a, MessageTemplate match) {
    this(a);
    requestTemplate = MessageTemplate.and(requestTemplate, match);
  }

  public void action() {
    ACLMessage msg = myAgent.receive(requestTemplate);
    if(msg != null) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.INFORM);
      reply.setSender(myAgent.getAID());

      try {
	List l = myAgent.extractContent(msg);
	Action a = (Action)l.get(0);

	// Use the ontology to discover the action name
	Ontology o = myAgent.lookupOntology(msg.getOntology());

	String actionName = getActionName(a, o);

	Factory actionFactory = (Factory)actions.get(actionName);

	if(actionFactory == null) {
	  sendNotUnderstood(reply);
	  return;
	}
	else {
	  ActionHandler toDo = actionFactory.create();
	  toDo.setRequest(msg);
	  toDo.setReply(reply);
	  myAgent.addBehaviour(toDo);
	}
      }
      catch(FIPAException fe) {
	fe.printStackTrace();
	sendNotUnderstood(reply);
	return;
      }
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
    This method is used to get the right behaviour from the <code>Factory</code>.
    It must return the name of the action that is then used to look-up 
    in the factory with the list of registered actions.
    A default implementation is provided that is case-sensitive.
    So, the case of the returned <code>String</code> and that of the 
    <code>String</code> passed as a parameter to <code>registerFactory</code> 
    must be the same.
    @param a An <code>Action</code> ontological object, that holds the
    content of a received <em>request</em> ACL message.
    @return the name of the action. If some problem occurs, it returns
    a null String.
    @see #registerFactory(String actionName, FipaRequestResponderBehaviour.Factory f)
  */
  protected String getActionName(Action a, Ontology o) {
    try {
      Object obj = a.get_1(); // get the ontological object for the actual action

      String roleName = o.getRoleName(obj.getClass());
      Frame f = o.createFrame(obj, roleName);
      return f.getName();

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
      return null; // So that the action lookup will fail and 'not-understood' will be sent back...
    }
    catch(ClassCastException cce) {
      cce.printStackTrace();
      return null; // So that the action lookup will fail and 'not-understood' will be sent back...
    }
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
