/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * LEAP license header to be added
 * SUN license header to be added (?)
 */
package jade.util.leap;

/**
 * This class represents the LEAP version of a &quot;List&quot;
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 23/10/00
 * 
 * @see java.util.List (J2SE)
 */
public interface List extends Collection {

    /**
     * Removes all of the elements from this list (optional operation).  This
     * list will be empty after this call returns.
     */
    void clear();

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * 
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    boolean contains(Object o);

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index &lt; 0 || index &gt;= size()).
     */
    Object get(int index);

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * 
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    int indexOf(Object o);

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     * 
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index &lt; 0 || index &gt;= size()).
     */
    Object remove(int index);
}

