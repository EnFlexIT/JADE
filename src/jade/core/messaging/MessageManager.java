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

import jade.util.Logger;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.InternalError;

import jade.core.AID;
import jade.core.ResourceManager;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

/**
 * This class manages the delivery of ACLMessages to remote destinations
 * in an asynchronous way.
 * If network problems prevent the delivery of a message, this class also 
 * embeds a mechanism to buffer the message and periodically retry to 
 * deliver it.
 * @author  Giovanni Caire - TILAB
 * @author  Elisabetta Cortese - TILAB
 * @author  Fabio Bellifemine - TILAB
 * @author  Jerome Picault - Motorola Labs
 * @version $Date$ $Revision$
 */
class MessageManager {

    public interface Channel {
	void deliverNow(GenericMessage msg, AID receiverID) throws UnreachableException, NotFoundException;
	void notifyFailureToSender(GenericMessage msg, AID receiver, InternalError ie);
    }


    // A shared instance to have a single thread pool
    private static MessageManager theInstance; // FIXME: Maybe a table, indexed by a profile subset, would be better?

	private static final int  DEFAULT_POOL_SIZE = 5;
	private static final int  MAX_POOL_SIZE = 100;
	private int poolSize = DEFAULT_POOL_SIZE;

	private OutBox outBox = new OutBox();

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
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
		// POOL_SIZE
		try {
			String tmp = p.getParameter("jade_core_MessageManager_pool-size", null);
			poolSize = Integer.parseInt(tmp);
		}
		catch (Exception e) {
			// Do nothing and keep default value
		}
			
		try {
			ResourceManager rm = p.getResourceManager();
			for (int i = 0; i < poolSize; ++i) {
				String name = "Deliverer-"+i;
				Thread t = rm.getThread(ResourceManager.TIME_CRITICAL, name, new Deliverer());
				if (myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE, "Starting deliverer "+name+". Thread="+t);
				}
				t.start();
			}
		}
		catch (ProfileException pe) {
			throw new RuntimeException("Can't get ResourceManager. "+pe.getMessage());
		}
	}
			
	/**
	   Activate the asynchronous delivery of a GenericMessage
   */
	public void deliver(GenericMessage msg, AID receiverID, Channel ch) {
		if (myLogger.isLoggable(Logger.FINEST)) {
			myLogger.log(Logger.FINEST, "Enqueuing message "+stringify(msg)+" for agent "+receiverID.getName());
		}
		outBox.addLast(receiverID, msg, ch);
	}
	

	
 	/**
 	   Inner class Deliverer
 	 */
 	class Deliverer implements Runnable {

 		public void run() {
 			while (true) {
 				// Get a message from the OutBox (block until there is one)
 				PendingMsg pm = outBox.get();
 				GenericMessage msg = pm.getMessage();
 				AID receiverID = pm.getReceiver();
				if (myLogger.isLoggable(Logger.FINEST)) {
					myLogger.log(Logger.FINEST, "Serving message "+stringify(msg)+" for agent "+receiverID.getName());
				}
    		
 				// Deliver the message
        Channel ch = pm.getChannel();
	    	try {
		    	ch.deliverNow(msg, receiverID);
	    		outBox.handleDelivered(receiverID);	
					if (myLogger.isLoggable(Logger.FINEST)) {
						myLogger.log(Logger.FINEST, "Message served.");
					}
	    	}
	    	catch (Throwable t) {
	    		// A MessageManager deliverer thread must never die
	    		myLogger.log(Logger.WARNING, "MessageManager cannot deliver message "+stringify(msg)+" to agent "+receiverID.getName()+". "+t);
				  ch.notifyFailureToSender(msg, receiverID, new InternalError("\""+t+"\""));
				}
	 		}
 		}
 	
 	} 	
 	
	/**
	   Inner class PendingMsg
	 */
	public static class PendingMsg {
	    private final GenericMessage msg;
	    private final AID receiverID;
	    private final Channel channel;
	    private long deadline;
		
	    public PendingMsg(GenericMessage msg, AID receiverID, Channel channel, long deadline) {
		this.msg = msg;
		this.receiverID = receiverID;
		this.channel = channel;
		this.deadline = deadline;
	    }

	    public GenericMessage getMessage() {
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
	
  
  /**
   */
  public static final String stringify(GenericMessage m) {
  	ACLMessage msg = m.getACLMessage();
  	if (msg != null) {
	  	StringBuffer sb = new StringBuffer("(");
	  	sb.append(ACLMessage.getPerformative(msg.getPerformative()));
	  	sb.append(" sender: ");
	  	sb.append(msg.getSender());
	  	if (msg.getOntology() != null) {
	  		sb.append(" ontology: ");
	  		sb.append(msg.getOntology());
	  	}
	  	if (msg.getConversationId() != null) {
	  		sb.append(" conversation-id: ");
	  		sb.append(msg.getConversationId());
	  	}
	  	sb.append(')');
	  	return sb.toString();
  	}
  	else {
  		return ("unavailable");
  	}
  }
}

