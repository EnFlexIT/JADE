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

package test.domain;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.proto.*;
import test.common.*;

public abstract class SuccessExpectedInitiator extends AchieveREInitiator {
  private int ret = Test.TEST_FAILED;
  private String resultKey;
  
	public SuccessExpectedInitiator(Agent a, ACLMessage msg, DataStore ds, String key){
  	super(a, msg, ds);
  	resultKey = key;
  }
  
  protected void handleInform(ACLMessage inform) {
  	if (check(inform)) {
  		ret = Test.TEST_PASSED;
  	}
  }
 
  protected void handleRefuse(ACLMessage refuse) {
  	failure(refuse);
  }

  protected void handleNotUnderstood(ACLMessage notUnderstood) {
  	failure(notUnderstood);
  }
    
  protected void handleFailure(ACLMessage failure) {
  	failure(failure);
  }
    
  protected void handleOutOfSequence(ACLMessage msg) {
  	failure(msg);
  }
    
  public int onEnd() {
  	getDataStore().put(resultKey, new Integer(ret));
  	return 0;
  }
  
  protected abstract boolean check(ACLMessage inform);
  
  private void failure(ACLMessage msg) {
  	System.out.println("Unexpected message received:");
  	System.out.println(msg);
  }	
}
