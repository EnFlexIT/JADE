/*****************************************************************
Copyright (C) 2004 Mooter Pty Ltd.

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation,
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package test.stress.deadlock;

import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Runtime;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import test.common.Test;
import test.common.TestException;


/**
   @author Richard Heycock
 */
public class TestForDeadlock extends Test {
	public static final String ITERATIONS_KEY = "iterations";
	
	private ContainerController cc;
	private AgentController ac;
	private int iterations;
	private int counter;

	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	
	public Behaviour load(Agent a) throws TestException {
		iterations = 100000000; // Default: 100 millions
		try {
			iterations = Integer.parseInt(getTestArgument(ITERATIONS_KEY));
		}
		catch (Exception e) {
			// Keep default
		}
						
		ParallelBehaviour pb = new ParallelBehaviour(a, ParallelBehaviour.WHEN_ANY);
				
		// Start a ThreadedBehaviour that is used to check the progress of the test.
		TickerBehaviour ticker = new TickerBehaviour(a, 10000) {
			private int previousCounter = 0;
			
			public void onStart() {
				super.onStart();
			}
			
			public void onTick() {
				if (counter == previousCounter) {
					// No progress has been done since last tick --> deadlock!
					failed("Deadlock after "+counter+" iterations");
					stop();
				}
				else {
					log("# "+counter+" iterations OK");
					previousCounter = counter;
				}
			}
		};
		pb.addSubBehaviour(tbf.wrap(ticker));

		
		// Create the Behaviour performing the test 
		Behaviour b = new OneShotBehaviour(a) {
			public void action() {
				try {
					log("Launching new container...");
					cc = Runtime.instance().createAgentContainer(new ProfileImpl(null, Test.DEFAULT_PORT, null));				

					if(cc != null) {
						log("Starting NullAgent...");
						// Condition variable used to synchronize with NullAgent setup.
						ConditionVariable condVar = new ConditionVariable();
						ac = cc.createNewAgent("nullAgent", "test.stress.deadlock.NullAgent", new Object[]{condVar});
						log("NullAgent created");

						try {
							ac.start();
							condVar.waitOn();
							log("NullAgent started");
						}
						catch(StaleProxyException ex) {
							failed("Error starting NullAgent. " + ex);
						}
						
						// At each iteration pass a message (i.e. we are not talking about 
						// ACLMessage here) to the agent and wait until it has been
						// processed
						for(counter = 0; counter < iterations; counter++){
							sendMessage(counter);
						}
						passed("All "+iterations+" iterations completed succesfully");
					}
					else {
						failed("Error creating container");
					}
				}
				catch(Exception e) {
					failed("Unexpected error: " + e.toString());
					e.printStackTrace();
				}
			}
			
			private void sendMessage(int n) throws InterruptedException, StaleProxyException{
				ConditionVariable condVar = new ConditionVariable();
				
				MessageBean mb = new MessageBean();
				mb.setN(n);
				mb.setCondVar(condVar);
				ac.putO2AObject(mb, AgentController.ASYNC);
				condVar.waitOn();
			}
		};

		pb.addSubBehaviour(b);
		
		return pb;
	}

	public void clean(Agent a) {
		tbf.interrupt();
		if(cc != null) {
			try {
				cc.kill();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
