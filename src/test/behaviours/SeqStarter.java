package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import java.util.Vector;
import java.util.Enumeration;
import test.common.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class SeqStarter extends Agent {	
	private static final String SENDER_CLASS = "test.behaviours.SeqSender";
	private static final String RECEIVER_CLASS = "test.behaviours.SeqReceiver";

	private static final int DEFAULT_N_AGENTS = 1;
	private static final int DEFAULT_N_MESSAGES = 100;
	private static final long DEFAULT_SHORTEST_PERIOD = 1000; // 1 sec
	private int nAgents = DEFAULT_N_AGENTS;
	private int nMessages = DEFAULT_N_MESSAGES;
	private long shortestPeriod = DEFAULT_SHORTEST_PERIOD;
	
  protected void setup() {
    Object[] args = getArguments();
    if (args != null && args.length > 0) {
    	// Number of senders/receivers
      nAgents = Integer.parseInt((String) args[0]);
      if (args.length > 1) {
      	// Number of messages
      	nMessages = Integer.parseInt((String) args[1]);
      	if (args.length > 2) {
      		// Shortest period
	      	shortestPeriod = Long.parseLong((String) args[2]);
      	}
      }
    } 

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    try {
	    TestUtility.launchJadeInstance("Container", null, "-container", new String[] {});
  	  for (int i = 0; i < nAgents; ++i) {
  	  	String senderName = new String("s"+i);
  	  	String receiverName = new String("r"+i);
    		// Launch the sender on the main
    		String[] agentArgs = new String[] {receiverName, String.valueOf(nMessages), String.valueOf(shortestPeriod*(i+1))}; 
    		TestUtility.createAgent(this, senderName, SENDER_CLASS, agentArgs, getAMS(), AgentManager.MAIN_CONTAINER_NAME); 
				// Launch the receiver on container-1 (it exists for sure)
    		agentArgs = new String[] {getLocalName()};
    		TestUtility.createAgent(this, receiverName, RECEIVER_CLASS, agentArgs, getAMS(), "Container-1");
    		// Prepare the message to start the senders
    		msg.addReceiver(new AID(senderName, AID.ISLOCALNAME));
  	  }
    }
    catch (TestException te) {
    	te.printStackTrace();
    }
    
    // Send the sterting message to the senders
    send(msg);
    
    addBehaviour(new SimpleBehaviour(this) {
    	private int cnt = 0;
    	public void action() {
    		ACLMessage msg1 = receive();
    		if (msg1 != null) {
    			cnt++;
    		}
    		block();
    	}
    	
    	public boolean done() {
    	 	return (cnt >= nAgents);
    	}
    	
    	public int onEnd() {
    		System.out.println("All "+cnt+" agents completed successfully");
    		return 0;
    	}
    } );
  }
}
