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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.UnrecognisedValue;

import jade.onto.*;
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
  @deprecated the class <code>AchieveREResponder</code> must be used instead.
  
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
       @param request The REQUEST message received.
       @return A new <code>ActionHandler</code> object.
     */
    ActionHandler create(ACLMessage request);
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
      @param request is the REQUEST message that needs to be responded
     */
    protected ActionHandler(Agent ag, ACLMessage request) {
      super(ag);
      if (request != null) {
	myRequest = request;
	myReply = request.createReply();
      }
    }

    protected ACLMessage getReply() {
      return myReply;
    } 

    protected void setReply (ACLMessage reply) {
      myReply =reply;
    }

    /**
       @return The <code>ACLMessage</code> that was received by the
       dispatcher and that this <code>Action</code> has to handle.
    */
    protected final ACLMessage getRequest() {
      return myRequest;
    }


    /**
      Send a <code>reply</code> message back to the
      requester. This method sends the ACL message object returned by
      <code>getReply()</code> method, after changing its message type
      to the passed parameter and after setting the content to the passed
      parameter. 
     
    */
    protected void sendReply(int performative, String content) {
      myReply.setPerformative(performative);
      myReply.setContent(content);
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
    @return the name of the action. If some problem occurs, it throws an Exception.
    @see #registerFactory(String actionName, FipaRequestResponderBehaviour.Factory f)
  */
  protected String getActionName(ACLMessage msg) throws NotUnderstoodException, RefuseException {
    try {
      List l = myAgent.extractMsgContent(msg);
      Action a = (Action)l.get(0);
      // Use the ontology to discover the action name
      Ontology o = myAgent.lookupOntology(msg.getOntology());
      Object obj = a.get_1(); // get the ontological object for the actual action
      String roleName = o.getRoleName(obj.getClass());
      Frame f = o.createFrame(obj, roleName);
      return f.getName();
    } catch(OntologyException oe) {
      oe.printStackTrace();
      throw new UnsupportedFunction();
    } catch(ClassCastException cce) {
      cce.printStackTrace();
      throw new UnrecognisedValue("content");
    } catch(FIPAException e) {
      throw new UnrecognisedValue("content");
    } catch (Exception ee) { // any other exception is catch as UnsupportedFunction
      ee.printStackTrace();
      throw new UnsupportedFunction();
    }
  }

  public void action() {
    ACLMessage msg = myAgent.receive(requestTemplate);
    if(msg != null) {
      ACLMessage reply = msg.createReply();
      try {
	String actionName = getActionName(msg).toUpperCase();
	Factory actionFactory = (Factory)actions.get(actionName);
	if(actionFactory == null) {
	  reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	  reply.setContent("( "+msg.getContent()+ "  "+(new UnsupportedFunction(actionName)).getMessage()+")");
	  myAgent.send(reply);
	  return;
	} else {
	  ActionHandler toDo = actionFactory.create(msg);
	  toDo.setReply(reply);
	  myAgent.addBehaviour(toDo);
	}
      } catch(FIPAException fe) {
	fe.printStackTrace();
	reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	reply.setContent("( "+msg.getContent()+ "  "+fe.getMessage()+")");
	myAgent.send(reply);
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
    In order to implement a case-insensitive match, all action names are 
    converted to uppercase  before registration.
    @param actionName The name of the action the <code>Factory</code>
    creates handlers for.
    @param f The actual <code>Factory</code> object; it will be used
    to create action handlers on demand.
  */
  public void registerFactory(String actionName, Factory f) {
    actions.put(actionName.toUpperCase(), f);
  }


  /**
    Remove a action name - <code>Factory</code> object
    association. This method deregisters a <code>Factory</code> object
    with the dispatcher. It can be used to suspend service for a
    specific action for a while; during that time, the agent will
    answer with <code>not-understood</code> messages to requests for
    the suspended action.
    In order to implement a case-insensitive match, all action names are 
    converted to uppercase  before deregistration.
    @param actionName The name of the action to remove from supported actions.
  */
  public void unregisterFactory(String actionName) {
    actions.remove(actionName.toUpperCase());
  }



} // End of FipaRequestResponderBehaviour class


