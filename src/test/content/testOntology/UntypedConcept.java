package test.content.testOntology;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class UntypedConcept implements Concept {
	private Object attr1;
	private Object attr2;
	private Object attr3;
	private Object attr4;
	private Object attr5;
	private List list = new ArrayList();
	
	public Object getAttr1() {
		return attr1;
	}
	public void setAttr1(Object attr1) {
		this.attr1 = attr1;
	}
	public Object getAttr2() {
		return attr2;
	}
	public void setAttr2(Object attr2) {
		this.attr2 = attr2;
	}
	public Object getAttr3() {
		return attr3;
	}
	public void setAttr3(Object attr3) {
		this.attr3 = attr3;
	}
	public Object getAttr4() {
		return attr4;
	}
	public void setAttr4(Object attr4) {
		this.attr4 = attr4;
	}
	public Object getAttr5() {
		return attr5;
	}
	public void setAttr5(Object attr5) {
		this.attr5 = attr5;
	}
	public Iterator getAllListElements() {
		return list.iterator();
	}
	public void addListElements(Object obj) {
		list.add(obj);
	}
}
