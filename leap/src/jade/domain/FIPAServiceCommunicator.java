/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.domain;

import java.util.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.onto.Frame;
import jade.onto.OntologyException;
import jade.util.leap.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import jade.lang.sl.SL0Codec;
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;

// import jade.onto.Ontology;
// import jade.onto.Frame;
// import jade.onto.OntologyException;
// import jade.onto.basic.Action;
// import jade.onto.basic.ResultPredicate;

/**
 * Class declaration
 * 
 * @author LEAP
 */
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

    //Runtime.instance().gc(32);

    return request;
  } 

  /**
   * @return the INFORM message received in the final state of the protocol, if
   * the protocol succeeded, otherwise it throws an Exception
   */
  static ACLMessage doFipaRequestClient(Agent a, ACLMessage request) throws FIPAException {
    a.send(request);

    MessageTemplate mt = MessageTemplate.MatchInReplyTo(request.getReplyWith());
    //Runtime.instance().gc(33);

    ACLMessage reply = a.blockingReceive(mt);
    //Runtime.instance().gc(34);

    if (reply.getPerformative() == ACLMessage.AGREE) {
      reply = a.blockingReceive(mt);

      //Runtime.instance().gc(35);

      if (reply.getPerformative() != ACLMessage.INFORM) {
        throw new FIPAException(reply.getContent());
      } 
      else {
        return reply;
      } 
    } 
    else if (reply.getPerformative() == ACLMessage.INFORM) {
      mt = null;

      return reply;
    } 
    else {
      throw new FIPAException(reply.getContent());
    } 
  } 

  /**
   * Utility method used to stringify an AID
   * 
   * static String stringifyAID(AID id) {
   * ByteArrayOutputStream outText = new ByteArrayOutputStream();
   * Writer                text = new OutputStreamWriter(outText);
   * 
   * id.toText(text);
   * 
   * return new String(outText.toByteArray());
   * }
   */

  /**
   * Utility methods that converts a Frame object representing
   * an AID into an AID object
   */
  public static AID convertFrameToAID(Frame f) throws OntologyException {
    AID aid = null;

    if (f != null) {
      aid = new AID();

      // Name (mandatory)
      aid.setName((String) f.getSlot("name"));

      // Addresses (optional)
      Frame f1 = (Frame) getFrameOptionalSlot(f, "addresses");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          aid.addAddresses((String) f1.getSlot(i));
        } 
      } 

      // Resolvers (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "resolvers");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          Frame f2 = (Frame) f1.getSlot(i);

          aid.addResolvers(convertFrameToAID(f2));
        } 
      } 
    } 

    return aid;
  } 

  /**
   * Method declaration
   * 
   * @param f
   * @param slotName
   * 
   * @return
   * 
   * @see
   */
  public static Object getFrameOptionalSlot(Frame f, String slotName) {
    Object slot = null;

    try {
      slot = f.getSlot(slotName);
    } 
    catch (OntologyException oe) {

      // Do nothing as this is an optional slot
    } 

    return slot;
  } 

}

