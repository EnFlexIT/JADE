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

package examples.protocols;


import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.FipaRequestResponderBehaviour;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

/**
This example shows how to use the class FipaRequestResponderBehaviour 
to implement the behaviour of a agent playing the role of 
responder in a Fipa-request protocol.
@author Tiziana Trucco - CSELT S.p.A.
@version $Date$ $Revision$
*/

// This agent plays the responder role in fipa-request protocol.

public class AgRequestResponder extends Agent {

   private DFAgentDescription dfd = new DFAgentDescription();    
  
  private class responderBehav extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory {
    
    responderBehav(Agent a){
      super(a);
    }

    /** 
      this is the factory method
      **/
   public FipaRequestResponderBehaviour.ActionHandler create(){
     return new responderBehav(myAgent);
   }
			
   public void action(){
     double chance = Math.random();
     System.out.println("\n Chance: "+chance);
     if (chance <0.2)
       sendNotUnderstood();
     else if (chance < 0.5)
       sendRefuse("\"I.m too busy at the moment. Retry later\"");
     else {
       sendAgree();
       chance = Math.random();
       if (chance<0.4)
	 sendFailure("\"Something went wrong with the teleport\"");
       else
	 sendInform();
     }
   }
			
    /** one shot only, therefore it returns always trues **/
   public boolean done() {
     return true;
   }
			
    /** there is nothing to reset in this simple sample behaviour **/
   public void reset() {
   }
  }//End of class responderBehav
		
		
  protected void setup(){

    /** Registration with the DF */
    ServiceDescription sd = new ServiceDescription();   
    sd.setType("RequestResponderAgent"); 
    sd.setName(getName());
    sd.setOwnership("ExampleprotocolsOfJADE");
    sd.addOntologies("Test_Example");
    dfd.setName(getAID());
    dfd.addServices(sd);
    try {
      registerWithDF(getDefaultDF(),dfd);
    } catch (FIPAException e) {
      System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
      doDelete();
    }
    /** End registration with the DF **/
    System.out.println(getLocalName()+ " succeeded in registration with DF");

    FipaRequestResponderBehaviour requester = new FipaRequestResponderBehaviour(this);
    requester.registerFactory("ExampleRequest",new responderBehav(this));
    addBehaviour(requester);
  }
	
} // End of class AgentResponder





