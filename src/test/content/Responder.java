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

public class Responder extends Agent {
	public static final String TEST_CONVERSATION = "_Test_";
	public static final String TEST_RESPONSE_ID = "_Response_";
	
  private MessageTemplate mt = MessageTemplate.and(
  	MessageTemplate.MatchConversationId(TEST_CONVERSATION),
  	MessageTemplate.MatchReplyWith(TEST_RESPONSE_ID));
  	
  public void setup() {
  	addBehaviour(new CyclicBehaviour(this) {
  		public void action() {
  			ACLMessage msg = myAgent.receive(mt);
  			if (msg != null) {
  				ACLMessage reply = msg.createReply();
	  			reply.setPerformative(msg.getPerformative());
  				if (msg.hasByteSequenceContent()) { 
  					reply.setByteSequenceContent(msg.getByteSequenceContent());
  				}
  				else {
  					reply.setContent(msg.getContent());
  				}
  				myAgent.send(reply);
  			}
  			else {
  				block();
  			}
  		}
  	} );
  }
}  
