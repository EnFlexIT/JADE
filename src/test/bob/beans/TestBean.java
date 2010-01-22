package test.bob.beans;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;


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
