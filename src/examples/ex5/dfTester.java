/*

  $Log$
  Revision 1.13  1998/12/01 23:33:19  rimassa
  Completely rewritten (again!) to use new Agent class API for DF
  access. Now all DF actions are carried on by means of suitable Agent
  class methods. This results in blocking interactions. Still missing
  search constraints.

  Revision 1.12  1998/11/30 00:12:23  rimassa
  Almost completely rewritten; now it correctly supports a complete DF
  interaction, search action included. Still missing some detail, such
  as search constraints.

  Revision 1.11  1998/11/23 00:11:58  rimassa
  Fixed a little bug: an instance variable vas not reset after each
  message received.

  Revision 1.10  1998/11/18 22:56:49  Giovanni
  Reading input from the standard input has been moved from setup()
  method to an agent Behaviour. Besides, a suitable reset() method was
  added to mainBehaviour, in order to make dfTester agent perform an
  endless loop of requests to the DF, asking for different parameters
  each time.

  Revision 1.9  1998/10/18 17:33:53  rimassa
  Added support for 'search' DF operation.

  Revision 1.8  1998/10/18 16:10:39  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.7  1998/10/11 19:09:17  rimassa
  Removed old,commented out code.

  Revision 1.6  1998/10/04 18:00:32  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex5;

// This agent allows a comprehensive testing of Directory Facilitator
// agent.

import java.io.*;
import java.util.Vector;


import jade.core.*;
import jade.lang.acl.*;
import jade.domain.SearchDFBehaviour;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;


public class dfTester extends Agent {

  private AgentManagementOntology.DFSearchResult searchResult;

  protected void setup() {

    ComplexBehaviour mainBehaviour = new SequentialBehaviour(this) {

      protected void postAction() {
	reset();
      }

    };

    mainBehaviour.addBehaviour(new OneShotBehaviour(this) {

      public void action() {

	int len = 0;
	byte[] buffer = new byte[1024];

	try {
	  System.out.println("Enter DF agent action to perform: (register, deregister, modify, search) ");
	  len = System.in.read(buffer);
	  AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
	  String actionName = new String(buffer,0,len-1);

	  System.out.println("Enter values for parameters (ENTER leaves them blank)");

	  String name = null;
	  String address = null;
	  System.out.print(":agent-name (e.g. Peter; mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    name = new String(buffer,0,len-1);

	  System.out.println(":agent-address (e.g. iiop://fipa.org:50/acc)");
	  System.out.print("  (mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    address = new String(buffer,0,len-1);

	  if(address != null) {
	    if(name != null)
	      dfd.setName(name + "@" + address);
	    dfd.addAddress(address);
	  }

	  System.out.print(":agent-type (mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setType(new String(buffer,0,len-1));

	  System.out.println(":interaction-protocols ");
	  System.out.print("  protocol name (ENTER to end) ");
	  len = System.in.read(buffer);
	  while(len > 1) {
	    dfd.addInteractionProtocol(new String(buffer,0,len-1));
	    System.out.print("  protocol name (ENTER to end) ");
	    len = System.in.read(buffer);
	  }

	  System.out.print(":ontology ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setOntology(new String(buffer,0,len-1));

	  System.out.print(":ownership (mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setOwnership(new String(buffer,0,len-1));

	  System.out.print(":df-state (active, suspended, retired; mandatory for 'register') ");
	  len = System.in.read(buffer);
	  if(len > 1)
	    dfd.setDFState(new String(buffer,0,len-1));

	  System.out.println(":agent-services ");
	  System.out.println(":service-description (leave name blank to end)");
	  System.out.print("  :service-name ");
	  len = System.in.read(buffer);
	  while(len > 1) {
	    AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
	    sd.setName(new String(buffer,0,len-1));
	    System.out.print("  :service-type ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setType(new String(buffer,0,len-1));
	    System.out.print("  :service-ontology ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setOntology(new String(buffer,0,len-1));
	    System.out.print("  :fixed-properties ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setFixedProps(new String(buffer,0,len-1));
	    System.out.print("  :negotiable-properties ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setNegotiableProps(new String(buffer,0,len-1));
	    System.out.print("  :communication-properties ");
	    len = System.in.read(buffer);
	    if(len > 1)
	      sd.setCommunicationProps(new String(buffer,0,len-1));

	    dfd.addAgentService(sd);

	    System.out.println(":service-description (leave name blank to end)");
	    System.out.print("  :service-name ");
	    len = System.in.read(buffer);
	  }

	  System.out.println("");

	  if(actionName.equalsIgnoreCase("search")) {

	    AgentManagementOntology.DFSearchResult agents = null;
	    System.out.println("Calling searchDF()");
	    try {
	      agents = searchDF("df", dfd, null);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	    try {
	      Writer w = new BufferedWriter(new OutputStreamWriter(System.out));
	      agents.toText(w);
	      System.out.println("");
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else if(actionName.equalsIgnoreCase("register")) {
	    System.out.println("Calling registerWithDF()");
	    try {
	      registerWithDF("df", dfd);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else if(actionName.equalsIgnoreCase("deregister")) {
	    System.out.println("Calling deregisterWithDF()");
	    try {
	      deregisterWithDF("df", dfd);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else if(actionName.equalsIgnoreCase("modify")) {
	    System.out.println("Calling modifyDFData()");
	    try {
	      modifyDFData("df", dfd);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else {
	    System.out.println("Invalid action name.");
	  }

	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

      }

    });

    addBehaviour(mainBehaviour);

  } // End of setup()

}

  
