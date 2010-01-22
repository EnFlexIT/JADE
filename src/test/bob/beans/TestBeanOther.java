package test.bob.beans;

import jade.content.Concept;


public class TestBeanOther implements Concept {
	private static final long serialVersionUID = 1L;

	private String string_private;

	public String getString_private() {
		return string_private;
	}

	public void setString_private(String string_private) {
		this.string_private = string_private;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TestBeanOther {");
		sb.append("string_private=");
		sb.append(string_private);
		sb.append('}');
		return sb.toString();
	}
}
