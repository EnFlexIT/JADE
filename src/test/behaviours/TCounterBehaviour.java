package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TCounterBehaviour extends SimpleBehaviour {
	private int cnt;
	private int max;
	private long waitingTime;
		
	public TCounterBehaviour(Agent a, long ms, int max) {
		super(a);
		waitingTime = ms;
		this.max = max;
		cnt = 0;
	}
		
	public void action() {
		System.out.println(cnt);
		cnt++;
		if (waitingTime > 0) {
			try {
				Thread.sleep(waitingTime);
			}
			catch (InterruptedException ie) {
			}
		}
	}		

	public boolean done() {
		if (max <= 0) {
			return false;
		}
		else {
			return cnt >= max;
		}
	}
}


