/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package examples.ex4;


import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

// This agent plays the responder role in fipa-request protocol.
public class AgentResponder extends Agent {


  // This behaviour plays responder's role in a fipa-request conversation
  private class ResponderBehaviour extends SimpleBehaviour {

    // A simple behaviour to send an ACL message to an agent
    private class SendBehaviour extends OneShotBehaviour {

      private ACLMessage message;

      public SendBehaviour(ACLMessage msg) {
	message = msg;
      }

      public void action() {
	send(message);
	message.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      }

    } // End of SendBehaviour


    private boolean finished = false;
    private String myPeer;
    private String myConvId;


    public ResponderBehaviour(ACLMessage msg) {
      myPeer = msg.getSource();
      myConvId = msg.getConversationId();
    }

    public void action() {

      // This agent answers 'not-understood' with 20% probability,
      // 'refuse' with 30% probability and 'agree' with 50%
      // probability. If a request is agreed, there is still a 40%
      // failure probability.

      ACLMessage reply = new ACLMessage("not-understood");
      reply.setSource(getLocalName());
      reply.removeAllDests();
      reply.addDest(myPeer);
      reply.setProtocol("fipa-request");
      reply.setConversationId(myConvId);

      double chance = Math.random();

      if(chance < 0.2) {
	// Reply with 'not-understood'
	reply.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
	send(reply);
      }
      else if(chance < 0.5) {
	// Reply with 'refuse'
	reply.setType("refuse");
	reply.setLanguage("\"Plain Text\"");
	reply.setContent("I'm too busy at the moment. Retry later.");
	reply.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
	send(reply);
      }
      else {
	// Reply with 'agree' and schedule next message
	reply.setType("agree");
	reply.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
	send(reply);

	chance = Math.random();
	if(chance < 0.4) {
	  // Select a 'failure' message
	  reply.setType("failure");
	  reply.setLanguage("\"Plain Text\"");
	  reply.setContent("Something went wrong with the teleport.");
	}
	else {
	  // Select an 'inform' message
	  reply.setType("inform");
	  reply.setLanguage("\"Plain Text\"");
	  reply.setContent("I hereby inform you that the action has been done.");
	}


	// Schedule a new behaviour to send the message, thereby
	// allowing other behaviours to run between the two send()
	// operations.
	addBehaviour(new SendBehaviour(reply));

      }

      finished = true;

    }


    public boolean done() {
      return finished;
    }

  } // End of ResponderBehaviour


  // This behaviour continously receives 'request' messages and then
  // spawns a ResponderBehaviour to handle them.
  private class MultipleBehaviour extends CyclicBehaviour {

    MessageTemplate pattern;

    public MultipleBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchType("request");

      pattern = MessageTemplate.and(mt1,mt2);

    }

    public void action() {

      ACLMessage request = receive(pattern);

      if(request != null) {
	System.out.println("Received: ");
	request.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
	addBehaviour(new ResponderBehaviour(request));
      }

      // With the following two lines no CPU time will be wasted
      else
	block();
    }

  }

  protected void setup() {
    addBehaviour(new MultipleBehaviour());
  }


}
