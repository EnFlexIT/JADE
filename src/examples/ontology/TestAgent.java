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


package examples.ontology;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.*;
import jade.core.*;
import jade.lang.sl.*;

public class TestAgent extends Agent {

protected void setup() {
  // register the codec of the language
  registerLanguage(SL0Codec.NAME,new SL0Codec());	
		
  // register the ontology used by application
  registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
	
  System.out.println("This TestAgent can be used to test the ACL Parser, the SL0 Parser, and the Fipa-Agent-Management ontology.");
  System.out.println("Send it an ACL message, whose language parameter is set to FIPA-SL0 and whose ontology parameter is set to fipa-agent-management. USE STDIN.");

  ACLParser parser = new ACLParser(System.in);
  ACLMessage msg;
  while (true) {  
    try {
      msg = parser.Message();
      Object obj=extractContent(msg);
      System.out.println(obj.getClass().toString());
      msg = msg.createReply();
      msg.setPerformative(ACLMessage.INFORM);
      msg.setContent("(true)");
      System.out.println(msg.toString());
    } catch (FIPAException e) {
      e.printStackTrace();
    } catch (jade.lang.acl.ParseException e) {
      e.printStackTrace();
    }
  }
}

public static void main(String args[]) {
  TestAgent a = new TestAgent();
  a.setup();
}



}
