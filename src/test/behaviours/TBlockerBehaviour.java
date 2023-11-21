package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TBlockerBehaviour extends TModifierBehaviour {
	
	public TBlockerBehaviour(Agent a, long ms, Behaviour[] bb, String s) {
		super(a, ms, bb, s);
	}
	
	public TBlockerBehaviour(Agent a, long ms, Behaviour b, String s) {
		this(a, ms, new Behaviour[]{b}, s);
	}
	
	protected void modify(Behaviour b) {
		b.block();
	}
}


