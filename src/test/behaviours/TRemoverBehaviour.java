package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TRemoverBehaviour extends TModifierBehaviour {
	private Behaviour target;
	
	public TRemoverBehaviour(Agent a, long ms, Behaviour parent, Behaviour[] bb, String s) {
		super(a, ms, bb, s);
		
		target = parent;
	}
	
	public TRemoverBehaviour(Agent a, long ms, Behaviour parent, Behaviour b, String s) {
		this(a, ms, parent, new Behaviour[]{b}, s);
	}
	
	protected void modify(Behaviour b) {
		if (target == null) {
			// Remove the behaviour from the Agent
			myAgent.removeBehaviour(b);
		}
		else {
			// Remove the behaviour from the the parent only if the 
			// parent is a parallel or sequential behaviour
			if (target instanceof ParallelBehaviour) { 
				((ParallelBehaviour) target).removeSubBehaviour(b);
			}
			else if (target instanceof SequentialBehaviour) { 
				((SequentialBehaviour) target).removeSubBehaviour(b);
			}
		}
	}
}

