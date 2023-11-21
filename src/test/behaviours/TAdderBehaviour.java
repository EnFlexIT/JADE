package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TAdderBehaviour extends TModifierBehaviour {
	private Behaviour target;
	
	public TAdderBehaviour(Agent a, long ms, Behaviour parent, Behaviour[] bb, String s) {
		super(a, ms, bb, s);
		
		target = parent;
	}
	
	public TAdderBehaviour(Agent a, long ms, Behaviour parent, Behaviour b, String s) {
		this(a, ms, parent, new Behaviour[]{b}, s);
	}
	
	protected void modify(Behaviour b) {
		if (target == null) {
			// Add the behaviour to the Agent
			myAgent.addBehaviour(b);
		}
		else {
			// Add the behaviour to the the parent only if the 
			// parent is a parallel or sequential behaviour
			if (target instanceof ParallelBehaviour) { 
				((ParallelBehaviour) target).addSubBehaviour(b);
			}
			else if (target instanceof SequentialBehaviour) { 
				((SequentialBehaviour) target).addSubBehaviour(b);
			}
		}
	}
}

