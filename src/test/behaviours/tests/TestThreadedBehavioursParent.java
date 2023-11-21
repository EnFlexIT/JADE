package test.behaviours.tests;

import jade.core.*;
import jade.core.behaviours.*;
import test.common.*;

/**
 Test that the parent behaviour (as returned by the getParent() method) 
 of a behaviour that is executed in a dedicated thread is what one
 would expect. 
 @author Giovanni Caire - TILAB
 */
public class TestThreadedBehavioursParent extends Test {
	private static final String FIRST = "First";
	private static final String PARENT_CHECKER = "Parent-Checker";
	private static final String CHECK_TERMINATION = "Check-termination";
	private static final String LAST = "Last";
	
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	private Behaviour expectedParent;
	private int expectedAttempt = 0;
	private int attempt;
	
	public Behaviour load(Agent a) throws TestException { 
		final Behaviour parentChecker = new OneShotBehaviour(a) {
			public void onStart() {
				attempt++;
			}
			
			public void action() {
				// Check the attempt to see if onStart() has been executed.
				// This means that reset() worked properly
				if (attempt != expectedAttempt) {
					failed("Wrong attempt: found "+attempt+" while "+expectedAttempt+" was expected.");
				}
				
				Behaviour parent = getParent();
				if (parent.equals(expectedParent)) {
					log("Parent behaviour is "+expectedParent.getClass().getName()+" as expected");
				}
				else {
					failed("Wrong parent: found "+parent.getClass().getName()+" while "+expectedParent.getClass().getName()+" was expected.");
				}
			}
		};
		
		final SequentialBehaviour testExecutor = new SequentialBehaviour(a) {
			public int onEnd() {
				passed("Test completed successfully");
				return super.onEnd();
			}
		};
		final FSMBehaviour fsm = new FSMBehaviour(a);
		
		// Step 1: Executes the parentChecker as a normal child of the 
		// testExecutor sequential behaviour 
		testExecutor.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				log("Step 1: parentChecker as normal child of a SequentialBehaviour"); 
				expectedParent = testExecutor;
				expectedAttempt = 1;
			}
		} );
		testExecutor.addSubBehaviour(parentChecker);
		
		// Step 2: Executes the parentChecker as a threaded child of the 
		// testExecutor sequential behaviour
		testExecutor.addSubBehaviour(new OneShotBehaviour(a) {
			public void action() {
				parentChecker.reset();
				log("Step 2: parentChecker as threaded child of a SequentialBehaviour"); 
				expectedParent = testExecutor;
				expectedAttempt = 2;
				testExecutor.addSubBehaviour(tbf.wrap(parentChecker));
				testExecutor.addSubBehaviour(fsm);
			}
		} );
		
		// Step 3: Executes the parentChecker as a threaded child of an
		// FSMBehaviour that visits it 2 times.
		fsm.registerFirstState(new OneShotBehaviour(a) {
			public void action() {
				parentChecker.reset();
				log("Step 3.a: parentChecker as threaded child of an FSMBehaviour (first visit)"); 
				expectedParent = fsm;
				expectedAttempt = 3;
				fsm.registerState(tbf.wrap(parentChecker), PARENT_CHECKER);
			}
		}, FIRST);  	
		fsm.registerState(new OneShotBehaviour() {
			public void action() {
				if (expectedAttempt == 3) {
					log("Step 3.b: parentChecker as threaded child of an FSMBehaviour (second visit)");
				}
				expectedAttempt++;
			}
			public int onEnd() {
				return expectedAttempt;
			}
		}, CHECK_TERMINATION);
		
		// Step 4: Executes the parentChecker once more as a normal child of
		// the testExecutor sequential behaviour
		fsm.registerLastState(new OneShotBehaviour() {
			public void action() {
				parentChecker.reset();
				log("Step 4: parentChecker again as normal child of a SequentialBehaviour"); 
				expectedParent = testExecutor;
				testExecutor.addSubBehaviour(parentChecker);
			}
		}, LAST);
		
		fsm.registerDefaultTransition(FIRST, PARENT_CHECKER);
		fsm.registerDefaultTransition(PARENT_CHECKER, CHECK_TERMINATION);
		fsm.registerTransition(CHECK_TERMINATION, PARENT_CHECKER, 4, new String[]{PARENT_CHECKER, CHECK_TERMINATION});
		fsm.registerDefaultTransition(CHECK_TERMINATION, LAST);
		
		return testExecutor;
	}					
}

