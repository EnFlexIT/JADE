package test.proto.tests.contractNet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import test.common.TestException;
import test.proto.tests.TestBase;

public class TestMoreAcceptances extends TestBase {
	
	private Map<String, ACLMessage> receiverResponseMap = new HashMap<String, ACLMessage>();
	private int currentReceiverIndex = 0;
	private int handleResultNotificationsCallCnt = 0;

	public TestMoreAcceptances() {
		responderBehaviours= new String[] {
				"test.proto.responderBehaviours.contractNet.ProposeFailureReplier",
				"test.proto.responderBehaviours.contractNet.ProposeInformReplier",
				"test.proto.responderBehaviours.contractNet.ProposeInformReplier",
				"test.proto.responderBehaviours.contractNet.ProposeInformReplier"
		};
	}

	@Override
	public Behaviour load(Agent a) throws TestException {
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		initialize(a, cfp);
		return new ContractNetInitiator(a, cfp) {
			@Override
			public void handleAllResponses(Vector responses, Vector acceptances) {
				for (Object obj : responses) {
					ACLMessage resp = (ACLMessage) obj;
					if (resp.getPerformative() == ACLMessage.PROPOSE) {
						log("--- PROPOSE received from agent "+resp.getSender().getLocalName()+" as expected");
						receiverResponseMap.put(resp.getSender().getLocalName(), resp);
					}
					else {
						failed("--- Unexpected "+ACLMessage.getPerformative(resp.getPerformative())+" received from agent "+resp.getSender().getLocalName()+". PROPOSE was expected");
						return;
					}
				}
				
				ACLMessage receiver1Resp = receiverResponseMap.get(RESPONDER_NAME+currentReceiverIndex);
				ACLMessage accept = receiver1Resp.createReply();
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				acceptances.add(accept);
			}
			
			@Override
			public void handleFailure(ACLMessage failure) {
				if (currentReceiverIndex == 0) {
					log("--- FAILURE received from agent "+failure.getSender().getLocalName()+" as expected");
					
					Vector nextAcceptances = new Vector();
					currentReceiverIndex++;
					ACLMessage nextReceiverResp = receiverResponseMap.get(RESPONDER_NAME+currentReceiverIndex);
					ACLMessage accept = nextReceiverResp.createReply();
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					nextAcceptances.add(accept);
					moreAcceptances(nextAcceptances);
				}
				else {
					failed("--- Unexpected FAILURE received from agent "+failure.getSender().getLocalName());
				}
			}
			
			@Override
			public void handleInform(ACLMessage inform) {
				if (currentReceiverIndex == 1) {
					log("--- INFORM received from agent "+inform.getSender().getLocalName()+" as expected");
					
					Vector nextAcceptances = new Vector();
					while (currentReceiverIndex < 3) {
						currentReceiverIndex++;
						ACLMessage nextReceiverResp = receiverResponseMap.get(RESPONDER_NAME+currentReceiverIndex);
						ACLMessage accept = nextReceiverResp.createReply();
						accept.setPerformative(ACLMessage.REJECT_PROPOSAL);
						nextAcceptances.add(accept);
					}
					moreAcceptances(nextAcceptances);
				}
				else {
					failed("--- Unexpected INFORM received from agent "+inform.getSender().getLocalName());
				}
			}
			
			@Override
			public void handleAllResultNotifications(Vector resultNotifications) {
				handleResultNotificationsCallCnt++;
				switch (handleResultNotificationsCallCnt) {
				case 1:
				case 2:
					if (resultNotifications.size() != 1) {
						failed("--- Wrong number of result-notifications at round "+handleResultNotificationsCallCnt+". "+resultNotifications.size()+" received while 1 was expected");
					}
					break;
				case 3:
					if (resultNotifications.size() != 0) {
						failed("--- Wrong number of result-notifications at round 3. "+resultNotifications.size()+" received while 0 was expected");
					}
					break;
				default:
					failed("--- Unexpected call ("+handleResultNotificationsCallCnt+") to handleAllResultNotifications()");
				}
			}
			
			public int onEnd() {
				int ret = super.onEnd();
				if (!TestMoreAcceptances.this.isFailed()) {
					passed("Test OK");
				}
				return ret;
			}
		};
	}
}
