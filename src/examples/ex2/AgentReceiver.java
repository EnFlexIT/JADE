/*
  $Log$
  Revision 1.10  1999/06/08 15:49:30  rimassa
  Added a timeout in blockingReceive() call.

  Revision 1.9  1999/05/20 14:12:39  rimassa
  Updated import clauses to reflect JADE package structure changes.

  Revision 1.8  1999/03/09 12:51:05  rimassa
  Removed deprecated 'ACLMessage.getDest()' and 'ACLMessage.setDest()'
  calls.

  Revision 1.7  1998/10/18 16:10:23  rimassa
  Some code changes to avoid deprecated APIs.

   - Agent.parse() is now deprecated. Use ACLMessage.fromText(Reader r) instead.
   - ACLMessage() constructor is now deprecated. Use ACLMessage(String type)
     instead.
   - ACLMessage.dump() is now deprecated. Use ACLMessage.toText(Writer w)
     instead.

  Revision 1.6  1998/10/04 18:00:18  rimassa
  Added a 'Log:' field to every source file.

*/

package examples.ex2;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

// An agent who continuously receives messages and sends back replies.
public class AgentReceiver extends Agent {

  protected void setup() {

    addBehaviour(new CyclicBehaviour(this) {

      public void action() {
        System.out.println("Now receiving (blocking style)...");
        ACLMessage msg = blockingReceive(1000);
	msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

        System.out.println("Sending back reply to sender ...");
        ACLMessage reply = new ACLMessage("inform");
        reply.setSource(getLocalName());
	reply.removeAllDests();
        reply.addDest(msg.getSource());
        reply.setContent("\"Thank you for calling, " + msg.getSource() + "\"");
        send(reply);
      }

    });

  }

}
