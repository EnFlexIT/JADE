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

package examples.MessageTemplate;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This example shows how to define an application-specific MessageTemplate.
 *
 * This agent waits for REQUEST-type ACLMessages from the set of agents 
 * specified in input by the user. 

 * Since the method provided by the MessageTemplate class does not allow 
 * to define such a pattern, an application specific Message Template has 
 * been defined.

 * @author Tiziana Trucco - CSELT S.p.A.
 * @version  $Date$ $Revision$  
*/


public class WaitAgent extends Agent {

    // This class implements the MessageTemplate.MatchExpression interface in order to
    // provide an application-specific match() method to use in the pattern matching phase.
    class myMatchExpression implements MessageTemplate.MatchExpression {
	List senders;
		
	myMatchExpression(List l) {
	    senders = l;
	}
		
	//This method verifies if the ACLMessage was sent from one of the expected senders. 
	public boolean match(ACLMessage msg){
	  		
	    AID sender = msg.getSender();
	    String name = sender.getName();
	    Iterator it_temp = senders.iterator();
	    boolean out = false;
	    
	    while(it_temp.hasNext() && !out) {
	    	String tmp = ((AID)it_temp.next()).getName();
	    	if(tmp.equalsIgnoreCase(name))
		    out = true;
	    }
	    
	    return out;
	}
    }
	
    //Simple Behaviour that waits for messages that match the given template
    // and reply with an inform message.
    class WaitBehaviour extends CyclicBehaviour {
  	
	private MessageTemplate template;
	
	public WaitBehaviour(Agent a, MessageTemplate mt) {
	    super(a);
	    template = mt;  
	}

	public void action() {
	    
	    ACLMessage  msg = blockingReceive(template);
	    System.out.println("\nReceived a REQUEST message from: " + msg.getSender().getName());

	    ACLMessage reply = msg.createReply();
	    reply.setPerformative(ACLMessage.INFORM);
	    send(reply);
	    System.out.println("\nSending an INFORM message.");
	}
    } //End class WaitBehaviour
    
  
    protected void setup() {
  	
  	try{
	    ArrayList sender = new ArrayList();
	    System.out.println("\nEnter the agent names, i.e. the globally unique identifiers (name@hap), of the expected senders separated by white spaces or tab (example da0@myhost:1099/JADE da1@myhost:1099/JADE): ");
	    BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
	    String agentNames = buff.readLine();
	    StringTokenizer st = new StringTokenizer(agentNames);
	    
	    while(st.hasMoreTokens()) {
		String name = st.nextToken();
		sender.add(new AID(name, AID.ISGUID));
	    }
    
	    if(sender.isEmpty())
    		System.out.println("WARNING: Set of senders empty so no message will match the pattern !");
    		
	    myMatchExpression me = new myMatchExpression(sender);
	    MessageTemplate myTemplate = new MessageTemplate(me);
      
	    //Examples of using the logic operator AND and the matchPerforamative() method 
	    //provided by the MessageTemplate class and a user defined MessageTemplate.
	    MessageTemplate mt = MessageTemplate.and(myTemplate,MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

	    WaitBehaviour Behaviour = new  WaitBehaviour(this,mt);
	    addBehaviour(Behaviour);
	}catch(java.io.IOException e){
	    e.printStackTrace();  	
  	}
    }

}//end class WaitAgent
