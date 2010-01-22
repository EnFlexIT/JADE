package test.bob.beans;

public class ClassThree extends ClassTwo {
	private static final long serialVersionUID = 1L;

	private double fieldThreeZero;
	private double fieldThreeOne;
	private double fieldThreeTwo;
	private double fieldThreeThree;
	private double fieldThreeFour;

	public double getFieldThreeZero() {
		return fieldThreeZero;
	}
	public void setFieldThreeZero(double fieldThreeZero) {
		this.fieldThreeZero = fieldThreeZero;
	}
	public double getFieldThreeOne() {
		return fieldThreeOne;
	}
	public void setFieldThreeOne(double fieldThreeOne) {
		this.fieldThreeOne = fieldThreeOne;
	}
	public double getFieldThreeTwo() {
		return fieldThreeTwo;
	}
	public void setFieldThreeTwo(double fieldThreeTwo) {
		this.fieldThreeTwo = fieldThreeTwo;
	}
	public double getFieldThreeThree() {
		return fieldThreeThree;
	}
	public void setFieldThreeThree(double fieldThreeThree) {
		this.fieldThreeThree = fieldThreeThree;
	}
	public double getFieldThreeFour() {
		return fieldThreeFour;
	}
	public void setFieldThreeFour(double fieldThreeFour) {
		this.fieldThreeFour = fieldThreeFour;
	}

	@Override
	protected String innerToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.innerToString());
		sb.append(" fieldThreeZero=");
		sb.append(fieldThreeZero);
		sb.append(" fieldThreeOne=");
		sb.append(fieldThreeOne);
		sb.append(" fieldThreeTwo=");
		sb.append(fieldThreeTwo);
		sb.append(" fieldThreeThree=");
		sb.append(fieldThreeThree);
		sb.append(" fieldThreeFour=");
		sb.append(fieldThreeFour);
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ClassThree {");
		sb.append(innerToString());
		sb.append('}');
		return sb.toString();
	}
}
