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

import jade.core.CaseInsensitiveString;

import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;

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
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
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
    In this method we can be send : AGREE- NOT UNDERSTOOD and Refuse.
    in this method we parse the content in order to know the action required to the DF.
    if the action is unsupported a NOT UDERSTOOD message is sent.
    if something went wrong with the ontology a REFUSE message will be sent, otherwise an AGREE will be sent.
  and performs the action.
    */
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException{

	isAnSLRequest(request);
	try {
	    //extract the content of the message this could throws a FIPAException
	    List l = myAgent.extractMsgContent(request);
	    SLAction = (Action)l.get(0);
	    action = SLAction.getAction();

	    if(action instanceof Register){
		agentDescription = (DFAgentDescription)((Register)action).get_0();
		//to avoid autoregistration.
		if ((agentDescription.getName()!=null) && ((agentDescription.getName().equals(myAgent.getAID()) || agentDescription.getName().equals(myAgent.getLocalName())))) {
		    Unauthorised e = new Unauthorised();
		    // send a refuse
		    createExceptionalMsgContent(SLAction, e, request);
		    throw e;
		}
		myAgent.checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, agentDescription);
		actionID = REGISTER;
	    }else if(action instanceof Deregister){
		//performs a DEREGISTER
		agentDescription = (DFAgentDescription)((Deregister)action).get_0();
		actionID = DEREGISTER;
		myAgent.checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, agentDescription);
	    }else if(action instanceof Modify){
		//performs a MODIFY
		actionID = MODIFY;
		agentDescription = (DFAgentDescription)((Modify)action).get_0();
		myAgent.checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, agentDescription);
	    }else if(action instanceof Search){
		//performs a SEARCH
		agentDescription = (DFAgentDescription)((Search)action).get_0();
		constraints = ((Search)action).get_1();
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
	}catch(FIPAException fe){
	    //Exception thrown by the parser.
	    fe.printStackTrace();
	    UnrecognisedValue uv = new UnrecognisedValue("content");
	    createExceptionalMsgContent(SLAction,uv,request);
	    throw uv;
	}catch (Exception e) {
	    e.printStackTrace();
	    UnrecognisedValue uv2 = new UnrecognisedValue("content");
	    createExceptionalMsgContent(SLAction,uv2,request);
	    throw uv2;
	}
    }
    
    /**
       Send the Inform message.
     */
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{

	ACLMessage res = request.createReply();
	res.setPerformative(ACLMessage.INFORM);

	switch (actionID){
	case REGISTER: 
	    try{
		myAgent.DFRegister(agentDescription);
		res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		break;
	    }catch(AlreadyRegistered ar){
		createExceptionalMsgContent(SLAction,ar,request);
		throw ar;
	    }
	case DEREGISTER:
	    try{
		myAgent.DFDeregister(agentDescription);
		res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		break;
	    }catch(NotRegistered nr){
		createExceptionalMsgContent(SLAction,nr,request);
		throw nr;
	    }
	case SEARCH: 
	    List result = myAgent.DFSearch(agentDescription,constraints,null);

	    //verify if the search must be recursive
	    Long maxResult = constraints.getMaxResults();
	    //search is recursive if there are less results than required and the depth is greater than one
	    if(maxResult != null) 
		if(result.size() >= maxResult.intValue()){
		    //more results than required have been found
		    ArrayList list = new ArrayList();
		    int j = 0;
		    for(Iterator i = result.iterator();i.hasNext()&& j < maxResult.intValue();j++)
			list.add(i.next());   
		    try{
			ResultPredicate rp = new ResultPredicate();
			rp.set_0(SLAction);
			for(int i=0;i<list.size();i++)
			    rp.add_1(list.get(i));
			list.clear();
			list.add(rp);
			myAgent.fillMsgContent(res,list);
			//found all the result required.
			return res;
		    }catch(FIPAException fe){
			//FIXME: verify the exception.
			//	throw new FailureException(createExceptionalMsgContent(SLAction,fe,request.getLanguage(),request.getOntology()));
			InternalError ie = new InternalError("error in creating the reply");
			createExceptionalMsgContent(SLAction,ie,request);
			throw ie;
		    }	   
		}
	    Long maxDepth = constraints.getMaxDepth();
	 
	    if(maxDepth != null)
		if(maxDepth.intValue()>0){
		    //recursive search on children
		    if(myAgent.performRecursiveSearch(result,constraints,agentDescription,request,SLAction))
			return null;
		}
	  
	    try{
		ResultPredicate rp = new ResultPredicate();
		rp.set_0(SLAction);
		for(int i=0;i<result.size();i++)
		    rp.add_1(result.get(i));
		result.clear();
		result.add(rp);
		myAgent.fillMsgContent(res,result);
	
		return res;
		//break;
	    }catch(FIPAException fe){
		//throw new FailureException(createExceptionalMsgContent(SLAction,fe,request.getLanguage(),request.getOntology()));
		InternalError ie2 = new InternalError("error in creating the reply");
		createExceptionalMsgContent(SLAction,ie2,request);
		throw ie2;
	    }  

	case MODIFY: 
	    try{
		myAgent.DFModify(agentDescription);
		res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		break;
	    }catch(NotRegistered nre){
		createExceptionalMsgContent(SLAction,nre,request);
		throw nre;
	    }
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
