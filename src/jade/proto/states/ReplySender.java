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
//import java.util.Vector;

import jade.util.leap.Iterator;


/**
 * This behaviour is a simple implementation of a reply message sender.
 * It read in DataStore the message and the reply at the key passed in Constrictor  
 * Set the reply's converationId, protocol and reply-to fields and 
 * reply's receiver and reply-with fields if not setted
 * @author Fabio Bellifemine - TILab
 * @author Giovanni Caire - TILab
 * @author Marco Monticone 
 * @version $Date$ $Revision$
 **/

public class ReplySender extends OneShotBehaviour{
	
		public static final int NO_REPLY_SENT = -1;
		private int ret;
		private String replyKey,requestKey;

 /**
  *  Constructor.
  * @param a a reference to the Agent
  * @param key_response DataStore's key where stored the reply message
  * @param key_request DataStore's key where stored the message to respond
  * @param ds the dataStore for this bheaviour
 **/ 
public ReplySender(Agent a,String key_response,String key_request,DataStore ds){
		this(a,key_response,key_request);
		setDataStore(ds);
}
		
/**
  *  Constructor.
  * @param a a reference to the Agent
  * @param key_response DataStore's key where stored the reply message
  * @param key_request DataStore's key where stored the message to respond
 **/ 

public ReplySender(Agent a,String key_reply,String key_request){
		super(a);
	  replyKey=key_reply;
	  requestKey=key_request;	
}
	
public void onStart(){
	ret=NO_REPLY_SENT;
}

public void action(){
	DataStore ds = getDataStore();
	ACLMessage reply = (ACLMessage)ds.get(replyKey);
	if (reply != null) {
			ACLMessage request = (ACLMessage) ds.get(requestKey);
			if(request!=null) {
			  sendReply(request,reply);
				ret = reply.getPerformative();
			}		
	}
}
 public int onEnd() {
		    return ret;
 }

 
 
private void sendReply(ACLMessage message,ACLMessage reply){

                        //set the conversationId
		
	
	
	reply.setConversationId(message.getConversationId());
			//set the inReplyTo
			reply.setInReplyTo(message.getReplyWith());
			//set the Protocol.
			reply.setProtocol(message.getProtocol());
		//set ReplyWith if not yet
			if (reply.getReplyWith() == null)
		      reply.setReplyWith(myAgent.getName() + java.lang.System.currentTimeMillis()); 
    
			if (!reply.getAllReceiver().hasNext()){
					//set Receiver if not yet
					boolean no_reply_to=true;
					Iterator i=message.getAllReplyTo();
					while(i.hasNext()){
						no_reply_to=false;
						reply.addReceiver((AID)i.next());
					}
					if(no_reply_to) 
						reply.addReceiver(message.getSender());
			}	
			//send
			myAgent.send(reply);
}
}