package jade.util.leap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;


public class HashSetTest {

	private HashSet hs;
	private String[] hsContent = new String[] {"one", "two", "three", "four"};

	@Before
	public void createTestData() {
		hs = new HashSet(hsContent.length);

		for (int i = 0; i < hsContent.length; i++) {
			hs.add(hsContent[i]);
		}
	}

	@Test
	public void testSize() {
		assertEquals(hsContent.length, hs.size());
		HashSet hs2 = new HashSet(0);
		assertEquals(0, hs2.size());
		hs2 = new HashSet();
		assertEquals(0, hs2.size());
	}

	@Test
	public void testIterator() {
		Iterator it = hs.iterator();
		assertTrue(it.hasNext());
		assertFalse(it.next() == null);
		assertTrue(it.hasNext());
		assertFalse(it.next() == null);
		assertTrue(it.hasNext());
		assertFalse(it.next() == null);
		assertTrue(it.hasNext());
		assertFalse(it.next() == null);

		assertFalse(it.hasNext());
	}

	@Test
	public void testContains() {
		for (int i = 0; i < hsContent.length; i++) {
			assertTrue(hs.contains(hsContent[i]));
		}
		assertFalse(hs.contains("unknown"));
		assertFalse(hs.contains(null));
	}

	@Test
	public void testIsEmpty() {
		assertFalse(hs.isEmpty());
		HashSet hs2 = new HashSet();
		assertTrue(hs2.isEmpty());
	}

	@Test
	public void testEquals() {
		HashSet hs2 = new HashSet();
		for (int i = hsContent.length-1; i >= 0; i--) {
			hs2.add(hsContent[i]);
		}
		assertTrue(hs2.equals(hs));
		assertTrue(hs.equals(hs2));
		assertFalse(hs.equals(null));

		hs2 = new HashSet();
		assertFalse(hs.equals(hs2));
		assertFalse(hs2.equals(hs));
	}

	@Test
	public void createFromCollection() {
		HashSet hs2 = new HashSet(hs);
		assertEquals(hs, hs2);
		List l = new ArrayList();
		for (int i = 0; i < hsContent.length; i++) {
			l.add(hsContent[i]);
		}
		hs2 = new HashSet(l);
		assertEquals(hs, hs2);
	}

	@Test
	public void testClone() {
		HashSet hs2 = (HashSet)hs.clone();

		assertFalse(hs == hs2);

		assertEquals(hs.size(), hs2.size());

		assertTrue(hs2.equals(hs));
		assertTrue(hs.equals(hs2));
	}

	@Test
	public void testRemove() {
		HashSet hs2 = (HashSet)hs.clone();

		assertFalse(hs2.remove("unexistent"));
		assertFalse(hs2.remove(null));

		for (int i = 0; i < hsContent.length; i++) {
			assertTrue(hs2.remove(hsContent[i]));
		}

		assertEquals(0, hs2.size());
		assertEquals(hsContent.length, hs.size());
	}

	@Test
	public void testClear() {
		HashSet hs2 = (HashSet)hs.clone();

		hs2.clear();

		assertEquals(0, hs2.size());
	}

	@Test
	public void testAddAll() {
		HashSet hs2 = new HashSet();
		hs2.addAll(hs);
		assertEquals(hs, hs2);
	}

	@Test
	public void testRemoveAll() {
		HashSet hs2 = new HashSet(hs);
		hs2.removeAll(hs);

		assertEquals(0, hs2.size());
		assertEquals(hsContent.length, hs.size());
	}

	@Test
	public void testContainsAll() {
		HashSet hs2 = new HashSet(hs);
		assertTrue(hs2.containsAll(hs));
	}

	@Test
	public void testToArray() {
		Object[] array = hs.toArray();

		assertTrue(array != null);
		assertEquals(array.length, hs.size());

		HashSet hs2 = new HashSet();
		for (int i = 0; i < array.length; i++) {
			hs2.add(array[i]);
		}

		assertEquals(hs, hs2);
	}

	@Test
	public void testNull() {
		HashSet hs2 = new HashSet();
		hs2.add(null);

		assertEquals(1, hs2.size());

		assertTrue(hs2.contains(null));

		Iterator it = hs2.iterator();

		assertTrue(it.hasNext());

		assertEquals(null, it.next());
	}

	@Test
	public void testIteratorRemove() {
		HashSet hs2 = new HashSet(hs);

		Iterator it = hs2.iterator();
		assertTrue(it.hasNext());
		try {
			it.remove();
			fail("cannot remove from Iterator before calling next()");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		assertFalse(it.next() == null);
		assertTrue(it.hasNext());
		it.remove();
		try {
			it.remove();
			fail("cannot remove from Iterator before calling next()");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		assertFalse(it.next() == null);
		assertTrue(it.hasNext());
		it.remove();
		try {
			it.remove();
			fail("cannot remove from Iterator before calling next()");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		assertFalse(it.next() == null);
		assertTrue(it.hasNext());
		it.remove();
		try {
			it.remove();
			fail("cannot remove from Iterator before calling next()");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		assertFalse(it.next() == null);

		assertFalse(it.hasNext());
		it.remove();
		try {
			it.remove();
			fail("cannot remove from Iterator before calling next()");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		assertEquals(0, hs2.size());

		it = hs2.iterator();
		try {
			it.remove();
			fail("cannot remove from Iterator when HashSet is empty");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		hs2 = new HashSet();
		it = hs2.iterator();
		try {
			it.remove();
			fail("cannot remove from Iterator when HashSet is empty");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}
	}

	@Test
	public void testIteratorConcurrentModification() {
		HashSet hs2 = new HashSet(hs);

		Iterator it = hs2.iterator();
		assertTrue(it.hasNext());
		hs2.add(null);
		try {
			it.next();
			fail("cannot call next() in Iterator after adding an element to the HashSet");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		it = hs2.iterator();
		assertTrue(it.hasNext());
		hs2.clear();
		try {
			it.next();
			fail("cannot call next() in Iterator after calling clear() on the HashSet");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}

		hs2 = new HashSet(hs);
		hs2.add(null);
		it = hs2.iterator();
		assertTrue(it.hasNext());
		hs2.remove(null);
		try {
			it.next();
			fail("cannot call next() in Iterator after removing an element from the HashSet");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		}
	}

	@Test
	public void testSerialization() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(hs);
		byte[] byteArray = baos.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		ObjectInputStream ois = new ObjectInputStream(bais);
		HashSet deserializedHs = (HashSet)ois.readObject();

		assertEquals(hs, deserializedHs);
	}

	@Test
	public void testSerializationWithNullElement() throws Exception {
		HashSet hs2 = new HashSet(hs);
		hs2.add(null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(hs2);
		byte[] byteArray = baos.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		ObjectInputStream ois = new ObjectInputStream(bais);
		HashSet deserializedHs = (HashSet)ois.readObject();

		assertEquals(hs2, deserializedHs);
	}
}
