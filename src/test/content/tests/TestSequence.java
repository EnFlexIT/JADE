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
import jade.util.leap.*;
import examples.content.ecommerceOntology.*;
import examples.content.musicShopOntology.*;
import test.common.*;
import test.content.*;
import test.content.testOntology.Exists;

public class TestSequence extends Test{
  public String getName() {
  	return "Sequence-attribute";
  }
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests a content including a concept with an aggregate attribute of type SEQUENCE");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		//Object[] args = getGroupArguments();
  		//final ACLMessage msg = (ACLMessage) args[0];
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.INFORM_MSG_NAME);;
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
					CD cd = new CD();
					cd.setSerialID(11111);
					cd.setTitle("Synchronicity");
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
					cd.setTracks(tracks);
  		
  				Exists e = new Exists(cd);
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
