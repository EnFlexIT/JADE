package test.bob.beans;

public class ClassTwo extends ClassOne {
	private static final long serialVersionUID = 1L;

	private long fieldTwoZero;
	private long fieldTwoOne;
	private long fieldTwoTwo;
	private long fieldTwoThree;

	public long getFieldTwoZero() {
		return fieldTwoZero;
	}
	public void setFieldTwoZero(long fieldTwoZero) {
		this.fieldTwoZero = fieldTwoZero;
	}
	public long getFieldTwoOne() {
		return fieldTwoOne;
	}
	public void setFieldTwoOne(long fieldTwoOne) {
		this.fieldTwoOne = fieldTwoOne;
	}
	public long getFieldTwoTwo() {
		return fieldTwoTwo;
	}
	public void setFieldTwoTwo(long fieldTwoTwo) {
		this.fieldTwoTwo = fieldTwoTwo;
	}
	public long getFieldTwoThree() {
		return fieldTwoThree;
	}
	public void setFieldTwoThree(long fieldTwoThree) {
		this.fieldTwoThree = fieldTwoThree;
	}

	@Override
	protected String innerToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.innerToString());
		sb.append(" fieldTwoZero=");
		sb.append(fieldTwoZero);
		sb.append(" fieldTwoOne=");
		sb.append(fieldTwoOne);
		sb.append(" fieldTwoTwo=");
		sb.append(fieldTwoTwo);
		sb.append(" fieldTwoThree=");
		sb.append(fieldTwoThree);
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ClassTwo {");
		sb.append(innerToString());
		sb.append('}');
		return sb.toString();
	}
}
