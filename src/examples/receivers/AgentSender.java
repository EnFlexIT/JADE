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

import java.io.StringReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.IOException;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
A simple agent that can send  custom messages to another agent.
@author Tiziana Trucco - CSELT S.p.A.
@version  $Date$ $Revision$  
*/
 
public class AgentSender extends Agent {

  protected void setup() {

    addBehaviour(new SimpleBehaviour(this) {

      private boolean finished = false;
      
    	public void action() {
         	
        	try{
        		System.out.println("\nEnter responder agent name (e.g. da0@myhost:1099/JADE): ");
        		BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
        		String responder = buff.readLine();
			AID r = new AID();
			r.setName(responder);
        		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        		msg.setSender(getAID());
        		msg.addReceiver(r);
        		msg.setContent("FirstInform");
        		send(msg);
        		System.out.println("\nFirst INFORM sent");
        		doWait(5000);
        		msg.setLanguage("PlainText");
        		msg.setContent("SecondInform");
        		send(msg);
        		System.out.println("\nSecond INFORM sent");
        		doWait(5000);
		        
		        // same that second
		        msg.setContent("\nThirdInform");
		        
		        send(msg);
		        System.out.println("\nThird INFORM sent");
		         
		        doWait(1000);
		        msg.setOntology("ReceiveTest");
		        msg.setContent("FourthInform");
		        send(msg);
		        System.out.println("\nFourth INFORM sent");
		        finished = true;
		        myAgent.doDelete();
		        
        	}catch (IOException ioe){
        	ioe.printStackTrace();
        		}
        		
        	}
        	public boolean done(){
        		return finished;
        	}
    }); 
    }
}
