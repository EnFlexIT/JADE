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

import jade.content.onto.basic.*;

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
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.Register;
import jade.domain.FIPAAgentManagement.Deregister;
import jade.domain.FIPAAgentManagement.Modify;
import jade.domain.FIPAAgentManagement.Unauthorised;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.UnrecognisedValue;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.NotRegistered;
import jade.domain.FIPAAgentManagement.AlreadyRegistered;
import jade.domain.FIPAAgentManagement.GetDescription;
import jade.domain.FIPAAgentManagement.APDescription;

//__SECURITY__BEGIN
import jade.security.AuthException;
//__SECURITY__END

/**
 

  @author Tiziana Trucco - Tilab
  @version $Date$ $Revision$


*/
class AMSFipaAgentManagementBehaviour extends DFResponderBehaviour{

  
    private ams myAgent;
  

    //the result notification that will be send into the prepareResultNotification.
    private ACLMessage res;

    protected AMSFipaAgentManagementBehaviour(ams a, MessageTemplate mt){
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

	AMSAgentDescription agentDescription = null;
	Object action = null;
	Action SLAction = null; 

	isAnSLRequest(request);
	try {
	    SLAction = (Action)myAgent.getContentManager().extractContent(request);
	    action = SLAction.getAction();

	    //the result notification that will be sent into the prepareResultNotification.
	    res = request.createReply();
	    res.setPerformative(ACLMessage.INFORM);
	    
	    if(action instanceof Register){
		agentDescription = (AMSAgentDescription)((Register)action).get_0();
		try{
		
		    myAgent.AMSRegisterAction(SLAction,agentDescription,request.getSender(),request.getOntology());
		    res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		}catch(AlreadyRegistered ar){
		    createExceptionalMsgContent(SLAction,ar,request);
		    res.setPerformative(ACLMessage.FAILURE);
		    res.setContent(ar.getMessage());
		}catch(AuthException ae){ 
		    Unauthorised un = new Unauthorised();
		    throw ae;	  
		}
	    }
	    else if(action instanceof Deregister){
		//performs a DEREGISTER
		agentDescription = (AMSAgentDescription)((Deregister)action).get_0();
		try{
		    myAgent.AMSDeregister(agentDescription,request.getSender());
		    res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		}catch(NotRegistered nr){
		    createExceptionalMsgContent(SLAction,nr,request);
		    res.setPerformative(ACLMessage.FAILURE);
		    res.setContent(nr.getMessage());
		}catch(AuthException ae){
		    Unauthorised un = new Unauthorised();
		    throw ae;	  
		}
	    }
	    else if(action instanceof Modify){
		//performs a MODIFY
		agentDescription = (AMSAgentDescription)((Modify)action).get_0();
		try{
		    myAgent.AMSModify(agentDescription,request.getSender());
		    res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
		}catch(NotRegistered nre){
		    createExceptionalMsgContent(SLAction,nre,request);
		    res.setPerformative(ACLMessage.FAILURE);
		    res.setContent(nre.getMessage());
		}catch(AuthException au){
		    Unauthorised un = new Unauthorised();
		    throw un;	  
		}
	    }
	    else if(action instanceof Search){
		//performs a SEARCH
		agentDescription = (AMSAgentDescription)((Search)action).get_0();
		SearchConstraints constraints = ((Search)action).get_1();
		try{
		
		    List result = myAgent.AMSSearch(agentDescription,constraints,request,request.getSender());
		    Result r = new Result();
		    r.setAction(SLAction);
		    r.setItems(result);
		    myAgent.getContentManager().fillContent(res, r);
		}catch(FIPAException fe){
		    //throw new FailureException(createExceptionalMsgContent(SLAction,fe,request.getLanguage(),request.getOntology()));
		    //FIXME: verify the exception.
		    InternalError ie2 = new InternalError("error in creating the reply");
		    createExceptionalMsgContent(SLAction,ie2,request);
		    res.setPerformative(ACLMessage.FAILURE);
		    res.setContent(ie2.getMessage());
		} catch(AuthException au){
		    Unauthorised un = new Unauthorised();
		    throw un;	  
		} 
	    }
	    else if(action instanceof GetDescription){
	
		APDescription ap = myAgent.getDescriptionAction();
      
		Result rp = new Result();
		rp.setAction(SLAction);
		List l = new ArrayList();
		l.add(ap);
		rp.setItems(l);
		try{
		    myAgent.getContentManager().fillContent(res, rp);
		}catch(Exception e){
		    InternalError ie = new InternalError("Error in creating the reply");
		    createExceptionalMsgContent(SLAction,ie,request);
		    res.setPerformative(ACLMessage.FAILURE);
		    res.setContent(ie.getMessage());
		}
	    }
	    else{
		//this case should never occur since if the action does not exist the extract content throws a Ontology Exception.
		//FIXME: the UnsupportedFunction exception requires as parameter the name of the unsupported function.
		//how can we retrive this name ?
		UnsupportedFunction uf = new UnsupportedFunction();
		createExceptionalMsgContent(SLAction,uf,request);
		throw uf;
	    }
	}catch(RefuseException re){
	    createExceptionalMsgContent(SLAction,re,request);
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

	//if everything is OK returns an AGREE message.
	ACLMessage agree = request.createReply();
	agree.setPerformative(ACLMessage.AGREE);
	agree.setContent(createAgreeContent(SLAction,request.getLanguage(),request.getOntology()));
	return agree;
    }
    
    /**
       Send the Inform or Failure message.
     */
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{
	return res;
    }
    
    //to reset the action
    public void reset(){
	super.reset();
	res = null;
    }
}//end class AMSFipaAgentManagementBehaviour
