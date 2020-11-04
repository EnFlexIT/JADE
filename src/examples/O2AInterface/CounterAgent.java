package examples.O2AInterface;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * This class is a agent that implements O2AInterface
 * 
 * @author Giovanni Iavarone - Michele Izzo
 */
public class CounterAgent extends Agent implements CounterManager1, CounterManager2 {

	private TickerBehaviour counter;
	 
	/**
     * Construct the CounterAgent agent. 
     */
	public CounterAgent() {
		// Register the interfaces that must be accessible by an external program through the O2A interface
		registerO2AInterface(CounterManager1.class, this);
		registerO2AInterface(CounterManager2.class, this);
	}

	/**
	 * Activate counter. This method adds a ticker behavior that
	 * increases the counter every 5 seconds and prints the value
	 */
	@Override
	public void activateCounter() {
		counter = new TickerBehaviour(this, 5000) {
			private static final long serialVersionUID = 1L;

			public void onStart() {
				super.onStart();
				System.out.println("Agent "+getLocalName()+" - Start counting");
			}
			
			protected void onTick() {
				System.out.println("Agent "+getLocalName()+" - Counter: " + getTickCount());
			}
			
			public int onEnd() {
				System.out.println("Agent "+getLocalName()+" - Stop counting");
				return super.onEnd();
			}
		};
		addBehaviour(counter);
	}

	/**
	 * Deactivate counter. This is method removes the ticker behavior
	 */
	public void deactivateCounter() {
		counter.stop();
	}
}

