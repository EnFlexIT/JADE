/*
  $Log$
  Revision 1.1  1999/06/16 00:29:12  rimassa
  Example program to show per-behaviour timeout usage in JADE.

  Revision 1.12  1999/06/09 12:52:19  rimassa
  Changed timeout value from 1 second to 10 seconds.

  Revision 1.11  1999/06/08 23:44:40  rimassa
  Added a timeout argument in blockingReceive().

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
public class AgentTimeout extends Agent {

  private ReceiverBehaviour.Handle h;

  protected void setup() {

    ComplexBehaviour main = new SequentialBehaviour(this) {
      protected void postAction() {
	reset();
      }

    };

    MessageTemplate mt = MessageTemplate.MatchType("inform");
    h = ReceiverBehaviour.newHandle();
    main.addSubBehaviour(new ReceiverBehaviour(this, h, 5000, mt));
    main.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	try {
	  System.out.println("About to read the message...");
	  ACLMessage msg = h.getMessage();
	  msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
	}
	catch(ReceiverBehaviour.TimedOut rbto) {
	  System.out.println("Exception caught: " + rbto.getMessage());
	}
	catch(ReceiverBehaviour.NotYetReady rbnyr) {
	  System.out.println("ERROR !!! It should't happen.");
	  rbnyr.printStackTrace();
	}
      }
    });

    addBehaviour(main);

  }

}

