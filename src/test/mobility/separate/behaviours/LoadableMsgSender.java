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

package test.mobility.separate.behaviours;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import test.mobility.tests.TestLoadBehaviour;

/**
   This behaviour is dinamically loaded within the TestLoadBehaviour
   test. It gets a receiver AID and a Serializable value as 
   parameters and after a while sends a message showing that the Serializable
   value has been correctly received to the receiver AID.
   @author Giovanni Caire - TILAB
 */
public class LoadableMsgSender extends WakerBehaviour {
	public LoadableMsgSender() {
		super(null, 2000);
	}
	
	public void handleElapsedTimeout() {
		AID receiver = (AID) getDataStore().get(TestLoadBehaviour.TEST_PARAM0);
		TestLoadBehaviour.TestParam tp = (TestLoadBehaviour.TestParam) getDataStore().get(TestLoadBehaviour.TEST_PARAM1);
		// This is just to have an inner class in the jar file
		DummyLogger myLogger = new DummyLogger();
		myLogger.log(myAgent.getName()+": Receiver is "+receiver.getName()+". Message is "+tp.getMessage());
		ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
		msg.addReceiver(receiver);
		msg.setContent(tp.getMessage());
		myAgent.send(msg);
		
		// Finally fill output parameter
		getDataStore().put(TestLoadBehaviour.TEST_PARAM2, TestLoadBehaviour.EXPECTED_OUT_VAL);
	}
	
	/**
	   Inner class DummyLogger
	 */
	private class DummyLogger {
		public void log(String s) {
			System.out.println(s);
		}
	}
}
