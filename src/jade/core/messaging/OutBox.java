package jade.core.messaging;

//#MIDP_EXCLUDE_FILE

import java.util.Hashtable;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.RoundList;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.core.messaging.MessageManager.PendingMsg;


/**
 * Object to mantain message to send and
 * to preserve the order for sending.
 * 
 * @author Elisabetta Cortese - TILAB
 */

class OutBox {
	private int verbosity = 2;
	// The massages to be delivered organized as an hashtable that maps
	// a receiver AID into the Box of messages to be delivered to that receiver
	private final Hashtable messagesByReceiver = new Hashtable(); 
	// The messages to be delivered organized as a round list of the Boxes of
	// messages for the currently addressed receivers 
	private final RoundList messagesByOrder = new RoundList();
	
	/**
	 * Add a message to the tail of the Box of messages for the indicated 
	 * receiver. If a Box for the indicated receiver is not yet present, a 
	 * new one is created.
	 * This method is executed by an agent's thread requesting to deliver 
	 * a new message.
	 */
	synchronized void addLast(AID receiverID, ACLMessage msg){
		Box b = (Box) messagesByReceiver.get(receiverID);
		if (b == null){
			// There is no Box of messages for this receiver yet. Create a new one 
			b = new Box(receiverID);
			messagesByReceiver.put(receiverID, b);
			messagesByOrder.add(b);
		}
		b.addLast(msg);
		//log("Message added", 2);
		// Wakes up all deliverers
		notifyAll();
	}

	/**
	 * Add a message to the head of the Box of messages for the indicated 
	 * receiver.
	 * This method is executed by the TimerDispatcher Thread when a 
	 * retransmission timer expires. Therefore a Box of messages for the
	 * indicated receiver must already exist. Moreover the busy flag of 
	 * this Box must be reset to allow deliverers to handle messages in it
   */
	synchronized void addFirst( AID receiverID, ACLMessage msg ){
		Box b = (Box) messagesByReceiver.get(receiverID);
		b.addFirst(msg);
		b.setBusy(false);
		// Wakes up all deliverers
		notifyAll();
	}
	


	/**
	 * Get the first message for the first idle (i.e. not busy) receiver.
	 * This is executed by a Deliverer thread just before delivering 
	 * a message.
	 */
	synchronized PendingMsg get(){
		Box b = null;
		// Wait until an idle (i.e. not busy) receiver is found
		while( (b = getNextIdle()) == null ){
			try{
				//log("Go to sleep", 2);
				wait();
				//log("Wake up", 2);
			}
			catch (InterruptedException ie) {
				// Just do nothing
			}
		}
		PendingMsg pm = new PendingMsg(b.removeFirst(), b.getReceiver(), -1);
	 	return pm;
	}
	
	

	/**
	 * Get the Box of messages for the first idle (i.e. not busy) receiver.
	 * @return null if all receivers are currently busy
	 * This method does not need to be synchronized as it is only executed
	 * inside a synchronized block.
   */
	private Box getNextIdle(){
		
		//log("Searching for an idle receiver. Current size is "+messagesByOrder.size(),2);
		for (int i = 0; i < messagesByOrder.size(); ++i) {
			Box b = (Box) messagesByOrder.get();
			if (!b.isBusy()) {
				b.setBusy(true);
				//log("Idele receiver found: "+b.getReceiver().getName(),2);
				return b;
			}
		}
		return null;	
	}
	
	/**
	 * A message for the receiver receiverID has been successfully delivered
	 * If the Box of messages for that receiver is now empty --> remove it.
	 * Otherwise just mark it as idel (not busy).
   */
	synchronized void handleDelivered( AID receiverID ){
		Box b = (Box) messagesByReceiver.get(receiverID);
		if (b.isEmpty()) {
			messagesByReceiver.remove(receiverID);
			messagesByOrder.remove(b);
			//log("Removed entry for receiver "+receiverId.getName(), 2);
		}
		else {
			b.setBusy(false);
		}
	}
	

	/**
	 * This class represents a Box of messages to be delivered to 
	 * a single receiver
	 */
	private class Box {
		private final AID receiver;
		private boolean busy;
		private final List messages;
		
		public Box(AID r){
			receiver = r;
			busy = false;
			messages = new LinkedList(); 
		}
		
		private AID getReceiver() {
			return receiver;
		}
		
		private void setBusy(boolean b){
			busy = b;
		}
		
		private boolean isBusy(){
			return busy;
		}
		
		private void addLast(ACLMessage msg) {
			messages.add(msg);
		}
		
		private void addFirst(ACLMessage msg) {
			messages.add(0, msg);
		}
		
		private ACLMessage removeFirst() {
			return (ACLMessage) messages.remove(0);
		}
		
		private boolean isEmpty() {
			return messages.isEmpty();
		}	
	}

	private void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
			//String name = Thread.currentThread().getName();
      System.out.println("MessageManager("+name+"): "+s);
    } 
  } 	
  
}
