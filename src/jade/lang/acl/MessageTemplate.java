/*
  $Log$
  Revision 1.14  1999/06/08 00:02:26  rimassa
  Removed an useless comment.
  Put some dead code to start implementing complete Bool algebra for
  message templates.

  Revision 1.13  1999/05/19 18:23:10  rimassa
  Changed static or() method access from public to private, since it
  isn't implemented yet.

  Revision 1.12  1999/04/06 00:10:10  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.11  1998/10/18 16:03:40  rimassa
  Modified code to avoid using deprecated ACLMessage constructor.
  Removed dump() method, now a toText() method is provided to print a
  MessageTemplate on any stream.

  Revision 1.10  1998/10/04 18:02:11  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.lang.acl;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;

import java.util.List;
import java.util.LinkedList;

/**
   A pattern for matching incoming ACL messages. This class allows to
   build complex slot patterns to select ACL messages. These patterns
   can then be used in <code>receive()</code> operations.
   @see jade.core.Agent#receive(MessageTemplate mt)

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public class MessageTemplate {

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

  // This class represents an elementary template term, that is a term
  // with a single non-null slot, which can be negated or not.
  private class ProductTerm extends ACLMessage {
    private boolean negated;

    private String name;
    private Class type;
    private Object value;

    public ProductTerm(String slotName, Class slotType, Object slotValue) {
      negated = false;
      name = slotName;
      type = slotType;
      value = slotValue;
    }

    public void not() {
      negated = !negated;
    }

    public boolean match(Object aValue) {
      return false;
    }

  }


  // This class represent a logical AND of one or more elementary terms.
  private class SumTerm extends ACLMessage {

    private List slots = new LinkedList();

    public SumTerm() {
    }

    public boolean match(ACLMessage msg) {
      return false;
    }

  }

  // A message template is a logical OR of logical ANDs of elementary
  // terms, negated or not.


  private ACLMessage template;

  // Creates an ACL message with all fields set to the special,
  // out-of-band wildcard value.
  private static ACLMessage allWildCard() {
    ACLMessage msg = new ACLMessage(wildCard);

    Class ACLMessageClass = msg.getClass();

    Method setValue = null;
    String name = null;

    // Formal parameter type for set<name>() method call
    Class[] paramType = { wildCard.getClass() };

    // Actual parameter for set<name>() method call
    Object[] param = { wildCard };

    for(int i = 0; i<fieldNames.length; i++) {
      name = fieldNames[i];
    
      try {
	// This means: msg.set<name>(param)
	setValue = ACLMessageClass.getMethod("set"+name, paramType);
	setValue.invoke(msg, param);
      }
      catch(Exception e) {
	e.printStackTrace();
      }

    }

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


  /**
     This <em>Factory Method</em> returns a message template that
     matches any message.
     @return A new <code>MessageTemplate</code> matching any given
     value.
  */
  public static MessageTemplate MatchAll() {
    return new MessageTemplate();
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:sender</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchSource(String value) {
    ACLMessage msg = allWildCard();
    msg.setSource(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:receiver</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchDest(String value) {
    ACLMessage msg = allWildCard();
    msg.setDest(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:content</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchContent(String value) {
    ACLMessage msg = allWildCard();
    msg.setContent(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:reply-with</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchReplyWith(String value) {
    ACLMessage msg = allWildCard();
    msg.setReplyWith(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:in-reply-to</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchReplyTo(String value) {
    ACLMessage msg = allWildCard();
    msg.setReplyTo(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:envelope</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchEnvelope(String value) {
    ACLMessage msg = allWildCard();
    msg.setEnvelope(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:language</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchLanguage(String value) {
    ACLMessage msg = allWildCard();
    msg.setLanguage(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:ontology</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchOntology(String value) {
    ACLMessage msg = allWildCard();
    msg.setOntology(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:reply-by</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchReplyBy(String value) {
    ACLMessage msg = allWildCard();
    msg.setReplyBy(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:protocol</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchProtocol(String value) {
    ACLMessage msg = allWildCard();
    msg.setProtocol(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given <code>:conversation-id</code> slot.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchConversationId(String value) {
    ACLMessage msg = allWildCard();
    msg.setConversationId(value);
    return new MessageTemplate(msg);
  }

  /**
     This <em>Factory Method</em> returns a message template that
     matches any message with a given message type.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
  */
  public static MessageTemplate MatchType(String value) {
    ACLMessage msg = allWildCard();
    msg.setType(value);
    return new MessageTemplate(msg);
  }

  /**
     Reads a <code>MessageTemplate</code> from a character stream.
     @param r A readable character stream containing a string
     representation of a message template.
     @return A new <code>MessageTemplate</code> object.
  */
  public static MessageTemplate fromText(Reader r) {
    MessageTemplate mt = new MessageTemplate();
    mt.template = ACLMessage.fromText(r);
    return mt;
  }

  /**
     Dumps a <code>MessageTemplate</code> to a character stream.
     @param w A writable character stream that will hold a string
     representation of this <code>MessageTemplate</code> objects.
  */
  public void toText(Writer w) {
    template.toText(w);
  }

  /**
     Logical <b>and</b> between two <code>MessageTemplate</code>
     objects. This method creates a new message template that is
     matched by those ACL messages matching <b><em>both</b></em>
     message templates given as operands.
     @param op1 The first <em>and</em> operand.
     @param op2 The second <em>and</em> operand.
     @return A new <code>MessageTemplate</code> object.
     @exception IllegalArgumentException When the two operands are
     incompatible.
     @see jade.lang.acl.MessageTemplate#or(MessageTemplate op1, MessageTemplate op2)
  */
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
	  if(s1.equalsIgnoreCase(s2)) {
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

  /**
     <b>NOT IMPLEMENTED.</b>
   */
  private static MessageTemplate or(MessageTemplate op1, MessageTemplate op2) {
    MessageTemplate result = new MessageTemplate();
    return result;
  }

  /**
     Matches an ACL message against this <code>MessageTemplate</code>
     object.
     @param msg The <code>ACLMessage</code> to check for matching.
     @return <code>true</code> if the ACL message matches this
     template, <code>false</code> otherwise.
  */
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

	if((!(s1.equalsIgnoreCase(wildCard))&&(!s1.equalsIgnoreCase(s2)))) {
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
