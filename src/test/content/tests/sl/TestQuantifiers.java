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

package test.content.tests.sl;

import test.common.*;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.ContentManager;
import jade.content.lang.sl.*;
import jade.content.abs.*;
import examples.content.ecommerceOntology.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class TestQuantifiers extends Test{
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(SLOperatorsTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				AbsVariable x = new AbsVariable("x", TestOntology.LOCATION);
  				AbsVariable y = new AbsVariable("y", TestOntology.LOCATION);
  				
  				AbsPredicate close = new AbsPredicate(TestOntology.CLOSE);
  				close.set(TestOntology.CLOSE_WHERE, x);
  				close.set(TestOntology.CLOSE_TO, y);
  				
  				AbsPredicate exists = new AbsPredicate(SLVocabulary.EXISTS);
  				exists.set(SLVocabulary.EXISTS_CONDITION, close);
  				exists.set(SLVocabulary.EXISTS_WHAT, y);
  				
  				AbsPredicate forall = new AbsPredicate(SLVocabulary.FORALL);
  				forall.set(SLVocabulary.FORALL_WHAT, x);
  				forall.set(SLVocabulary.FORALL_CONDITION, exists);
  				
  				myAgent.getContentManager().fillContent(msg, forall);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				AbsPredicate forall = (AbsPredicate) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				AbsPredicate exists = (AbsPredicate) forall.getAbsObject(SLVocabulary.FORALL_CONDITION);
  				AbsPredicate c = (AbsPredicate) exists.getAbsObject(SLVocabulary.EXISTS_CONDITION);
  				AbsVariable y = (AbsVariable) c.getAbsTerm(TestOntology.CLOSE_TO);
  				if (y.getName().equals("y")) {
  					l.log("Content OK");
  					return true;
  				}
  				else {
	  				l.log("Wrong content: expected variable name y, found "+y.getName());
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
