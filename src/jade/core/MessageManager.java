/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.core;

import jade.util.leap.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.InternalError;

import java.util.Date;
import java.util.Hashtable;

/**
 * @author  Giovanni Caire - TILAB
 */
class MessageManager implements TimerListener {
	// Default values for retry-interval and retry-maximum
	//private static final int  DEFAULT_POOL_SIZE = 1;
	private static final long  DEFAULT_RETRY_INTERVAL = 60000; // 1 minute
	private static final long  DEFAULT_RETRY_MAXIMUM = 300000; // 5 minutes
	//private int poolSize = DEFAULT_POOL_SIZE;
	private long retryInterval = DEFAULT_RETRY_INTERVAL;
	private long retryMaximum = DEFAULT_RETRY_MAXIMUM;
	
	private AgentContainerImpl myContainer;
	private List         errBox = new ArrayList();
	private List         outBox = new LinkedList();
	//private HashMap      receivers = new HashMap();
	//private Hashtable    waiting = new Hashtable();
	
	private int verbosity = 2;
	
	public MessageManager() {
	}
	
	public void initialize(Profile p, AgentContainerImpl ac) {
		myContainer = ac;
		
		try {
			/* POOL_SIZE
			String tmp = p.getParameter("jade.core.MessageManager.pool-size");
			try {
				poolSize = Integer.parseInt(tmp);
			}
			catch (Exception e) {
				// Do nothing and keep default value
			}*/
			
			// RETRY_INTERVAL
			String tmp = p.getParameter("jade.core.MessageManager.retry-interval");
			try {
				retryInterval = Long.parseLong(tmp);
			}
			catch (Exception e) {
				// Do nothing and keep default value
			}
			
			// RETRY_MAXIMUM
			tmp = p.getParameter("jade.core.MessageManager.retry-maximum");
			try {
				retryMaximum = Long.parseLong(tmp);
			}
			catch (Exception e) {
				// Do nothing and keep default value
			}
		}
		catch (ProfileException pe) {
			// Print a warning and keep default values
			System.out.println("Error reading MessageManager configuration. Keep default values");
		}
		
		try {
			ResourceManager rm = p.getResourceManager();
			//for (int i = 0; i < poolSize; ++i) {
				//Thread t = rm.getThread(ResourceManager.TIME_CRITICAL, "Deliverer-"+i, new Deliverer());
				Thread t = rm.getThread(ResourceManager.TIME_CRITICAL, "Deliverer", new Deliverer());
				t.start();
			//}
		}
		catch (ProfileException pe) {
			throw new RuntimeException("Can't get ResourceManager. "+pe.getMessage());
		}
	}
			
	/**
	   Activate the asynchronuos delivery of an ACLMessage
	 */
	public void deliver(ACLMessage msg, AID receiverID) {
		//enqueue(new PendingMsg(msg, receiverID, -1));
		putInOutBox(new PendingMsg(msg, receiverID, -1));
	}
	
	/*private void enqueue(PendingMsg pm) {
		ACLMessage msg = pm.getMessage();
		AID receiverID = pm.getReceiver();
		
		synchronized (receivers) {
			if (!receivers.containsKey(receiverID)) {
				receivers.put(receiverID, null);
				putInOutBox(pm);
			}
			else {
				Deliverer d = null;
				while ((d = (Deliverer) receivers.get(receiverID)) == null) {
					try {
						waiting.put(Thread.currentThread(), receiverID);
						//log("before wait");
						receivers.wait();
						//log("after wait");
					}
					catch (InterruptedException ie) {
					}
				}
				d.enqueue(msg);
				//log("Message "+stringify(msg, receiverID)+" enqueued to active deliverer");
			}
		}
	}*/
	
	private void putInOutBox(PendingMsg pm) {
		synchronized (outBox) {
			outBox.add(pm);
			//log("Message "+stringify(pm.getMessage(), pm.getReceiver())+" inserted in OutBox "+outBox.size());
			// Wake up one delivering thread. Don't want to wake up all of them
			// to avoid unnecessary task-switches
			//outBox.notify();
			outBox.notifyAll();
		}
	}
	
	private PendingMsg getFromOutBox() {
		synchronized (outBox) {
		    //log("getFromOutBox. size="+outBox.size());
			while (outBox.isEmpty()) {
				try {
				    //log("before wait");
					outBox.wait();
					//log("after wait");
				}
				catch (InterruptedException ie) {
				}
			}
			return (PendingMsg) outBox.remove(0);
		}
	}
	
 	/**
 	   Inner class Deliverer
 	 */
 	class Deliverer implements Runnable {
 		//private List queued = new LinkedList();
 		
 		public void run() {
 			//log("Started");
 			while (true) {
 				// Get the next message to be delivered
 				PendingMsg pm = getFromOutBox();
 				ACLMessage msg = pm.getMessage();
 				AID receiverID = pm.getReceiver();
 				/*synchronized (receivers) {
 					receivers.put(receiverID, this);
 					receivers.notifyAll();
 				}*/
 				
	 			//log("Serving message "+stringify(msg, receiverID));
 				//while (msg != null) {
    			try {
    				myContainer.deliverNow(msg, receiverID);
    				//log("Message served");
    			}
    			catch (UnreachableException ue) {
    				// The receiver is currently not reachable --> retry later
 						log("Destination unreachable. Will retry later", 1);
    				deliverLater(pm);
    			}
    			
    			/*synchronized (this) {
	    			while (waiting.contains(receiverID)) {
	    				try {
	    					wait();
	    				}
	    				catch (InterruptedException ie) {
	    				}
	    			}
    			}
    			
    			// Check if there are other messages for the same receiver
    			synchronized (receivers) {
    				if (queued.isEmpty()) {
    					msg = null;
    					receivers.remove(receiverID);
    				}
    				else {
    					//log("Serving enqueued message "+stringify(msg, receiverID));
    					msg = (ACLMessage) queued.remove(0);
    				}
    			}*/
 				//}
 			}
 		}
 		
 		/*void enqueue(ACLMessage msg) {
 			synchronized (this) {
	 			queued.add(msg);
	 			waiting.remove(Thread.currentThread());
	 			notifyAll();
 			}
 		}*/
 	}
 	
	/**
	   Inner class PendingMsg
	 */
	private class PendingMsg {
		private ACLMessage msg;
		private AID receiverID;
		private long deadline;
		
		public PendingMsg(ACLMessage msg, AID receiverID, long deadline) {
			this.msg = msg;
			this.receiverID = receiverID;
			this.deadline = deadline;
		}
		
		public ACLMessage getMessage() {
			return msg;
		}
		
		public AID getReceiver() {
			return receiverID;
		}
		
		public long getDeadline() {
			return deadline;
		}
		
		public void setDeadline(long deadline) {
			this.deadline = deadline;
		}
	}
	
 	/////////////////////////////////////////////////////////
 	// Methods dealing with buffering and retransmission when
	// the destination is temporarily unreachable
 	/////////////////////////////////////////////////////////
	private void deliverLater(PendingMsg pm) {
		synchronized (errBox) {
			// If the errBox is not empty --> a proper Timer is already active.
			if (errBox.isEmpty()) {
				activateTimer();
			}
			
			// Set the deadline unless already set
			if (pm.getDeadline() < 0) {
				long current = System.currentTimeMillis();
				long deadline = current + retryMaximum;
				// If the reply_by field is set in the message and this is less 
				// than retryMaximum --> set the deadline to the reply_by field
				Date d = pm.getMessage().getReplyByDate();
				if (d != null) {
					long rb = d.getTime();
					if (rb < deadline) {
						deadline = rb;
					}
				}
				pm.setDeadline(deadline);
			}
	
			errBox.add(pm);
		}
	}
		
	/**
	   Put all messages in the errBox back in the outBox (unless their
	   deadline has already expired).
	 */
	public void doTimeOut(Timer t) {
		synchronized (errBox) {
			log("Retry Timer expired. Handle "+errBox.size()+" messages", 1);
			Iterator it = errBox.iterator();
			while (it.hasNext()) {
				PendingMsg pm = (PendingMsg) it.next();
			
				if (System.currentTimeMillis() > pm.getDeadline()) {
					// If the deadline has expired, don't even try again
      		myContainer.notifyFailureToSender(pm.getMessage(), new InternalError("\"Agent unreachable\""));
				}
				else {
					// Otherwise schedule again the message for delivery
					//enqueue(pm);
					putInOutBox(pm);
				}
			}
			errBox.clear();
		}
	}
		
	private void activateTimer() {
		Timer t = new Timer(System.currentTimeMillis() + retryInterval, this);
		Runtime.instance().getTimerDispatcher().add(t);
		// DEBUG
		log("Timer activated. Period is "+retryInterval, 1);
 	}
	
 	//////////////////////
 	// LOG methods
 	//////////////////////
  /**
   */
  private void log(String s) {
    log(s, 2);
  } 

  /**
   */
  private void log(String s, int level) {
    if (verbosity >= level) {
      String name = Thread.currentThread().toString();
      System.out.println("MessageManager("+name+"): "+s);
    } 
  } 	
  
  private String stringify(ACLMessage msg, AID receiverID) {
  	return new String(ACLMessage.getPerformative(msg.getPerformative())+" to "+receiverID.getName()+" ("+msg.getConversationId()+")");
  }
}

