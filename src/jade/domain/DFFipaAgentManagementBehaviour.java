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

import jade.core.CaseInsensitiveString;


import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;

import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.Search;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.FIPAAgentManagement.Register;
import jade.domain.FIPAAgentManagement.Deregister;
import jade.domain.FIPAAgentManagement.Modify;
import jade.domain.FIPAAgentManagement.Unauthorised;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.UnrecognisedValue;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.NotRegistered;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;

class DFFipaAgentManagementBehaviour extends DFResponderBehaviour{

  
    private df myAgent;
    private Object action = null;
    // private String actionName = null;
    private Action SLAction = null; 

    private DFAgentDescription agentDescription = null;
    private SearchConstraints constraints = null;

    private int actionID = UNSUPPORTED;

    private final static int REGISTER = 0;
    private final static int DEREGISTER = 1;
    private final static int MODIFY = 2;
    private final static int SEARCH = 3;
    private final static int UNSUPPORTED = -1;
	

    protected DFFipaAgentManagementBehaviour(df a, MessageTemplate mt){
	super(a,mt);
	myAgent = a;
    }

    /*
    In this method we can be send : AGREE, NOT UNDERSTOOD and REFUSE.
    in this method we parse the content in order to know the action required to the DF.
    if the action is unsupported a REFUSE message is sent.
    if something went wrong with the ontology a NOT UDERSTOOD message is sent, 
    otherwise an AGREE is sent and the action will be performed.
    */
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException{

	isAnSLRequest(request);
	try {
	    //extract the content of the message this could throws a FIPAException
	    SLAction = (Action) myAgent.getContentManager().extractContent(request);
	    action = SLAction.getAction();

	    if(action instanceof Register){
		agentDescription = (DFAgentDescription)((Register)action).getDescription();
		//to avoid autoregistration.
		if ((agentDescription.getName()!=null) && ((agentDescription.getName().equals(myAgent.getAID()) || agentDescription.getName().equals(myAgent.getLocalName())))) {
		    Unauthorised e = new Unauthorised();
		    // send a refuse
		    createExceptionalMsgContent(SLAction, e, request);
		    throw e;
		}
		myAgent.checkMandatorySlots(FIPAManagementOntology.REGISTER, agentDescription);
		actionID = REGISTER;
	    }else if(action instanceof Deregister){
		//performs a DEREGISTER
		agentDescription = (DFAgentDescription)((Deregister)action).getDescription();
		actionID = DEREGISTER;
		myAgent.checkMandatorySlots(FIPAManagementOntology.DEREGISTER, agentDescription);
	    }else if(action instanceof Modify){
		//performs a MODIFY
		actionID = MODIFY;
		agentDescription = (DFAgentDescription)((Modify)action).getDescription();
		myAgent.checkMandatorySlots(FIPAManagementOntology.MODIFY, agentDescription);
	    }else if(action instanceof Search){
		//performs a SEARCH
		agentDescription = (DFAgentDescription)((Search)action).getDescription();
		constraints = ((Search)action).getConstraints();
		actionID = SEARCH;
	       }
	    else{
		//this case should never occur since if the action does not exist the extract content throws a Ontology Exception.
		//FIXME: the UnsupportedFunction exception requires as parameter the name of the unsupported function.
		//how can we retrive this name ?
		UnsupportedFunction uf = new UnsupportedFunction();
		createExceptionalMsgContent(SLAction,uf,request);
		throw uf;
	    }

	    //if everything is OK returns an AGREE message.
	    ACLMessage agree = request.createReply();
	    agree.setPerformative(ACLMessage.AGREE);
	    agree.setContent(createAgreeContent(SLAction,request.getLanguage(),request.getOntology()));
	    return agree;

	}catch(RefuseException re){ // catch and rethrow the unsupportedFunction and unauthorized exceptions
	    throw re;
	/*}catch(FIPAException fe){
	    //Exception thrown by the parser.
	    fe.printStackTrace();
	    UnrecognisedValue uv = new UnrecognisedValue("content");
	    createExceptionalMsgContent(SLAction,uv,request);
	    throw uv; */
	}catch (Exception e) {
	    e.printStackTrace();
	    UnrecognisedValue uv2 = new UnrecognisedValue("content");
	    createExceptionalMsgContent(SLAction,uv2,request);
	    throw uv2;
	}
    }
    
	
	
	
    /**
       Do the action and send the Inform message.
     */
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{

	ACLMessage res = request.createReply();
	res.setPerformative(ACLMessage.INFORM);

	switch (actionID){
	case REGISTER: 
	    try{
		myAgent.DFRegister(agentDescription);
		res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    }catch(AlreadyRegistered ar){
		createExceptionalMsgContent(SLAction,ar,request);
		throw ar;
	    }
		break;
	case DEREGISTER:
	    try{
		myAgent.DFDeregister(agentDescription);
		res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    }catch(NotRegistered nr){
		createExceptionalMsgContent(SLAction,nr,request);
		throw nr;
	    }
		break;
	case SEARCH: 
                List result;
                // check if the search has to be served (e.g. the search-id is new)
                if (myAgent.searchMustBeServed(constraints)) {
                    result = myAgent.DFSearch(agentDescription,constraints,null);
                    // Note that if the local search produced more results than
                    // required, we don't even consider the recursive search 
                    // regardless of the maxDepth parameter.
                    int maxResult = myAgent.getActualMaxResults(constraints); 
                    if(result.size() >= maxResult) {
                        // More results than required have been found, remove the unwanted results
                        for (int i=maxResult; i<result.size(); i++)
                            result.remove(i);
                     } else { // result.size < maxResult
                        // check if the search has to be propagated
                        if (myAgent.searchMustBePropagated(constraints,result.size()))
                            if(myAgent.performRecursiveSearch(result,constraints,agentDescription,request,SLAction)) 
                                  return null; // here return null because the recursive search takes care of sending back the INFORM
                     } // end else
                } else {  
                    // this search has been seen already, therefore send back a FAILURE
                    InternalError ie3 = new InternalError("search-id already served");
                    createExceptionalMsgContent(SLAction,ie3,request);
                    throw ie3;
                }
                // send back the results
                try{
                    Result rs = new Result();
                    rs.setAction(SLAction);
                    rs.setItems(result);
                    myAgent.getContentManager().fillContent(res, rs);
                }
                catch(Exception e){
                    InternalError ie2 = new InternalError("error in creating the reply");
                    createExceptionalMsgContent(SLAction,ie2,request);
                    throw ie2;
                }  
                break;
	case MODIFY: 
	    try{
		myAgent.DFModify(agentDescription);
		res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    }catch(NotRegistered nre){
		createExceptionalMsgContent(SLAction,nre,request);
		throw nre;
	    }
		break;
	default : break; //should never occur.

	}

	return res;       
    }
    
    //to reset the action
    public void reset(){
	super.reset();
	action = null;
	actionID = UNSUPPORTED;
	agentDescription = null;
	constraints = null;
	SLAction = null;
    }
}//end class DFFipaAgentManagementBehaviour
