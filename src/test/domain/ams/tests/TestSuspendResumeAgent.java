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
import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import test.common.*;
import test.domain.ams.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestSuspendResumeAgent extends Test {
	private static final long MAX_TIME = 10000;
	private static final String T1 = "test-1";
	private static final String T2 = "test-2";
	
	private JadeController jc;
  private int ret;
	private AID target = null;
  private Logger l = Logger.getLogger();
  private ContentManager cm = new ContentManager();
  private Codec codec = new SLCodec();
  private Ontology fipaOnto = FIPAManagementOntology.getInstance();
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	cm.registerLanguage(codec);
  	cm.registerOntology(fipaOnto);
  	
  	// Create a peripheral container and a target agent on it
		jc = TestUtility.launchJadeInstance("Container-1", null, new String("-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)), null); 
  	target = TestUtility.createAgent(a, "target", Replier.class.getName(), null, null, jc.getContainerName());
		ret = Test.TEST_FAILED;
  	
		SequentialBehaviour sb = new SequentialBehaviour(a) {
  		public int onEnd() {
  			store.put(key, new Integer(ret));
  			return 0;
  		}	
  	};
  		
   	// 1) Send a message to the target agent and gets back a reply (just to be sure evrything is OK)
  	Behaviour b = new OneShotBehaviour(a) {	
  		public void action() {
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(target);
  			msg.setReplyWith(T1);
  			myAgent.send(msg);
  			l.log(myAgent.getLocalName()+": Test message 1 sent. waiting for reply from target agent...");
  			msg = myAgent.blockingReceive(MessageTemplate.MatchInReplyTo(T1), MAX_TIME);
  			if (msg != null) {
	  			if (!msg.getSender().equals(target)) {
	  				l.log(myAgent.getLocalName()+": Wrong reply received.");
	  				((SequentialBehaviour) parent).skipNext();
	  			}
	  			l.log(myAgent.getLocalName()+": Target agent replied correctly.");
  			}
  			else {
	  			l.log(myAgent.getLocalName()+": No reply received from target agent.");
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
  			
		// 2) Requests the AMS to suspend the target agent
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			Modify m = new Modify();
  			AMSAgentDescription dsc = new AMSAgentDescription();
  			dsc.setName(target);
  			dsc.setState(AMSAgentDescription.SUSPENDED);
  			m.setDescription(dsc);
  			Action slAct = new Action(myAgent.getAMS(), m);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(fipaOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				l.log(myAgent.getLocalName()+": Suspending target agent...");
  				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request, MAX_TIME);
  				if (inform != null) {
  					Done d = (Done) cm.extractContent(inform);
	  				l.log(myAgent.getLocalName()+": Target agent correctly suspended");
  				}
  				else {
	  				l.log(myAgent.getLocalName()+": No reply received from the AMS");
	  				((SequentialBehaviour) parent).skipNext();
  				}
  			}
  			catch (OntologyException oe) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding suspension request/reply.");
  				oe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Codec.CodecException ce) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding suspension request/reply.");
  				ce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log(myAgent.getLocalName()+": Suspension error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (ClassCastException cce) {
  				l.log(myAgent.getLocalName()+": Unexpected suspension result.");
  				cce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log(myAgent.getLocalName()+": Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
   
   	// 3) Send a message to the target agent. As it is suspended it 
  	// should not reply 
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(target);
  			msg.setReplyWith(T2);
  			myAgent.send(msg);
  			l.log(myAgent.getLocalName()+": Test message 2 sent. waiting for reply from target agent...");
  			msg = myAgent.blockingReceive(MessageTemplate.MatchInReplyTo(T2), MAX_TIME);
  			if (msg != null) {
	  			l.log(myAgent.getLocalName()+": Target agent unexpectedly replied.");
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			else {
	  			l.log(myAgent.getLocalName()+": No reply received from target agent as expected.");
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
  			
		// 4) Requests the AMS to resume the target agent
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			Modify m = new Modify();
  			AMSAgentDescription dsc = new AMSAgentDescription();
  			dsc.setName(target);
  			dsc.setState(AMSAgentDescription.ACTIVE);
  			m.setDescription(dsc);
  			Action slAct = new Action(myAgent.getAMS(), m);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(fipaOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				l.log(myAgent.getLocalName()+": Resuming target agent...");
  				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request, MAX_TIME);
  				if (inform != null) {
  					Done d = (Done) cm.extractContent(inform);
	  				l.log(myAgent.getLocalName()+": Target agent correctly resumed");
  				}
  				else {
	  				l.log(myAgent.getLocalName()+": No reply received from the AMS");
	  				((SequentialBehaviour) parent).skipNext();
  				}
  			}
  			catch (OntologyException oe) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding resumption request/reply.");
  				oe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Codec.CodecException ce) {
  				l.log(myAgent.getLocalName()+": Error encoding/decoding resumption request/reply.");
  				ce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log(myAgent.getLocalName()+": Resumption error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (ClassCastException cce) {
  				l.log(myAgent.getLocalName()+": Unexpected resumption result.");
  				cce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log(myAgent.getLocalName()+": Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
   
   	// 5) Wait for the previous reply
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			l.log(myAgent.getLocalName()+": Waiting again for reply to test message 2 from target agent...");
  			ACLMessage msg = myAgent.blockingReceive(MessageTemplate.MatchInReplyTo(T2), MAX_TIME);
  			if (msg != null) {
	  			if (!msg.getSender().equals(target)) {
	  				l.log(myAgent.getLocalName()+": Wrong reply received.");
	  				((SequentialBehaviour) parent).skipNext();
	  			}
	  			l.log(myAgent.getLocalName()+": Target agent replied correctly.");
  			}
  			else {
	  			l.log(myAgent.getLocalName()+": No reply received from target agent.");
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
