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
public class TestAbsDescriptorAsConcept extends Test{
  public String getName() {
  	return "AbsConcept-as-Java-class";
  }

  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		//Object[] args = getGroupArguments();
  		//final ACLMessage msg = (ACLMessage) args[0];
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.INFORM_MSG_NAME);;
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				AbsConcept p = new AbsConcept(TestOntology.POSITION);
  				p.set(TestOntology.POSITION_X, 1.1);
  				p.set(TestOntology.POSITION_Y, 2.2);
  				p.set(TestOntology.POSITION_PRECISE, true);
  				
  				AbsConcept l = new AbsConcept(TestOntology.LOCATION);
  				l.set(TestOntology.LOCATION_NAME, "Office");
  				l.set(TestOntology.LOCATION_POSITION, p);
  				
  				Exists e = new Exists(l);
  				myAgent.getContentManager().fillContent(msg, e);
  				return msg;
  			}
  		};
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

}
