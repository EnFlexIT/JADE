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

import jade.domain.JADEAgentManagement.ShowGui;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

class DFJadeAgentManagementBehaviour extends DFResponderBehaviour{

    private df myAgent;
    private Action SLAction = null;
    private int actionID = UNSUPPORTED;

    private final static int UNSUPPORTED = -1;
    private final static int SHOWGUI = 0;

    protected DFJadeAgentManagementBehaviour(df a, MessageTemplate mt){
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
	    //extract the content of the message this could throws a FIPAException
	    SLAction = (Action) myAgent.getContentManager().extractContent(request);
	    Object action = SLAction.getAction();
	   
	    if(action instanceof ShowGui){
		//must perform a SHOWGUI.
		actionID = SHOWGUI;
	    }
	    else{
		//action not supported.
		actionID = UNSUPPORTED;
		//FIXME: the unsupported function exception requires as parameter the name of the unsupported action.
		UnsupportedFunction uf = new UnsupportedFunction();
		createExceptionalMsgContent(SLAction,uf,request);
		throw uf;
	    }
	 
	    return null;

	}catch(RefuseException re){
	    throw re;
	}catch(Exception e){
	    //Exception thrown by the parser.
	    e.printStackTrace();
	    UnrecognisedValue uv = new UnrecognisedValue("content");
	    createExceptionalMsgContent(SLAction,uv,request);
	    throw uv;
	}   
    }
    
    /**
       Send the Inform message.
     */
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException{

	ACLMessage reply = request.createReply();

	switch (actionID){
	case SHOWGUI:
	    myAgent.showGuiAction(SLAction);
	    reply.setPerformative(ACLMessage.INFORM);
	    reply.setContent("( )");
	    break;
	default: break; //FIXME: should never occur
       }

	return reply;
    }
    
    //to reset the action
    public void reset(){
	super.reset();
	SLAction = null;
	actionID = UNSUPPORTED;
    }
   
}//end class DFJadeAgentManagementBehaviour
