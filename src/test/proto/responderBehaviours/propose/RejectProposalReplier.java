package test.proto.responderBehaviours.propose;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;

import test.common.TestUtility;

/**
   @author Jerome Picault - Motorola Labs
 */
public class RejectProposalReplier extends ProposeResponder {
	public RejectProposalReplier() {
		super(null, createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE));
	}
	
  protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		TestUtility.log(myAgent.getName()+": prepare response");
  	ACLMessage response = request.createReply();
		response.setPerformative(ACLMessage.REJECT_PROPOSAL);
		return response;
  }
}
