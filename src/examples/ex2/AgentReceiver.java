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


package examples.ex2;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$  
*/


// An agent who continuously receives messages and sends back replies.
public class AgentReceiver extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
        System.out.println("Now receiving (blocking style)...");
        ACLMessage msg = blockingReceive(10000);
	if(msg != null) {
	  msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

	  System.out.println("Sending back reply to sender ...");
	  ACLMessage reply = new ACLMessage("inform");
	  reply.setSource(getLocalName());
	  reply.removeAllDests();
	  reply.addDest(msg.getSource());
	  reply.setContent("\"Thank you for calling, " + msg.getSource() + "\"");
	  send(reply);
	}
	else {
	  System.out.println("Timeout expired!");
	}
      }

    });

  }

}
