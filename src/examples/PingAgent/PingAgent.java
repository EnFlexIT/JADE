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

package examples.PingAgent;

import java.util.Date;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFServiceCommunicator;
import jade.domain.FIPAException;

/**
This agent implements a simple Ping Agent. 
First of all the agent registers itself with the DF of the platform and 
then waits for ACLMessages.
If arrives a QUERY_REF message with the string "ping" as content then it replies 
with an INFORM message whose content will be the string "pong". 
If it receives a NOT_UNDERSTOOD message no reply is sent. 
For any other message received it replies with a NOT_UNDERSTOOD message.
The exchanged message are written in a log file whose name is the local name of the agent.

@author Tiziana Trucco - CSELT S.p.A.
@version  $Date$ $Revision$  
*/


public class PingAgent extends Agent {

	BufferedWriter logFile;
	
  class WaitPingAndReplyBehaviour extends SimpleBehaviour {

  	private boolean finished = false;
    
    public WaitPingAndReplyBehaviour(Agent a) {
      super(a);
    }

    public void action() {
    	
    	ACLMessage  msg = blockingReceive();
    	
      if(msg != null){
      	if(msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD)
      	{
      		log("\nReceived the following message: "+ msg.toString());
      		log("\nNo reply message sent.");
      	}
      	else{
      		log("\nReceived the following message: "+ msg.toString());
      		ACLMessage reply = msg.createReply();
      	
      		if(msg.getPerformative()== ACLMessage.QUERY_REF)
      		{
      		  String content = msg.getContent();
      	    if (content != null)
      		  	if(content.equalsIgnoreCase("ping"))
      					{
      						reply.setPerformative(ACLMessage.INFORM);
      				  	reply.setContent("pong");
      					}
      				else
      			  	{
      			  		reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
      			  		reply.setContent("( uncorrect content for a ping message )");
      			  	}
      			else
      			{
      				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
      			  reply.setContent("( uncorrect content for a ping message )");
      			}
      			 
      		}
      		else
      		reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
       	      		
      		log("\nReplied with the following message: "+ reply.toString());
          send(reply);
      	}
      }else{
      	//System.out.println("No message received");
      }
    }
  
    public boolean done() {
      return finished;
    }
  } //End class WaitPingAndReplyBehaviour
    
  
  protected void setup() {
  	
    	/** Registration with the DF */
  	  DFAgentDescription dfd = new DFAgentDescription();
    	ServiceDescription sd = new ServiceDescription();   
    	sd.setType("PingAgent"); 
    	sd.setName(getName());
    	sd.setOwnership("ExampleReceiversOfJADE");
    	sd.addOntologies("PingAgent");
    	dfd.setName(getAID());
    	dfd.addServices(sd);
    	try {
      	DFServiceCommunicator.register(this,dfd);
    	} catch (FIPAException e) {
      	System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
      	doDelete();
    	}

    	try{
    		logFile = new BufferedWriter(new FileWriter(getLocalName()+".log"));
    		log("\nAgent: " + getName() + " born on " + new Date());
  			WaitPingAndReplyBehaviour PingBehaviour = new  WaitPingAndReplyBehaviour(this);
    		addBehaviour(PingBehaviour);
    	}catch(IOException e){
    		System.out.println("WARNING: The agent needs the "+ getLocalName()+".log file.");
    		e.printStackTrace();
    	}
  }

	public synchronized void log(String str) {
  	try {
    	logFile.write(str,0,str.length());
    	logFile.flush();
  	} catch (IOException e) {
    	e.printStackTrace();
    	doDelete();
  	}
	}

}//end class PingAgent