/*
  $Log$
  Revision 1.11  1999/03/31 15:54:30  rimassa
  Added correct handling of IOException.

  Revision 1.10  1999/03/30 13:35:15  rimassa
  Changed some getName() calls to getLocalName().

  Revision 1.9  1998/10/18 16:10:24  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.8  1998/10/04 18:00:19  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex2;

import java.io.StringReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.InterruptedIOException;
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

      }

    });

  }

}

