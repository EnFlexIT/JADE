package examples.ex7;

import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

import java.util.*;
import java.io.*;



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
      msg.dump();

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


