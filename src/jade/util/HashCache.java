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

package jade.util;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.util.Vector;

import jade.util.leap.Set;
import jade.util.leap.Map;
import jade.util.leap.Collection;
import jade.util.leap.HashMap;


/**
 * This class is a cache with fixed dimension that can be set in the constructur.
 * All element are indexed with an hashcode. 
 * 
 * When an element is added and the cache is already full,the oldest element is deleted. 
 *
 * @author Alessandro Chiarotto, Fabio Bellifemine - TILAB S.p.A.
 * @version $Date$ $Revision$
**/
public class HashCache implements Map
{
	private Vector v; 
	private Map ht;
	private int cs;
	/**
	* Constructs a new, empty HashCache with the specified size.
	* @param cacheSize is the size of this cache
	**/
	public HashCache(int cacheSize) 
	{
		v = new Vector(cacheSize);
		ht = new HashMap(cacheSize);
		cs = cacheSize;
	}

	
	 /**
	 * Adds the specified element to this hashcache if it is not already
	 * present.
	 * If the cache is already full,the oldest element is deleted.
	 * 
	 * @param o element to be added to this set.
	 * @return o the specified added object
	 * element.
	 */
	public Object add(Object o) {
	    return put(o, o);
	}

        /**
	 * Adds a key-value pair to this cache
	 * @param key The key with which the value can be retrieved in
	 * the future.
	 * @param value The value to store in the cache.
	 * @return The value previously associated to the key, if any.
	 */
        public Object put(Object key, Object value) {
	    if (v.size() >= cs) 
		{
		    // remove the oldest (LRU-wise) element
		    remove(v.elementAt(0));
		}
	    ht.put(key, value);
	    v.addElement(key);
	    return key;
	}

        /**
	 * Remove an existing key-value pair from the cache
	 *
	 * @param o The key to be removed (together with its associated value.
	 * @return The value associated to the given key, if any.
	 */
         public Object remove(Object key)
         {
	     v.removeElement(key);
	     return ht.remove(key);
	 }


	/**
	 * Tests if the specified object is a key in this hashcache.
	 * present.
	 * the oldest element is deleted. 
	 * @param o element to be added to this set.
	 * @return true if the haschcache contains the object <CODE>o</CODE>,
	 * otherwise false
	 * 
	 */
	public boolean contains(Object o) 
	{
		return ht.containsKey(o);
	}

        /**
	 * Retrieves a cached element. The retrieved element is also
	 * marked as the last used one, so that the cache replacement
	 * policy becomes LRU instead of FIFO.
	 * @param o The 
	 */
         public Object get(Object key)
         {
	     if(v.removeElement(key)) {
		 v.addElement(key);
	     }

	     return ht.get(key);
	 }

        /**
         * Clears the cache, removing all key-value pairs
	 *
	 */
        public void clear() {
	     ht.clear();
	     v.removeAllElements();
	}



    // Remaining methods needed to implement jade.util.leap.Map are
    // simply delegated to the inner HashMap...

    public boolean isEmpty() {
	return ht.isEmpty();
    }

    public Set keySet() {
	return ht.keySet();
    }

    public Collection values() {
	return ht.values();
    }

    public boolean containsKey(Object key) {
	return ht.containsKey(key);
    }

    public int size() {
	return ht.size();
    }

}
