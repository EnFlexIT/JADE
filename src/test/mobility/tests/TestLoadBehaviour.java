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

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.leap.LEAPCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.LoaderBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.mobility.BehaviourLoadingOntology;
import jade.domain.mobility.LoadBehaviour;
import jade.domain.mobility.Parameter;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Serializable;

import java.io.FileInputStream;
import java.io.IOException;

import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;
import test.mobility.MobilityTesterAgent;

/**
   Test dynamic loading of behaviours.
   @author Giovanni Caire - TILAB
 */
public class TestLoadBehaviour extends Test {
	public static final String TEST_PARAM0 = "receiver";
	public static final String TEST_PARAM1 = "test-param1";
	public static final String TEST_PARAM2 = "test-param2";
	public static final String EXPECTED_OUT_VAL = "XXXXXX";
	
	public static final String TEST_MESSAGE = "Test-message";
	public static final String LOADER_AGENT_NAME = "loader";
	
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
  			Parameter p = new Parameter(TEST_PARAM0, myAgent.getAID());
  			params.add(p);
  			p = new Parameter(TEST_PARAM1, new TestParam(TEST_MESSAGE));
  			params.add(p);
  			p = new Parameter(TEST_PARAM2, null, Parameter.OUT_MODE);
  			params.add(p);
  			LoadBehaviour lb = new LoadBehaviour();
  			lb.setClassName("test.mobility.separate.behaviours.LoadableMsgSender");
  			lb.setParameters(params);
			FileInputStream str = null;
  			try {
  				str = new FileInputStream("separate-behaviours.jar");
  				int length = str.available();
  				byte[] zip = new byte[length];
  				str.read(zip, 0, length);
  				lb.setZip(zip);
  				
  				Action actionExpr = new Action(myAgent.getAID(), lb);
	  			myContentManager.fillContent(request, actionExpr);	  			
	  			log(myAgent.getName()+": Sending LoadBehaviour request to "+la.getName());
	  			myAgent.send(request);
  			}
  			catch (IOException ioe) {
  				failed("Error reading behaviour jar file. "+ioe);
  			}
  			catch (Exception e) {
  				failed("Error encoding LoadBehaviour request. "+e);
  			} finally {
  				if (str != null) {
  					try {
						str.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
  				}
  			}
  		}
  		
  		public void action() {
  			ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(la));
  			if (msg != null) {
  				switch(cnt) {
  				case 0: 
  					if (msg.getPerformative() == ACLMessage.AGREE) {
  						log(myAgent.getName()+": LoadBehaviour request served");
  						cnt++;
  					}
  					else {
  						failed(myAgent.getName()+": LoadBehaviour request failed. "+msg.getContent());
  					}
  					break;
  				case 1:
  					if (msg.getPerformative() == ACLMessage.CONFIRM) {
  						log(myAgent.getName()+": Confirmation message from dynamically loaded behaviour received");
  						if (TEST_MESSAGE.equals(msg.getContent())) {
  							log(myAgent.getName()+": Serializable parameter OK.");
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
  				case 2:
  					if (msg.getPerformative() == ACLMessage.INFORM) {
  						log(myAgent.getName()+": Notification received");
  						try {
				  			Result r = (Result) myContentManager.extractContent(msg);
				  			List params = r.getItems();
				  			// Check the output parameter
				  			Parameter p = (Parameter) params.get(2);
				  			if (EXPECTED_OUT_VAL.equals(p.getValue())) {
				  				passed(myAgent.getName()+": Output parameter value OK.");
				  			}
				  			else {
				  				failed(myAgent.getName()+": Unexpected output parameter value "+p.getValue());
				  			}
  						}
  						catch (Exception e) {
  							failed(myAgent.getName()+": Error extracting notification content. "+e);
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
  			return cnt >= 3;
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
