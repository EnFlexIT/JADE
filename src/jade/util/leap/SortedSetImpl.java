/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * LEAP license header to be added
 * SUN license header to be added (?)
 */
package jade.util.leap;

// import java.util.TreeSet;
// import java.util.SortedSet;
// import java.util.Iterator;

/**
 * This class represents the J2SE version of a &quot;SortedSet&quot; implementation
 * to be used in LEAP.
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 20/10/00
 * 
 * @see java.util.SortedSet
 * @see java.util.TreeSet
 */
public class SortedSetImpl implements SortedSet {
    private java.util.TreeSet hiddenSet = null;

    /**
     * Default Constructor, creates an empty Set,
     * according to the elements' natural order.
     */
    public SortedSetImpl() {
        hiddenSet = new java.util.TreeSet();
    }

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean add(Object o) {
        return hiddenSet.add(o);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean isEmpty() {
        return hiddenSet.isEmpty();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean remove(Object o) {
        return hiddenSet.remove(o);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public Iterator iterator() {
        return new Iterator() {
            java.util.Iterator it = SortedSetImpl.this.hiddenSet.iterator();

            /**
             * Method declaration
             * 
             * @return
             * 
             * @see
             */
            public boolean hasNext() {
                return it.hasNext();
            } 

            /**
             * Method declaration
             * 
             * @return
             * 
             * @see
             */
            public Object next() {
                return it.next();
            } 

            /**
             * Method declaration
             * 
             * @see
             */
            public void remove() {
                it.remove();
            } 

        };
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public Object[] toArray() {
        return hiddenSet.toArray();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public int size() {
        return hiddenSet.size();
    } 

    /**
     * @see jade.util.leap.SortedSet interface
     */
    public Object first() {
        return hiddenSet.first();
    } 

}

