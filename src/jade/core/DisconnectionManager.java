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

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.InternalError;

import java.util.Date;

/**
 * This class handles messages that cannot be dispatched because
 * of disconnection problems (of the source or of the desctination) and
 * periodically try to re-send them for a given amount of time.
 * 
 * @author  Giovanni Caire - TILAB
 */
class DisconnectionManager implements TimerListener {
	// Message handling in disconnected state modes
	private static final int   NONE = 0;
	private static final int   EXCEPTION = 1;
	private static final int   BUFFER = 2;
	
	// Default values for retry-interval and retry-maximum
	private static final long  DEFAULT_RETRY_INTERVAL = 60000; // 1 minute
	private static final long  DEFAULT_RETRY_MAXIMUM = 600000; // 10 minutes
	
	private AgentContainerImpl myContainer;
	private int                mode = NONE; 
	private ArrayList          pendings;
	private long               retryInterval = DEFAULT_RETRY_INTERVAL;
	private long               retryMaximum = DEFAULT_RETRY_MAXIMUM;
	
	public DisconnectionManager() {
	}
	
	public void initialize(Profile p, AgentContainerImpl ac) {
		myContainer = ac;
		
		try {
			// MODE
			String tmp = p.getParameter("jade.core.DisconnectionManager.mode");
			if (tmp != null) {
				if (CaseInsensitiveString.equalsIgnoreCase(tmp, "EXCEPTION")) {
					mode = EXCEPTION;
				}
				else if (CaseInsensitiveString.equalsIgnoreCase(tmp, "BUFFER")) {
					mode = BUFFER;
					// DEBUG
					System.out.println("DM: mode set to BUFFER");
					pendings = new ArrayList();
				}
			}
			
			// RETRY_INTERVAL
			tmp = p.getParameter("jade.core.DisconnectionManager.retry_interval");
			try {
				retryInterval = Long.parseLong(tmp);
			}
			catch (Exception e1) {
				// Do nothing and keep default value
			}
			
			// RETRY_MAXIMUM
			tmp = p.getParameter("jade.core.DisconnectionManager.retry_maximum");
			try {
				retryMaximum = Long.parseLong(tmp);
			}
			catch (Exception e2) {
				// Do nothing and keep default value
			}
		}
		catch (ProfileException pe) {
			// Print a warning and keep default values
			System.out.println("Error reading DM configuration. Keep default values");
		}
	}
			
	/**
	   Handle a message that cannot be delivered now because of 
	   disconnection problems according to the specified operation mode
	 */
	public void deliverLater(ACLMessage msg, AID receiverID) {
		// DEBUG
		System.out.println("DM: handling message "+msg);
		switch (mode) {
		case BUFFER:	
	    // Mutual exclusion with doTimeOut() that is executed inside the 
	    // TimerDispatcher thread (both methods modify the pool of pending
	    // messages)
			synchronized (this) { 
				// If the pool of messages to be dispatched is not empty -->
				// a proper Timer is already active.
				if (pendings.isEmpty()) {
					activateTimer();
				}
			
				long current = System.currentTimeMillis();
				long deadline = current + retryMaximum;
				// If the reply_by field is set in the message and this is less 
				// than retryMaximum --> set the deadline to the reply_by field
				Date d = msg.getReplyByDate();
				if (d != null) {
					long rb = d.getTime();
					if (rb < deadline) {
						deadline = rb;
					}
				}
				PendingMsg pm = new PendingMsg(msg, receiverID, deadline);
				pendings.add(pm);
			}
			break;
		case EXCEPTION:
			// FIXME: Not yet implemented --> fall through
		default: // NONE
      myContainer.notifyFailureToSender(msg, new InternalError("\"Agent unreachable\""));
		}
	}
	
	/**
	   Try to send all the pending messages
	 */
	public void doTimeOut(Timer t) {
		Iterator it = null;
		synchronized (this) {
			// Iteration is done on a clone so that elements can be removed without
			// causing a ConcurrentModificationException
			it = ((ArrayList) pendings.clone()).iterator();
		}
		
		while (it.hasNext()) {
			PendingMsg pm = (PendingMsg) it.next();
			boolean remove = false;
			
			// If the deadline has expired, don't even try again
			if (System.currentTimeMillis() > pm.getDeadline()) {
      	myContainer.notifyFailureToSender(pm.getMessage(), new InternalError("\"Agent unreachable\""));
      	remove = true;
			}
			else {
				try {
					// DEBUG
					System.out.println("DM: traying to send message "+pm.getMessage());
					myContainer.deliverNow(pm.getMessage(), pm.getReceiver());
					// DEBUG
					System.out.println("DM: message delivered");
					remove = true;
				}
				catch (UnreachableException ue) {
				}
			}
			
			if (remove) {
				synchronized (this) {
					pendings.remove(pm);
				}
			}
		}
		
		// If the list of pending messages is not empty --> activate another Timer
		synchronized (this) {
			if (!pendings.isEmpty()) {
				activateTimer();
			}
		}
	}		
					
	private void activateTimer() {
		Timer t = new Timer(System.currentTimeMillis() + retryInterval, this);
		Runtime.instance().getTimerDispatcher().add(t);
		// DEBUG
		System.out.println("Timer activated. Period is "+retryInterval);
 	}
	
	/**
	   Inner class PendingMsg
	 */
	private class PendingMsg {
		private ACLMessage msg;
		private AID receiverID;
		private long deadline;
		
		PendingMsg(ACLMessage msg, AID receiverID, long deadline) {
			this.msg = msg;
			this.receiverID = receiverID;
			this.deadline = deadline;
		}
		
		ACLMessage getMessage() {
			return msg;
		}
		
		AID getReceiver() {
			return receiverID;
		}
		
		long getDeadline() {
			return deadline;
		}
	}
}

