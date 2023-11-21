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

package test.proto.tests.twoPh;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.proto.*;
import test.common.*;
import test.common.Logger;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

/**
   Test the Two phase commit protocol support in the case that
   all responders reply successfully in all steps.
   @author Giovanni Caire - TILAB
   @author Elena Quarantotto - TILAB
 */
public class TestPh0End extends Test {

  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
    final Logger l = Logger.getLogger();

    // Create and return the behaviour that will actually perform the test
  	Behaviour b = new TwoPhInitiator(a, null) {

  		public int onEnd() {
              int ret = Test.TEST_PASSED;
              TwoPh0Initiator ph0 = (TwoPh0Initiator) getPhase(PH0_STATE);
              l.log("\n\nLOG - (TwoPhInitiator, onEnd(), " + myAgent.getLocalName() + ") - " +
                      "PH0 return " + ph0.getState("Dummy-final").onEnd());
              if(ph0.getState("Dummy-final").onEnd() != 0) {
                  l.log("\n\nLOG - (TwoPhInitiator, onEnd(), " + myAgent.getLocalName() + ") - " +
                          "PH0 return " + ph0.getState("Dummy-final").onEnd() + " while 0 was expetced");
                  ret = Test.TEST_FAILED;
              }
              store.put(key, new Integer(ret));
              return 0;
  		}
  	};

  	return b;
  }

}
