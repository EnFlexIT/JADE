package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * @author Giovanni Caire - TILAB
 */
public class SeqReceiver extends Agent {	
	private Object outputLock = SeqSender.outputLock;
	private int max = SeqSender.DEFAULT_MAX;
  private Vector received = new Vector();
  private AID starterID = null;

  /**
   */
  protected void setup() {
    Object[] args = getArguments();
    if (args != null && args.length > 0) {
    	// Name of agent to notify completion
      starterID = new AID((String) args[0], AID.ISLOCALNAME);
    } 

    log("Ready.");

    Behaviour b = new SimpleBehaviour(this) {

      /**
       */
      public void action() {
        ACLMessage msg = receive();
        if (msg != null) {
          switch (msg.getPerformative()) {
          case ACLMessage.PROPOSE: {
            // This message carries the number of messages that the sender will send
            String n = msg.getContent();
            max = Integer.parseInt(n);
            log("Expecting "+max+" messages");
            break;
          }
          case ACLMessage.INFORM: {
            // Normal sequence message
            String n = msg.getContent();
            log("Received message "+n);
            received.addElement(n);
            break;
          }
          case ACLMessage.REQUEST: {
            // Request the current status
          	synchronized (outputLock) {
	            log("Received messages are");
  	          printReceived();
    	        log("Missing messages are");
      	      printMissing();
          	}
            break;
          }
          default:
            log("WARNING: unexpected message received\n"+msg);
            break;
          }
        } 
        else {
          block();
        } 
      } 

      /**
       */
      public boolean done() {
        return (received.size() >= max);
      } 

      /**
       */
      public int onEnd() {
      	synchronized (outputLock) {
		      System.out.println("\n\n---------------------");
  		    System.out.println(getName()+": All messages received. Sequence as below");
    		  printReceived();
      	}
      	// If a starter agent was specified notify it
      	if (starterID != null) {
	      	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  	    	msg.addReceiver(starterID);
  	    	msg.setContent("COMPLETED");
  	    	myAgent.send(msg);
      	}
        return 0;
      } 

    };

    addBehaviour(b);
  } 

  /**
   * Method declaration
   *
   * @see
   */
  private void printReceived() {
    Enumeration e = received.elements();
    while (e.hasMoreElements()) {
      String n = (String) e.nextElement();
      System.out.print(n+" ");
    } 
    System.out.println("");
  } 

  /**
   * Method declaration
   *
   * @see
   */
  private void printMissing() {
    for (int i = 0; i < max; ++i) {
      String n = String.valueOf(i);
      if (!wasReceived(n)) {
        System.out.print(n+" ");
      } 
    } 
    System.out.println("");
  } 

  /**
   * Method declaration
   *
   * @param n
   *
   * @return
   *
   * @see
   */
  private boolean wasReceived(String n) {
    Enumeration e = received.elements();
    while (e.hasMoreElements()) {
      if (n.equals((String) e.nextElement())) {
        return true;
      } 
    } 
    return false;
  } 

  private void log(String s) {
  	synchronized (outputLock) {
  		System.out.println(getName()+": "+s);
  	}
	}
}
