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

import jade.util.leap.List;

import jade.content.onto.basic.Action;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.UnsupportedFunction;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.UnrecognisedValue;

import jade.domain.DFGUIManagement.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

class DFAppletManagementBehaviour extends DFResponderBehaviour{

    private df myAgent;
    private Action SLAction = null;
    private int actionID = UNSUPPORTED;
    private Object action = null;

    //action supported by the df for the applet
    private final static int UNSUPPORTED = -1;
    private final static int GETPARENT = 0;
    private static final int GETDEFAULTDESCRIPTION= 1;
    private static final int FEDERATEWITH = 2;
    private static final int GETDESCRIPTIONUSED = 3;
    private static final int DEREGISTERFROM = 4;
    private static final int REGISTERWITH = 5;
    private static final int SEARCHON = 6;
    private static final int MODIFYON = 7;


    protected DFAppletManagementBehaviour(df a, MessageTemplate mt){
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
	try{
	    //extract the content of the message
	    SLAction = (Action) myAgent.getContentManager().extractContent(request);
	    action = SLAction .getAction();
	    
	    if(action instanceof GetParent)
		actionID = GETPARENT;
	    else if(action instanceof GetDefaultDescription)
		actionID = GETDEFAULTDESCRIPTION;
	    else if(action instanceof Federate)
		actionID = FEDERATEWITH;
	    else if(action instanceof GetDescriptionUsed)
		actionID = GETDESCRIPTIONUSED;
	    else if(action instanceof DeregisterFrom)
		actionID = DEREGISTERFROM;
	    else if(action instanceof RegisterWith)
		actionID = REGISTERWITH;
	    else if(action instanceof SearchOn)
		actionID = SEARCHON;
	    else if(action instanceof ModifyOn)
		actionID = MODIFYON;
	    else{
		//action not supported.
		actionID = UNSUPPORTED;
		//should never occur since the parser throws an exception before.
		//FXIME:the exception should have a parameter.
		UnsupportedFunction uf = new UnsupportedFunction();
		createExceptionalMsgContent(SLAction,uf,request);
		throw uf;
	    }
	 
	    //if everything is OK returns an AGREE message.
	    ACLMessage agree = request.createReply();
	    agree.setPerformative(ACLMessage.AGREE);
	    agree.setContent("( ( true ) )");
	    return agree;
	    
	}catch(RefuseException re){
	    throw re;
	}catch(Exception e){
	    //Exception thrown by the parser.
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

	ACLMessage reply = null;

	switch (actionID){
	case GETPARENT:
	    reply = myAgent.getParentAction(SLAction,request);
	    break;
	case GETDEFAULTDESCRIPTION:
	    reply = myAgent.getDescriptionOfThisDFAction(SLAction,request);
	    break;
	case FEDERATEWITH : 
	    myAgent.federateWithAction(SLAction,request);
	    break;
	case GETDESCRIPTIONUSED :
	    reply = myAgent.getDescriptionUsedAction(SLAction,request);
	    break; 
	case DEREGISTERFROM : 
	    myAgent.deregisterFromAction(SLAction,request);
	    break;
	case REGISTERWITH: 
	    myAgent.registerWithAction(SLAction,request);
	    break;
	case SEARCHON: 
	    myAgent.searchOnAction(SLAction,request);
	    break;
	case MODIFYON: 
	    myAgent.modifyOnAction(SLAction,request);
	    break;
	default: break; //FIXME: should never occur
	}

	return reply;
    }
    
    //to reset the action
    public void reset(){
	super.reset();
	action = null;
	actionID = UNSUPPORTED;
	SLAction = null;
    }
   
}//end class DFJadeAgentManagementBehaviour
