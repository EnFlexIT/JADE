package test.mobility.separate.dummy;

import java.net.URL;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class DummyAgent extends Agent {
	public static final String DUMMY_RESOURCE_NAME = "test/mobility/separate/dummy/dummyResource.txt";
	
	private DummyClass myDummyObject;
	private AID tester;
	
	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			tester = (AID) args[0];
		}
		
		myDummyObject = new DummyClass();
		
		// Try loading a resource from the separate jar file and notify the tester
		loadDummyResource();
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				block();
			}
		});
	}
	
	private void loadDummyResource() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM); 
		msg.addReceiver(tester);
		URL dummyUrl = getClass().getClassLoader().getResource(DUMMY_RESOURCE_NAME);
		if (dummyUrl == null) {
			// Dummy resource not found!!!
			msg.setPerformative(ACLMessage.FAILURE);
			msg.setContent("Dummy resource "+DUMMY_RESOURCE_NAME+" not found in separate jar file");
		}
		else {
			// Try to read the resource
			try {
				byte[] buffer = new byte[10];
				dummyUrl.openStream().read(buffer);
				System.out.println("Bytes read from dummy resource = "+new String(buffer));
			}
			catch (Exception e) {
				e.printStackTrace();
				msg.setPerformative(ACLMessage.FAILURE);
				msg.setContent("Dummy resource "+DUMMY_RESOURCE_NAME+" cannot be read. "+e);
			}
		}
		send(msg);
	}
}
