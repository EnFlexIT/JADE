/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

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

package test.behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.util.leap.*;
import test.common.behaviours.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class PerformanceHelperAgent extends Agent {
	
	private int id;
	private boolean debugMode = false;
	
	private ACLMessage request;
	
	private TerminationChecker checker;
	private ListProcessor resumable;
	private TickerBehaviour ticker;
	private ParallelBehaviour parallel;
	private SequentialBehaviour sequential;
	private Behaviour blocker;
	
	protected void setup() {
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				if (msg != null) {
					request = msg;
					execute();
				}
				else {
					block();
				}
			}
		} );
	}
	
	private void execute() {
		id = 0;
		
		// This Behaviours checks that all the others have terminated and
		// then sends the reply to the tester
		checker = new TerminationChecker(this);
		addBehaviour(checker);
		
		// A SequentialBehaviour with some children.
		// This should stress CompositeBehaviour children scheduling
		sequential = new SequentialBehaviour(this) {
			public int onEnd() {
				checker.remove(this);
				return 0;
			}
		};
		sequential.setBehaviourName("Sequential");
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new FSM(this));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new FSM(this));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new DummyBehaviour(this, 100));
		sequential.addSubBehaviour(new FSM(this));
		addBehaviour(sequential);
		checker.add(sequential);
		
		// A ParallelBehaviour with some children.
		// This should stress CompositeBehaviour children scheduling
		parallel = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL) {
			public int onEnd() {
				checker.remove(this);
				ticker.stop();
				return 0;
			}
		};
		parallel.setBehaviourName("Parallel");
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new FSM(this));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new FSM(this));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new FSM(this));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		parallel.addSubBehaviour(new DummyBehaviour(this, 100));
		addBehaviour(parallel);
		checker.add(parallel);
		
		// A behaviour that periodically blocks and restarts the above 
		// sequential and parallel behaviours.
		// This should stress Scheduler.block()/restart() and block/restart
		// propagation among children of a CompositeBehaviour.
		blocker = new CyclicBehaviour(this) {
			private int cnt = 0;
			private int step;
			
			public void action() {
				cnt++;
				if (cnt == step) {
					sequential.block();
				}
				else if (cnt == step+1) {
					sequential.restart();
				}
				else if (cnt == 2*step) {
					parallel.block();
				}
				else if (cnt == 2*step+1) {
					parallel.restart();
					cnt = 0;
				}
			}
		};
		addBehaviour(blocker);
		
		// A simple behaviour that re-add itself on termination 
		// This should stress Scheduler.add()/remove()
		Behaviour b = new DummyBehaviour(this, 1) {
			private int cnt = 0;
			public int onEnd() {
				if (cnt < 4000) {
					cnt++;
					reset();
					myAgent.addBehaviour(this);
				}
				else {
					checker.remove(this);
				}
				return 0;
			}
		};
		addBehaviour(b);
		checker.add(b);
		
		// A list processor that at each round executes a behaviour and waits for 
		// its termination.
		// This should stress Scheduler.add()/remove()/block()/restart()
		List l = new ArrayList();
		for (int i = 0; i < 400; ++i) {
			l.add(new Resumer(this, 10));
		}
		resumable = new ListProcessor(this, l) {
			protected void processItem(Object item, int index) {
				Behaviour b = (Behaviour) item;
				myAgent.addBehaviour(b);
				pause();
			}
			
			public int onEnd() {
				checker.remove(this);
				return 0;
			}
		};
		resumable.setBehaviourName("Resumable");
		addBehaviour(resumable);
		checker.add(resumable);
		
		// A TickerBehaviour that adds some CyclicBlocker-s and then sends dummy 
		// messages to this agent to wake them up periodically.
		// This should stress Scheduler.restartAll() and time-limited behaviour blocking
		ticker = new TickerBehaviour(this, 10) {
			private ACLMessage msg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
			private Behaviour[] bb = new Behaviour[10];
			
			{
				for (int i = 0; i < bb.length; ++i) {
					bb[i] = new CyclicBlocker(myAgent, "CB-"+i);
					myAgent.addBehaviour(bb[i]);
				}
				msg.addReceiver(myAgent.getAID());
			}
				
			protected void onTick() {
				myAgent.send(msg);
			}
			
			public int onEnd() {
				for (int i = 0; i < bb.length; ++i) {
					myAgent.removeBehaviour(bb[i]);
				}
				checker.remove(this);
				return 0;
			}
		};
		ticker.setBehaviourName("Ticker");
		addBehaviour(ticker);
		checker.add(ticker);
	}
	
	
	/**
	   Inner class TerminationChecker
	 */
	class TerminationChecker extends SimpleBehaviour {
		private List checkedBehaviours = new LinkedList();
		
		TerminationChecker(Agent a) {
			super(a);
		}
		
		public void action() {
			if (!checkedBehaviours.isEmpty()) {
				block();
			}
		}
		
		public boolean done() {
			return checkedBehaviours.isEmpty();
		}
		
		public void add(Behaviour b) {
			checkedBehaviours.add(b);
			if (debugMode) {
				System.out.println("Active cnt = "+checkedBehaviours.size());
			}
		}

		public void remove(Behaviour b) {
			checkedBehaviours.remove(b);
			if (debugMode) {
				System.out.println("Active cnt = "+checkedBehaviours.size());
			}
			restart();
		}
		
		public int onEnd() {
			myAgent.removeBehaviour(blocker);
			ACLMessage reply = request.createReply();
			myAgent.send(reply);
			return 0;
		}
	}
	
	/**
	   Inner class DummyBehaviiour
	 */
	class DummyBehaviour extends SimpleBehaviour {
		private int maxIteration;
		private int nIteration;
		
		DummyBehaviour(Agent a, int n) {
			super(a);
			maxIteration = n;
			nIteration  = 0;
			setBehaviourName("DB-"+id);
			id++;
		}
		
		public void action() {
			if (debugMode) {
				System.out.println(this);
			}
			nIteration++;
		}
		
		public boolean done() {
			return (nIteration >= maxIteration);
		}
		
		public int onEnd() {
			reset();
			nIteration = 0;
			return 0;
		}
	}

	/**
	   Inner class Resumer
	 */
	class Resumer extends DummyBehaviour {
		Resumer(Agent a, int n) {
			super(a, n);
		}
		
		public int onEnd() {
			resumable.resume();
			return super.onEnd();
		}
	}
	
	/**
	   Inner class CyclicBlocker
	 */
	class CyclicBlocker extends CyclicBehaviour {
		CyclicBlocker(Agent a, String n) {
			super(a);
			setBehaviourName(n);
		}
		
		public void action() {
			block();
		}
	}
	
	/**
	   Inner class FSM
	 */
	class FSM extends FSMBehaviour {
		int roundCnt = 0;
		FSM(Agent a) {
			super(a);
			registerFirstState(new DummyBehaviour(a, 10), "One");
			registerState(new DummyBehaviour(a, 10), "Two");
			registerState(new DummyBehaviour(a, 10), "Three");
			registerState(new DummyBehaviour(a, 10), "Four");
			registerState(new DummyBehaviour(a, 10), "Five");
			registerState(new DummyBehaviour(a, 10), "Six");
			registerState(new DummyBehaviour(a, 10), "Seven");
			registerState(new DummyBehaviour(a, 10), "Eight");
			registerState(new DummyBehaviour(a, 10), "Nine");
			registerState(new DummyBehaviour(a, 10) {
				public int onEnd() {
					super.onEnd();
					roundCnt++;
					if (roundCnt > 10) {
						return 1;
					}
					else {
						return 0;
					}
				}
			}, "Ten");
			registerLastState(new DummyBehaviour(a, 1), "Final");
			
			registerDefaultTransition("One", "Two");
			registerDefaultTransition("Two", "Three");
			registerDefaultTransition("Three", "Four");
			registerDefaultTransition("Four", "Five");
			registerDefaultTransition("Five", "Six");
			registerDefaultTransition("Six", "Seven");
			registerDefaultTransition("Seven", "Eight");
			registerDefaultTransition("Eight", "Nine");
			registerDefaultTransition("Nine", "Ten");
			registerDefaultTransition("Ten", "One");
			registerTransition("Ten", "Final", 1);
		}
	}
}
