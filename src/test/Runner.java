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

package test;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Hashtable;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.lang.sl.*;
import jade.onto.*;
import jade.onto.basic.*;
import jade.domain.FIPAException;

public class Runner extends Agent {
	
	public static final String ONE = "ONE";
	public static final String TWO = "TWO";
	public static final String THREE = "THREE";
	public static final String FOUR = "FOUR";
	
	public static final int FORWARD_EVENT = 10;
	public static final int BACKWARD_EVENT = 11;
	public static final int DONT_CHANGE_EVENT = 0;
	
	public static final int NO_DIRECTION = 0;
	public static final int FORWARD_DIRECTION = 1;
	public static final int BACKWARD_DIRECTION = -1;
	
	//private ACLContentManager contentManager;
	private int direction;
	private long timeSlot = 2000;
	
	private Hashtable myBehaviours = new Hashtable();
	
	public void setup() {
		//contentManager = new ACLContentManager();
		// register the SL0 content language
		//contentManager.registerLanguage(SL0Codec.NAME, new SL0Codec());
		// register the mobility ontology
		//contentManager.registerOntology(TestOntology.NAME, TestOntology.instance());

		direction = FORWARD_DIRECTION;
		
		FSMBehaviour runnerBehaviour = new FSMBehaviour(this);
		
		runnerBehaviour.registerFirstState(new SimpleRunnerState(this), ONE);
		runnerBehaviour.registerState(new SimpleRunnerState(this), TWO);
		runnerBehaviour.registerState(new SimpleRunnerState(this), THREE);
		runnerBehaviour.registerState(new ComplexRunnerState(this, "B1."+FOUR), FOUR);
		
		runnerBehaviour.registerTransition(ONE, TWO, FORWARD_EVENT);
		runnerBehaviour.registerTransition(ONE, FOUR, BACKWARD_EVENT);
		runnerBehaviour.registerTransition(TWO, THREE, FORWARD_EVENT);
		runnerBehaviour.registerTransition(TWO, ONE, BACKWARD_EVENT);
		runnerBehaviour.registerTransition(THREE, FOUR, FORWARD_EVENT);
		runnerBehaviour.registerTransition(THREE, TWO, BACKWARD_EVENT);
		runnerBehaviour.registerTransition(FOUR, ONE, FORWARD_EVENT);
		runnerBehaviour.registerTransition(FOUR, THREE, BACKWARD_EVENT);
		runnerBehaviour.registerDefaultTransition(ONE, ONE);
		runnerBehaviour.registerDefaultTransition(TWO, TWO);
		runnerBehaviour.registerDefaultTransition(THREE, THREE);
		runnerBehaviour.registerDefaultTransition(FOUR, FOUR);
		
		addBehaviour(runnerBehaviour);
		
		// Store into myBehaviours
		myBehaviours.put("B1", runnerBehaviour);
		myBehaviours.put("B1."+ONE, runnerBehaviour.getState(ONE));		
		myBehaviours.put("B1."+TWO, runnerBehaviour.getState(TWO));		
		myBehaviours.put("B1."+THREE, runnerBehaviour.getState(THREE));		
		myBehaviours.put("B1."+FOUR, runnerBehaviour.getState(FOUR));
		
		// Add the behaviour processing incoming messages
		Behaviour b1 = new CommandReceiverBehaviour(this);
		addBehaviour(b1);
	}
	
	private int computeResult() {
		int result = -1;
		switch (direction) {
		case NO_DIRECTION:
			result = DONT_CHANGE_EVENT;
			break;
		case FORWARD_DIRECTION:
			result = FORWARD_EVENT;
			break;
		case BACKWARD_DIRECTION:
			result = BACKWARD_EVENT;
			break;
		}
		return result;
	}
	
	class SimpleRunnerState extends OneShotBehaviour {
		public SimpleRunnerState(Agent a) {
			super(a);
		}
		
		public void action() {
			String myName = ((FSMBehaviour) parent).getName(this);
			System.out.println(myName);
			
			try {
				Thread.sleep(timeSlot);
			}
			catch (InterruptedException ie) {
			}
		}
		
		public int onEnd() {
			return computeResult();
		}
	}
	
	class ComplexRunnerState extends ParallelBehaviour {
		private String myName;
		
		public ComplexRunnerState(Agent a, String n) {
			super(a, 0);
		
			myName = n;
			
			Behaviour b;
			String name;
			
			name = new String(myName + ".Counter");
			b = new CountUntil10Behaviour(a, name);
			addSubBehaviour(b);
			myBehaviours.put(name, b);
				
			name = new String(myName + ".Receiver");
			b = new ReceiveTwoInformBehaviour(a, name);
			addSubBehaviour(b);
			myBehaviours.put(name, b);
		}
		
		protected void preAction() {
			String myName = ((FSMBehaviour) parent).getName(this);
			System.out.println(myName);
		}
		
		protected void postAction() {
			System.out.println("");
			reset();
		}
		
		public int onEnd() {
			super.onEnd();
			return computeResult();
		}
	}
			
	class CountUntil10Behaviour extends SequentialBehaviour {
		private String myName;
		
		public CountUntil10Behaviour(Agent a, String n) {
			super(a);
		
			myName = n;
			
			addSubBehaviour(new PrintStringBehaviour(a, "1")); 
			addSubBehaviour(new PrintStringBehaviour(a, "2")); 
			addSubBehaviour(new PrintStringBehaviour(a, "3")); 
			addSubBehaviour(new PrintStringBehaviour(a, "4")); 
			addSubBehaviour(new PrintStringBehaviour(a, "5"));
			addSubBehaviour(new PrintStringBehaviour(a, "6"));
			addSubBehaviour(new PrintStringBehaviour(a, "7")); 
			addSubBehaviour(new PrintStringBehaviour(a, "8")); 
			addSubBehaviour(new PrintStringBehaviour(a, "9")); 
			addSubBehaviour(new PrintStringBehaviour(a, "10")); 
		}
	}
	
	class ReceiveTwoInformBehaviour extends FSMBehaviour {
		private String myName;
		
		public ReceiveTwoInformBehaviour(Agent a, String n) {
			super(a);
			
			myName = n;
			
			registerFirstState(new ReceiveInformBehaviour(a), ONE);
			registerLastState(new ReceiveInformBehaviour(a), TWO);
			
			registerDefaultTransition(ONE, TWO);
		}
	}
	
	class PrintStringBehaviour extends OneShotBehaviour {
		private String toPrint;
		
		public PrintStringBehaviour(Agent a, String s) {
			super(a);
			toPrint = s;
		}
		
		public void action() {
			System.out.print(toPrint);
			try {
				Thread.sleep(timeSlot);
			}
			catch (InterruptedException ie) {
			}
			
		}
	}

	class ReceiveInformBehaviour extends SimpleBehaviour {
		private boolean ok;
		
		public ReceiveInformBehaviour(Agent a) {
			super(a);
		}
		
		public void action() {
			
			ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			if (msg == null) {
				block();
				ok = false;
			}
			else {
				System.out.println("INFORM Received");
				ok = true;
			}
		}
		
		public boolean done() {
			return ok;
		}
	}
	
	class CommandReceiverBehaviour extends CyclicBehaviour {
		public CommandReceiverBehaviour(Agent a) {
			super(a);
		}
		
		public void action() {
			
	 		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String c = msg.getContent();
				if (c.startsWith("\"")) {
					c = c.substring(1, c.length()-1);
				}
				
				if (c.equals("forward")) {
					System.out.println("Runner will move forward");
					((Runner) myAgent).direction = FORWARD_DIRECTION;
				}
				else if (c.equals("backward")) {
					System.out.println("Runner will move backward");
					((Runner) myAgent).direction = BACKWARD_DIRECTION;
				}
				else if (c.equals("stop")) {
					System.out.println("Runner will stop moving");
					((Runner) myAgent).direction = NO_DIRECTION;
				}
				else if (c.startsWith("block")) {
					String name = c.substring(c.indexOf(' ')).trim();
					if (name != null) {	
						System.out.print("Try to block behaviour " + name + "...");
						Behaviour b = (Behaviour) myBehaviours.get(name);
						if (b != null) {
							System.out.println("Behaviour found!");
							b.block();
						}
						else {
							System.out.println("Behaviour NOT found!");
						}	
					}
				}	
				else if (c.startsWith("tmpblock")) {
					String name = c.substring(c.indexOf(' ')).trim();
					if (name != null) {	
						System.out.print("Try to temporary block behaviour " + name + "...");
						Behaviour b = (Behaviour) myBehaviours.get(name);
						if (b != null) {
							System.out.println("Behaviour found!");
							b.block(10000);
						}
						else {
							System.out.println("Behaviour NOT found!");
						}	
					}
				}	
				else if (c.startsWith("restart")) {
					String name = c.substring(c.indexOf(' ')).trim();
					if (name != null) {	
						System.out.print("Try to restart behaviour " + name + "...");
						Behaviour b = (Behaviour) myBehaviours.get(name);
						if (b != null) {
							System.out.println("Behaviour found!");
							b.restart();
						}
						else {
							System.out.println("Behaviour NOT found!");
						}	
					}
				}	
			}
			else {
				block();
			}	
		}
	}
}