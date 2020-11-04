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
 * Object to maintain message to send and
 * to preserve the order for sending.
 * 
 * @author Elisabetta Cortese - TILAB
 */

class OutBox {
	private static final int PENDING_MSG_PER_RECEIVER_THR = -1;
	
	private int size = 0; // Approximated size in bytes
	private int pendingCnt = 0;   
	private int warningSize; 
	private int maxSize;
	private int sleepTimeFactor;
	private boolean enableMultipleDelivery;
	private boolean overWarningSize = false;
	
	private MessageManager manager;
	
	private long lastDiscardedLogTime = -1;
	private long discardedSinceLastLogCnt = 0;
	private long servedSinceLastDiscardedLogCnt = -1;

	// The messages to be delivered organized as an hashtable that maps
	// a receiver AID into the Box of messages to be delivered to that receiver
	private final Map messagesByReceiver = new HashMap(); 
	// The messages to be delivered organized as a round list of the Boxes of
	// messages for the currently addressed receivers 
	private final RoundList messagesByOrder = new RoundList();

	private Logger myLogger;

	OutBox(int warningSize, int maxSize, int sleepTimeFactor, boolean enableMultipleDelivery, MessageManager manager) {
		this.warningSize = warningSize;
		this.maxSize = maxSize;
		this.sleepTimeFactor = sleepTimeFactor;
		this.enableMultipleDelivery = enableMultipleDelivery;
		this.manager = manager;
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
		// Check the max queue size threshold
		if ((size + msg.length()) > maxSize) {
			long time = System.currentTimeMillis();
			// Avoid printing more than 1 log per sec
			synchronized (this) {
				if ((time - lastDiscardedLogTime) > 1000) {
					String servedWhileDiscardingStr = servedSinceLastDiscardedLogCnt >= 0 ? " ("+servedSinceLastDiscardedLogCnt+" messages served since last discard-log)" : "";
					myLogger.log(Logger.SEVERE, String.valueOf(discardedSinceLastLogCnt+1)+" message(s) discarded by MessageManager! Current-queue-size = "+size+", max-size = "+maxSize+", number of pending messages = "+pendingCnt+", size of last message = "+msg.length()+servedWhileDiscardingStr);
					lastDiscardedLogTime = time;
					discardedSinceLastLogCnt = 0;
					servedSinceLastDiscardedLogCnt = 0;
				}
				else {
					discardedSinceLastLogCnt++;
				}
			}
			throw new QueueFullException();
		}
		
		boolean logActivated = myLogger.isLoggable(Logger.FINER);
		if (logActivated)
			myLogger.log(Logger.FINER,"Entering addLast for receiver "+receiverID.getName());
		if (msg.getPayload() != null && msg.isModifiable()) {
			ACLMessage acl = msg.getACLMessage();
			if (acl != null) {
				acl.setContent(null);
			}
		}

		// This must fall outside the synchronized block because the method may call Thread.sleep
		int length = msg.length();
		increaseSize(length);

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
						
			if (b.size() > PENDING_MSG_PER_RECEIVER_THR) {
				// To many messages for a single receiver. Be sure the owner of this box is not stuck
				String owner = b.getOwner();
				if (manager.isStuck(owner)) {
					decreaseSize(length);
					throw new StuckDeliverer(owner);
				}
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
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Deliverer "+Thread.currentThread()+" go to sleep...");
				}
				wait();
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Deliverer "+Thread.currentThread()+" wake up");
				}
			}
			catch (InterruptedException ie) {
				// Just do nothing
			}
		}
		PendingMsg pm = b.removeFirst();
		int s = pm.getMessage().length();
		decreaseSize(s);
		//#J2ME_EXCLUDE_BEGIN
		// Multiple-delivery
		// Activate it if enabled and either there are at least 3 pending messages for the same receiver or we are over the warning size
		if (b.size() >= 3 || size > warningSize && enableMultipleDelivery) {
			int mulMessageSize = s;
			java.util.List<GenericMessage> mm = null;
			while (!b.isEmpty() && (mulMessageSize < 100000)) { // Max 100 Kbyte
				if (mm == null) {
					mm = new java.util.ArrayList<GenericMessage>();
					mm.add(pm.getMessage());
				}
				PendingMsg next = b.removeFirst();
				GenericMessage g = next.getMessage();
				s = g.length();
				decreaseSize(s);
				mulMessageSize += s;
				mm.add(g);
			}
			if (mm != null) {
				MultipleGenericMessage mgm = new MultipleGenericMessage(mulMessageSize);
				mgm.setMessages(mm);
				pm.setMessage(mgm);
			}
		}
		//#J2ME_EXCLUDE_END
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
	 * Some messages for the receiver receiverID have been served
	 * If the Box of messages for that receiver is now empty --> remove it.
	 * Otherwise just mark it as idle (not busy).
	 */
	synchronized final void handleServed(AID receiverID, int n) {
		if (servedSinceLastDiscardedLogCnt >= 0) {
			servedSinceLastDiscardedLogCnt += n;
		}
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

	private void increaseSize(int lastMessageSize) {
		long sleepTime = 0;
		synchronized (this) {
			pendingCnt++;
			size += lastMessageSize;
			if (size > warningSize) {
				if (!overWarningSize) {
					myLogger.log(Logger.WARNING, "MessageManager queue size ("+size+") > "+warningSize+". Number of pending messages = "+pendingCnt+", size of last message = "+lastMessageSize);
					overWarningSize = true;
				}
				if (sleepTimeFactor > 0) {
					sleepTime = (1 + ((size - warningSize) / 1000000)) * sleepTimeFactor;
				}
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
		pendingCnt--;
		size -= k;
		if (size < warningSize) {
			if (overWarningSize) {
				myLogger.log(Logger.INFO, "MessageManager queue size < "+warningSize);
				overWarningSize = false;
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
		private String owner;
		private final List messages;

		public Box(AID r) {
			receiver = r;
			busy = false;
			messages = new LinkedList(); 
		}

		private AID getReceiver() {
			return receiver;
		}

		private void setBusy(boolean b){
			busy = b;
			//#J2ME_EXCLUDE_BEGIN
			owner = (busy ? Thread.currentThread().getName() : null);
			//#J2ME_EXCLUDE_END
		}

		private boolean isBusy(){
			return busy;
		}
		
		private String getOwner() {
			return owner;
		}

		private void addLast(PendingMsg pm) {
			messages.add(pm);
		}

		private PendingMsg removeFirst() {
			return (PendingMsg) messages.remove(0);
		}

		private boolean isEmpty() {
			return messages.isEmpty();
		}	
		
		private int size() {
			return messages.size();
		}

		// For debugging purpose
		public String toString() {
			return "("+receiver.getName()+" :busy "+busy+ (owner != null ? " :owner "+owner : "") + " :message-cnt "+messages.size()+")";
		}
	} // END of inner class Box


	// For debugging purpose
	synchronized String[] getStatus() {
		Object[] boxes = messagesByOrder.toArray();
		String[] status = new String[boxes.length];
		for (int i = 0; i < boxes.length; ++i) {
			status[i] = boxes[i].toString();
		}
		return status;
	}	

	// For debugging purpose 
	int getSize() {
		return size;
	}

	int getPendingCnt() {
		return pendingCnt;
	}
}
