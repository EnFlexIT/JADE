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

public class MsgReceiver extends SimpleBehaviour {
	public static final int TIMEOUT_EXPIRED = -1;
	
	private MessageTemplate template;
	//private Object msgTemplateKey;
	private long msTimeout;
	private long startTime;
	private long elapsedTime;
	//private Object timeoutValueKey;
	private Object receivedMsgKey;
	private boolean received;
	private boolean expired;
	private int ret;
	
	public MsgReceiver(Agent a, MessageTemplate mt, long timeout, Object msgKey) {
		super(a);
		
		template = mt;
		msTimeout = timeout;
		//msgTemplateKey = null;
		//timeoutValueKey = null;
		receivedMsgKey = msgKey;
		
		received = false;
		expired = false;
	}
	
	/*public MsgReceiver(Agent a, Object templateKey, Object timeoutKey, Object msgKey) {
		super(a);
		
		template = null;
		msTimeout = 0;
		msgTemplateKey = templateKey;
		timeoutValueKey = timeoutKey;
		receivedMsgKey = msgKey;
		
		received = false;
		expired = false;
	}
	*/
	
	/*public void onStart() {
		if (msgTemplateKey != null) {
			template = (MessageTemplate) getDataStore().get(msgTemplateKey); 
		}
		if (timeoutValueKey != null) {
			msTimeout = ((Long) getDataStore().get(timeoutValueKey)).longValue(); 
		}
		if (msTimeout > 0) {		
			startTime = System.currentTimeMillis();
		}
	}
	*/
	public void setMessageTemplate(MessageTemplate mt) {
		template = mt;
	}
	
	public void setTimeout(long ms) {
		msTimeout = ms;
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
			System.out.println("Agent "+myAgent.getName()+" has received message");
			System.out.println(msg);
			
			getDataStore().put(receivedMsgKey, msg);
			received = true;
			ret = msg.getPerformative();
		}
		else {
			if (msTimeout > 0) {
				// If a timeout was set, then check if it is expired
				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - startTime;
				if (elapsedTime > msTimeout) {
					expired = true;
					ret = TIMEOUT_EXPIRED;
				}
				else {
					block(msTimeout - elapsedTime);
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
		return ret;
	}
	
	public void reset() {
		super.reset();
		expired = false;
		received = false;
	}
}
	