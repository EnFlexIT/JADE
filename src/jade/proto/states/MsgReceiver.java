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

package jade.proto.states;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
mette null in datastore se è scaduto in timeout.
 **/
public class MsgReceiver extends SimpleBehaviour {

    public static final int TIMEOUT_EXPIRED = -1;
	
    private MessageTemplate template;
    private long deadline;
    private Object receivedMsgKey;
    private boolean received;
    private boolean expired;
    private int ret;
	
	public MsgReceiver(Agent a, MessageTemplate mt, long timeout, DataStore s, Object msgKey) {
		super(a);
		setDataStore(s);
		template = mt;
		deadline = timeout;
		receivedMsgKey = msgKey;
		received = false;
		expired = false;
	}
	
	public void action() {
		ACLMessage msg = null;
	    
		if (template != null) {
			msg = myAgent.receive(template);
		}
		else {
			msg = myAgent.receive();
		}
		
		if (msg != null) {
			// DEBUG
			//System.out.println("Agent "+myAgent.getName()+" has received message");
			//System.out.println(msg);
			
			getDataStore().put(receivedMsgKey, msg);
			received = true;
			ret = msg.getPerformative();
		}
		else {
			if (deadline > 0) {
				// If a timeout was set, then check if it is expired
				long blockTime = deadline - System.currentTimeMillis();
				if(blockTime <=0){
				    //timeout expired
				    getDataStore().put(receivedMsgKey, null); 
				    expired = true;
				    ret = TIMEOUT_EXPIRED;

				}else{
				    block(blockTime);
				}
			}
			else {
				block();
			}
		}
	}
	
	public boolean done() {
		return received || expired;
	}
	
	public int onEnd() {
		received =false;
		expired =false;
		return ret;
	}
	

    public void reset(MessageTemplate mt, long timeout, DataStore s, Object msgKey) {
	super.reset();
	set(mt, timeout, s, msgKey);
	received = false;
	expired = false;
    }


    public void set(MessageTemplate mt, long timeout, DataStore s, Object msgKey) {
	setDataStore(s);
	template=mt;
	deadline = timeout;
	receivedMsgKey = msgKey;
    }
	
}
	
