/**
 * @version $Id$
 *
 * Copyright (c) 1998 CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the agreement you entered into with CSELT.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 */

package examples.jess;

import jade.lang.acl.ACLMessage;
import jade.core.Agent;

/**
 * This class extends and implements a BasicJessBehaviour.
 * @see BasicJessBehaviour
 * It has only one method JessString
 */
public class JessBehaviour extends BasicJessBehaviour {

     public JessBehaviour(Agent agent, String jessFile, int maxJessPasses){
       super(agent,jessFile,maxJessPasses);
     }

  /**
   * Returns a String representing the received ACLMessage.
   * If the message content starts with the character "(", that is it is
   * an Expression, then the <code>assert</code> method converts it into
   * a String and the appropriate parsing functionality must be implemented
   * directly in Jess.
   */
  public String JessString(ACLMessage msg) {
    String     fact;
    
    if (msg == null) return null;
    // I create a string that asserts the template fact
    fact = "(assert (ACLMessage (receiver " + msg.getDest() + ") (communicative-act " + msg.getType();     
    if (msg.getSource() != null)         fact = fact + ") (sender " + msg.getSource();   
    if (msg.getContent() != null) {
      if ((msg.getContent()).charAt(0) == '(')
	                                 fact = fact + ") (content \"" + msg.getContent() + "\"";
      else 	                         fact = fact + ") (content " + msg.getContent();
    }
    if (msg.getReplyWith() != null)      fact=fact+") (reply-with " + msg.getReplyWith();
    if (msg.getReplyTo() != null)        fact=fact+") (in-reply-to " + msg.getReplyTo();   
    if (msg.getEnvelope() != null)       fact=fact+") (envelope " + msg.getEnvelope();    
    if (msg.getLanguage() != null)       fact=fact+") (language " + msg.getLanguage();    
    if (msg.getOntology() != null)       fact=fact+") (ontology " + msg.getOntology();    
    if (msg.getReplyBy() != null)        fact=fact+") (reply-by " + msg.getReplyBy();    
    if (msg.getProtocol() != null)       fact=fact+") (protocol " + msg.getProtocol();  
    if (msg.getConversationId() != null) fact=fact+") (conversation-id " + msg.getConversationId(); 
    fact=fact+")))";
    return fact;
  }

}


