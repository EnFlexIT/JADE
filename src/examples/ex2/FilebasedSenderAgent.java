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

/*
  FilebasedSenderAgent.java
*/

package examples.ex2;

import java.io.*;
import java.io.StringReader;


import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$  
*/


/**
 * This class implements a simple Agent.
 * It prompts for the name of a file, converts the content of the file
 * to an ACLMessage, and finally sends the message.
 */
public class FilebasedSenderAgent extends Agent {

  private SentReceivedGUI myGui = new SentReceivedGUI(this);
    
  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
	ACLMessage msg = null;
	msg = receive();
	if (msg == null) {
	  block();
	  return;
	}
	else {
	  //System.err.println("FilebasedSenderAgent: ");
	  //msg.dump();
	  StringWriter w = new StringWriter();
	  msg.toText(w);
	  myGui.receivedMessage(w.toString());
	}
      }
    });

    myGui.setTitle("FilebasedSenderAgent " + getName());
    myGui.setVisible(true);    

  }

  protected void takeDown() {

    myGui.setVisible(false);  // hide the Frame
    myGui.dispose();	      // free the system resources

  }

}





