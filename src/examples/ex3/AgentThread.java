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


package examples.ex3;

import java.io.StringReader;
import java.io.IOException;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

// This agent starts a conversation thread and collect the reply.
public class AgentThread extends Agent {

  protected void setup() {

    addBehaviour(new OneShotBehaviour(this) {

      public void action() {
	try {
	  byte[] buffer = new byte[1024];
	  System.out.println("Enter a message:");
	  int len = System.in.read(buffer);
	  String content = new String(buffer,0,len-1);

	  System.out.println("Enter destination agent name:");
	  len = System.in.read(buffer);
	  String dest = new String(buffer,0,len-1);

	  System.out.println("Sending message to " + dest);


	  String text = new String("( request :sender " + getName());
	  text = text + " :receiver " + dest + " :content ( " + content + " ) )";
	  System.out.println(text);
	  ACLMessage msg = ACLMessage.fromText(new StringReader(text));


	  send(msg);
	  send(msg);
	  send(msg);

	  msg.setReplyWith("alt.agents.fipa");

	  send(msg);

	  System.out.println("Waiting for reply..");

	  MessageTemplate mt = MessageTemplate.MatchReplyTo("alt.agents.fipa");
	  ACLMessage reply = blockingReceive(mt);
	  System.out.println("Received from " + reply.getSource());
	  System.out.println(reply.getContent());
	}
	catch(jade.lang.acl.ParseException jlape) {
	  jlape.printStackTrace();
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}

	doDelete(); // Terminates the agent
      }

    });

  }

}
