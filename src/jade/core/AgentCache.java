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


package jade.core;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

class AgentCache {

  private class CacheKey {
    private AID name;
    private long lastAccess;

    public CacheKey(AID id) {
      lastAccess = getAccessNumber();
      name = id;
    }

    public int compareToByName(CacheKey ck) {
      return name.compareTo(ck.getName());
    }

    public int compareToByAccess(CacheKey ck) {
      long l1 = lastAccess;
      long l2 = ck.lastAccess;
      if(l1 == l2)
	return 0;
      else
	return (l1 < l2) ? -1 : 1;
    }

    public AID getName() {
      return name;
    }

    public void setName(AID id) {
      name = id;
    }

    public void touch() {
      lastAccess = getAccessNumber();
    }

  }

  // Maintains agent name -> Agent proxy mappings. These are sorted by
  // agent name, ignoring case differences.
  private SortedMap mappings;

  // This Map contains the cache keys sorted according to agent name,
  // ignoring case differences.
  private SortedMap keysByName;

  // This Set is sorted according to last access date and is used to
  // implement LRU replacement policy.
  private SortedSet keysByTime;

  private int maxSize;

  public AgentCache(int size) {

    maxSize = size;
    currentAccessNumber = 0;

    mappings = new TreeMap(new Comparator() {
      public int compare(Object o1, Object o2) {
	CacheKey c1 = (CacheKey)o1;
	CacheKey c2 = (CacheKey)o2;
	return c1.compareToByName(c2);
      }
    });

    keysByName = new TreeMap();

    keysByTime = new TreeSet(new Comparator() {
      public int compare(Object o1, Object o2) {
	CacheKey c1 = (CacheKey)o1;
	CacheKey c2 = (CacheKey)o2;
	return c1.compareToByAccess(c2); 
      }
    });

  }

  // This is used as an ever increasing (2^64 times, then overflow)
  // counter to mark cache keys for LRU replacement policy.
  private long currentAccessNumber;

  private long getAccessNumber() {
    return currentAccessNumber++;
  }

  public synchronized AgentProxy get(AID agentName) {
    CacheKey key = (CacheKey)keysByName.get(agentName);
    if(key == null)
      return null;
    keysByTime.remove(key);
    key.touch();
    keysByTime.add(key);
    return (AgentProxy)mappings.get(key);
  }

  public synchronized AgentProxy put(AID agentName, AgentProxy ap) {

    CacheKey key = null;

    if(mappings.size() >= maxSize) { // Replace Least Recently Used cache item
      // get oldest cache key
      key = (CacheKey)keysByTime.first();

      AID LRUName = key.getName();

      // Remove oldest key from cache
      keysByName.remove(key.getName());
      keysByTime.remove(key);
      mappings.remove(key);

      // Use just removed key again for new AgentProxy
      key.setName(agentName);
      key.touch();
    }
    else {
      key = new CacheKey(agentName);
    }

    keysByName.put(agentName, key);
    keysByTime.add(key);
    return (AgentProxy)mappings.put(key, ap);

  }

  public synchronized AgentProxy remove(AID agentName) {
    AgentProxy result = null;
    CacheKey key = (CacheKey)keysByName.remove(agentName);
    if(key != null) {
      keysByTime.remove(key);
      result = (AgentProxy)mappings.remove(key);
    }
    return result;
  }

}
