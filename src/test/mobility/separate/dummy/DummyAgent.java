package test.mobility.separate.dummy;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class DummyAgent extends Agent {
	private DummyClass myDummyObject;
	
	protected void setup() {
		myDummyObject = new DummyClass();
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				block();
			}
		});
	}
}
