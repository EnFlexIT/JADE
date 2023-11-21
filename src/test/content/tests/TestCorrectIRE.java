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
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.*;
import jade.content.abs.*;
import test.common.*;
import test.content.*;
import examples.content.ecommerceOntology.*;

public class TestCorrectIRE extends Test{
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				msg.setPerformative(ACLMessage.QUERY_REF);
  				AbsIRE ire = new AbsIRE("iota");
  				AbsVariable x = new AbsVariable("x", ECommerceOntology.PRICE);
  				AbsPredicate costs = new AbsPredicate(ECommerceOntology.COSTS);
  				costs.set(ECommerceOntology.COSTS_ITEM, new AbsConcept(ECommerceOntology.ITEM));
  				costs.set(ECommerceOntology.COSTS_PRICE, x);
  				ire.setVariable(x);
  				ire.setProposition(costs);
  				myAgent.getContentManager().fillContent(msg, ire);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				AbsIRE ire = (AbsIRE) myAgent.getContentManager().extractAbsContent(reply);
  				l.log("Content correctly decoded");
  				AbsPredicate costs = ire.getProposition();
  				AbsVariable x = (AbsVariable) costs.getAbsTerm(ECommerceOntology.COSTS_PRICE);
  				if (x.getName().equals("x")) {
  					l.log("IRE OK");
  					return true;
  				}
  				else {
  					l.log("Wrong IRE: expected variable name x, found "+x.getName());
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
