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
package examples.protocols;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.*;

/**
* This agent acts as a  responder for the FIPARequest and FIPAQuery protocol
* The example shows how to write two  simple the FIPARequest protocol and  the FIPAQuery Protocol
using the <code>AchiveREResponder<code> behaviour.
* If a REQUEST message arrives the agent could reply in one of the following way:
<UL>
* <li> send an AGREE message and then an INFORM message
* <li> send an AGREE message and then a FAILURE message
* <li> send an AGREE message and then no message 
* <li> send an AGREE message and then an out of sequence message
* <li> send an INFORM message
* <li> send a REFUSE message
* <li> send a NOT_UNDERSTOD message
* <li> send an out of sequence message
* <li> send no message
* </ul>
* If QUERY-IF or QUERY-REF message arrive the agent could reply in one of the following way:
* <ul>
* <li> send a NOT_UNDERSTOOD message
* <li> send a REFUSE message
* <li> send a FAILURE message
* <li> send an INFORM message
* <li> send an out of sequence message
* <li> send no message
* </ul> 
* This agent can be use together with an <code>Initiator</code> agent to see an agent conversation 
* using random the FIPARequest or FIPAQuery protocol.
*
* @see jade.proto.AchieveREInitiator
* @see jade.proto.AchieveREResponder
* @author Tiziana Trucco - Telecom Italia Lab S.p.A
* @version $Date$ $Revision$
**/


public class Responder extends Agent {
    
    boolean sendingAgree = false;
	
    public void setup() {
	
	Behaviour requestB = new MyRequestResponder(this, AchieveREResponder.createMessageTemplate(FIPAProtocolNames.FIPA_REQUEST));
	addBehaviour(requestB);
	
	Behaviour queryB = new MyQueryResponder(this,AchieveREResponder.createMessageTemplate(FIPAProtocolNames.FIPA_QUERY));
	addBehaviour(queryB);
	
    }
    
    /**
       Inner class MyRequestResponder
    */
    class MyRequestResponder extends AchieveREResponder {
	
	public MyRequestResponder(Agent a, MessageTemplate mt) {
	    super(a, mt, null);
	}
	
	protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException, NotUnderstoodException {
	    
	    double chance = Math.random();
	    
	    double range = 1.0 / 6.0;
	    ACLMessage response = request.createReply();
	    
	    if(chance < range ){
		//send a NOT UNDERSTOOD
		System.out.println( myAgent.getLocalName() + " --> is  sending a NOT_UNDERSTOOD response in RequestProtocol" );
		response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	    }else if(chance <(range * 2.0)){
		//send a REFUSE	   
		System.out.println( myAgent.getLocalName() +  " --> is sending a REFUSE response in RequestProtocol" );
		response.setPerformative(ACLMessage.REFUSE);
	    }else if (chance < (range * 3.0)){
		//send an AGREE
		System.out.println( myAgent.getLocalName() +  " --> is sending an AGREE response in RequestProtocol" );
		response.setPerformative(ACLMessage.AGREE);
		sendingAgree = true;
	    }else if (chance < (range * 4.0)){
		//send an out of sequence Message
		System.out.println( myAgent.getLocalName() +  " --> is sending an out of sequence response in RequestProtocol" );
		response.setPerformative(ACLMessage.SUBSCRIBE);
	    }else if(chance <(range * 5.0)){
		//send an INFORM
		System.out.println( myAgent.getLocalName() +  " --> is sending an INFORM response in RequestProtocol" );
		response.setPerformative(ACLMessage.INFORM);
	    }else{
		//check the time out expiration in initiator. 
		System.out.println( myAgent.getLocalName() +  " --> is sending NO response in RequestProtocol" );
		response = null;
	    }
	    return response;	
	}
	
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
	    
	    double chance = Math.random();
	    
	    ACLMessage resNot = request.createReply();
	    
	    if(sendingAgree){
		if(chance < 0.25){
		    //SENDING INFORM
		    System.out.println( myAgent.getLocalName() +  " --> is sending INFORM result in RequestProtocol");
		    resNot.setPerformative(ACLMessage.INFORM);
		}else if(chance < 0.50){
		    // sending FAILURE
		    System.out.println( myAgent.getLocalName() +  " --> is sending FAILURE result in RequestProtocol" );
		    resNot.setPerformative(ACLMessage.FAILURE);
		}else if(chance < 0.75){
		    //sending out of sequence message
		    System.out.println(  myAgent.getLocalName()+  " --> is sending out of sequence result in RequestProtocol" );
		    resNot.setPerformative(ACLMessage.SUBSCRIBE);
		}else{
		    // checking TIMEOUT into Initiator.
		    System.out.println(getLocalName() + " No result sent in ResultProtocol");
		    resNot = null;
		}
	    }else {
		// the inform message has been already sent.
		System.out.println(myAgent.getLocalName() + " The inform message has been already sent in RequestProtocol");
		resNot = null;
	    }
	    return resNot;
	}
    } // End of inner class MyRequestResponder
    
    
    /**
       Inner class MyQueryResponder
    */
    class MyQueryResponder extends AchieveREResponder {
	public MyQueryResponder(Agent a, MessageTemplate mt) {
	    super(a, mt, null);
	}
	
	protected ACLMessage prepareResponse(ACLMessage query) throws RefuseException, NotUnderstoodException {
	    
	    double chance = Math.random();
	    
	    double range = 1.0 / 6.0;
	    ACLMessage response = query.createReply();
	    
	    if(chance < range ){
		//send a NOT UNDERSTOOD
		System.out.println( myAgent.getLocalName() + " --> is  sending a NOT_UNDERSTOOD response in QueryProtocol" );
		response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	    }else if(chance <(range * 2.0)){
		//send a REFUSE
		System.out.println( myAgent.getLocalName() +  " --> is sending a REFUSE response in QueryProtocol" );
		response.setPerformative(ACLMessage.REFUSE);
	    }else if (chance < (range * 3.0)){
		//send an FAILURE
		System.out.println( myAgent.getLocalName() +  " --> is sending an FAILURE response in QueryProtocol" );
		response.setPerformative(ACLMessage.FAILURE);
	    }else if (chance < (range * 4.0)){
		//send an out of sequence Message
		System.out.println( myAgent.getLocalName() +  " --> is sending an out of sequence response in QueryProtocol" );
		response.setPerformative(ACLMessage.SUBSCRIBE);
	    }else if(chance <(range * 5.0)){
		//send an INFORM
		System.out.println( myAgent.getLocalName() +  " --> is sending an INFORM response in QueryProtocol" );
		response.setPerformative(ACLMessage.INFORM);
	    }else{
		//check the time out expiration in initiator. 
		System.out.println( myAgent.getLocalName() +  " --> is sending NO response in QueryProtocol" );
		response = null;
	    }
	    return response;	
	}
	
    } // End of inner class MyQueryResponder
    
}//end of class Responder 
