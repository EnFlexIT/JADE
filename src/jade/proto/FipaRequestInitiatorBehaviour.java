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
  This behaviour plays the <em>Initiator</em> role in
  <code>fipa-request</code> protocol. This is an abstract class,
  defining an abstract method for each message type expected from a
  <code>fipa-request</code> interaction.
  @see jade.proto.FipaRequestResponderBehaviour
  
  
  @author Tiziana Trucco - CSELT S.p.A.
  @version $Date$ $Revision$
*/
public abstract class FipaRequestInitiatorBehaviour extends SimpleBehaviour {

	private final static int INITIAL_STATE = 0;
	private final static int FIRSTANSWER_STATE = 1;
	private final static int SECONDANSWER_STATE = 2;
	
	private ACLMessage reqMsg, firstAnswerMsg, secondAnswerMsg;
	private MessageTemplate reqTemplate, firstReqTemplate, secondReqTemplate;
	private int state = INITIAL_STATE;
	
	/**
	I metodi handle  può cambiare per bloccare il protocolo in qualunque stato
	
	verificare che si parli dello stato aggiuntivo e handleothermessages
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
   caling the constructor. If present, <code>:conversation-id</code>
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
  
  */
  public FipaRequestInitiatorBehaviour(Agent client, ACLMessage request){
  	this(client,request, null);
  
  }
  
  /**
  
  */
  public void reset(){
  	finished = false;
  	state = INITIAL_STATE; 
  }
    
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
  
  public void reset(ACLMessage request, MessageTemplate template){
  
  	reqTemplate = template;
  	reset(request);
  
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
  		
  		//FIXME: in next version take care also of the timeout expressed in the reply-by.
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
  				case ACLMessage.INFORM:{
  					// this new state has been added in Fipa99.
  				  finished = true;
  					handleAgree(firstAnswerMsg);
  					handleInform(firstAnswerMsg);
  					break;
  				}
  				default:{
  					handleOtherMessages(firstAnswerMsg);
  					break;
  				}
  			}
  		
  		}
  		else
  			block();
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
  
  /*
  poiche lavora su var private della classe no param. cosa fa....
  */
  private void arrangeReqMsg(){
  
    // Set type and protocol for request
    reqMsg.setPerformative(ACLMessage.REQUEST);
    reqMsg.setProtocol("fipa-request");
		if (reqMsg.getReplyWith().length()<1)
      	reqMsg.setReplyWith("Req"+(new Date()).getTime());
    if (reqMsg.getConversationId().length()<1)
	      reqMsg.setConversationId("Req"+(new Date()).getTime());

  }
  
  /**
  poiche lavora su var private della classe no param. cosa fa....
  must be called after arrangereqMsg() otherwise the generated template is uncorret
  */
  private void arrangeReqTemplate(){
  	
  	  secondReqTemplate = MessageTemplate.MatchProtocol("fipa-request");
  		if (reqTemplate != null)  		
  			secondReqTemplate = MessageTemplate.and(reqTemplate, secondReqTemplate);
 			// converation-id and reply-with are forced to be present 
  		// by the method arrageReqMsg. 
      secondReqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(reqMsg.getConversationId()),
  																			secondReqTemplate);
			firstReqTemplate = MessageTemplate.and(MessageTemplate.MatchReplyTo(reqMsg.getReplyWith()),
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
  di default manda solo un msg di warning su stderr ma specifiche implemtnazioni posso fare override
  */
  protected void handleOtherMessages(ACLMessage reply){
  	System.err.println(myAgent.getLocalName() + " WARNING: not expected message received during fipa-request protocol, initiator role"); 
  }


}