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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE

import java.util.Date;

import jade.domain.FIPAAgentManagement.Envelope;

import jade.core.VerticalCommand;
import jade.core.AgentContainer;
import jade.core.Filter;

import jade.core.AID;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;
import jade.lang.acl.LEAPACLCodec;

import jade.util.leap.Iterator;
import jade.util.leap.Map;


/**
 * Class that filters outgoing commands related to the encoding of ACL messages
 *
 * @author Jerome Picault - Motorola Labs
 * @author Nicolas Lhuillier - Motorola Labs
 * @version $Date$ $Revision$
 */
public class OutgoingEncodingFilter extends Filter {
  
  private Map messageEncodings;
  private AgentContainer myAgentContainer;
  private MessagingService myService; 

  public OutgoingEncodingFilter(Map m, AgentContainer ac, MessagingService ms){
    messageEncodings = m;
    myAgentContainer = ac;
    myService = ms;
    setPreferredPosition(10);
  }


  /**
   * Receive a command object for processing.
   *
   * @param cmd A <code>VerticalCommand</code> describing what operation has
   * been requested from previous layers (that can be the actual
   * prime source of the command or previous filters in the chain).
   */
  public boolean accept(VerticalCommand cmd) {
    String name = cmd.getName();
    Object[] params = cmd.getParams();

    // The awaited command should contain an ACLMessage
    if(name.equals(MessagingSlice.SEND_MESSAGE)) {
      // DEBUG
      //System.out.println("-- Filtering a SEND_MESSAGE command (outgoing)--");

      GenericMessage gmsg = (GenericMessage)params[1];
      AID sender = (AID) params[0];
      AID receiver = (AID) params[2];
      ACLMessage msg = gmsg.getACLMessage();

      // Set the sender unless already set
      try {
        if (msg.getSender().getName().length() < 1)
          msg.setSender(sender);
      }
      catch (NullPointerException e) {
        msg.setSender(sender);
      }
      //DEBUG
      //System.out.println(gmsg);

      // check if the agent is on the same container or not
      synchronized (myAgentContainer){
        if (myAgentContainer.acquireLocalAgent(receiver)!=null){
          // local container
          myAgentContainer.releaseLocalAgent(receiver);
          // message should not be encoded
          // command is not modified
          //DEBUG
          //          System.out.println("[EncodingService] Local message");
          return true;
        } else {
          // add necessary fields to the envelope
          prepareEnvelope(msg, receiver, gmsg);

        }
      }

      // in both cases (intra and inter), encode the message 
      // using the specified encoding
      try{
        byte[] payload = encodeMessage(msg);
        // DEBUG
        //        System.out.println("[EncodingService] msg encoded.");
        Envelope env =  msg.getEnvelope();
        if (env!=null)
          env.setPayloadLength(new Long(payload.length));

        // update the ACLMessage: some information is kept because it is 
        // required in other services
        ((GenericMessage)cmd.getParams()[1]).update(msg,env,payload);

      } catch (MessagingService.UnknownACLEncodingException ee){
        //FIXME
        ee.printStackTrace();
      }            
    }
    return true;
  }


  /**
   * This method puts into the envelope the missing information
   */
  public void prepareEnvelope(ACLMessage msg, AID receiver, GenericMessage gmsg) {
    Envelope env = msg.getEnvelope();
    String defaultRepresentation = null;
  	//if (myAgentContainer.livesHere(receiver)) {
  	if (myService.livesHere(receiver)) {
  		// The agent lives in the platform
	    if (env == null) {
	    	// Nothing to do
	      return;
	    }
	    else {
	    	defaultRepresentation = LEAPACLCodec.NAME;
	    }
  	}
    else {
  		gmsg.setForeignReceiver(true);
    	if (env == null) {
	    	// The agent lives outside the platform
		    msg.setDefaultEnvelope();
		    env = msg.getEnvelope();
    	}
	    else {
	    	defaultRepresentation = StringACLCodec.NAME;
	    }
    }

    // If no 'to' slot is present, copy the 'to' slot from the
    // 'receiver' slot of the ACL message
    Iterator itTo = env.getAllTo();
    if(!itTo.hasNext()) {
	    Iterator itReceiver = msg.getAllReceiver();
	    while(itReceiver.hasNext())
        env.addTo((AID)itReceiver.next());
    }

    // If no 'from' slot is present, copy the 'from' slot from the
    // 'sender' slot of the ACL message
    AID from = env.getFrom();
    if(from == null) {
	    env.setFrom(msg.getSender());
    }

    // Set the 'date' slot to 'now' if not present already
    Date d = env.getDate();
    if(d == null)
	    env.setDate(new Date());

    // If no ACL representation is found, then default to String
    // representation
    String rep = env.getAclRepresentation();
    if(rep == null)
	    env.setAclRepresentation(defaultRepresentation);

    // Write 'intended-receiver' slot as per 'FIPA Agent Message
    // Transport Service Specification': this ACC splits all
    // multicasts, since JADE has already split them in the
    // handleSend() method
    env.clearAllIntendedReceiver();
    env.addIntendedReceiver(receiver);
    
    Long payloadLength = env.getPayloadLength();
    if(payloadLength == null)
	    env.setPayloadLength(new Long(-1));
    
    /*
      Moved to IIOP MessageTransportProtocol class
      String comments = env.getComments();
      if(comments == null)
      env.setComments("");
      
      String payloadEncoding = env.getPayloadEncoding();
      if(payloadEncoding == null)
      env.setPayloadEncoding("");
    */
  }

  /**
   * Encodes an ACL message according to the acl-representation described 
   * in the envelope. If there is no explicit acl-representation in the
   * envelope, uses the String representation
   * @param msg the message to be encoded
   * @return the payload of the message
   */
  public byte[] encodeMessage(ACLMessage msg) throws MessagingService.UnknownACLEncodingException{

    Envelope env = msg.getEnvelope();
    String enc;
    if (env==null) enc = LEAPACLCodec.NAME;
    else enc = env.getAclRepresentation();

    if(enc != null) { // A Codec was selected
	    ACLCodec codec =(ACLCodec)messageEncodings.get(enc.toLowerCase());
	    if(codec!=null) {
    		// Supported Codec
    		// FIXME: should verifY that the receivers supports this Codec
    		String charset;  
        if ((env == null) ||
            ((charset = env.getPayloadEncoding()) == null)) {
          charset = ACLCodec.DEFAULT_CHARSET;
        }
        return codec.encode(msg,charset);
	    }
	    else {
    		// Unsupported Codec
    		//FIXME: find the best according to the supported, the MTP (and the receivers Codec)
    		throw new MessagingService.UnknownACLEncodingException("Unknown ACL encoding: " + enc + ".");
	    }
    }
    else {
	    // no codec indicated. 
	    //FIXME: find the better according to the supported Codec, the MTP (and the receiver codec)
	    throw new MessagingService.UnknownACLEncodingException("No ACL encoding set.");
    }
  }


} // End of EncodingOutgoingFilter class


