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

package test.domain.df.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.util.leap.*;
import test.common.*;
import test.domain.df.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestRemoteRegistration extends Test {
	private static final String REMOTE_PLATFORM_NAME = "Remote-platform";
	private static final int REMOTE_PLATFORM_PORT = 9003;
	
	private JadeController jcp, jcc;
	private AID id;
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	// Launch a remote platform
		jcp = TestUtility.launchJadeInstance(REMOTE_PLATFORM_NAME, null, new String("-name "+REMOTE_PLATFORM_NAME+" -port "+REMOTE_PLATFORM_PORT), new String[]{"IOR"}); 

		// Construct the AID of the AMS of the remote platform 
		AID remoteAMS = new AID("ams@"+REMOTE_PLATFORM_NAME, AID.ISGUID);
		Iterator it = jcp.getAddresses().iterator();
		while (it.hasNext()) {
			remoteAMS.addAddresses((String) it.next());
		}
		
		// Launch another container with the IIOP MTP to communicate with the 
		// remote platform
		jcc = TestUtility.launchJadeInstance("Container-1", null, new String("-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)+" -mtp jade.mtp.iiop.MessageTransportProtocol"), null); 
		
		// Launch an agent on the remote platform. This agent will register to the local DF
		id = TestUtility.createAgent(a, "Remote-registerer", "test.domain.df.tests.TestRemoteRegistration$RemoteRegistrationAgent", null, remoteAMS, null);
		
  	Behaviour b = new OneShotBehaviour(a) {
  		int ret;
  		
  		public void action() {
  			Logger l = Logger.getLogger();
  			ret = Test.TEST_FAILED;
  			
  			// Send the startup message
  			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  			msg.addReceiver(id);
  			myAgent.send(msg);
  			l.log(myAgent.getLocalName()+": Startup message sent");
  			
  			// Wait a bit
  			l.log(myAgent.getLocalName()+": Wait a bit to give time to the remote agent to register");
  			try {
  				Thread.sleep(5000);
  			}
  			catch (InterruptedException ie) {
  				ie.printStackTrace();
  			}
  			
  			// Search with the DF 
  			DFAgentDescription template = TestDFHelper.getSampleTemplate1();
  			DFAgentDescription[] result = null;
  			try {
	  			result = DFService.search(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints());
  			}
  			catch (FIPAException fe) {
  				l.log(myAgent.getLocalName()+": DF search-1 failed");
  				fe.printStackTrace();
  				return;
  			}	
  			l.log(myAgent.getLocalName()+": DF search-1 done");
  			if (result.length != 1 || (!id.equals(result[0].getName()))) {
  				l.log(myAgent.getLocalName()+": DF search-1 result different from what was expected");
  				return;
  			}
  			l.log(myAgent.getLocalName()+": DF search-1 result OK");
  			
  			// Send the continuation message
  			myAgent.send(msg);
  			l.log(myAgent.getLocalName()+": Continuation message sent");
  			
  			// Wait a bit
  			l.log(myAgent.getLocalName()+": Wait a bit to give time to the remote agent to de-register");
  			try {
  				Thread.sleep(5000);
  			}
  			catch (InterruptedException ie) {
  				ie.printStackTrace();
  			}
  			
  			// Search again with the DF 
  			try {
	  			result = DFService.search(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints());
  			}
  			catch (FIPAException fe) {
  				l.log(myAgent.getLocalName()+": DF search-2 failed");
  				fe.printStackTrace();
  				return;
  			}	
  			l.log(myAgent.getLocalName()+": DF search-2 done");
  			if (result.length > 0) {
  				l.log(myAgent.getLocalName()+": DF search-2 failed: no result expected, found "+result.length);
  				return;
  			}
  			l.log(myAgent.getLocalName()+": DF search-2 no result found as expected. OK");

  			// If we get here the test is PASSED
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
		jcp.kill();
		jcc.kill();
  }
  
  /** 
     Inner class RemoteRegistrationAgent
   */
  public static class RemoteRegistrationAgent extends Agent {
  	private Logger l = Logger.getLogger();
  	
  	protected void setup() {
  		// Wait for the startup message
  		ACLMessage msg = blockingReceive();
  		l.log(getLocalName()+": Startup message received. Registering with remote DF...");
  		
  		// Construct the remote DF AID
  		AID remoteDF = TestUtility.createNewAID("df", msg.getSender());
  		
			// Register with the remote DF
			DFAgentDescription dfd = TestDFHelper.getSampleDFD(getAID());
			try {
  			DFService.register(this, remoteDF, dfd);
			}
			catch (FIPAException fe) {
				l.log(getLocalName()+": DF registration failed");
				fe.printStackTrace();
				return;
			}	
			l.log(getLocalName()+": DF registration done");
  		
  		// Wait for the continuation message
  		msg = blockingReceive();
  		l.log(getLocalName()+": Continuation message received. Deregistering with remote DF...");
  		
			// Deregister with the remote DF
			try {
  			DFService.deregister(this, remoteDF, new DFAgentDescription());
			}
			catch (FIPAException fe) {
				l.log(getLocalName()+": DF de-registration failed");
				fe.printStackTrace();
				return;
			}	
			l.log(getLocalName()+": DF de-registration done");
  	}
  }
}
