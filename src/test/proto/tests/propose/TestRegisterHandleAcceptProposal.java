package test.proto.tests.propose;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import test.common.*;
import test.proto.tests.TestBase;
import test.proto.responderBehaviours.achieveRE.*;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
   @author Jerome Picault - Motorola Labs
 */
public class TestRegisterHandleAcceptProposal extends TestBase {
	public static final String TEST_NAME = "RegisterHandleAcceptProposal";
	public static final String ACCEPT_PROPOSAL_CNT_KEY = "accept-proposal-cnt";
	private Behaviour b;
	
	public TestRegisterHandleAcceptProposal() {
		responderBehaviours= new String[] {
			"test.proto.responderBehaviours.propose.AcceptProposalReplier",
			"test.proto.responderBehaviours.propose.AcceptProposalReplier",
			"test.proto.responderBehaviours.propose.AcceptProposalReplier"
		};
	}
	
  public String getName() {
  	return TEST_NAME;
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	ds.put(resultKey, new Integer(TEST_FAILED));
  	ds.put(ACCEPT_PROPOSAL_CNT_KEY, new Integer(0));
 
  	ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
 		initialize(a, msg);
  	
 		SequentialBehaviour acceptProposalHandler = new SequentialBehaviour(a);
 		Behaviour b1 = new OneShotBehaviour(a) {
 			public void action() {
  			TestUtility.log("ACCEPT_PROPOSAL received");
 				Integer cnt = (Integer) getDataStore().get(ACCEPT_PROPOSAL_CNT_KEY);
 				cnt = new Integer(cnt.intValue() + 1);
 				getDataStore().put(ACCEPT_PROPOSAL_CNT_KEY, cnt);
 			}
 		};
 		b1.setDataStore(ds);
 		acceptProposalHandler.addSubBehaviour(b1);
 		
 		Behaviour b2 = new OneShotBehaviour(a) {
 			public void action() {
 				System.out.println("Dummy");
 			}
 		};
 		acceptProposalHandler.addSubBehaviour(b2);
 		
  	ProposeInitiator b = new BasicProposeInitiator(a, msg, ds, resultKey, 10000, new int[] {3, 0, 0, 0}) { // 3 ACCEPT_PROPOSAL
			public boolean check() {
 				Integer cnt = (Integer) getDataStore().get(ACCEPT_PROPOSAL_CNT_KEY);
 				acceptProposalCnt = cnt.intValue();
 				return super.check();
			}
  	};
  	b.registerHandleAcceptProposal(acceptProposalHandler);
  	return b;
  }  
  
}

