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

//#MIDP_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.CaseInsensitiveString;

import jade.proto.SimpleAchieveREResponder;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


//import jade.content.onto.basic.Action;
//import jade.content.onto.basic.TrueProposition;
//import jade.content.onto.basic.DonePredicate;
// NEW ONTOLOGY
import jade.content.onto.basic.Action;
import jade.content.onto.basic.TrueProposition;
import jade.content.onto.basic.Done;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.ContentElementList;

import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.UnrecognisedValue;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.UnexpectedAct;
import jade.domain.FIPAAgentManagement.UnsupportedValue;

import jade.util.leap.List;
import jade.util.leap.ArrayList;

//this class can be used to define a DFResponder using the SimpleAchiveREResponder.
//It provides some utility method for filling the content of the ACLMessage.
//The user will have to define the method: prepareResponse and prepareResultNotification.

class DFResponderBehaviour extends SimpleAchieveREResponder{



    DFResponderBehaviour(Agent agent, MessageTemplate mt){
	super(agent, mt);
	//myAgent = agent;
    }

    /**
     * check that the passed parameter is a valid request, i.e.
     * the performative is REQUEST and the content language is one between SL,SL0,SL1,SL2
     * @throws UnexpectedAct if the performative is not REQUEST
     * @throws UnsupportedValue if the content language is not a profile of SL 
     **/
    protected void isAnSLRequest(ACLMessage msg) throws UnsupportedValue{
	
	String language = msg.getLanguage();
	if ( (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL0, language)) &&
	     (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL1, language)) &&
	     (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL2, language)) &&
	     (!CaseInsensitiveString.equalsIgnoreCase(FIPANames.ContentLanguage.FIPA_SL, language)))
	throw new UnsupportedValue("language");
	//FIXME: otherwise set to FIPA_SL.
    }

    /**
     * Create the content for the AGREE message
     * @param a is the action that has been agreed to perform
     * @param language the language used to express the content.
     * @param ontology the ontology used.
     * @return a String with the content ready to be set into the message
     **/
    protected String createAgreeContent(Action a,String language, String ontology) {
	ACLMessage temp = new ACLMessage(ACLMessage.AGREE); 
	temp.setLanguage(language);
	temp.setOntology(ontology);
	ContentElementList l = new ContentElementList();
	l.add(a);
	l.add(new TrueProposition());
	try {
	    myAgent.getContentManager().fillContent(temp,l);
	} catch (Exception ee) { // in any case try to return some good content
	    return "(( true ))";
	} 
	return temp.getContent();
    }

     /**
     * Create the content for the INFORM done message
     * @param a is the action that has been performed
     * @param language the language used to express the content.
     * @param ontology the ontology used.
     * @return a String with the content ready to be set into the message
     **/
    protected String createInformDoneContent(Action a,String language, String ontology) {
	ACLMessage temp = new ACLMessage(ACLMessage.INFORM); 
	temp.setLanguage(language);
	temp.setOntology(ontology);
	Done d = new Done();
	d.setAction(a);
	try {
	    myAgent.getContentManager().fillContent(temp,d);
	} catch (Exception e) {
	    return "( (done unknownAction) )";
	}
	return temp.getContent();
    }


  /**
     * Create the content for a so-called "exceptional" message, i.e.
     * one of NOT_UNDERSTOOD, FAILURE, REFUSE message
     * @param a is the Action that generated the exception
     * @param e is the generated Exception
     * @return a String containing the content to be sent back in the reply
     * message; in case an exception is thrown somewhere, the method
     * try to return anyway a valid content with a best-effort strategy
     **/
    protected void createExceptionalMsgContent(Action a, FIPAException e, ACLMessage request) {
	String cont;
	if (e instanceof NotUnderstoodException) { // the object of the exception is the ACLMessage received
	    cont = "( (action "+request.getSender()+" "+request+") "+e.getMessage()+")";
	} else { // the object of the exception is the content of the ACLMessage received
	    if (a == null) {
				cont = "( (action "+request.getSender()+" UnknownAction) "+e.getMessage()+")";
	    }
	    else {
	    	ACLMessage temp = new ACLMessage(ACLMessage.NOT_UNDERSTOOD); 
	    	temp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
	    	temp.setOntology(request.getOntology());
	    	ContentElementList l = new ContentElementList();
	    	l.add(a);
	    	l.add(e);
	    	try {
					myAgent.getContentManager().fillContent(temp,l);
	    		cont = temp.getContent();
	    	} 
	    	catch (Exception ee) {
					cont = "( (action "+request.getSender()+" UnknownAction) "+e.getMessage()+")";
	    	} 
	    }
	}
	e.setMessage(cont);
    }
}
