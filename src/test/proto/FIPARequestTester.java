package test.proto;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.*;

public class FIPARequestTester extends Agent {
	
	public void setup() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		Behaviour b = new MyResponder(this, mt);
		addBehaviour(b);
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(getAID());
		b = new MyInitiator(this, request);
		addBehaviour(b);
	}
	
	/**
	   Inner class MyResponder
	 */
	class MyResponder extends FIPARequestResponder {
		public MyResponder(Agent a, MessageTemplate mt) {
			super(a, mt, null);
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException, NotUnderstoodException {
			ACLMessage response = request.createReply();
			response.setPerformative(ACLMessage.AGREE);
			response.setProtocol("FIPA-Request");
			System.out.println("Send respoonse: AGREE");
			return response;
		}
		
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			ACLMessage resNot = request.createReply();
			resNot.setPerformative(ACLMessage.INFORM);
			resNot.setProtocol("FIPA-Request");
			System.out.println("Send result notification: INFORM");
			return resNot;
		}
	} // End of inner class MyResponder
	
	/**
	   Inner class MyInitiator
	 */
	class MyInitiator extends FIPARequestInitiator {
		public MyInitiator(Agent a, ACLMessage req) {
			super(a, req, null);
		}
		
		protected void handleAgree(ACLMessage agree) {
			System.out.println("Received AGREE");
		}
		protected void handleRefuse(ACLMessage refuse) {
			System.out.println("Received REFUSE");
		}
		protected void handleNotUnderstood(ACLMessage notUnderstood) {
			System.out.println("Received NOT_UNDERSTOOD");
		}
		protected void handleInform(ACLMessage inform) {
			System.out.println("Received INFORM");
		}
		protected void handleAllResponses(List allResponses) {
			System.out.println("List of responses:");
			Iterator it = allResponses.iterator();
			while(it.hasNext()) {
				ACLMessage rsp = (ACLMessage) it.next();
				System.out.println(ACLMessage.getPerformative(rsp.getPerformative()));
			}
		}
		protected void handleAllResultNotifications(List allResultNotifications) {
			System.out.println("List of result notifications:");
			Iterator it = allResultNotifications.iterator();
			while(it.hasNext()) {
				ACLMessage rsp = (ACLMessage) it.next();
				System.out.println(ACLMessage.getPerformative(rsp.getPerformative()));
			}
		}
	} // End of inner class MyInitiator
			
}
