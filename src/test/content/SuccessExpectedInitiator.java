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

package test.content;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import test.common.Test;

public abstract class SuccessExpectedInitiator extends FSMBehaviour {
	public static final String SEND_MSG_STATE = "Send-msg";
	public static final String GET_REPLY_STATE = "Get-reply";
	public static final String DUMMY_FINAL_STATE = "Dummy-final";
	
	public static final int PREPARE_MSG_OK = 1;
	public static final int PREPARE_MSG_NOK = 0;
	
  private MessageTemplate mt = MessageTemplate.and(
  	MessageTemplate.MatchConversationId(Responder.TEST_CONVERSATION),
  	MessageTemplate.MatchInReplyTo(Responder.TEST_RESPONSE_ID));

  private ACLMessage sentMsg = null;
  
  public SuccessExpectedInitiator(Agent a, DataStore ds, String key) {
  	super(a);
  	final String resultKey = key;
  	
  	registerTransition(SEND_MSG_STATE, GET_REPLY_STATE, PREPARE_MSG_OK);
  	registerTransition(SEND_MSG_STATE, DUMMY_FINAL_STATE, PREPARE_MSG_NOK);
  	
  	// SEND_MSG_STATE
  	Behaviour b = new OneShotBehaviour() {
  		private int ret;
  		
  		public void action() {
  			try {
  				sentMsg = prepareMessage();
  				myAgent.send(sentMsg);
  				ret = PREPARE_MSG_OK;
  			}
  			catch (Exception e) {
  				e.printStackTrace();
  				getDataStore().put(resultKey, new Integer(Test.TEST_FAILED));
  				ret = PREPARE_MSG_NOK;
  			}
  		}
  		
  		public int onEnd() {
  			return ret;
  		}
  	};
  	b.setDataStore(ds);
  	registerFirstState(b, SEND_MSG_STATE);
  		
	  // GET_REPLY_STATE
		b = new SimpleBehaviour() {
			private boolean finished = false;
		
			public void action() {
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					if (msg.getPerformative() == getExpectedPerformative(sentMsg)) {
  					getDataStore().put(resultKey, new Integer(Test.TEST_PASSED));
					}
					else {
	  				getDataStore().put(resultKey, new Integer(Test.TEST_FAILED));
					}
					finished = true;
				}
				else {
					block();
				}
			}
		
			public boolean done() {
				return finished;
			}
		};
		b.setDataStore(ds);
		registerLastState(b, GET_REPLY_STATE);
						
		// DUMMY_FINAL
		registerLastState(new OneShotBehaviour() {
			public void action() {
			}
		}, DUMMY_FINAL_STATE);
	}
	
	protected abstract ACLMessage prepareMessage() throws Exception;
	
	protected int getExpectedPerformative(ACLMessage sentMsg) {
		return ACLMessage.INFORM;
	}
}  
