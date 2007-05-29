package test.content.testOntology;

import java.util.*;
import jade.content.Concept;

public class CFThing implements Concept {
	private Collection list;
	private Object object;
	
	public Collection getList() {
		return list;
	}
	
	public void setList(Collection list) {
		this.list = list;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
