package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

import java.io.*;

public class ParallelBehaviourTester extends Agent {
	
	private long tick;
	
	public void setup() {
		try{
			BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("This test checks the ParallelBehaviour by adding/removing/blocking/restarting sub-behaviours.");
			System.out.print("Press any key to start...");
			String dummy = buff.readLine();
		}
		catch (IOException ioe) { 
			System.err.println("I/O error: " + ioe.getMessage()); 
		}
		
		tick = 2000;
		
		final Behaviour clock = new TCounterBehaviour(this, tick, 0); 
		addBehaviour(clock);
		
		Behaviour b1 = new TCyclicPrinterBehaviour(this, 0, "B1");
		Behaviour b2 = new TCyclicPrinterBehaviour(this, 0, "B2");
		Behaviour b3 = new TCyclicPrinterBehaviour(this, 0, "B3");
		Behaviour b4 = new TCyclicPrinterBehaviour(this, 0, "B4");
		b1.block();
		b2.block();
		b4.block();
		ParallelBehaviour main = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL) {
			public int onEnd() {
				System.out.println("Test terminated");
				myAgent.removeBehaviour(clock);
				return 0;
			}
		};
		main.addSubBehaviour(b1);
		main.addSubBehaviour(b2);
		addBehaviour(main);
		
		System.out.println("Initial condition - main behaviour (Parallel) with two children: B1 (blocked) and B2 (blocked)"); 
		System.out.println("--> main should immediately block"); 

		// After 5 ticks restart main --> B1 and B2 should start running
		Behaviour waker = new TWakerBehaviour(this, 5*tick, main, new String("restart main --> B1 and B2 should start running"));
		addBehaviour(waker);
		
		// After 10 ticks block main --> B1 and B2 should block too
		Behaviour blocker = new TBlockerBehaviour(this, 10*tick, main, new String("block main --> B1 and B2 should block too"));
		addBehaviour(blocker);
		
		// After 15 ticks add B3 (runnable) --> only B3 should start running
		Behaviour adder = new TAdderBehaviour(this, 15*tick, main, b3, new String("add B3 (runnable) --> only B3 should start running"));
		addBehaviour(adder);
		
		// After 20 ticks remove B3 --> main should block too
		Behaviour remover = new TRemoverBehaviour(this, 20*tick, main, b3, new String("remove B3 --> main should block too"));
		addBehaviour(remover);
		
		// After 25 ticks add B4 (blocked) --> nothing should happen
		adder = new TAdderBehaviour(this, 25*tick, main, b4, new String("add B4 (blocked) --> nothing should happen"));
		addBehaviour(adder);
		
		// After 30 ticks restart B1 --> only B1 should start running
		waker = new TWakerBehaviour(this, 30*tick, b1, new String("restart B1 --> only B1 should start running"));
		addBehaviour(waker);
		
		// After 35 ticks block B1 --> main should block too
		blocker = new TBlockerBehaviour(this, 35*tick, b1, new String("block B1 --> main should block too"));
		addBehaviour(blocker);
		
		// After 40 ticks remove B1, B2 and B4 --> main should terminate when it will become active again
		remover = new TRemoverBehaviour(this, 40*tick, main, new Behaviour[]{b1, b2, b4}, new String("remove B1, B2 and B4 --> main should terminate when it will become active again"));
		addBehaviour(remover);
		
		// After 45 ticks restart main --> main should terminate as it has no children
		waker = new TWakerBehaviour(this, 45*tick, main, new String("restart main --> main should terminate as it has no children"));
		addBehaviour(waker);
	}
	
}
			