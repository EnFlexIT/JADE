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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Serializable;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.domain.mobility.*;
import jade.content.*;
import jade.content.onto.Ontology;
import jade.content.lang.Codec;
import jade.content.lang.leap.LEAPCodec;

import test.common.*;
import test.mobility.MobilityTesterAgent;

import java.io.*;

/**
   Test dynamic loading of behaviours.
   @author Giovanni Caire - TILAB
 */
public class TestLoadBehaviour extends Test {
	private static final String TEST_MESSAGE = "Test-message";
	private static final String LOADER_AGENT_NAME = "loader";
	
	private AID la;
	private int cnt = 0;
	private Ontology onto = BehaviourLoadingOntology.getInstance();
	private Codec codec = new LEAPCodec();
	private ContentManager myContentManager = new ContentManager();
	
  public Behaviour load(Agent a) throws TestException {
  	setTimeout(5000);
  	
  	myContentManager.registerLanguage(codec);
  	myContentManager.registerOntology(onto);
  	
  	// Launch the BehaviourLoaderAgent on Container-1
		String c1 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER1_KEY);
  	la = TestUtility.createAgent(a, LOADER_AGENT_NAME, "test.mobility.tests.TestLoadBehaviour$BehaviourLoaderAgent", null, null, c1);
		  	
		// Create the test behaviour
  	Behaviour b = new SimpleBehaviour(a) {
  		
  		public void onStart() {
  			// Send the LoadBehaviour request to the BehaviourLoaderAgent
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(la);
  			request.setLanguage(codec.getName());
  			request.setOntology(onto.getName());
  			List params = new ArrayList(3);
  			Parameter p = new Parameter("receiver", myAgent.getAID());
  			params.add(p);
  			p = new Parameter("test-param", new TestParam(TEST_MESSAGE));
  			params.add(p);
  			LoadBehaviour lb = new LoadBehaviour();
  			lb.setClassName("test.mobility.separate.behaviours.LoadableMsgSender");
  			lb.setParameters(params);
  			try {
  				FileInputStream str = new FileInputStream("separate-behaviours.jar");
  				int length = str.available();
  				byte[] zip = new byte[length];
  				str.read(zip, 0, length);
	  			lb.setZip(zip);
	  			myContentManager.fillContent(request, lb);	  			
	  			log(myAgent.getName()+": Sending LoadBehaviour request to "+la.getName());
	  			myAgent.send(request);
  			}
  			catch (IOException ioe) {
  				failed("Error reading behaviour jar file. "+ioe);
  			}
  			catch (Exception e) {
  				failed("Error encoding LoadBehaviour request. "+e);
  			}
  		}
  		
  		public void action() {
  			ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(la));
  			if (msg != null) {
  				switch(cnt) {
  				case 0: 
  					if (msg.getPerformative() == ACLMessage.INFORM) {
  						log(myAgent.getName()+": LoadBehaviour request served");
  						cnt++;
  					}
  					else {
  						failed(myAgent.getName()+": LoadBehaviour request failed. "+msg.getContent());
  					}
  					break;
  				case 1:
  					if (msg.getPerformative() == ACLMessage.INFORM) {
  						log(myAgent.getName()+": Message from dynamically loaded behaviour received");
  						if (TEST_MESSAGE.equals(msg.getContent())) {
  							passed(myAgent.getName()+": Serializable parameter OK. Test completed");
  						}
  						else {
  							failed(myAgent.getName()+": Unexpected Serializable parameter value. "+msg.getContent());
  						}
  					}
  					else {
							failed(myAgent.getName()+": Unexpected message received. "+msg.getContent());
  					}
  					cnt++;
  					break;
  				}
  			}
  			else {
  				block();
  			}
  		}
  		
  		public boolean done() {
  			return cnt >= 2;
  		}  		
  	};
  			
  	return b;
  }
  	  
  public void clean(Agent a) {
  	try {
	  	TestUtility.killAgent(a, la);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  
  
  /**
     Inner class TestParam
   */
	public static class TestParam implements Serializable {
		private String myMsg;
		
		public TestParam(String msg) {
			myMsg = msg;
		}
		
		public String getMessage() {
			return myMsg;
		}
	} // END of inner class TestParam
	
  /** 
     Inner class BehaviourLoaderAgent
   */
  public static class BehaviourLoaderAgent extends Agent {
  	protected void setup() {
  		addBehaviour(new LoaderBehaviour() {
  			protected boolean accept(ACLMessage msg) {
  				System.out.println(myAgent.getName()+": LOAD_BEHAVIOUR request received.");
  				return true;
  			}
  		} );
  	}
  }		
}
