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
 * @author Jerome Picault - Motorola Labs
 */
public class TestTimeoutAndOutOfSeq extends TestBase {
	public static final String TEST_NAME = "Timeout and Out-of-sequence";
	private Behaviour b;
	
	public TestTimeoutAndOutOfSeq() {
		responderBehaviours= new String[] {
			"test.proto.responderBehaviours.propose.AcceptProposalReplier",
			"test.proto.responderBehaviours.propose.RejectProposalReplier",
			"test.proto.responderBehaviours.propose.NotUnderstoodReplier",
     	"test.proto.responderBehaviours.propose.RequestReplier",
      "test.proto.responderBehaviours.propose.NullReplier"
		};
	}
	
  public String getName() {
  	return TEST_NAME;
  }
  
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	ds.put(resultKey, new Integer(TEST_FAILED));
 
  	ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
 		initialize(a, msg);
  	
  	return new BasicProposeInitiator(a, msg, ds, resultKey, 10000, 
                                     new int[] {1, 1, 1, 1}); // 1 ACCEPT_PROPOSAL, 1 REJECT_PROPOSAL, 1 NOT_UNDERSTOOD, 1 OUT_OF_SEQ
  }  
  
}
