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
import jade.lang.acl.ACLMessage;
import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.proto.AchieveREInitiator;

import test.common.*;
import test.domain.df.*;

/**
   Test that registering an already registered agent results in 
   receiving a FAILURE message.
   @author Giovanni Caire - TILAB
 */
public class TestAlreadyRegistered extends Test {
	private Codec codec = new SLCodec();
	private Ontology ontology = FIPAManagementOntology.getInstance();
	private DFAgentDescription dfd; 
	
  public Behaviour load(Agent a) throws TestException {

  	// Register a DFD with the DF
  	dfd = TestDFHelper.getSampleDFD(a.getAID());
		try {
			DFService.register(a, dfd);
		}
		catch (FIPAException fe) {
			throw new TestException("Error registering with the DF", fe);
		}	
		log("DF registration done");

		// Register language and ontology
		a.getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);
		a.getContentManager().registerOntology(ontology);
		
		// Prepare the REQUEST message to register again
  	ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  	request.addReceiver(a.getDefaultDF());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		request.setOntology(ontology.getName());
		try {
			Register r = new Register();
			r.setDescription(dfd);
			Action act = new Action(a.getDefaultDF(), r);
			a.getContentManager().fillContent(request, act);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		
  	Behaviour b = new AchieveREInitiator(a, request) {
  		private boolean failureReceived = false;
  		
  		protected void handleFailure(ACLMessage failure) {
  			failureReceived = true;
  			
  			// Check that this is the already-registered notification from the DF
  			if (failure.getSender().equals(myAgent.getDefaultDF())) {
	  			try {
	  				ContentElementList cel = (ContentElementList) myAgent.getContentManager().extractContent(failure);
	  				if (cel.get(1) instanceof AlreadyRegistered) {
	  					passed("Already-registered FAILURE notification received from the DF as expected");
	  				}
	  				else {
	  					failed("Wrong FAILURE notification received from the DF");
	  				}
	  			}
	  			catch (Exception e) {
	  				failed("Unknown FAILURE notification received from the DF");
	  				e.printStackTrace();
	  			}
  			}
  			else {
  				failed("Unexpected FAILURE message received from "+failure.getSender().getName());
  			}
  		}
  		
  		public int onEnd() {
  			if (!failureReceived) {
  				failed("No failure notification received");
  			}
  			return 0;
  		}
  	};
  	
  	return b;
  }
  
  public void clean(Agent a) {
		try {
			DFService.deregister(a, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}	
  }
  	
}
