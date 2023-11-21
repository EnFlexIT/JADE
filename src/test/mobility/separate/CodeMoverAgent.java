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

package test.mobility.separate;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.*;
import jade.util.leap.*;
import jade.content.*;
import jade.content.onto.*;
import jade.content.abs.*;
import jade.content.lang.*;
import jade.content.lang.leap.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;

/**
   @author Giovanni Caire - TILAB
 */
public class CodeMoverAgent extends Agent {
	private AID tester;
	private MessageTemplate template;
	private AchieveREResponder responder;
	private SequentialBehaviour sb;
	private ParallelBehaviour pb;
	private ACLMessage msg;
	
	// Some fields just used to test code compatibility 
	Codec c = new LEAPCodec();
	Ontology o = FIPAManagementOntology.getInstance();
	AbsPredicate p = new AbsPredicate("Sample");
	Location l = new ContainerID("Main-Container", null);
	
	protected void setup() {
		// Wait for the startup message and get tester name
		msg = blockingReceive();
		System.out.println(getLocalName()+": Startup message received.");
		tester = msg.getSender();
		// Reply 
		send(msg.createReply());
		System.out.println(getLocalName()+": Reply sent.");
		
		template = MessageTemplate.and(
			MessageTemplate.MatchOntology("onto"),
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST) );
		responder = new AchieveREResponder(this, template);
		
		Behaviour b = new OneShotBehaviour(this) {
			public void action() {
				block();
			}
		};
		
		sb = new SequentialBehaviour(this);
		sb.addSubBehaviour(responder);
		sb.addSubBehaviour(b);
		
		b = new TickerBehaviour(this, 1000) {
			protected void onTick() {
				System.out.println(myAgent.getLocalName()+": "+getTickCount());
			}
		};
		
		pb = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
		pb.addSubBehaviour(sb);
		pb.addSubBehaviour(b);
		
		addBehaviour(pb);
		
		msg = new ACLMessage(ACLMessage.CONFIRM);
		msg.addReceiver(tester);
	}
	
	protected void beforeMove() {
		System.out.println(getLocalName()+": Leaving location "+here().getName());
	}
	
	protected void afterMove() {
		System.out.println(getLocalName()+": Entering location "+here().getName());
		// Inform the tester about where I am
		msg.setContent(here().getName());
		send(msg);
	}	
	
	protected void beforeClone() {
		System.out.println(getLocalName()+": Cloning from location "+here().getName());
	}
	
	protected void afterClone() {
		System.out.println(getLocalName()+": Cloned on location "+here().getName());
		// Inform the tester about where I am
		msg.setContent(here().getName());
		send(msg);
	}	
}
