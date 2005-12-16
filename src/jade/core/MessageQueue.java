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

import jade.util.leap.Iterator;
import jade.util.leap.LinkedList;
import jade.util.leap.EnumIterator;

import java.util.Vector;

import jade.lang.acl.ACLMessage;

/**
 @author Giovanni Rimassa - Universita` di Parma
 @version $Date$ $Revision$
 */
class MessageQueue {
	
	//#MIDP_EXCLUDE_BEGIN
	// In MIDP we use Vector instead of jade.util.leap.LinkedList as the latter has been implemented in terms of the first
	private LinkedList list;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	 private Vector list;
	 #MIDP_INCLUDE_END*/
	
	private int maxSize;
	
	public MessageQueue(int size) {
		maxSize = size;
		//#MIDP_EXCLUDE_BEGIN
		list = new LinkedList();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 list = new Vector();
		 #MIDP_INCLUDE_END*/
	}
	
	public MessageQueue() {
		this(0);
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public void setMaxSize(int newSize) throws IllegalArgumentException {
		if(newSize < 0)
			throw new IllegalArgumentException("Invalid MsgQueue size");
		maxSize = newSize;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	/**
	 * @return the number of messages
	 * currently in the queue
	 **/
	public int size() {
		return list.size();
	}
	
	public void addFirst(ACLMessage msg) {
		if((maxSize != 0) && (list.size() >= maxSize)) {
			//#MIDP_EXCLUDE_BEGIN
			list.removeFirst(); // FIFO replacement policy
		}
		list.addFirst(msg);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 list.setElementAt(msg,0);
		 } else
		 list.insertElementAt(msg,0);
		 #MIDP_INCLUDE_END*/
	}
	
	public void addLast(ACLMessage msg) {
		if((maxSize != 0) && (list.size() >= maxSize)){
			//#MIDP_EXCLUDE_BEGIN
			list.removeFirst(); // FIFO replacement policy
			System.err.println("WARNING: a message has been lost by an agent because of the FIFO replacement policy of its message queue.\n Notice that, under some circumstances, this might not be the proper expected behaviour and the size of the queue needs to be increased. Check the method Agent.setQueueSize()");
		}
		list.addLast(msg);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 list.removeElementAt(0);
		 } 
		 list.addElement(msg);
		 #MIDP_INCLUDE_END*/
	}
	
	public ACLMessage removeFirst() {
		//#MIDP_EXCLUDE_BEGIN
		return (ACLMessage)list.removeFirst();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 ACLMessage msg = (ACLMessage)list.firstElement();
		 list.removeElementAt(0);
		 return msg;
		 #MIDP_INCLUDE_END*/
	}
	
	public boolean remove(ACLMessage item) {
		//#MIDP_EXCLUDE_BEGIN
		return list.remove(item);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return list.removeElement(item);
		 #MIDP_INCLUDE_END*/
	}
	
	public Iterator iterator() {
		//#MIDP_EXCLUDE_BEGIN
		return list.iterator();
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 return new EnumIterator(list.elements());
		 #MIDP_INCLUDE_END*/
	}
	
	//#J2ME_EXCLUDE_BEGIN
	
	// For persistence service
	private void setMessages(java.util.List l) {
		// FIXME: To be implemented
		System.out.println(">>> MessageQueue::setMessages() <<<");
	}
	
	// For persistence service
	private java.util.List getMessages() {
		// FIXME: To be implemented
		System.out.println(">>> MessageQueue::getMessages() <<<");
		return null;
	}
	
	//#J2ME_EXCLUDE_END
	
	
	// For persistence service
	private Long persistentID;
	
	// For persistence service
	private Long getPersistentID() {
		return persistentID;
	}
	
	// For persistence service
	private void setPersistentID(Long l) {
		persistentID = l;
	}
	
}
