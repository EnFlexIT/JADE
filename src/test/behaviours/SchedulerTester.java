package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class SchedulerTester extends Agent {
	
	public void setup() {
		final Behaviour clock = new TCounterBehaviour(this, 1000, 0); 
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
		
		// After 5 sec restart B1 --> B4 should start running
		Behaviour b = new TWakerBehaviour(this, 5000, b1, new String("restart B1 --> B1 should start running"));
		addBehaviour(b);
		
		// After 10 sec block B3 --> nothing should happen as B3 is already terminated
		b = new TBlockerBehaviour(this, 10000, b3, new String("block B3 --> nothing should happen as B3 is already terminated"));
		addBehaviour(b);
		
		// After 15 sec remove B4 --> B4 should stop
		b = new TRemoverBehaviour(this, 15000, null, b4, new String("remove B4 --> B4 should stop"));
		addBehaviour(b);
		
		// After 20 sec remove B2 --> Nothing should happen as B2 is blocked
		b = new TRemoverBehaviour(this, 20000, null, b4, new String("remove B2 --> Nothing should happen as B2 is blocked"));
		addBehaviour(b);
		
		// After 25 sec add B5 and B6 --> B5 should first remove B6 (B6 shuld never start) and then add B7 
		b = new TAdderBehaviour(this, 25000, null, new Behaviour[]{b5, b6}, new String("add B5 and B6 --> B5 should first remove B6 (B6 shuld never start) and then add B7"));
		addBehaviour(b);
		
		b1.block();
		b2.block();
		addBehaviour(b1);
		addBehaviour(b2);
		addBehaviour(b3);
		addBehaviour(b4);

		System.out.println("Initial condition: five behaviours added clock, B1(blocked), B2(blocked), B3 and B4"); 
		System.out.println("--> only B3 and B4 should run"); 

	}
	
}
