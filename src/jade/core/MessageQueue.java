/*
  $Log$
  Revision 1.3  1999/08/10 15:36:11  rimassa
  Added support for serialization with a Memento pattern in order to
  tranfer just the size of the queue and not its content.

  Revision 1.2  1999/06/30 12:24:54  rimassa
  Fixed a bug dealing with unlimited message queues.

  Revision 1.1  1999/03/25 16:50:44  rimassa
  This class implements a message queue for an agent, that can be either bounded
  or unbounded and adopts a FIFO replacement policy.

*/

package jade.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import jade.lang.acl.ACLMessage;

class MessageQueue implements Serializable {

  // This class is sent onto the stream in place of the MessageQueue;
  // when read and resolved, it yields a new MessageQueue with the
  // same maximum size as the original one.
  private static class Memento implements Serializable {
    private int size;

    public Memento(int sz) {
      size = sz;
    }

    private Object readResolve() throws java.io.ObjectStreamException {
      return new MessageQueue(size);
    }

  }

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
    if((maxSize != 0) && (list.size() >= maxSize))
      list.removeFirst(); // FIFO replacement policy
    list.addFirst(msg);
  }

  public void addLast(ACLMessage msg) {
    if((maxSize != 0) && (list.size() >= maxSize))
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

  // This class is serialized by sending only its current size
  private Object writeReplace() throws java.io.ObjectStreamException {
    return new Memento(maxSize);
  }

}
