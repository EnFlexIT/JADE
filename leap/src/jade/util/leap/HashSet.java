/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.util.leap;

import jade.util.leap.Iterator;

//#MIDP_EXCLUDE_BEGIN
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//#MIDP_EXCLUDE_END
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

public class HashSet implements Set, Serializable {
	//#MIDP_EXCLUDE_BEGIN
	private static final long serialVersionUID = -6600588891113252130L;
	//#MIDP_EXCLUDE_END

	private static final String CONCURRENT_MODIFICATION = "concurrent modification";

	private static final Object PRESENT = new Object();
	private static final Object NULL = new Object();

	private transient Hashtable hiddenTable;
	private transient volatile int instanceRevision = 0;

	public HashSet() {
		hiddenTable = new Hashtable();
	}

	public HashSet(int size) {
		hiddenTable = new Hashtable(size);
	}

	public HashSet(Collection collection) {
		if (collection == null) {
			throw new NullPointerException();
		}
		hiddenTable = new Hashtable(collection.size());
		Iterator iter = collection.iterator();
		while (iter.hasNext()) {
			hiddenTable.put(iter.next(), PRESENT);
		}
	}

	public boolean add(Object o) {
		instanceRevision++;
		return hiddenTable.put(o == null ? NULL : o, PRESENT) == null;
	}

	public boolean isEmpty() {
		return hiddenTable.isEmpty();
	}

	public Iterator iterator() {
		if (hiddenTable.size() == 0) {
			return EmptyIterator.getInstance();
		}
		return new HashSetIterator();
	}

	public boolean remove(Object o) {
		instanceRevision++;
		return hiddenTable.remove(o == null ? NULL : o) == PRESENT;
	}

	public int size() {
		return hiddenTable.size();
	}

	public void clear() {
		instanceRevision++;
		hiddenTable.clear();
	}

	public boolean contains(Object o) {
		return hiddenTable.containsKey(o == null ? NULL : o);
	}

	public boolean containsAll(Collection c) {
		Iterator e = c.iterator();
		while (e.hasNext()) {
			if (!contains(e.next())) {
				return false;
			}
		}
		return true;
	}

	public Object[] toArray() {
		Object[] result = new Object[hiddenTable.size()];
		Iterator iter = iterator();

		for (int i = 0; iter.hasNext(); i++) {
			result[i] = iter.next();
		}

		return result;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Set)) {
			return false;
		}

		Collection c = (Collection) obj;
		if (c.size() != size()) {
			return false;
		}
		try {
			return containsAll(c);
		} catch (ClassCastException dummy) {
			return false;
		} catch (NullPointerException dummy) {
			return false;
		}
	}

	public int hashCode() {
		int hashCode = 0;
		Iterator iter = iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj != null) {
				hashCode += obj.hashCode();
			}
		}
		return hashCode;
	}

	private class HashSetIterator implements Iterator {

		private Enumeration elements;
		private int expectedInstanceRevision;
		private Object nextElem = null;
		private Object currElem = null;

		public HashSetIterator() {
			elements = HashSet.this.hiddenTable.keys();
			expectedInstanceRevision = instanceRevision;
			if (elements.hasMoreElements()) {
				nextElem = elements.nextElement();
			}
		}

		public boolean hasNext() {
			return nextElem != null;
		}

		public Object next() {
			if (instanceRevision != expectedInstanceRevision) {
				// ConcurrentModificationException does not exist in MIDP
				throw new RuntimeException(CONCURRENT_MODIFICATION);
			}
			if (nextElem == null) {
				throw new NoSuchElementException();
			}
			currElem = nextElem;
			Object ret = nextElem == NULL ? null : nextElem;
			if (elements.hasMoreElements()) {
				nextElem = elements.nextElement();
			} else {
				nextElem = null;
			}
			return ret;
		}

		public void remove() {
			if (currElem == null) {
				throw new IllegalStateException();
			}
			if (instanceRevision != expectedInstanceRevision) {
				// ConcurrentModificationException does not exist in MIDP
				throw new RuntimeException(CONCURRENT_MODIFICATION);
			}
			Object key = currElem;
			currElem = null;
			HashSet.this.hiddenTable.remove(key);
			expectedInstanceRevision = instanceRevision;
		}
	}

	//#MIDP_EXCLUDE_BEGIN
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		out.writeInt(hiddenTable.size());

		Iterator it = iterator();

		while (it.hasNext()) {
			out.writeObject(it.next());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		int size = in.readInt();

		hiddenTable = new Hashtable(size);

		for (int i = 0; i < size; i++) {
			add(in.readObject());
		}
	}
	//#MIDP_EXCLUDE_END
}
