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

import java.io.IOException;

import java.util.Vector;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$  
*/


// An agents which sends a message to three other agents, collects the
// replies and prints them.
public class AgentBroadcaster extends Agent {

  private Vector messages = new Vector();

  private class BehaviourElement extends SimpleBehaviour {

    private ACLMessage myMessage;
    private boolean msgSent = false;
    private boolean replyReceived = false;


    public BehaviourElement(String source, String dest, String content) {
      myMessage = new ACLMessage("request");
      myMessage.setSource(source);
      myMessage.setContent(content);
      myMessage.removeAllDests();
      myMessage.addDest(dest);
    }

    public void action() {
      if(msgSent == false) {
	System.out.println("Sending to " + myMessage.getFirstDest());
	send(myMessage);
	msgSent = true;
      }
      else {
	ACLMessage reply = receive();
	if(reply != null) {
	  System.out.println("Received reply");
	  appendMessage(reply);
	  replyReceived = true;
	}
      }
    }

    public boolean done() {
      return replyReceived;
    }

  }

  protected void setup() {

    ComplexBehaviour cb = new SequentialBehaviour(this) {

      protected void preAction() {
	try {
	  byte[] buffer = new byte[1024];
	  System.out.println("Enter a message:");
	  int len = System.in.read(buffer);
	  String content = new String(buffer,0,len-1);

	  System.out.println("Enter 3 destination agent names:");
	  len = System.in.read(buffer);
	  String dest1 = new String(buffer,0,len-1);
	  len = System.in.read(buffer);
	  String dest2 = new String(buffer,0,len-1);
	  len = System.in.read(buffer);
	  String dest3 = new String(buffer,0,len-1);

	  String source = getName();

	  addSubBehaviour(new BehaviourElement(source,dest1,content));
	  addSubBehaviour(new BehaviourElement(source,dest2,content));
	  addSubBehaviour(new BehaviourElement(source,dest3,content));
	}
	catch(IOException ioe) {
	  ioe.printStackTrace();
	}
      }

      protected void postAction() {
	System.out.println("Post-Action");
	dumpMessage();
	doDelete(); // Terminates the agent
      }

    };

    addBehaviour(cb);

  }

  public synchronized void appendMessage(ACLMessage msg) {
    messages.addElement(msg.getContent());
  }

  public void dumpMessage() {
    System.out.print("The three pieces are: " + messages.elementAt(0) + ", ");
    System.out.println(messages.elementAt(1) + ", " + messages.elementAt(2));
    System.out.println("The result is: " + messages.elementAt(0) + messages.elementAt(1) + messages.elementAt(2));
  }

}
