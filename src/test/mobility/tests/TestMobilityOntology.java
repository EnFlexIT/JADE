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
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.util.leap.*;
import jade.domain.mobility.*;
import jade.domain.*;
import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.abs.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;

import test.common.*;
import test.common.behaviours.*;
import test.mobility.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestMobilityOntology extends Test {
	private static final String MOBILE_AGENT_NAME = "ma";
	private AID ma;
	private String cloneName;
	private JadeController jc;
  private Logger l = Logger.getLogger();
  private Codec codec = new SLCodec();
  private Ontology mobOnto = MobilityOntology.getInstance();
  private ContentManager cm = new ContentManager();
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	// Launch Container 3 with the additional classpath
  	String additionalClasspath = (String) getGroupArgument(MobilityTesterAgent.ADDITIONAL_CLASSPATH_KEY);
  	jc = TestUtility.launchJadeInstance("Container-3", "+"+additionalClasspath, "-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT), null);

  	// Launch the MobileInitiatorAgent on Container3
  	ma = TestUtility.createAgent(a, MOBILE_AGENT_NAME, "test.mobility.separate.CodeMoverAgent", null, null, jc.getContainerName());
		
		// Create the test behaviour
  	Behaviour b = new OneShotBehaviour(a) {
  		private int ret = Test.TEST_FAILED;
  		
  		public void onStart() {
  			// Send the startup message to the MobileInitiatorAgent
  			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
  			inform.addReceiver(ma);
  			myAgent.send(inform);
  			l.log(myAgent.getLocalName()+": Startup message sent");
  			
  			// Register languages and ontologies
  			cm.registerLanguage(codec);
  			cm.registerOntology(mobOnto);
  		}
  		
  		public void action() {
  			// Step1: Request the AMS to move the mobile agent on C1
				String c1 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER1_KEY);
				MobileAgentDescription dsc = new MobileAgentDescription();
				dsc.setName(ma);
				dsc.setDestination(new ContainerID(c1, null));
				MoveAction moveAct = new MoveAction();
				moveAct.setMobileAgentDescription(dsc);
  			Action slAct = new Action(myAgent.getAMS(), moveAct);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(mobOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				ACLMessage inform = FIPAServiceCommunicator.doFipaRequestClient(myAgent, request);
  				Done d = (Done) cm.extractContent(inform);
  				l.log(myAgent.getLocalName()+": Agent correctly moved");
  			}
  			catch (OntologyException oe) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding MoveAction request/reply.");
  				oe.printStackTrace();
  				return;
  			}
  			catch (Codec.CodecException ce) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding MoveAction request/reply.");
  				ce.printStackTrace();
  				return;
  			}
  			catch (FIPAException fe) {
  				l.log(myAgent.getLocalName()+": Move action error.");
  				fe.printStackTrace();
  				return;
  			}
  			catch (ClassCastException cce) {
  				l.log(myAgent.getLocalName()+": MoveAction: Unexpected notification.");
  				cce.printStackTrace();
  				return;
  			}
  			catch (Exception e) {
  				l.log(myAgent.getLocalName()+": Unexpected error.");
  				e.printStackTrace();
  				return;
  			}
  			// Now wait for the confirmation from the mobile agent (at most 10 sec)
  			ACLMessage confirm = myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), 10000);
  			if (confirm == null) {
  				l.log(myAgent.getLocalName()+": Move confirmation message NOT received.");
  				return;
  			}
  			l.log(myAgent.getLocalName()+": Move confirmation message received");
  			
  			// Step2: Request the AMS to clone the mobile agent on C2
				String c2 = (String) getGroupArgument(MobilityTesterAgent.CONTAINER2_KEY);
				dsc.setDestination(new ContainerID(c2, null));
				CloneAction cloneAct = new CloneAction();
				cloneAct.setMobileAgentDescription(dsc);
				cloneName = "cloneof"+MOBILE_AGENT_NAME;
				cloneAct.setNewName(cloneName);
  			slAct.setAction(cloneAct);
  			try {
  				cm.fillContent(request, slAct);
  				ACLMessage inform = FIPAServiceCommunicator.doFipaRequestClient(myAgent, request);
  				Done d = (Done) cm.extractContent(inform);
  				l.log(myAgent.getLocalName()+": Agent correctly cloned");
  			}
  			catch (OntologyException oe) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding CloneAction request/reply.");
  				oe.printStackTrace();
  				return;
  			}
  			catch (Codec.CodecException ce) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding CloneAction request/reply.");
  				ce.printStackTrace();
  				return;
  			}
  			catch (FIPAException fe) {
  				l.log(myAgent.getLocalName()+": Clone action error.");
  				fe.printStackTrace();
  				return;
  			}
  			catch (ClassCastException cce) {
  				l.log(myAgent.getLocalName()+": CloneAction: Unexpected notification.");
  				cce.printStackTrace();
  				return;
  			}
  			catch (Exception e) {
  				l.log(myAgent.getLocalName()+": Unexpected error.");
  				e.printStackTrace();
  				return;
  			}
  			// Now wait for the confirmation from the mobile agent
  			confirm = myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), 10000);
  			if (confirm == null) {
  				l.log(myAgent.getLocalName()+": Clone confirmation message NOT received.");
  				return;
  			}
  			l.log(myAgent.getLocalName()+": Clone confirmation message received");
				
				// If we get here the test is passed.
				ret = Test.TEST_PASSED;
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
	  	TestUtility.killAgent(a, new AID(cloneName, AID.ISLOCALNAME));
	  	jc.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }    
}
