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

package test.domain.ams.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import test.common.*;
import test.domain.ams.*;

/**
   Performs the following steps:
   1) Requests the AMS to create an agent on a given container
   2) Send a message to that agent and gets back a reply
   3) Requests the AMS to create another agent with the same name.
   Should get back a FAILURE
   4) Requests the AMS to kill the agent
   5) Send a message to the killed agent. Should get back a FAILURE
   from the AMS.
   @author Giovanni Caire - TILAB
 */
public class TestCreateKillAgent extends Test {
	
	private JadeController jc;
  private int ret;
	private AID target = null;
  private Logger l = Logger.getLogger();
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
		jc = TestUtility.launchJadeInstance("Container-1", null, new String("-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)), null); 
		ret = Test.TEST_FAILED;
  	
		SequentialBehaviour sb = new SequentialBehaviour(a) {
  		public int onEnd() {
  			store.put(key, new Integer(ret));
  			return 0;
  		}	
  	};
  		
   	// 1) Requests the AMS to create an agent on a given container
  	Behaviour b = new OneShotBehaviour(a) {			
  		public void action() {
  			try {
  				target = TestUtility.createAgent(myAgent, "target", Replier.class.getName(), null, null, jc.getContainerName());
  			}
  			catch (TestException te) {
  				l.log("Error creating target agent.");
  				te.printStackTrace();
  				((SequentialBehaviour) parent).skipNext();
  			}
  			l.log("Target agent correctly created.");
  		}
  	};
  	sb.addSubBehaviour(b);
  			
   	// 2) Send a message to that agent and gets back a reply
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(target);
  			msg.setReplyWith("R1-"+myAgent.getLocalName()+System.currentTimeMillis());
  			myAgent.send(msg);
  			msg = myAgent.blockingReceive(MessageTemplate.MatchInReplyTo(msg.getReplyWith()), 5000);
  			if (msg != null) {
	  			if (!msg.getSender().equals(target)) {
	  				l.log("Target agent didn't reply.");
	  				((SequentialBehaviour) parent).skipNext();
	  			}
	  			l.log("Target agent replied correctly.");
  			}
  			else {
	  			l.log("No reply received from target.");
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
  			
		// 3) Requests the AMS to create another agent with the same name.
		// Should get back a FAILURE
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			try {
  				target = TestUtility.createAgent(myAgent, "target", Replier.class.getName(), null);
	  			l.log("Creation of another agent with the same name erroneously suceeded.");
  				((SequentialBehaviour) parent).skipNext();
  			}
  			catch (TestException te) {
  				if (te.getNested() instanceof FIPAException) {
	  				l.log("Creation of another agent with the same name generated a FIPAException as expected.");
  				}
  				else {
	  				l.log("Creation of another agent with the same name generated an unexpected exception.");
  					te.printStackTrace();
  					((SequentialBehaviour) parent).skipNext();
  				}
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
   
  	// 4) Requests the AMS to kill the target agent
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			try {
  				TestUtility.killAgent(myAgent, target);
  			}
  			catch (TestException te) {
  				l.log("Error killing target agent.");
  				te.printStackTrace();
  				((SequentialBehaviour) parent).skipNext();
  			}
  			l.log("Target agent correctly killed.");
  		}
  	};
  	sb.addSubBehaviour(b);
  		  	
   	// 5) Send a message to the killed agent. Should get back a FAILURE
   	// from the AMS
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(target);
  			msg.setReplyWith("R2-"+myAgent.getLocalName()+System.currentTimeMillis());
  			myAgent.send(msg);
  			msg = myAgent.blockingReceive(MessageTemplate.MatchInReplyTo(msg.getReplyWith()), 5000);
  			if (msg != null) {
	  			if (msg.getPerformative() == ACLMessage.FAILURE && msg.getSender().equals(myAgent.getAMS())) {
	  				l.log("FAILURE message received from the AMS as expected.");
	  			}
	  			else {
		  			l.log("Unexpected message received.");
	  				((SequentialBehaviour) parent).skipNext();
	  			}
  			}
  			else {
	  			l.log("No reply received at all.");
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);

  	// If all steps were OK the test is passed
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			ret = Test.TEST_PASSED;
  		}
  	};
  	sb.addSubBehaviour(b);
  		  	
  	return sb;
  }
  
  public void clean(Agent a) {
  	jc.kill();
  }
}
