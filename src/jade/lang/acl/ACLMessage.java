////////////////////////////////////////////////////////////////////////
// Copyright (c) 1998 FIPA All Rights Reserved
//
// This software module was developed by
// CSELT (Centro Studi e Laboratori Telecomunicazioni S.p.A)
// and DII University of Parma
// in the course of development of the FIPA97 standard. 
// It is an implementation of the parser of the 
// FIPA97 Agent Communication Language
// specified by the FIPA97 standard.
//
//
//
// The copyright of this software belongs to FIPA. FIPA gives
// free license to its members to use this software module or
// modifications thereof for hardware or software products claiming
// conformance to the FIPA97 standard.
//
//
//
// Those intending to use this software module in hardware or software
// products are advised that use may infringe existing  patents. The
// original developers of this software module and their companies, the
// subsequent editors and their companies, and FIPA have no liability
// for use of this software module or modification thereof in an
// implementation.
//
//
//
// FIPA thanks Fabio Bellifemine, Agostino Poggi, and Paolo Marenzoni
// for releasing the copyright of this software.
// Part of this software has been developed within the framework of the
// FACTS project, AC317, of the European ACTS Programme.
////////////////////////////////////////////////////////////////////////
/*
 $Log$
 Revision 1.13  1999/03/07 22:56:47  rimassa
 Deprecated getMessage() method.
 Added support for more agent names in ':receiver' slot. Now methods
 getDest() and setDest() are deprecated and addDest(), removeDest(),
 getDests() and removeAllDests() methods have been added.

 Revision 1.12  1999/02/22 09:25:18  rimassa
 Added support for ISO 8601 time format using a custom new class for
 all format conversions.

 Revision 1.11  1999/02/04 11:28:26  rimassa
 Added checks for null object references in modifier methods.
 Removed redundant code from time handling.

 Revision 1.10  1999/02/03 11:53:59  rimassa
 Added a more flexible time handling to use in ':reply-by' ACL message
 field.

 Revision 1.9  1998/10/18 16:01:17  rimassa
 Deprecated constructor without arguments and dump() method. Added a
 newer constructor and a fromText() static Factory Method.
 Modified toText() output formatting to insert a newline character
 after each message field.

 Revision 1.8  1998/10/04 18:02:09  rimassa
 Added a 'Log:' field to every source file.

 Revision 1.7  1998/10/04 15:25:19  rimassa
 Fixed a bug: an 'if' clause tested the wrong variable.

 Revision 1.6  1998/09/28 22:40:44  Giovanni
 Added a 'toText()' method to write an ACLMessage on an arbitrary
 Writer (String, OutputStream, File, ecc.).

 Revision 1.5  1998/09/02 00:42:16  rimassa
 Added code to make ACLMessages cloneable. Now a public clone() method
 is provided to make copies of an ACL message.

 Revision 1.4  1998/08/08 21:37:22  rimassa
 Added a missing import clause: 'import java.ioSerializable'.

 Revision 1.3  1998/08/08 18:06:53  rimassa
 Used official FIPA version of Java class for ACL messages. Some minor changes:

  - Changed class name from 'alcMessage' to 'ACLMessage'.
  - Added 'package jade.lang.acl` declaration.
  - Added 'implements Serializable' to be able to send ACL messages with Java RMI.

*/

package jade.lang.acl;

import java.io.Reader;
import java.io.Writer;
import java.io.Serializable;
import java.io.IOException;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Date;

import jade.core.AgentGroup;

/**
 * The class ACLMessage implements an ACL message compliant to the FIPA97 specs.
 * All parameters are couples <IT>keyword: value</IT>.
 * All keywords are private final String.
 * All values can be set by using the methods <em>set</em> and can be read by using
 * the methods <em>get</em>.
 */
public class ACLMessage implements Cloneable, Serializable {

  private static final String SOURCE          = new String(" :sender ");
  private static final String DEST            = new String(" :receiver ");
  private static final String CONTENT         = new String(" :content ");
  private static final String REPLY_WITH      = new String(" :reply-with ");
  private static final String IN_REPLY_TO     = new String(" :in-reply-to ");
  private static final String ENVELOPE        = new String(" :envelope ");
  private static final String LANGUAGE        = new String(" :language ");
  private static final String ONTOLOGY        = new String(" :ontology ");
  private static final String REPLY_BY        = new String(" :reply-by ");
  private static final String PROTOCOL        = new String(" :protocol ");
  private static final String CONVERSATION_ID = new String(" :conversation-id ");

  private String        source;
  private AgentGroup    dests = new AgentGroup();
  private String        msgType;
  private String        content;
  private String        reply_with;
  private String        in_reply_to;
  private String        envelope;
  private String        language;
  private String        ontology;
  private String        reply_by;
  private long          reply_byInMillisec;  
  private String        protocol;
  private String        conversation_id;

  /**
     @deprecated Since every ACL Message must have a message type, you
     should use the new constructor which gets a message type as a
     parameter.  To avoid problems, now this constructor silently sets
     the message type to <code>not-understood</code>.
     @see jade.lang.acl.ACLMessage#ACLMessage(String type)
  */
  public ACLMessage() {
    msgType = "not-understood";
  }

  public ACLMessage(String type) {
    msgType = new String(type);
  }

  public static ACLMessage fromText(Reader r) {
    ACLMessage msg = null;
    try {
      msg = ACLParser.create().parse(r);
    }
    catch(ParseException pe) {
      pe.printStackTrace();
    }
    catch(TokenMgrError tme) {
      tme.printStackTrace();
    }

    return msg;
  }

  /**
 * <em>set</em> methods to set the actual values of parameters.
 * NOTICE: correctness of actual value of parameters is not checked
 */
  public void setSource( String source ) {
    if (source != null)
      this.source = new String(source);
  }

  /**
  @deprecated Now <code>ACLMessage</code> class supports multiple
  receivers, so <code>addDest()</code>, <code>removeDest()</code> and
  <code> getDests()</code> should be used. Currently, this
  method removes all previous receivers and inserts the one
  given.
  @see jade.lang.acl.ACLMessage#addDest()
  @see jade.lang.ACLMessage#removeDest()
  @see jade.lang.acl.ACLMessage#getDests()
  */
  public void setDest( String dest ) {
    if (dest != null) {
      dests = new AgentGroup();
      dests.addMember(new String(dest));
    }
  }

  public void addDest(String dest) {
    dests.addMember(new String(dest));
  }

  public void removeDest(String dest) {
    dests.removeMember(new String(dest));
  }

  public void removeAllDests() {
    dests.reset();
  }

  public void setType( String type ) {
    if (type != null)
      msgType = new String(type);
  }

  public void setContent( String content ) {
    if (content != null)
      this.content = new String(content);
  }

  public void setReplyWith( String reply ) {
    if (reply != null)
      reply_with = new String(reply);
  }

  public void setReplyTo( String reply ) {
    if (reply != null)
      in_reply_to = new String(reply);
  }
  
  public void setEnvelope( String str ) {
    if (str != null)
      envelope = new String(str);
  }

  public void setLanguage( String str ) {
    if (str != null)
      language = new String(str);
  }

  public void setOntology( String str ) {
    if (str != null)
      ontology = new String(str);
  }

  public void setReplyBy( String str ) {
    if (str != null) {
      reply_by = new String(str);
      try {
	reply_byInMillisec = ISO8601.toDate(str).getTime();
      } catch (Exception e) {
	reply_byInMillisec = new Date().getTime(); // now
      }
    }
  }	



  public void setReplyByDate(Date date) {
   reply_byInMillisec = date.getTime();
   reply_by=ISO8601.toString(date);
  }


  public void setProtocol( String str ) {
    if (str != null)
      protocol = new String(str);
  }

  public void setConversationId( String str ) {
    if (str != null)
      conversation_id = new String(str);
  }

 /**
  @deprecated Now <code>ACLMessage</code> class supports multiple
  receivers, so <code>addDest()</code>, <code>removeDest()</code> and
  <code> getDests()</code> should be used.
  @see jade.lang.acl.ACLMessage#addDest()
  @see jade.lang.ACLMessage#removeDest()
  @see jade.lang.acl.ACLMessage#getDests() 
 */
  public String getDest() {
    Enumeration e = dests.getMembers();
    String dest = null;
    if(e.hasMoreElements())
      dest = (String)e.nextElement();
    return dest;
  }

  public AgentGroup getDests() {
    return (AgentGroup)dests.clone();
  }

  public String getSource() {
    return source;
  }

  public String getType() {
    return msgType;
  }

  public String getContent() {
    return content;
  }

  public String getReplyWith() {
    return reply_with;
  }

  public String getReplyTo() {
    return in_reply_to;
  }



  public String getEnvelope() {
    return envelope;
  }

  public String getLanguage() {
    return language;
  }

  public String getOntology() {
    return ontology;
  }

  public String getReplyBy() {
    return reply_by;
  }

  public Date getReplyByDate() {
   return new Date(reply_byInMillisec);
  }

  public String getProtocol() {
    return protocol;
  }

  public String getConversationId() {
    return conversation_id;
  }
 
 /**
 * @deprecated Users should never use this method and it will soon go away.
 */
 public byte[] getMessage() {

    byte message[];
    int  msgLength=0;
    String dest = getDest();
    if (this.source != null) 
       msgLength = msgLength + this.SOURCE.length() + this.source.length();
    if (dest != null) 
       msgLength = msgLength + this.DEST.length() + dest.length();
    if (this.content != null) 
       msgLength = msgLength + this.CONTENT.length() + this.content.length();
    if (this.reply_with != null) 
       msgLength = msgLength + this.REPLY_WITH.length() + this.reply_with.length();
    if (this.in_reply_to != null) 
       msgLength = msgLength + this.IN_REPLY_TO.length() + this.in_reply_to.length();
    if (this.envelope != null) 
       msgLength = msgLength + this.ENVELOPE.length() + this.envelope.length();
    if (this.language != null) 
       msgLength = msgLength + this.LANGUAGE.length() + this.language.length();
    if (this.ontology != null) 
       msgLength = msgLength + this.ONTOLOGY.length() + this.ontology.length();
    if (this.reply_by != null) 
       msgLength = msgLength + this.REPLY_BY.length() + this.reply_by.length();
    if (this.protocol != null) 
       msgLength = msgLength + this.PROTOCOL.length() + this.protocol.length();
    if (this.conversation_id != null) 
       msgLength = msgLength + this.CONVERSATION_ID.length() + this.conversation_id.length();
   
    msgLength = msgLength + 3 + this.msgType.length();

    message = new byte[msgLength];

    int pos = 0; 
    int i = 0;
    message[pos++] = (byte)'(';
    for (i=0; i<this.msgType.length(); i++) 
       message[pos++] = (byte)this.msgType.charAt(i);
    if (this.source != null) { 
       for (i=0; i<this.SOURCE.length(); i++) 
          message[pos++] = (byte)this.SOURCE.charAt(i);
       for (i=0; i<this.source.length(); i++) 
          message[pos++] = (byte)this.source.charAt(i);
    }
    if (dest != null) { 
       for (i=0; i<this.DEST.length(); i++) 
          message[pos++] = (byte)this.DEST.charAt(i);
       for (i=0; i<dest.length(); i++) 
          message[pos++] = (byte)dest.charAt(i);
    }
    if (this.content != null) { 
       for (i=0; i<this.CONTENT.length(); i++) 
          message[pos++] = (byte)this.CONTENT.charAt(i);
       for (i=0; i<this.content.length(); i++) 
          message[pos++] = (byte)this.content.charAt(i);
    }
    if (this.reply_with != null) { 
       for (i=0; i<this.REPLY_WITH.length(); i++) 
          message[pos++] = (byte)this.REPLY_WITH.charAt(i);
       for (i=0; i<this.reply_with.length(); i++) 
          message[pos++] = (byte)this.reply_with.charAt(i);
    }
    if (this.in_reply_to != null) { 
       for (i=0; i<this.IN_REPLY_TO.length(); i++) 
          message[pos++] = (byte)this.IN_REPLY_TO.charAt(i);
       for (i=0; i<this.in_reply_to.length(); i++) 
          message[pos++] = (byte)this.in_reply_to.charAt(i);
    }
    if (this.envelope != null) { 
       for (i=0; i<this.ENVELOPE.length(); i++) 
          message[pos++] = (byte)this.ENVELOPE.charAt(i);
       for (i=0; i<this.envelope.length(); i++) 
          message[pos++] = (byte)this.envelope.charAt(i);
    }
    if (this.language != null) { 
       for (i=0; i<this.LANGUAGE.length(); i++) 
          message[pos++] = (byte)this.LANGUAGE.charAt(i);
       for (i=0; i<this.language.length(); i++) 
          message[pos++] = (byte)this.language.charAt(i);
    }
    if (this.ontology != null) { 
       for (i=0; i<this.ONTOLOGY.length(); i++) 
          message[pos++] = (byte)this.ONTOLOGY.charAt(i);
       for (i=0; i<this.ontology.length(); i++) 
          message[pos++] = (byte)this.ontology.charAt(i);
    }
    if (this.reply_by != null) { 
       for (i=0; i<this.REPLY_BY.length(); i++) 
          message[pos++] = (byte)this.REPLY_BY.charAt(i);
       for (i=0; i<this.reply_by.length(); i++) 
          message[pos++] = (byte)this.reply_by.charAt(i);
    }
    if (this.protocol != null) { 
       for (i=0; i<this.PROTOCOL.length(); i++) 
          message[pos++] = (byte)this.PROTOCOL.charAt(i);
       for (i=0; i<this.protocol.length(); i++) 
          message[pos++] = (byte)this.protocol.charAt(i);
    }
    if (this.conversation_id != null) { 
       for (i=0; i<this.CONVERSATION_ID.length(); i++) 
          message[pos++] = (byte)this.CONVERSATION_ID.charAt(i);
       for (i=0; i<this.conversation_id.length(); i++) 
          message[pos++] = (byte)this.conversation_id.charAt(i);
    }
    message[pos++] = (byte)')';
    message[pos] = (byte)'\n';
    // String s = new String( message, 0x00, 0, pos );
    // System.out.println(s);
    return message;
  }



  private static int counter = 0; // This variable is only used as a counter in dump()
  /**
     @deprecated This method dumps the message on System.out, so it's
     not suitable for use with GUIs or streams. Now fromText()/toText()
     methods allow reading and writing an ACL message on any stream;
     besides they are inverse of each other. Besides, this method will corrupt
     the ACL message object when more than one receiver is present.
     @see #toText(Writer w)
  */
  public void dump() {
    counter++;
    String dest = getDest();
    System.out.println( counter + ") " + msgType.toUpperCase());
    if (source != null)          System.out.println("   " + SOURCE + source);
    if (dest != null)             System.out.println("   " + DEST + dest);
    if (content != null)         System.out.println("   " + CONTENT + content);
    if (reply_with != null)      System.out.println("   " + REPLY_WITH + reply_with);
    if (in_reply_to != null)     System.out.println("   " + IN_REPLY_TO + in_reply_to);
    if (envelope != null)        System.out.println("   " + ENVELOPE + envelope);
    if (language != null)        System.out.println("   " + LANGUAGE + language);
    if (ontology != null)        System.out.println("   " + ONTOLOGY + ontology);
    if (reply_by != null)        System.out.println("   " + REPLY_BY + reply_by);
    if (protocol != null)        System.out.println("   " + PROTOCOL + protocol);
    if (conversation_id != null) System.out.println("   " + CONVERSATION_ID + conversation_id);
    System.out.println();
  }

  public void toText(Writer w) {
    try {
      w.write("(");
      w.write(msgType + "\n");
      if(source != null)
	w.write(SOURCE + " " + source + "\n");
      Enumeration e = dests.getMembers();
      if(e.hasMoreElements()) 
	w.write(DEST + "\n");
      while(e.hasMoreElements())
	w.write((String)e.nextElement() + "\n");
      if(content != null)
	w.write(CONTENT + " " + content + "\n");
      if(reply_with != null)
	w.write(REPLY_WITH + " " + reply_with + "\n");
      if(in_reply_to != null)
	w.write(IN_REPLY_TO + " " + in_reply_to + "\n");
      if(envelope != null)
	w.write(ENVELOPE + " " + envelope + "\n");
      if(language != null)
	w.write(LANGUAGE + " " + language + "\n");
      if(ontology != null)
	w.write(ONTOLOGY + " " + ontology + "\n");
      if(reply_by != null)
       w.write(REPLY_BY + " " + reply_by + "\n");
      if(protocol != null)
	w.write(PROTOCOL + " " + protocol + "\n");
      if(conversation_id != null)
	w.write(CONVERSATION_ID + " " + conversation_id + "\n");
      w.write(")");
      w.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public synchronized Object clone() {

    Object result;

    try {
      result = super.clone();
    }
    catch(CloneNotSupportedException cnse) {
      throw new InternalError(); // This should never happen
    }

    return result;
  }

  public String toString(){
    StringWriter text = new StringWriter();
    toText(text);
    return text.toString();
  }

 /**
 * This method is used by ACLParser to reset the data structure
 */
 public void reset() {
  source=null;
  dests=new AgentGroup();
  msgType=null;
  content=null;
  reply_with=null;
  in_reply_to=null;
  envelope=null;
  language=null;
  ontology=null;
  reply_by=null;
  reply_byInMillisec = new Date().getTime();
  protocol=null;
  conversation_id=null;
 }
}
