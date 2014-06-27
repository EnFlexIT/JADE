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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import test.common.Test;
import test.common.TestException;

/**
 * Search with the AMS with max-results=-1 and checks that more than one agent is returned  
 * 
 * @author Fabio Bellifemine
 * @version $Date:  $ $Revision: $
 *
 */
public class TestSearchConstraints extends Test {
	
	public Behaviour load(Agent a) throws TestException {  	
		
		return new OneShotBehaviour(a) {
			public void action() {
				try {
					log("--- Searching the AMS with searchConstraints=null ...");
					AMSAgentDescription[] results;
					results = AMSService.search(myAgent, new AMSAgentDescription());
					if (results.length != 1) {
						failed("--- Search with searchConstraints=null returned "+results.length+" results while 1 was expected");
						return;
					}
					log("--- ...passed. Searching the AMS with searchConstraints.maxResults=null ...");
					SearchConstraints s = new SearchConstraints();
					results = AMSService.search(myAgent, new AMSAgentDescription(), s);
					if (results.length != 1) {
						failed("--- Search with searchConstraints.maxResults=null returned "+results.length+" results while 1 was expected.");
						return;
					}
					log("--- ...passed. Searching the AMS with searchConstraints.maxResults=-1 ...");
					s.setMaxResults(new Long(-1));
					results = AMSService.search(myAgent, new AMSAgentDescription(), s);
					// Normally we expect 5 agents: ams, df, rma, test-suite and tester.
					// However if we are running with the Misc add-on in the classpath we also have monitor agents -->
					// Do not count them 
					int count = 0;
					for (int i = 0; i < results.length; ++i) {
						if (!results[i].getName().getLocalName().startsWith("monitor-")) {
							count++;
						}
					}
					if (count != 5) {
						failed("---Search with searchConstraints.maxResults=-1 returned "+count+" results while 5 were expected.");
						return;
					}
					log("--- ...passed. Searching the AMS with searchConstraints.maxResults=3 ...");
					s.setMaxResults(new Long(3));
					results = AMSService.search(myAgent, new AMSAgentDescription(), s);
					if (results.length != 3) {
						failed("---Search with searchConstraints.maxResults=3 returned "+results.length+" results while 3 were expected.");
						return;
					}	
					log("--- ...passed.");
					passed("--- AMSService.search returned proper number of results for different values of SearchConstraints and maxResults.");
				} catch (FIPAException e) {
					TestSearchConstraints.this.failed("Exception"+e.getMessage()); 
					e.printStackTrace();
				}
			}
		};
	}
	
	public void clean(Agent a) {
	}
	
}
