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
import test.common.*;
import test.mobility.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestDoMove extends Test {
	private static final String MOBILE_AGENT_NAME = "ma";
	
  private Logger l = Logger.getLogger();
	private int stepCnt = 0;
	private AID ma;
	
  public String getName() {
  	return "Test doMove()";
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests the mobility proactively initiated by an agent by means of a call to the doMove() method\n");
  	sb.append("This test requests an example.mobile.MobileAgent to move to another containerand then\n");
  	sb.append("checks it has actually moved by asking its current location\n");
  	sb.append("This process is repeated 4 times (including moving to the local container)\n");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	// Launch the MobileAGent
  	ma = TestUtility.createAgent(a, MOBILE_AGENT_NAME, "examples.mobile.MobileAgent", null, a.getAMS(), a.here().getName());
		
  	final ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(ma);
		
  	Behaviour b = new TickerBehaviour(a, 1000) {
  		int cnt = 0;
  		int ret = Test.TEST_FAILED;
  		String c1 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER1_KEY);
  		String c2 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER2_KEY);
  		
  		public void onTick() {
  			cnt++;
  			switch(cnt) {
  			case 1:
  				// Request the mobile agent to go to container 1
  				l.log("Requesting MobileAgent to move to "+c1);
					request.setContent(new String("move "+c1));
					request.setReplyWith("1");
					// don't even wait for the reply
  				break;
  			case 2:
  				// Request the mobile agent to tell his current location
  				l.log("Requesting MobileAgent to tell his current location");
					request.setContent("where-are-you");
					request.setReplyWith("2");
					myAgent.addBehaviour(new AnswerCollector(myAgent, c1, "2"));
  				break;
  			case 3:
  				// Request the mobile agent to go to container 2
  				l.log("Requesting MobileAgent to move to "+c2);
					request.setContent(new String("move "+c2));
					request.setReplyWith("3");
					// don't even wait for the reply
  				break;
  			case 4:
  				// Request the mobile agent to tell his current location
  				l.log("Requesting MobileAgent to tell his current location");
					request.setContent("where-are-you");
					request.setReplyWith("4");
					myAgent.addBehaviour(new AnswerCollector(myAgent, c2, "4"));
  				break;
  			case 5:
  				// Request the mobile agent to go to container 2
  				l.log("Requesting MobileAgent to move to "+c2);
					request.setContent(new String("move "+c2));
					request.setReplyWith("5");
					// don't even wait for the reply
  				break;
  			case 6:
  				// Request the mobile agent to tell his current location
  				l.log("Requesting MobileAgent to tell his current location");
					request.setContent("where-are-you");
					request.setReplyWith("6");
					myAgent.addBehaviour(new AnswerCollector(myAgent, c2, "6"));
  				break;
  			case 7:
  				// Request the mobile agent to go to the local container
  				l.log("Requesting MobileAgent to move to "+myAgent.here().getName());
					request.setContent(new String("move "+myAgent.here().getName()));
					request.setReplyWith("7");
					// don't even wait for the reply
  				break;
  			case 8:
  				// Request the mobile agent to tell his current location
  				l.log("Requesting MobileAgent to tell his current location");
					request.setContent("where-are-you");
					request.setReplyWith("8");
					myAgent.addBehaviour(new AnswerCollector(myAgent, myAgent.here().getName(), "8"));
  				break;
  			case 9:
  				// If we get here and the step counter is now 4 --> test passed
  				if (stepCnt == 4) {
	  				ret = Test.TEST_PASSED;
  				}
  				stop();
  				break;
  			}
  			myAgent.send(request);
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
	  	TestUtility.killTarget(a, ma);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  
	class AnswerCollector extends SimpleBehaviour {
		private String containerName;
		private MessageTemplate mt;
		private boolean finished = false;
		
		AnswerCollector(Agent a, String name, String inReplyTo) {
			super(a);
			
			containerName = name;
			mt = MessageTemplate.MatchInReplyTo(inReplyTo);
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (containerName.equals(msg.getContent())) {
					l.log("Notification received from MobileAgent: current location is "+msg.getContent());
					stepCnt++;
					finished = true;
				}
				else {
					l.log("Error: current location is "+msg.getContent()+" while "+containerName+" was expected");
				}
			}
			else {
				block();
			}		
		}
		
		public boolean done() {
			return finished;
		}
	}
}
