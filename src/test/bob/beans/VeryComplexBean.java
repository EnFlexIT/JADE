package test.bob.beans;

import jade.content.Concept;
import jade.content.onto.BOUtils;
import jade.content.onto.annotations.AggregateSlot;
import jade.content.onto.annotations.Element;
import jade.content.onto.annotations.Slot;
import jade.content.onto.annotations.SuppressSlot;

import java.util.Iterator;
import java.util.Random;

@Element(name="beanComplicato")
public class VeryComplexBean implements Concept {
	private static final long serialVersionUID = 1L;

	public final static String EXPECTED_ELEMENT_NAME = "beanComplicato";
	public final static String[] EXPECTED_SLOT_NAMES = new String[] {"normalSlot", "aJavaListSlot", "aJavaSetSlot", "aJadeListSlot", "aJadeSetSlot"};

	private java.util.List<ClassZero> aJavaList;
	private java.util.Set<ClassOne> aJavaSet;
	private jade.util.leap.List aJadeList;
	private jade.util.leap.Set aJadeSet;
	private String suppressedString;
	private String normalSlot;
	private int hashcode;

	private static String suppressedStringDefaultValue = null;

	static {
		Random r = new Random();
		suppressedStringDefaultValue = Double.toString(r.nextDouble());
	}

	public static String getSuppressedStringDefaultValue() {
		return suppressedStringDefaultValue;
	}

	public VeryComplexBean() {
		aJavaList = null;
		aJavaSet = null;
		aJadeList = null;
		aJadeSet = null;
		suppressedString = suppressedStringDefaultValue;
		normalSlot = null;
		updateHashCode();
	}

	private boolean objsAreEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VeryComplexBean)) {
			return false;
		}
		VeryComplexBean vcb = (VeryComplexBean)obj;
		boolean result =
			objsAreEqual(normalSlot, vcb.normalSlot) &&
			objsAreEqual(aJavaList, vcb.aJavaList) &&
			objsAreEqual(aJavaSet, vcb.aJavaSet);
		if (!result) {
			return false;
		}
		result = BOUtils.leapListsAreEqual(aJadeList, vcb.aJadeList);
		if (result) {
			result = BOUtils.leapSetsAreEqual(aJadeSet, aJadeSet);
		}
		return result;
	}

	private void updateHashCode() {
		hashcode = 97;
		if (normalSlot != null) {
			hashcode ^= normalSlot.hashCode();
		}
		if (aJavaList != null) {
			hashcode ^= aJavaList.hashCode();
		}
		if (aJavaSet != null) {
			hashcode ^= aJavaSet.hashCode();
		}
		if (aJadeList != null) {
			hashcode ^= aJadeList.hashCode();
		}
		if (aJadeSet != null) {
			hashcode ^= aJadeSet.hashCode();
		}
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Slot(name="aJavaListSlot")
	@AggregateSlot(type=ClassZero.class,cardMin=2,cardMax=4)
	public java.util.List<ClassZero> getAJavaList() {
		return aJavaList;
	}

	@Slot(name="aJavaSetSlot")
	@AggregateSlot(type=ClassOne.class,cardMin=2)
	public java.util.Set<ClassOne> getAJavaSet() {
		return aJavaSet;
	}

	@Slot(name="aJadeListSlot")
	@AggregateSlot(type=ClassZero.class,cardMax=4)
	public jade.util.leap.List getAJadeList() {
		return aJadeList;
	}

	@Slot(name="aJadeSetSlot")
	@AggregateSlot(type=ClassZero.class)
	public jade.util.leap.Set getAJadeSet() {
		return aJadeSet;
	}

	@SuppressSlot
	public String getSuppressedString() {
		return suppressedString;
	}

	public String getNormalSlot() {
		return normalSlot;
	}

	public void setAJavaList(java.util.List<ClassZero> javaList) {
		aJavaList = javaList;
		updateHashCode();
	}

	public void setAJavaSet(java.util.Set<ClassOne> javaSet) {
		aJavaSet = javaSet;
		updateHashCode();
	}

	public void setAJadeList(jade.util.leap.List jadeList) {
		aJadeList = jadeList;
		updateHashCode();
	}

	public void setAJadeSet(jade.util.leap.Set jadeSet) {
		aJadeSet = jadeSet;
		updateHashCode();
	}

	public void setSuppressedString(String suppressedString) {
		this.suppressedString = suppressedString;
	}

	public void setNormalSlot(String normalSlot) {
		this.normalSlot = normalSlot;
		updateHashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("VeryComplexBean {aJavaList=");
		sb.append(aJavaList);
		sb.append(" aJavaSet=");
		sb.append(aJavaSet);
		sb.append(" aJadeList=");
		sb.append(aJadeList);
		sb.append(" aJadeSet=[");
		if (aJadeSet == null) {
			sb.append((String)null);
		} else {
			Iterator iter = aJadeSet.iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					sb.append(iter.next());
					sb.append(", ");
				}
				sb.setLength(sb.length()-2);
			}
		}
		sb.append("] suppressedString=");
		sb.append(suppressedString);
		sb.append(" normalSlot=");
		sb.append(normalSlot);
		sb.append('}');
		return sb.toString();
	}
}
