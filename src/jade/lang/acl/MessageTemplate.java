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

  // Beware: null is used as a matches-all string. Maybe a different
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

  private static final String wildCard = null;
  private ACLMessage template;

  // Private constructor: use static factory methods to create message
  // templates.
  private MessageTemplate() {
    template = new ACLMessage();
    template.setSource(wildCard);
    template.setDest(wildCard);
    template.setType(wildCard);
    template.setContent(wildCard);
    template.setReplyWith(wildCard);
    template.setReplyTo(wildCard);
    template.setEnvelope(wildCard);
    template.setLanguage(wildCard);
    template.setOntology(wildCard);
    template.setReplyBy(wildCard);
    template.setProtocol(wildCard);
    template.setConversationId(wildCard);
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
    MessageTemplate msg = new ACLMessage();
    msg.setSource(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchDest(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setDest(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchContent(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setContent(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchReplyWith(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setReplyWith(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchReplyTo(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setReplyTo(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchEnvelope(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setEnvelope(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchLanguage(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setLanguage(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchOntology(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setOntology(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchReplyBy(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setReplyBy(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchProtocol(String value) {
    MessageTemplate msg = new ACLMessage();
    msg.setProtocol(value);
    return new MessageTemplate(msg);
  }

  public static MessageTemplate MatchConversationId(String value) {
    MessageTemplate msg = new ACLMessage();
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
    if((s != wildCard)&&(!s.equals(msg.getSource())))
      matchingResult = false;
    else {
      s = template.getDest();
      if((s != wildCard)&&(!s.equals(msg.getDest())))
	matchingResult = false;
      else {
	s = template.getType();
	if((s != wildCard)&&(!s.equals(msg.getType())))
	  matchingResult = false;
	else {
	  s = template.getContent();
	  if((s != wildCard)&&(!s.equals(msg.getContent())))
	    matchingResult = false;
	  else {
	    s = template.getReplyWith();
	    if((s != wildCard)&&(!s.equals(msg.getReplyWith())))
	      matchingResult = false;
	    else {
	      s = template.getReplyTo();
	      if((s != wildCard)&&(!s.equals(msg.getReplyTo())))
		matchingResult = false;
	      else {
		s = template.getEnvelope();
		if((s != wildCard)&&(!s.equals(msg.getEnvelope())))
		  matchingResult = false;
		else {
		  s = template.getLanguage();
		  if((s != wildCard)&&(!s.equals(msg.getLanguage())))
		    matchingResult = false;
		  else {
		    s = template.getOntology();
		    if((s != wildCard)&&(!s.equals(msg.getOntology())))
		      matchingResult = false;
		    else {
		      s = template.getReplyBy();
		      if((s != wildCard)&&(!s.equals(msg.getReplyBy())))
			matchingResult = false;
		      else {
			s = template.getProtocol();
			if((s != wildCard)&&(!s.equals(msg.getProtocol())))
			  matchingResult = false;
			else {
			  s = template.getConversationId();
			  if((s != wildCard)&&(!s.equals(msg.getConversationId())))
			    matchingResult = false;
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
