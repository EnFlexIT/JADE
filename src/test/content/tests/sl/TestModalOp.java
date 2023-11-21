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
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.ContentManager;
import jade.content.lang.sl.*;
import jade.content.abs.*;
import jade.content.onto.BasicOntology;
import examples.content.ecommerceOntology.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class TestModalOp extends Test{
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(SLOperatorsTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				Item it = new Item();
  				it.setSerialID(1000);
  				Exists e = new Exists(it);
  				
  				AbsPredicate i = new AbsPredicate(SLVocabulary.INTENTION);
  				i.set(SLVocabulary.INTENTION_AGENT, BasicOntology.getInstance().fromObject(new AID("John", AID.ISLOCALNAME)));
  				i.set(SLVocabulary.INTENTION_CONDITION, TestOntology.getInstance().fromObject(e));
  					
  				AbsPredicate b = new AbsPredicate(SLVocabulary.BELIEF);
  				b.set(SLVocabulary.BELIEF_CONDITION, i);
  	  		b.set(SLVocabulary.BELIEF_AGENT, BasicOntology.getInstance().fromObject(new AID("Bill", AID.ISLOCALNAME)));
  					
					AbsPredicate u = new AbsPredicate(SLVocabulary.UNCERTAINTY);
  				u.set(SLVocabulary.UNCERTAINTY_AGENT, BasicOntology.getInstance().fromObject(new AID("Peter", AID.ISLOCALNAME)));
  				u.set(SLVocabulary.UNCERTAINTY_CONDITION, b);
  				
  				myAgent.getContentManager().fillContent(msg, u);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				AbsPredicate u = (AbsPredicate) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				AbsPredicate b = (AbsPredicate) u.getAbsObject(SLVocabulary.UNCERTAINTY_CONDITION);
  				AbsPredicate i = (AbsPredicate) b.getAbsObject(SLVocabulary.BELIEF_CONDITION);
  				AbsPredicate e = (AbsPredicate) i.getAbsObject(SLVocabulary.INTENTION_CONDITION);
  				Exists ex = (Exists) TestOntology.getInstance().toObject(e);
  				Item it = (Item) ex.getWhat();
  				if (it.getSerialID() == 1000) {
	  				l.log("Content OK");
	  				return true;
  				}
  				else {
	  				l.log("Wrong content: expected 1000, found "+it.getSerialID());
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
