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

//#J2ME_EXCLUDE_FILE


import java.io.IOException;
import java.util.Date;

import jade.core.AID;
import jade.core.Profile;

import jade.domain.FIPAAgentManagement.InternalError;

import jade.lang.acl.ACLMessage;

import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;


/**
 * This class supports the ACL persistent delivery service, managing
 * actual ACL messages storage, scheduled message delivery and other
 * utility tasks related to the service.
 *
 * @author  Giovanni Rimassa - FRAMeTech s.r.l.
 *
 */
class PersistentDeliveryManager {

    public static synchronized PersistentDeliveryManager instance(Profile p, MessageManager.Channel ch) {
	if(theInstance == null) {
	    theInstance = new PersistentDeliveryManager();
	    theInstance.initialize(p, ch);
	}

	return theInstance;
    }

    private static final String ACL_USERDEF_DUE_DATE = "JADE-persistentdelivery-duedate";

    // How often to check for expired deliveries
    private static final long DEFAULT_SENDFAILUREPERIOD = 60*1000; // One minute

    // How often to send enqueued messages
    private static final long DEFAULT_DELIVERPERIOD = 10*1000; // 10 sec

    private class DeliveryItem {

	public DeliveryItem(ACLMessage msg, AID id, long delay, MessageManager.Channel ch) {
	    if(delay != PersistentDeliveryFilter.NEVER) {
		dueDate = new Date(System.currentTimeMillis() + delay);
	    }

	    toDeliver = msg;
	    receiver = id;
	    channel = ch;
	}

	public DeliveryItem(ACLMessage msg, AID id, Date d, MessageManager.Channel ch) {
	    toDeliver = msg;
	    receiver = id;
	    dueDate = d;
	    channel = ch;
	}

	public boolean isExpired() {
	    if(dueDate != null) {
		return new Date().after(dueDate);
	    }
	    else {
		return false;
	    }
	}

	public ACLMessage getMessage() {
	    return toDeliver;
	}

	public AID getReceiver() {
	    return receiver;
	}

	public Date getDueDate() {
	    return dueDate;
	}

	public MessageManager.Channel getChannel() {
	    return channel;
	}

	private Date dueDate;
	private ACLMessage toDeliver;
	private AID receiver;
	private MessageManager.Channel channel;


    } // End of DeliveryItem class


    private class DeliveryItemProcessor implements Runnable {

	public DeliveryItemProcessor(long t) {
	    period = t;
	    myThread = new Thread(this, "Persistent Delivery Service -- Delivery Processor Thread");
	    enqueuedItems = new HashMap();
	}

	public void run() {
	    while(active) {

		// Dequeue and send a message for every different receiver
		// Wait forever if nothing to send
		// Wait for a while if there are more messages for the same receiver
		synchronized(this) {
		    try {
			while(enqueuedItems.isEmpty()) {
			    wait();
			}

			Object[] keys = enqueuedItems.keySet().toArray();
			for(int i = 0; i < keys.length; i++) {
			    List l = (List)enqueuedItems.get(keys[i]);
			    DeliveryItem item = (DeliveryItem)l.remove(0);

			    try {
				storage.delete(item.getMessage(), item.getReceiver());
			    }
			    catch(IOException ioe) {
				ioe.printStackTrace();
			    }

			    // Send either the stored message or a
			    // failure if the due date has passed
			    MessageManager.Channel ch = item.getChannel();
			    if(item.isExpired()) {
				ch.notifyFailureToSender(item.getMessage(), item.getReceiver(), new InternalError("Message Undelivered after its due date"), true);
			    }
			    else {
				// Set the due date as a user defined property of the message.
				ACLMessage msg = item.getMessage();
				Date dueDate = item.getDueDate();
				if(dueDate != null) {
				    msg.addUserDefinedParameter(ACL_USERDEF_DUE_DATE, Long.toString(dueDate.getTime()));
				}
				myMessageManager.deliver(item.getMessage(), item.getReceiver(), ch);
			    }

			    if(l.isEmpty()) {
				enqueuedItems.remove(keys[i]);
			    }
			}
		    }
		    catch(InterruptedException ie) {
			// Do nothing...
		    }
		}

		// Wait a bit before scanning the list again...
		try {
		    Thread.sleep(period);
		}
		catch(InterruptedException ie) {
		    // Do nothing...
		}
	    }
	}

	public void start() {
	    active = true;
	    myThread.start();
	}

	public void stop() {
	    active = false;
	    myThread.interrupt();
	}

	public synchronized void enqueue(DeliveryItem item) {
	    AID id = item.getReceiver();
	    List l = (List)enqueuedItems.get(id);
	    if(l == null) {
		l = new LinkedList();
		enqueuedItems.put(id, l);
	    }

	    l.add(item);
	    notifyAll();
	}

	private boolean active = false;
	private long period;
	private Thread myThread;

	private Map enqueuedItems;

    } // End of DeliveryItemProcessor class


    private class ExpirationChecker implements Runnable {

	public ExpirationChecker(long t) {
	    period = t;
	    myThread = new Thread(this, "Persistent Delivery Service -- Expiration Checker Thread");
	}

	public void run() {
	    while(active) {

		try {
		    synchronized(pendingMessages) {

			// Scan all pending messages lists...
			Object[] keys = pendingMessages.keySet().toArray();
			for(int i = 0; i < keys.length; i++) {

			    List l = (List)pendingMessages.get(keys[i]);
			    Object[] items = l.toArray();

			    for(int j = 0; j < items.length; j++) {

				// Send all expired messages...
				DeliveryItem item = (DeliveryItem)items[j];
				if(item.isExpired()) {
				    l.remove(item);
				    enqueue(item);
				}
				else {
				    l.remove(item);
				    enqueue(item);
				}
			    }

			    if(l.isEmpty()) {
				pendingMessages.remove(keys[i]);
			    }
			}

		    }
		    Thread.sleep(period);
		}
		catch(InterruptedException ie) {
		    // Do nothing...
		}
	    }
	}

	public void start() {
	    active = true;
	    myThread.start();
	}

	public void stop() {
	    active = false;
	    myThread.interrupt();
	}

	private boolean active = false;
	private long period;
	private Thread myThread;

    } // End of ExpirationChecker class


    private class DummyStorage implements MessageStorage {

	public void store(ACLMessage msg, AID receiver, Date dueDate) throws IOException {
	    // Do nothing
	}

	public void delete(ACLMessage msg, AID receiver) throws IOException {
	    // Do nothing
	}

	public void loadAll(LoadListener il) throws IOException {
	    // Do nothing
	}

    } // End of DummyStorage class


    public void initialize(Profile p, MessageManager.Channel ch) {

	users = 0;
	myMessageManager = MessageManager.instance(p);
	deliveryChannel = ch;

	// Choose the persistent storage method
	String storageMethod = p.getParameter(Profile.PERSISTENT_DELIVERY_STORAGEMETHOD, null);

	if(storageMethod != null) {
	    // Load the proper class and instantiate it
	    if(storageMethod.equals("file")) {
		storage = new FileMessageStorage(p);
	    }
	    else {
		storage = new DummyStorage();
	    }
	}
	else {
	    // Use the default, no-op implementation
	    storage = new DummyStorage();
	}

	// Load all data persisted from previous sessions
	try {
	    storage.loadAll(new MessageStorage.LoadListener() {
		    public void loadStarted(String storeName) {
			System.out.println("--> Load BEGIN <--");
		    }

		    public void itemLoaded(String storeName, ACLMessage msg, AID receiver, Date dueDate) {

			// Put the item into the pending messages table
			synchronized(pendingMessages) {
			    List msgs = (List)pendingMessages.get(receiver);
			    if(msgs == null) {
				msgs = new LinkedList();
				pendingMessages.put(receiver, msgs);
			    }

			    DeliveryItem item = new DeliveryItem(msg, receiver, dueDate, deliveryChannel);
			    msgs.add(item);
			}

			System.out.println("Message for <" + receiver.getLocalName() + ">");
			System.out.println("Expiration date: " + ((dueDate != null) ? dueDate.toString() : "NEVER"));
		    }

		    public void loadEnded(String storeName) {
			System.out.println("--> Load END <--");
		    }

	    });
	}
	catch(IOException ioe) {
	    ioe.printStackTrace();
	}

	sendFailurePeriod = DEFAULT_SENDFAILUREPERIOD;
	String s = p.getParameter(Profile.PERSISTENT_DELIVERY_SENDFAILUREPERIOD, null);
	if(s != null) {
	    try {
		sendFailurePeriod = Long.parseLong(s);
	    }
	    catch(NumberFormatException nfe) {
		// Do nothing: the default value will be used...
	    }
	}

	deliverPeriod = DEFAULT_DELIVERPERIOD;

    }

    public void storeMessage(String storeName, ACLMessage msg, AID receiver, long delay) throws IOException {

	// Store the ACL message and its receiver for later re-delivery...
	synchronized(pendingMessages) {
	    List msgs = (List)pendingMessages.get(receiver);
	    if(msgs == null) {
		msgs = new LinkedList();
		pendingMessages.put(receiver, msgs);
	    }

	    // If a due date is present in the message, use it, otherwise use the passed delay
	    String dueDate = msg.getUserDefinedParameter(ACL_USERDEF_DUE_DATE);
	    DeliveryItem item = null;
	    if(dueDate != null) {
		try {
		    item = new DeliveryItem(msg, receiver, new Date(Long.parseLong(dueDate)), deliveryChannel);
		}
		catch(NumberFormatException nfe) {
		    item = new DeliveryItem(msg, receiver, delay, deliveryChannel);
		}
	    }
	    else {
		item = new DeliveryItem(msg, receiver, delay, deliveryChannel);
	    }

	    storage.store(item.getMessage(), item.getReceiver(), item.getDueDate());
	    msgs.add(item);
	}

    }

    public void flushMessages(AID receiver) {

	// Send messages for this agent, if any...
	List l = null;
	synchronized(pendingMessages) {
	    l = (List)pendingMessages.remove(receiver);
	}

	if(l != null) {
	    Iterator it = l.iterator();
	    while(it.hasNext()) {
		DeliveryItem item = (DeliveryItem)it.next();
		enqueue(item);
	    }
	}

    }

    public synchronized void start() {

	if(users == 0) {
	    processor = new DeliveryItemProcessor(deliverPeriod);
	    failureSender = new ExpirationChecker(sendFailurePeriod);
	    processor.start();
	    failureSender.start();
	}

	users++;

    }

    public synchronized void stop() {
	users--;

	if(users == 0) {
	    processor.stop();
	    failureSender.stop();
	}
    }


    // A shared instance to have a single thread pool
    private static PersistentDeliveryManager theInstance; // FIXME: Maybe a table, indexed by a profile subset, would be better?


    private PersistentDeliveryManager() {
    }

    private void enqueue(DeliveryItem item) {
	processor.enqueue(item);
    }


    // The component managing asynchronous message delivery and retries
    private MessageManager myMessageManager;

    // The actual channel over which messages will be sent
    private MessageManager.Channel deliveryChannel;

    // How often multiple ACL messages for the same recipient will be
    // delivered (this parameter acts as a bandwidth limiter)
    private long deliverPeriod;

    // How often pending messages due date will be checked (the
    // message will be sent out if expired)
    private long sendFailurePeriod;

    // How many containers are sharing this active component
    private long users;

    // The table of undelivered messages to send
    private Map pendingMessages = new HashMap();

    // The active object that periodically checks the due date of ACL
    // messages and sends them after it expired
    private ExpirationChecker failureSender;

    // The active object that sends out stored messages when it's time
    // to do so, acting also as a bandwidth limiter towards messages
    // receiver agents.
    private DeliveryItemProcessor processor;

    // The component performing the actual storage and retrieval from
    // a persistent support
    private MessageStorage storage;

}
