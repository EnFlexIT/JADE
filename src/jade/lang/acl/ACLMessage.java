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
 $Id$
 $Log$
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

import java.io.Serializable;

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
  private String        dest;
  private String        msgType;
  private String        content;
  private String        reply_with;
  private String        in_reply_to;
  private String        envelope;
  private String        language;
  private String        ontology;
  private String        reply_by;
  private String        protocol;
  private String        conversation_id;


  public ACLMessage() {
  }


/**
 * <em>set</em> methods to set the actual values of parameters.
 * NOTICE: correctness of actual value of parameters is not checked
 */
  public void setSource( String source ) {
    this.source = new String(source);
  }

  public void setDest( String dest ) {
    this.dest = new String(dest);
  }

  public void setType( String type ) {
    msgType = new String(type);
  }

  public void setContent( String content ) {
    this.content = new String(content);
  }

  public void setReplyWith( String reply ) {
    reply_with = new String(reply);
  }

  public void setReplyTo( String reply ) {
    in_reply_to = new String(reply);
  }
  
  public void setEnvelope( String str ) {
    envelope = new String(str);
  }

  public void setLanguage( String str ) {
    language = new String(str);
  }

  public void setOntology( String str ) {
    ontology = new String(str);
  }

  public void setReplyBy( String str ) {
    reply_by = new String(str);
  }

  public void setProtocol( String str ) {
    protocol = new String(str);
  }

  public void setConversationId( String str ) {
    conversation_id = new String(str);
  }

 /**
 * <em>get</em> methods to read the actual values of parameters.
 */
  public String getDest() {
    return dest;
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

  public String getProtocol() {
    return protocol;
  }

  public String getConversationId() {
    return conversation_id;
  }
 
 /**
 * this method returns the message in a sequence of byte format.
 */
 public byte[] getMessage() {

    byte message[];
    int  msgLength=0;
    
    if (this.source != null) 
       msgLength = msgLength + this.SOURCE.length() + this.source.length();
    if (this.dest != null) 
       msgLength = msgLength + this.DEST.length() + this.dest.length();
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
    if (this.dest != null) { 
       for (i=0; i<this.DEST.length(); i++) 
          message[pos++] = (byte)this.DEST.charAt(i);
       for (i=0; i<this.dest.length(); i++) 
          message[pos++] = (byte)this.dest.charAt(i);
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
 * this method dumps the message.
 */
  public void dump() {
    counter++;	
    System.out.println( counter + ") " + msgType.toUpperCase());
    if (source != null)          System.out.println("   " + SOURCE + source);
    if (dest!= null)             System.out.println("   " + DEST + dest);
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

 /**
 * This method is used by ACLParser to reset the data structure
 */
 public void reset() {
  source=null;
  dest=null;
  msgType=null;
  content=null;
  reply_with=null;
  in_reply_to=null;
  envelope=null;
  language=null;
  ontology=null;
  reply_by=null;
  protocol=null;
  conversation_id=null;
 }
}
