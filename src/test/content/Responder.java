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

package test.content;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.lang.*;

import test.content.testOntology.TestOntology;

public class Responder extends CyclicBehaviour {
	public static final String TEST_CONVERSATION = "_Test_";
	public static final String TEST_RESPONSE_ID = "_Response_";
	
	private boolean verbose = false;
	
  private MessageTemplate mt = MessageTemplate.and(
  	MessageTemplate.MatchConversationId(TEST_CONVERSATION),
  	MessageTemplate.MatchReplyWith(TEST_RESPONSE_ID));
  	
  public void onStart() {
  	myAgent.getContentManager().registerOntology(TestOntology.getInstance()); 
  }
  
  public void action() {
  	ACLMessage msg = myAgent.receive(mt);
  	if (msg != null) {
  		ACLMessage reply = msg.createReply();
  		try {
  			handleContent(msg);
  			// Condent handling OK --> reply with an empty INFORM message
  			reply.setPerformative(ACLMessage.INFORM);
  		}
  		catch (Throwable t) {
  			// Content handling FAILED --> reply with an empty FAILURE message
	  		t.printStackTrace();
  			reply.setPerformative(ACLMessage.FAILURE);
  		}
  		myAgent.send(reply);
  	}
  	else {
  		block();
  	}
  }
  	
  private void handleContent(ACLMessage msg) throws Throwable {
  	if (verbose) {
  		System.out.println("Received content is:");
  	}
  	try {
  		ContentElement ce = myAgent.getContentManager().extractContent(msg);
  		if (verbose) {
  			System.out.println(ce);
  		}
  	}
  	catch (UngroundedException ue) {
  		AbsContentElement ace = myAgent.getContentManager().extractAbsContent(msg);
  		if (verbose) {
  			System.out.println(ace);
  		}
  	}
  }
}  
