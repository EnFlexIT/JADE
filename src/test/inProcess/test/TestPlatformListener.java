package test.inProcess.test;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.AID;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.*;
import jade.content.onto.basic.*;
import jade.domain.JADEAgentManagement.*;
import jade.util.leap.*;
import test.common.*;

/**
   
   @author Giovanni Caire - TILAB
 */
public class TestPlatformListener extends Test {
	public static final String HELPER_NAME = "helper";
	public static final String TEST_NAME = "test";
	
	public static final String CONV_ID = "TPL-conv-id";
	
	private ContainerController cc;
	private AID helper = null;
	private AID test = null;
	
	
	public Behaviour load(Agent a) throws TestException {
		setTimeout(30000);
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		// Launch a helper agent on the Main-Container. This
		// will get the Platform Controller and register a 
		// PlatformListener
		Behaviour b = new OneShotBehaviour(a) {
			public void action() {
				try {
					log("Launching helper agent on the Main Container...");
					helper = TestUtility.createAgent(myAgent, HELPER_NAME, HelperAgent.class.getName(), new String[]{myAgent.getLocalName()}, myAgent.getAMS(), AgentContainer.MAIN_CONTAINER_NAME);
					log("Helper agent OK");
				}
				catch (TestException te) {
					failed("Error launching helper agent. "+te);
					te.printStackTrace();
				}
			}
		};
		sb.addSubBehaviour(b);
		
		// Wait until the helper agent has registered its platform listener
		b = new SimpleBehaviour(a) {
			private boolean finished = false;
			private MessageTemplate template = MessageTemplate.MatchConversationId(CONV_ID);
			
			public void onStart() {
				log("Waiting for PlatformListener registration...");
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						log("PlatformListener registration OK.");
					}
					else {
						failed("Error registering PlatformListener. "+msg.getContent());
					}
					finished = true;
				}
			}
			
			public boolean done() {
				return finished;
			}
		};
		sb.addSubBehaviour(b);
		
		// Start a new agent and wait for the notification from the
		// helper agent that should have detected that thanks to its 
		// PlatformListener
		b = new SimpleBehaviour(a) {
			private boolean finished = false;
			private MessageTemplate template = MessageTemplate.MatchConversationId(CONV_ID);
			
			public void onStart() {
				log("Starting a test agent...");
				try {
					test = TestUtility.createAgent(myAgent, TEST_NAME, "jade.core.Agent", null);
					log("Test agent startup OK. Waiting for helper notification...");
				}
				catch (TestException te) {
					failed("Error creating test agent. "+te);
					te.printStackTrace();
				}
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM && test.getName().equals(msg.getContent())) {
						log("PlatformListener correctly detected agent startup.");
					}
					else {
						failed("Error detecting agent startup. "+msg.getContent());
					}
					finished = true;
				}
			}
			
			public boolean done() {
				return finished;
			}
		};
		sb.addSubBehaviour(b);
		
		// Kill the test agent and wait for the notification from the
		// helper agent that should have detected that thanks to its 
		// PlatformListener
		b = new SimpleBehaviour(a) {
			private boolean finished = false;
			private MessageTemplate template = MessageTemplate.MatchConversationId(CONV_ID);
			
			public void onStart() {
				log("Killing test agent...");
				try {
					TestUtility.killAgent(myAgent, test);
					log("Test agent take down OK. Waiting for helper notification...");
				}
				catch (TestException te) {
					failed("Error killing test agent. "+te);
					te.printStackTrace();
				}
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM && test.getName().equals(msg.getContent())) {
						passed("PlatformListener correctly detected agent take down.");
					}
					else {
						failed("Error detecting agent take down. "+msg.getContent());
					}
					finished = true;
				}
			}
			
			public boolean done() {
				return finished;
			}
		};
		sb.addSubBehaviour(b);
		
		return sb;
	}
	
	public void clean(Agent a) {
		// Kill the helper agent
		if (helper != null) {
			try {
				TestUtility.killAgent(a, helper);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}			
	
	
	/**
	   Inner class HelperAgent
	 */
	public static class HelperAgent extends Agent {
		private AID myTester;
		private ACLMessage notification;
		private PlatformController myPlatformController;
		private PlatformController.Listener myPlatformListener;
		
		protected void setup() {
			Object[] args = getArguments();
			if (args != null && args.length == 1) {
				myTester = new AID((String) args[0], AID.ISLOCALNAME);
				notification = new ACLMessage(ACLMessage.INFORM);
				notification.addReceiver(myTester);
				notification.setConversationId(CONV_ID);
				
				try {
					ContainerController cc = getContainerController();
					myPlatformController = cc.getPlatformController();
					
					myPlatformListener = new PlatformController.Listener() { 
		        public void bornAgent(PlatformEvent anEvent) {
		        	notification.setContent(anEvent.getAgentGUID());
		        	send(notification);
		        }
		        public void deadAgent(PlatformEvent anEvent) {
		        	notification.setContent(anEvent.getAgentGUID());
		        	send(notification);
		        }
		        public void startedPlatform(PlatformEvent anEvent) {
		        }
		        public void suspendedPlatform(PlatformEvent anEvent) {
		        }
		        public void resumedPlatform(PlatformEvent anEvent) {
		        }
		        public void killedPlatform(PlatformEvent anEvent) {
		        }
				  };
					myPlatformController.addPlatformListener(myPlatformListener);
					System.out.println(getName()+": PlatformListener correctly registered.");
					send(notification);
				}
				catch (ControllerException ce) {
					System.out.println(getName()+": Error registering PlatformListener. "+ce);
					ce.printStackTrace();
				}	
			}
			else {
				System.out.println(getName()+": Wrong arguments "+args);
			}
		}
		
		protected void takeDown() {
			// Deregister the PlatformListener
			if (myPlatformController != null) {
				try {
					myPlatformController.removePlatformListener(myPlatformListener);
				}
				catch (ControllerException ce) {
					ce.printStackTrace();
				}
			}			
		}
	}  // END of inner class HelperAgent			
}
