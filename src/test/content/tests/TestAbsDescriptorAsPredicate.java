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
import jade.content.ContentManager;
import jade.content.abs.*;
import examples.content.ecommerceOntology.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestAbsDescriptorAsPredicate extends Test{

	public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				AbsConcept p1 = new AbsConcept(TestOntology.POSITION);
  				p1.set(TestOntology.POSITION_X, 1.1);
  				p1.set(TestOntology.POSITION_Y, 2.2);
  				p1.set(TestOntology.POSITION_PRECISE, true);
  				
  				AbsConcept l1 = new AbsConcept(TestOntology.LOCATION);
  				l1.set(TestOntology.LOCATION_NAME, "Office");
  				l1.set(TestOntology.LOCATION_POSITION, p1);
  				
  				AbsConcept p2 = new AbsConcept(TestOntology.POSITION);
  				p2.set(TestOntology.POSITION_X, 10.1);
  				p2.set(TestOntology.POSITION_Y, 20.2);
  				p2.set(TestOntology.POSITION_PRECISE, true);
  				
  				AbsConcept l2 = new AbsConcept(TestOntology.LOCATION);
  				l2.set(TestOntology.LOCATION_NAME, "Home");
  				l2.set(TestOntology.LOCATION_POSITION, p2);
  				
  				AbsPredicate c = new AbsPredicate(TestOntology.CLOSE);
  				c.set(TestOntology.CLOSE_WHERE, l1);
  				c.set(TestOntology.CLOSE_TO, l2);
  				myAgent.getContentManager().fillContent(msg, c);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				AbsPredicate c = (AbsPredicate) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				AbsConcept loc = (AbsConcept) c.getAbsTerm(TestOntology.CLOSE_WHERE);
  				if (loc.getTypeName().equals(TestOntology.LOCATION)) {
	  				l.log("Content OK");
	  				return true;
  				}
  				else {
	  				l.log("Wrong content: type expected "+TestOntology.LOCATION+", found "+loc.getTypeName());
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
