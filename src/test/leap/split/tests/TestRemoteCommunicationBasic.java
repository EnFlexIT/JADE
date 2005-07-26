package test.leap.split.tests;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementVocabulary;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import test.common.*;

/**
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public class TestRemoteCommunicationBasic extends Test{
	
	private JadeController jcR = null;
	private JadeController jcCHTTP = null;
	private JadeController jcC = null;
	
	private static final String REMOTE_PLATFORM_NAME = "Remote-platform";
	private static final int REMOTE_PLATFORM_PORT = 9003;
	static final String PING_NAME = "ping";
	static final String CONTROLLER_NAME="controller_";
	private String containerName;
	private AID remoteAMS;
	Agent myAgent;
	
	public Behaviour load(Agent a) throws TestException {  
		

		myAgent = a;
		//Launch a remote platform
		jcR = TestUtility.launchJadeInstance(REMOTE_PLATFORM_NAME, "+"+TestUtility.HTTP_MTP_CLASSPATH, new String("-name "+REMOTE_PLATFORM_NAME+" -port "+REMOTE_PLATFORM_PORT+" -mtp "+Test.DEFAULT_MTP+" "+TestUtility.HTTP_MTP_ARG), new String[]{Test.DEFAULT_PROTO}); 
		
		// Construct the AID of the AMS of the remote platform 
		remoteAMS = new AID("ams@"+REMOTE_PLATFORM_NAME, AID.ISGUID);
		Iterator it = jcR.getAddresses().iterator();
		while (it.hasNext()) {
			remoteAMS.addAddresses((String) it.next());
		}
		
	  //Launch another container with an MTP to communicate with the remote platform
		jcCHTTP = TestUtility.launchJadeInstance("Container-1", "+"+TestUtility.HTTP_MTP_CLASSPATH, new String("-container -host "+TestUtility.getContainerHostName(a, null)+" -port "+Test.DEFAULT_PORT+" -mtp "+Test.DEFAULT_MTP+" "+TestUtility.HTTP_MTP_ARG), null);
		
		SequentialBehaviour sb = new SequentialBehaviour(a);
		
		//Step 1: Initialization phase of the split-container
		sb.addSubBehaviour(new OneShotBehaviour(a) {
  		public void action() {
  			try {
					containerName = createSplitContainer();
  			}
  			catch (Exception e) {
  				failed("Error initilizing split-container. " + e);
  				e.printStackTrace();
  			}
  		}
  	});
		
		//Step 2: launch ping agent on remote platform
		sb.addSubBehaviour(new OneShotBehaviour(a){
			public void action(){
				try{
					log("Starting ping agent on " + jcR.getContainerName());
					//Launch an agent on the remote platform. 
					AID pingAgent = TestUtility.createAgent(myAgent, PING_NAME, "test.leap.split.tests.TestRemoteCommunicationBasic$PingAgent", null, remoteAMS, null);
					log("Ping agent named: " + pingAgent.getLocalName() + " correctly started");
				}
				catch(TestException e){
					failed("Error starting Ping agent. " + e);
					e.printStackTrace();
				}
			}
		});
		
		//Step 3: launch controller agent on split-container
		sb.addSubBehaviour(new OneShotBehaviour(a){
			public void action(){
				try{
					log("Starting ping agent on " + containerName);
					String[] addresses = remoteAMS.getAddressesArray();
					String[] args = new String[addresses.length + 1];
					//1-arg: the name of the tester agent to send back the results.
					args[0] = myAgent.getAID().getLocalName();
					//-args the addresses of the remote platform where the ping agent is running.
					int j = 1;
					for(int i=0; i<addresses.length; i++){
						args[j] = addresses[i];
						j = j+1;
					}
					TestUtility.createAgent(myAgent, CONTROLLER_NAME + JADEManagementVocabulary.CONTAINER_WILDCARD,  "test.leap.split.tests.TestRemoteCommunicationBasic$ControllerAgent", args, null, containerName);
					log("Controller agent started on " + containerName);
				}
				catch(Exception e){
					failed("Error starting Controller agent. " + e);
					e.printStackTrace();
				}
			}
		});
		
		//Step 4: Collects INFORM messages from the controller
  	Behaviour collector = new SimpleBehaviour(a) {
  		private boolean finished = false;
  		
  		public void action() {
  			//waits for messages from controller
  			ACLMessage msg = myAgent.receive();
  			AID controllerAID = new AID(CONTROLLER_NAME + containerName, AID.ISLOCALNAME);
  			if (msg != null) {
  				if(msg.getSender().equals(controllerAID)){
  					//received message from controller
	  				if(msg.getPerformative() == ACLMessage.INFORM){
	  					//test successful
	  					passed("Remote communication successful. " + msg.getContent());
	  				}else{
	  					//test failed
	  					failed("FAILURE: Remote communication failed. " + msg.getContent());
	  				}
  				}else{
  					//received message from unexpected agent
  					failed("FAILURE: Received message from unexpected agent " + msg.getSender() + ". " + msg.toString());
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
		sb.addSubBehaviour(collector);
		
	
  	return sb;
	}
	
	/**
	 * Override this method to initialize the environment for the test.
	 * @return name the name of the split-container created.
	 * @throws TestException
	 */
	protected String createSplitContainer() throws TestException{
		log("Creating split container...");
		jcC = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)));
		log("split-container created successfully !");
		return jcC.getContainerName();
	}
	
	
	protected void killSplitContainer(Agent a){
		try{
			TestUtility.killContainer(a, jcC.getContainerName());
		}catch(TestException te){
			te.printStackTrace();
		}
		if(jcC != null) {
	  	jcC.kill();
  	}
	}
	
	public void clean(Agent a) {
		
		killSplitContainer(a);
		
		if(jcCHTTP != null){
			jcCHTTP.kill();
		}
		if(jcR != null){
			jcR.kill();
		}
	}

	
	/**
	 Inner class PingAgent
	 */
	public static class PingAgent extends Agent {
		protected void setup() {
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					ACLMessage msg = myAgent.receive();
					if (msg != null) {
						if(msg.getPerformative() == ACLMessage.REQUEST){
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							myAgent.send(reply);
						}
					}
					else {
						block();
					}
				}
			} );
		}
	} // END of inner class PingAgent
	
	public static class ControllerAgent extends Agent{
		
		String testerName;
		AID pingAgent;
		
		protected void setup(){
			
			Object args[] = getArguments();
			if(args != null){
				testerName = (String)args[0];
				pingAgent = new AID(PING_NAME + '@' + REMOTE_PLATFORM_NAME, AID.ISGUID);
				for(int i=1; i<args.length; i++){
					pingAgent.addAddresses((String)args[i]);
				}
			}
			//send a message to the remote ping agent.
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(pingAgent);
			send(request);
		
			addBehaviour(new CyclicBehaviour(this) {
				public void action() {
					
					ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
					AID testerAID = new AID(testerName, AID.ISLOCALNAME);
					reply.addReceiver(testerAID);
					
					ACLMessage msg = myAgent.blockingReceive(5000);
					if (msg != null) {
						if(msg.getSender().equals(pingAgent)){
							if(msg.getPerformative() == ACLMessage.INFORM){
								reply.setPerformative(ACLMessage.INFORM);	
								reply.setContent("Controller received an INFORM from remote ping agent as expected.");
							}
						}else{
							reply.setPerformative(ACLMessage.FAILURE);
							reply.setContent("Controller received unexpected: " + ACLMessage.getPerformative(msg.getPerformative()) + " from: " + msg.getSender().toString());
						}
					}else{
						//se è null allora è uscito dalla blocking senza ricevere il messaggio.
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("Controller does not receive any message from ping agent.");
					}
					myAgent.send(reply);
				}
			});
		}
	}//end class ControllerAgent
}
