package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

import java.io.*;

public class SchedulerTester extends Agent {
	private long tick;
	
	public void setup() {
		try{
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("This test checks the Agent Scheduler by adding/removing/blocking/restarting behaviours.");
			System.out.print("Press any key to start...");
			String dummy = buff.readLine();
		}
		catch (IOException ioe) { 
			System.err.println("I/O error: " + ioe.getMessage()); 
		}
		
		tick = 2000;
		
		final Behaviour clock = new TCounterBehaviour(this, tick, 0); 
		addBehaviour(clock);
		
		Behaviour b1 = new TNTimePrinterBehaviour(this, 0, 3, "B1") {
			public int onEnd() {
				System.out.println("B1 Terminated");
				return 0;
			}
		};
		Behaviour b2 = new TCyclicPrinterBehaviour(this, 0, "B2");
		Behaviour b3 = new TNTimePrinterBehaviour(this, 0, 3, "B3") {
			public int onEnd() {
				System.out.println("B3 Terminated");
				return 0;
			}
		};
		Behaviour b4 = new TCyclicPrinterBehaviour(this, 0, "B4");
		final Behaviour b6 = new TCyclicPrinterBehaviour(this, 0, "B6");
		final Behaviour b7 = new TNTimePrinterBehaviour(this, 0, 3, "B7") {
			public int onEnd() {
				System.out.println("B7 Terminated");
				System.out.println("Test Terminated");
				myAgent.removeBehaviour(clock);
				return 0;
			}
		};
		Behaviour b5 = new SimpleBehaviour(this) {
			private int cnt = 0;
			private boolean end = false;
			
			public void action() {
				if (cnt == 0) {
					System.out.println("B5: removing B6");
					myAgent.removeBehaviour(b6);
					cnt++;
					return;
				}
				else {
					System.out.println("B5: adding B7");
					myAgent.addBehaviour(b7);
					System.out.println("B5 Terminated");
					end  = true;
					return;
				}
			}
			
			public boolean done() {
				return end;
			}
		};
		
		// After 5 ticks restart B1 --> B4 should start running
		Behaviour b = new TWakerBehaviour(this, 5*tick, b1, new String("restart B1 --> B1 should start running"));
		addBehaviour(b);
		
		// After 10 ticks block B3 --> nothing should happen as B3 is already terminated
		b = new TBlockerBehaviour(this, 10*tick, b3, new String("block B3 --> nothing should happen as B3 is already terminated"));
		addBehaviour(b);
		
		// After 15 ticks remove B4 --> B4 should stop
		b = new TRemoverBehaviour(this, 15*tick, null, b4, new String("remove B4 --> B4 should stop"));
		addBehaviour(b);
		
		// After 20 ticks remove B2 --> Nothing should happen as B2 is blocked
		b = new TRemoverBehaviour(this, 20*tick, null, b4, new String("remove B2 --> Nothing should happen as B2 is blocked"));
		addBehaviour(b);
		
		// After 25 ticks add B5 and B6 --> B5 should first remove B6 (B6 shuld never start) and then add B7 
		b = new TAdderBehaviour(this, 25*tick, null, new Behaviour[]{b5, b6}, new String("add B5 and B6 --> B5 should first remove B6 (B6 shuld never start) and then add B7"));
		addBehaviour(b);
		
		b1.block();
		b2.block();
		addBehaviour(b1);
		addBehaviour(b2);
		addBehaviour(b3);
		addBehaviour(b4);

		System.out.println("Initial condition - five behaviours added: clock, B1(blocked), B2(blocked), B3 and B4"); 
		System.out.println("--> only B3 and B4 should run"); 

	}
	
}
