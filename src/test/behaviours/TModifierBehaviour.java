package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public abstract class TModifierBehaviour extends SimpleBehaviour {
	private Behaviour[] toBeModified;
	private long after;
	private boolean starting;
	private boolean finishing;
	private String msg;
		
	public TModifierBehaviour(Agent a, long ms, Behaviour[] bb, String s) {
		super(a);
			
		toBeModified = bb;
		after = ms;
		msg = s;
		starting = true;
		finishing = false;
	}
		
	public TModifierBehaviour(Agent a, long ms, Behaviour b, String s) {
		this(a, ms, new Behaviour[]{b}, s);
	}
		
	public void action() {
		if (starting) {
			block(after);
			starting = false;
			return;
		}
		else {
			if (toBeModified != null) {
				for (int i = 0; i < toBeModified.length; ++i) {
					Behaviour b = toBeModified[i];
					modify(b);
				}
				if (msg != null) {
					System.out.println(msg);
				}
			}
			finishing = true;
		}
	}
		
	public boolean done() {
		return finishing;
	}
		
	protected abstract void modify(Behaviour b);
}

