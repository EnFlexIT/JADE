package test.bob.beans;

import jade.content.onto.annotations.AggregateResult;

@AggregateResult(type=ClassOne.class,cardMin=1,cardMax=2)
public class ExtendedAction extends SimpleAction {
	private static final long serialVersionUID = 1L;

	private String somethingElse;

	public String getSomethingElse() {
		return somethingElse;
	}

	public void setSomethingElse(String somethingElse) {
		this.somethingElse = somethingElse;
	}
}
