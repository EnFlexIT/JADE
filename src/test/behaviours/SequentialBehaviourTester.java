package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class SequentialBehaviourTester extends Agent {
	
	public void setup() {
		final Behaviour clock = new TCounterBehaviour(this, 1000, 0); 
		addBehaviour(clock);
		
		Behaviour b1 = new TNTimePrinterBehaviour(this, 0, 3, "B1") {
			public int onEnd() {
				System.out.println("B1 Terminated");
				return 0;
			}
		};
		Behaviour b2 = new TNTimePrinterBehaviour(this, 0, 3, "B2") {
			public int onEnd() {
				System.out.println("B2 Terminated");
				return 0;
			}
		};
		Behaviour b3 = new TNTimePrinterBehaviour(this, 0, 3, "B3") {
			public int onEnd() {
				System.out.println("B3 Terminated");
				return 0;
			}
		};
		Behaviour b4 = new TNTimePrinterBehaviour(this, 0, 3, "B4") {
			public int onEnd() {
				System.out.println("B4 Terminated");
				return 0;
			}
		};
		Behaviour b5 = new TNTimePrinterBehaviour(this, 0, 3, "B5") {
			public int onEnd() {
				System.out.println("B5 Terminated");
				return 0;
			}
		};
		b2.block();
		b4.block();
		SequentialBehaviour main = new SequentialBehaviour(this) {
			public int onEnd() {
				System.out.println("Test terminated");
				myAgent.removeBehaviour(clock);
				return 0;
			}
		};
		main.addSubBehaviour(b1);
		main.addSubBehaviour(b2);
		main.addSubBehaviour(b3);
		addBehaviour(main);

		System.out.println("Initial condition: main behaviour (Sequential) with three children: B1, B2(blocked) and B3"); 
		System.out.println("--> B1 should be executed and then main should block as B2 is blocked"); 

		// After 10 sec add B4(blocked) --> nothing should change
		Behaviour b = new TAdderBehaviour(this, 10000, main, b4, new String("add B4(blocked) --> nothing should change"));
		addBehaviour(b);
		
		// After 15 sec restart main --> B2 should be executed and main should block again when it's B4 turn
		b = new TWakerBehaviour(this, 15000, main, new String("restart main --> B2 should be executed and main should block again when it's B4 turn"));
		addBehaviour(b);
		
		// After 20 sec add B5 --> nothing should change
		b = new TAdderBehaviour(this, 20000, main, b5, new String("add B5 --> nothing should change"));
		addBehaviour(b);
		
		// After 20 sec remove B3 --> if B3 is running, main should move to B4 and block (B4 is blocked)
		b = new TRemoverBehaviour(this, 20000, main, b3, new String("remove B3 --> B3 (currently running) should stop, main should move to B4 and block (B4 is blocked)"));
		addBehaviour(b);
		
		// After 25 sec restart B4 --> main should restart and B4 should be executed
		b = new TWakerBehaviour(this, 25000, b4, new String("restart B4 --> main should restart and B4 should be executed"));
		addBehaviour(b);
		
		// After 28 sec block B5 --> main should block as soon as B5 starts
		b = new TBlockerBehaviour(this, 28000, b5, new String("block B5 --> main should block as soon as B5 starts"));
		addBehaviour(b);
		
		// After 40 sec restart B5 and then remove B5 --> main should restart and terminate
		b = new TWakerBehaviour(this, 40000, b5, new String("restart B5 --> main should restart"));
		addBehaviour(b);
		b = new TRemoverBehaviour(this, 41000, main, b5, new String("remove B5 --> main should terminate"));
		addBehaviour(b);
		
	}
	
}
			