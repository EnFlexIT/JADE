package test.proto;

import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.*;

public class FIPARequestInitiatorTest extends Agent {

    ACLMessage createNewMessage(){

	ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
	request.setProtocol(jade.proto.FIPAProtocolNames.FIPA_REQUEST);
	//set the receivers according to the parameter setted.
	Object[] receivers = getArguments();
	for (int i=0; i<receivers.length; i++){
	    // System.out.println( "receiver: " + receivers[i]);
	    request.addReceiver(new AID((String)receivers[i],false));
	}
	
	//set the timeout to 2 minutes.
	long timeout = System.currentTimeMillis() + 10000;
	request.setReplyByDate(new java.util.Date(timeout));
	return request;
    }
	
	public void setup() {
	   
		Behaviour b;
	

		ACLMessage request = createNewMessage();
		//System.out.println(request);
		b = new MyInitiator(this, request);
		addBehaviour(b);
	}
	
	/**
	   Inner class MyInitiator
	 */
    class MyInitiator extends AchieveREInitiator {
	public MyInitiator(Agent a, ACLMessage req) {
	    super(a, req);
	}
		
	protected void handleAgree(ACLMessage agree) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleAgree: " + agree);
	}
	
	protected void handleRefuse(ACLMessage refuse) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleRefuse: " + refuse);
	}
	protected void handleFailure(ACLMessage failure) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleFailure: " + failure);
	}
	protected void handleNotUnderstood(ACLMessage notUnderstood) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleNotUnderstood: " + notUnderstood);
	}
	protected void handleInform(ACLMessage inform) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleInform: " + inform);
	}
	protected void handleAllResponses(List allResponses) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleAllResponses");
	    System.out.println("handleAllResponses - List of responses:");
	    Iterator it = allResponses.iterator();
	    while(it.hasNext()) {
		ACLMessage rsp = (ACLMessage) it.next();
		System.out.println(ACLMessage.getPerformative(rsp.getPerformative()));
	    }
	}
	protected void handleAllResultNotifications(List allResultNotifications) {
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleAllResultNotification");
	    System.out.println("handle All Result Notification - List of result notifications:");
	    Iterator it = allResultNotifications.iterator();
	    while(it.hasNext()) {
		ACLMessage rsp = (ACLMessage) it.next();
		System.out.println(ACLMessage.getPerformative(rsp.getPerformative()));
	    }
	}
	protected void handleOutOfSequence(ACLMessage msg){
	    System.out.println("Agent: " + myAgent.getLocalName()+ " in handleOutOfSequence: " + msg);
	}

	public int onEnd(){
	    try{
		System.out.println("\nPress a Key to repeat the protocol: ");
		BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
		String reply = buff.readLine();
	  // blockingReceive(10000);
		reset(createNewMessage());
		myAgent.addBehaviour(this);
	    }catch(Exception e){
		e.printStackTrace();
	    }
	    return super.onEnd();
	}
	
    } // End of inner class MyInitiator
    
}
