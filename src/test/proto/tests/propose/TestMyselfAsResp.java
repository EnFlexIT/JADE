package test.proto.tests.propose;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import test.common.*;
import test.proto.tests.TestBase;
import test.proto.responderBehaviours.propose.*;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
 * @author Jerome Picault - Motorola Labs
 */
public class TestMyselfAsResp extends TestBase {
	public static final String TEST_NAME = "Initiator and responder in the same agent";
	private Behaviour b;
	
	public TestMyselfAsResp() {
		responderBehaviours= new String[] {
			"test.proto.responderBehaviours.propose.AcceptProposalReplier"
		};
	}
	
  public String getName() {
  	return TEST_NAME;
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	ds.put(resultKey, new Integer(TEST_FAILED));
 
  	ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
 		initialize(a, msg);
 		b = new AcceptProposalReplier();
 		a.addBehaviour(b);
 		msg.addReceiver(a.getAID());
  	
  	return new BasicProposeInitiator(a, msg, ds, resultKey, 10000, 
  		new int[] {2, 0, 0, 0}); // 2 ACCEPT_PROPOSAL
  }  
  
  public void clean(Agent a) {
  	super.clean(a);
  	a.removeBehaviour(b);
  }
}

