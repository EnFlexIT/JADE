


package test.behaviours.tests;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import test.common.Test;
import test.common.TestException;


public class TestCompositeBehaviourRestart extends Test{

    private boolean testing = false;
    private boolean executed = false;
    private boolean tested = false;

    public Behaviour load(final Agent a) throws TestException { 
	final SimpleBehaviour b = new SimpleBehaviour(){
		public void action(){
		    if (!testing){
			// Adds the parallel behaviour with one child
			final ParallelBehaviour cb = new ParallelBehaviour();
			cb.addSubBehaviour(new SimpleBehaviour(){
				public void action(){
				    executed = true;
				    block();
				}
				public boolean done(){
				    return false;
				}
			    });
			a.addBehaviour(cb);
			// Starts the restarting thread
			Thread t = new Thread(){
				public void run(){
				    try{
					Thread.sleep(100);
				    }catch(InterruptedException e){
				    }
				    executed = false;
				    cb.restart();
				    try{
					Thread.sleep(100);
				    }catch(InterruptedException e){
				    }
				    tested = true;
				    restart();
				};
			    };
			t.start();
			// block until restarted by the thread
			testing = true;
			block();

		    }else if (tested){
			if (executed){
			    passed("subehaviour executed");
			}else{
			    failed("subehaviour NOT executed");
			}
		    }
		}

		public boolean done(){
		    return tested;
		}

	    };
	return b;
    }

}
