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

import java.util.Date;
import jade.core.*;
import jade.core.behaviours.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** 
  Behaviour class for <code>fipa-request</code><em> Initiator</em> role.This abstract 
  behaviour implements the <code>fipa-request</code> interaction protocol from the point 
  of view of the agent initiating the protocol, that is the agent that sends the
  <code>request</code> message to an agent.
  In order to use correctly this behaviour, the programmer shoud do the following:<ul>
  
  <li> Implement a class that extends <code>FipaRequestInitiatorBehaviour</code>. This class
  must implements five methods that are called by <code>FipaRequestInitiatorBehaviour</code>:<ul>
  
  <li> <code>protected handleAgree(ACLMessage msg)</code> to handle the <code>agree</code> reply. 
  <li> <code>protected handleInform(ACLMessage msg)</code> to handle the <code>inform</code> message received from 
  the peer agent.
  <li> <code>protected handleNotUnderstood(ACLMessage msg)</code> to handle the <code>not-understood</code> reply.
  <li> <code>protected handleFailure(ACLMessage msg))</code>to handle the <code>failure</code> reply.
  <li> <code>protected handleRefuse(ACLMessage msg)</code> to handle the <code>refuse</code> reply. 
  </ul>
  <li> Create a new instance of this class and add it to the agent with <code>Agent.addBehaviour()</code>
  method.
  </ul>
  <p>
  The behaviour can be hot reset, calling one of the following method in one of the handle methods 
  other than from <code>handleAgree</code>:
  <ul>
  <li> <code>reset()</code> to simply restart the protocol with the same ACLMessage and MessageTemplate.
  <li> <code>reset(ACLMessage request)</code> to restart the protocol with a new ACLMessage;
  <li> <code>reset(ACLMessage request, MessageTemplate template)</code> to restart the protocol 
  with new ACLMessage and MessageTemplate. 
  </ul>
  The programmer can override the method <code> public handleOtherMessages(ACLMessage reply)</code> 
  in order to handle replies different from those stated by the protocol.
  
  The method <code>public long handleTimeOut()</code> can be override to handle 
  the  expiration of the timeout.
  
  @see jade.proto.FipaRequestResponderBehaviour
  
  @author Tiziana Trucco - CSELT S.p.A.
  @version $Date$ $Revision$
*/
public abstract class FipaRequestInitiatorBehaviour extends SimpleBehaviour {

	private final static int INITIAL_STATE = 0;
	private final static int FIRSTANSWER_STATE = 1;
	private final static int SECONDANSWER_STATE = 2;
	
	/**
	@serial
	*/
	private ACLMessage reqMsg, firstAnswerMsg, secondAnswerMsg;
	/**
	@serial
	*/
	private MessageTemplate reqTemplate, firstReqTemplate, secondReqTemplate;
	/**
	@serial
	*/
	private int state = INITIAL_STATE;
	
  /**
  @serial
  */
	private long timeout,blockTime, endingTime;

	/**
	Use this protected variable to block the protocol in whatever state.
	@serial
	*/
	protected boolean finished = false;
  


  /**
   Public constructor for this behaviour. Creates a
   <code>Behaviour</code> object that sends a <code>request</code> ACL
   message and calls user-defined methods to handle the different
   kinds of reply expected whithin a <code>fipa-request</code>
   interaction.
   @param client The agent this behaviour belongs to, that embodies
   <em>Initiator</em> role in this <code>fipa-request</code>
   interaction.
   @param request The <code>ACLMessage</code> object to be sent. When
   passed to this constructor, the message type is set to
   <code>request</code> and the <code>:protocol</code> slot is set to
   <code>fipa-request</code>, so there's no need to set them before
   calling the constructor. If present, <code>:conversation-id</code>
   and <code>:reply-with</code> slots are used for interaction
   labelling. Application programmer must ensure the following
   properties of <code>request</code> parameter when calling this
   constructor:
   <ol>
   <li> <code>request</code> has a valid <code>:receiver</code> slot value.
   <li> <code>request</code> has a valid <code>:content</code> slot value.
   <li> <code>request</code> has a valid <code>:language</code> slot value.
   <li> <code>request</code> has a valid <code>:ontology</code> slot value.
   </ol>
   However, only <code>:receiver</code> slot is actually used by this
   behaviour to send the message to the destination agent (only one
   receiver is supported by <code>fipa-request</code> protocol).
   @param template A <code>MessageTemplate</code> object used to match
   incoming replies. This behaviour automatically matches replies
   according to message type and <code>:protocol</code> slot value;
   also, <code>:conversation-id</code> and <code>:reply-to</code> slot
   values are matched when corresponding slot values are present in
   <code>request</code> parameter. This constructor argument can be
   used to match additional fields, such as <code>:language</code> and
   <code>:ontology</code> slots.
  */
  public FipaRequestInitiatorBehaviour(Agent client, ACLMessage request, MessageTemplate template) {
    super(client);

    // if request is null, clone throws an exception, we catch this exception
    // and create an empty request message. 
    try{
    	reqMsg = (ACLMessage)request.clone();
    }catch(Exception e){
    	reqMsg = new ACLMessage(ACLMessage.REQUEST);
    }
    
    reqTemplate = template;

  }
  
  /**
  Constructor for this behaviour.
  In this case the MessageTemplate is the default one.
  */
  public FipaRequestInitiatorBehaviour(Agent client, ACLMessage request){
  	this(client,request, null);
  
  }
  
  /**
  This method resets this behaviour so that it restarts from the initial 
  state of the protocol with the same request message.   
  */
  public void reset(){
  	finished = false;
  	state = INITIAL_STATE; 
	super.reset();
  }
    
  /**
  This method resets this behaviour so that it restarts the protocol with 
  another request message.
  @param request updates request message to be sent.
  */
  public void reset(ACLMessage request){
  
  	// if request is null, clone throws an exception, we catch this exception
    // and create an empty request message. 
  	try{
    	reqMsg = (ACLMessage)request.clone();
    }catch(Exception e){
    	reqMsg = new ACLMessage(ACLMessage.REQUEST);
    }
    reset();

  }
  /**
  This method resets this behaviour so that it restarts the protocol with
  other request message and MessageTemplate
  @param request update request message to be sent.
  @param template a new MessageTemplate.  
  */
  public void reset(ACLMessage request, MessageTemplate template){  
  	reqTemplate = template;
  	reset(request);
  }

  /**
     This method gives access to the <code>request</code> ACL message,
     originally set by the class constructor.
     @return The ACL <code>request</code> message sent by this
     behaviour.
   */
  protected ACLMessage getRequest() {
    return reqMsg;
  }


  public boolean done(){
  
  	return finished;
  }
  
  public void action(){
 
  switch(state){ 
    
  	case INITIAL_STATE:{
  		
  		arrangeReqMsg();
  		myAgent.send(reqMsg);
  		arrangeReqTemplate();
  		state = FIRSTANSWER_STATE;
  		break;
  	}
  	case FIRSTANSWER_STATE:{
  		
  		firstAnswerMsg = myAgent.receive(firstReqTemplate);
  		
  		if (firstAnswerMsg != null){
  			switch (firstAnswerMsg.getPerformative()) {
  				case ACLMessage.AGREE:{
  					state = SECONDANSWER_STATE;
  					handleAgree(firstAnswerMsg);
  					break;
  				}
  				case ACLMessage.REFUSE:{		
  					finished = true;
  					handleRefuse(firstAnswerMsg);
  					break;
  				}
  				case ACLMessage.NOT_UNDERSTOOD:{
  					finished = true;	
  					handleNotUnderstood(firstAnswerMsg);
  					break;  				
  				}
  				case ACLMessage.FAILURE:{ // agree is considered optional
  					finished = true;	
  					handleAgree(firstAnswerMsg); // agree is subsumed
  					handleFailure(firstAnswerMsg);
  					break;
  				}
  				case ACLMessage.INFORM:{
  					// This new state has been added in Fipa99.
  				  finished = true;
  					handleAgree(firstAnswerMsg); // agree is subsumed
  					handleInform(firstAnswerMsg);
  					break;
  				}
  				default:{
  					handleOtherMessages(firstAnswerMsg);
  					break;
  				}
  			}
  		
  		}
  		else{
  			if (timeout>0){
  				blockTime = endingTime - System.currentTimeMillis();
  				if (blockTime <=0)
  				{// timeout expired
  				
  					timeout = handleTimeout(); //default: infinite timeout
  					state = FIRSTANSWER_STATE;
  					return;
  				}
  				else{
  				
  				  block(blockTime);
  				  return;
  				}
  			}
  			else{//request without timeout
  				block();
  				}
  		
  		}
  		break;
  	}
  	case SECONDANSWER_STATE:{
  		
  	//FIXME: in next version take care also of the timeout expressed in the reply-by.
  		secondAnswerMsg = myAgent.receive(secondReqTemplate);
  		if (secondAnswerMsg != null){
  			switch (secondAnswerMsg.getPerformative()) {
  			  			
  				case ACLMessage.INFORM:{
  					finished = true;	
  					handleInform(secondAnswerMsg);
  					break;
  				}
  				case ACLMessage.FAILURE:{
  					finished = true;	
  					handleFailure(secondAnswerMsg);
  					break;
  				}
  				default:{
  					handleOtherMessages(secondAnswerMsg);
  					break;
  				}
  			}//switch
  		} //if
  		else
  			block();
  		break;

  	}//case
  }
  }
  
  /**
  This private method works on private variables of the class so it has no parameters.
  It intializes the ACLMessage, setting the performative field to "request", and the protocol field 
  to "fipa-request",
  If the ReplyWith and ConversationId fields have not been set, it initializes them.
  If no timeout is specified it will be set to infinite.
  */
  private void arrangeReqMsg(){

    // Set type and protocol for request
    reqMsg.setPerformative(ACLMessage.REQUEST);
    reqMsg.setProtocol("fipa-request");
    if ((reqMsg.getReplyWith() == null) || reqMsg.getReplyWith().length()<1)
      	reqMsg.setReplyWith("Req"+(new Date()).getTime());
    if ((reqMsg.getConversationId() == null) || reqMsg.getConversationId().length()<1)
	      reqMsg.setConversationId("Req"+(new Date()).getTime());

    Date d = reqMsg.getReplyByDate();
    if(d != null) {
      timeout = d.getTime() - (new Date()).getTime();
    }
    else
      timeout = -1;
    endingTime = System.currentTimeMillis() + timeout;	
  }
  
  /**
  This  private method works on private variable of the class so it has no parameters.
  It initializes the MessageTemplates used by the receive methods in the <code>action</code> method.
  It must be called after arrangeReqMsg() otherwise the generated template is incorret.
  */
  private void arrangeReqTemplate(){
  	
  	  secondReqTemplate = MessageTemplate.MatchProtocol("fipa-request");
  		if (reqTemplate != null)  		
  			secondReqTemplate = MessageTemplate.and(reqTemplate, secondReqTemplate);
		// conversation-id and reply-with are forced to be present 
  		// by the method arrageReqMsg. 
      secondReqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(reqMsg.getConversationId()),
  	                                          secondReqTemplate);
			firstReqTemplate = MessageTemplate.and(MessageTemplate.MatchInReplyTo(reqMsg.getReplyWith()),
				                                     secondReqTemplate);

  } 
  

  /**
    Abstract method to handle <code>not-understood</code>
    replies. This method must be implemented by
    <code>FipaRequestInitiatorBehaviour</code> subclasses to react to
    <code>not-understood</code> messages from the peer agent.
    @param reply The actual ACL message received. It is of
    <code>not-understood</code> type and matches the conversation
    template.
  */
  protected abstract void handleNotUnderstood(ACLMessage reply);

  /**
    Abstract method to handle <code>refuse</code> replies. This method
    must be implemented by <code>FipaRequestInitiatorBehaviour</code>
    subclasses to react to <code>refuse</code> messages from the peer
    agent.
    @param reply The actual ACL message received. It is of
    <code>refuse</code> type and matches the conversation
    template.
  */
  protected abstract void handleRefuse(ACLMessage reply);

  /**
    Abstract method to handle <code>agree</code> replies. This method
    must be implemented by <code>FipaRequestInitiatorBehaviour</code>
    subclasses to react to <code>agree</code> messages from the peer
    agent.
    @param reply The actual ACL message received. It is of
    <code>agree</code> type and matches the conversation
    template.
  */
  protected abstract void handleAgree(ACLMessage reply);

  /**
    Abstract method to handle <code>failure</code>
    replies. This method must be implemented by
    <code>FipaRequestInitiatorBehaviour</code> subclasses to react to
    <code>failure</code> messages from the peer agent.
    @param reply The actual ACL message received. It is of
    <code>failure</code> type and matches the conversation
    template.
  */
  protected abstract void handleFailure(ACLMessage reply);

  /**
    Abstract method to handle <code>inform</code> replies. This method
    must be implemented by <code>FipaRequestInitiatorBehaviour</code>
    subclasses to react to <code>inform</code> messages from the peer
    agent.
    @param reply The actual ACL message received. It is of
    <code>inform</code> type and matches the conversation
    template.
  */
  protected abstract void handleInform(ACLMessage reply);
  
  /**
  This method can be override to handle other received messages 
  different from those stated by the protocol.
  By default it prints a warning message. 
  */
  protected void handleOtherMessages(ACLMessage reply){
  	System.err.println(myAgent.getLocalName() + " WARNING: not expected message received during fipa-request protocol, initiator role"); 
  }
  
/**
This method is called by the action method when the timeout set in the request message
is expired. By default it returns -1 so that the agent will wait for an infinite timeout.
It can be override by the programmer to:
<li> update his own data base;
<li> call the <code>reset</code> to restart the protocol with different message or template;
<li> stop the protocol, setting <code>finished</code> = true;
<li> return an additional timeout;
*/

public long handleTimeout(){

	return -1;
}


}
