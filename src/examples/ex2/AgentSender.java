/*
  $Id$
*/

package examples.ex2;

import java.io.StringReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import jade.core.*;
import jade.lang.acl.*;


// A simple agent that can send a custom message to another agent.
public class AgentSender extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
        try {
          byte[] buffer = new byte[1024];
          System.out.println(getName()+" Enter an ACL message:");
          int len = System.in.read(buffer);
          String content = new String(buffer,0,len-1);

          ACLMessage msg = parse(new StringReader(content));
	  msg.setSource(getName());

          send(msg);

          System.out.println(getName()+" is waiting for reply..");

          ACLMessage reply = blockingReceive();
	  System.out.println(getName()+ " received the following ACLMessage: " );
	  reply.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
        catch(IOException ioe) {
          ioe.printStackTrace();
        }

      }

    });

  }

}

