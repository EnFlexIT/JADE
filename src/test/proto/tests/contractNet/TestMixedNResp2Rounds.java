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
public class TestMixedNResp2Rounds extends TestBase {
	public static final String TEST_NAME = "Mixed flow with N responders 2 rounds";
	
	public TestMixedNResp2Rounds() {
		responderBehaviours= new String[] {
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

  	BasicContractNetInitiator b1 = new BasicContractNetInitiator(a, msg, ds, null, 10000,
  		new int[] {4, 1, 1, 1, 2, 3});
  	
  	BasicContractNetInitiator b2 = new BasicContractNetInitiator(a, msg, ds, resultKey, 10000, 
  		new int[] {4, 1, 1, 1, 2, 3}) {
  		
  		public Vector prepareCfps(ACLMessage cfp) {
  			BasicContractNetInitiator p = (BasicContractNetInitiator) parent;
  			if (p.check()) {
  				System.out.println("Round 1 OK");
  				return super.prepareCfps(cfp);
  			}
  			else {
  				System.out.println("Error in round 1:");
  				p.printDetails();  				
  				return null;
  			}
  		}
  	};
  	
  	b1.registerHandleAllResultNotifications(b2);	
  	return b1;
  }
  
}

