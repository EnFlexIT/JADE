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

import java.util.List;
import java.io.*;

import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.domain.FIPAException;
import jade.lang.acl.ACLParser;
import jade.lang.acl.ParseException;
import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.lang.sl.SL0Codec;
import jade.domain.MobilityOntology;

public class TestAgent extends Agent {

protected void setup() {
  // register the codec of the language
  registerLanguage(SL0Codec.NAME,new SL0Codec());	
		
  // register the ontology used by application
  registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
  // register the ontology used by application
  registerOntology(MobilityOntology.NAME, MobilityOntology.instance());
	
  System.out.println("This TestAgent can be used to test the ACL Parser, the SL0 Parser, and the Fipa-Agent-Management ontology all together.");
  System.out.println("It is an application (i.e. do not need to run a JADE Agent Platform).");
  System.out.println("The application reads from the file examples/ontology/testmessages.msg a sequence of ACL messages, whose language parameter is set to FIPA-SL0 and whose ontology parameter is set to fipa-agent-management.");

  try {
    ACLParser parser = new ACLParser(new FileReader("examples/ontology/testmessages.msg")); //System.in);
  ACLMessage msg;
  while (true) {  
    try {
      PushAKey();
      System.out.println("\nREADING NEXT MESSAGE FROM THE FILE ...");
      msg = parser.Message();
      System.out.println("  read the following message:\n"+msg.toString());
      System.out.println("\nEXTRACTING THE CONTENT AND CREATING A LIST OF JAVA OBJECTS ...");
      List l=extractContent(msg);
      System.out.print("  created the following classes: (");
      for (int i=0; i<l.size(); i++)
	System.out.print(l.get(i).getClass().toString()+" ");
      System.out.println(")");

      msg = msg.createReply();
      System.out.println("\nFILLING BACK THE CONTENT WITH THE LIST OF JAVA OBJECTS ...");
      fillContent(msg,l);
      System.out.println("  created the following message:\n"+msg.toString());

      System.out.println("\nDOUBLE CHECK BY EXTRACTING THE CONTENT AGAIN ...");
      l=extractContent(msg);
      System.out.print("  created the following classes: (");
      for (int i=0; i<l.size(); i++)
	System.out.print(l.get(i).getClass().toString()+" ");
      System.out.println(")");

      System.out.println("\n FINAL CHECK BY FILLING AGAIN THE CONTENT WITH THE LIST OF JAVA OBJECTS ...");
      fillContent(msg,l);
      System.out.println(" created the following message:\n"+msg.toString());

    } catch (FIPAException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (jade.lang.acl.ParseException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
  } catch (FileNotFoundException e) {
    e.printStackTrace();
  }

}

private void PushAKey(){
  System.out.println("Press ENTER");
  try {
    BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
    buff.readLine();
  } catch (Exception e) {
  }
}

public static void main(String args[]) {
  TestAgent a = new TestAgent();
  a.setup();
}



}
