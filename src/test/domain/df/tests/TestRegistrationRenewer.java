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

/*
 * TestRegistrationRenewer.java
 *
 * Created on 31 gennaio 2003, 14.04
 */

package test.domain.df.tests;

import test.domain.df.*;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import test.common.*;
import test.domain.df.*;
import java.util.Date;

/**
 *
 * @author  chiarotto
 */
public class TestRegistrationRenewer extends Test {
    private static final String RENEWER_AGENT = "RenewerAgent";    
    private static final long LEASE_TIME = 10000; // 10 sec
	
  private int informCnt = 0;
	
  public String getName() {
  	return "Test DF Lease Time";
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	Behaviour b = new OneShotBehaviour(a) {
                
            // create agent 
            public void onStart() {
                try{
                  TestUtility.createAgent(myAgent,
                  RENEWER_AGENT,
                  "test.domain.df.tests.TestRegistrationRenewerAgent",
                  null,
                  myAgent.getAMS(),
                  null);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            int ret = Test.TEST_FAILED;
            
            public void action() {
                Logger l = Logger.getLogger();
                try{
                		// Search 1
                    l.log("Wait 5 secs");
                    Thread.sleep(5000);
                    l.log("Search the DF. Should find 1 agent");
                    DFAgentDescription dfd = new DFAgentDescription();
                    DFAgentDescription[] dfds = DFService.search(myAgent, dfd); 
                    if(dfds.length == 1) {
                        DFAgentDescription dfa = (DFAgentDescription) dfds[0];
                        if(!dfa.getName().getLocalName().equals(RENEWER_AGENT)) {
                            l.log("Search Error: expected agent "+RENEWER_AGENT+", found "+dfa.getName().getLocalName());
                            return;
                        }
                    }
                    else {
                      l.log("Search Error: expected 1 agent, found "+dfds.length);
                      return;
                    }
                    l.log("Search 1 OK");
                    
                    // Search 2
                    l.log("Wait 10 secs");
                    Thread.sleep(10000);
                    l.log("Search the DF. Should find 1 agent again");
                    dfds = DFService.search(myAgent, dfd); 
                    if(dfds.length == 1) {
                        DFAgentDescription dfa = (DFAgentDescription) dfds[0];
                        if(!dfa.getName().getLocalName().equals(RENEWER_AGENT)) {
                            l.log("Search Error: expected agent "+RENEWER_AGENT+", found "+dfa.getName().getLocalName());
                            return;
                        }
                    }
                    else {
                      l.log("Search Error: expected 1 agent, found "+dfds.length);
                      return;
                    }
                    l.log("Search 2 OK");
                    
                    // Search 3
                    l.log("Wait 10 secs to let the registration lease time expire");
                    Thread.sleep(10000);
                    l.log("Search the DF. Should find NO agent");
                    dfds = DFService.search(myAgent, dfd); 
                    if(dfds.length != 0) {
                        l.log("Search error: expected 0 agents, found "+dfds.length);
                        return;
                    }
                    l.log("Search 3 OK");
                    ret = Test.TEST_PASSED;
                   
                }catch(Exception e) {
                    e.printStackTrace();
                }
                
                
            }
            
            public int onEnd() {
                store.put(key, new Integer(ret));
                try {
                TestUtility.killAgent(myAgent,new AID(RENEWER_AGENT,AID.ISLOCALNAME));
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
        
        return b;
  }
  
  
  
}
