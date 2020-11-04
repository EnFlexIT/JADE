/*****************************************************************
The LEAP libraries, when combined with certain JADE platform components, 
provide a run-time environment for enabling FIPA agents to execute on 
lightweight devices running Java. LEAP and JADE teams have jointly 
designed the API for ease of integration and hence to take advantage 
of these dual developments and extensions so that users only see 
one development platform and a
single homogeneous set of APIs. Enabling deployment to a wide range of
devices whilst still having access to the full development
environment and functionalities that JADE provides. 

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

package jade.util.leap;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Class declaration
 * 
 * @author LEAP
 */
public class SortedSetImpl implements SortedSet {
    private Vector        storage;
    private transient int modCount = 0;

    /**
     * Creates an empty SortedSet.
     */
    public SortedSetImpl() {
        storage = new Vector();
    }

    /**
     * Adds an object if it is not already present.
     * The object must implement the interface
     * Comparable otherwise a ClassCastException is returned.
     */
    public boolean add(Object o) {
        Comparable oc = (Comparable) o;

        if (!storage.contains(oc)) {
            modCount++;

            if (storage.isEmpty()) {
                storage.addElement(oc);
            } 
            else {
                int     nelem = storage.size();
                int     ptr = 0;
                boolean cont = true;

                while (cont) {
                    if ((oc.compareTo((Comparable) (storage.elementAt(ptr)))) 
                            <= 0) {
                        storage.insertElementAt(oc, ptr);

                        cont = false;
                    } 
                    else {
                        ptr++;

                        if (ptr == nelem) {
                            storage.addElement(oc);

                            cont = false;
                        } 
                    } 
                } 
            } 

            return (true);
        } 
        else {
            return (false);
        } 
    } 

    /**
     * Removes an object.
     */
    public boolean remove(Object o) {
        modCount++;

        return (storage.removeElement(o));
    } 

    /**
     * Checks if the set is empty.
     */
    public boolean isEmpty() {
        return (storage.isEmpty());
    } 

    /**
     * Returns the first element.
     */
    public Object first() {
        return (storage.firstElement());
    } 

    /**
     * Returns a Iterator of the elements.
     */
    public Iterator iterator() {
        return new SortedSetIterator();
    } 

    /**
     * Returns an array of the elements.
     */
    public Object[] toArray() {
        Object[] returnedArray = new Object[storage.size()];

        for (int i = 0; i < returnedArray.length; i++) {
            returnedArray[i] = storage.elementAt(i);
        } 

        return returnedArray;
    } 

    /**
     * Gives the size of the set.
     */
    public int size() {
        return (storage.size());
    } 

    /* Inner implementation of an iterator */

    /**
     * Class declaration
     * 
     * @author LEAP
     */
    private class SortedSetIterator implements Iterator {
        Enumeration elements;
        private int expectedModCount = modCount;

        /**
         * Constructor declaration
         * 
         */
        SortedSetIterator() {
            elements = SortedSetImpl.this.storage.elements();
        }

        /**
         * Returns true if the iteration has more elements.
         */
        public boolean hasNext() {
            return elements.hasMoreElements();
        } 

        /**
         * Returns the next element in the interation.
         */
        public Object next() {
            if (modCount != expectedModCount) {
                throw new RuntimeException();
            } 

            return elements.nextElement();
        } 

        /**
         * Removes from the underlying collection the last element returned
	 * by the iterator (optional operation).
         */
        public void remove() {
            throw new RuntimeException();
        } 

    }

}
