/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.util.leap;

import java.util.Vector;
import java.util.Enumeration;

/**
 * This class represents the J2ME version of a &quot;LinkedList&quot;
 * to be used in LEAP.
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 17/10/00
 * 
 * @see java.util.LinkedList
 */
public class LinkedList implements List, Serializable {
  private Vector            hiddenList;
  private transient int     modCount = 0;
  private static final long serialVersionUID = 3487495895819394L;

  /**
   * Constructs an empty list.
   */
  public LinkedList() {
    hiddenList = new Vector();
  }

  /**
   * Removes and returns the first element from this list.
   * 
   * @return the first element from this list.
   * @throws    NoSuchElementException if this list is empty.
   */
  public Object removeFirst() {
    modCount++;

    Object element = hiddenList.firstElement();

    hiddenList.removeElementAt(0);

    return element;
  } 

  /**
   * Inserts the given element at the beginning of this list.
   * 
   * @param o the element to be inserted at the beginning of this list.
   */
  public void addFirst(Object o) {
    modCount++;

    hiddenList.insertElementAt(o, 0);
  } 

  /**
   * Appends the given element to the end of this list.  (Identical in
   * function to the <tt>add</tt> method; included only for consistency.)
   * 
   * @param o the element to be inserted at the end of this list.
   */
  public void addLast(Object o) {
    modCount++;

    hiddenList.addElement(o);
  } 

  /**
   * @see jade.util.leap.List interface
   */
  public void clear() {
    modCount++;

    hiddenList.removeAllElements();
  } 

  /**
   * @see jade.util.leap.List interface
   */
  public boolean contains(Object o) {
    return hiddenList.contains(o);
  } 

  /**
   * @see jade.util.leap.List interface
   */
  public Object get(int index) {
    return hiddenList.elementAt(index);
  } 

  /**
   * @see jade.util.leap.List interface
   */
  public int indexOf(Object o) {
    return hiddenList.indexOf(o);
  } 

  /**
   * @see jade.util.leap.List interface
   */
  public Object remove(int index) {
    modCount++;

    Object oldElement = hiddenList.elementAt(index);

    hiddenList.removeElementAt(index);

    return oldElement;
  } 

  /**
   * @see jade.util.leap.Collection interface
   */
  public boolean add(Object o) {
    modCount++;

    hiddenList.addElement(o);

    return true;
  } 

  /**
   * @see jade.util.leap.List interface
   */
  public void add(int index, Object o) {
    modCount++;

    hiddenList.insertElementAt(o, index);
  } 

  /**
   * @see jade.util.leap.Collection interface
   */
  public boolean isEmpty() {
    return hiddenList.isEmpty();
  } 

  /**
   * @see jade.util.leap.Collection interface
   */
  public boolean remove(Object o) {
    modCount++;

    return hiddenList.removeElement(o);
  } 

  /**
   * @see jade.util.leap.Collection interface
   */
  public Iterator iterator() {
    return new ListIterator();
  } 

  /**
   * @see jade.util.leap.Collection interface
   */
  public Object[] toArray() {
    Object[] returnedArray = new Object[hiddenList.size()];

    for (int i = 0; i < returnedArray.length; i++) {
      returnedArray[i] = hiddenList.elementAt(i);
    } 

    return returnedArray;
  } 

  /**
   * @see jade.util.leap.Collection interface
   */
  public int size() {
    return hiddenList.size();
  } 

  /* Inner implementation of an iterator */

  /**
   * Class declaration
   * 
   * @author LEAP
   */
  private class ListIterator implements Iterator {
    private int expectedModCount = modCount;
    private int index = 0;

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public boolean hasNext() {
      return index < hiddenList.size();
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public Object next() throws RuntimeException {
      if (expectedModCount != modCount) {
        throw new RuntimeException("Concurrent modification");
      } 

      return hiddenList.elementAt(index++);
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    public void remove() throws RuntimeException {
      if (expectedModCount != modCount) {
        throw new RuntimeException("a concurrent modification has been occurred.");
      } 

      hiddenList.removeElementAt(--index);

      modCount++;
      expectedModCount++;
    } 

  }

}

