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

package test.proto.tests.contractNet;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import test.common.*;
import test.proto.tests.TestBase;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
   @author Giovanni Caire - TILAB
 */
public class TestMixedNResp extends TestBase {
	public static final String TEST_NAME = "Mixed flow with N responders";
	
	public TestMixedNResp() {
		responderBehaviours = new String[] {
			"test.proto.responderBehaviours.contractNet.NotUnderstoodReplier",
			"test.proto.responderBehaviours.contractNet.RefuseReplier",
			"test.proto.responderBehaviours.contractNet.InformReplier",
			"test.proto.responderBehaviours.contractNet.RequestReplier",
			"test.proto.responderBehaviours.Dummy",
			"test.proto.responderBehaviours.contractNet.ProposeInformReplier",
			"test.proto.responderBehaviours.contractNet.ProposeFailureReplier",
			"test.proto.responderBehaviours.contractNet.ProposeRequestReplier",
			"test.proto.responderBehaviours.contractNet.ProposeNothingReplier"
		};
	}
	
  public String getName() {
  	return TEST_NAME;
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	ds.put(resultKey, new Integer(TEST_FAILED));
  	
  	ACLMessage msg = new ACLMessage(ACLMessage.CFP);
 		initialize(a, msg);
  	msg.addReceiver(new AID(new String(RESPONDER_NAME+9), AID.ISLOCALNAME));

  	return new BasicContractNetInitiator(a, msg, ds, resultKey, 10000,
  		new int[] {4, 1, 1, 1, 2, 3}); // 4 PROPOSE, 2 FAILURE, 3 OUT_OF_SEQ
  }
  
}

