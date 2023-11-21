package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TNTimePrinterBehaviour extends SimpleBehaviour {
	private int cnt;
	private int times;
	private long waitingTime;
	private String msg;
		
	public TNTimePrinterBehaviour(Agent a, long ms, int n, String s) {
		super(a);
		waitingTime = ms;
		times = n;
		msg = s;
		cnt = 0;
	}
		
	public void action() {
		System.out.println(msg);
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
		if (times <= 0) {
			return false;
		}
		else {
			return cnt >= times;
		}
	}
}


