package test.behaviours;

import jade.core.*;
import jade.core.behaviours.*;

public class ParallelBehaviourTester extends Agent {
	
	public void setup() {
		final Behaviour clock = new TCounterBehaviour(this, 1000, 0); 
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
		
		System.out.println("Initial condition: main behaviour (Parallel) with two blocked children (B1 and B2)"); 
		System.out.println("--> main should immediately block"); 

		// After 5 sec restart main --> B1 and B2 should start running
		Behaviour waker = new TWakerBehaviour(this, 5000, main, new String("restart main --> B1 and B2 should start running"));
		addBehaviour(waker);
		
		// After 10 sec block main --> B1 and B2 should block too
		Behaviour blocker = new TBlockerBehaviour(this, 10000, main, new String("block main --> B1 and B2 should block too"));
		addBehaviour(blocker);
		
		// After 15 sec add B3 (runnable) --> only B3 should start running
		Behaviour adder = new TAdderBehaviour(this, 15000, main, b3, new String("add B3 (runnable) --> only B3 should start running"));
		addBehaviour(adder);
		
		// After 20 sec remove B3 --> main should block too
		Behaviour remover = new TRemoverBehaviour(this, 20000, main, b3, new String("remove B3 --> main should block too"));
		addBehaviour(remover);
		
		// After 25 sec add B4 (blocked) --> nothing should happen
		adder = new TAdderBehaviour(this, 25000, main, b4, new String("add B4 (blocked) --> nothing should happen"));
		addBehaviour(adder);
		
		// After 30 sec restart B1 --> only B1 should start running
		waker = new TWakerBehaviour(this, 30000, b1, new String("restart B1 --> only B1 should start running"));
		addBehaviour(waker);
		
		// After 35 sec block B1 --> main should block too
		blocker = new TBlockerBehaviour(this, 35000, b1, new String("block B1 --> main should block too"));
		addBehaviour(blocker);
		
		// After 40 sec remove B1, B2 and B4 --> main should terminate when it will become active again
		remover = new TRemoverBehaviour(this, 40000, main, new Behaviour[]{b1, b2, b4}, new String("remove B1, B2 and B4 --> main should terminate when it will become active again"));
		addBehaviour(remover);
		
		// After 45 sec restart main --> main should terminate as it has no children
		waker = new TWakerBehaviour(this, 45000, main, new String("restart main --> main should terminate as it has no children"));
		addBehaviour(waker);
	}
	
}
			