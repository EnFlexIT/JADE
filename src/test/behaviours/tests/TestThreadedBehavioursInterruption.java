package test.behaviours.tests;

import jade.core.*;
import jade.core.behaviours.*;
import test.common.*;

/**
   Test the threaded behaviour interruption mechanism. 
   @author Giovanni Caire - TILAB
 */
public class TestThreadedBehavioursInterruption extends Test {
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
		
  public Behaviour load(Agent a) throws TestException { 
  	setTimeout(5000);
  	
  	Behaviour b = new WakerBehaviour(a, 1000) {
  		private Behaviour threadedBehaviour;
  		
  		public void onStart() {
				log("Adding threaded behaviour"); 
  			threadedBehaviour = new CyclicBehaviour() {
  				private long cnt = 0;
  				
  				public void action() {
  					cnt++;
  				}
  			};
  			myAgent.addBehaviour(tbf.wrap(threadedBehaviour));
  			
  			super.onStart();
  		}

  		public void onWake() {
				log("Interrupting threaded behaviour"); 
  			tbf.getThread(threadedBehaviour).interrupt();
				log("Wait until ThreadedBehaviourFactory is empty..."); 
  			if (tbf.waitUntilEmpty(5000)) {
  				passed("ThreadedBehaviourFactory empty");
  			}
  			else {
  				failed("Some threads still present in ThreadedBehaviourFactory");
  			}
  		}
  	};
  	
  	return b;
  }					
}

