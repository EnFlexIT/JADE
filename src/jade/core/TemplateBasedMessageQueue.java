package jade.core;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Enhanced version of the agent MessageQueue that supports creating dedicated queues of messages 
 * to explicitly registered MessageTemplates.
 * 
 * @see registerTemplate()
 * @see Agent#createMessageQueue()
 * 
 * @author Caire
 */
public class TemplateBasedMessageQueue extends ExtendedMessageQueue {

	public TemplateBasedMessageQueue(int maxSize, int warningLimit, MessageTemplate warningDiscardTemplate, Agent a) {
		super(maxSize, warningLimit, warningDiscardTemplate, a);
		list = new PerTemplateList();
	}

	public TemplateBasedMessageQueue() {
		this(0, 0, null, null);
	}
	
	
	/** 
	 * Register a specific MessageTemplate to this MessageQueue. All incoming messages matching 
	 * this template will be inserted into a dedicated queue and calls to receive() with the registered
	 * template as parameter will extract messages from the dedicated queue only. 
	 * In situations with heavy load of messages (several messages in the agent queue), this can speed-up
	 * performances of the receive() method a lot.  
	 * If an incoming message matches more than one registered template it is inserted in the queue
	 * associated to the first matching template.
	 * @param tpl The MessageTemplate to be registered
	 */
	public synchronized void registerTemplate(MessageTemplate tpl) {
		// This method is executed in mutual exclusion with receive() and addLast()/addFirst() that 
		// are ALWAYS invoked inside code blocks synchronized on the MessageQueue object
		LinkedList<ACLMessage> l = new LinkedList<ACLMessage>();
		// If there are messages in the default list matching this template, move them into the 
		// per-template list we are creating
		List<ACLMessage> mm = super.receive(tpl, -1);
		if (mm != null) {
			l.addAll(mm);
		}
		((PerTemplateList) list).tplListMap.put(tpl, l);
	}
	
	@Override
	public ACLMessage receive(MessageTemplate pattern) {
		LinkedList<ACLMessage> tmp = null;
		LinkedList<ACLMessage> l = ((PerTemplateList) list).tplListMap.get(pattern);
		if (l != null) {
			tmp = list;
			list = l;
		}
		ACLMessage msg = super.receive(pattern);
		if (tmp != null) {
			list = tmp;
		}
		if (msg != null) {
			((PerTemplateList) list).currentSize--;
		}
		return msg;
	}
	
	@Override
	public List<ACLMessage> receive(MessageTemplate pattern, int max) {
		LinkedList<ACLMessage> tmp = null;
		LinkedList<ACLMessage> l = ((PerTemplateList) list).tplListMap.get(pattern);
		if (l != null) {
			tmp = list;
			list = l;
		}
		List<ACLMessage> mm = super.receive(pattern, max);
		if (tmp != null) {
			list = tmp;
		}
		if (mm != null) {
			((PerTemplateList) list).currentSize -= mm.size();
		}
		return mm;
	}

	@Override
	public void copyTo(jade.util.leap.List messages) {
		// Copy all messages from default list
		for (Iterator<ACLMessage> i = list.iterator(); i.hasNext(); messages.add(i.next()));
		// Copy all messages from per-template lists
		for (LinkedList<ACLMessage> l : ((PerTemplateList) list).tplListMap.values()) {
			for (Iterator<ACLMessage> i = l.iterator(); i.hasNext(); messages.add(i.next()));
		}
	}

	@Override
	public String dump(int limit) {
		StringBuilder sb = new StringBuilder("Default queue:\n");
		sb.append(super.dump(limit));
		for (Map.Entry<MessageTemplate, LinkedList<ACLMessage>> e : ((PerTemplateList) list).tplListMap.entrySet()) {
			sb.append("--------------------------------------\nQueue for Template "+e.getKey()+":\n");
			LinkedList<ACLMessage> tmp = list;
			list = e.getValue();
			sb.append(super.dump(limit));
			list = tmp;
		}
		return sb.toString();
	}
	
	
	/**
	 * Inner class PerTemplateList
	 */
	class PerTemplateList extends LinkedList<ACLMessage> {
		int currentSize = 0;
		Map<MessageTemplate, LinkedList<ACLMessage>> tplListMap = new HashMap<MessageTemplate, LinkedList<ACLMessage>>();

		@Override
		public int size() {
			return currentSize;
		}
		
		@Override
		public void addFirst(ACLMessage msg) {
			LinkedList<ACLMessage> l = getPerTplList(msg);
			if (l != null) {
				// msg matches a registered Template --> Insert it into the proper per-template-list
				l.addFirst(msg);
			}
			else {
				// msg does not match any registered Template --> Insert it into the default list
				super.addFirst(msg);
			}
			currentSize++;
		}
		
		@Override
		public void addLast(ACLMessage msg) {
			LinkedList<ACLMessage> l = getPerTplList(msg);
			if (l != null) {
				// msg matches a registered Template --> Append it to the proper per-template-list
				l.addLast(msg);
			}
			else {
				// msg does not match any registered Template --> Append it to the default list
				super.addLast(msg);
			}
			currentSize++;
		}
		
		LinkedList<ACLMessage> getPerTplList(ACLMessage msg) {
			for (Map.Entry<MessageTemplate, LinkedList<ACLMessage>> e : tplListMap.entrySet()) {
				if (e.getKey().match(msg)) {
					return e.getValue();
				}
			}
			return null;
		}
	}  // END of inner class PerTemplateList
}
