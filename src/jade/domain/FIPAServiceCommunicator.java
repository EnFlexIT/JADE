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

package jade.domain;

import java.util.*; 

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;

import jade.onto.Ontology;
import jade.onto.Frame;
import jade.onto.OntologyException;

import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;

public class FIPAServiceCommunicator {

  /**
   * create a REQUEST message with the following slots:
   * <code> (REQUEST :sender sender.getAID() :receiver receiver
   * :protocol fipa-request :language FIPA-SL0 :ontology fipa-agent-management
   * :reply-with xxx :conversation-id xxx) </code>
   * where <code>xxx</code> are unique words generated on the basis of
   * the sender's name and the current time.
   * @param sender is the Agent sending the message
   * @param receiver is the AID of the receiver agent
   * @return an ACLMessage object 
   */
  static ACLMessage createRequestMessage(Agent sender, AID receiver) {
    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
    request.setSender(sender.getAID());
    request.addReceiver(receiver);
    request.setProtocol("fipa-request");
    request.setLanguage(SL0Codec.NAME);
    request.setOntology(FIPAAgentManagementOntology.NAME);
    request.setReplyWith("rw"+sender.getName()+(new Date()).getTime());
    request.setConversationId("conv"+sender.getName()+(new Date()).getTime());
    return request;
  }
  
  

  /**
   * @return the INFORM message received in the final state of the protocol, if
   * the protocol succeeded, otherwise it throws an Exception
   */
  static ACLMessage doFipaRequestClient(Agent a, ACLMessage request) throws FIPAException {
    a.send(request);
    MessageTemplate mt = MessageTemplate.MatchInReplyTo(request.getReplyWith());
    ACLMessage reply = a.blockingReceive(mt);
    if(reply.getPerformative() == ACLMessage.AGREE) {
      reply =  a.blockingReceive(mt);
      if(reply.getPerformative() != ACLMessage.INFORM) 
	throw new FIPAException(reply.getContent());
      else 
	return reply;
    } else if(reply.getPerformative() == ACLMessage.INFORM) 
      return reply;
    else 
      throw new FIPAException(reply.getContent());
  }

  /**
   * this method is here to avoid any agent using this class to register before
   * the SL-0 codec and the fipa-agent-management ontology
   */
  public static String encode(Action act, Codec c, Ontology o) throws FIPAException {
    // Write the action in the :content slot of the request
    List l = new ArrayList();
    try {
      Frame f = o.createFrame(act, o.getRoleName(act.getClass()));
      l.add(f);
    } catch (OntologyException oe) {
      throw new FIPAException(oe.getMessage());
    }
    return c.encode(l,o);
  }
  
  /**
   * this method is here to avoid any agent using this class to register before
   * the SL-0 codec and the fipa-agent-management ontology
   */
  public static ResultPredicate extractContent(String content, Codec c, Ontology o) throws FIPAException {
    try {
      List tuple = c.decode(content,o);
      tuple = o.createObject(tuple);
      return (ResultPredicate)tuple.get(0);
    } catch (Codec.CodecException e1) {
      e1.printStackTrace();
      throw new FIPAException(e1.getMessage());
    } catch (OntologyException e2) {
      e2.printStackTrace();
      throw new FIPAException(e2.getMessage());
    }
  }
}
