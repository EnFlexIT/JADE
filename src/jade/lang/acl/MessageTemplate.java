/*
  $Log$
  Revision 1.15  1999/07/11 20:27:08  rimassa
  Reimplemented the whole class using Interpreter design pattern. Now
  complete logical expressions with AND, OR and NOT operators are
  supported.

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
import java.io.IOException;
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

  private static interface MatchExpression {
    boolean match(ACLMessage msg);
    void toText(Writer w);
  }

  private static class AndExpression implements MatchExpression {

    private MatchExpression op1;
    private MatchExpression op2;

    public AndExpression(MatchExpression e1, MatchExpression e2) {
      op1 = e1;
      op2 = e2;
    }

    public boolean match(ACLMessage msg) {
      return op1.match(msg) && op2.match(msg);
    }

    public void toText(Writer w) {
      try {
	w.write("( ");
	op1.toText(w);
	w.write(" AND ");
	op2.toText(w);
	w.write(" )");
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of AndExpression class

  private static class OrExpression implements MatchExpression {

    private MatchExpression op1;
    private MatchExpression op2;

    public OrExpression(MatchExpression e1, MatchExpression e2) {
      op1 = e1;
      op2 = e2;
    }

    public boolean match(ACLMessage msg) {
      return op1.match(msg) || op2.match(msg);
    }

    public void toText(Writer w) {
      try {
	w.write("( ");
	op1.toText(w);
	w.write(" OR ");
	op2.toText(w);
	w.write(" )");
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of OrExpression class

  private static class NotExpression implements MatchExpression {
    private MatchExpression op;

    public NotExpression(MatchExpression e) {
      op = e;
    }

    public boolean match(ACLMessage msg) {
      return ! op.match(msg);
    }

    public void toText(Writer w) {
      try {
	w.write(" NOT ");
	op.toText(w);
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
    }

  } // End of NotExpression class

  private static class Literal implements MatchExpression {

    private ACLMessage template;

    public Literal(ACLMessage msg) {
      template = (ACLMessage)msg.clone();
    }

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

    public void toText(Writer w) {
      try {
	w.write("(\n");
	for(int i = 0; i<fieldNames.length; i++) {
	  String name = fieldNames[i];
	  String value = null;
	  try {
	    Method getValue = ACLMessage.class.getMethod("get"+name, new Class[0]);
	    // This means: s1 = template.get<value>();
	    value = (String)getValue.invoke(template, new Object[0]);
	  }
	  catch(Exception e) {
	    e.printStackTrace();
	  }
	  if((value != null) && (!value.equals(wildCard)))
	    w.write(" :" + name + " == " + value + "\n");
	}
	w.write(")\n");
      }
      catch(IOException ioe) {
	ioe.printStackTrace();
      }
      
    }

  } // End of Literal class


  private MatchExpression toMatch;

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
	setValue = ACLMessageClass.getMethod("set" + name, paramType);
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
  private MessageTemplate(MatchExpression e) {
    toMatch = e;
  }

  // Private constructor: use static factory methods to create message
  // templates.
  private MessageTemplate(ACLMessage msg) {
    toMatch = new Literal((ACLMessage)msg.clone());
  }


  /**
     This <em>Factory Method</em> returns a message template that
     matches any message.
     @return A new <code>MessageTemplate</code> matching any given
     value.
  */
  public static MessageTemplate MatchAll() {
    return new MessageTemplate(allWildCard());
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
    msg.removeAllDests();
    msg.addDest(value);
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
     This <em>Factory Method</em> returns a message template that
     matches ACL messages against a given one, passed as
     parameter. The following algorithm is used:
     When the given <code>ACLMessage</code> has a non
     <code>null</code> slot, subsequent messages must have the same
     slot value in that slot to have a match.
     When the given <code>ACLMessage</code> has a <code>null</code>
     slot, subsequent messages can have any value for that slot and
     still match the template.
     In short, a <code>null</code> value for a slot means <em>don't
     care</em>.
     @param msg The <code>ACLMessage</code> used to build a custom
     message template.
     @return A new <code>MessageTemplate</code>, matching the given
     message according to the above algorithm.
  */
  public static MessageTemplate MatchCustom(ACLMessage msg) {
    ACLMessage message = allWildCard();

    Class ACLMessageClass = msg.getClass();

    // Used to hold the classes of the formal parameters of
    // get<name>() methods.
    Class[] noClass = new Class[0];

    // Used to hold actual parameters of get<name>() methods.
    Object[] noParams = new Object[0];

    // Used to hold the classes of the formal parameters of
    // set<name>() methods.
    Class[] stringClass = { String.class };

    Method getValue = null;
    Method setValue = null;

    String s1 = null;
    String s2 = null;

    boolean result = true;
    for(int i = 0; i<fieldNames.length; i++) {
      String name = fieldNames[i];
      try {
	getValue = ACLMessageClass.getMethod("get" + name, noClass);
	setValue = ACLMessageClass.getMethod("set" + name, stringClass);

	// This means: s1 = msg.get<value>();
	s1 = (String)getValue.invoke(msg, noParams);
	if(s1 != null) {
	  Object[] stringParams = { s1 };
	  // This means: message.set<value>(s1);
	  getValue.invoke(message, stringParams);
	}
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }

    return new MessageTemplate(message);
  }

  /**
     Reads a <code>MessageTemplate</code> from a character stream.
     @param r A readable character stream containing a string
     representation of a message template.
     @return A new <code>MessageTemplate</code> object.
  */
  public static MessageTemplate fromText(Reader r) {
    Literal l  = new Literal(ACLMessage.fromText(r));
    return new MessageTemplate(l);
  }

  /**
     Dumps a <code>MessageTemplate</code> to a character stream.
     @param w A writable character stream that will hold a string
     representation of this <code>MessageTemplate</code> object.
  */
  public void toText(Writer w) {
    try {
      toMatch.toText(w);
      w.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
     Logical <b>and</b> between two <code>MessageTemplate</code>
     objects. This method creates a new message template that is
     matched by those ACL messages matching <b><em>both</b></em>
     message templates given as operands.
     @param op1 The first <em>and</em> operand.
     @param op2 The second <em>and</em> operand.
     @return A new <code>MessageTemplate</code> object.
     @see jade.lang.acl.MessageTemplate#or(MessageTemplate op1, MessageTemplate op2)
  */
  public static MessageTemplate and(MessageTemplate op1, MessageTemplate op2) {
    AndExpression e = new AndExpression(op1.toMatch, op2.toMatch);
    MessageTemplate result = new MessageTemplate(e);
    return result;
  }

  /**
     Logical <b>or</b> between two <code>MessageTemplate</code>
     objects. This method creates a new message template that is
     matched by those ACL messages matching <b><em>any</b></em> of the
     two message templates given as operands.
     @param op1 The first <em>or</em> operand.
     @param op2 The second <em>or</em> operand.
     @return A new <code>MessageTemplate</code> object.
     @see jade.lang.acl.MessageTemplate#and(MessageTemplate op1, MessageTemplate op2)
  */
  private static MessageTemplate or(MessageTemplate op1, MessageTemplate op2) {
    OrExpression e = new OrExpression(op1.toMatch, op2.toMatch);
    MessageTemplate result = new MessageTemplate(e);
    return result;
  }

  /**
     Logical <b>not</b> of a <code>MessageTemplate</code> object. This
     method creates a new message template that is matched by those
     ACL messages <b><em>not</em></b> matching the message template
     given as operand.
     @param op The <em>not</em> operand.
     @return A new <code>MessageTemplate</code> object.
  */
  private static MessageTemplate not(MessageTemplate op) {
    NotExpression e = new NotExpression(op.toMatch);
    MessageTemplate result = new MessageTemplate(e);
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
    return toMatch.match(msg);
  }

}
