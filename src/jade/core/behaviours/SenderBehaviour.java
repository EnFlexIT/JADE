
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


package jade.core.behaviours;

import jade.core.Agent;
import jade.core.AgentGroup;

import jade.lang.acl.ACLMessage;

/**
   Behaviour for sending an ACL message. This class encapsulates a
   <code>send()</code> as an atomic operation. This behaviour sends a
   given ACL message and terminates.
   @see jade.core.behaviours.ReceiverBehaviour
   @see jade.core.Agent#send(ACLMessage msg)
   @see jade.lang.acl.ACLMessage
   
   
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

 */
public final class SenderBehaviour extends OneShotBehaviour {


  // The ACL message to send
	/**
	@serial
	*/
  private ACLMessage message;

  // An AgentGroup to perform multicasting
  /**
  @serial
  */
  private AgentGroup receivers;

  /**
     Send a given ACL message to an agent group. This constructor
     creates a <code>SenderBehaviour</code> which sends an ACL
     message, multicasting it to an <code>AgentGroup</code>.
     @param a The agent this behaviour belongs to, and that will
     <code>send()</code> the message.
     @param msg An ACL message to send.
     @param ag The agent group to send the message to.
  */
  public SenderBehaviour(Agent a, ACLMessage msg, AgentGroup ag) {
    super(a);
    message = msg;
    receivers = ag;

    message.setSource(myAgent.getLocalName());
  }

  /**
     Send a given ACL message. This constructor creates a
     <code>SenderBehaviour</code> which sends an ACL message.
     @param a The agent this behaviour belongs to, and that will
     <code>send()</code> the message.
     @param msg An ACL message to send.
  */
  public SenderBehaviour(Agent a, ACLMessage msg) {
    this(a, msg, null);
  }

  /**
     Actual behaviour implementation. This method sends an ACL
     message, using either the given <code>AgentGroup</code> or the
     <code>:receiver</code> message slot to get the message recipient
     names.
  */
  public void action() {
    if(receivers == null)
      myAgent.send(message);
    else
      myAgent.send(message, receivers);
  }

}