/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content;

import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.content.abs.*;
import jade.content.onto.*;

/**
 * @author Giovanni Caire - TILAB
 */
public class OntoACLMessage extends ACLMessage implements AgentAction {

  /**
   * Constructs an ontological ACL message whose performative
   * is ACLMessage.NOT_UNDERSTOOD
   */
	public OntoACLMessage() {
		super(ACLMessage.NOT_UNDERSTOOD);
	}
	
  /** 
   * Constructor for an ontological ACL message with a given 
   * performative
   * @param performative the performative of this ACL message. 
   * @see ACLMessage#ACLMessage(int)
   */
	public OntoACLMessage(int performative) {
		super(performative);
	}
	
	/** 
	 * Create an ontological ACL message that wraps an existing 
	 * <code>ACLMessage</code>.
	 * @param msg the <code>ACLMessage</code>to be wrapped. If 
	 * <code>msg</code>
	 * is already an ontological ACL message no new object is 
	 * created and <code>msg</code> is returned with the sender
	 * and receivers properly wrapped if necessary.
	 */
	public static OntoACLMessage wrap(ACLMessage msg) {
		OntoACLMessage wrapper = null;
		if (msg instanceof OntoACLMessage) {
			wrapper = (OntoACLMessage) msg;
		}
		else {
			wrapper = new OntoACLMessage(msg.getPerformative());
			// This automatically performs the wrapping
			wrapper.setSender(msg.getSender());
			Iterator it = msg.getAllReceiver();
			while (it.hasNext()) {
				// This automatically performs the wrapping
				wrapper.addReceiver((AID) it.next());
			}
			
			it = msg.getAllReplyTo();
			while (it.hasNext()) {
				// This automatically performs the wrapping
				wrapper.addReplyTo((AID) it.next());
			}
			
    	wrapper.setLanguage(msg.getLanguage());
    	wrapper.setOntology(msg.getOntology());
    	wrapper.setProtocol(msg.getProtocol());
    	wrapper.setInReplyTo(msg.getInReplyTo());
      wrapper.setReplyWith(msg.getReplyWith()); 
    	wrapper.setConversationId(msg.getConversationId());
    	wrapper.setReplyByDate(msg.getReplyByDate());
    	if (msg.hasByteSequenceContent()) {
    	wrapper.setByteSequenceContent(msg.getByteSequenceContent());
    	}
    	else {
    		wrapper.setContent(msg.getContent());
    	}
    	wrapper.setEncoding(msg.getEncoding());
    
    	//FIXME: Message Envelope is missing
		}
		
		return wrapper; 
	}
	
	/**
	 * This method is redefined so that the sender AID is automatically
	 * wrapped into an OntoAID
	 */
	public void setSender(AID aid) {
		super.setSender(OntoAID.wrap(aid));
	}
	
	/**
	 * This method is redefined so that the receiver AID is automatically
	 * wrapped into an OntoAID
	 */
	public void addReceiver(AID aid) {
		super.addReceiver(OntoAID.wrap(aid));
	}
	
	/**
	 * This method is redefined so that the replyTo AID is automatically
	 * wrapped into an OntoAID
	 */
	public void addReplyTo(AID aid) {
		super.addReplyTo(OntoAID.wrap(aid));
	}
	
	// FIXME: clone method should be redefined too
	
	
  public void externalise(AbsObject abs, Ontology onto) throws OntologyException {
  	try {
  		AbsAgentAction absMsg = (AbsAgentAction) abs;
  		absMsg.set(BasicOntology.ACLMSG_PERFORMATIVE, AbsPrimitive.wrap(getPerformative()));
  		absMsg.set(BasicOntology.ACLMSG_SENDER, (AbsTerm) onto.fromObject(getSender()));
  		// Receivers
  		AbsAggregate recvs = new AbsAggregate(BasicOntology.SEQUENCE);
  		Iterator it = getAllReceiver();
  		while (it.hasNext()) {
  			recvs.add((AbsTerm) onto.fromObject(it.next()));
  		}
  		absMsg.set(BasicOntology.ACLMSG_RECEIVERS, recvs);
  		// Reply_to
  		AbsAggregate repls = new AbsAggregate(BasicOntology.SEQUENCE);
  		it = getAllReceiver();
  		while (it.hasNext()) {
  			repls.add((AbsTerm) onto.fromObject(it.next()));
  		}
  		absMsg.set(BasicOntology.ACLMSG_REPLY_TO, repls);
  		
  		absMsg.set(BasicOntology.ACLMSG_LANGUAGE, getLanguage());
  		absMsg.set(BasicOntology.ACLMSG_ONTOLOGY, getOntology());
  		absMsg.set(BasicOntology.ACLMSG_PROTOCOL, getProtocol());
  		absMsg.set(BasicOntology.ACLMSG_IN_REPLY_TO, getInReplyTo());
  		absMsg.set(BasicOntology.ACLMSG_REPLY_WITH, getReplyWith());
  		absMsg.set(BasicOntology.ACLMSG_CONVERSATION_ID, getConversationId());
  		absMsg.set(BasicOntology.ACLMSG_REPLY_BY, getReplyByDate());
  		// Content
  		if (hasByteSequenceContent()) {
	  		absMsg.set(BasicOntology.ACLMSG_BYTE_SEQUENCE_CONTENT, getByteSequenceContent());
  		}
  		else {
	  		absMsg.set(BasicOntology.ACLMSG_CONTENT, getContent());
  		}
  		absMsg.set(BasicOntology.ACLMSG_ENCODING, getEncoding());
  	}
  	catch (ClassCastException cce) {
  		throw new OntologyException("Error externalising OntoACLMessage");
  	}
  }

  public void internalise(AbsObject abs, Ontology onto) throws UngroundedException, OntologyException {
    try {
  		AbsAgentAction absMsg = (AbsAgentAction) abs;
  		setPerformative(absMsg.getInteger(BasicOntology.ACLMSG_PERFORMATIVE)); 
  		setSender((AID) onto.toObject(absMsg.getAbsObject(BasicOntology.ACLMSG_SENDER))); 
  		// Receivers
  		clearAllReceiver();
  		List l = (List) onto.toObject(absMsg.getAbsObject(BasicOntology.ACLMSG_RECEIVERS));
  		Iterator it = l.iterator();
  		while (it.hasNext()) {
  			addReceiver((AID) it.next());
  		}
  		// ReplyTo
  		clearAllReplyTo();
  		l = (List) onto.toObject(absMsg.getAbsObject(BasicOntology.ACLMSG_REPLY_TO));
  		it = l.iterator();
  		while (it.hasNext()) {
  			addReplyTo((AID) it.next());
  		}
  		setLanguage(absMsg.getString(BasicOntology.ACLMSG_LANGUAGE)); 
  		setOntology(absMsg.getString(BasicOntology.ACLMSG_ONTOLOGY)); 
  		setProtocol(absMsg.getString(BasicOntology.ACLMSG_PROTOCOL)); 
  		setInReplyTo(absMsg.getString(BasicOntology.ACLMSG_IN_REPLY_TO)); 
  		setReplyWith(absMsg.getString(BasicOntology.ACLMSG_REPLY_WITH)); 
  		setConversationId(absMsg.getString(BasicOntology.ACLMSG_CONVERSATION_ID)); 
  		setReplyByDate(absMsg.getDate(BasicOntology.ACLMSG_REPLY_BY)); 
  		String c = absMsg.getString(BasicOntology.ACLMSG_CONTENT);
  		if (c != null) {
  			setContent(c);
  		}
  		else {
	  		byte[] bsc = absMsg.getByteSequence(BasicOntology.ACLMSG_BYTE_SEQUENCE_CONTENT);
	  		if (bsc != null) {
	  			setByteSequenceContent(bsc);
	  		}
  		}
  		setEncoding(absMsg.getString(BasicOntology.ACLMSG_ENCODING)); 
  	}
  	catch (ClassCastException cce) {
  		throw new OntologyException("Error internalising OntoACLMessage");
  	}
  }
}

