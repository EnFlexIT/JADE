/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package examples.ex7;

import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

import java.util.*;
import java.io.*;


/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class ObjectReaderAgent extends Agent {

private AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();    

protected void setup() {

  /** Registration with the DF */

  dfd.setType("ObjectReaderAgent"); 
  dfd.setName(getName());
  dfd.addAddress(getAddress());
  dfd.setOwnership("Example7OfJADE");
  dfd.setOntology("Test_Example");
  dfd.setDFState("active");
  try {
    registerWithDF("DF",dfd);
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
      
      msg.toText(new BufferedWriter( new OutputStreamWriter(System.out)));
      ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(msg.getContentBase64()));

      /* Read all objects written in the content up to the end of the content */
      while (true) {
	Object o = oin.readObject();
	System.out.println(getLocalName()+" read Java Object: "+o.getClass().getName()+" toString()="+o.toString());
      }

    } catch (IOException e ) {
      System.err.println(getLocalName()+" catched exception "+e.getMessage());
    } catch (ClassNotFoundException e1) {
      System.err.println(getLocalName()+" catched exception "+e1.getMessage());
    }
  }
}

  public void takeDown() {
    try {
      deregisterWithDF("DF", dfd);
    }
    catch (FIPAException e) {
      System.err.println(getLocalName()+" deregistration with DF unsucceeded. Reason: "+e.getMessage());
    }
  }


}


