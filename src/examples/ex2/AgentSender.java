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

import java.io.StringReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.InterruptedIOException;
import java.io.IOException;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$  
*/


// A simple agent that can send a custom message to another agent.
public class AgentSender extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
        try {
          byte[] buffer = new byte[1024];
          System.out.println(getLocalName()+" Enter an ACL message:");
          int len = System.in.read(buffer);
          String content = new String(buffer,0,len-1);

          ACLMessage msg = ACLMessage.fromText(new StringReader(content));
	  msg.setSource(getLocalName());

          send(msg);

          System.out.println(getLocalName() + " is waiting for reply..");

          ACLMessage reply = blockingReceive();
	  System.out.println(getLocalName()+ " received the following ACLMessage: " );
	  reply.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
	catch(InterruptedIOException iioe) {
	  doDelete();
	}
        catch(IOException ioe) {
          ioe.printStackTrace();
        }
	catch(jade.lang.acl.ParseException jlape) {
	  jlape.printStackTrace();
	}
      }

    });

  }

}

