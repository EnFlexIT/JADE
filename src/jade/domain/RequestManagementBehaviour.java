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

package jade.domain;

import jade.core.Agent;
import jade.core.CaseInsensitiveString;

import jade.content.onto.OntologyException;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.basic.Action;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.proto.SimpleAchieveREResponder;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.UnsupportedValue;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.security.AuthException;
import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
   Base class for AMS and DF behaviours managing requests from agents.
   This class handles the FIPA-request protocol and in particular prepares
   the response taking into account all possible exceptions.
   The preparation of the result notification is delegated to subclasses as
   its form (RESULT or DONE) and sending time (i.e. whether it can be sent 
   immediately or must be delayed at a later time) depends on the specific 
   action.
   @author Giovanni Caire - Tilab
*/

abstract class RequestManagementBehaviour extends SimpleAchieveREResponder {
	private ACLMessage notification;

  protected RequestManagementBehaviour(Agent a, MessageTemplate mt){
		super(a,mt);
  }
 
  protected abstract ACLMessage performAction(Action slAction, ACLMessage request) throws AuthException, FIPAException; 

  /**
   */
  protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
  	//System.out.println("Request received");
  	//System.out.println(request);		
  	ACLMessage response = request.createReply();
		try{	
			// Check the language is SL0, SL1, SL2 or SL. 
	    isAnSLRequest(request);

	    // Extract the content
	    Action slAction = (Action) myAgent.getContentManager().extractContent(request);
	    
	    // Perform the action
	    notification = performAction(slAction, request);
	    
	    // Action OK --> AGREE
	    response.setPerformative(ACLMessage.AGREE);
	    response.setContent(request.getContent()+" (true)");
		} 
		catch (OntologyException oe) {
			// Error decoding request --> NOT_UNDERSTOOD
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			response.setContent("("+ExceptionVocabulary.UNRECOGNISEDVALUE+" content)");
		}	
		catch (CodecException ce) {
			// Error decoding request --> NOT_UNDERSTOOD
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			response.setContent("("+ExceptionVocabulary.UNRECOGNISEDVALUE+" content)");
		}	
		catch (RefuseException re) {
			// RefuseException thrown during action execution --> REFUSE
			response.setPerformative(ACLMessage.REFUSE);
			response.setContent(request.getContent()+" ("+re.getMessage()+")");
		}	
		catch (FailureException fe) {
			// FailureException thrown during action execution --> AGREE+FAILURE
			response.setPerformative(ACLMessage.AGREE);
			response.setContent(request.getContent()+" (true)");
			notification = request.createReply();
			notification.setPerformative(ACLMessage.FAILURE);
			notification.setContent(request.getContent()+" ("+fe.getMessage()+")");
		}	
		catch(FIPAException fe){
			// Malformed request --> NOT_UNDERSTOOD
			response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			response.setContent("("+fe.getMessage()+")");
		}
		catch(Throwable t){
			t.printStackTrace();
			// Generic error --> AGREE+FAILURE
			response.setPerformative(ACLMessage.AGREE);
			response.setContent(request.getContent()+" (true)");
			notification = request.createReply();
			notification.setPerformative(ACLMessage.FAILURE);
			notification.setContent(request.getContent()+" ("+ExceptionVocabulary.INTERNALERROR+" \""+t.getMessage()+"\")");
		}
  	//System.out.println("Response is");		
  	//System.out.println(response);		
		return response;
  }
    
  /**
     Just return the (already prepared) notification message.
   */
  protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{	
  	//System.out.println("Notification is");		
  	//System.out.println(notification);		
		return notification;       
  }
    
  //to reset the action
  public void reset(){
		super.reset();
		notification = null;
  }

  private void isAnSLRequest(ACLMessage msg) throws FIPAException { 
		String language = msg.getLanguage();
		if ( (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL0, language)) &&
	  	   (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL1, language)) &&
	    	 (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL2, language)) &&
	     	 (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL, language))) {
			throw new UnsupportedValue("language");
    }
  }
}