/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package jade.util.leap;

/**
 * The root of the LEAP Collection hierarchy.
 */
public interface Collection {

    /**
     * Adds an element.
     * 
     * @return <tt>true</tt> if the element has been added.
     */
    boolean add(Object o);

    /**
     * Checks if the collection contains elements.
     * 
     * @return <tt>true</tt> if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * Removes one instance of the specified element.
     * 
     * @param o the element to be removed
     * @return <tt>true</tt> if the element has been removed
     */
    boolean remove(Object o);

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned.
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    Iterator iterator();

    /**
     * Returns an array containing all of the elements in this collection.
     * 
     * @return an array containing all of the elements in this collection
     */
    Object[] toArray();

    /**
     * Returns the number of elements in this collection.
     * 
     * @return the number of elements in this collection.
     */
    int size();
}

