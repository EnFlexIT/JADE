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

import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.AID;

import java.util.*;

import java.io.*;

/**
* Behaviour class for <code>fipa-contract-net</code>
* <em>Initiator</em> role.  This abstract behaviour implements the
* <code>fipa-contract-net</code> interaction protocol from the point
* of view of the agent initiating the protocol, that is the agent that
* sends the <code>cfp</code> message (<em>Call for Proposal</em>) to a
* set of agents.  In order to use correctly this behaviour, the
* programmer should do the following: <ul>

* <li> Implement a class that extends
* <code>FipaContractNetInitiatorBehaviour</code>.  This class must
* implement four methods that are called by
* <code>FipaContractNetInitiatorBehaviour</code>:
* <ul>

* <li> <code>public Vector handleProposeMessages(Vector proposals)</code>
* to evaluate all the received proposals and to return a vector of
* <code>ACLMessage</code> objects to be sent in response to the
* proposals (return <code>null</code> to terminate the protocol).

* <li> <code>public void handleOtherMessages(ACLMessage msg)</code>
* to handle all received messages different from <code>propose</code>

* <li> <code>public Vector handleFinalMessages(Vector
* messages)</code> to evaluate the messages received in the last state
* of the protocol, that is <code>inform Done</code> and
* <code>failure</code> messages, and to return a <code>Vector</code>
* of <code>ACLMessages</code> to be sent before terminating the
* behaviour (return <code>null</code> to terminate the protocol).

* <li> <code> public String createCfpContent(String cfpContent, AID receiver)</code> 
* to return the cfp content for each receiver.
* </ul>
* <li> Create a new instance of this class and add it to the agent
* with <code>Agent.addBehaviour()</code> method.
* </ul>
* <p>
* This behaviour can be hot reset, i.e. it is sensitive to the call of
* the <code>reset</code> method and it is able to react in every state of the
* protocol.
* <p>
* For this reason, this class can also be used to implement the 
* <b>FIPA-ITERATED-CONTRACT-NET PROTOCOL</b>. In such a case, the following
* should be done:
* <ul>
* <li> the protocol field of the <code>cfp</code> parameter, 
* that has to be passed both in the 
* constructor of this class and in the <code>reset</code> method,
* should be set to the constant string "Fipa-Iterated-Contract-Net" 
* (actually this value increase the level of compatibility to the FIPA
* specifications but it is not strictly necessary for the execution of
* the behaviour);
* <li> implement the iteration within the method 
* <code>handleProposeMessages</code>
* by calling the method <code>reset(newcfp,newagentlist)</code> 
* (eventually updating the cfp
* message and the agent group);  the vector of ACLMessages returned by the 
* <code>handleProposeMessages</code> method are still sent by this class, 
* so that the
* previous iteration can be appropriately closed with the right number
* of reject-proposal messages.
* </ul>
* <p>
* @see jade.proto.FipaContractNetResponderBehaviour
*
* 
* @author Fabio Bellifemine - CSELT
* @version $Date$ $Revision$
*/
public abstract class FipaContractNetInitiatorBehaviour extends SimpleBehaviour {
    
  /** 
   * This is the cfpMsg sent in the first state of the protocol
   * @serial
   **/
  protected ACLMessage cfpMsg; 
  /**
  @serial
  */
  private int state = 0;  // state of the protocol
  /**
  @serial
  */
  private long timeout, blockTime, endingTime;
  /**
  @serial
  */
  private MessageTemplate template;
  /**
  @serial
  */
  private Vector msgProposals = new Vector(); // vector of ACLMessage with the proposals
  /**
  @serial
  */
  private Vector msgAcceptReject = new Vector(); // vector with the ACLMessages to send (accept/reject proposal)
  /**
  @serial
  */
  private Vector msgFinal = new Vector(); // vector with the ACLMessages received after accept/reject-proposal
  /**
  @serial
  */
  private Vector msgFinalAnswers = new Vector(); // vector with the ACLMessages to send at the end of the protocol
  
  /**
  @serial
  */
  private List proposerAgents;
  
  /**
  @serial
  */
  private List waitedAgents;

  /**
   * This boolean should be set to true in order to finish abnormally
   * the protocol.
   * @serial
   */
  protected boolean finished=false; // true when done()

  /**
  @serial
  */
  private boolean hasBeenReset=false; // set to true in the method reset()

  /**
   * constructor of the behaviour.
   * @param a is the current agent. The protected variable 
   * <code>Agent myAgent</code> contains then the pointer to the agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and use the methods of the extended class. 
   * For instance 
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments() </code>
   * @param msg is the Call for Proposal message to be sent. If not set 
   * already in the parameter, the protocol is set to the value
   * <code>FIPA-Contract-Net</code>
   * @param responders is the group of agents to which the cfp must be sent 
   * sintactically it is a <code>List of AID</code>
   */
    public FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg, List responders) {
      super(a);
      cfpMsg = (ACLMessage)msg.clone();
      proposerAgents = responders;
      state=0;
      hasBeenReset = false;
    }


  /**
   * constructor of the behaviour. In this case the group of responder agents
   * is extracted directly 
   * by the receiver field of the ACLMessage that has been passed as
   * parameter.
   * @see #FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg, List responders)
   */
    public FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg) {
      this(a,msg,null);
      proposerAgents = new ArrayList();
      Iterator i=msg.getAllReceiver();
      while (i.hasNext())
	proposerAgents.add(i.next());
    }
    


  /**
   * Action method of the behaviour. This method cannot be overriden
   * by subclasses because it implements the actual FipaContractNet
   * protocol
   */
  final public void action() {
    switch (state) {
    case 0: {
      // The following variables must be rightly set before entering in this 
      // state: cfpMsg, proposerAgents, myAgent, state, hasBeenReset
      // At the end of the state the following variables are set:
      // hasBeenReset, timeout, endingTime, template, waitedAgents, state

      if (hasBeenReset)
	hasBeenReset = false;

      // In this state the cfp message is sent to all the receivers
      // the content of the message is tailored to each single receiver
      // the endingTime to receive all the proposals is set
      // the list of agents from which to wait a proposal is set
      // and finally it goes to state 1

      cfpMsg.setPerformative(ACLMessage.CFP);
      if (cfpMsg.getProtocol().length() < 1)
	cfpMsg.setProtocol("FIPA-Contract-Net");
      cfpMsg.setSender(myAgent.getAID());
      if (cfpMsg.getReplyWith().length() < 1)
	cfpMsg.setReplyWith("ContractNet"+(new Date()).getTime());
      if (cfpMsg.getConversationId().length() < 1)
	cfpMsg.setConversationId("ContractNet"+(new Date()).getTime());
      timeout = cfpMsg.getReplyByDate().getTime()-(new Date()).getTime();
      if (timeout <= 1000) timeout = -1; // infinite timeout
      endingTime = System.currentTimeMillis() + timeout;

      //replace the content with the actual actor name 
      // that is 1 actor for each message.
      AID actor;
      String oldcontent = cfpMsg.getContent();
      Iterator e = proposerAgents.iterator();
      while (e.hasNext()) {
	actor = (AID)e.next();
	cfpMsg.setContent(createCfpContent(oldcontent,actor));
	cfpMsg.clearAllReceiver();
	cfpMsg.addReceiver(actor);
	myAgent.send(cfpMsg);
      }

      template = MessageTemplate.MatchInReplyTo(cfpMsg.getReplyWith());
      waitedAgents = proposerAgents;
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      state = 1;
      break;
    }
    case 1: { // waiting for propose
      // remains in this state until a message from each receiver arrives
      // or the timeout expires. In both cases it goes to state 2

      // The following variables must be rightly set before entering in this 
      // state: myAgent, state, hasBeenReset, template, waitedAgents, 
      //        msgProposals (must be != null)
      // At the end of the state the following variables are set:
      // blockTime, state
      // In this state the user method handleOtherMessages is called and
      // its result is ignored. 
      if (hasBeenReset) { 
	// this block of code cannot be moved outside the switch
	// because in the state 2 I don't want to sense a reset until
	// the state 3 has completed
	hasBeenReset = false;
	state = 0;
	return;
      }
      ACLMessage msg=myAgent.receive(template);
      if (msg == null) {
	if (timeout > 0) {
	  blockTime = endingTime - System.currentTimeMillis();
	  //	  System.err.println("FipaContractNetInitiatorBehaviour: timeout="+timeout+" endingTime="+endingTime+" currTime="+System.currentTimeMillis()+" blockTime="+blockTime);
	  if (blockTime <= 0) { //timeout expired
	    state=2;
	    return;
	  } else {
	    block(blockTime);
	    return;
	  }
	} else { // query without timeout
	  block();
	  return;
	  }
      }
      
      // Here the receive() has really read a message
      //System.err.println("FipaContractNetInitiatorBehaviour: receive");
      //msg.dump();
      waitedAgents.remove(msg.getSender());
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (waitedAgents.size()==0) {
	state=2;
	}
      if (ACLMessage.PROPOSE == msg.getPerformative()) 
	msgProposals.addElement(msg);    
      else { 
	handleOtherMessages(msg);
	if (hasBeenReset) { 
	  // this block of code cannot be moved outside the switch
	  // because in the state 2 I don't want to sense a reset until
	  // the state 3 has completed
	  hasBeenReset = false;
	  state = 0;
	  return;
	}
      }
      break;
    }
    case 2: { // evaluate the proposals
      if (hasBeenReset) { 
	// this block of code cannot be moved outside the switch
	// because in the state 2 I don't want to sense a reset until
	// the state 3 has completed
	hasBeenReset = false;
	state = 0;
	return;
      }
      msgAcceptReject = handleProposeMessages(msgProposals);
      if ((msgAcceptReject == null) && (!hasBeenReset) )
	finished=true;
      else state=3;
      break;
    }
    case 3: { // send accept-proposals and reject-proposals
      ACLMessage tmpmsg;
      waitedAgents = new ArrayList();
      long tmptime;
      timeout = -1;
      String replyWith = "ContractNetState4"+(new Date()).getTime();
      for (int i=0; i<msgAcceptReject.size(); i++) {
	tmpmsg = (ACLMessage)msgAcceptReject.elementAt(i);
	if (ACLMessage.ACCEPT_PROPOSAL ==tmpmsg.getPerformative()) {
	  tmptime = tmpmsg.getReplyByDate().getTime()-(new Date()).getTime();
	  if (timeout < tmptime)
	    timeout = tmptime; // put in timeout the maximum timeout
	  waitedAgents.add(tmpmsg.getAllReceiver().next());
	}
	tmpmsg.setSender(myAgent.getAID());
	tmpmsg.setReplyWith(replyWith);
	tmpmsg.setConversationId(cfpMsg.getConversationId());
	tmpmsg.setProtocol(cfpMsg.getProtocol());
	myAgent.send(tmpmsg);
	//System.err.println("FipaContractNetInitiatorBehaviour: send");
	//tmpmsg.dump();
	//System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      }
      template = MessageTemplate.MatchInReplyTo(replyWith);
      endingTime = System.currentTimeMillis() + timeout;
      state = 4;
      if (hasBeenReset) {
	hasBeenReset = false;
	state = 0;
	return;
      }
      if (waitedAgents.size()==0)  // If No proposal has been accepted
	finished=true;
      break;
    }
    case 4: { // I can here receive failure, or inform(done) or not-understood
      if (hasBeenReset) {
	hasBeenReset = false;
	state = 0;
	return;
      } 
      ACLMessage msg=myAgent.receive(template);
      if (msg == null) {
	if (timeout > 0) {
	  blockTime = endingTime - System.currentTimeMillis();
	  //	  System.err.println("FipaContractNetInitiatorBehaviour: timeout="+timeout+" endingTime="+endingTime+" currTime="+System.currentTimeMillis()+" blockTime="+blockTime);
	  if (blockTime <= 0) { //timeout expired
	    state=5;
	    return;
	  } else {
	    block(blockTime);
	    return;
	  }
	} else { // no timeout
	  block();
	  return;
	}
      }
      
      //System.err.println("FipaContractNetInitiatorBehaviour: receive");
      //msg.dump();
      waitedAgents.remove(msg.getSender());
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (waitedAgents.size() == 0) 
	state=5;
      msgFinal.addElement(msg);
      break;
    }
    case 5: {
      if (hasBeenReset) {
	hasBeenReset = false;
	state = 0;
	return;
      } 
      msgFinalAnswers = handleFinalMessages(msgFinal);
      if ((msgFinalAnswers == null) && (!hasBeenReset))
	finished = true;
      state=6;
      break;
    }
    case 6: { // final state of the protocol
      for (int i=0; i<msgFinalAnswers.size(); i++) {
	((ACLMessage)msgFinalAnswers.elementAt(i)).setSender(myAgent.getAID());
	myAgent.send((ACLMessage)msgFinalAnswers.elementAt(i));
	//System.err.println("FipaContractNetInitiatorBehaviour: send");
	//((ACLMessage)msgFinalAnswers.elementAt(i)).dump();
      }
      if (hasBeenReset) {
	hasBeenReset = false;
	state = 0;
      } else
	finished = true; // end Behaviour
      break;
    }
    } // end of switch
  } // end of action()


  /**
   * This method is called by the JADE scheduler of agent behaviours and
   * it should usually be ignored by application code. It returns true
   * when the behaviour has terminated and should be removed from the queue
   * of active behaviours. */
  public boolean done() {
    return finished;
  }


  /**
   * This method must be implemented by all subclasses.  After the agent has
   * sent the <code> cfp </code> message, this
   * method is called
   * everytime a new message arrives that is different from a <code>
   * propose </code> message.  The method should react to this message
   * in an application-dependent way. 
   * <p>
   * The class variable <code>myAgent </code> can be
   * used to send messages or, after casting, to execute other
   * application-dependent methods that belongs to the Agent
   * object.
   * @param msg is the ACLMessage just arrived */
    public abstract void handleOtherMessages(ACLMessage msg);  

  /**
   * This method is called after all the <code>propose</code> messages
   * have been collected or after the timeout has expired.
   * By default an infinite timeout is used.
   * That default is overriden by
   * the reply-by parameter of the <code>cfp</code> message, if set.
   * @param proposals is the Vector that contains the received
   * <code>propose</code> ACL messages.
   * @return a <code>Vector</code> of ACLMessages to be sent in the
   * next phase of the protocol. Usually, these messages should be of
   * type <code>accept-proposal</code> or
   * <code>reject-proposal</code>. If <code>null</code> is returned instead,
   * then the protocol is prematurely terminated. 
   * <p>
   * <b>REMEMBER</b> to set the
   * value of <code>:in-reply-to</code> parameter in all the returned
   * messages of this vector. This implementation of the protocol is
   * not able to set that value on your behalf because it implements a
   * one-to-many protocol and, unfortunately, each of the many might
   * use a different value of <code>:in-reply-to</code>.
   * <p>
   * <b>NOTICE</b> that all the returned messages are sent even if
   * the <code>reset</code> method is called. That allows the implementation
   * of the FIPA-Iterated-Contract-Net protocol. See at the top for
   * some usefull instructions.
   */
    public abstract Vector handleProposeMessages(Vector proposals);

  /**
   * After having sent the messages returned by 
   * <code>handleProposeMessages()</code>,
   * the protocol waits for the expiration of the
   * maximum timeout specified in those messages
   * (reply-by parameter), or until all the answers are received. 
   * If no reply-by parameter was set, an infinite timeout 
   * is used, instead.
   * After this timeout, this method is called to react to all the received
   * messages. At the next state of the protocol, all the returned messages
   * are sent and then the protocol terminates.
   * @param messages is the <code>Vector</code> of ACL messages received so far
   * @return a <code>Vector</code> of ACL messages to be sent in the next state of the 
   * protocol. return null to terminate the protocol
   * <b>REMEMBER</b> to set the value of <code>:in-reply-to</code> parameter
   * in all the returned messages of this vector. This implementation of
   * the protocol is not able to set that value on your behalf because
   * it implements a one-to-many protocol and, unfortunately, 
   * each of the many might
   * use a different value of <code>:in-reply-to</code>. 
   */
   public abstract Vector handleFinalMessages(Vector messages);

  /**
   * Some content languages require that the name of the actor be included
   * within the proposed action itself. For instance, in order to request
   * to N sellers (s1,s2, ..., sN) the cost of a car, the cfp message should
   * have this content (by using SL0 content language): <ul>
   * <li> <code> <i, cfp(s1, ( (action s1 (sell car)), cost < 10000))> </code>
   * <li> <code> <i, cfp(s2, ( (action s2 (sell car)), cost < 10000))> </code>
   * <li> ...
   * <li> <code> <i, cfp(sN, ( (action sN (sell car)), cost < 10000))> </code>
   * </ul>
   * You can notice that the content changes for every receiver because
   * the actor is the name of the receiver itself.
   * The purpose of this abstract method is to return the actual content for 
   * the cfp message to be sent to the given receiver.
   * A suggestion for the implementation is to insert a special symbol within
   * the cfpContent that, at every call, is replaced with the AID of the 
   * receiver. Unfortunatelly, this default implementation cannot be 
   * provided by this behaviour because there exist no such a universal
   * special symbol.
   * @param cfpContent this is the content of the cfp message that was passed
   * in the constructor of the behaviour
   * @param receiver this is the AID of the receiver agent to which this 
   * content is destinated
   * @return the actual content to be sent to this receiver
   */
  public abstract String createCfpContent(String cfpContent, AID receiver);


  /**
   * This method resets this behaviour object so that it restarts from
   * the initial state of the protocol.
   * <p>
   * Care must be taken to where this method is called because, in some
   * intermediate states of the contract net protocol, restarting may cause
   * unwanted side effects 
   * (e.g. not receiving some propose messages
   * that refers to the previous cfp message).
   */
public void reset() {
  super.reset();
  state = 0;  // state of the protocol
  finished=false; // true when done()
  hasBeenReset = true;
  msgProposals = new Vector();
  msgFinal = new Vector();
}


  /**
   * @param msg updates the cfp message to be sent
   * @param responders updates the group of agents to which the cfp message should be sent
   * @see #reset()
   */
public void reset(ACLMessage msg, List responders) {
  reset();
  cfpMsg = (ACLMessage)msg.clone();
  proposerAgents = responders;
}
}

