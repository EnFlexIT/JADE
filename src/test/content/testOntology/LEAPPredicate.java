package test.content.testOntology;

import jade.content.Predicate;
import jade.util.leap.List;

public class LEAPPredicate implements Predicate {
	private List list;
	private Object object;
	
	public List getList() {
		return list;
	}
	
	public void setList(List list) {
		this.list = list;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
