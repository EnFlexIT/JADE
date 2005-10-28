package jade.core.messaging;

//import java.util.Hashtable;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.RoundList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.core.AID;
import jade.core.messaging.MessageManager.PendingMsg;
import jade.core.messaging.MessageManager.Channel;
import jade.lang.acl.ACLMessage;

import jade.util.Logger;

/**
 * Object to mantain message to send and
 * to preserve the order for sending.
 * 
 * @author Elisabetta Cortese - TILAB
 */

class OutBox {
	private int size = 0;
	private int maxSize; 
	private boolean overMaxSize = false;
	
	// The massages to be delivered organized as an hashtable that maps
	// a receiver AID into the Box of messages to be delivered to that receiver
	private final Map messagesByReceiver = new HashMap(); 
	// The messages to be delivered organized as a round list of the Boxes of
	// messages for the currently addressed receivers 
	private final RoundList messagesByOrder = new RoundList();
	
	private Logger myLogger;
	
	OutBox(int s) {
		maxSize = s;
		myLogger = Logger.getMyLogger(getClass().getName());
	}
	
	/**
	 * Add a message to the tail of the Box of messages for the indicated 
	 * receiver. If a Box for the indicated receiver is not yet present, a 
	 * new one is created.
	 * This method is executed by an agent's thread requesting to deliver 
	 * a new message.
	 */
	void addLast(AID receiverID, GenericMessage msg, Channel ch) {
		boolean logActivated = myLogger.isLoggable(Logger.FINER);
		if (logActivated)
			myLogger.log(Logger.FINER,"Entering addLast for receiver "+receiverID.getName());
		if (msg.getPayload() != null) {
			ACLMessage acl = msg.getACLMessage();
			if (acl != null) {
				acl.setContent(null);
			}
		}
		
		// This must fall outside the synchronized block because the method calls Thread.sleep
		increaseSize(msg.length());
		
		synchronized (this) {
			Box b = (Box) messagesByReceiver.get(receiverID);
			if (logActivated) {
				String msgDebug = (b==null)? "No box for receiver "+receiverID.getName():"Box for receiver "+receiverID.getName()+" busy ?  "+b.isBusy();
				myLogger.log(Logger.FINER,msgDebug);
			}
			if (b == null){
				// There is no Box of messages for this receiver yet. Create a new one 
				b = new Box(receiverID);
				messagesByReceiver.put(receiverID, b);
				messagesByOrder.add(b);
				if (logActivated)
					myLogger.log(Logger.FINER,"Box created for receiver "+receiverID.getName());
			}
			if (logActivated)
				myLogger.log(Logger.FINER,"Message entered in box for receiver "+receiverID.getName());
			b.addLast(new PendingMsg(msg, receiverID, ch, -1));
			// Wakes up all deliverers
			notifyAll();
		}
		if (logActivated)
			myLogger.log(Logger.FINER,"Exiting addLast for receiver "+receiverID.getName());
	}

	/**
	 * Add a message to the head of the Box of messages for the indicated 
	 * receiver.
	 * This method is executed by the TimerDispatcher Thread when a 
	 * retransmission timer expires. Therefore a Box of messages for the
	 * indicated receiver must already exist. Moreover the busy flag of 
	 * this Box must be reset to allow deliverers to handle messages in it
   *
	synchronized void addFirst(PendingMsg pm){
		Box b = (Box) messagesByReceiver.get(pm.getReceiver());
		b.addFirst(pm);
		b.setBusy(false);
		// Wakes up all deliverers
		notifyAll();
	}*/
	


	/**
	 * Get the first message for the first idle (i.e. not busy) receiver.
	 * This is executed by a Deliverer thread just before delivering 
	 * a message.
	 */
	synchronized final PendingMsg get(){
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
	 	PendingMsg pm = b.removeFirst();
	 	decreaseSize(pm.getMessage().length());
	 	return pm;
	}
	
	

	/**
	 * Get the Box of messages for the first idle (i.e. not busy) receiver.
	 * @return null if all receivers are currently busy
	 * This method does not need to be synchronized as it is only executed
	 * inside a synchronized block.
   */
	private final Box getNextIdle(){
		for (int i = 0; i < messagesByOrder.size(); ++i) {
			Box b = (Box) messagesByOrder.get();
			if (!b.isBusy()) {
				b.setBusy(true);
				if( myLogger.isLoggable(Logger.FINER) )
					myLogger.log(Logger.FINER,"Setting box busy for receiver "+b.getReceiver().getName());
				return b;
			}
		}
		return null;	
	}
	
	/**
	 * A message for the receiver receiverID has been served
	 * If the Box of messages for that receiver is now empty --> remove it.
	 * Otherwise just mark it as idel (not busy).
   */
	synchronized final void handleServed( AID receiverID ){
		boolean logActivated = myLogger.isLoggable(Logger.FINER);
		if (logActivated)
			myLogger.log(Logger.FINER,"Entering handleServed for "+receiverID.getName());
		Box b = (Box) messagesByReceiver.get(receiverID);
		if (b.isEmpty()) {
			messagesByReceiver.remove(receiverID);
			messagesByOrder.remove(b);
			if (logActivated)
				myLogger.log(Logger.FINER,"Removed box for receiver "+receiverID.getName());
		}
		else {
			b.setBusy(false);
			if (logActivated)
				myLogger.log(Logger.FINER,"Freeing box for receiver "+receiverID.getName());
		}
		if (logActivated)
			myLogger.log(Logger.FINER,"Exiting handleServed for "+receiverID.getName());
	}

	private void increaseSize(int k) {
		long sleepTime = 0;
		synchronized (this) {
			size += k;
			if (size > maxSize) {
				if (!overMaxSize) {
					myLogger.log(Logger.WARNING, "MessageManager queue size > "+maxSize);
					overMaxSize = true;
				}
				sleepTime = (1 + ((size - maxSize) / 1000000)) * 100;
			}
		}
			if (sleepTime > 0) {
				try { // delay a bit this Thread because the queue is becoming too big
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException ie) {}
			}
		}

	/**
	 * The method decreases the value of size and, eventually,
	 * set to false the value of overMaxSize.
	 * <p>
	 * This method should have been declared synchronized.
	 * For possible better peformance, it is not declared synchronized because 
	 * it is a private method and it is called only
	 * by the method get() which is already synchronized.
	 * @param k the value by which size must be decremented
	 */
	private void decreaseSize(int k) {
		size -= k;
		if (size < maxSize) {
			if (overMaxSize) {
				myLogger.log(Logger.INFO, "MessageManager queue size < "+maxSize);
				overMaxSize = false;
			}
		}
	}
	
	/**
	 * This class represents a Box of messages to be delivered to 
	 * a single receiver
	 */
	private class Box {
	    private final AID receiver;
	    private boolean busy;
	    //private MessageManager.Channel channel;  
	    private final List messages;
		
		public Box(AID r) {
			receiver = r;
			//channel = ch;
			busy = false;
			messages = new LinkedList(); 
		}
		
		private AID getReceiver() {
			return receiver;
		}

	  /*      private MessageManager.Channel getChannel() {
		    return channel;
		}*/
		
		private void setBusy(boolean b){
			busy = b;
		}
		
		private boolean isBusy(){
			return busy;
		}
		
		private void addLast(PendingMsg pm) {
			messages.add(pm);
		}
		/*private void addLast(ACLMessage msg) {
			messages.add(msg);
		}*/
		
		/*private void addFirst(PendingMsg pm) {
			messages.add(0, pm);
		}*/
		/*private void addFirst(ACLMessage msg) {
			messages.add(0, msg);
		}*/
		
		private PendingMsg removeFirst() {
			return (PendingMsg) messages.remove(0);
		}
		/*private ACLMessage removeFirst() {
			return (ACLMessage) messages.remove(0);
		}*/
		
		private boolean isEmpty() {
			return messages.isEmpty();
		}	
	} // END of inner class Box
}
