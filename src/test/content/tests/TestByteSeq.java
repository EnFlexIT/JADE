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
	private byte[] bytes = new byte[] {0, 1, 0, -128, 0, 127, 0};
  public String getName() {
  	return "Byte-sequence-attribute";
  }

  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	try {
  		//Object[] args = getGroupArguments();
  		//final ACLMessage msg = (ACLMessage) args[0];
  		final ACLMessage msg = (ACLMessage) getGroupArgument(ContentTesterAgent.INFORM_MSG_NAME);;
  		if (!msg.getLanguage().startsWith("FIPA-SL")) {
  			return new SuccessExpectedInitiator(a, ds, resultKey) {
  				protected ACLMessage prepareMessage() throws Exception {
  					Track t = new Track();
  					t.setName("Blowing in the wind");
  					t.setDuration(new Integer(240));
  					t.setPcm(bytes);
  					Exists e = new Exists(t);
  					myAgent.getContentManager().fillContent(msg, e);
  					return msg;
  				}
  				
  				protected boolean check(ACLMessage reply) {
  					if (super.check(reply)) {
  						// Also check that the sequence of byte is exactly the same as 
  						// the sent one.
  						try {
  							Exists e = (Exists) myAgent.getContentManager().extractContent(reply);
  							Track t = (Track) e.getWhat();
  							byte[] returnedBytes = t.getPcm();
  							return compare(bytes, returnedBytes);
  						}
  						catch (Exception ex) {
  							TestUtility.log("Unexpected exception while extracting returned sequence of bytes");
  							ex.printStackTrace();
  							return false;
  						}
  					}
  					else {
  						return false;
  					}
  				}		
  			};
  		}
  		else {
  			return new FailureExpectedInitiator(a, ds, resultKey) {
  				protected ACLMessage prepareMessage() throws Exception {
  					Track t = new Track();
  					t.setName("Blowing in the wind");
  					t.setDuration(new Integer(240));
  					t.setPcm(bytes);
  					Exists e = new Exists(t);
  					myAgent.getContentManager().fillContent(msg, e);
  					return msg;
  				}
  			};
  		}
  	}
  	catch (Exception e) {
  		throw new TestException("Wrong group argument", e);
  	}
  }

  private boolean compare(byte[] s1, byte[] s2) {
  	if (s1.length == s2.length) {
  		for (int i = 0; i < s1.length; ++i) {
  			if (s1[i] != s2[i]) {
  				return false;
  			}
  		}
  		return true;
  	}
  	else {
  		return false;
  	}
  }
}
