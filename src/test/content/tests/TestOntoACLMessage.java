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

package test.content.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.*;
import jade.content.onto.basic.*;
import jade.util.leap.Iterator;
import test.common.*;
import test.content.*;
import test.content.testOntology.Exists;
import java.util.Date;

public class TestOntoACLMessage extends Test {
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				msg.setPerformative(ACLMessage.REQUEST);
  				ACLMessage act = new ACLMessage(ACLMessage.INFORM);
  				Iterator it = msg.getAllReceiver();
  				AID recv = (AID) it.next();
  				act.setSender(recv);
  				act.addReceiver(myAgent.getAID());
  				act.setLanguage(msg.getLanguage());
  				act.setOntology(msg.getOntology());
  				Exists e = new Exists(OntoAID.wrap(myAgent.getAID()));
  				myAgent.getContentManager().fillContent(act, e);
  				act.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
  				
  				Action action = new Action(recv, OntoACLMessage.wrap(act));
  				myAgent.getContentManager().fillContent(msg, action);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				Action action = (Action) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				ACLMessage msg1 = (ACLMessage) action.getAction();
  				// Check the performative
  				if (msg1.getPerformative() != ACLMessage.INFORM) {
  					l.log("Wrong content: expected performative "+ACLMessage.INFORM+", found "+msg1.getPerformative());
  					return false;
  				}
  				// Check the content
  				Exists e = (Exists) myAgent.getContentManager().extractContent(msg1);
  				AID id = (AID) e.getWhat();
  				if (id.equals(myAgent.getAID())) {
  					l.log("Content OK");
  					return true;
  				}
  				else {
  					l.log("Wrong content: expected "+myAgent.getAID()+", found "+id);
  					return false;
  				}
  			}
  		};
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

}
