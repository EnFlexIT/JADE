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
import java.util.*;

/**
* @author Giovanni Caire - CSELT S.p.A
* @version $Date$ $Revision$
*/
public class EngagerAgent extends Agent {
	
	// AGENT BEHAVIOURS
	/**
	* This behaviour handles queries about people working for a company
	* following the FIPA-Query protocol
	*/
	class HandleEnganementQueriesBehaviour extends FipaQueryResponderBehaviour{
		// Constructor
		public HandleEnganementQueriesBehaviour(Agent myAgent){
			super(myAgent, MessageTemplate.MatchOntology(EmploymentOntology.NAME));
		}
		
		// This method handles the received QUERY_IF messages
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
			
			// Get the predicate for which the truth is queried	
			try{
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
					//try {
    			// Write the NOT object in the :content slot of the reply message
		    	l = new ArrayList(1);
		    	l.add(not);
		    	myAgent.fillContent(reply, l);
					//}
					//catch (FIPAException fe) {
						//System.err.println(myAgent.getLocalName()+" Fill content unsucceeded. Reason:" + fe.getMessage());
					//}
				}
			}
			catch (FIPAException fe) {
				System.err.println(myAgent.getLocalName()+" Fill content unsucceeded. Reason:" + fe.getMessage());
			}
			catch (OntologyException oe){
				System.err.println(myAgent.getLocalName()+" getRoleName() unsucceeded. Reason:" + oe.getMessage());
			}
				
	
			return (reply);
			
		} // END of handleQueryMessage() method
		
	} // END of HandleEnganementQueriesBehaviour
				
			
	class HandleEnganementRequestsBehaviour extends CyclicBehaviour{
		public void action(){
		}
	}
	
	// AGENT SETUP
	protected void setup() {
		
		// Register the codec for the SL0 language
		registerLanguage(SL0Codec.NAME, new SL0Codec());	
		
		// Register the ontology used by this application
		registerOntology(EmploymentOntology.NAME, EmploymentOntology.instance());
			
		// Create and add the behaviours of this agent:
  	addBehaviour(new HandleEnganementQueriesBehaviour(this));
  	addBehaviour(new HandleEnganementRequestsBehaviour());
	}
	
	// AGENT METHODS
	boolean isWorking(Person p, Company c){
		return false;
	}
}