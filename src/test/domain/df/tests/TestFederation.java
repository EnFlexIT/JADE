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
import jade.lang.acl.ACLMessage;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.proto.AchieveREInitiator;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFGUIManagement.*;
import test.common.*;
import test.domain.df.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestFederation extends Test {
	private AID df1;
	
  public String getName() {
  	return "Test Federation";
  }
  
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests DF federation and recursive search on federated DFs\n");
  	sb.append("More in details this test behaves as follows:\n");
  	sb.append("Load phase\n");
  	sb.append("- Create another DF agent called DF1\n");
  	sb.append("- Register a DFD on DF1\n");
  	sb.append("Actual Test\n");
  	sb.append("- Request DF1 to federate with the default DF\n");
  	sb.append("- Searches the default DF specifying maxDepth ==1 (he should find 1 agent)");
  	return sb.toString();
  }
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
 		final Codec codec = new SLCodec();
    final Logger l = Logger.getLogger();
  	
  	// Create another DF called DF1
  	df1 = TestUtility.createAgent(a, "DF1", "jade.domain.df", null, a.getAMS(), null);
  	// Register a DFD with DF1
  	final DFAgentDescription dfd = TestDFHelper.getSampleDFD(a.getAID());
  	try {
	  	DFService.register(a, a.getDefaultDF(), dfd);
  	}
  	catch (FIPAException fe) {
  		throw new TestException("Error registering a dfd", fe);
  	}	
  	
    // Register the ontologies and codec required to request the federation
    a.getContentManager().registerLanguage(codec, "FIPA-SL0");
    //a.getContentManager().registerOntology(FIPAManagementOntology.getInstance());
    a.getContentManager().registerOntology(DFAppletOntology.getInstance());
    
  	// Create the REQUEST message to be sent to df1 to make it federate with the default DF
  	ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  	request.addReceiver(df1);
		request.setLanguage("FIPA-SL0");
		request.setOntology(DFAppletOntology.getInstance().getName());
		try {
			Federate f = new Federate();
			f.setParentDF(a.getDefaultDF());
			DFAgentDescription dfd1 = new DFAgentDescription();
			dfd1.setName(df1);
			f.setChildrenDF(dfd1);
			Action act = new Action(df1, f);
			a.getContentManager().fillContent(request, act);
		}
		catch (Exception e) {
  		throw new TestException("Error creating the message to request the federation", e);
		}
		
		// Create and return the behaviour that will actually perform the test
  	Behaviour b = new AchieveREInitiator(a, request, store) {
  		int ret = Test.TEST_FAILED;
  		
    	protected void handleInform(ACLMessage inform) {
    		// If the federation succeeded, search the default DF
 				try {
 					Done d = (Done) myAgent.getContentManager().extractContent(inform);
 					l.log("Federation done");
  				// Search with the DF
  				try {
  					DFAgentDescription template = TestDFHelper.getSampleTemplate1();
  					SearchConstraints constraints = new SearchConstraints();
  					constraints.setMaxDepth(new Long(1));
	  				DFAgentDescription[] result = DFService.search(myAgent, myAgent.getDefaultDF(), template, constraints);
	  				l.log("Recursive search done");
  					if (result.length != 1 || (!TestDFHelper.compare(result[0], dfd))) {
  						l.log("Recursive search result NOT OK: "+result.length+" items found, while 1 was expected");
  					}
  					else {
  						l.log("Recursive search result OK");
  						ret = Test.TEST_PASSED;
  					}
  				}
  				catch (FIPAException fe) {
  					l.log("Recursive search failed");
  					fe.printStackTrace();
  				}	
 				}
 				catch (Exception e) {
  				l.log("Error decoding federation notification");
  				e.printStackTrace();
 					e.printStackTrace();
 				}
    	}
    	
    	protected void handleRefuse(ACLMessage refuse) {
    		l.log("Federation request refused: message is "+refuse);
    	}
    	
    	protected void handleNotUnderstood(ACLMessage notUnderstood) {
    		l.log("Federation request not understood: message is "+notUnderstood);
    	}
    	
    	protected void handleFailure(ACLMessage failure) {
    		l.log("Federation failed: message is "+failure);
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
	  	TestUtility.killAgent(a, df1);
	  	DFAgentDescription  dfd = new DFAgentDescription();
	  	dfd.setName(a.getAID());
	  	DFService.deregister(a, dfd);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  
}
