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

import test.domain.df.*;
import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.WhereIsAgentAction;
import jade.lang.acl.*;
import test.common.*;
import test.domain.df.*;
import java.util.Date;

/**
 *
 * @author  chiarotto
 */
public class TestRegistrationRenewer extends Test {
  private static final String RENEWER_AGENT = "RenewerAgent";    
  private static final long PERIOD = 10000; // 10 sec
	private AID renewer;
	private ContainerID dfCid;
	
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	// Kills the DF and restarts it (on the same container) with a 
  	// properties file as a parameter.
  	// The properties file includes a maxleasetime indication that should 
  	// override the default one.
  	// This approach allows using this test with both the normal and
  	// the persistent DF.
  	try {
  		WhereIsAgentAction wiaa = new WhereIsAgentAction();
  		wiaa.setAgentIdentifier(a.getDefaultDF());
  		dfCid = (ContainerID) TestUtility.requestAMSAction(a, a.getAMS(), wiaa);
  	}
  	catch (ClassCastException cce) {
  		throw new TestException("Error getting DF location.", cce);
  	}
  	TestUtility.killAgent(a, a.getDefaultDF());
  	TestUtility.createAgent(a, "df", "jade.domain.df", new String[]{"test/domain/df/df.properties"}, a.getAMS(), dfCid.getName());
  	
  	// Create the Renewer-agent
    renewer = TestUtility.createAgent(a, RENEWER_AGENT, "test.domain.df.tests.TestRegistrationRenewerAgent", null);
      
  	Behaviour b = new OneShotBehaviour(a) {
            int ret = Test.TEST_FAILED;
            
            public void onStart() {
            	// Send the startup message to the Renewer-agent
            	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            	msg.addReceiver(renewer);
            	msg.setContent(String.valueOf(4*PERIOD)); // 40 sec
            	myAgent.send(msg);
            }
            
            public void action() {
                Logger l = Logger.getLogger();
                try{
                    l.log(myAgent.getLocalName()+": Wait "+(PERIOD/2000)+" secs to be sure the Renewer-agent has registered");
                    Thread.sleep(PERIOD/2);
                    
                		// Search 1 (after 5 sec)
                    if (!search(1)) {
                    	return;
                    }
                    
                    l.log(myAgent.getLocalName()+": Wait "+(PERIOD/1000)+" secs");
                    Thread.sleep(PERIOD);
                    
                    // Search 2 (after 15 sec)
                    if (!search(2)) {
                    	return;
                    }
                                        
                    l.log(myAgent.getLocalName()+": Wait "+(PERIOD/1000)+" secs");
                    Thread.sleep(PERIOD);
                    
                    // Search 3 (after 25 sec)
                    if (!search(3)) {
                    	return;
                    }
                    
                    l.log(myAgent.getLocalName()+": Wait "+(PERIOD/1000)+" secs");
                    Thread.sleep(PERIOD);
                    
                    // Search 4 (after 35 sec)
                    if (!search(4)) {
                    	return;
                    }
                    
                    l.log(myAgent.getLocalName()+": Wait "+(PERIOD/1000)+" secs to let the registration lease time expire");
                    Thread.sleep(PERIOD);
                    
                    // Search 5 (after 45 secs)
                    l.log(myAgent.getLocalName()+": 5) Search the DF ("+new Date()+"). Should find NO agent");
                    DFAgentDescription[] dfds = DFService.search(myAgent, new DFAgentDescription()); 
                    if(dfds.length != 0) {
                        l.log(myAgent.getLocalName()+": Search error: expected 0 agents, found "+dfds.length);
                        return;
                    }
                    l.log(myAgent.getLocalName()+": Search 5 OK");
                    
                    // If we get here the test is passed
                    ret = Test.TEST_PASSED;
                   
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            
            public int onEnd() {
                store.put(key, new Integer(ret));
                return 0;
            }
            
            private boolean search(int step) throws Exception {
            	Logger l = Logger.getLogger();
            	
              l.log(myAgent.getLocalName()+": "+step+") Search the DF ("+new Date()+"). Should find 1 agent");
              DFAgentDescription dfd = new DFAgentDescription();
              DFAgentDescription[] dfds = DFService.search(myAgent, dfd); 
              if(dfds.length == 1) {
                  DFAgentDescription dfa = (DFAgentDescription) dfds[0];
                  if(!dfa.getName().getLocalName().equals(RENEWER_AGENT)) {
                      l.log(myAgent.getLocalName()+": Search Error: expected agent "+RENEWER_AGENT+", found "+dfa.getName().getLocalName());
                      return false;
                  }
              }
              else {
                l.log(myAgent.getLocalName()+": Search Error: expected 1 agent, found "+dfds.length);
                return false;
              }
              l.log(myAgent.getLocalName()+": Search "+step+" OK");
              return true;
            }
        };
        
        return b;
  }
  
  public void clean(Agent a) {
  	try {
  		// Kill the Renewer-agent
      TestUtility.killAgent(a, renewer);
      
      // Restore the DF
	  	TestUtility.killAgent(a, a.getDefaultDF());
	  	TestUtility.createAgent(a, "df", "jade.domain.df", null, a.getAMS(), dfCid.getName());
  	}
  	catch (Exception fe) {
  		fe.printStackTrace();
  	}
  }
}
