/*
  $Log$
  Revision 1.2  1999/03/31 15:56:14  rimassa
  Remoded dead code.
  Added a check for null argument in remove() method.

  Revision 1.1  1999/03/24 12:22:58  rimassa
  This class acts as an address cache for agents, using an LRU replacement policy.

*/

package jade.core;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

class AgentCache {

  private class CacheKey {
    private String name;
    private long lastAccess;

    public CacheKey(String s) {
      lastAccess = getAccessNumber();
      name = s;
    }

    public int compareToByName(CacheKey ck) {
      return String.CASE_INSENSITIVE_ORDER.compare(getName(), ck.getName());
    }

    public int compareToByAccess(CacheKey ck) {
      long l1 = lastAccess;
      long l2 = ck.lastAccess;
      if(l1 == l2)
	return 0;
      else
	return (l1 < l2) ? -1 : 1;
    }

    public String getName() {
      return name;
    }

    public void setName(String s) {
      name = s;
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

    keysByName = new TreeMap(String.CASE_INSENSITIVE_ORDER);

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

  public synchronized AgentProxy get(String agentName) {
    CacheKey key = (CacheKey)keysByName.get(agentName);
    if(key == null)
      return null;
    keysByTime.remove(key);
    key.touch();
    keysByTime.add(key);
    return (AgentProxy)mappings.get(key);
  }

  public synchronized AgentProxy put(String agentName, AgentProxy ap) {

    CacheKey key = null;

    if(mappings.size() >= maxSize) { // Replace Least Recently Used cache item
      // get oldest cache key
      key = (CacheKey)keysByTime.first();

      String LRUName = key.getName();

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

  public synchronized AgentProxy remove(String agentName) {
    AgentProxy result = null;
    CacheKey key = (CacheKey)keysByName.remove(agentName);
    if(key != null) {
      keysByTime.remove(key);
      result = (AgentProxy)mappings.remove(key);
    }
    return result;
  }

}
