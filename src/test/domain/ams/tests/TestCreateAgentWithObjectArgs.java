package test.domain.ams.tests;

import test.common.*;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.*;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Test requesting the AMS to create an agent with a Serializable object as argument. 
 */
public class TestCreateAgentWithObjectArgs extends Test {
	private static final String A_STRING = "astring";
	private static final Integer AN_INTEGER = new Integer(1234);
	
	private AID agentID;
	
	public Behaviour load(Agent a) throws TestException {
		setTimeout(10000);
		
		return new Behaviour(a) {
			private boolean finished = false;
			private MessageTemplate template = null;
			
			public void onStart() {
				String convId = myAgent.getName()+System.currentTimeMillis();
				template = MessageTemplate.MatchConversationId(convId);
				List l = new ArrayList();
				l.add(A_STRING);
				l.add(AN_INTEGER);
				try {
					log("Creating Serializable-argument Agent");
					agentID = TestUtility.createAgent(myAgent, "dummy", TestCreateAgentWithObjectArgs.this.getClass().getName()+"$SerializableArgAgent", new Object[]{l, myAgent.getAID(), convId});
					log("Serializable-argument Agent correctly created");
				}
				catch (TestException te) {
					failed("Error creating agent with Serializable argument. "+te);
					te.printStackTrace();
					finished = true;
				}
			}
			
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						log("Message from Serializable-argument Agent received");
						try {
							Object obj = msg.getContentObject();
							if (check(obj)) {
								passed("Serializable argument OK");
							}
						}
						catch (Exception e) {
							failed("Cannot read message content. "+e);
							e.printStackTrace();
						}
					}
					else {
						failed(msg.getContent());
					}
					finished = true;
				}
				else {
					 block();
				}
			}
			
			public boolean done() {
				return finished;
			}
		};
	}
	
	public void clean(Agent a) {
		if (agentID != null) {
			try {
				TestUtility.killAgent(a, agentID);
			}
			catch (TestException te) {
				te.printStackTrace();
			}
		}
	}
	
	private boolean check(Object obj) {
		if (obj == null) {
			failed("Null serializable argument");
			return false;
		} 
		else {
			if (obj instanceof List) {
				List l = (List) obj;
				if (l.size() == 2) {
					if (!A_STRING.equals(l.get(0))) {
						failed("Wrong serializable argument: " + l.get(0) + " found while " + A_STRING + " was expected");
						return false;
					}
					if (!AN_INTEGER.equals(l.get(1))) {
						failed("Wrong serializable argument: " + l.get(1) + " found while " + AN_INTEGER + " was expected");
						return false;
					}
				} 
				else {
					failed("Wrong serializable argument: " + l.size() + " elements found while " + 2 + " were expected");
					return false;
				}
			} 
			else {
				failed("Wrong serializable argument type: " + obj.getClass().getName() + " found while java.util.List was expected");
				return false;
			}
		}
		return true;
	}

	
	/**
	 * Inner class SerializableArgAgent
	 */
	public static class SerializableArgAgent extends Agent {
		protected void setup() {
			Object[] args = getArguments();
			if (args != null && args.length == 3) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				AID id = (AID) args[1];
				msg.addReceiver(id);
				String convId = (String) args[2];
				msg.setConversationId(convId);
				try {
					msg.setContentObject((Serializable) args[0]);
				}
				catch (Exception e) {
					msg.setPerformative(ACLMessage.FAILURE);
					msg.setContent(e.toString());
				}
				send(msg);
			}
			else {
				System.out.println("Worng arguments!!!!!!!!! "+args);
			}
		}
	}
}
