package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

import java.io.*;

public class SequentialBehaviourTester extends Agent {
	
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

		System.out.println("Initial condition - main behaviour (Sequential) with three children: B1, B2(blocked) and B3"); 
		System.out.println("--> B1 should be executed and then main should block as B2 is blocked"); 

		// After 10 ticks add B4(blocked) --> nothing should change
		Behaviour b = new TAdderBehaviour(this, 10*tick, main, b4, new String("add B4(blocked) --> nothing should change"));
		addBehaviour(b);
		
		// After 15 ticks restart main --> B2 should be executed and main should block again when it's B4 turn
		b = new TWakerBehaviour(this, 15*tick, main, new String("restart main --> B2 should be executed and main should block again when it's B4 turn"));
		addBehaviour(b);
		
		// After 20 ticks add B5 --> nothing should change
		b = new TAdderBehaviour(this, 20*tick, main, b5, new String("add B5 --> nothing should change"));
		addBehaviour(b);
		
		// After 20 ticks remove B3 --> if B3 is running, main should move to B4 and block (B4 is blocked)
		b = new TRemoverBehaviour(this, 20*tick, main, b3, new String("remove B3 --> B3 (currently running) should stop, main should move to B4 and block (B4 is blocked)"));
		addBehaviour(b);
		
		// After 25 ticks restart B4 --> main should restart and B4 should be executed
		b = new TWakerBehaviour(this, 25*tick, b4, new String("restart B4 --> main should restart and B4 should be executed"));
		addBehaviour(b);
		
		// After 28 ticks block B5 --> main should block as soon as B5 starts
		b = new TBlockerBehaviour(this, 28*tick, b5, new String("block B5 --> main should block as soon as B5 starts"));
		addBehaviour(b);
		
		// After 40 ticks restart B5 and then remove B5 --> main should restart and terminate
		b = new TWakerBehaviour(this, 40*tick, b5, new String("restart B5 --> main should restart"));
		addBehaviour(b);
		b = new TRemoverBehaviour(this, 41*tick, main, b5, new String("remove B5 --> main should terminate"));
		addBehaviour(b);
		
	}
	
}
			