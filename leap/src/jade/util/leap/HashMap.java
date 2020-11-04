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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 * This class represents the J2ME version of a &quot;HashMap&quot;
 * to be used in LEAP.
 * Note: compared to J2SE, J2ME version of HashMap is synchronized,
 * since it relies on an Hashtable.
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 29/09/00
 * 
 * @see java.util.Hashtable
 * @see java.util.HashMap
 */
public class HashMap implements Map, Serializable {

  /**
   * The number of times this HashMap has been structurally modified.
   * This field is used to make iterators of the HashMap fail-fast.
   */
  private transient int        modCount = 0;

  /**
   * The key view
   */
  private transient Set        keySet = null;
  private transient Collection values = null;
  private Hashtable            hiddenMap;
  private static final long    serialVersionUID = 3487495895819395L;

  /**
   * The following elements are necessary to handle null keys and
   * values that are allowed in HashMap, but not in Hashtable.
   */
 	private static final Long     nullValue = new Long(serialVersionUID);

  /**
   * Default constructor, creates a new empty Map
   */
  public HashMap() {
    hiddenMap = new Hashtable();
  }

  /**
   * Constructor, creates a new empty Map with initial size
   */
  public HashMap(int s) {
    hiddenMap = new Hashtable(s);
  }

  /**
   * @see jade.util.leap.Map interface
   */
  public boolean isEmpty() {
    return hiddenMap.isEmpty();
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public Object remove(Object o) {
    modCount++;

    o = (o != null ? o : nullValue);

    Object ret = hiddenMap.remove(o);
    return (nullValue.equals(ret) ? null : ret);
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public Object put(Object key, Object value) {
    modCount++;

    key = (key != null ? key : nullValue);
    value = (value != null ? value : nullValue);

    Object previous = hiddenMap.put(key, value);

    return (nullValue.equals(previous) ? null : previous);
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public boolean containsKey(Object key) {
    key = (key != null ? key : nullValue);

    return hiddenMap.containsKey(key);
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public Object get(Object key) {
    key = (key != null ? key : nullValue);

    Object ret = hiddenMap.get(key);
    return (nullValue.equals(ret) ? null : ret);
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public int size() {
    return hiddenMap.size();
  } 

  /**
   * Remove all mapping from this HashMap.
   */
  public void clear() {
    modCount++;

    hiddenMap.clear();
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public Set keySet() {
    if (keySet == null) {
      keySet = new AbstractSet() {

        /**
         * @see jade.util.leap.Set interface
         */
        public Iterator iterator() {
          return HashMap.this.getMapIterator(KEYS);
        } 

        /**
         * @see jade.util.leap.Set interface
         */
        public boolean isEmpty() {
          // return HashMap.this.isEmpty();
          return HashMap.this.hiddenMap.isEmpty();
        } 

        /**
         * @see jade.util.leap.Set interface
         */
        public boolean remove(Object o) {
          int oldSize = size();

          // HashMap.this.remove(o);
          HashMap.this.hiddenMap.remove(o);

          return size() != oldSize;
        } 

        /**
         * @see jade.util.leap.Set interface
         */
        public int size() {
          // return HashMap.this.size();
          return HashMap.this.hiddenMap.size();
        } 

      };
    } 

    return keySet;
  } 

  /**
   * @see jade.util.leap.Map interface
   */
  public Collection values() {
    if (values == null) {
      values = new Collection() {

        /**
         * @see jade.util.leap.Collection interface
         */
        public boolean add(Object o) {
          throw new RuntimeException();
        } 

        /**
         * @see jade.util.leap.Collection interface
         */
        public boolean isEmpty() {
          // return HashMap.this.isEmpty();
          return HashMap.this.hiddenMap.isEmpty();
        } 

        /**
         * @see jade.util.leap.Collection interface
         */
        public boolean remove(Object o) {
          throw new RuntimeException();
        } 

        /**
         * @see jade.util.leap.Collection interface
         */
        public Iterator iterator() {
          return HashMap.this.getMapIterator(VALUES);
        } 

        /**
         * @see jade.util.leap.Collection interface
         */
        public Object[] toArray() {
          Object[] result = new Object[size()];
          Iterator e = iterator();

          for (int i = 0; e.hasNext(); i++) {
            result[i] = e.next();
          } 

          return result;
        } 

        /**
         * @see jade.util.leap.Collection interface
         */
        public int size() {
          // return HashMap.this.size();
          return HashMap.this.hiddenMap.size();
        } 

      };
    } 

    return values;
  } 

  /**
   * Returns the accurate iterator
   */
  private Iterator getMapIterator(int type) {
    if (size() == 0) {
      return EmptyIterator.getInstance();
    } 
    else {
      return new MapIterator(type);
    } 
  } 

  // Basic implementation of the Set interface

  /**
   * Class declaration
   * 
   * @author LEAP
   */
  private abstract class AbstractSet implements Set {

    /**
     */
    public Object[] toArray() {
      Object[] result = new Object[this.size()];
      Iterator e = iterator();

      for (int i = 0; e.hasNext(); i++) {
        result[i] = e.next();
      } 

      return result;
    } 

    /**
     */
    public boolean add(Object o) {
      throw new RuntimeException();
    } 

    /**
     * @see
     */
    public boolean isEmpty() {
      throw new RuntimeException();
    } 

    /**
     */
    public boolean remove(Object o) {
      throw new RuntimeException();
    } 

    /**
     */
    public Iterator iterator() {
      throw new RuntimeException();
    } 

    /**
     */
    public int size() {
      throw new RuntimeException();
    } 

  }

  // Implementations of Iterator.
  // Types of Iterators
  private static final int KEYS = 0;
  private static final int VALUES = 1;
  private static final int ENTRIES = 2;

  /**
   * Implementation of an Iterator for an non-empty Map
   */
  private class MapIterator implements Iterator {

    /**
     * the elements of the Map
     */
    Enumeration elements;

    /**
     * The modCount value that the iterator believes that the backing
     * List should have.  If this expectation is violated, the iterator
     * has detected concurrent modification.
     */
    private int expectedModCount = modCount;

    /**
     * Constructor declaration
     * 
     * @param type
     * 
     */
    MapIterator(int type) {
      if (type == KEYS) {
        elements = HashMap.this.hiddenMap.keys();
      } 
      else if (type == VALUES) {
        elements = HashMap.this.hiddenMap.elements();
      } 
    }

    /**
     * Method declaration
     */
    public boolean hasNext() {
      return elements.hasMoreElements();
    } 

    /**
     * Method declaration
     */
    public Object next() {
      if (modCount != expectedModCount) {
        // ConcurrentModificationException does not exist in CLDC
        throw new RuntimeException("concurrent modification");
      } 

      Object ret = elements.nextElement();
      return (nullValue.equals(ret) ? null : ret);
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    public void remove() {
      throw new RuntimeException();
    } 

  }

}

