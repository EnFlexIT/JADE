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
 * This behaviour is a simple implementation of a message receiver.
 * It puts into the given key of the given datastore the received message
 * according to the given message template and timeout. All these
 * data must be passed in the constructor.
 * If the timeout expires before any message arrives, the behaviour
 * terminates and put null into the datastore.
 * @author Tiziana Trucco - TILab
 * @version $Date$ $Revision$
 **/
public class MsgReceiver extends SimpleBehaviour {

    public static final int TIMEOUT_EXPIRED = -1;
	
    private MessageTemplate template;
    private long deadline;
    private Object receivedMsgKey;
    private boolean received;
    private boolean expired;
    private int ret;
	
    /**
     *  Constructor.
     * @param a a reference to the Agent
     * @param mt the MessageTemplate of the message to be received, if null
     * the first received message is returned by this behaviour
     * @param deadline a timeout for waiting until a message arrives. It must
     * be expressed as an absolute time, as it would be returned by
     * <code>System.currentTimeMillisec()</code>
     * @param s the dataStore for this bheaviour
     * @param msgKey the key where the beahviour must put the received
     * message into the DataStore.
     **/
	public MsgReceiver(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
		super(a);
		setDataStore(s);
		template = mt;
		this.deadline = deadline;
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
			if (deadline >= 0) {
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
	
    /**
     * @return the performative if a message is arrived
     * @return TIMEOUT_EXPIRED if the timeout expired
     **/
	public int onEnd() {
		received =false;
		expired =false;
		return ret;
	}
	

    public void reset(MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
	super.reset();
	set(mt, deadline, s, msgKey);
	received = false;
	expired = false;
    }

    /**
     * This method allows to modify the values of the parameters passed in 
     * the constructor.
     **/
    public void set(MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
	setDataStore(s);
	template=mt;
	this.deadline = deadline;
	receivedMsgKey = msgKey;
    }
	
    /**
     * This method allows modifying the timeout
     **/
    public void setDeadline(long deadline) {
	this.deadline = deadline;
    }
	
}
	
