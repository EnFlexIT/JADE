package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class TCyclicPrinterBehaviour extends CyclicBehaviour {
	private String msg;
	private long waitingTime;
		
	public TCyclicPrinterBehaviour(Agent a, long ms, String msg) {
		super(a);
		this.msg = msg;
		waitingTime = ms;
	}
		
	public void action() {
		System.out.println(msg);
		if (waitingTime > 0) {
			try {
				Thread.sleep(waitingTime);
			}
			catch (InterruptedException ie) {
			}
		}
	}		
}
	
