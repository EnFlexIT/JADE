package test.bob.beans;

import java.util.Arrays;
import java.util.List;


public class ClassOne extends ClassZero implements ExtendedConcept {
	private static final long serialVersionUID = 1L;

	private String fieldOneZero;
	private String fieldOneOne;
	private String fieldOneTwo;
	private List<String> listOfStrings;
	private String[] arrayOfStrings;

	public String getFieldOneZero() {
		return fieldOneZero;
	}
	public void setFieldOneZero(String fieldOneZero) {
		this.fieldOneZero = fieldOneZero;
	}
	public String getFieldOneOne() {
		return fieldOneOne;
	}
	public void setFieldOneOne(String fieldOneOne) {
		this.fieldOneOne = fieldOneOne;
	}
	public String getFieldOneTwo() {
		return fieldOneTwo;
	}
	public void setFieldOneTwo(String fieldOneTwo) {
		this.fieldOneTwo = fieldOneTwo;
	}
	public List<String> getListOfStrings() {
		return listOfStrings;
	}
	public void setListOfStrings(List<String> listOfStrings) {
		this.listOfStrings = listOfStrings;
	}
	public String[] getArrayOfStrings() {
		return arrayOfStrings;
	}
	public void setArrayOfStrings(String[] arrayOfStrings) {
		this.arrayOfStrings = arrayOfStrings;
	}

	@Override
	protected String innerToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.innerToString());
		sb.append(" fieldOneZero=");
		sb.append(fieldOneZero);
		sb.append(" fieldOneOne=");
		sb.append(fieldOneOne);
		sb.append(" fieldOneTwo=");
		sb.append(fieldOneTwo);
		sb.append(" listOfStrings=");
		sb.append(listOfStrings);
		sb.append(" arrayOfStrings=");
		sb.append(Arrays.toString(arrayOfStrings));
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ClassOne {");
		sb.append(innerToString());
		sb.append('}');
		return sb.toString();
	}
}
