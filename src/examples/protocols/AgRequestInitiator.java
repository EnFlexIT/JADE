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

// This agent plays the initiator role in fipa-request protocol.

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.AgentManagementOntology;
import jade.proto.FipaRequestInitiatorBehaviour;

/**
This agent plays initiator role in a conversation following standard
'fipa-request' protocol. The example program shows how more complex 
interactions (such as agent interaction  protocols) can be handled
with primitives provided by JADE framework, such as composite
behaviours and advanced message passing.
@author Tiziana Trucco - CSELT S.p.A.
@version $Date$ $Revision$
*/


public class AgRequestInitiator extends Agent{

	// Used to generate conversation IDs.
	private int convCounter = 0;
	
	private String responder = new String();
	
	protected void setup(){
		
		/** Search with the DF for the name of the RequestResponderAgent **/
  
    AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();    
  	dfd.setType("RequestResponderAgent"); 
  	try {
    	while (true) {
      	System.out.println(getLocalName()+ " waiting for an RequestResponderAgent registering with the DF");
      	AgentManagementOntology.DFSearchResult result;
      	Vector vc = new Vector(1);
      	AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      	c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      	c.setFn(AgentManagementOntology.Constraint.MAX); // MIN
      	c.setArg(3);
      	vc.addElement(c);
      	result = searchDF("DF",dfd,vc);
      	Enumeration e = result.elements();
      	if (e.hasMoreElements()) {
					dfd = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
					responder = dfd.getName();
					break;
      	} 
      	Thread.sleep(10000);
    	}
  	} catch (Exception fe) {
    	System.err.println(getLocalName()+" search with DF is not succeeded because of " + fe.getMessage());
    	doDelete();
    }		
    
		String convID = newConvID();
		
		//Use this version to reproduce the bug
	/*	try{	
			System.out.println("Enter the name of the agent responder:");
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
      responder = buff.readLine();
		}catch(IOException e){}*/
			
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addDest(responder);
			request.setLanguage("Plain-Text");
			request.setContent("( action " + responder +" "+ " (ExampleRequest " + convCounter + " ) )");
			request.setConversationId(convID);
		
			MessageTemplate mt = MessageTemplate.MatchSource(responder);

			addBehaviour(new myFipaRequestInitiatorBehaviour(this,request,mt));
		}

private String newConvID(){

	String out = new String(getLocalName() + (new Integer(convCounter).toString()));
	++convCounter;
	return out;

}

private class myFipaRequestInitiatorBehaviour extends FipaRequestInitiatorBehaviour{
	
	myFipaRequestInitiatorBehaviour(Agent a,ACLMessage msg, MessageTemplate mt)
	{
		super(a, msg, mt);
	
		System.out.println("\nAgent " + getLocalName() + " has sent the following message:\n");
	  msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

	}
	
	protected void handleNotUnderstood(ACLMessage msg){
		
		System.out.println("\nWARNING: Responder not understand the request");
		System.out.println(msg.toString());
	//	msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
		reset(waitAndGetNewMsg(msg));
	}
	
	protected void handleRefuse(ACLMessage msg)
	{
		System.out.println("\nResponder refuse to process the request:");
	//	msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
		System.out.println(msg.toString());
		reset(waitAndGetNewMsg(msg));

	}
	
	protected void handleAgree(ACLMessage msg)
	{
		System.out.println("\n\tSuccess!!! Responder agreed to do action !");
		System.out.println(msg.toString());

    //msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
  
	}
	
	protected void handleFailure(ACLMessage msg)
	{
		System.out.println("\nResponder failed to process the request. Reason was:");
    System.out.println(msg.getContent());
    System.out.println(msg.toString());

    //msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    reset(waitAndGetNewMsg(msg));

	}
	
	protected void handleInform(ACLMessage msg)
	{
		System.out.println("\nResponder has just informed me that the action has been carried out.");
    //msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
    System.out.println(msg.toString());

    reset(waitAndGetNewMsg(msg));
	}	
	
	private ACLMessage waitAndGetNewMsg(ACLMessage msg){
	
		//Wait random time between zero and ten seconds.
		int timeout = (new Double(Math.random()*10000.0)).intValue();
		System.out.println("Agent :"+myAgent.getLocalName()+" is waiting for "+ timeout + "milliseconds before re-initiating the protocol");  
		myAgent.doWait(timeout);
		System.out.println("Agent :"+myAgent.getLocalName()+" is waked up "); 
		ACLMessage reply = msg.createReply();
		reply.setContent("( action " + responder +" "+ " (ExampleRequest "+convCounter+") )");
		System.out.println("Agent: "+myAgent.getLocalName()+"has sent the following message");
		System.out.println(reply.toString());

		return reply;
		
	}
	
	
}

}//end class AgentRequest