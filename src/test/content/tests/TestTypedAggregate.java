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
import jade.content.onto.*;
import jade.content.onto.basic.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;
import examples.content.ecommerceOntology.*;
import examples.content.musicShopOntology.*;

public class TestTypedAggregate extends Test{
  public String getName() {
  	return "Wrong-type-in-typed-aggregate";
  }
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests a content including a typed aggregate containing an element of a wrong type");
  	return sb.toString();
  }
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		Object[] args = getGroupArguments();
  		final ACLMessage msg = (ACLMessage) args[0];
  		return new FailureExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				AbsAggregate agg = new AbsAggregate(BasicOntology.SEQUENCE);
  				// Add an element of the right type
  				AbsConcept t = new AbsConcept(MusicShopOntology.TRACK);
  				t.set(MusicShopOntology.TRACK_NAME, "Every breath you take");
  				agg.add(t);
  				// Add an element of a wrong type
  				AbsConcept i = new AbsConcept(ECommerceOntology.ITEM);
  				agg.add(i);
  		
  				AbsConcept cd = new AbsConcept(MusicShopOntology.CD);
  				cd.set(MusicShopOntology.CD_TITLE, "Synchronicity");
  				cd.set(MusicShopOntology.CD_TRACKS, agg);
  		
  				AbsPredicate e = new AbsPredicate(TestOntology.EXISTS);
  				e.set(TestOntology.EXISTS_WHAT, cd);
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
