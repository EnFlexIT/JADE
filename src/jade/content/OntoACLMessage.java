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
import jade.util.leap.Iterator;

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
			// FIXME: fills other fields wrapping sender and receivers
		}
		
		return wrapper; 
	}
	
}

