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

package test.proto.tests.achieveRE;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import test.common.*;
import test.proto.tests.TestBase;
import test.proto.responderBehaviours.achieveRE.*;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
   @author Giovanni Caire - TILAB
 */
public class TestTimeoutAndOutOfSeq extends TestBase {
	public static final String TEST_NAME = "Timeout and Out-of-sequence";
	private Behaviour b;
	
	public TestTimeoutAndOutOfSeq() {
		responderBehaviours= new String[] {
			"test.proto.responderBehaviours.achieveRE.AgreeInformReplier",
			"test.proto.responderBehaviours.achieveRE.FailureReplier",
			"test.proto.responderBehaviours.achieveRE.RefuseReplier",
			"test.proto.responderBehaviours.achieveRE.RequestReplier",
			"test.proto.responderBehaviours.achieveRE.NullReplier"
		};
	}
	
  public String getName() {
  	return TEST_NAME;
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests the protocol when a responder does not reply or reply with a wrong message");
  	sb.append("\nMore in details there will be 5 responders");
  	sb.append("\nResponder-0 replies with AGREE and INFORM)");
  	sb.append("\nResponder-0 replies with FAILURE)");
  	sb.append("\nResponder-2 replies with REFUSE");
  	sb.append("\nResponder-3 replies with REQUEST (an out of sequence message)");
  	sb.append("\nResponder-4 does not reply at all");
  	sb.append("\nNOTES:");
  	sb.append("\n- This test also checks that the handleAllResponses handler is properly called");
  	sb.append("\n- This test will take ~ 10 sec as the timeout must expire");
  	sb.append("\n- All handlers are defined overriding methods");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	ds.put(resultKey, new Integer(TEST_FAILED));
 
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
 		initialize(a, msg);
  	
  	return new BasicAchieveREInitiator(a, msg, ds, resultKey, 10000, 
  		new int[] {1, 1, 0, 1, 1, 1}) { // 1 AGREE, 1 REFUSE, 1 INFORM, 1 FAILURE and 1 OUT_OF_SEQ
  		
  		boolean handleAllResponsesCalled = false;
  		protected void handleAllResponses(Vector responses) {
  			handleAllResponsesCalled = true;
  		}
  		
  		protected void handleAllResultNotifications(Vector resultNotifications) {
  			if (!handleAllResponsesCalled) {
  				System.out.println("handleAllResponses() not called");
  			}
  			else {
  				super.handleAllResultNotifications(resultNotifications);
  			}
  		}	
  	};
  }  
  
}

