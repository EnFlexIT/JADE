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

import java.util.Date;

import java.io.*;

/**
* Behaviour class for <code>fipa-contract-net</code>
* <em>Responder</em> role.  This abstract behaviour implements the
* <code>fipa-contract-net</code> interaction protocol from the point
* of view of a responder to a call for proposal (<code>cfp</code>)
* message.  In order to use correctly this behaviour, the programmer
* should do the following:

* <ul> <li> Implement a class that extends
* <code>FipaContractNetResponderBehaviour</code>.  This class must
* implement 4 methods that are called by
* <code>FipaContractNetResponderBehaviour</code>:

* <ul>

* <li> <code>public ACLMessage handleCfpMessage(ACLMessage cfp)</code> to
* evaluate the <code>cfp</code> message received and to return an
* <code>ACLMessage</code> to be sent in response
* (<code>propose</code>, <code>refuse</code> or
* <code>not-understood</code>).  If <code>null</code> is returned,
* then the <code>cfp</code> is ignored and the behaviour is reset and
* start again waiting for <code>cfp</code> messages.

* <li> <code>public ACLMessage handleAcceptProposalMessage(ACLMessage msg)</code>
* to evaluate the received <code>accept-proposal</code> message and to
* return an <code>ACLMessage</code> to be sent back (<code>inform
* Done</code> or <code>failure</code>).

* <li> <code>public void handleRejectProposalMessage(ACLMessage msg)</code> to
* evaluate the received <code>reject-proposal</code>. After this
* method, the protocol is reset and it restarts again.

* <li> <code>public void handleOtherMessages(ACLMessage msg)</code> to
* handle all the other types of messages, eventually answering not-understood.

* <li> Optionally, the programmer might override the
* <code>reset()</code> method that is called to reset the behaviour
* and wait for a new <code>cfp</code> message. In this case, remind to
* call also <code>super.reset()</code> from within overridden
* <code>reset()</code> version!

* </ul>

* <li> create a new instance of this class and add it to the agent
* calling <code>Agent.addBehaviour()</code>)

* </ul>
* @see jade.proto.FipaContractNetInitiatorBehaviour
*
* Javadoc documentation for the file
* @author Fabio Bellifemine - CSELT
* @version $Date$ $Revision$
*/
public abstract class FipaContractNetResponderBehaviour extends SimpleBehaviour {
    
 private MessageTemplate mt=MessageTemplate.MatchType("cfp");
 private MessageTemplate template;
 private int   state=0; // state of the protocol
private long timeout, blockTime, endingTime;
 private ACLMessage cfpMsg, acceptMsg, informMsg, proposeMsg;
  /**
   * this variable should be set to true when the behaviour should
   * terminate
 */
 public boolean finished = false;

 
  /**
   * This method is called to restart the protocol and wait again for
   * a new call for proposal message.  The method can be overriden by
   * subclasses, but <code>super.reset()</code> should always be
   * called.
   */
 public void reset() {
    mt=MessageTemplate.MatchType("cfp");
    template = null;
    state = 0;
    cfpMsg = null;
    acceptMsg = null;
    informMsg = null;
    proposeMsg = null;
    finished = false;
 }
 

  /**
   * Constructor of the class.
   * @param a is the pointer to the Agent class
   */ 
  public FipaContractNetResponderBehaviour(Agent a) {
    super(a); 
  }
 
  
  /**
   * This method checks whether this behaviour has finished its task.
   * @return <code>true</code> if the behaviour must be terminated,
   * <code>false</code> otherwise.
   */
  public boolean done() {
    return finished;
  }
 
 
  /**
   * Actual body of the behaviour. It cannot be overriden.
   */
  final public void action() {
    switch (state) {
    case 0: {
      cfpMsg = myAgent.receive(mt);
      if (cfpMsg == null) {
	block();
	return;
      }
      //System.err.println("FipaContractNetResponderBehaviour: receive");
      //cfpMsg.dump();
      state = 1;
      break;
    }
    case 1: {
      proposeMsg = handleCfpMessage(cfpMsg);
      state = 2;
      if (proposeMsg == null) 
	reset();
      break;
    }
    case 2: {
      state = 3;
      proposeMsg.setSource(myAgent.getName());
      proposeMsg.setReplyTo(cfpMsg.getReplyWith());
      proposeMsg.setProtocol("FIPA-Contract-Net");
      proposeMsg.setConversationId(cfpMsg.getConversationId());
      if (proposeMsg.getReplyWith().length()<1)
	proposeMsg.setReplyWith("ContractNetResponder"+(new Date()).getTime());
      template = MessageTemplate.MatchReplyTo(proposeMsg.getReplyWith());
      myAgent.send(proposeMsg);
      //System.err.println("FipaContractNetResponderBehaviour: send");
      //proposeMsg.dump();
      if (! proposeMsg.getType().equalsIgnoreCase("propose"))
	reset();
      else {
	timeout = proposeMsg.getReplyByDate().getTime()-(new Date()).getTime();
	if (timeout <= 1000) timeout = -1; // infinite timeout
	endingTime = System.currentTimeMillis() + timeout;
      //      System.err.println("FipaQueryInitiatorBehaviour: timeout="+timeout+" endingTime="+endingTime+" currTime="+System.currentTimeMillis());
      }
      break;
    }   
    case 3: { // in this state I can receive only accept-proposal or reject-proposal
      acceptMsg = myAgent.receive(template);
      if (acceptMsg == null) {
	if (timeout > 0) {
	  blockTime = endingTime - System.currentTimeMillis();
	  //	  System.err.println("FipaContractNetResponderBehaviour: timeout="+timeout+" endingTime="+endingTime+" currTime="+System.currentTimeMillis()+" blockTime="+blockTime);
	  if (blockTime <= 0) { //timeout expired
	    reset();
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
      if (acceptMsg.getType().equalsIgnoreCase("accept-proposal")) {
	//System.err.println("FipaContractNetResponderBehaviour: receive");
	//acceptMsg.dump();
	state = 4;
	informMsg = handleAcceptProposalMessage(acceptMsg);
      } else if (acceptMsg.getType().equalsIgnoreCase("reject-proposal")) {
	//System.err.println("FipaContractNetResponderBehaviour: receive");
	//acceptMsg.dump();
	handleRejectProposalMessage(acceptMsg);
	reset();
      } else 
	handleOtherMessages(acceptMsg);
      break;
    }
    case 4: { // send the last message
      informMsg.setSource(myAgent.getName());
      informMsg.addDest(acceptMsg.getSource());
      informMsg.setReplyTo(acceptMsg.getReplyWith());
      informMsg.setProtocol("FIPA-Contract-Net");
      informMsg.setConversationId(acceptMsg.getConversationId());
      myAgent.send(informMsg);
      //System.err.println("FipaContractNetResponderBehaviour: send");
      //informMsg.dump();
      reset();
      break;
    }
    } // end of switch
  }


  /**
   * This method is called when the <code>accept-proposal</code>
   * message is received. 
   * @param msg contains the received <code>accept-proposal</code> message.
   * @return the <code>ACLMessage</code> to be sent as a response at
   * the next state of the protocol.
   */
  public abstract ACLMessage handleAcceptProposalMessage(ACLMessage msg);

  /**
   * This method is called when the <code>reject-proposal</code>
   * message is received.  The method is also called when the timeout
   * is elapsed.  The protocol uses a default timeout, that is the
   * class variable DEFAULTTIMEOUT; the <code>:reply-by</code> slot of
   * the <code>propose</code> message overrides the default timeout.
   * After the execution of this method the protocol is reset.
   * @param msg contains the received reject-proposal message.
   */
public abstract void handleRejectProposalMessage(ACLMessage msg);


  /**
   * This method is called when the <code>cfp</code>
   * message is received. 
   * @param msg contains the received cfp message.
   * @return the <code>ACLMessage</code> to be sent as a response at
   * the next state of the protocol. If null, or different from
   * <code>propose</code>, then the protocol is reset.
   */
public abstract ACLMessage handleCfpMessage(ACLMessage cfp);

  /**
   * This method must be implemented by all subclasses.
   * After having sent the <code> propose </code> message, the base class calls
   * this method everytime a new message arrives that is not an <code> accept-proposal / reject-proposal
   * </code> message.
   * The method should react to this message in an
   * implementation-dependent way. The instruction
   * <code> finished=true; </code> should be executed to finish the
   * protocol.
   * The class variable <code>myAgent </code> can be used to send
   * messages or, after casting, to execute other implementation-dependent
   * methods that belongs to the actual Agent object.
   * @param msg is the ACLMessage just arrived
   */
public abstract void handleOtherMessages(ACLMessage msg);
}














