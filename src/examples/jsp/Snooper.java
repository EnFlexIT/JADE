package examples.jsp;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 * This agent is to be used in a JSP page. It just sends
 * messages to a buffer agent.
 */
public class Snooper extends Agent {
    private ACLMessage msg;

    public Snooper() {
	// Create the message to send to the client
	msg = new ACLMessage(ACLMessage.INFORM);
    }

    /**
     * The method that will be invoked in the JSP page.
     * @param str the message to send to the client
     */
    public void snoop(String str) {
	// JADE 2.0: 
      // getHap() cannot be moved in the constructor because it would not work!
	msg.addReceiver(new AID("buffer@"+getHap()));
	// JADE 1.4:
	//msg.addDest("buffer");
	msg.setContent(str);
	send(msg);
    }

  /* just for testing 
protected void setup() {
  snoop("CIAO");
}*/
}
