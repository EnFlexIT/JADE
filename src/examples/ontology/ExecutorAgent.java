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

package examples.ontology;

import jade.proto.FipaQueryResponderBehaviour;
import jade.core.Agent;
import jade.domain.*;
import jade.onto.*;
import jade.lang.sl.*;
import jade.lang.acl.ACLMessage;

/**
* @author Angelo Difino - CSELT S.p.A
* @version $Date$ $Revision$
*/
public class ExecutorAgent extends Agent {
	
	
	private final static String ontology = "Engagement";
	
	private AgentManagementOntology.DFAgentDescriptor _dfd = new AgentManagementOntology.DFAgentDescriptor();    
	
	
	/**
	* Method that register this agent with a specified typeAgent
	*
	*	@parameter typeAgent Type of the Agent
	*/
	void registerWithDF(String typeAgent){
  	System.out.println(getLocalName()+" start registration DF");
	  _dfd.setType(typeAgent); 
  	_dfd.setName(getName());
  	_dfd.addAddress(getAddress());
  	_dfd.setOwnership("Difino_Angelo");
  	_dfd.setOntology("Ontology_Example");
  	_dfd.setDFState("active");
  	try {
    	registerWithDF("DF",_dfd);
  	} 
  	catch (FIPAException e) {
    	System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
    	doDelete();
  	}
  	System.out.println(getLocalName()+ " finish registration DF");
	}

	/**
	* Method that create an application-oriented Ontology
	*
	* @return an application-oriented ontology
	*/
	private DefaultOntology createOntology() {
		
		//create an default ontology
		DefaultOntology myOntology = new DefaultOntology();	

		try {
			
		//adding the Address ontology
		myOntology.addFrame("Address",Ontology.CONCEPT_TYPE,
			new TermDescriptor[] {
				new TermDescriptor("STREET", Ontology.STRING_TYPE,Ontology.M),
				new TermDescriptor("CITY", Ontology.STRING_TYPE,Ontology.M)
			}, new AddressFactory()
		);
	
		//adding the Person ontology
		myOntology.addFrame("Person",Ontology.CONCEPT_TYPE,
			new TermDescriptor[] {
				new TermDescriptor("NAME", Ontology.STRING_TYPE,Ontology.M),
				new TermDescriptor("AGE",Ontology.LONG_TYPE,Ontology.M),
				new TermDescriptor("ADDRESS",Ontology.CONCEPT_TYPE,"Address", Ontology.O)
			}, new  PersonFactory()
		);
	
		//adding the Company ontology
		myOntology.addFrame("Company",Ontology.CONCEPT_TYPE,
			new TermDescriptor[] {
				new TermDescriptor("NAME", Ontology.STRING_TYPE,Ontology.M),
			  new TermDescriptor("ADDRESS", Ontology.CONCEPT_TYPE,"Address", Ontology.M)
			}, new CompanyFactory()
		);
	
		//adding the Engage Action ontology
		myOntology.addFrame("EngageAction",Ontology.ACTION_TYPE,
			new TermDescriptor[] {
				new TermDescriptor(Ontology.CONCEPT_TYPE, "Company", Ontology.M),
				new TermDescriptor(Ontology.CONCEPT_TYPE, "Person", Ontology.M)
			}, new EngageActionFactory()
		);
		} catch (OntologyException oe) {
			System.err.println(getLocalName()+" Adding parameters to frame unsucceeded. Reason:" + oe.getMessage());
    	doDelete();
		}
		return myOntology;
	}

	protected void setup() {
		// register the codec of the language
		registerLanguage(SL0Codec.NAME,new SL0Codec());	
		
		// register the ontology used by application
		registerOntology(ontology,createOntology());
	
		//register the agent with the DF
		registerWithDF("ResponderAgent");
		
		//waiting for an request
		ACLMessage msg = blockingReceive();
  	
		try {
			//extract the Action from the msg using the language and
			//the ontology specified
  		EngageAction action = (EngageAction) extractContent(msg);
  		//execution of a DUMMY action
  		action.execute();
  	} 
  	catch (FIPAException e) {
			System.err.println(getLocalName()+" Extracting information from msg unsucceeded. Reason:" + e.getMessage());
 		}
 		
 		//send a replay-message to inform the requester that the action 
 		//has been done
  	msg = msg.createReply();
  	msg.setPerformative(ACLMessage.AGREE);
  	//FIXME the content must be set
  	send(msg);
		msg.setPerformative(ACLMessage.INFORM);
  	//FIXME the content must be set
  	send(msg);
	}
		
	public void takeDown() {
    try {
    	//deregister the agent from the DF
      deregisterWithDF("DF", _dfd);
    }
    catch (FIPAException e) {
      System.err.println(getLocalName()+" deregistration with DF unsucceeded. Reason: "+e.getMessage());
    }
  }
}