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
import jade.util.leap.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.*;
import examples.content.ecommerceOntology.*;
import examples.content.musicShopOntology.*;

public class TestCardinality extends Test{
  public String getName() {
  	return "Cardinality";
  }
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests a content with an aggregate including a wrong number of elements");
  	sb.append("\n");
  	sb.append("NOTE: This also tests adding a facet in a class for a slot defined in a superclass");
  	return sb.toString();
  }
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		//Object[] args = getGroupArguments();
  		//final ACLMessage msg = (ACLMessage) args[0];
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.INFORM_MSG_NAME);;
  		return new FailureExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
					Single s = new Single();
					s.setSerialID(11111);
					// A Single only has 2 tracks, but we add 3 of them
					s.setTitle("Synchronicity");
					List tracks = new ArrayList();
					Track t = new Track();
					t.setName("Synchronicity");
					tracks.add(t);
					t = new Track();
					t.setName("Every breath you take");
					tracks.add(t);
					t = new Track();
					t.setName("King of pain");
					t.setDuration(new Integer(240));
					tracks.add(t);
					s.setTracks(tracks);
  		
  				Exists e = new Exists(s);
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
