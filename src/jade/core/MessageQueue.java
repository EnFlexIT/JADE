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

import jade.lang.acl.ACLMessage;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
class MessageQueue {

  // This class is sent onto the stream in place of the MessageQueue;
  // when read and resolved, it yields a new MessageQueue with the
  // same maximum size as the original one.
  /*private static class Memento implements Serializable {
    private int size;

    public Memento(int sz) {
      size = sz;
    }

    private Object readResolve() throws java.io.ObjectStreamException {
      return new MessageQueue(size);
    }

  }
  */

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
  /*private Object writeReplace() throws java.io.ObjectStreamException {
    return new Memento(maxSize);
  }
	*/
	
}
