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
import jade.core.AID;

import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.UnrecognisedValue;
import jade.domain.FIPAAgentManagement.Unauthorised;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import jade.domain.JADEAgentManagement.*;

import jade.core.Location;

//__SECURITY__BEGIN
import jade.security.AuthException;
//__SECURITY__END

/**
  @author Tiziana Trucco - Tilab
  @version $Date$ $Revision$
*/

class AMSJadeAgentManagementBehaviour extends DFResponderBehaviour{

    private ams myAgent;
    private MobilityManager myMobilityMgr;
   
    //the result notification prepared into the prepareResponse.
    private ACLMessage res;

    protected AMSJadeAgentManagementBehaviour(ams a, MobilityManager b, MessageTemplate mt){
		super(a,mt);
		myAgent = a;
		myMobilityMgr = b;
    }

    /*
    In this method we can be send : AGREE- NOT UNDERSTOOD and Refuse.
    in this method we parse the content in order to know the action required to the DF.
    if the action is unsupported a NOT UDERSTOOD message is sent.
    if something went wrong with the ontology a REFUSE message will be sent, otherwise an AGREE will be sent.
    and performs the action.
    */
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException{

	Object action = null;
	Action SLAction = null;
 
	try{	
	    isAnSLRequest(request);

	    //prepare the resultNotification
	    res = request.createReply();
	    res.setPerformative(ACLMessage.INFORM);
	    	    
	    SLAction = (Action)myAgent.getContentManager().extractContent(request);
	    
	    action = SLAction.getAction();

	    if(action instanceof CreateAgent){
  			ACLMessage reply = request.createReply();
			reply.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
			reply.setPerformative(ACLMessage.INFORM);
			//res is null in this case. The INFORM will be sent by the AMS later.
			res = myAgent.createAgentAction((CreateAgent)action,request,reply);
	    } else if(action instanceof KillAgent){
			//performs a KILLAGENT
			myAgent.killAgentAction((KillAgent)action);
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof KillContainer){
			//performs a KILLCONTAINER
			myAgent.killContainerAction((KillContainer)action,request.getSender());
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof SniffOn){
			//performs a SNIFFON
			myAgent.sniffOnAction((SniffOn)action);
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof SniffOff){
			myAgent.sniffOffAction((SniffOff)action); 
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof DebugOn){
			myAgent.debugOnAction((DebugOn)action);
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof DebugOff){
			myAgent.debugOffAction((DebugOff)action);
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof InstallMTP){
			myAgent.installMTPAction((InstallMTP)action);
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof UninstallMTP){
			myAgent.unistallMTPAction((UninstallMTP)action);
			res.setContent(createInformDoneContent(SLAction,request.getLanguage(),request.getOntology()));
	    } else if(action instanceof QueryAgentsOnLocation) {
	    	Location loc = ((QueryAgentsOnLocation)action).getLocation();
	    	List l = myAgent.queryAgentsOnLocationAction(loc);
	    	Result r  = new Result();
	    	r.setAction(SLAction);
	    	r.setItems(l);
	    	try {
		    	myAgent.getContentManager().fillContent(res, r);
		  	} catch (Exception e) {
		  		System.out.println(e);
		  	}
	 	} else if(action instanceof WhereIsAgentAction){
		    AID agentN = ((WhereIsAgentAction)action).get_0();
		    Location where = myAgent.AMSWhereIsAgent(agentN, request.getSender());
		    Result r = new Result();
		    r.setAction(SLAction);
		    List l1 = new ArrayList();
		    l1.add(where);
		    r.setItems(l1);
		    try {
		    	myAgent.getContentManager().fillContent(res, r);
		  	} catch (Exception e) {
		  		System.out.println(e);
		  	}
		} else if(action instanceof QueryPlatformLocationsAction){
		    Result r2 = new Result();
		    r2.setAction(SLAction);
			r2.setItems(myMobilityMgr.getLocations());
		    try {
		    	myAgent.getContentManager().fillContent(res, r2); 
		  	} catch (Exception e) {
		  		System.out.println(e);
		  	}
	   } else {
			//this case should never occur since if the action does not exist the extract content throws a Ontology Exception.
			//FIXME: the UnsupportedFunction exception requires as parameter the name of the unsupported function.
			//how can we retrive this name ?
			UnsupportedFunction uf = new UnsupportedFunction();
			//createExceptionalMsgContent(SLAction,uf,request);
			throw uf;
	   }
	} catch(RefuseException re){
	    createExceptionalMsgContent(SLAction,re,request);
	    throw re;
	}catch(FailureException ie){
	    res.setPerformative(ACLMessage.FAILURE);
	    createExceptionalMsgContent(SLAction,ie,request);  
	    res.setContent(ie.getMessage());
	}catch(AuthException au){
	    //FIXME: perhaps this AuthEception could be caught into the createAgentAction.
	    Unauthorised un = new Unauthorised();
	    createExceptionalMsgContent(SLAction, un,request);
	    throw un;
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
       Send the Inform message.
     */
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{	
		return res;       
    }
    
    //to reset the action
    public void reset(){
	super.reset();
	res = null;
    }
}//end class AMSJadeAgentManagementBehaviour
