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

import java.util.Vector;
import java.util.Hashtable;

/**
 * This class is a cache with fixed dimension that can be set in the constructur.
 * All element are indexed with an hashcode. 
 * 
 * When an element is added and the cache is already full,the oldest element is deleted. 
 *
 * @author Alessandro Chiarotto, Fabio Bellifemine - TILAB S.p.A.
 * @version $Date$ $Revision$
**/
public class HashCache 
{
	private Vector v; 
	private Hashtable ht;
	private int cs;
	/**
	* Constructs a new, empty HashCache with the specified size.
	* @param cacheSize is the size of this cache
	**/
	public HashCache(int cacheSize) 
	{
		v = new Vector(cacheSize);
		ht = new Hashtable(cacheSize);
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
		
		
		if (v.size() >= cs) 
		{
		 // remove the oldest element in the hashtable
		 ht.remove(v.elementAt(0));
		 // remove the oldest element in the vector
		 v.remove(0);
		}
		//ht.put(o, null);
		ht.put(o, new Object());
		v.add(o);
		return o;
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

}