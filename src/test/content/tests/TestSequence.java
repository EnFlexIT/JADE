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
	private static final String T1 = "Synchronicity";
	private static final String T2 = "Every breath you take";
	private static final String T3 = "King of pain";
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final Logger l = Logger.getLogger();
  	
  	try {
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.MSG_NAME);
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
					CD cd = new CD();
					cd.setSerialID(11111);
					cd.setTitle("Synchronicity");
					List tracks = new ArrayList();
					Track t = new Track();
					t.setName(T1);
					tracks.add(t);
					t = new Track();
					t.setName(T2);
					tracks.add(t);
					t = new Track();
					t.setName(T3);
					t.setDuration(new Integer(240));
					tracks.add(t);
					cd.setTracks(tracks);
  		
  				Exists e = new Exists(cd);
  				myAgent.getContentManager().fillContent(msg, e);
  				l.log("Content correctly encoded");
  				l.log(msg.getContent());
  				return msg;
  			}
  			
  			protected boolean checkReply(ACLMessage reply) throws Exception {
  				Exists e = (Exists) myAgent.getContentManager().extractContent(reply);
  				l.log("Content correctly decoded");
  				CD cd = (CD) e.getWhat();
  				List tracks = cd.getTracks();
  				if (T1.equals(((Track) tracks.get(0)).getName()) &&
  					  T2.equals(((Track) tracks.get(1)).getName()) &&
  					  T3.equals(((Track) tracks.get(2)).getName()) ) {
  					l.log("Sequence OK");
  					return true;
  				}
  				else {
  					l.log("Wrong sequence");
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
