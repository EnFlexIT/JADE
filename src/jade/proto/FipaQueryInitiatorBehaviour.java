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
import jade.util.leap.*;

import java.io.*;

import java.util.Vector;
import java.util.Date;


/**
* This abstract behaviour implements the Fipa Query Interaction Protocol
* from the point of view of the agent initiating the protocol, that is the
* agent that sends the query-ref/query-if to a set of agents.
* In order to use correctly this behaviour, the programmer should do the following:
* <ul>
* <li> implements a class that extends FipaQueryInitiatorBehaviour.
* This class must implement 2 methods that are called by FipaQueryInitiatorBehaviur:
* <ul>
* <li> <code> public void handleOtherMessages(ACLMessage msg) </code>
* to handle all received messages different from "inform" message with
* the value of <code>:in-reply-to</code> parameter set (fixed) correctly
* <li> <code> public void handleInformeMessages(Vector messages) </code>
* to handle the "inform" messages received (eventually no message)
* </ul> <li> create a new instance of this class and add it to the agent (agent.addBehaviour())
* </ul>
* <p>
* @author Fabio Bellifemine - CSELT S.p.A
* @version $Date$ $Revision$
* @deprecated
*/


public abstract class FipaQueryInitiatorBehaviour extends SimpleBehaviour {



  /** 
  * This is the query-refMsg sent in the first state of the protocol 
  * @serial
  */
  protected ACLMessage queryMsg;

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
  private Vector msgInforms = new Vector(); // vector of the inform ACLMessages received
  /**
  @serial
  */
  private Vector msgFinalAnswers = new Vector(); // vector with the ACLMessages to send at the end of the protocol
  /**
  @serial
  */
  private List informerAgents;
  /**
  @serial
  */
  private List waitedAgents;
  /**
  @serial
  */
  private boolean finished;


//__BACKWARD_COMPATIBILITY__BEGIN
  /**
   * constructor of the behaviour.
   * @param a is the current agent. The public variable
   * <code> Agent myAgent </code> contains then the pointer to the agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and use the methods of the extended class.
   * For instance
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments() </code>
   * @param msg is the Query message to be sent (notice that the performative
   * must be set to <code>QUERY-IF</code> or <code>QUERY-REF</code>
   * @param responders is the group of agents 
   * (i.e. a <code>List</code> of <code>AID</code>)
   * to which the query must be sent
   */
    public FipaQueryInitiatorBehaviour(Agent a, ACLMessage msg, java.util.List responders) {
      this(a,msg);
      queryMsg.clearAllReceiver();
      informerAgents =new ArrayList();
      for (int i=0; i<responders.size(); i++) { 
	queryMsg.addReceiver((AID)responders.get(i));
	informerAgents.add((AID)responders.get(i));
      }
    }
//__BACKWARD_COMPATIBILITY__END

  /**
   * constructor of the behaviour.
   * @param a is the current agent. The public variable
   * <code> Agent myAgent </code> contains then the pointer to the agent class.
   * A common usage of this variable is to cast it to the actual type of
   * Agent class and use the methods of the extended class.
   * For instance
   * <code>appointments = (AppointmentAgent)myAgent.getAppointments() </code>
   * @param msg is the Query message to be sent (notice that the performative
   * must be set to <code>QUERY-IF</code> or <code>QUERY-REF</code>
   * @param responders is the group of agents 
   * (i.e. a <code>List</code> of <code>AID</code>)
   * to which the query must be sent
   */
    public FipaQueryInitiatorBehaviour(Agent a, ACLMessage msg, List responders) {
      this(a,msg);
      queryMsg.clearAllReceiver();
      informerAgents =new ArrayList();
      for (int i=0; i<responders.size(); i++) { 
	queryMsg.addReceiver((AID)responders.get(i));
	informerAgents.add((AID)responders.get(i));
      }
    }

    /**
    * constructor of the behaviour.
    * In this case the set of agents to which the message is sent is
    * exactly the receivers set in the passed ACLMessage.
    * @see #FipaQueryInitiatorBehaviour(Agent , ACLMessage , List )
    */
public FipaQueryInitiatorBehaviour(Agent a, ACLMessage msg) {
  super(a);
  queryMsg = msg;
  finished = false;
  informerAgents =new ArrayList();
  Iterator i=msg.getAllReceiver();
  while (i.hasNext())
    informerAgents.add(i.next());
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
      //queryMsg.setType("query-ref");
      queryMsg.setProtocol("FIPA-Query");
      queryMsg.setSender(myAgent.getAID());
      if (queryMsg.getReplyWith() == null)
      	queryMsg.setReplyWith("Query"+myAgent.getLocalName()+(new Date()).getTime());
      if (queryMsg.getConversationId() == null)
	      queryMsg.setConversationId("Query"+myAgent.getLocalName()+(new Date()).getTime());
      if (queryMsg.getReplyByDate() != null){
	    	timeout = queryMsg.getReplyByDate().getTime() - System.currentTimeMillis();
      	if (timeout <= 0) //DUBBIO: perche` se la risposta e` richiesta entro un secondo questo requisito viene ignorato?
      		timeout = 1; // Minimum default timeout
      }
      else 
      	timeout = -1; // infinite timeout
      endingTime = System.currentTimeMillis() + timeout; // Has no meaning if timeout is < 0
      //      System.err.println("FipaQueryInitiatorBehaviour: timeout="+timeout+" endingTime="+endingTime+" currTime="+System.currentTimeMillis());
      myAgent.send(queryMsg);

      template = MessageTemplate.MatchInReplyTo(queryMsg.getReplyWith());
      waitedAgents = informerAgents;
      //      System.err.println("FipaQueryInitiatorBehaviour: waitedAgents="+waitedAgents.toString());
      break;
    }
    case 1: { // waiting for "inform"
      // remains in this state until all the inform arrive or timeout expires
      ACLMessage msg=myAgent.receive(template);
      if (msg == null) {
				if (timeout > 0) {
	  			blockTime = endingTime - System.currentTimeMillis();
	  			//	  System.err.println("FipaQueryInitiatorBehaviour: timeout="+timeout+" endingTime="+endingTime+" currTime="+System.currentTimeMillis()+" blockTime="+blockTime);
	  			if (blockTime <= 0) { //timeout expired
	    			state=2;
	    			return;
	  			} 
	  			else {
	    			block(blockTime);
	    			return;
	  			}
				} 
				else { // query without timeout
	  			block();
	  			return;
				}
      }

      // a new message is arrived and must be processed.		
      
      // remove the sender of this message from the list of responders that have not yet responded
      waitedAgents.remove(msg.getSender());
      // if all responders have already responded then go into the next state
      if (waitedAgents.size()==0) 
      	state=2;
      
      if (msg.getPerformative() == ACLMessage.INFORM) 
        // add msg to the vector of the received inform messages
	      msgInforms.addElement(msg);
      else	
      	handleOtherMessages(msg);
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
   * If no reply-by parameter was set, an infinite timeout
   * is used, instead.
   * After this timeout, this method is called to react to all the received
   * INFORM messages.
   * Notice that the method might be called even when the timeout is expired
   * but no INFORM message is yet arrived; in such a case the size of the 
   * <code>Vector</code>parameter is 0. Therefore, this method is always
   * called to handle the last state of the protocol. The condition
   * <code>messages.size()==0</code> allows to discriminate between
   * timeout elapsed / all responders have sent an answer (not necessarily
   * an INFORM answer!).
   * @param messages is the Vector of ACLMessage received so far
   */
   public abstract void handleInformMessages(Vector messages);
}

