package jade.proto;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

import java.io.*;

/**
* This abstract behaviour implements the Fipa Contract Net Interaction Protocol
* from the point of view of the agent initiating the protocol, that is the
* agent that sends the Call for Proposal to a set of agents.
* In order to use correctly this behaviour, the programmer should do the following:
* <ul>
* <li> implements a class that extends FipaContractNetInitiatorBehaviour.
* This class must implement 3 methods that are called by FipaContractNetInitiatorBehaviur:
* <ul>
* <li> <code> public Vector evaluateProposals(Vector proposals) </code>
* to evaluate all the received proposals and to return a vector of ACLMessages
* to be sent in response to the proposals (return null to terminate 
* the protocol)
* <li> <code> public void handleOtherMessages(ACLMessage msg) </code>
* to handle all received messages different from "propose"
* <li> <code> public Vector evaluateFinalMessages(Vector messages) </code>
* to evaluate the messages received in the last state of the protocol, that is
* "inform done" and "failure" messages, and to return a Vector of ACLMessages to
* be sent before terminating the behaviour
* </ul>* <li> create a new instance of this class and add it to the agent (agent.addBehaviour())
* </ul>
* <p>
* <B>Important Note:</B> The FIPA semantics and the FIPA content language
* do require that the actor name be a single name. When this protocol is
* initiated with more than one agent (i.e. the cfp is sent to a group of agents
* rather than a single agent), the content of the cfp message must be
* changed for each receiver. This class supports this requirement by
* replacing the first character '*' in the passed message content with
* the name of the receiver. 
* This feature may introduce errors in those contents whose ontology contains
* the symbol '*'. Future releases will consider improving this
* feature.
* <p>
* KNOWN BUGS: If the message content contains the character '*', it is
* replaced with the name of the receiver.  
*/
public abstract class FipaContractNetInitiatorBehaviour extends SimpleBehaviour {
    
  /** It is the pointer to the Agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and then use the methods of the extended class. 
   * For instance 
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments() </code>
   */
  public Agent myAgent;
  
  /* This is the cfpMsg sent in the first state of the protocol */
protected ACLMessage cfpMsg; 
  private int state = 0;  // state of the protocol
  long timeout;
  MessageTemplate template;
  Vector msgProposals = new Vector(); /* vector of ACLMessage with the proposals */
  Vector msgAcceptReject = new Vector(); // vector with the ACLMessages to send (accept/reject proposal)
  Vector msgFinal = new Vector(); // vector with the ACLMessages received after accept/reject-proposal
  Vector msgFinalAnswers = new Vector(); // vector with the ACLMessages to send at the end of the protocol
  AgentGroup proposerAgents;
  private AgentGroup waitedAgents;
  /**
   * this variable should be set to true when the behaviour should terminate
   */
  public boolean finished; // true when done()
  /** 
   * default timeout in milliseconds to wait for proposals. 
   * This timeout is overriden by
   * the reply-by parameter of the <code>cfp</code> message, if set.
   */
  public static final long DEFAULTTIMEOUT = 30000; 
  ACLMessage wakeMsg;
  Waker waker;

  /**
   * constructor of the behaviour.
   * @param a is the current agent. The public variable 
   * <code> Agent myAgent </code> contains then the pointer to the agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and use the methods of the extended class. 
   * For instance 
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments() </code>
   * @param msg is the Call for Proposal message to be sent
   * @param group is the group of agents to which the cfp must be sent
   */
    public FipaContractNetInitiatorBehaviour(Agent a, ACLMessage msg, AgentGroup group) {
      myAgent = a;
      cfpMsg = (ACLMessage)msg.clone();
      proposerAgents = (AgentGroup)group.clone();
    }
    
  /**
   * This constructor is here only to make happy the Java compiler.
   * It should never be used, however.
   */
   public FipaContractNetInitiatorBehaviour(){ // default constructor
     System.err.println("!! Called wrong constructor for FipaContractNetInitiatorBehaviur !!");
     finished = true;
   }

  /**
   * action method of the behaviour. This method cannot be overriden by 
   * subclasses because it implements the actual FipaContractNet protocol
   */
  final public void action() {
    switch (state) {
    case 0: {
      /* This is executed only when the Behaviour is started*/
      state = 1;
      cfpMsg.setType("cfp");
      cfpMsg.setProtocol("FIPA-Contract-Net");
      cfpMsg.setSource(myAgent.getName());
      if (cfpMsg.getReplyWith() == null)
	cfpMsg.setReplyWith("ContractNet"+(new Date()).getTime());
      if (cfpMsg.getConversationId() == null)
	cfpMsg.setConversationId("ContractNet"+(new Date()).getTime());
      timeout = cfpMsg.getReplyByDate().getTime()-(new Date()).getTime();
      if (timeout <= 1000) timeout = DEFAULTTIMEOUT; // at least 1 second
      //start a thread with the timeout
      wakeMsg = new ACLMessage("inform");
      wakeMsg.setReplyTo(cfpMsg.getReplyWith());
      wakeMsg.setConversationId("WAKEUP"+cfpMsg.getReplyWith());
      wakeMsg.setContent("(timeout " + timeout + ")");
      waker = new Waker(myAgent,wakeMsg,timeout);
      waker.start();
      int p = cfpMsg.getContent().indexOf('*');
      if (p != -1) {
	//replace with the actual actor name that is 1 actor for each message.
	String oldcontent = cfpMsg.getContent();
	String actor;
	Enumeration e = proposerAgents.getMembers();
	while (e.hasMoreElements()) {
	  actor = (String)e.nextElement();
	  cfpMsg.setContent(oldcontent.substring(0,p-1) + actor + oldcontent.substring(p+1,oldcontent.length()));
	  cfpMsg.setDest(actor);
	  myAgent.send(cfpMsg);
	}
      } else { // the content does not contain the character '*'
	myAgent.send(cfpMsg,proposerAgents);
      }
      template = MessageTemplate.MatchReplyTo(cfpMsg.getReplyWith());
      waitedAgents = (AgentGroup)proposerAgents.clone();
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      break;
    }
    case 1: { // waiting for propose
      // remains in this state
      ACLMessage msg=myAgent.receive(template);
      if (msg == null) {
	block();
	return;
      }
      //System.err.println("FipaContractNetInitiatorBehaviour: receive");
      //msg.dump();
      waitedAgents.removeMember(msg.getSource());
      //System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (!waitedAgents.getMembers().hasMoreElements()) {
	waker.stop();
	state=2;
      }
      if (msg.getType().equalsIgnoreCase("propose")) {
	// msg contains a propose ACLMessage
	msgProposals.addElement(msg);    
      } else if (msg.getConversationId() == null) {
	handleOtherMessages(msg);
      } else if (msg.getConversationId().equalsIgnoreCase(wakeMsg.getConversationId())) { 
	// wake-up message
	state = 2;
      } else 
	handleOtherMessages(msg);
      break;
    }
    case 2: { // evaluate the proposals
	msgAcceptReject = evaluateProposals(msgProposals);
	if (msgAcceptReject == null) finished=true;
	else state=3;
	break;
    }
    case 3: { // send accept-proposals and reject-proposals
      ACLMessage tmpmsg;
      waitedAgents = new AgentGroup();
      long tmptime;
      timeout = DEFAULTTIMEOUT;
      state = 4;
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
	System.err.println("FipaContractNetInitiatorBehaviour: send");
	tmpmsg.dump();
	System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      }
      template = MessageTemplate.MatchReplyTo(replyWith);
      wakeMsg = new ACLMessage("inform");
      wakeMsg.setReplyTo(replyWith);
      wakeMsg.setContent("(timeout " + timeout + ")");
      wakeMsg.setConversationId("WAKEUP"+cfpMsg.getReplyWith());
      waker = new Waker(myAgent,wakeMsg,timeout);
      waker.start();
      break;
    }
    case 4: { // I can here receive failure, or inform(done) or not-understood
      ACLMessage msg=myAgent.receive(template);
      if (msg == null) {
	block();
	return;
      }
      System.err.println("FipaContractNetInitiatorBehaviour: receive");
      msg.dump();
      waitedAgents.removeMember(msg.getSource());
      System.err.println("FipaContractNetInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (!waitedAgents.getMembers().hasMoreElements()) {
	waker.stop();
	state=5;
      }
      if (msg.getConversationId() == null) 
	msgFinal.addElement(msg);
      else if (msg.getConversationId().equalsIgnoreCase(wakeMsg.getConversationId())) {
	state = 5; // go to the next state of the protocol
	// wake-up message
      } else 
	msgFinal.addElement(msg);
      break;
    }
    case 5: {
      state=6;
      msgFinalAnswers = evaluateFinalMessages(msgFinal);
      if (msgFinalAnswers == null)
	finished = true;
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
   * This method must be implemented by all subclasses.
   * After having sent the <code> cfp </code> message, the base class calls
   * this method everytime a new message arrives that is not a <code> propose
   * </code> message.
   * The method should react to this message in an 
   * implementation-dependent way. The instruction
   * <code> finished=true; </code> should be executed to finish the
   * contract net protocol.
   * The class variable <code>myAgent </code> can be used to send
   * messages or, after casting, to execute other implementation-dependent
   * methods that belongs to the actual Agent object.
   * @param msg is the ACLMessage just arrived
   */
public abstract void handleOtherMessages(ACLMessage msg);  

  /**
   * This method is called after all the <code>propose</code> messages
   * have been collected or after the timeout.
   * The default timeout is specified by the class variable 
   * <code>DEFAULTTIMEOUT</code>. 
   * This timeout is overriden by
   * the reply-by parameter of the <code>cfp</code> message, if set.
   * @param proposals is the Vector that contains the received
   * <code>propose</code> ACLMessage 
   * @return a Vector of ACLMessage to be sent in the next phase of the
   * protocol. Usually, these messages should be of type 
   * <code>accept-proposal   reject-proposal</code>. If <code>null</code>
   * is returned, then the protocol is prematurely terminated.
   * REMIND to set the value of <code>:in-reply-to</code> parameter
   * in all the returned messages of this vector. This implementation of
   * the protocol is not able to set that value on your behalf because
   * it implements a one-to-many protocol and, unfortunatelly, 
   * each of the many might
   * use a different value of <code>:in-reply-to</code>. 
   */
public abstract Vector evaluateProposals(Vector proposals);

  /**
   * After having sent the messages returned by <code>evaluateProposals</code>,
   * the protocol waits for the maximum timeout specified in those messages
   * (reply-by parameter), or until all the answers are received. 
   * If no reply-by parameter was set, <code>
   * DEFAULTTIMEOUT</code> is used, instead.
   * After this timeout, this method is called to react to all the received
   * messages. At the next state of the protocol, all the returned messages
   * are sent and then the protocol terminates.
   * @param messages is the Vector of ACLMessage received so far
   * @return a Vector of ACLMessage to be send in the next state of the 
   * protocol. return null to terminate the protocol
   * REMIND to set the value of <code>:in-reply-to</code> parameter
   * in all the returned messages of this vector. This implementation of
   * the protocol is not able to set that value on your behalf because
   * it implements a one-to-many protocol and, unfortunatelly, 
   * each of the many might
   * use a different value of <code>:in-reply-to</code>. 
   */
   public abstract Vector evaluateFinalMessages(Vector messages);
}











