package jade.util.leap;

import java.util.Collection;
import java.util.HashMap;


public class HashSet implements Set {

	private java.util.HashSet internalHashSet; 

	public HashSet() {
		internalHashSet = new java.util.HashSet();
	}

	public HashSet(Collection c) {
		internalHashSet = new java.util.HashSet(c);
	}

	public HashSet(int initialCapacity, float loadFactor) {
		internalHashSet = new java.util.HashSet(initialCapacity, loadFactor);
	}

	public HashSet(int initialCapacity) {
		internalHashSet = new java.util.HashSet(initialCapacity);
	}

    public boolean add(Object o) {
		return internalHashSet.add(o);
	}

	public boolean isEmpty() {
		return internalHashSet.isEmpty();
	}

	public Iterator iterator() {
        return new Iterator() {
			java.util.Iterator it = internalHashSet.iterator();

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

	public boolean remove(Object o) {
		return internalHashSet.remove(o);
	}

	public int size() {
		return internalHashSet.size();
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return internalHashSet.toArray();
	}

	public void clear() {
		internalHashSet.clear();
	}

	public boolean contains(Object o) {
		return internalHashSet.contains(o);
	}
}
