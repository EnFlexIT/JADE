/*
  $Log$
  Revision 1.1  1999/03/25 16:50:44  rimassa
  This class implements a message queue for an agent, that can be either bounded
  or unbounded and adopts a FIFO replacement policy.

*/

package jade.core;

import java.util.Iterator;
import java.util.LinkedList;

import jade.lang.acl.ACLMessage;

class MessageQueue {

  private LinkedList list;
  private int maxSize;

  public MessageQueue(int size) {
    maxSize = size;
    list = new LinkedList();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public void setMaxSize(int newSize) throws IllegalArgumentException {
    if(newSize < 0)
      throw new IllegalArgumentException("Negative message queue size is not allowed.");
    maxSize = newSize;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public void addFirst(ACLMessage msg) {
    if(list.size() >= maxSize)
      list.removeFirst(); // FIFO replacement policy
    list.addFirst(msg);
  }

  public void addLast(ACLMessage msg) {
    if(list.size() >= maxSize)
      list.removeFirst(); // FIFO replacement policy
    list.addLast(msg);
  }

  public ACLMessage removeFirst() {
    return (ACLMessage)list.removeFirst();
  }

  public boolean remove(ACLMessage item) {
    return list.remove(item);
  }

  public Iterator iterator() {
    return list.iterator();
  }


}
