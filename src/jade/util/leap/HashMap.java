/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * LEAP license header to be added
 * SUN license header to be added (?)
 */
package jade.util.leap;

// import java.util.HashMap;
// import java.util.Iterator;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This class represents the J2SE version of a &quot;HashMap&quot;
 * to be used in LEAP.
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 29/09/00
 * 
 * @see java.util.HashMap
 */
public class HashMap implements Map, LEAPSerializable {
    private transient java.util.HashMap realHiddenMap = null;
    private Hashtable                   hiddenMap;
    private static final long           serialVersionUID = 3487495895819395L;

    /**
     * Proxy to the realHiddenMap keys Set
     */
    private transient Set               keySet = null;

    /**
     * Proxy to the realHiddenMap values Collection
     */
    private transient Collection        values = null;

    /**
     * Default constructor, creates a new empty Map
     */
    public HashMap() {
        realHiddenMap = new java.util.HashMap();
    }

    /**
     * Constructor, creates a new Map with initial size
     */
    public HashMap(int s) {
        realHiddenMap = new java.util.HashMap(s);
    }

    /**
     * @see jade.util.leap.Map interface
     */
    public boolean isEmpty() {
        return realHiddenMap.isEmpty();
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Object remove(Object o) {
        return realHiddenMap.remove(o);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Object put(Object key, Object value) {
        return realHiddenMap.put(key, value);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Object get(Object key) {
        return realHiddenMap.get(key);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public int size() {
        return realHiddenMap.size();
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public boolean containsKey(Object key) {
        return realHiddenMap.containsKey(key);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Set keySet() {
        if (keySet == null) {
            keySet = new Set() {

                /**
                 * @see jade.util.leap.Set interface
                 */
                public Object[] toArray() {
                    return HashMap.this.realHiddenMap.keySet().toArray();
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public boolean add(Object o) {
                    return HashMap.this.realHiddenMap.keySet().add(o);
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public boolean isEmpty() {
                    return HashMap.this.realHiddenMap.keySet().isEmpty();
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public boolean remove(Object o) {
                    return HashMap.this.realHiddenMap.keySet().remove(o);
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public Iterator iterator() {
                    return new Iterator() {
                        java.util.Iterator it = 
                            HashMap.this.realHiddenMap.keySet().iterator();

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public boolean hasNext() {
                            return it.hasNext();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public Object next() {
                            return it.next();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public void remove() {
                            it.remove();
                        } 

                    };
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public int size() {
                    return HashMap.this.realHiddenMap.keySet().size();
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
                public Object[] toArray() {
                    return HashMap.this.realHiddenMap.values().toArray();
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public boolean add(Object o) {
                    return HashMap.this.realHiddenMap.values().add(o);
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public boolean isEmpty() {
                    return HashMap.this.realHiddenMap.values().isEmpty();
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public boolean remove(Object o) {
                    return HashMap.this.realHiddenMap.values().remove(o);
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public Iterator iterator() {
                    return new Iterator() {
                        java.util.Iterator it = 
                            HashMap.this.realHiddenMap.values().iterator();

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public boolean hasNext() {
                            return it.hasNext();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public Object next() {
                            return it.next();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public void remove() {
                            it.remove();
                        } 

                    };
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public int size() {
                    return HashMap.this.realHiddenMap.values().size();
                } 

            };
        } 

        return values;
    } 

    // private Object writeReplace() throws java.io.ObjectStreamException {
    // return new HashMapSerializer(this);
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
        hiddenMap = new Hashtable();

        java.util.Iterator it = realHiddenMap.keySet().iterator();

        while (it.hasNext()) {
            Object key = it.next();
            Object value = realHiddenMap.get(key);

            hiddenMap.put(key, value);
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
        realHiddenMap = new java.util.HashMap();

        in.defaultReadObject();

        Enumeration e = hiddenMap.keys();

        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = hiddenMap.get(key);

            realHiddenMap.put(key, value);
        } 
    } 

}

