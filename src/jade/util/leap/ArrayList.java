/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * LEAP license header to be added
 * SUN license header to be added (?)
 */
package jade.util.leap;

// import java.util.List;
// import java.util.ArrayList;
// import java.util.Iterator;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class represents the J2SE version of a &quot;ArrayList&quot;
 * to be used in LEAP.
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 29/09/00
 * 
 * @see java.util.ArrayList
 */
public class ArrayList implements List, LEAPSerializable {
    private transient java.util.ArrayList realHiddenList = null;
    private Vector                        hiddenList;
    private static final long             serialVersionUID = 
        3487495895819393L;

    /**
     * Default Constructor, creates an empty List
     */
    public ArrayList() {
        realHiddenList = new java.util.ArrayList();
    }

    /**
     * Constructor specifying list size
     */
    public ArrayList(int size) {
        realHiddenList = new java.util.ArrayList(size);
    }

    /**
     * Private constructor used for cloning.
     */
    public ArrayList(java.util.ArrayList toBeHiddenList) {
        realHiddenList = toBeHiddenList;
    }

    /**
     * @see jade.util.leap.List interface
     */
    public void clear() {
        realHiddenList.clear();
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public boolean contains(Object o) {
        return realHiddenList.contains(o);
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public Object get(int index) {
        return realHiddenList.get(index);
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public int indexOf(Object o) {
        return realHiddenList.indexOf(o);
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public Object remove(int index) {
        return realHiddenList.remove(index);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean add(Object o) {
        return realHiddenList.add(o);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean isEmpty() {
        return realHiddenList.isEmpty();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean remove(Object o) {
        return realHiddenList.remove(o);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public Iterator iterator() {
        return new Iterator() {
            java.util.Iterator it = ArrayList.this.realHiddenList.iterator();

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
        return realHiddenList.toArray();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public int size() {
        return realHiddenList.size();
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public Object clone() {
        return (new ArrayList((java.util.ArrayList) realHiddenList.clone()));    // FIXME: To be checked if this is a real cloning
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public java.util.List toList() {
        return realHiddenList;
    } 

    // private Object writeReplace() throws java.io.ObjectStreamException {
    // return new ArrayListSerializer(this);
    // }

    /**
     * Method declaration
     *
     * @param out
     *
     * @throws IOException
     *
     * @see
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        hiddenList = new Vector();

        java.util.Iterator it = realHiddenList.iterator();

        while (it.hasNext()) {
            hiddenList.add(it.next());
        } 

        out.defaultWriteObject();
    } 

    /**
     * Method declaration
     *
     * @param in
     *
     * @throws ClassNotFoundException
     * @throws IOException
     *
     * @see
     */
    private void readObject(ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        realHiddenList = new java.util.ArrayList();

        in.defaultReadObject();

        Enumeration e = hiddenList.elements();

        while (e.hasMoreElements()) {
            realHiddenList.add(e.nextElement());
        } 
    } 

}

