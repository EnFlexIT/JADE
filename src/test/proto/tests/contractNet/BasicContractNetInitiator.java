/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package test.proto.tests.contractNet;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import test.common.*;
import test.proto.tests.TestBase;

import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

/**
   @author Giovanni Caire - TILAB
 */
public class BasicContractNetInitiator extends ContractNetInitiator {

	protected int informCnt = 0;
	protected int proposeCnt = 0;
	protected int notUnderstoodCnt = 0;
	protected int refuseCnt = 0;
	protected int failureCnt = 0;
	protected int outOfSeqCnt = 0;
	
	private String key;
	private long waitingTime;
  private int[] expected;
  
	public BasicContractNetInitiator(Agent a, ACLMessage msg, DataStore ds, String key, long waitingTime, int[] expected) {
		super(a, msg, ds);
		this.key = key;
		this.waitingTime = waitingTime;
		if (expected.length != 6) {
			throw new IllegalArgumentException("6 expected number of messages must be specified");
		}
		this.expected = expected;
	}
	
  protected Vector prepareCfps(ACLMessage cfp) {
		Vector v = new Vector(1);
		cfp.setProtocol(FIPAProtocolNames.FIPA_CONTRACT_NET);
  	cfp.setReplyByDate(new Date((new Date()).getTime() + waitingTime));
		v.addElement(cfp);
		return v;
  }
  		
  protected void handlePropose(ACLMessage propose, Vector acceptances) {
  	proposeCnt++;
  }
  		
  protected void handleRefuse(ACLMessage refuse) {
  	refuseCnt++;
  }
  		
  protected void handleNotUnderstood(ACLMessage notUnderstood) {
  	notUnderstoodCnt++;
	}
  		
  protected void handleInform(ACLMessage inform) {
  	informCnt++;
  }
  	
  protected void handleFailure(ACLMessage failure) {
  	failureCnt++;
  }
  		
  protected void handleOutOfSequence(ACLMessage msg) {
  	outOfSeqCnt++;
  }
  		
  // Reply with ACCEPT_PROPOSAL to all PROPOSE
  protected void handleAllResponses(Vector responses, Vector acceptances) {
  	Enumeration e = responses.elements();
  	while (e.hasMoreElements()) {
  		ACLMessage msg = (ACLMessage) e.nextElement();
  		if (msg.getPerformative() == ACLMessage.PROPOSE) {
  			ACLMessage rsp = msg.createReply();
  			rsp.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
  			rsp.setReplyByDate(new Date((new Date()).getTime() + waitingTime)); 
  			acceptances.addElement(rsp);
  		}
  	}
  }
  		
  protected void handleAllResultNotifications(Vector resultNotifications) {
  	if (check()) {
  		getDataStore().put(key, new Integer(Test.TEST_PASSED));
  	}
  	else {
  		printDetails();
  		getDataStore().put(key, new Integer(Test.TEST_FAILED));
  	}
  }
  		
	public boolean check() {
  	if (
  		(proposeCnt == expected[0]) &&
  		(refuseCnt == expected[1]) &&
  		(notUnderstoodCnt == expected[2]) &&
  		(informCnt == expected[3]) &&
  		(failureCnt == expected[4]) && 
  		(outOfSeqCnt == expected[5]) ) {
  		return true;
 		}	
 		else {
 			return false;
 		}
	}
	
	public void printDetails() {
  	System.out.println("PROPOSE: expected "+expected[0]+", received "+proposeCnt);  
  	System.out.println("REFUSE: expected "+expected[1]+", received "+refuseCnt);  
  	System.out.println("NOT_UNDERSTOOD: expected "+expected[2]+", received "+notUnderstoodCnt);  
  	System.out.println("INFORM: expected "+expected[3]+", received "+informCnt);  
  	System.out.println("FAILURE: expected "+expected[4]+", received "+failureCnt);  
  	System.out.println("OUT OF SEQUENCE: expected "+expected[5]+", received "+outOfSeqCnt);
  }
}

