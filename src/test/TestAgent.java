/*****************************************************************
Jade - Java Agent DEvelopment Framework is a framework to develop
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


package test;

import java.util.List;
import java.io.*;

import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.domain.MobilityOntology;
import jade.domain.JADEAgentManagement.JADEAgentManagementOntology;
import jade.domain.FIPAException;
import jade.domain.introspection.JADEIntrospectionOntology;
import jade.lang.acl.*;
import jade.core.Agent;
import jade.lang.sl.SL0Codec;
import jade.onto.basic.*;
import jade.onto.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
public class TestAgent extends Agent {

final static String SAMPLEFILE = "test/testmessages.msg";

  private StringACLCodec getStringACLCodec() {
    System.out.println("This TestAgent can be used to test the ACL Parser, the SL0 Parser, the Fipa-Agent-Management ontology, the Mobility Ontology, and the JADE-Agent-Management ontology all together.");
    System.out.println("It is an application (i.e. do not need to run a JADE Agent Platform).");
    System.out.println("The application reads from a file, or from standard input,  a sequence of ACL messages, whose language parameter is set to FIPA-SL0 and whose ontology parameter is set to fipa-agent-management or to jade-mobility-ontology. Then it parses the message, creates the Java objects according to the ontology, fills back the content on the basis of these Java objects, and double check these last two operations.");
    System.out.println("The file "+SAMPLEFILE+ " is provided with a lot of interesting sample messages"); 

    System.out.println("ENTER the name of the input file (write sample to use the sample file; write in to use the standard input, any other string is used as a file name");
    String fileName;
    Reader r;
    while (true) {
      try {
	BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
	fileName = buff.readLine();
	if ("sample".equalsIgnoreCase(fileName))
	  r = new FileReader(SAMPLEFILE);
	else if ("in".equalsIgnoreCase(fileName))
	  r = new InputStreamReader(System.in);
	else
	  r = new FileReader(fileName);
	break;
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
    return new StringACLCodec(r, new OutputStreamWriter(System.out)); 
}

protected void setup() {
  // register the codec of the language
  registerLanguage(SL0Codec.NAME,new SL0Codec());	
	
  // test to/from string
  Ontology o1 = new DefaultOntology();
  try {
    o1.fromSL0String(FIPAAgentManagementOntology.instance().toSL0String(FIPAAgentManagementOntology.NAME));
  } catch (Exception e) {
    e.printStackTrace();
  }
  // register the ontology used by application
  registerOntology(FIPAAgentManagementOntology.NAME, o1);

  // test to/from string
  Ontology o2 = new DefaultOntology();
  try {
    o2.fromSL0String(MobilityOntology.instance().toSL0String(MobilityOntology.NAME));
  } catch (Exception e) {
    e.printStackTrace();
  }
  // register the ontology used by application
  registerOntology(MobilityOntology.NAME, o2);

  // test to/from string
  Ontology o3 = new DefaultOntology();
  try {
    o3.fromSL0String(JADEAgentManagementOntology.instance().toSL0String(JADEAgentManagementOntology.NAME));
  } catch (Exception e) {
    e.printStackTrace();
  }
  registerOntology(JADEAgentManagementOntology.NAME, o3); 

  // test to/from string
  Ontology o4 = new DefaultOntology();
  try {
    o4.fromSL0String(JADEIntrospectionOntology.instance().toSL0String(JADEIntrospectionOntology.NAME));
  } catch (Exception e) {
    e.printStackTrace();
  }
  registerOntology(JADEIntrospectionOntology.NAME, o4); 
	

  StringACLCodec codec = getStringACLCodec(); 
  ACLMessage msg;
  while (true) {  
    try {
      PushAKey();
      System.out.println("\nREADING NEXT MESSAGE FROM THE FILE ...");
      msg = codec.decode();
      System.out.println("  read the following message:\n"+msg.toString());
      codec.write(msg);
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
    } catch (ACLCodec.CodecException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
  

}

private void PushAKey(){
  System.out.println("Press ENTER to start processing next message ...");
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
