/*
  $Id$
*/

package jade.lang.acl;

import java.util.Hashtable;

/**************************************************************

  Name: MessageTemplate

  Responsibility and Collaborations:

  + Represents set of ACL messages

  + Performs a pattern matching against a given ACL message
    (ACLMessage)

****************************************************************/
public class MessageTemplate {

  // Beware: '*' is used as a matches-all string. Maybe a different
  // class could hold out-of-band values, e.g. :

  //        interface TemplateItem {
  //          public boolean equal(TemplateItem rhs);
  //        }

  //        class WildCard implements TemplateItem {
  //          public boolean equal(TemplateItem rhs) {
  //            return true;
  //          }
  //        }

  //        class ItemString implements TemplateItem {
  //          private String content;
  //          public boolean equal(TemplateItem rhs) {
  //            if(<rhs is a WildCard>)
  //              return true
  //            else
  //              <Compare the two Strings>
  //          }
  //        }

  private static final String wildCard = "*";
  private ACLMessage template;

  // Creates an ACL message with all fields set to the special,
  // out-of-band wildcard value.
  private static ACLMessage allWildCard() {
    ACLMessage msg = new ACLMessage();

    msg.setSource(wildCard);
    msg.setDest(wildCard);
    msg.setType(wildCard);
    msg.setContent(wildCard);
    msg.setReplyWith(wildCard);
    msg.setReplyTo(wildCard);
    msg.setEnvelope(wildCard);
    msg.setLanguage(wildCard);
    msg.setOntology(wildCard);
    msg.setReplyBy(wildCard);
    msg.setProtocol(wildCard);
    msg.setConversationId(wildCard);

    return msg;
  }


  // Private constructor: use static factory methods to create message
  // templates.
  private MessageTemplate() {
    template = allWildCard();
  }

  // Private constructor: use static factory methods to create message
  // templates.
  private MessageTemplate(ACLMessage msg) {
    template = msg;
  }


  // This Factory Method returns a message template that matches any
  // message.
  public static MessageTemplate MatchAll() {
    return new MessageTemplate();
  }


  // All these Factory Methods return a message template that
  // matches any message where a field has a particular value.

  public static MessageTemplate MatchSource(String value) {
    ACLMessage msg = allWildCard();
    msg.setSource(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchDest(String value) {
    ACLMessage msg = allWildCard();
    msg.setDest(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchContent(String value) {
    ACLMessage msg = allWildCard();
    msg.setContent(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchReplyWith(String value) {
    ACLMessage msg = allWildCard();
    msg.setReplyWith(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchReplyTo(String value) {
    ACLMessage msg = allWildCard();
    msg.setReplyTo(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchEnvelope(String value) {
    ACLMessage msg = allWildCard();
    msg.setEnvelope(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchLanguage(String value) {
    ACLMessage msg = allWildCard();
    msg.setLanguage(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchOntology(String value) {
    ACLMessage msg = allWildCard();
    msg.setOntology(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchReplyBy(String value) {
    ACLMessage msg = allWildCard();
    msg.setReplyBy(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchProtocol(String value) {
    ACLMessage msg = allWildCard();
    msg.setProtocol(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchConversationId(String value) {
    ACLMessage msg = allWildCard();
    msg.setConversationId(value);
    return new MessageTemplate(msg);
  }


  // Boolean operation on message templates.

  public static MessageTemplate and(MessageTemplate op1, MessageTemplate op2) {
      // FIXME: To be implemented
    return null;
  }

  public static MessageTemplate or(MessageTemplate op1, MessageTemplate op2) {
      // FIXME: To be implemented
    return null;
  }


  // Pattern matching with an ACL message
  public boolean match(ACLMessage msg) {

    boolean matchingResult = true;
    String s = template.getSource();
    if((!s.equals(wildCard))&&(!s.equals(msg.getSource()))) {
      matchingResult = false;
    }
    else {
      //  System.out.println("Source matched");
      s = template.getDest();
      if((!s.equals(wildCard))&&(!s.equals(msg.getDest())))
	matchingResult = false;
      else {
	//  System.out.println("Dest matched");
	s = template.getType();
	if((!s.equals(wildCard))&&(!s.equals(msg.getType())))
	  matchingResult = false;
	else {
	  // System.out.println("Type matched");
	  s = template.getContent();
	  if((!s.equals(wildCard))&&(!s.equals(msg.getContent())))
	    matchingResult = false;
	  else {
	    // System.out.println("Content matched");
	    s = template.getReplyWith();
	    if((!s.equals(wildCard))&&(!s.equals(msg.getReplyWith())))
	      matchingResult = false;
	    else {
	      // System.out.println("ReplyWith matched");
	      s = template.getReplyTo();
	      if((!s.equals(wildCard))&&(!s.equals(msg.getReplyTo())))
		matchingResult = false;
	      else {
		// System.out.println("ReplyTo matched");
		s = template.getEnvelope();
		if((!s.equals(wildCard))&&(!s.equals(msg.getEnvelope())))
		  matchingResult = false;
		else {
		  // System.out.println("Envelope matched");
		  s = template.getLanguage();
		  if((!s.equals(wildCard))&&(!s.equals(msg.getLanguage())))
		    matchingResult = false;
		  else {
		    // System.out.println("Language matched");
		    s = template.getOntology();
		    if((!s.equals(wildCard))&&(!s.equals(msg.getOntology())))
		      matchingResult = false;
		    else {
		      // System.out.println("Ontology matched");
		      s = template.getReplyBy();
		      if((!s.equals(wildCard))&&(!s.equals(msg.getReplyBy())))
			matchingResult = false;
		      else {
			// System.out.println("ReplyBy matched");
			s = template.getProtocol();
			if((!s.equals(wildCard))&&(!s.equals(msg.getProtocol())))
			  matchingResult = false;
			else {
			  // System.out.println("Protocol matched");
			  s = template.getConversationId();
			  if((!s.equals(wildCard))&&(!s.equals(msg.getConversationId())))
			    matchingResult = false;
			  //			  else
			  // System.out.println("ConversationId matched");
			}
		      }
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
    }

    return matchingResult;

  }

}
