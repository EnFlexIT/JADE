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

package jade.lang.acl;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;

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
public class MessageTemplate implements Serializable {

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
					       "Source"
  };

  private static interface MatchExpression {
    boolean match(ACLMessage msg);
    void toText(Writer w);
  }

  private static class AndExpression implements MatchExpression, Serializable {

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

  private static class OrExpression implements MatchExpression, Serializable {

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

  private static class NotExpression implements MatchExpression, Serializable {
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

  private static class Literal implements MatchExpression, Serializable {

    private class WildCardedMessage {
      private boolean hasPerformative;
      private ACLMessage myMessage;

      public WildCardedMessage(ACLMessage msg, boolean b) {
	myMessage = msg;
	hasPerformative = b;
      }

      public boolean matchPerformative() {
	return hasPerformative;
      }

      public ACLMessage getMsg() {
	return myMessage;
      }

    }

    private WildCardedMessage template;

    public Literal(ACLMessage msg, boolean wildcardOnPerformative) {
      template = new WildCardedMessage((ACLMessage)msg.clone(), wildcardOnPerformative);
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
      ACLMessage templMessage = template.getMsg();

      if(template.matchPerformative()) {
	int perf1 = templMessage.getPerformative();
	int perf2 = msg.getPerformative();
	if(perf1 != perf2)
	  return false;
      }

      for(int i = 0; i<fieldNames.length; i++) {
	String name = fieldNames[i];

	try {

	  getValue = ACLMessageClass.getMethod("get"+name, noClass);

	  // This means: s1 = templMessage.get<value>();
	  s1 = (String)getValue.invoke(templMessage, noParams);

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
	    ACLMessage msg = template.getMsg();
	    Method getValue = ACLMessage.class.getMethod("get"+name, new Class[0]);
	    // This means: s1 = msg.get<value>();
	    value = (String)getValue.invoke(msg, new Object[0]);
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
  private MessageTemplate(ACLMessage msg, boolean matchPerformative) {
    toMatch = new Literal((ACLMessage)msg.clone(), matchPerformative);
  }


  /**
     This <em>Factory Method</em> returns a message template that
     matches any message.
     @return A new <code>MessageTemplate</code> matching any given
     value.
  */
  public static MessageTemplate MatchAll() {
    return new MessageTemplate(allWildCard(), false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
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
    return new MessageTemplate(msg, false);
  }

  /**
     @deprecated This <em>Factory Method</em> returns a message template that
     matches any message with a given message type.Use <code>matchPerformative</code>
     instead.
     @param value The value the message slot will be matched against.
     @return A new <code>MessageTemplate</code> matching the given
     value.
     @see jade.lang.acl.MessageTemplate#MatchPerformative(int value)
     @see jade.lang.acl.ACLMessage

  */
  public static MessageTemplate MatchType(String value) {
    ACLMessage msg = allWildCard();
    msg.setType(value);
    return new MessageTemplate(msg, true);
  }

  
 /**
 		 This <em>Factory Method</em> returns a message template that
 		 matches any message with a given performative.
 		 @param value The value the message slot will be matched against.
 		 @return A new <code>MessageTenplate</code>matching the given
 		 value.
 */
	public static MessageTemplate MatchPerformative(int value){
  	ACLMessage msg = allWildCard();
  	msg.setPerformative(value);  	
  	return new MessageTemplate(msg,true);
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

     @param matchPerformative a <code>bool</code> value. When
     <code>true</code>, the performative of the <code>msg</code> will
     be considered as a part of the template (i.e. the message
     template will match only ACL messages with the same performativa
     as <code>msg</code>).
     When <false>, the performative of <code>msg</code> is ignored and
     the resulting message template will not consider it when matching
     messages.
     @return A new <code>MessageTemplate</code>, matching the given
     message according to the above algorithm.
  */
  public static MessageTemplate MatchCustom(ACLMessage msg, boolean matchPerformative) {
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
	  setValue.invoke(message, stringParams);
	}
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }

    return new MessageTemplate(message, matchPerformative);
  }

  /**
     Reads a <code>MessageTemplate</code> from a character stream.
     @param r A readable character stream containing a string
     representation of a message template.
     @return A new <code>MessageTemplate</code> object.
     @exception ParseException Thrown when the string format is incorrect.
  */
  public static MessageTemplate fromText(Reader r) throws ParseException {
    Literal l = new Literal(ACLMessage.fromText(r), true);
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
  public static MessageTemplate or(MessageTemplate op1, MessageTemplate op2) {
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
  public static MessageTemplate not(MessageTemplate op) {
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

