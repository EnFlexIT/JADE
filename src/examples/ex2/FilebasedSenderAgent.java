/*
  FilebasedSenderAgent.java
*/

package examples.ex2;

import java.io.*;
import java.io.StringReader;


import jade.core.*;
import jade.lang.acl.*;


/**
 * This class implements a simple Agent.
 * It prompts for the name of a file, converts the content of the file
 * to an ACLMessage, and finally sends the message.
 */
public class FilebasedSenderAgent extends Agent {

  private Frame1 myGui=new Frame1(this); 
    
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
	  StringWriter w = new StringWriter();
	  msg.toText(w);
	  myGui.receivedMessage("("+w.toString()+")");
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





