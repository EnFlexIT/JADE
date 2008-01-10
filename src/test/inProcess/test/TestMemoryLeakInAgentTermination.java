package test.inProcess.test;

import test.common.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.wrapper.*;

public class TestMemoryLeakInAgentTermination extends Test {
	private static final int TOTAL_ROUNDS = 10000;
	private static final int ROUNDS_IN_STEP = TOTAL_ROUNDS / 100;

	public Behaviour load(Agent a) {
		
		Behaviour b = new CyclicBehaviour(a) {
			private long initialMemory;
			int roundCnt = 0;
			
			public void onStart() {
				super.onStart();
				initialMemory = getUsedMemory();
			}
			
			public void action() {
				long usedMemory = getUsedMemory();
				if ((roundCnt % ROUNDS_IN_STEP) == 0) {
					log("--- Memory used after "+roundCnt+" agent creations/terminations = "+usedMemory);
				}
					
				if (roundCnt == TOTAL_ROUNDS) {
					passed("--- Total memory increment after "+TOTAL_ROUNDS+" agent creations/terminations = "+(usedMemory - initialMemory));
				}
				else {
					try {
						PlatformController container = myAgent.getContainerController();
						AgentController ac = container.createNewAgent("a"+roundCnt, "jade.core.Agent", null);
						ac.start();
						ac.kill();
					}
					catch (Throwable t) {
						failed("--- Error creating/killing agent. "+t);
					}
				}
				roundCnt++;
			}
		};
		
		return b;
	}
		
	private long getUsedMemory() {
		java.lang.Runtime rt = java.lang.Runtime.getRuntime();
		rt.gc();
		return (rt.totalMemory() - rt.freeMemory());
	}
}
