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

import jade.core.VerticalCommand;
import jade.core.Filter;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;
import jade.util.leap.Map;
import jade.util.leap.Iterator;
import jade.lang.acl.LEAPACLCodec;


/**
 * Class that filters incoming commands related to encoding of ACL messages.
 *
 * @author Jerome Picault - Motorola Labs
 * @version $Date$ $Revision$
 */
public class IncomingEncodingFilter implements Filter {

  private Map messageEncodings;

  public IncomingEncodingFilter(Map m){
    messageEncodings = m;
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
    if(name.equals(MessagingSlice.SEND_MESSAGE)) {
      //DEBUG
      //System.out.println("-- Filtering an SEND_MESSAGE command (incoming) --");
      // Params could be an ACLMessage or Envelope + payload
      // encapsulated inside a GenericMessage object.
      GenericMessage gmsg = (GenericMessage)params[1];

      //DEBUG
      //System.out.println(gmsg);
      // The command always contains a non-null ACLMessage (for the purpose
      // of notification of failures), but it contains the real ACLMessage
      // when the payload is null
      if(gmsg.getPayload()==null){
        // If this is a real ACLMessage, nothing to do!
        return true;
      }
      else {
        Envelope env = gmsg.getEnvelope();
        byte[] payload = gmsg.getPayload();
        ACLMessage msg ;
        try{
          msg = decodeMessage(env,payload);
          msg.setEnvelope(env);
          
          if (env!=null){
          // If the 'sender' AID has no addresses, replace it with the
          // 'from' envelope slot
          AID sender = msg.getSender();
          if(sender == null) {
				    System.err.println("ERROR: Trying to dispatch a message with a null sender.");
				    System.err.println("Aborting send operation...");
				    return true;
          }
          Iterator itSender = sender.getAllAddresses();
          if(!itSender.hasNext())
				    msg.setSender(env.getFrom());
          }
          ((GenericMessage)params[1]).update(msg,null,null);
        } catch (MessagingService.UnknownACLEncodingException ee){
          //FIXME 
          ee.printStackTrace();
        } catch (ACLCodec.CodecException ce){
          //FIXME
          ce.printStackTrace();
        }
      }
    }
    return true;
  }

  public void setBlocking(boolean newState) {
    // Do nothing. Blocking and Skipping not supported
  }

  public boolean isBlocking() {
    return false; // Blocking and Skipping not implemented
  }

  public void setSkipping(boolean newState) {
    // Do nothing. Blocking and Skipping not supported
  }

  public boolean isSkipping() {
    return false; // Blocking and Skipping not implemented
  }



  /**
   * Decodes an endoded ACL message according to the acl-representation 
   * described in the envelope. 
   * @param env the Envelope of the message
   * @param payload the encoded message
   * @return the decoded <code>ACLMessage</code>
   */
  public ACLMessage decodeMessage(Envelope env, byte[] payload) throws MessagingService.UnknownACLEncodingException, ACLCodec.CodecException{
    String enc;
    if (env!=null){
      enc = env.getAclRepresentation();
    } else {
      // no envelope means inter-container communication; use LEAP codec
      enc = LEAPACLCodec.NAME.toLowerCase();
    }
    if(enc != null) { // A Codec was selected
	    ACLCodec codec =(ACLCodec)messageEncodings.get(enc.toLowerCase());
	    if(codec!=null) {
    		// Supported Codec
    		// FIXME: should verifY that the receivers supports this Codec
    		return codec.decode(payload);
	    } else {
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

} // End of IncomingEncodingFilter class