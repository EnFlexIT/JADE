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

import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.domain.*;
import jade.proto.FipaRequestInitiatorBehaviour;
import jade.lang.sl.*;
import jade.onto.*;

import java.util.*;

/**
* @author Angelo Difino - CSELT S.p.A
* @version $Date$ $Revision$
*/
public class RequesterAgent extends Agent {
	
	
	private final static String ontology = "Engagement";

	/**
	* Behaviour used by the Requester Agent that perform an request
	* specified by parameter to the Executor Agent
	*/
	class EngagePersonBehaviour extends FipaRequestInitiatorBehaviour {
		
		/**
		* Constructor for the Engage Person Behaviour
		*
		* @parameter a Agent  that are requesting and action
		* @parameter p Person that has to be engaged
		* @parameter c Company that are engaging the person p
		* @parameter executor Agent that can perform the request
		*/
		public EngagePersonBehaviour(Agent a, Person p, Company c,String executor){
			super(a,new ACLMessage(ACLMessage.REQUEST));
			//creating the action to  perform
			EngageAction action = new EngageAction();
			action.setPerson(p);	
			action.setCompany(c);
			action.setActor(executor);
			
			// creates the message to Request the engagement of a Person in a Company
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addDest(executor);
			msg.setLanguage(SL0Codec.NAME);
			msg.setOntology(((RequesterAgent)myAgent).ontology);
			
			try {
				//fill the Action into a msg to send to the executor using the language and
				//the ontology specified
				myAgent.fillContent(msg,action,"EngageAction");
			}
			catch (FIPAException fe) {
				System.err.println(getLocalName()+" Fill convent unsucceeded. Reason:" + fe.getMessage());
			}	

			// reset the behaviour in order to change the message to be sent
			reset(msg);
		}

		protected void handleAgree(ACLMessage msg) {
			System.out.println(getLocalName()+ " Handle Agree msg" + msg.toString());
		}
		protected void handleInform(ACLMessage msg) {
			System.out.println(getLocalName()+ " Handle Inform msg" + msg.toString());	
			myAgent.doDelete();
		}
		protected void handleNotUnderstood(ACLMessage msg) {
			System.out.println(getLocalName()+ " Handle Not Understood msg" + msg.toString());			
		}
		protected void handleFailure(ACLMessage msg) {
			System.out.println(getLocalName()+ " Handle Failure msg" + msg.toString());
		}
		protected void handleRefuse(ACLMessage msg) {
			System.out.println(getLocalName()+ " Handle Refuse msg" + msg.toString());
		}
	}		
		
	/**
	* Method that search into the DF an agent of agentType
	*
	* parameter searchAgent Type of the agent to search into the DF
	*/
	private String searchAgent(String agentType) {
		System.out.println(getLocalName()+ " searching into DF");
		String reader = new String("");
		try {
    	while (true) {
  		AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();    
  		dfd.setType(agentType); 
      AgentManagementOntology.DFSearchResult result;
      Vector vc = new Vector(1);
      AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
      c.setName(AgentManagementOntology.Constraint.DFDEPTH);
      c.setFn(AgentManagementOntology.Constraint.MAX);
      c.setArg(3);
      vc.addElement(c);
      result = searchDF("DF",dfd,vc);
      Enumeration e = result.elements();
      if (e.hasMoreElements()) {
				dfd = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
				reader = dfd.getName();
				break;
      } 
      Thread.sleep(1000);
    	}
  	} 
  	catch (Exception fe) {
    	System.err.println(getLocalName()+" search with DF is not succeeded. Reason:" + fe.getMessage());
    	doDelete();
  	}
		System.out.println(getLocalName()+ " founded into DF");
		return reader;
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
	
	// create some dummy data to represent a person and a company 
	Address a1 = new Address();
	a1.setStreet("\"Via Roma 2\"");
	a1.setCity("Torino");
	
	Person p = new Person();
	p.setName("\"Roberto Lomele\"");
	p.setAge(25);
	p.setAddress(a1);
	
	Address a2 = new Address();
	a2.setStreet("\"Via Avigliana 7\"");
	a2.setCity("Rivoli");
	
	Company c = new Company();
	c.setName("\"Firp SPA\"");
	c.setAddress(a2);
		
	// search with the DF for the ResponderAgent
	String executor = searchAgent("ResponderAgent");
	
	// finally, adds the Task to the Agent scheduler
  addBehaviour(new EngagePersonBehaviour(this,p,c,executor));
	}
}