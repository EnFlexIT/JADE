/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package examples.jess;

import jade.lang.acl.ACLMessage;
import jade.core.Agent;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/
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
   * The messate content is quoted before asserting the Jess Fact.
   * It is unquoted by the Jess send function (cf. basicJessBehaviour). 
   */
  public String JessString(ACLMessage msg) {
    String     fact;
    
    if (msg == null) return null;
    // I create a string that asserts the template fact
    fact = "(assert (ACLMessage (receiver " + msg.getFirstDest() + ") (communicative-act " + ACLMessage.getPerformative(msg.getPerformative());     
    if (msg.getSource() != null)         fact = fact + ") (sender " + msg.getSource();   
    if (msg.getContent() != null) 
      fact = fact + ") (content " + super.quote(msg.getContent());

    if (!isEmpty(msg.getReplyWith()))    fact=fact+") (reply-with " + msg.getReplyWith();
    if (!isEmpty(msg.getReplyTo()))      fact=fact+") (in-reply-to " + msg.getReplyTo();   
    if (!isEmpty(msg.getEnvelope()))     fact=fact+") (envelope " + msg.getEnvelope();    
    if (!isEmpty(msg.getLanguage()))     fact=fact+") (language " + msg.getLanguage();    
    if (!isEmpty(msg.getOntology()))     fact=fact+") (ontology " + msg.getOntology();    
    if (!isEmpty(msg.getReplyBy()))      fact=fact+") (reply-by " + msg.getReplyBy();    
    if (!isEmpty(msg.getProtocol()))     fact=fact+") (protocol " + msg.getProtocol();  
    if (!isEmpty(msg.getConversationId())) fact=fact+") (conversation-id " + msg.getConversationId(); 
    fact=fact+")))";
    return fact;
  }
  private boolean isEmpty(String string) {
    return string == null || string.equals("");
  }

}






