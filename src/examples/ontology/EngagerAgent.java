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
import jade.proto.FipaRequestResponderBehaviour;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPAException;
import jade.onto.*;
import jade.lang.sl.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import examples.ontology.employment.*;
import jade.onto.basic.*;
import jade.util.leap.*;

/**
	This agent is able to engage people on behalf of company 
	CSELT
	Via Reiss Romoli 274 - Turin
	
	@author Giovanni Caire - CSELT S.p.A
	@version $Date$ $Revision$
	@see examples.ontology.RequesterAgent
*/
public class EngagerAgent extends Agent {
	
	// AGENT BEHAVIOURS
	/**
		This behaviour handles all queries about people working for a company
		following the FIPA-Query protocol
	*/
	class HandleEnganementQueriesBehaviour extends FipaQueryResponderBehaviour{
		/**
			Constructor for the <code>HandleEnganementQueriesBehaviour</code>
			class.
			
			@param myAgent The agent owning this behaviour
		*/
		public HandleEnganementQueriesBehaviour(Agent myAgent){
			super(myAgent, MessageTemplate.and(
												MessageTemplate.MatchProtocol("FIPA-Query"),
												MessageTemplate.MatchOntology(EmploymentOntology.NAME)));
		}
		
		/**
			This method is called when a QUERY-IF or QUERY-REF message is received.
			
			@param msg The received query message
			@return The ACL message to be sent back as reply
			@see jade.proto.FipaQueryResponderBehaviour
		*/
		public ACLMessage handleQueryMessage(ACLMessage msg){
			ACLMessage reply = msg.createReply();
			
			// The QUERY message could be a QUERY-REF. In this case reply 
			// with NOT_UNDERSTOOD
			if (msg.getPerformative() != ACLMessage.QUERY_IF){
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  			String content = "(" + msg.toString() + ")"; 
				reply.setContent(content);
				return(reply);
			}
			
			try{
				// Get the predicate for which the truth is queried	
				List l = myAgent.extractContent(msg);
				Object requestedInfo = l.get(0);
				Ontology o = myAgent.lookupOntology(msg.getOntology());
				String requestedInfoName = o.getRoleName(requestedInfo.getClass());
				// If the predicate for which the truth is queried is not WORKS_FOR
				// reply with NOT_UNDERSTOOD
				if (requestedInfoName != EmploymentOntology.WORKS_FOR){
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  				String content = "(" + msg.toString() + ")"; 
					reply.setContent(content);
					return(reply);
				}
			
				// Reply 
				reply.setPerformative(ACLMessage.INFORM);
				WorksFor wf = (WorksFor) requestedInfo;
				Person p = wf.get_0();
				Company c = wf.get_1();
				if (((EngagerAgent) myAgent).isWorking(p, c))
					reply.setContent(msg.getContent());
				else{
					// Create an object representing the fact that the WORKS_FOR 
					// predicate is NOT true.
					Not not = new Not();
					not.set_0(wf);
    			// Write the NOT object in the :content slot of the reply message
		    	l = new ArrayList(1);
		    	l.add(not);
		    	myAgent.fillContent(reply, l);
				}
			}
			catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()+" Fill/extract content unsucceeded. Reason:" + fe.getMessage());
			}
			catch (OntologyException oe){
				System.err.println(myAgent.getLocalName()+" getRoleName() unsucceeded. Reason:" + oe.getMessage());
			}
				
			return (reply);
			
		} // END of handleQueryMessage() method
		
	} // END of HandleEnganementQueriesBehaviour
				
			
	/**
		This behaviour handles a single engagement action that has been  
		requested following the FIPA-Request protocol
		@see jade.proto.FipaRequestResponderBehaviour
	*/
	class HandleEngageBehaviour 
		extends FipaRequestResponderBehaviour.ActionHandler 
  	implements FipaRequestResponderBehaviour.Factory {
  		
  	/**
  		Constructor for the <code>HandleEngageBehaviour</code> class.
  		
  		@param myAgent The agent owning this behaviour
  		@param requestMsg The ACL message by means of which the engagement
  		action has been requested
  	*/
  	public HandleEngageBehaviour(Agent myAgent, ACLMessage requestMsg){
  		super(myAgent, requestMsg);
  	}
  	
  	/**
	  	This method implements the <code>FipaRequestResponderBehaviour.Factory</code>
  		interface.
  		It will be called within a <code>FipaRequestResponderBehaviour</code> 
  		when an engagement action is requested to instantiate a new 
  		<code>HandleEngageBehaviour</code> handling the requested action
   
  		@param msg The ACL message by means of which the engagement
  		action has been requested
  	*/
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new HandleEngageBehaviour(EngagerAgent.this, msg);
    }
    
    /**
    	This method actually handles the engagement action
     */
		public void action(){
			// Prepare a dummy ACLMessage used to create the content of all reply messages
			ACLMessage tmp = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
			tmp.setLanguage(SL0Codec.NAME);
			tmp.setOntology(EmploymentOntology.NAME);
			
			// Get the request message
			ACLMessage msg = getRequest();
			
			try{
				// Get the requested action. 
				List l = myAgent.extractContent(msg);
				Action a = (Action) l.get(0);
				Engage e = (Engage) a.get_1();
				Person p = e.get_0();
				Company c = e.get_1();
				
				// Check person's age. If < 35 --> AGREE, else REFUSE and exit
				if (p.getAge().intValue() < 35){
					// AGREE to accomplish the engagement action without any 
					// special condition.
					l.clear();
					l.add(a);
					l.add(new TrueProposition());
					myAgent.fillContent(tmp, l);
					sendReply(ACLMessage.AGREE,tmp.getContent());
				}
				else {
					l.clear();
					l.add(a);
					l.add(new PersonTooOld());
					myAgent.fillContent(tmp, l);
					sendReply(ACLMessage.REFUSE,tmp.getContent());
					return;
				}
				
				// Perform the engagement action
				int result = ((EngagerAgent) myAgent).doEngage(p, c);
				
				// Reply according to the result
				if (result > 0){
					// OK --> INFORM action done
					DonePredicate d = new DonePredicate();
					d.set_0(a);
					l.clear();
					l.add(d);
					myAgent.fillContent(tmp, l);
      		sendReply(ACLMessage.INFORM, tmp.getContent());
				}
				else{
					// NOT OK --> FAILURE
					l.clear();
					l.add(a);
					l.add(new EngagementError());
					myAgent.fillContent(tmp, l);
      		sendReply(ACLMessage.FAILURE, tmp.getContent());
				}
			}
			catch (FIPAException fe){
				System.out.println(myAgent.getName() + ": Error handling the engagement action.");
				System.out.println(fe.getMessage().toString());
			}
		}
		
		/**
			This is a "one shot behaviour"
		 */
		public boolean done(){
			return true;
		}
		
		/**
			No need for any specific action to reset this behaviour 
		 */
		public void reset(){
		}
	}
	
	
	// AGENT LOCAL VARIABLES
	private Company representedCompany; // The company on behalf of which this agent is able to engage people 
	private List employees;	// The people currently working for the company
	
	// AGENT CONSTRUCTOR
	public EngagerAgent(){
		super();
		
		representedCompany = new Company();
		representedCompany.setName("CSELT");
		Address a = new Address();
		a.setStreet("\"Via Reiss Romoli\"");
		a.setNumber(new Long(274));
		a.setCity("Turin");
		representedCompany.setAddress(a);
		
		employees = new ArrayList();
	}
	
	// AGENT SETUP
	protected void setup() {
		
		// Register the codec for the SL0 language
		registerLanguage(SL0Codec.NAME, new SL0Codec());	
		
		// Register the ontology used by this application
		registerOntology(EmploymentOntology.NAME, EmploymentOntology.instance());
			
		// Create and add the behaviour for handling QUERIES using the employment-ontology
  	addBehaviour(new HandleEnganementQueriesBehaviour(this));
  	
		// Create and add the behaviour for handling REQUESTS using the employment-ontology
		MessageTemplate mt = MessageTemplate.and(
														MessageTemplate.MatchProtocol("FIPA-Request"),
														MessageTemplate.MatchOntology(EmploymentOntology.NAME));
  	FipaRequestResponderBehaviour b = new FipaRequestResponderBehaviour(this, mt);
  	b.registerFactory(EmploymentOntology.ENGAGE, new HandleEngageBehaviour(this, new ACLMessage(ACLMessage.NOT_UNDERSTOOD)));
  	addBehaviour(b);
	}
	
	// AGENT METHODS
	boolean isWorking(Person p, Company c){
		boolean isAnEmployee = false;	
		Iterator i = employees.iterator();
		while (i.hasNext()){
			Person empl = (Person) i.next();
			if (p.equals(empl))
				isAnEmployee = true;
		}
		
		if (c.equals(representedCompany))
			return isAnEmployee;
		else
			return !isAnEmployee;
	}
	
	int doEngage(Person p, Company c){
		if (!c.equals(representedCompany))
			return (-1); // Can engage people on behalf of representedCompany only
		else
			employees.add(p);
		return (1);
	}
}