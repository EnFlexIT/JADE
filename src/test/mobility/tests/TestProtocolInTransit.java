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

package test.mobility.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.util.leap.*;
import jade.proto.ContractNetResponder;
import jade.domain.FIPAAgentManagement.*;
import test.common.*;
import test.common.behaviours.*;
import test.mobility.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestProtocolInTransit extends Test {
	private static final String MOBILE_AGENT_NAME = "ma";
	private String[] responders = new String[]{"r1", "r2", "r3"};
	private AID ma, r1, r2, r3;
	private JadeController jc;
  private Logger l = Logger.getLogger();
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	// Launch Container 3 with the additional classpath
  	String additionalClasspath = (String) getGroupArgument(MobilityTesterAgent.ADDITIONAL_CLASSPATH_KEY);
  	jc = TestUtility.launchJadeInstance("Container-3", "+"+additionalClasspath, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT), null);

  	// Launch the MobileInitiatorAgent on Container3
  	ma = TestUtility.createAgent(a, MOBILE_AGENT_NAME, "test.mobility.separate.MobileInitiatorAgent", null, null, jc.getContainerName());
		
  	// Launch the responders on Container1, Container2 and Main
  	r1 = TestUtility.createAgent(a, responders[0], "test.mobility.tests.TestProtocolInTransit$ResponderAgent", null, null, (String) getGroupArgument(MobilityTesterAgent.CONTAINER1_KEY));
  	r2 = TestUtility.createAgent(a, responders[1], "test.mobility.tests.TestProtocolInTransit$ResponderAgent", null, null, (String) getGroupArgument(MobilityTesterAgent.CONTAINER2_KEY));
  	r3 = TestUtility.createAgent(a, responders[2], "test.mobility.tests.TestProtocolInTransit$ResponderAgent", null, null, a.here().getName());
  	
		// Create the test behaviour
  	Behaviour b = new SimpleBehaviour(a) {
  		private int ret = Test.TEST_FAILED;
  		private int status = 0;
  		
  		public void onStart() {
  			// Send the startup message to the MobileInitiatorAgent
  			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
  			inform.addReceiver(ma);
  			inform.setContent(responders[0]+" "+responders[1]+" "+responders[2]);
  			myAgent.send(inform);
  			l.log(myAgent.getLocalName()+": Startup message sent");
  		}
  		
  		public void action() {
  			ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(ma));
  			if (msg != null) {
  				switch(status) {
  				case 0: 
  					if (msg.getContent().equals("responses")) {
  						l.log(myAgent.getLocalName()+": Responses INFORM received");
  						status = 1;
  					}
  					else {
  						l.log(myAgent.getLocalName()+": Unexpected INFORM received");
  						status = 3; // Means error
  					}
  					break;
  				case 1:
  					if (msg.getContent().equals("notifications")) {
  						l.log(myAgent.getLocalName()+": Notifications INFORM received");
  						status = 2;
  						ret = Test.TEST_PASSED;
  					}
  					else {
  						l.log(myAgent.getLocalName()+": Unexpected INFORM received");
  						status = 3; // Means error
  					}
  					break;
  				}
  			}
  			else {
  				block();
  			}
  		}
  		
  		public boolean done() {
  			return status >= 2;
  		}
  		
  		public int onEnd() {
  			store.put(key, new Integer(ret));
  			return 0;
  		}
  	};
  			
  	return b;
  }
  	  
  public void clean(Agent a) {
  	try {
	  	TestUtility.killAgent(a, ma);
	  	TestUtility.killAgent(a, r1);
	  	TestUtility.killAgent(a, r2);
	  	TestUtility.killAgent(a, r3);
	  	jc.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  
  
  /** 
     Inner class ResponderAgent
   */
  public static class ResponderAgent extends Agent {
  	protected void setup() {
  		addBehaviour(new ContractNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)) {
    		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
    			Logger.getLogger().log(myAgent.getLocalName()+": CFP received");
    			ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
    			propose.setContent(myAgent.here().getName());
    			return propose;
    		}
    		
				protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
    			Logger.getLogger().log(myAgent.getLocalName()+": ACCEPT_PROPOSAL received");
    			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
    			inform.setContent(myAgent.here().getName());
    			return inform;
				}
  		} );
  	}
  }
}
