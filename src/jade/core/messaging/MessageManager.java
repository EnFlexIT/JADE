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

package jade.core.messaging;

import jade.util.leap.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.InternalError;

import jade.core.AID;
import jade.core.AgentContainerImpl;
import jade.core.ResourceManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Runtime;
import jade.core.Timer;
import jade.core.TimerListener;
import jade.core.UnreachableException;

import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This class manages the delivery of ACLMessages to remote destinations
 * in an asynchronous way.
 * If network problems prevent the delivery of a message, this class also 
 * embeds a mechanism to buffer the message and periodically retry to 
 * deliver it.
 * @author  Giovanni Caire - TILAB
 * @author  Elisabetta Cortese - TILAB
 * @author  Fabio Bellifemine - TILAB
 */
class MessageManager implements TimerListener {

    public interface Channel {
	void deliverNow(ACLMessage msg, AID receiverID) throws UnreachableException;
	void notifyFailureToSender(ACLMessage msg, AID receiver, InternalError ie);
    }


	// Default values for retry-interval and retry-maximum
	private static final long  DEFAULT_RETRY_INTERVAL = 20000; // 20 sec
	private static final long  DEFAULT_RETRY_MAXIMUM = 300000; // 5 minutes

    // A shared instance to have a single thread pool
    private static MessageManager theInstance; // FIXME: Maybe a table, indexed by a profile subset, would be better?

	private long retryInterval = DEFAULT_RETRY_INTERVAL;
	private long retryMaximum = DEFAULT_RETRY_MAXIMUM;

//#MIDP_EXCLUDE_BEGIN
	private static final int  DEFAULT_POOL_SIZE = 5;
	private static final int  MAX_POOL_SIZE = 100;
	private int poolSize = DEFAULT_POOL_SIZE;

	private OutBox outBox = new OutBox();
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
	private List         outBox = new LinkedList();
#MIDP_INCLUDE_END*/    
	private List   errBox = new ArrayList();

	private int verbosity = 2;


	private MessageManager() {
	}

    public static synchronized MessageManager instance(Profile p) {
	if(theInstance == null) {
	    theInstance = new MessageManager();
	    theInstance.initialize(p);
	}

	return theInstance;
    }

	public void initialize(Profile p) {

		String tmp = null;
		// RETRY_INTERVAL
		try {
			tmp = p.getParameter("jade_core_MessageManager_retry-interval", null);
			retryInterval = Long.parseLong(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
			
		// RETRY_MAXIMUM
		try {
			tmp = p.getParameter("jade_core_MessageManager_retry-maximum", null);
			retryMaximum = Long.parseLong(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
		
//#MIDP_EXCLUDE_BEGIN
		// POOL_SIZE
		try {
			tmp = p.getParameter("jade_core_MessageManager_pool-size", null);
			poolSize = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
			
		try {
			ResourceManager rm = p.getResourceManager();
			for (int i = 0; i < poolSize; ++i) {
				Thread t = rm.getThread(ResourceManager.TIME_CRITICAL, "Deliverer-"+i, new Deliverer());
				//log("[Initialize] Thread created: "+t.getName());
				t.start();
			}
		}
		catch (ProfileException pe) {
			throw new RuntimeException("Can't get ResourceManager. "+pe.getMessage());
		}
//#MIDP_EXCLUDE_END
		
/*#MIDP_INCLUDE_BEGIN
		try {
			ResourceManager rm = p.getResourceManager();
			Thread t = rm.getThread(ResourceManager.TIME_CRITICAL, "Deliverer", new Deliverer());
			t.start();
		}
		catch (ProfileException pe) {
			throw new RuntimeException("Can't get ResourceManager. "+pe.getMessage());
		}
#MIDP_INCLUDE_END*/    
	}
			
	/**
	   Activate the asynchronuos delivery of an ACLMessage
   */
	public void deliver(ACLMessage msg, AID receiverID, Channel ch) {
//#MIDP_EXCLUDE_BEGIN
		outBox.addLast(receiverID, msg, ch);
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
		putInOutBox(new PendingMsg(msg, receiverID, ch, -1));
#MIDP_INCLUDE_END*/    
	}
	

/*#MIDP_INCLUDE_BEGIN
	private void putInOutBox(PendingMsg pm) {
		synchronized (outBox) {
			outBox.add(pm);
			outBox.notifyAll();
		}
	}
	
	private PendingMsg getFromOutBox() {
		synchronized (outBox) {
			while (outBox.isEmpty()) {
				try {
					outBox.wait();
				}
				catch (InterruptedException ie) {
				}
			}
			return (PendingMsg) outBox.remove(0);
		}
	}
#MIDP_INCLUDE_END*/    
	
 	/**
 	   Inner class Deliverer
 	 */
 	class Deliverer implements Runnable {

 		public void run() {
 			while (true) {
 				// Get a message from the OutBox
//#MIDP_EXCLUDE_BEGIN
 				PendingMsg pm = outBox.get();
 				ACLMessage msg = pm.getMessage();
 				AID receiverID = pm.getReceiver();
				//log("Serving message for "+receiverID.getName());
	    	try {
	    		// Deliver the message
		        Channel ch = pm.getChannel();
		    	ch.deliverNow(msg, receiverID);
					//log("Message served");
	    		outBox.handleDelivered(receiverID);	
	    	}
	    	catch (UnreachableException ue) {
	    		// If delivery fails insert the message in the ErrBox. It will
	    		// be delivered at a later time.
	    		deliverLater(pm);
	    	}
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
 				// Get the next message to be delivered
 				PendingMsg pm = getFromOutBox();
 				ACLMessage msg = pm.getMessage();
 				AID receiverID = pm.getReceiver();
				Channel ch = pm.getChannel();
 				if (checkPostpone(receiverID, msg.getSender())) {
 					log("Postpone delivery to preserve order", 1);
 					deliverLater(pm);
 				}
 				else {
    			try {
    				ch.deliverNow(msg, receiverID);
    				//log("Message served");
    			}
    			catch (UnreachableException ue) {
    				// The receiver is currently not reachable --> retry later
 						log("Destination unreachable. Will retry later", 1);
    				deliverLater(pm);
    			}
 				}
#MIDP_INCLUDE_END*/    
	 		}
 		}
 	
 	} 	
 	
	/**
	   Inner class PendingMsg
	 */
	public static class PendingMsg {
	    private final ACLMessage msg;
	    private final AID receiverID;
	    private final Channel channel;
	    private long deadline;
		
	    public PendingMsg(ACLMessage msg, AID receiverID, Channel channel, long deadline) {
		this.msg = msg;
		this.receiverID = receiverID;
		this.channel = channel;
		this.deadline = deadline;
	    }

	    public ACLMessage getMessage() {
		return msg;
	    }
		
	    public AID getReceiver() {
		return receiverID;
	    }

	    public Channel getChannel() {
		return channel;
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
/*#MIDP_INCLUDE_BEGIN
	private boolean checkPostpone(AID receiverID, AID senderID) {
		synchronized (errBox) {
			Iterator it = errBox.iterator();
			while (it.hasNext()) {
				PendingMsg pm = (PendingMsg) it.next();
				if (receiverID.equals(pm.getReceiver())) {
					if (senderID.equals(pm.getMessage().getSender())) {
						return true;
					}
				}
			}
			return false;
		}
	}
#MIDP_INCLUDE_END*/    
	
	private void deliverLater(PendingMsg pm) {
		// Mutual exclusion with doTimeOut()
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
	   Put all messages in the errBox back in the OutBox (unless their
	   deadline has already expired).
	   Note that, due to the "receiver busy" mechanism, there can't be  
	   two or more messages for the same receiver in the errBox. 
	*/
	public void doTimeOut(Timer t) {
		// Mutual exclusion with deliverLater()
		synchronized (errBox) {
			log("Retry Timer expired. Handle "+errBox.size()+" messages", 1);
			Iterator it = errBox.iterator();
			while (it.hasNext()) {
				PendingMsg pm = (PendingMsg) it.next();
				if (System.currentTimeMillis() > pm.getDeadline()) {
				    // If the deadline has expired, don't even try again
				    Channel ch = pm.getChannel();
				    ch.notifyFailureToSender(pm.getMessage(), pm.getReceiver(), new InternalError("\"Agent unreachable\""));
				}
				else {
					// Otherwise schedule again the message for delivery
//#MIDP_EXCLUDE_BEGIN
					outBox.addFirst(pm.getReceiver(), pm.getMessage());
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
					putInOutBox(pm);
#MIDP_INCLUDE_END*/    
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
  
  /**
   */
  private String stringify(ACLMessage msg, AID receiverID) {
  	return new String(ACLMessage.getPerformative(msg.getPerformative())+" to "+receiverID.getName()+" ("+msg.getConversationId()+")");
  }
}

