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

package examples.Base64;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import java.util.*;
import java.io.*;


/**
This agent makes the following task: 
1. registers itself with the df as a reader;
2. waits a message from its companion, the ObjectWriterAgent; 
3. reads the content of the message, knowing a-priori that 
   it is encoded in Base64 and contains a Java object;
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class ObjectReaderAgent extends Agent {

private DFAgentDescription dfd = new DFAgentDescription();    

protected void setup() {

  /** Registration with the DF */
  ServiceDescription sd = new ServiceDescription();
  sd.setType("ObjectReaderAgent"); 
  sd.setName(getName());
  sd.setOwnership("ExampleOfJADE");
  dfd.addServices(sd);
  dfd.setName(getAID());
  dfd.addOntologies("Test_Example");
  try {
    registerWithDF(getDefaultDF(),dfd);
  } catch (FIPAException e) {
    System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
    doDelete();
  }
  /** End registration with the DF **/
  System.out.println(getLocalName()+ " succeeded in registration with DF");

  while (true) {
    try {
      System.out.println(getLocalName()+" is waiting for a message");
      ACLMessage msg = blockingReceive(); 
  
      Person p = (Person)msg.getContentObject();
      System.out.println(getLocalName()+ " read Java Object " + p.getClass().getName() + p.toString());
      
    } catch(UnreadableException e3){
    	  System.err.println(getLocalName()+ " catched exception "+e3.getMessage());
    }
  }
}

  public void takeDown() {
    try {
      deregisterWithDF(getDefaultDF(), dfd);
    }
    catch (FIPAException e) {
      System.err.println(getLocalName()+" deregistration with DF unsucceeded. Reason: "+e.getMessage());
    }
  }


}
