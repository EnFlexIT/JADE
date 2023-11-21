package test.bob.beans;

import jade.content.Predicate;

public class SimplePredicate implements Predicate {
	private static final long serialVersionUID = 1L;

	private String aString;
	private int anInt;

	public String getAString() {
		return aString;
	}
	public int getAnInt() {
		return anInt;
	}

	public void setAString(String string) {
		aString = string;
	}
	public void setAnInt(int anInt) {
		this.anInt = anInt;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SimplePredicate {aString=");
		sb.append(aString);
		sb.append(" anInt=");
		sb.append(anInt);
		sb.append('}');
		return sb.toString();
	}
}
