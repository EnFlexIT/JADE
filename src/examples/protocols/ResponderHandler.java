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
import java.io.*;
import jade.proto.states.*;

/**
* Simple example of a responder using the AchieveREResponder setting an HandlerSelector 
* for the prepareResultNotification state.
* 
* @see jade.proto.AchieveREInitiator
* @see jade.proto.AchieveREResponder
* @author Tiziana Trucco - Telecom Italia Lab S.p.A
* @version $Date$ $Revision$
**/


public class ResponderHandler extends Agent {
    
    //the action key used to store into the datastore the action found into the request msg.
    static final String ACTION_KEY= "action_name";
   
    AchieveREResponder requestB;
    
    public void setup() {
	
	requestB = new MyRequestResponder(this, AchieveREResponder.createMessageTemplate(FIPAProtocolNames.FIPA_REQUEST));
	
	//register an handler to manage  differents action.
	myHandler handler = new myHandler(this,requestB.getDataStore(),ACTION_KEY);
	
	//handler for the doThis Action
	DoThisBehaviour doThisB = new DoThisBehaviour(this);
	doThisB.setDataStore(requestB.getDataStore());
	
	//handler for the doThat Action
	DoThatBehaviour doThatB = new DoThatBehaviour(this);
	doThatB.setDataStore(requestB.getDataStore());
	
	handler.registerHandler(InitiatorHandler.DO_THIS_ACTION, doThisB);
	handler.registerHandler(InitiatorHandler.DO_THAT_ACTION, doThatB);
	
	requestB.registerPrepareResultNotification(handler);
	
	addBehaviour(requestB);
	
    }

    
    /**
       Inner class MyRequestResponder
    */
    class MyRequestResponder extends AchieveREResponder {

	
	public MyRequestResponder(Agent a, MessageTemplate mt) {
	    super(a, mt, null);
	}
	
	//get the action from the received REQUEST message and put it into the data store using the 
	//ACTION_KEY and then send an AGREE message.
	
	protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException, NotUnderstoodException {
	

	   //retrive action from the content of request msg received.
	   String action = request.getContent();
	   
	   getDataStore().put(ACTION_KEY,action);
	   
	   ACLMessage response = request.createReply(); 
	   response.setPerformative(ACLMessage.AGREE);
	   System.out.println("The agent: "+ myAgent.getLocalName() + " found the following action: "  + request.getContent()+ " into the request message");
	   System.out.println( myAgent.getLocalName() +  " --> is sending "+(response==null?"no":(response.getPerformative()==ACLMessage.SUBSCRIBE?"an out-of-sequence":ACLMessage.getPerformative(response.getPerformative())))+ " response to the protocol initiator." );
	   
	   return response;	
	}

	} // End of inner class MyRequestResponder
    
    

    // defines an handler for dispatching according to the action to different 
    //behaviours for preparing the result notification. 
    private class myHandler extends HandlerSelector{
    	//constructor of the class
    	myHandler(Agent a, DataStore s,Object accessKey){
    		super(a,s,accessKey);
    		}
    	
    	//retrived the action from the data store. 
    	protected Object getSelectionKey(Object selectionVar){
    		return selectionVar;
    	}
    	}//end class myHandler
    
    //behaviour for the do this action. It simply sends an INFORM which content is: "do this action done".	
    private class DoThisBehaviour extends OneShotBehaviour{
    	
    	DoThisBehaviour(Agent a){
    		super(a);
    		}
    		
    	public void action(){
    	
	    ACLMessage doThisReply = ((ACLMessage)getDataStore().get(requestB.REQUEST_KEY)).createReply();	
	    doThisReply.setPerformative(ACLMessage.INFORM);
	    doThisReply.setContent("do This Action done"); 
	    getDataStore().put(requestB.RESULT_NOTIFICATION_KEY,doThisReply);
    		
    		}
    	}//end class DoThisBehaviour
    
    //behaviour for the do that action. It symply sends	an INFORM which content is: "do that action done"
    private class DoThatBehaviour extends OneShotBehaviour{
    	
    	DoThatBehaviour(Agent a){
    		super(a);
    		}
    		
    	public void action(){
	  
	    ACLMessage doThatReply =((ACLMessage)getDataStore().get(requestB.REQUEST_KEY)).createReply();
	    doThatReply.setPerformative(ACLMessage.INFORM);
	    doThatReply.setContent("do That Action done"); 
	    getDataStore().put(requestB.RESULT_NOTIFICATION_KEY,doThatReply);
    		}
    	}//end class DoThatBehaviour
    			
}//end of class ResponderHandler 
