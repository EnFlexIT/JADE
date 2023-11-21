package test.bob.beans;

import jade.content.AgentAction;
import jade.content.onto.annotations.Result;

@Result(type=ClassZero.class)
public class SimpleAction implements AgentAction {
	private static final long serialVersionUID = 1L;

	private String something;

	public String getSomething() {
		return something;
	}

	public void setSomething(String something) {
		this.something = something;
	}
}
