/*

  $Log$
  Revision 1.17  1999/02/25 08:02:46  rimassa
  Added a correct InterruptedIOException handler; the agent is
  terminated.

  Revision 1.16  1999/02/14 22:52:26  rimassa
  Renamed addBehaviour() calls to addSubBehaviour() calls.

  Revision 1.15  1999/02/03 15:34:09  rimassa
  Horrible hack to deal with CR/LF vs. LF. It should be fixed...

  Revision 1.14  1998/12/07 23:37:24  rimassa
  Added support for search constraints and for multiple DFs. Now dfTester
  is a complete FIPA 98 client for Directory Facilitators.

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

    mainBehaviour.addSubBehaviour(new OneShotBehaviour(this) {

      public void action() {

	int len = 0;
	byte[] buffer = new byte[1024];

	try {
	  System.out.println("Enter DF agent action to perform: (register, deregister, modify, search) ");
	  len = System.in.read(buffer);
	  AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
          String actionName = new String(buffer,0,len-2);

	  System.out.println("Enter DF name (ENTER uses platform default DF)");
	  len = System.in.read(buffer);

	  String dfName = "df";
          if(len > 2)
            dfName = new String(buffer,0,len-2);

	  System.out.println("Enter values for parameters (ENTER leaves them blank)");

	  String name = null;
	  String address = null;
	  System.out.print(":agent-name (e.g. Peter; mandatory for 'register') ");
	  len = System.in.read(buffer);
          if(len > 2)
            name = new String(buffer,0,len-2);

	  System.out.println(":agent-address (e.g. iiop://fipa.org:50/acc)");
	  System.out.print("  (mandatory for 'register') ");
	  len = System.in.read(buffer);
          if(len > 2)
            address = new String(buffer,0,len-2);

	  if(address != null) {
	    if(name != null)
	      dfd.setName(name + "@" + address);
	    dfd.addAddress(address);
	  }

	  System.out.print(":agent-type (mandatory for 'register') ");
	  len = System.in.read(buffer);
          if(len > 2)
            dfd.setType(new String(buffer,0,len-2));

	  System.out.println(":interaction-protocols ");
	  System.out.print("  protocol name (ENTER to end) ");
	  len = System.in.read(buffer);
          while(len > 2) {
            dfd.addInteractionProtocol(new String(buffer,0,len-2));
	    System.out.print("  protocol name (ENTER to end) ");
	    len = System.in.read(buffer);
	  }

	  System.out.print(":ontology ");
	  len = System.in.read(buffer);
          if(len > 2)
            dfd.setOntology(new String(buffer,0,len-2));

	  System.out.print(":ownership (mandatory for 'register') ");
	  len = System.in.read(buffer);
          if(len > 2)
            dfd.setOwnership(new String(buffer,0,len-2));

	  System.out.print(":df-state (active, suspended, retired; mandatory for 'register') ");
	  len = System.in.read(buffer);
          if(len > 2)
            dfd.setDFState(new String(buffer,0,len-2));

	  System.out.println(":agent-services ");
	  System.out.println(":service-description (leave name blank to end)");
	  System.out.print("  :service-name ");
	  len = System.in.read(buffer);
          while(len > 2) {
	    AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
            sd.setName(new String(buffer,0,len-2));
	    System.out.print("  :service-type ");
	    len = System.in.read(buffer);
            if(len > 2)
              sd.setType(new String(buffer,0,len-2));
	    System.out.print("  :service-ontology ");
	    len = System.in.read(buffer);
            if(len > 2)
              sd.setOntology(new String(buffer,0,len-2));
	    System.out.print("  :fixed-properties ");
	    len = System.in.read(buffer);
            if(len > 2)
              sd.setFixedProps(new String(buffer,0,len-2));
	    System.out.print("  :negotiable-properties ");
	    len = System.in.read(buffer);
            if(len > 2)
              sd.setNegotiableProps(new String(buffer,0,len-2));
	    System.out.print("  :communication-properties ");
	    len = System.in.read(buffer);
            if(len > 2)
              sd.setCommunicationProps(new String(buffer,0,len-2));

	    dfd.addAgentService(sd);

	    System.out.println(":service-description (leave name blank to end)");
	    System.out.print("  :service-name ");
	    len = System.in.read(buffer);
	  }

	  System.out.println("");

	  if(actionName.equalsIgnoreCase("search")) {

	    System.out.println("Enter search constraints: ");
	    System.out.println("  Constraint name (':df-depth' or ':resp-req')");
	    System.out.print("  ENTER to end: ");
	    len = System.in.read(buffer);
	    Vector constraints = null;
            while(len > 2) {
	      if(constraints == null)
		constraints = new Vector();
	      AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
              c.setName(new String(buffer,0,len-2));
	      System.out.print("  Constraint function ('Min', 'Max' or 'Exactly'): ");
	      len = System.in.read(buffer);
              if(len > 2)
                c.setFn(new String(buffer,0,len-2));
	      else
		c.setFn("Exactly");
	      System.out.print("  Constraint argument (a positive integer): ");
	      len = System.in.read(buffer);
              if(len > 2)
                c.setArg(Integer.parseInt(new String(buffer,0,len-2)));
	      else
                c.setArg(1);

	      constraints.addElement(c);

	      System.out.println("  Constraint name (':df-depth' or ':resp-req')");
	      System.out.print("  ENTER to end: ");
	      len = System.in.read(buffer);

	    }

	    AgentManagementOntology.DFSearchResult agents = null;
	    System.out.println("Calling searchDF()");
	    try {
	      agents = searchDF(dfName, dfd, constraints);
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
	      registerWithDF(dfName, dfd);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else if(actionName.equalsIgnoreCase("deregister")) {
	    System.out.println("Calling deregisterWithDF()");
	    try {
	      deregisterWithDF(dfName, dfd);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else if(actionName.equalsIgnoreCase("modify")) {
	    System.out.println("Calling modifyDFData()");
	    try {
	      modifyDFData(dfName, dfd);
	    }
	    catch(FIPAException fe) {
	      System.out.println("Caught a FIPA exception: " + fe.getMessage());
	    }
	  }
	  else {
	    System.out.println("Invalid action name.");
	  }

	}
	catch(InterruptedIOException iioe) {
	  doDelete();
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

      }

    });

    addBehaviour(mainBehaviour);

  } // End of setup()

}

  
