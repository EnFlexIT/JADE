package jade.util.leap;

import static org.junit.Assert.*;
import jade.util.leap.ArrayList;
import jade.util.leap.HashSet;
import jade.util.leap.Iterator;
import jade.util.leap.List;

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
}
