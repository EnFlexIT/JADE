/*
  $Log$
  Revision 1.3  1999/05/20 14:12:39  rimassa
  Updated import clauses to reflect JADE package structure changes.

  Revision 1.2  1999/02/25 08:00:10  rimassa
  Removed direct usage of 'myName' and 'myAddress' variables.
  Added a correct handling of InterruptedIOException; it now terminates
  the agent.

  Revision 1.1  1998/12/08 00:24:41  rimassa
  Added this new example to show how to set up multiple Agent Domains.
  This agent is simply a DF agent, but on startup it asks the user for a
  parent DF to register with. Using this simple example it is possible to
  build complex DF trees or graphs and exploit recursive search capabilities
  of JADE DF agent.

*/

package examples.ex5;

import java.io.IOException;
import java.io.InterruptedIOException;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

public class SubDF extends jade.domain.df {

  public void setup() {

    // Input df name
    int len = 0;
    byte[] buffer = new byte[1024];

    try {

      String parentName = "df";
      System.out.print("Enter parent DF name (ENTER uses platform default DF): ");
      len = System.in.read(buffer);
      if(len > 1)
	parentName = new String(buffer,0,len-1);

      AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
      dfd.setName(getName());
      dfd.addAddress(getAddress());
      dfd.setType("SubDF");
      dfd.addInteractionProtocol("fipa-request");
      dfd.setOntology("fipa-agent-management");
      dfd.setOwnership("JADE");
      dfd.setDFState("active");

    AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
    sd.setName(getLocalName() + "-sub-df");
    sd.setType("fipa-df");

    dfd.addAgentService(sd);

    try {
      registerWithDF(parentName, dfd);
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

    super.setup();
    }
    catch(InterruptedIOException iioe) {
      doDelete();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }

  }


}
