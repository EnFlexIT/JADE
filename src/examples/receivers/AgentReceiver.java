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

package examples.receivers;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
This example shows some of the methods an agent can use to receive messages
@author Tiziana Trucco - CSELT S.p.A.
@version  $Date$ $Revision$  
*/


public class AgentReceiver extends Agent {

  class my3StepBehaviour extends SimpleBehaviour {

  	final int FIRST = 1;
  	final int SECOND = 2;
  	final int THIRD = 3;

  	private int state = FIRST;
  	private boolean finished = false;
    
    public my3StepBehaviour(Agent a) {
      super(a);
    }

    public void action() {
      switch (state){
      	case FIRST: {if (op1()) state = SECOND; else  state= FIRST; break;}
      	case SECOND:{op2(); state = THIRD; break;}
      	case THIRD:{op3(); state = FIRST; finished = true; break;}
      	
      }
    }

    public boolean done() {
      return finished;
    }

    
    private boolean op1(){
    		  

    	MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    	MessageTemplate m2 = MessageTemplate.MatchLanguage("PlainText");
    	MessageTemplate m3 = MessageTemplate.MatchOntology("ReceiveTest");
    	MessageTemplate m1andm2 = MessageTemplate.and(m1,m2);
    	MessageTemplate notm3 = MessageTemplate.not(m3);
    	MessageTemplate m1andm2_and_notm3 = MessageTemplate.and(m1andm2, notm3);

    	System.out.println( "\nAgent "+getLocalName()+" in state FIRST.op1 is waiting for a message matching the template ");    	
    	//The agent waits for a specific message. If it doesn't arrive the behaviour is suspended until a new message arrives.
    	ACLMessage msg = receive(m1andm2_and_notm3);
    	
    	if (msg!= null){
    		System.out.println("\nAgent "+ getLocalName() + " received the following message in state FIRST.op1: ");
		System.out.println(msg.toString());
    		return true;
    	}
    	else 
    		{
    			System.out.println("\nNo message matching the template received in state FIRST.op1. Blocking the behaviour");
    			block();
    			return false;
    		}
	
    }
    
    private void op2(){
  
    	System.out.println("\nAgent "+ getLocalName() + " in state SECOND.op2 is waiting for any message in next 5000 msec");
    	
    	//Using a blocking receive causes the block of all the behaviours
    	ACLMessage msg = blockingReceive(5000);
    	if(msg != null) {
    		System.out.println("\nAgent "+ getLocalName() + " received the following message in state SECOND.op2: ");
		System.out.println(msg.toString());
    	}
	  	else{
	  		System.out.println("\nNo message received in state SECOND.op2");
	  	}

    }
    
    
    
    private void op3(){
    	
    	MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    	MessageTemplate m2 = MessageTemplate.MatchLanguage("PlainText");
    	MessageTemplate m3 = MessageTemplate.MatchOntology("ReceiveTest");
    	
    	MessageTemplate m1andm2 = MessageTemplate.and(m1,m2);
    
    	MessageTemplate m1andm2_and_m3 = MessageTemplate.and(m1andm2, m3);

    	System.out.println("\nAgent: "+getLocalName()+" in state THIRD.op3 is blocked waiting for a message matching the template"); 
    	//blockingReceive and template
    	ACLMessage msg = blockingReceive(m1andm2_and_m3);
    	
    	if (msg!= null)
    		System.out.println("\nAgent "+ getLocalName() + " received the following message in state THIRD.op3: "+msg.toString());
    	else 
    	  System.out.println("\nNo message received in state THIRD.op3");

    	
    }     
  } // End of my3StepBehaviour class

  
  protected void setup() {

    my3StepBehaviour mybehaviour = new my3StepBehaviour(this);
    addBehaviour(mybehaviour);
		
  }


}
