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

package test.proto.tests.achieveRE;

import jade.core.*;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import test.common.*;
import test.proto.tests.TestBase;

import java.util.Date;
import java.util.Vector;

/**
   @author Giovanni Caire - TILAB
 */
public class BasicAchieveREInitiator extends AchieveREInitiator {

	protected int agreeCnt = 0;
	protected int notUnderstoodCnt = 0;
	protected int refuseCnt = 0;
	protected int informCnt = 0;
	protected int failureCnt = 0;
	protected int outOfSeqCnt = 0;
	
	private String key;
	private long waitingTime;
  private int[] expected;
  
  private boolean verbose = true;
  
	public BasicAchieveREInitiator(Agent a, ACLMessage msg, DataStore ds, String key, long waitingTime, int[] expected) {
		super(a, msg, ds);
		this.key = key;
		this.waitingTime = waitingTime;
		if (expected.length != 6) {
			throw new IllegalArgumentException("6 expected number of messages must be specified");
		}
		this.expected = expected;
	}
	
  protected Vector prepareRequests(ACLMessage request) {
		Vector v = new Vector(1);
		request.setProtocol(FIPAProtocolNames.FIPA_REQUEST);
  	request.setReplyByDate(new Date((new Date()).getTime() + waitingTime));
		v.addElement(request);
		return v;
  }
  		
  protected void handleAgree(ACLMessage agree) {
  	if (verbose) {
  		System.out.println("AGREE received. Message is");
  		System.out.println(agree);
  	}
  	agreeCnt++;
  }
  		
  protected void handleRefuse(ACLMessage refuse) {
  	if (verbose) {
  		System.out.println("REFUSE received. Message is");
  		System.out.println(refuse);
  	}
  	refuseCnt++;
  }
  		
  protected void handleNotUnderstood(ACLMessage notUnderstood) {
  	if (verbose) {
  		System.out.println("NOT_UNDERSTOOD received. Message is");
  		System.out.println(notUnderstood);
  	}
  	notUnderstoodCnt++;
	}
  		
  protected void handleInform(ACLMessage inform) {
  	if (verbose) {
  		System.out.println("INFORM received. Message is");
  		System.out.println(inform);
  	}
  	informCnt++;
  }
  	
  protected void handleFailure(ACLMessage failure) {
  	if (verbose) {
  		System.out.println("FAILURE received. Message is");
  		System.out.println(failure);
  	}
  	failureCnt++;
  }
  		
  protected void handleOutOfSequence(ACLMessage msg) {
  	if (verbose) {
  		System.out.println("OUT_OF_SEQUENCE received. Message is");
  		System.out.println(msg);
  	}
  	outOfSeqCnt++;
  }
  		
  // Reply with ACCEPT_PROPOSAL to all PROPOSE
  protected void handleAllResponses(Vector responses) {
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
  		(agreeCnt == expected[0]) &&
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
  	System.out.println("AGREE: expected "+expected[0]+", received "+agreeCnt);  
  	System.out.println("REFUSE: expected "+expected[1]+", received "+refuseCnt);  
  	System.out.println("NOT_UNDERSTOOD: expected "+expected[2]+", received "+notUnderstoodCnt);  
  	System.out.println("INFORM: expected "+expected[3]+", received "+informCnt);  
  	System.out.println("FAILURE: expected "+expected[4]+", received "+failureCnt);  
  	System.out.println("OUT OF SEQUENCE: expected "+expected[5]+", received "+outOfSeqCnt);
  }
}

