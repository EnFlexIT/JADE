/*
  $Id$
*/

package jade.lang.acl;

import java.lang.reflect.Method;
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

  // Names of the various fields of an ACL messages.
  // Used to build the names of get()/set() methods.
  private static final String[] fieldNames = { "Content",
					       "ConversationId",
					       "Dest",
					       "Envelope",
					       "Language",
					       "Ontology",
					       "Protocol",
					       "ReplyBy",
					       "ReplyTo",
					       "ReplyWith",
					       "Source",
					       "Type"
  };
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

  public static MessageTemplate and(MessageTemplate op1, MessageTemplate op2) throws IllegalArgumentException {
    MessageTemplate result = new MessageTemplate();

    ACLMessage m1 = op1.template;
    ACLMessage m2 = op2.template;
    ACLMessage m3 = result.template;
    String name = null;
    String s1 = null;
    String s2 = null;

    Class ACLMessageClass = m1.getClass();

    // Used to hold the classes of the formal parameters of
    // get<name>() and set<name>() methods.
    Class[] noClass = new Class[0];
    Class[] stringClass = { new String().getClass() };

    // Used to hold actual parameters of get<name>() and set<name>()
    // methods.
    Object[] noParams = new Object[0];
    Object[] oneParam = new Object[1];

    Method getValue = null;
    Method setValue = null;

    // Use Reflection API to scan all the fields of ACL message.

    for(int i = 0; i<fieldNames.length;i++) {
      name = fieldNames[i];
      try {
	// Process 'name' field of ACL message
	getValue = ACLMessageClass.getMethod("get"+name, noClass);
	setValue = ACLMessageClass.getMethod("set"+name, stringClass);

	// Invokes get<name>() methods on m1 and m2, putting results in
	// s1 and s2 respectively.
	s1 = (String)getValue.invoke(m1, noParams);
	s2 = (String)getValue.invoke(m2, noParams);

	if(s1.equals(wildCard) && !s2.equals(wildCard)) {
	  // This means: m3.set<value>(s2)
	  oneParam[0] = s2;
	  setValue.invoke(m3, oneParam);
	}
	if(!s1.equals(wildCard) && s2.equals(wildCard)) {
	  // This means: m3.set<value>(s1);
	  oneParam[0] = s1;
	  setValue.invoke(m3, oneParam);
	}
	if(!s1.equals(wildCard) && !s2.equals(wildCard)) 
	  if(s1.equals(s2)) {
	    // This means:  m3.set<value>(s1);
	    oneParam[0] = s1;
	    setValue.invoke(m3, oneParam);
	  }
	  else
	    throw new IllegalArgumentException("and: operands are in contradiction");
      }
      catch(Exception e) {
	e.printStackTrace();
      }
    }

    return result;
  }

  // FIXME: Not implemented, and maybe it's even meaningless ...
  public static MessageTemplate or(MessageTemplate op1, MessageTemplate op2) {
    MessageTemplate result = new MessageTemplate();
    return result;
  }


  // Pattern matching with an ACL message -- now uses Reflection API
  // to avoid code duplication.
  public boolean match(ACLMessage msg) {

    Class ACLMessageClass = msg.getClass();

    // Used to hold the classes of the formal parameters of
    // get<name>() methods.
    Class[] noClass = new Class[0];

    // Used to hold actual parameters of get<name>() methods.
    Object[] noParams = new Object[0];

    Method getValue = null;

    String s1 = null;
    String s2 = null;

    boolean result = true;
    for(int i = 0; i<fieldNames.length; i++) {
      String name = fieldNames[i];

      try {
	getValue = ACLMessageClass.getMethod("get"+name, noClass);

	// This means: s1 = template.get<value>();
	s1 = (String)getValue.invoke(template, noParams);

	// This means: s2 = msg.get<value>();
	s2 = (String)getValue.invoke(msg, noParams);

	if((!(s1.equals(wildCard))&&(!s1.equals(s2)))) {
	  result = false;
	  break; // Exit for loop
	}
      }
      catch(Exception e) {
	e.printStackTrace();
      }
    }

    return result;

  }


}
