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


package examples.ex5;

// This agent allows a comprehensive testing of Directory Facilitator
// agent.

import java.io.*;
import java.util.Vector;


import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.SearchDFBehaviour;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

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

  
