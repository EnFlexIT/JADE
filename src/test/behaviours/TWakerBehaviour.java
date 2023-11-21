package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TWakerBehaviour extends TModifierBehaviour {
	
	public TWakerBehaviour(Agent a, long ms, Behaviour[] bb, String s) {
		super(a, ms, bb, s);
	}
	
	public TWakerBehaviour(Agent a, long ms, Behaviour b, String s) {
		this(a, ms, new Behaviour[]{b}, s);
	}
	
	protected void modify(Behaviour b) {
		b.restart();
	}
}

