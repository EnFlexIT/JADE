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
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPAException;
import jade.proto.FipaRequestInitiatorBehaviour;
import jade.proto.FipaQueryInitiatorBehaviour;
import jade.lang.sl.*;
import jade.onto.*;
import jade.onto.basic.*;
import examples.ontology.employment.*;

import java.util.*;
import java.io.*;


/**
* @author Giovanni Caire - CSELT S.p.A
* @version $Date$ $Revision$
*/
public class RequesterAgent extends Agent {
	
	// AGENT BEHAVIOURS
	/**
	* Main behaviour for the Requester Agent.
	* First the personal particulars of the person to engage are requested 
	* to the user.
	* Then a check is performed to verify that the indicated person is not
	* already working for the indicated company
	* Finally, according to the above check, the engagement is requested.
	*/
	class HandleEngagementBehaviour extends SequentialBehaviour {
		// Local variables
		Behaviour queryBehaviour = null;
		Behaviour requestBehaviour = null;
		
		// Constructor
		public HandleEngagementBehaviour(Agent myAgent){
			super(myAgent);
		}
		
		// This is executed at the beginning of the behaviour
		protected void preAction(){
			// Get detail of person to be engaged
			try{
				BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
				Person p = new Person();
				Address a = new Address();
				System.out.println("ENTER details of person to engage");
				System.out.print("  Person name --> ");			
				p.setName(buff.readLine());
				System.out.print("  Person age --> ");			
				Integer i = new Integer(buff.readLine());
				p.setAge(i.intValue());
				System.out.println("  Person address");
				System.out.print("    Street --> ");
				a.setStreet(buff.readLine());
				System.out.print("    Number --> ");
				i = new Integer(buff.readLine());
				a.setNumber(i.intValue());
				System.out.print("    City   --> ");
				a.setCity(buff.readLine());
				p.setAddress(a);
			
				// Create an object representing the fact that person p works for company c
				WorksFor wf = new WorksFor();
				wf.set_0(p);
				wf.set_1(((RequesterAgent) myAgent).c);
			
				// Create an ACL message to query the engager agent if the above fact is true or false
				ACLMessage queryMsg = new ACLMessage(ACLMessage.QUERY_IF);
				queryMsg.addReceiver(((RequesterAgent) myAgent).engager);
				queryMsg.setLanguage(SL0Codec.NAME);
				queryMsg.setOntology(EmploymentOntology.NAME);
    		// Write the works for predicate in the :content slot of the message
		    List l = new ArrayList(1);
		    l.add(wf);
		    myAgent.fillContent(queryMsg, l);
				
		    // Create and add a behaviour to query the engager agent whether
				// person p already works for company c following a FIPAQeury protocol
				queryBehaviour = new CheckAlreadyWorkingBehaviour(myAgent, queryMsg);
				addSubBehaviour(queryBehaviour);
			}
			catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()+" Fill content unsucceeded. Reason:" + fe.getMessage());
			}
			catch (IOException ioe) { 
				System.out.println("I/O error"); 
			}
			
		}
		
		// This is executed at the end of the behaviour
		protected void postAction(){
			reset(); // This makes this behaviour be cyclically executed
		}
		
		// Extends the reset method in order to remove the sub-behaviours that
		// are dynamically added 
		public void reset(){
			if (queryBehaviour != null){
				removeSubBehaviour(queryBehaviour);
				queryBehaviour = null;
			}
			if (requestBehaviour != null){
				removeSubBehaviour(requestBehaviour);
				requestBehaviour = null;
			}
			super.reset();
		}
	}
	
	
	/**
	* This behaviour embeds the check that the indicated person is not
	* already working for the indicated company.
	* This is done following a FIPA-Query interaction protocol
	*/
	class CheckAlreadyWorkingBehaviour extends FipaQueryInitiatorBehaviour {
		// Constructor
		public CheckAlreadyWorkingBehaviour(Agent myAgent, ACLMessage queryMsg){
			super(myAgent, queryMsg);
		}
		
		public void handleInformMessages(Vector messages) {
			ACLMessage msg = (ACLMessage) messages.get(0);
			try{
				List l = myAgent.extractContent(msg);
				Object resp = l.get(0);
				Ontology o = myAgent.lookupOntology(msg.getOntology());
				String respName = o.getRoleName(resp.getClass());
				if (respName == EmploymentOntology.WORKS_FOR){
					// The indicated person is already working for company c. 
					// Inform the user
					WorksFor wf = (WorksFor) resp;
					Person p = (Person) wf.get_0();
					Company c = (Company) wf.get_1();
					System.out.println("Person " + p.getName() + " is already working for " + c.getName());
				}
				else if (respName == BasicOntologyVocabulary.NOT){
					// The indicated person is NOT already working for company c.
					// Get person and company details and create an object representing the engagement action
					WorksFor wf = (WorksFor) ((Not) resp).get_0();
					Person p = (Person) wf.get_0();
					Company c = (Company) wf.get_1();
					Engage e = new Engage();
					e.set_0(p);
					e.set_1(c);
					Action a = new Action();
					a.set_0(((RequesterAgent) myAgent).engager);
					a.set_1(e);
			
					// Create an ACL message to request the above action
					ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
					requestMsg.addReceiver(((RequesterAgent) myAgent).engager);
					requestMsg.setLanguage(SL0Codec.NAME);
					requestMsg.setOntology(EmploymentOntology.NAME);
    			// Write the action in the :content slot of the message
		    	l = new ArrayList(1);
		    	l.add(a);
		    	myAgent.fillContent(requestMsg, l);
				
					// Create and add a behaviour to request the engager agent to engage
					// person p in company c following a FIPARequest protocol
					((HandleEngagementBehaviour) parent).requestBehaviour = new RequestEngagementBehaviour(myAgent, requestMsg);
					parent.addSubBehaviour(((HandleEngagementBehaviour) parent).requestBehaviour);
				}
				else{
					// Unexpected response received from the engager agent.
					// Inform the user
					System.out.println("Unexpected response from engager agent");
				}
				
			} // End of try
			catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()+" Fill/extract content unsucceeded. Reason:" + fe.getMessage());
			}
			catch (OntologyException fe) {
				System.err.println(myAgent.getLocalName()+" getRoleName() unsucceeded. Reason:" + fe.getMessage());
			}
		}
		
		public void handleOtherMessages(ACLMessage msg) {
			System.out.println("Unexpected message in FIPAQuery interaction protocol");
		}
	}
			
			
	/**
	* This behaviour embeds the request to engage the indicated person 
	* in the indicated company.
	* This is done following a FIPA-Request interaction protocol
	*/
	class RequestEngagementBehaviour extends FipaRequestInitiatorBehaviour {
		// Constructor
		public RequestEngagementBehaviour(Agent myAgent, ACLMessage requestMsg){
			super(myAgent, requestMsg);
		}

		protected void handleAgree(ACLMessage msg) {
			System.out.println("Engagement agreed. Waiting for completion notification");
		}
		protected void handleInform(ACLMessage msg) {
			System.out.println("Engagement successfully completed");	
			myAgent.doDelete();
		}
		protected void handleNotUnderstood(ACLMessage msg) {
			System.out.println("Engagement request not understood by engager agent");			
		}
		protected void handleFailure(ACLMessage msg) {
			System.out.println("Engagement failed");
		}
		protected void handleRefuse(ACLMessage msg) {
			System.out.println("Engagement refused by engager agent");
		}
	}		
		

	// AGENT LOCAL VARIABLES
	AID engager; // AID of the agent the engagement requests will have to be sent to
	Company c;   // The  company where people will be engaged
	
	
	// AGENT SETUP
	protected void setup() {
		
		// Register the codec for the SL0 language
		registerLanguage(SL0Codec.NAME, new SL0Codec());	
		
		// Register the ontology used by this application
		registerOntology(EmploymentOntology.NAME, EmploymentOntology.instance());
	
		// Get from the user the name of the agent the engagement requests
		// will have to be sent to
		try {
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("ENTER the local name of the Engager agent --> ");
			String name = buff.readLine() + '@' + getHap();
			engager = (AID) getAID().clone();
			engager.setName(name);
		
			// Get from the user the details of the company where people will 
			// be engaged
			c  = new Company();
			Address a = new Address();
			System.out.println("ENTER details of the company where people will be engaged");
			System.out.print("  Company name --> ");			
			c.setName(buff.readLine());
			System.out.println("  Company address");
			System.out.print("    Street --> ");
			a.setStreet(buff.readLine());
			System.out.print("    Number --> ");
			Integer i = new Integer(buff.readLine());
			a.setNumber(i.intValue());
			System.out.print("    City   --> ");
			a.setCity(buff.readLine());
			c.setAddress(a);
		}
		catch (IOException ioe) { System.out.println("I/O error"); }
		
		// Create and add the main behaviour of this agent
  	addBehaviour(new HandleEngagementBehaviour(this));
	}
}