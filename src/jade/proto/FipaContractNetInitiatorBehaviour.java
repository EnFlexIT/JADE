package jade.proto;

import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

import java.io.*;

/**
* Behaviour class for <code>fipa-contract-net</code>
* <em>Initiator</em> role.  This abstract behaviour implements the
* <code>fipa-contract-net</code> interaction protocol from the point
* of view of the agent initiating the protocol, that is the agent that
* sends the <code>cfp</code> message (<em>Call for Proposal</em> to a
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
* behaviour

* <li> <code> public String createCfpContent(String cfpContent, String receiver)</code> 
* to return the cfp content for each receiver.
* </ul>
* <li> Create a new instance of this class and add it to the agent
* with <code>Agent.addBehaviour()</code> method)
* </ul>
* <p>
* @see jade.proto.FipaContractNetResponderBehaviour
* @author Fabio Bellifemine - CSELT
* @version $Date$ $Revision$
*/
public abstract class FipaContractNetInitiatorBehaviour extends SimpleBehaviour {
    
  /** It is the pointer to the Agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and then use the methods of the extended class. 
   * For instance 
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments()</code>
   */
  
  /* This is the cfpMsg sent in the first state of the protocol */
protected ACLMessage cfpMsg; 

  private int state = 0;  // state of the protocol
  private long timeout, blockTime, endingTime;
  private MessageTemplate template;
  private Vector msgProposals = new Vector(); // vector of ACLMessage with the proposals
  private Vector msgAcceptReject = new Vector(); // vector with the ACLMessages to send (accept/reject proposal)
  private Vector msgFinal = new Vector(); // vector with the ACLMessages received after accept/reject-proposal
  private Vector msgFinalAnswers = new Vector(); // vector with the ACLMessages to send at the end of the protocol
  private AgentGroup proposerAgents;
  private AgentGroup waitedAgents;

  /**
   * this variable should be set to true when the behaviour should terminate
   */
  protected boolean finished=false; // true when done()


  /**
   * constructor of the behaviour.
   * @param a is the current agent. The public variable 
   * <code>Agent myAgent</code> contains then the pointer to the agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and use the methods of the extended class. 
   * For instance 
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments() </code>
   * @param msg is the Call for Proposal message to be sent
   * @param group is the group of agents to which the cfp must be sent
   */
    public FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg, AgentGroup group) {
      super(a);
      cfpMsg = (ACLMessage)msg.clone();
      proposerAgents = (AgentGroup)group.clone();
      state=0;
    }


  /**
   * constructor of the behaviour. In this case the group of responder agents
   * is extracted by the receivers of the ACLMessage that has been passed as
   * parameter.
   * @see #FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg, AgentGroup group)
   */
    public FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg) {
      this(a,msg,msg.getDests());
    }
    


  /**
   * Action method of the behaviour. This method cannot be overriden
   * by subclasses because it implements the actual FipaContractNet
   * protocol
   */
  final public void action() {
    switch (state) {
    case 0: {
      /* This is executed only when the Behaviour is started*/
      cfpMsg.setType("cfp");
      cfpMsg.setProtocol("FIPA-Contract-Net");
      cfpMsg.setSource(myAgent.getName());
      if (cfpMsg.getReplyWith() == null)
	cfpMsg.setReplyWith("ContractNet"+(new Date()).getTime());
      if (cfpMsg.getConversationId() == null)
	cfpMsg.setConversationId("ContractNet"+(new Date()).getTime());
      timeout = cfpMsg.getReplyByDate().getTime()-(new Date()).getTime();
      if (timeout <= 1000) timeout = -1; // infinite timeout
      endingTime = System.currentTimeMillis() + timeout;

      //replace the content with the actual actor name 
      // that is 1 actor for each message.
      String actor;
      String oldcontent = cfpMsg.getContent();
      Enumeration e = proposerAgents.getMembers();
      while (e.hasMoreElements()) {
	actor = (String)e.nextElement();
	cfpMsg.setContent(createCfpContent(oldcontent,actor));
	cfpMsg.setDest(actor);
	myAgent.send(cfpMsg);
      }

      template = MessageTemplate.MatchReplyTo(cfpMsg.getReplyWith());
      waitedAgents = (AgentGroup)proposerAgents.clone();
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      state = 1;
      break;
    }
    case 1: { // waiting for propose
      // remains in this state
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
      waitedAgents.removeMember(msg.getSource());
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (!waitedAgents.getMembers().hasMoreElements()) {
	state=2;
      }
      if (msg.getType().equalsIgnoreCase("propose")) 
	msgProposals.addElement(msg);    
      else handleOtherMessages(msg);
      break;
    }
    case 2: { // evaluate the proposals
	msgAcceptReject = handleProposeMessages(msgProposals);
	if (msgAcceptReject == null) finished=true;
	else state=3;
	break;
    }
    case 3: { // send accept-proposals and reject-proposals
      ACLMessage tmpmsg;
      waitedAgents = new AgentGroup();
      long tmptime;
      timeout = -1;
      String replyWith = "ContractNetState4"+(new Date()).getTime();
      for (int i=0; i<msgAcceptReject.size(); i++) {
	tmpmsg = (ACLMessage)msgAcceptReject.elementAt(i);
	if (tmpmsg.getType().equalsIgnoreCase("accept-proposal")) {
	  tmptime = tmpmsg.getReplyByDate().getTime()-(new Date()).getTime();
	  if (timeout < tmptime)
	    timeout = tmptime; // put in timeout the maximum timeout
	  waitedAgents.addMember(tmpmsg.getDest());
	}
	tmpmsg.setSource(myAgent.getName());
	tmpmsg.setReplyWith(replyWith);
	tmpmsg.setConversationId(cfpMsg.getConversationId());
	tmpmsg.setProtocol("FIPA-Contract-Net");
	myAgent.send(tmpmsg);
	//System.err.println("FipaContractNetInitiatorBehaviour: send");
	//tmpmsg.dump();
	//System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      }
      template = MessageTemplate.MatchReplyTo(replyWith);
      endingTime = System.currentTimeMillis() + timeout;
      state = 4;
      break;
    }
    case 4: { // I can here receive failure, or inform(done) or not-understood
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
      waitedAgents.removeMember(msg.getSource());
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (!waitedAgents.getMembers().hasMoreElements()) 
	state=5;
      msgFinal.addElement(msg);
      break;
    }
    case 5: {
      msgFinalAnswers = handleFinalMessages(msgFinal);
      if (msgFinalAnswers == null)
	finished = true;
      state=6;
      break;
    }
    case 6: { // final state of the protocol
      finished = true; // end Behaviour
      for (int i=0; i<msgFinalAnswers.size(); i++) {
	((ACLMessage)msgFinalAnswers.elementAt(i)).setSource(myAgent.getName());
	myAgent.send((ACLMessage)msgFinalAnswers.elementAt(i));
	//System.err.println("FipaContractNetInitiatorBehaviour: send");
	//((ACLMessage)msgFinalAnswers.elementAt(i)).dump();
      }
      break;
    }
    } // end of switch
  } // end of action()


  public boolean done() {
    return finished;
  }


  /**
   * This method must be implemented by all subclasses.  After having
   * sent the <code> cfp </code> message, the base class calls this
   * method everytime a new message arrives that is not a <code>
   * propose </code> message.  The method should react to this message
   * in an implementation-dependent way. The instruction <code>
   * finished=true; </code> should be executed to finish the contract
   * net protocol.  The class variable <code>myAgent </code> can be
   * used to send messages or, after casting, to execute other
   * implementation-dependent methods that belongs to the actual Agent
   * object.
   * @param msg is the ACLMessage just arrived */
    public abstract void handleOtherMessages(ACLMessage msg);  

  /**
   * This method is called after all the <code>propose</code> messages
   * have been collected or after the timeout.
   * By default an infinite timeout is used.
   * This timeout is overriden by
   * the reply-by parameter of the <code>cfp</code> message, if set.
   * @param proposals is the Vector that contains the received
   * <code>propose</code> ACL message.
   * @return a <code>Vector</code> of ACLMessage to be sent in the
   * next phase of the protocol. Usually, these messages should be of
   * type <code>accept-proposal</code> or
   * <code>reject-proposal</code>. If <code>null</code> is returned,
   * then the protocol is prematurely terminated. <b>REMEMBER</b> to set the
   * value of <code>:in-reply-to</code> parameter in all the returned
   * messages of this vector. This implementation of the protocol is
   * not able to set that value on your behalf because it implements a
   * one-to-many protocol and, unfortunately, each of the many might
   * use a different value of <code>:in-reply-to</code>.
   */
    public abstract Vector handleProposeMessages(Vector proposals);

  /**
   * After having sent the messages returned by <code>handleProposeMessages()</code>,
   * the protocol waits for the maximum timeout specified in those messages
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
   * You can notice that the content changes for every receiver.
   * The purpose of this abstract method is to return the actual content for 
   * the cfp message to be sent to the given receiver.
   * A suggestion for the implementation is to insert a special symbol within
   * the cfpContent that, at every call, is replaced with the name of the 
   * receiver. Unfortunatelly, this default implementation cannot be 
   * provided by this behaviour because there exist no such a universal
   * special symbol.
   * @param cfpContent this is the content of the cfp message that was passed
   * in the constructor of the behaviour
   * @param receiver this is the name of the receiver agent to which this 
   * content is destinated
   * @return the actual content to be sent to this receiver
   */
  public abstract String createCfpContent(String cfpContent, String receiver);
}











