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
public class TestLogicalOp extends Test{
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(SLOperatorsTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				Item i1 = new Item();
  				i1.setSerialID(1000);
  				Item i2 = new Item();
  				i2.setSerialID(2000);
  				Item i3 = new Item();
  				i3.setSerialID(3000);
  				Exists e1 = new Exists(i1);
  				Exists e2 = new Exists(i2);
  				Exists e3 = new Exists(i3);
  				
  				AbsPredicate and = new AbsPredicate(SLVocabulary.AND);
  				and.set(SLVocabulary.AND_LEFT, TestOntology.getInstance().fromObject(e2));
  				and.set(SLVocabulary.AND_RIGHT, TestOntology.getInstance().fromObject(e3));
  				
  				AbsPredicate or = new AbsPredicate(SLVocabulary.OR);
  				or.set(SLVocabulary.OR_RIGHT, and);
  				or.set(SLVocabulary.OR_LEFT, TestOntology.getInstance().fromObject(e1));
  				
  				AbsPredicate not = new AbsPredicate(SLVocabulary.NOT);
  				not.set(SLVocabulary.NOT_WHAT, or);
  				
  				myAgent.getContentManager().fillContent(msg, not);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				AbsPredicate not = (AbsPredicate) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				AbsPredicate or = (AbsPredicate) not.getAbsObject(SLVocabulary.NOT_WHAT);
  				AbsPredicate and = (AbsPredicate) or.getAbsObject(SLVocabulary.OR_RIGHT);
  				AbsPredicate e = (AbsPredicate) and.getAbsObject(SLVocabulary.AND_LEFT);
  				Exists ex = (Exists) TestOntology.getInstance().toObject(e);
  				Item i = (Item) ex.getWhat();
  				if (i.getSerialID() == 2000) {
	  				l.log("Content OK");
	  				return true;
  				}
  				else {
	  				l.log("Wrong content: expected 2000, found "+i.getSerialID());
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
