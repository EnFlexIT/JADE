package test.proto.tests.propose;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPANames;
import test.common.*;
import test.proto.tests.TestBase;

import java.util.Date;
import java.util.Vector;

/**
 * @author Jerome Picault - Motorola Labs
 */
public class BasicProposeInitiator extends ProposeInitiator {

	protected int acceptProposalCnt = 0;
	protected int rejectProposalCnt = 0;
	protected int notUnderstoodCnt = 0;
	protected int outOfSeqCnt = 0;
	
	private String key;
	private long waitingTime;
  private int[] expected;
  
	public BasicProposeInitiator(Agent a, ACLMessage msg, DataStore ds, String key, long waitingTime, int[] expected) {
		super(a, msg, ds);
		this.key = key;
		this.waitingTime = waitingTime;
		if (expected.length != 4) {
			throw new IllegalArgumentException("4 expected number of messages must be specified");
		}
		this.expected = expected;
	}
	
  protected Vector prepareInitiations(ACLMessage request) {
		Vector v = new Vector(1);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
  	request.setReplyByDate(new Date((new Date()).getTime() + waitingTime));
		v.addElement(request);
		return v;
  }
  		
  		
  protected void handleAcceptProposal(ACLMessage accept) {
  	TestUtility.log("ACCEPT_PROPOSAL received. Message is");
  	TestUtility.log(accept);
  	acceptProposalCnt++;
  }

  protected void handleRejectProposal(ACLMessage reject) {
  	TestUtility.log("REJECT_PROPOSAL received. Message is");
  	TestUtility.log(reject);
  	rejectProposalCnt++;
  }
  		
  protected void handleNotUnderstood(ACLMessage notUnderstood) {
  	TestUtility.log("NOT_UNDERSTOOD received. Message is");
  	TestUtility.log(notUnderstood);
  	notUnderstoodCnt++;
	}
  		  		
  protected void handleOutOfSequence(ACLMessage msg) {
  	TestUtility.log("OUT_OF_SEQUENCE received. Message is");
  	TestUtility.log(msg);
  	outOfSeqCnt++;
  }
  		
  protected void handleAllResponses(Vector responses) {
 	if (check()) {
    printDetails();
 		getDataStore().put(key, new Integer(Test.TEST_PASSED));
  }
  	else {
  		printDetails();
  		getDataStore().put(key, new Integer(Test.TEST_FAILED));
  	} 
  }
  	  		
	public boolean check() {
  	if (
  		(acceptProposalCnt == expected[0]) &&
  		(rejectProposalCnt == expected[1]) &&
  		(notUnderstoodCnt == expected[2]) &&
  		(outOfSeqCnt == expected[3]) ) {
  		return true;
 		}	
 		else {
 			return false;
 		}
	}
	
	public void printDetails() {
  	System.out.println("ACCEPT_PROPOSAL: expected "+expected[0]+", received "+acceptProposalCnt);  
  	System.out.println("REJECT_PROPOSAL: expected "+expected[1]+", received "+rejectProposalCnt);  
  	System.out.println("NOT_UNDERSTOOD: expected "+expected[2]+", received "+notUnderstoodCnt);  
  	System.out.println("OUT OF SEQUENCE: expected "+expected[3]+", received "+outOfSeqCnt);
  }
}
