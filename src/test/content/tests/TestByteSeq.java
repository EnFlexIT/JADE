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
import test.common.*;
import test.content.*;
import test.content.testOntology.*;
import examples.content.musicShopOntology.*;

public class TestByteSeq extends Test{
  public String getName() {
  	return "Byte-sequence-attribute";
  }
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests a content including a concept with an attribute of type byte-sequence");
  	return sb.toString();
  }
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		Object[] args = getGroupArguments();
  		final ACLMessage msg = (ACLMessage) args[0];
  		return new SuccessExpectedInitiator(a, ds, resultKey) {
  			protected ACLMessage prepareMessage() throws Exception {
  				Track t = new Track();
  				t.setName("Blowing in the wind");
  				t.setDuration(new Integer(240));
  				t.setPcm(new byte[3000]);
  				Exists e = new Exists(t);
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
