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

import java.util.Date;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import jade.content.*;
import jade.content.abs.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;

import jade.proto.SubscriptionInitiator;

import test.common.*;
import test.domain.df.*;

/**
   @author Elisabetta Cortese - TILAB
 */
public class TestDFLeaseTime extends Test {
  private int informCnt = 0;
	
  public String getName() {
  	return "Test DF Lease Time";
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	Behaviour b = new OneShotBehaviour(a) {
  		int ret;
  		
  		public void action() {
  			Logger l = Logger.getLogger();
  			ret = Test.TEST_PASSED;
  			
  			// Register with the DF
  			DFAgentDescription dfd = TestDFHelper.getSampleDFD(myAgent.getAID());
  			dfd.setLeaseTime(new Date(System.currentTimeMillis()+6000));
  			try {
	  			DFService.register(myAgent, myAgent.getDefaultDF(), dfd);
  			}
  			catch (FIPAException fe) {
  				l.log("DF registration failed");
  				fe.printStackTrace();
  				ret = Test.TEST_FAILED;
  				return;
  			}	
  			l.log("DF registration done");
  			
  			// Search with the DF
  			DFAgentDescription template = TestDFHelper.getSampleTemplate1();
  			DFAgentDescription[] result = null;
  			try {
	  			result = DFService.search(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints());
  			}
  			catch (FIPAException fe) {
  				l.log("DF search-1 failed");
  				fe.printStackTrace();
  				ret = Test.TEST_FAILED;
  				return;
  			}	
  			l.log("DF search-1 done");
  			if (result.length != 1 || (!TestDFHelper.compare(result[0], dfd))) {
  				l.log("DF search-1 result different from what was expected");
  				ret = Test.TEST_FAILED;
  				return;
  			}
  			l.log("DF search-1 result OK");
  			
  			// Wait
  			try{Thread.sleep(6000);}catch(Exception e){}
  			l.log("DF waited done");
  			
  	  	// Search again with the DF
  			try {
  				result = DFService.search(myAgent, myAgent.getDefaultDF(), template, new SearchConstraints());
  			}
  			catch (FIPAException fe) {
  				l.log("DF search-2 failed");
  				fe.printStackTrace();
  				ret = Test.TEST_FAILED;
  				return;
  			}	
  			l.log("DF search-2 done");
  			if (result.length != 0) {
   				l.log("DF search-2 result different from what was expected");
 					ret = Test.TEST_FAILED;
  				return;
  			}
  			l.log("DF search-2 result OK");
  			l.log("No results returned.");
  		}
  		
  		public int onEnd() {
  			store.put(key, new Integer(ret));
  			try {
	  			DFService.deregister(myAgent, myAgent.getDefaultDF(), new DFAgentDescription());
  			}
  			catch (FIPAException fe) {
  				fe.printStackTrace();
  				ret = Test.TEST_FAILED;
  			}	

  			return 0;
  		}	
  	};
  	
  	return b;
  }
  	
}
