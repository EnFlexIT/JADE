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
import test.common.*;
import test.common.behaviours.*;
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
  	sb.append("This test requests an example.mobile.MobileAgent to move to another container and then\n");
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
		
  	String c1 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER1_KEY);
  	String c2 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER2_KEY);
		List containers = new ArrayList();
		containers.add(c1);
		containers.add(c2);
		containers.add(c2);
		containers.add(a.here().getName());
		
  	Behaviour b = new ListProcessor(a, containers) {
  		
  		public void processItem(Object item, int index) {
  			String cName = (String) item;
  			// Request the mobile agent to move
  			l.log("Requesting MobileAgent to move to "+cName);
				request.setContent(new String("move "+cName));
				myAgent.send(request);
				// Give the mobile agent some time to move
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				// Check the current position of the mobile agent
				myAgent.addBehaviour(new PositionChecker(myAgent/*, ma*/, cName, this));
				pause();
  		}
  		
  		public int onEnd() {
  			if (isStopped()) {
  				store.put(key, new Integer(Test.TEST_FAILED));
  			}
  			else {
  				store.put(key, new Integer(Test.TEST_PASSED));
  			}
  			return 0;
  		}  			
  	};
  	
  	return b;
  }
  	  
  public void clean(Agent a) {
  	try {
	  	TestUtility.killAgent(a, ma);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  
	class PositionChecker extends SimpleBehaviour {
		private String containerName;
		//private ACLMessage request;
		private MessageTemplate mt;
		private ListProcessor toBeResumed;
		private boolean finished = false;
		
		PositionChecker(Agent a/*, AID ma*/, String name, ListProcessor b) {
			super(a);
			
			containerName = name;
			//request = new ACLMessage(ACLMessage.REQUEST);
			//request.addReceiver(ma);
			//mt = MessageTemplate.MatchInReplyTo(request.getReplyWith());
			toBeResumed = b;
		}
		
		public void onStart() {
  		l.log("Requesting MobileAgent to tell his current location");
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(ma);
			request.setReplyWith("R_"+hashCode()+"_"+System.currentTimeMillis());
			request.setContent("where-are-you");
			myAgent.send(request);
			mt = MessageTemplate.MatchInReplyTo(request.getReplyWith());
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (containerName.equals(msg.getContent())) {
					l.log("MobileAgent current location is "+msg.getContent()+" as expected");
					toBeResumed.resume();
				}
				else {
					l.log("Error: MobileAgent current location is "+msg.getContent()+" while "+containerName+" was expected");
					toBeResumed.stop();
				}
				finished = true;
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
