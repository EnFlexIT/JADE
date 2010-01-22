package test.content.testOntology;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class BCThing extends CFThing {
	private List lll = new ArrayList();
	
	public Iterator getAllLll() {
		return lll.iterator();
	}
	
	public void addLll(String str) {
		lll.add(str);
	}
}
