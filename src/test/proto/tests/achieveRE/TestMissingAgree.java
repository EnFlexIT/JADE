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
public class TestMissingAgree extends TestBase {
	public static final String TEST_NAME = "Missing AGREE";
	private Behaviour b;
	
	public TestMissingAgree() {
		responderBehaviours= new String[] {
			"test.proto.responderBehaviours.achieveRE.AgreeInformReplier",
			"test.proto.responderBehaviours.achieveRE.InformReplier",
			"test.proto.responderBehaviours.achieveRE.FailureReplier"
		};
	}
	
  public String getName() {
  	return TEST_NAME;
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests the protocol when a responder directly replies with the result notification (INFORM or FAILURE) and no AGREE message is sent");
  	sb.append("\nMore in details there will be three responders");
  	sb.append("\nResponder-0 behaves normally (i.e. AGREE, INFORM)");
  	sb.append("\nResponder-1 replies with INFORM");
  	sb.append("\nResponder-2 replies with FAILURE");
  	sb.append("\nAll handlers are defined overriding methods");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	ds.put(resultKey, new Integer(TEST_FAILED));
 
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
 		initialize(a, msg);
  	
  	return new BasicAchieveREInitiator(a, msg, ds, resultKey, 10000, 
  		new int[] {1, 0, 0, 2, 1, 0}); // 1 AGREE, 2 INFORM and 1 FAILURE 		
  }  
  
}

