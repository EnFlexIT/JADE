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
* This abstract behaviour implements the Fipa Query Interaction Protocol
* from the point of view of the agent initiating the protocol, that is the
* agent that sends the Query-ref to a set of agents.
* In order to use correctly this behaviour, the programmer should do the following:
* <ul>
* <li> implements a class that extends FipaQueryInitiatorBehaviour.
* This class must implement 2 methods that are called by FipaQueryInitiatorBehaviur:
* <ul>
* <li> <code> public void handleOtherMessages(ACLMessage msg) </code>
* to handle all received messages different from "inform" message with
* the value of <code>:in-reply-to</code> parameter set (fixed) correctly
* <li> <code> public void handleInformeMessages(Vector messages) </code>
* to handle the "inform" messages received 
* </ul>* <li> create a new instance of this class and add it to the agent (agent.addBehaviour())
* </ul>
* <p>
*/
public abstract class FipaQueryInitiatorBehaviour extends SimpleBehaviour {



  /* This is the query-refMsg sent in the first state of the protocol */
  protected ACLMessage queryMsg;

  private int state = 0;  // state of the protocol
  long timeout;
  MessageTemplate template;
  Vector msgInforms = new Vector(); // vector of the inform ACLMessages received
  Vector msgFinalAnswers = new Vector(); // vector with the ACLMessages to send at the end of the protocol
  AgentGroup informerAgents;
  private AgentGroup waitedAgents;
  /**
   * this variable should be set to true when the behaviour should terminate
   */
  public boolean finished; // true when done()
  /**
   * default timeout in milliseconds to wait for proposals.
   * This timeout is overriden by
   * the reply-by parameter of the <code>query-ref</code> message, if set.
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
   * @param msg is the Query-ref message to be sent
   * @param group is the group of agents to which the query-ref must be sent
   */
    public FipaQueryInitiatorBehaviour(Agent a, ACLMessage msg, AgentGroup group) {
      this(a);
      queryMsg = msg;
      informerAgents = (AgentGroup)group.clone();
    }


  /** 
   * Constructor.
   * @param a is the <code>Agent</code> that runs the behaviour.
   */
   public FipaQueryInitiatorBehaviour(Agent a){ // default constructor
     super(a);
     queryMsg = new ACLMessage("query-ref");
     informerAgents = new AgentGroup(); 
     finished = true;
   }

  /**
   * action method of the behaviour. This method cannot be overriden by
   * subclasses because it implements the actual FipaQuery protocol
   */
  final public void action() {
    switch (state) {
    case 0: {
      /* This is executed only when the Behaviour is started*/
      state = 1;
      queryMsg.setType("query-ref");
      queryMsg.setProtocol("FIPA-Query");
      queryMsg.setSource(myAgent.getName());
      if (queryMsg.getReplyWith() == null)
      	queryMsg.setReplyWith("Query"+(new Date()).getTime());
      if (queryMsg.getConversationId() == null)
	      queryMsg.setConversationId("Query"+(new Date()).getTime());
      timeout = queryMsg.getReplyByDate().getTime()-(new Date()).getTime();
      if (timeout <= 1000) timeout = DEFAULTTIMEOUT; // at least 1 second
      //start a thread with the timeout
      wakeMsg = new ACLMessage("inform");
      wakeMsg.setReplyTo(queryMsg.getReplyWith());
      wakeMsg.setConversationId("WAKEUP"+queryMsg.getReplyWith());
      wakeMsg.setContent("(timeout " + timeout + ")");
      waker = new Waker(myAgent,wakeMsg,timeout);
      waker.start();

    	myAgent.send(queryMsg,informerAgents);

      template = MessageTemplate.MatchReplyTo(queryMsg.getReplyWith());
      waitedAgents = (AgentGroup)informerAgents.clone();
      //System.err.println("FipaQueryInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      break;
    }
    case 1: { // waiting for "inform"
      // remains in this state
      ACLMessage msg=myAgent.receive(template);

      if (msg == null) {
       	block();
       	return;
      }
      if (msg.getConversationId() != null)
         if (msg.getConversationId().equalsIgnoreCase(wakeMsg.getConversationId())) {
     	        // wake-up message
              state = 2;
              break;
         }


      //System.err.println("FipaQueryInitiatorBehaviour: receive");
//      msg.dump();

      waitedAgents.removeMember(msg.getSource());
      //System.err.println("FipaQueryInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      if (!waitedAgents.getMembers().hasMoreElements()) {
	      waker.stop();
      	state=2;
      }
      if (msg.getType().equalsIgnoreCase("inform")) {
        // msg contains an inform ACLMessage
    	    msgInforms.addElement(msg);
      } else	handleOtherMessages(msg);
      break;
    }
    case 2: {
      handleInformMessages(msgInforms);
     	finished = true;
      break;
    }
    } // end of switch
  } // end of action()


  public boolean done() {
    return finished;
  }


  /**
   * This method must be implemented by all subclasses.
   * After having sent the <code> query-ref </code> message, the base class calls
   * this method everytime a new message arrives that is not an <code> inform
   * </code> message.
   * The method should react to this message in an
   * implementation-dependent way. The instruction
   * <code> finished=true; </code> should be executed to finish the
   * query protocol.
   * The class variable <code>myAgent </code> can be used to send
   * messages or, after casting, to execute other implementation-dependent
   * methods that belongs to the actual Agent object.
   * @param msg is the ACLMessage just arrived
   */
  public abstract void handleOtherMessages(ACLMessage msg);



  /**
   * After having sent the <code>queryMsg</code> messages,
   * the protocol waits for the maximum timeout specified in those messages
   * (reply-by parameter), or until all the answers are received.
   * If no reply-by parameter was set, <code>
   * DEFAULTTIMEOUT</code> is used, instead.
   * After this timeout, this method is called to react to all the received
   * messages.
   * @param messages is the Vector of ACLMessage received so far
   */
   public abstract void handleInformMessages(Vector messages);
}

