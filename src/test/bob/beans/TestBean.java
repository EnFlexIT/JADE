package test.bob.beans;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.Set;


public class TestBean implements Concept {
	private static final long serialVersionUID = 1L;

	private String stringOne;
	private String stringTwo;
	private String stringThree;
	private String stringFour;

	@Slot(name="slotForStringOne")
	public String getStringOne() {
		return stringOne;
	}

	public void setStringOne(String stringOne) {
		this.stringOne = stringOne;
	}

	public String getStringTwo() {
		return stringTwo;
	}

	public void setStringTwo(String stringTwo) {
		this.stringTwo = stringTwo;
	}

	public String getStringThree() {
		return stringThree;
	}

	public void setStringThree(String stringThree) {
		this.stringThree = stringThree;
	}

	public String getStringFour() {
		return stringFour;
	}

	public void setStringFour(String stringFour) {
		this.stringFour = stringFour;
	}

	private static boolean compareSequences(Iterator i1, Iterator i2) {
		boolean result = true;

		Object o1, o2;
		while (i1.hasNext()) {
			o1 = i1.next();
			o2 = i2.next();
			result = (o1 == null ? o2 == null : o1.equals(o2));
			if (!result) {
				break;
			}
		}
		return result;
	}

	static boolean leapListsAreEqual(List l1, List l2) {

		if (l1 == l2) {
			return true;
		}
		if (l1 == null) {
			return l2 == null;
		}
		if (l2 == null) {
			return false;
		}
		if (l1.size() != l2.size()) {
			return false;
		}

		return compareSequences(l1.iterator(), l2.iterator());
	}

	static boolean leapSetsAreEqual(Set s1, Set s2) {

		if (s1 == s2) {
			return true;
		}
		if (s1 == null) {
			return s2 == null;
		}
		if (s2 == null) {
			return false;
		}
		if (s1.size() != s2.size()) {
			return false;
		}

		return compareSequences(s1.iterator(), s2.iterator());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TestBean {");
		sb.append("stringOne=");
		sb.append(stringOne);
		sb.append(" stringTwo=");
		sb.append(stringTwo);
		sb.append(" stringThree=");
		sb.append(stringThree);
		sb.append(" stringFour=");
		sb.append(stringFour);
		sb.append('}');
		return sb.toString();
	}
}
