/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * LEAP license header to be added
 * SUN license header to be added (?)
 */
package jade.util.leap;

/**
 * This class represents the LEAP version of a &quot;Map&quot;
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 23/10/00
 * 
 * @see java.util.Map (J2SE)
 */
public interface Map {

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     * @see java.util.Map
     */
    boolean isEmpty();

    /**
     * Removes the mapping for this key from this map if present (optional
     * operation).
     * 
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     * if there was no mapping for key.
     * @see java.util.Map
     */
    Object remove(Object key);

    /**
     * Returns a set view of the keys contained in this map.
     * 
     * @return a set view of the keys contained in this map.
     * @see java.util.Map
     */
    Set keySet();

    /**
     * Returns a Collection view of the values contained in this map.
     * 
     * @return the Collection of elements contained in this map.
     * @see java.util.Map
     */
    Collection values();

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).
     * 
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     * if there was no mapping for key.
     * @see java.util.Map
     */
    Object put(Object key, Object value);

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * 
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * @see java.util.Map
     */
    boolean containsKey(Object key);

    /**
     * Returns the value to which this map maps the specified
     * key. Returns null if the map contains no mapping for this
     * key.
     * 
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key,
     * or null if the map contains no mapping for this key.
     * @see java.util.Map
     */
    Object get(Object key);

    /**
     * Returns the number of mappings in this map.
     * @see java.util.Map
     */
    int size();
}

