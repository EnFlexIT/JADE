package test.proto;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.*;

/**
This agent waits for a REQUEST message and replies random accoding to the following rules:
- send a AGREE message and the a INFORM message
- send an AGREE message and then a FAILURE message
- send a not Understood message
- send a REFUSE message.
- timeout
 **/

public class FIPARequestResponderTest extends Agent {
    
    boolean sendingAgree = false;
	
    public void setup() {
	MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
  
	Behaviour b = new MyResponder(this, AchieveREResponder.createMessageTemplate(FIPAProtocolNames.FIPA_REQUEST));
	addBehaviour(b);
    }
	
	/**
	   Inner class MyResponder
	 */
    class MyResponder extends AchieveREResponder {
	public MyResponder(Agent a, MessageTemplate mt) {
	    super(a, mt, null);
	}
	
	protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException, NotUnderstoodException {
	    
		double chance = Math.random();
	
		//double chance = 0.4;
	    double range = 1.0 / 6.0;
	    ACLMessage response = request.createReply();
	    System.out.println( "prepareResponse Method of Agent: " + myAgent.getLocalName());
	    if(chance < range ){
		//send a NOT UNDERSTOOD
		System.out.println( "change: " + chance +  " --> sending a NOT_UNDERSTOOD message" );
		response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	    }else if(chance <(range * 2.0)){
		//send a REFUSE
		System.out.println( "change: " + chance +  " --> sending a REFUSE message" );
		response.setPerformative(ACLMessage.REFUSE);
	    }else if (chance < (range * 3.0)){
		//send an AGREE
		System.out.println( "change: " + chance +  " --> sending an AGREE message" );
		response.setPerformative(ACLMessage.AGREE);
		sendingAgree = true;
	    }else if (chance < (range * 4.0)){
		//send an out of sequence Message
		System.out.println( "change: " + chance +  " --> sending an out of sequence message" );
		response.setPerformative(ACLMessage.SUBSCRIBE);
	    }else if(chance <(range * 5.0)){
		//send an INFORM
		System.out.println( "change: " + chance +  " --> sending an INFORM message" );
		response.setPerformative(ACLMessage.INFORM);
	    }else{
		//check the time out expiration in initiator. 
		System.out.println( "change: " + chance +  " --> sending NO message" );
		response = null;
	    }
	    return response;	
	}
		
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
	    double chance = Math.random();
		//double chance = 0.80;
	    ACLMessage resNot = request.createReply();
	    System.out.println( "prepareResultNotification Method of Agent: " + myAgent.getLocalName());
	    if(sendingAgree){
		if(chance < 0.25){
		    //SENDING INFORM
		    System.out.println( "change: " + chance +  " --> sending INFORM message" );
		    resNot.setPerformative(ACLMessage.INFORM);
		}else if(chance < 0.50){
		    // sending FAILURE
		    System.out.println( "chance: " + chance +  " --> sending FAILURE message" );
		    resNot.setPerformative(ACLMessage.FAILURE);
		}else if(chance < 0.75){
		    //sending out of sequence message
		    System.out.println( "chance: " + chance +  " --> sending out of sequence message" );
		    resNot.setPerformative(ACLMessage.SUBSCRIBE);
		}else{
		    // checking TIMEOUT into Initiator.
			System.out.println(getLocalName() + " No message sent");
		    resNot = null;
		}
	    }else {
		// the inform message has been already sent.
		System.out.println("The inform message has been already sent");
		resNot = null;
	    }
	    return resNot;
	}
    } // End of inner class MyResponder

}//end of class FIPARequestResponderTest 
