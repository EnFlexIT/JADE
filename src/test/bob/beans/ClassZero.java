package test.bob.beans;

import jade.content.Concept;

public class ClassZero implements Concept, Comparable<ClassZero> {
	private static final long serialVersionUID = 1L;
	
	public enum EnumNumber { Zero, One, Two, Three };

	private int fieldZeroZero;
	private int fieldZeroOne;
	private EnumNumber fieldZeroEnum;

	public ClassZero() {
		fieldZeroZero = -19;
		fieldZeroOne = -79;
		fieldZeroEnum = EnumNumber.Two;
	}

	public ClassZero(int fieldZeroOne, int fieldZeroZero, EnumNumber fieldZeroEnum) {
		this.fieldZeroOne = fieldZeroOne;
		this.fieldZeroZero = fieldZeroZero;
		this.fieldZeroEnum = fieldZeroEnum;
	}

	public int getFieldZeroZero() {
		return fieldZeroZero;
	}

	public void setFieldZeroZero(int fieldZeroZero) {
		this.fieldZeroZero = fieldZeroZero;
	}

	public int getFieldZeroOne() {
		return fieldZeroOne;
	}

	public void setFieldZeroOne(int fieldZeroOne) {
		this.fieldZeroOne = fieldZeroOne;
	}

	public EnumNumber getFieldZeroEnum() {
		return fieldZeroEnum;
	}

	public void setFieldZeroEnum(EnumNumber fieldZeroEnum) {
		this.fieldZeroEnum = fieldZeroEnum;
	}
	
	/* needs to be Comparable in order to be put into a jade Set */
	public int compareTo(ClassZero o) {
		int result = fieldZeroZero-o.fieldZeroZero;
		if (result == 0) {
			result = fieldZeroOne-o.fieldZeroOne;
			if (result == 0) {
				result = fieldZeroEnum.compareTo(o.fieldZeroEnum);
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ClassZero)) {
			return false;
		}
		return compareTo((ClassZero)obj) == 0;
	}

	@Override
	public int hashCode() {
		return fieldZeroZero ^ fieldZeroOne ^ fieldZeroEnum.ordinal();
	}

	protected String innerToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("fieldZeroZero=");
		sb.append(fieldZeroZero);
		sb.append(" fieldZeroOne=");
		sb.append(fieldZeroOne);
		sb.append(" fieldZeroEnum=");
		sb.append(fieldZeroEnum);
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ClassZero {");
		sb.append(innerToString());
		sb.append('}');
		return sb.toString();
	}
}
