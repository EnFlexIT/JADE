package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class SeqSender extends Agent {
	public static final Object outputLock = new Object();
  public static final int    DEFAULT_MAX = 100;
  public static final String DEFAULT_RECV_NAME = "r";
  public static final long   DEFAULT_PERIOD = 3000;

  String                     recvName = DEFAULT_RECV_NAME;
  int                        max = DEFAULT_MAX;
  long                       period = DEFAULT_PERIOD;
  int                        cnt = 0;
  int                        failureCnt = 0;
  ACLMessage                 msg = null;

  /**
   * Method declaration
   *
   * @see
   */
  protected void setup() {
    // Get the name of the receiver, max messages to send and period if specified
    Object[] args = getArguments();
    if (args != null && args.length > 0) {
      recvName = (String) args[0];
      if (args.length > 1) {
      	max = Integer.parseInt((String) args[1]);
      	if (args.length > 2) {
	      	period = Long.parseLong((String) args[2]);
      	}
      }
    } 

    log("Sending "+max+" messages to "+recvName+". Period is "+period+" ms");
    log("Send me a message to start");
    
    // Whatever received message makes the test start
    blockingReceive();
    
    // Send the number of messages that are going to be sent to the receiver
    sendPropose();

    msg = new ACLMessage(ACLMessage.INFORM);
    msg.addReceiver(new AID(recvName, AID.ISLOCALNAME));

    Behaviour b = new TickerBehaviour(this, period) {

      /**
       */
      public void onTick() {
        msg.setContent(String.valueOf(cnt));
        send(msg);
        log("Sent message "+(cnt++));
        if (cnt >= max) {
          stop();
        } 
      } 

      /**
       */
      public int onEnd() {
        log("All messages sent.");
        return 0;
      } 

    };
    addBehaviour(b);

    b = new CyclicBehaviour() {

      /**
       */
      public void action() {
        ACLMessage msg = receive();
        if (msg != null) {
          switch (msg.getPerformative()) {
          case ACLMessage.FAILURE:
            failureCnt++;
            break;
          case ACLMessage.REQUEST:
            log("Received "+failureCnt+" FAILURE messages");
            break;
          }
        } 
        else {
          block();
        } 
      } 

    };
    addBehaviour(b);
  } 

  private void sendPropose() {
  	ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
  	msg.addReceiver(new AID(recvName, AID.ISLOCALNAME));
  	msg.setContent(String.valueOf(max));
  	send(msg);
  }
  
  private void log(String s) {
  	synchronized (outputLock) {
  		System.out.println(getName()+": "+s);
  	}
	}
}
