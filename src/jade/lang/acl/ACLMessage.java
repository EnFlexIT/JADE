
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
import java.io.Serializable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.lang.ClassNotFoundException;

import java.util.Enumeration;
import java.util.Date;
import java.util.Vector;

import jade.core.AgentGroup;

import starlight.util.Base64;

/**
 * The class ACLMessage implements an ACL message compliant to the <b>FIPA 97</b> specs.
 * All parameters are couples <em>keyword: value</em>.
 * All keywords are <code>private final String</code>.
 * All values can be set by using the methods <em>set</em> and can be read by using
 * the methods <em>get</em>. <p>
 * Notice that the <em>get</em> methods never
 * return null, rather they return an empty String. <p>
 * The methods <code> setContentObject() </code> and 
 * <code> getContentObject() </code> allow to send
 * serialized Java objects over the content of an ACLMessage.
 * These method are not FIPA compliant so thier usage is not encouraged.
 
 
 @author Fabio Bellifemine - CSELT
 @version $Date$ $Revision$

 */
 
public class ACLMessage implements Cloneable, Serializable {

  /** constant identifying the FIPA performative **/
  public static final int ACCEPT_PROPOSAL = 0;
  /** constant identifying the FIPA performative **/
  public static final int AGREE = 1;
  /** constant identifying the FIPA performative **/
  public static final int CANCEL = 2;
  /** constant identifying the FIPA performative **/
  public static final int CFP = 3;
  /** constant identifying the FIPA performative **/
  public static final int CONFIRM = 4;
  /** constant identifying the FIPA performative **/
  public static final int DISCONFIRM = 5;
  /** constant identifying the FIPA performative **/
  public static final int FAILURE = 6;
  /** constant identifying the FIPA performative **/
  public static final int INFORM = 7;
  /** constant identifying the FIPA performative **/
  public static final int INFORM_IF = 8;
  /** constant identifying the FIPA performative **/
  public static final int INFORM_REF = 9;
  /** constant identifying the FIPA performative **/
  public static final int NOT_UNDERSTOOD = 10;
  /** constant identifying the FIPA performative **/
  public static final int PROPOSE = 11;
  /** constant identifying the FIPA performative **/
  public static final int QUERY_IF = 12;
  /** constant identifying the FIPA performative **/
  public static final int QUERY_REF = 13;
  /** constant identifying the FIPA performative **/
  public static final int REFUSE = 14;
  /** constant identifying the FIPA performative **/
  public static final int REJECT_PROPOSAL = 15;
  /** constant identifying the FIPA performative **/
  public static final int REQUEST = 16;
  /** constant identifying the FIPA performative **/
  public static final int REQUEST_WHEN = 17;
  /** constant identifying the FIPA performative **/
  public static final int REQUEST_WHENEVER = 18;
  /** constant identifying the FIPA performative **/
  public static final int SUBSCRIBE = 19;
  /** constant identifying an unknown performative **/
  public static final int UNKNOWN = -1;
 
private int performative; // keeps the performative type of this object
  private static Vector performatives = new Vector(20);
  static { // initialization of the Vector of performatives
    performatives.addElement("ACCEPT-PROPOSAL");
    performatives.addElement("AGREE");
    performatives.addElement("CANCEL");
    performatives.addElement("CFP");
    performatives.addElement("CONFIRM");
    performatives.addElement("DISCONFIRM");
    performatives.addElement("FAILURE");
    performatives.addElement("INFORM");
    performatives.addElement("INFORM-IF");
    performatives.addElement("INFORM-REF");
    performatives.addElement("NOT-UNDERSTOOD");
    performatives.addElement("PROPOSE");
    performatives.addElement("QUERY-IF");
    performatives.addElement("QUERY-REF");
    performatives.addElement("REFUSE");
    performatives.addElement("REJECT-PROPOSAL");
    performatives.addElement("REQUEST");
    performatives.addElement("REQUEST-WHEN");
    performatives.addElement("REQUEST-WHENEVER");
    performatives.addElement("SUBSCRIBE");
  }

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

  private StringBuffer source = new StringBuffer();
  private AgentGroup dests = new AgentGroup();
  private StringBuffer content = new StringBuffer();
  private StringBuffer reply_with = new StringBuffer();
  private StringBuffer in_reply_to = new StringBuffer();
  private StringBuffer envelope = new StringBuffer();
  private StringBuffer language = new StringBuffer();
  private StringBuffer ontology = new StringBuffer();
  private StringBuffer reply_by = new StringBuffer();
  private long reply_byInMillisec;  
  private StringBuffer protocol = new StringBuffer();
  private StringBuffer conversation_id = new StringBuffer();

  /**
     @deprecated Since every ACL Message must have a message type, you
     should use the new constructor which gets a message type as a
     parameter.  To avoid problems, now this constructor silently sets
     the message type to <code>not-understood</code>.
     @see jade.lang.acl.ACLMessage#ACLMessage(String type)
  */
  public ACLMessage() {
    performative = NOT_UNDERSTOOD;
  }

  /**
    @deprecated It increases the probability of error when the passed
    String does not belong to the set of performatives supported by
    FIPA. This constructor creates an ACL message object with the
    specified type.    To avoid problems, the constructor <code>ACLMessage(int)</code>
    should be used instead.
     @param type The type of the communicative act represented by this
     message.
     @see jade.lang.acl.ACLMessage#ACLMessage(int type)
*/
  public ACLMessage(String type) {
    performative = performatives.indexOf(type.toUpperCase());
  }


  /**
   * This constructor creates an ACL message object with the specified
   * performative. If the passed integer does not correspond to any of
   * the known performatives, it silently initializes the message to
   * <code>not-understood</code>.
   **/
  public ACLMessage(int perf) {
    performative = perf;
  }

  /**
     Parses an ACL message object from a text representation. Using
     this static <em>Factory Method</em>, an <code>ACLMessage</code>
     object can be built starting from a character stream.
     @param r A redable stream containing a string representation of
     an ACL message.
     @see jade.lang.acl.ACLMessage#toText(Writer w)
  */
    public static ACLMessage fromText(Reader r) throws ParseException {
      ACLMessage msg = null;
      msg = ACLParser.create().parse(r);
      return msg;
    }

  /**
     Writes the <code>:sender</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param source The new value for the slot.
     @see jade.lang.acl.ACLMessage#getSource()
  */
  public void setSource( String source ) {
    if (source != null)
      this.source = new StringBuffer(source);
    else
      this.source = new StringBuffer();
  }

  /**
     @deprecated Now <code>ACLMessage</code> class supports multiple
     receivers, so <code>addDest()</code>, <code>removeDest()</code> and
     <code> getDests()</code> should be used. Currently, this
     method removes all previous receivers and inserts the one
     given.
     @see jade.lang.acl.ACLMessage#addDest(String dest)
     @see jade.lang.acl.ACLMessage#removeDest(String dest)
     @see jade.lang.acl.ACLMessage#getDests()
  */
  public void setDest( String dest ) {
    if (dest != null) {
      dests = new AgentGroup();
      dests.addMember(new String(dest));
    } else
      removeAllDests();
  }

  /**
     Adds a value to <code>:receiver</code> slot. <em><b>Warning:</b>
     no checks are made to validate the slot value.</em>
     @param dest The value to add to the slot value set.
  */
  public void addDest(String dest) {
    if (dest != null) 
      dests.addMember(new String(dest));
  }

  /**
     Removes a value from <code>:receiver</code>
     slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
     @param dest The value to remove from the slot value set.
  */
  public void removeDest(String dest) {
    if (dest != null)
      dests.removeMember(new String(dest));
  }

  /**
     Removes all values from <code>:receiver</code>
     slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em> 
  */
  public void removeAllDests() {
    dests.reset();
  }


  /**
    @deprecated Use <code>setPerformative</code> instead.
     Writes the message type. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param type The new value for the slot.
     @see jade.lang.acl.ACLMessage#setPerformative(int perf)
  */
  public void setType(String type) {
    if (type != null)
      performative = performatives.indexOf(type.toUpperCase());
    else
      performative = NOT_UNDERSTOOD;
  }

  /**
   * set the performative of this ACL message object to the passed constant.
   * Remind to 
   * use the set of constants (i.e. <code> INFORM, REQUEST, ... </code>)
   * defined in this class
   */
  public void setPerformative(int perf) {
    performative = perf;
  }

  /**
   * Writes the <code>:content</code> slot. <em><b>Warning:</b> no
   * checks are made to validate the slot value.</em> <p>
   * In order to transport serialized Java objects,
   * or arbitrary sequence of bytes (i.e. something different from 
   * a Java <code>String</code>) over an ACL message, it is suggested to use
   * the method <code>ACLMessage.setContentObject()</code> instead. 
   * @param content The new value for the slot.
   * @see jade.lang.acl.ACLMessage#getContent()
   * @see jade.lang.acl.ACLMessage#setContentObject(Serializable)
   */
  public void setContent( String content ) {
    if (content != null)
      this.content = new StringBuffer(content);
    else
      this.content = new StringBuffer();
  }





  /**
   * This method sets the current content of this ACLmessage to
   * the passed sequence of bytes. 
   * Base64 encoding is applied. <p>
   * This method should be used to write serialized Java objects over the 
   * content of an ACL Message to override the limitations of the Strings. <p>
   * For example, to write Java objects into the content: <br>
   * <PRE>
   *    ACLMessage msg;
   *    ByteArrayOutputStream c = new ByteArrayOutputStream();
   *    ObjectOutputStream oos = new ObjectOutputStream(c);
   *    oos.writeInt(1234); 
   *    oos.writeObject("Today"); 
   *    oos.writeObject(new Date()); 
   *    oos.flush();
   *    msg.setContentBase64(c.toByteArray());
   *
   * </PRE>   
   * 
   * @see jade.lang.acl.ACLMessage#getContentBase64()
   * @see jade.lang.acl.ACLMessage#getContent()
   * @see java.io.ObjectOutputStream#writeObject(Object)
   * @param bytes is the the sequence of bytes to be appended to the content of this message
   */
  private void setContentBase64(byte[] bytes) {
    try {
      content = new StringBuffer().append(Base64.encode(bytes));
    }
    catch(java.lang.NoClassDefFoundError jlncdfe) {
      System.err.println("\n\t===== E R R O R !!! =======\n");
      System.err.println("Missing support for Base64 conversions");
      System.err.println("Please refer to the documentation for details.");
      System.err.println("=============================================\n\n");
      System.err.println("");
      try {
	Thread.currentThread().sleep(3000);
      }
      catch(InterruptedException ie) {
      }

      content = new StringBuffer();
    }
  }

  /**
  * This method sets the content of this ACLMessage to a Java object.
  * It is not FIPA compliant so its usage is not encouraged.
  * For example:<br>
  * <PRE>
  * ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
  * Date d = new Date(); 
  * try{
  *  msg.setContentObject(d);
  * }catch(IOException e){}
  * </PRE>
  *
  * @param s the object that will be used to set the content of the ACLMessage. 
  * @exception IOException if an I/O error occurs.
  */
  public void setContentObject(Serializable s) throws IOException
  {
  	ByteArrayOutputStream c = new ByteArrayOutputStream();
  	ObjectOutputStream oos = new ObjectOutputStream(c);
  	oos.writeObject(s);
  	oos.flush();
  	setContentBase64(c.toByteArray());
  }
  /**
   * This method returns the content of this ACLmessage
   * after decoding according to Base64.
   * For example to read Java objects from the content 
   * (when they have been written by using the setContentBase64() method,: <br>
   * <PRE>
   *    ACLMessage msg;
   *    ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(msg.getContentBase64()));;
   *
   *    int i = oin.readInt();
   *    String today = (String)oin.readObject();
   *    Date date = (Date)oin.readObject();
   *
   * </PRE>   
   * @see jade.lang.acl.ACLMessage#setContentBase64(byte[])
   * @see jade.lang.acl.ACLMessage#getContent()
   * @see java.io.ObjectInputStream#readObject()
   */
  private byte[] getContentBase64() {
    try {
      char[] cc = new char[content.length()];
      content.getChars(0,content.length(),cc,0);
      return Base64.decode(cc);
    } catch(java.lang.StringIndexOutOfBoundsException e){
    		return new byte[0];
    }
    	catch(java.lang.NoClassDefFoundError jlncdfe) {
      	System.err.println("\t\t===== E R R O R !!! =======\n");
      	System.err.println("Missing support for Base64 conversions");
      	System.err.println("Please refer to the documentation for details.");
      	System.err.println("=============================================\n\n");
      	try {
					Thread.currentThread().sleep(3000);
      	}catch(InterruptedException ie) {
      	}
      	return new byte[0];
    }
    
  }
  /**
  * This method returns the content of this ACLMessage after decoding according to Base64.
  * It is not FIPA compliant so its usage is not encouraged.
  * For example to read Java objects from the content 
  * (when they have been written by using the setContentOnbject() method): <br>
  * <PRE>
  * ACLMessage msg = blockingReceive();
  * try{
  *  Date d = (Date)msg.getContentObject();
  * }catch(UnreadableException e){}
  * </PRE>
  * 
  * @return the object read from the content of this ACLMessage
  * @exception UnreadableException when an error occurs during the deconding.
  */
  public Serializable getContentObject() throws UnreadableException
  {
  	
  		try{
  			ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(getContentBase64()));
  		  Serializable s = (Serializable)oin.readObject();
  		  return s;
  		}catch (java.lang.Error e){
  		 throw new UnreadableException(e.getMessage());
  		}catch (IOException e1){
  			throw new UnreadableException(e1.getMessage());
  		}catch(ClassNotFoundException e2){
  			throw new UnreadableException(e2.getMessage());
  		}
  	
  }

  /**
     Writes the <code>:reply-with</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param reply The new value for the slot.
     @see jade.lang.acl.ACLMessage#getReplyWith()
  */
  public void setReplyWith( String reply ) {
    if (reply != null)
      reply_with = new StringBuffer(reply);
    else
      reply_with = new StringBuffer();
  }

  /**
     Writes the <code>:in-reply-to</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param reply The new value for the slot.
     @see jade.lang.acl.ACLMessage#getReplyTo()
  */
  public void setReplyTo( String reply ) {
    if (reply != null)
      in_reply_to = new StringBuffer(reply);
    else
      in_reply_to = new StringBuffer();
  }
  
  /**
     Writes the <code>:envelope</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getEnvelope()
  */
  public void setEnvelope( String str ) {
    if (str != null)
      envelope = new StringBuffer(str);
    else
      envelope = new StringBuffer();
  }

  /**
     Writes the <code>:language</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getLanguage()
  */
  public void setLanguage( String str ) {
    if (str != null)
      language = new StringBuffer(str);
    else
      language = new StringBuffer();
  }

  /**
     Writes the <code>:ontology</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getOntology()
  */
  public void setOntology( String str ) {
    if (str != null)
      ontology = new StringBuffer(str);
    else
      ontology = new StringBuffer();
  }

  /**
     Writes the <code>:reply-by</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot, as ISO8601 time.
     @see jade.lang.acl.ACLMessage#getReplyBy()
  */
  public void setReplyBy( String str ) {
    if (str != null) {
      reply_by = new StringBuffer(str);
      try {
	reply_byInMillisec = ISO8601.toDate(str).getTime();
      } catch (Exception e) {
	reply_byInMillisec = new Date().getTime(); // now
      }
    } else {
      reply_by = new StringBuffer();
      reply_byInMillisec = new Date().getTime();
    }
  }	

  /**
     Writes the <code>:reply-by</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param date The new value for the slot.
     @see jade.lang.acl.ACLMessage#getReplyByDate()
  */
  public void setReplyByDate(Date date) {
   reply_byInMillisec = date.getTime();
   reply_by = new StringBuffer(ISO8601.toString(date));
  }

  /**
     Writes the <code>:protocol</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getProtocol()
  */
  public void setProtocol( String str ) {
    if (str != null)
      protocol = new StringBuffer(str);
    else
      protocol = new StringBuffer();
  }

  /**
     Writes the <code>:conversation-id</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getConversationId()
  */
  public void setConversationId( String str ) {
    if (str != null)
      conversation_id = new StringBuffer(str);
    else
      conversation_id = new StringBuffer();
  }

 /**
  @deprecated Now <code>ACLMessage</code> class supports multiple
  receivers, so <code>addDest()</code>, <code>removeDest()</code> and
  <code>getDests()</code> should be used.
  @see jade.lang.acl.ACLMessage#addDest(String dest)
  @see jade.lang.acl.ACLMessage#removeDest(String dest)
  @see jade.lang.acl.ACLMessage#getDests() 
 */
  public String getDest() {
    return getFirstDest();
  }

  /**
     Reads <code>:receiver</code> slot.
     @return An <code>AgentGroup</code> containing the names of the
     receiver agents for this message.
  */
  public AgentGroup getDests() {
    return (AgentGroup)dests.clone();
  }

  /**
     Reads first value of <code>:receiver</code> slot.
     @return The first receiver agent name.
  */
  public String getFirstDest() {
    Enumeration e = dests.getMembers();
    if(e.hasMoreElements())
      return (String)e.nextElement();
    else
      return null;
  }

  /**
     Reads <code>:sender</code> slot.
     @return The value of <code>:sender</code>slot.
     @see jade.lang.acl.ACLMessage#setSource(String).
  */
  public String getSource() {
    return new String(source);
  }

  /**
     @deprecated Now suitable symbolic constants are to be used to
     represent ACL performatives. <code>getPerformative()</code>
     method should be used instead of this one.
     Reads message type.
     @return The value of the message type..
     @see jade.lang.acl.ACLMessage#getPerformative().

  */
  public String getType() {
    return getPerformative(performative); 
  }
  
  /**
    Returns the string corresponding to the integer for the performative
    @return the string corresponding to the integer for the performative; 
    "NOT-UNDERSTOOD" if the integer is out of range.
  */
  public static String getPerformative(int perf){
    try {
      return new String((String)performatives.elementAt(perf));
    } catch (Exception e) {
      return new String((String)performatives.elementAt(NOT_UNDERSTOOD));
    }
  }
    
  /**
    Returns the integer corresponding to the performative
    @returns the integer corresponding to the performative; -1 otherwise
  */
  public static int getInteger(String perf)
  {
    return performatives.indexOf(perf.toUpperCase());
    }

  /**
   * return the integer representing the performative of this object
   * @return an integer representing the performative of this object
   */
  public int getPerformative() {
    return performative;
  }

  /**
   * Reads <code>:content</code> slot. <p>
   * It is sometimes useful to transport serialized Java objects,
   * or arbitrary sequence of bytes (i.e. something different from 
   * a Java <code>String</code>) over an ACL message. See
   * getContentObject(). 
   * @return The value of <code>:content</code> slot.
   * @see jade.lang.acl.ACLMessage#setContent(String).
   * @see jade.lang.acl.ACLMessage#getContentObject(Serializable).
   * @see java.io.ObjectInputStream
  */
  public String getContent() {
    return new String(content);
  }

  /**
     Reads <code>:reply-with</code> slot.
     @return The value of <code>:reply-with</code>slot.
     @see jade.lang.acl.ACLMessage#setReplyWith(String).
  */
  public String getReplyWith() {
    return new String(reply_with);
  }

  /**
     Reads <code>:reply-to</code> slot.
     @return The value of <code>:reply-to</code>slot.
     @see jade.lang.acl.ACLMessage#setReplyTo(String).
  */
  public String getReplyTo() {
    return new String(in_reply_to);
  }



  /**
     Reads <code>:envelope</code> slot.
     @return The value of <code>:envelope</code>slot.
     @see jade.lang.acl.ACLMessage#setEnvelope(String).
  */
  public String getEnvelope() {
    return new String(envelope);
  }

  /**
     Reads <code>:language</code> slot.
     @return The value of <code>:language</code>slot.
     @see jade.lang.acl.ACLMessage#setLanguage(String).
  */
  public String getLanguage() {
    return new String(language);
  }

  /**
     Reads <code>:ontology</code> slot.
     @return The value of <code>:ontology</code>slot.
     @see jade.lang.acl.ACLMessage#setOntology(String).
  */
  public String getOntology() {
    return new String(ontology);
  }

  /**
     Reads <code>:reply-by</code> slot.
     @return The value of <code>:reply-by</code>slot, as a string.
     @see jade.lang.acl.ACLMessage#setReplyBy(String).
  */
  public String getReplyBy() {
    return new String(reply_by);
  }

  /**
     Reads <code>:reply-by</code> slot.
     @return The value of <code>:reply-by</code>slot, as a
     <code>Date</code> object.
     @see jade.lang.acl.ACLMessage#setReplyByDate(Date).
  */
  public Date getReplyByDate() {
   return new Date(reply_byInMillisec);
  }

  /**
     Reads <code>:protocol</code> slot.
     @return The value of <code>:protocol</code>slot.
     @see jade.lang.acl.ACLMessage#setProtocol(String).
  */
  public String getProtocol() {
    return new String(protocol);
  }

  /**
     Reads <code>:conversation-id</code> slot.
     @return The value of <code>:conversation-id</code>slot.
     @see jade.lang.acl.ACLMessage#setConversationId(String).
  */
  public String getConversationId() {
    return new String(conversation_id);
  }
 



  private static int counter = 0; // This variable is only used as a counter in dump()
  /**
     @deprecated This method dumps the message on System.out, so it's
     not suitable for use with GUIs or streams. Now
     <code>fromText()</code>/<code>toText()</code> methods allow
     reading and writing an ACL message on any stream; besides they
     are inverse of each other. Besides, this method will corrupt the
     ACL message object when more than one receiver is present.
     @see jade.lang.acl.ACLMessage#toText(Writer w)
  */
  public void dump() {
    counter++;
    System.out.println("\n"+counter+")"+toString()+"\n");
  }

  /**
     Writes an ACL message object on a stream as a character
     string. This method allows to write a string representation of an
     <code>ACLMessage</code> object onto a character stream.
     @param w A <code>Writer</code> object to write the message onto.
     @see jade.lang.acl.ACLMessage#fromText(Reader r)
  */
  public void toText(Writer w) {
    try {
      w.write("(");
      w.write(getPerformative(getPerformative()) + "\n");
      if(source.length() > 0)
	w.write(SOURCE + " " + source + "\n");
      Enumeration e = dests.getMembers();
      if(e.hasMoreElements()) 
	w.write(DEST + "\n");
	  if (dests.size() > 1)
		w.write("(");
      while(e.hasMoreElements())
	w.write((String)e.nextElement() + "\n");
	  if (dests.size() > 1)
		w.write(")");
      if(content.length() > 0)
	w.write(CONTENT + " " + content + "\n");
      if(reply_with.length() > 0)
	w.write(REPLY_WITH + " " + reply_with + "\n");
      if(in_reply_to.length() > 0)
	w.write(IN_REPLY_TO + " " + in_reply_to + "\n");
      if(envelope.length() > 0)
	w.write(ENVELOPE + " " + envelope + "\n");
      if(language.length() > 0)
	w.write(LANGUAGE + " " + language + "\n");
      if(ontology.length() > 0)
	w.write(ONTOLOGY + " " + ontology + "\n");
      if(reply_by.length() > 0)
       w.write(REPLY_BY + " " + reply_by + "\n");
      if(protocol.length() > 0)
	w.write(PROTOCOL + " " + protocol + "\n");
      if(conversation_id.length() > 0)
	w.write(CONVERSATION_ID + " " + conversation_id + "\n");
      w.write(")");
      w.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
     Clone an <code>ACLMessage</code> object.
     @return A copy of this <code>ACLMessage</code> object. The copy
     must be casted back to <code>ACLMessage</code> type before being
     used.
  */
  public synchronized Object clone() {

    ACLMessage result;

    try {
      result = (ACLMessage)super.clone();
      result.dests = getDests(); // Deep copy
    }
    catch(CloneNotSupportedException cnse) {
      throw new InternalError(); // This should never happen
    }

    return result;
  }

  /**
     Convert an ACL message to its string representation. This method
     writes a representation of this <code>ACLMessage</code> into a
     character string.
     @return A <code>String</code> representing this message.
     @see jade.lang.acl.ACLMessage#fromText(Reader r)
  */
  public String toString(){
    StringWriter text = new StringWriter();
    toText(text);
    return text.toString();
  }

 /**
  * Resets all the message slots.
 */
 public void reset() {
    /* Fabio, 26/8/99. Attenzione new StringBuffer(null) genera una
       NullPointerException. Allo stesso modo new String(null).
       Pertanto non inizializzare direttamente a null.*/
  source=new StringBuffer();
  dests=new AgentGroup();
  performative = NOT_UNDERSTOOD;
  content=new StringBuffer();
  reply_with=new StringBuffer();
  in_reply_to=new StringBuffer();
  envelope=new StringBuffer();
  language=new StringBuffer();
  ontology=new StringBuffer();
  reply_by=new StringBuffer();
  reply_byInMillisec = new Date().getTime();
  protocol=new StringBuffer();
  conversation_id=new StringBuffer();
 }

  /**
   * create a new ACLMessage that is a reply to this message.
   * In particular, it sets the following parameters of the new message:
   * receiver, language, ontology, protocol, conversation-id,
   * in-reply-to, reply-with.
   * The programmer needs to set the communicative-act and the content.
   * Of course, if he wishes to do that, he can reset any of the fields.
   * @return the ACLMessage to send as a reply
   */
public ACLMessage createReply() {
  ACLMessage m = (ACLMessage)clone();
  m.removeAllDests();
  m.addDest(getSource());
  m.setSource(null);
  //m.setSource(getLocalName());
  m.setReplyTo(getReplyWith());
  m.setReplyWith(getSource()+java.lang.System.currentTimeMillis()); 
  m.setReplyBy(null);
  m.setContent(null);
  m.setEnvelope(null);
  return m;
}

}
