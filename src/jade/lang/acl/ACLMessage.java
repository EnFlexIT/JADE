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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Properties;
import java.util.Enumeration;

import jade.domain.FIPAAgentManagement.AID;

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
 * These method are not FIPA compliant so their usage is not encouraged.
 
 
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
  /** constant identifying the FIPA performative **/
  public static final int PROXY = 20;
  /** constant identifying the FIPA performative **/
  public static final int PROPAGATE = 21;
  /** constant identifying an unknown performative **/
  public static final int UNKNOWN = -1;
  
/**
@serial
*/
private int performative; // keeps the performative type of this object
  private static List performatives = new ArrayList(22);
  static { // initialization of the Vector of performatives
    performatives.add("ACCEPT-PROPOSAL");
    performatives.add("AGREE");
    performatives.add("CANCEL");
    performatives.add("CFP");
    performatives.add("CONFIRM");
    performatives.add("DISCONFIRM");
    performatives.add("FAILURE");
    performatives.add("INFORM");
    performatives.add("INFORM-IF");
    performatives.add("INFORM-REF");
    performatives.add("NOT-UNDERSTOOD");
    performatives.add("PROPOSE");
    performatives.add("QUERY-IF");
    performatives.add("QUERY-REF");
    performatives.add("REFUSE");
    performatives.add("REJECT-PROPOSAL");
    performatives.add("REQUEST");
    performatives.add("REQUEST-WHEN");
    performatives.add("REQUEST-WHENEVER");
    performatives.add("SUBSCRIBE");
    performatives.add("PROXY");
    performatives.add("PROPAGATE");
  }

  private static final String SENDER          = new String(" :sender ");
  private static final String RECEIVER        = new String(" :receiver ");
  private static final String CONTENT         = new String(" :content ");
  private static final String REPLY_WITH      = new String(" :reply-with ");
  private static final String IN_REPLY_TO     = new String(" :in-reply-to ");
  private static final String REPLY_TO        = new String(" :reply-to ");
  private static final String LANGUAGE        = new String(" :language ");
  private static final String ENCODING        = new String(" :encoding ");
  private static final String ONTOLOGY        = new String(" :ontology ");
  private static final String REPLY_BY        = new String(" :reply-by ");
  private static final String PROTOCOL        = new String(" :protocol ");
  private static final String CONVERSATION_ID = new String(" :conversation-id ");
 
  /**
  @serial
  */
  private AID source = new AID();

  /**
  @serial
  */
  private ArrayList dests = new ArrayList();

  /**
  @serial
  */
  private ArrayList reply_to = new ArrayList();

  /**
  @serial
  */
  private StringBuffer content = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer reply_with = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer in_reply_to = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer encoding = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer language = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer ontology = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer reply_by = new StringBuffer();

  /**
  @serial
  */
  private long reply_byInMillisec; 

  /**
  @serial
  */
  private StringBuffer protocol = new StringBuffer();

  /**
  @serial
  */
  private StringBuffer conversation_id = new StringBuffer();

  /**
  @serial
  */
  private Properties userDefProps = new Properties();

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
     Writes the <code>:sender</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param source The new value for the slot.
     @see jade.lang.acl.ACLMessage#getSender()
  */
  public void setSender(AID s) {
    if (s != null)
      source = (AID)s.clone();
    else
      source = new AID();
  }

  /**
     Adds a value to <code>:receiver</code> slot. <em><b>Warning:</b>
     no checks are made to validate the slot value.</em>
     @param r The value to add to the slot value set.
  */
  public void addReceiver(AID r) {
    if(r != null)
      dests.add(r);
  }

  /**
     Removes a value from <code>:receiver</code>
     slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
     @param r The value to remove from the slot value set.
     @return true if the AID has been found and removed, false otherwise
  */
  public boolean removeReceiver(AID r) {
    if (r != null)
      return dests.remove(r);
    else
      return false;
  }

  /**
     Removes all values from <code>:receiver</code>
     slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em> 
  */
  public void clearAllReceiver() {
    dests.clear();
  }



  /**
     Adds a value to <code>:reply-to</code> slot. <em><b>Warning:</b>
     no checks are made to validate the slot value.</em>
     @param dest The value to add to the slot value set.
  */
  public void addReplyTo(AID dest) {
    if (dest != null) 
      reply_to.add(dest);
  }

  /**
     Removes a value from <code>:reply_to</code>
     slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
     @param dest The value to remove from the slot value set.
     @return true if the AID has been found and removed, false otherwise
  */
  public boolean removeReplyTo(AID dest) {
    if (dest != null)
      return reply_to.remove(dest);
    else
      return false;
  }

  /**
     Removes all values from <code>:reply_to</code>
     slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em> 
  */
  public void clearAllReplyTo() {
    reply_to.clear();
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
   * @see jade.lang.acl.ACLMessage#setContentObject(Serializable s)
   */
  public void setContent(String content) {
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
   * @see java.io.ObjectOutputStream#writeObject(Object o)
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
    }
    catch (java.lang.Error e) {
      throw new UnreadableException(e.getMessage());
    }
    catch (IOException e1) {
      throw new UnreadableException(e1.getMessage());
    }
    catch(ClassNotFoundException e2) {
      throw new UnreadableException(e2.getMessage());
    }

  }

  /**
     Writes the <code>:reply-with</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param reply The new value for the slot.
     @see jade.lang.acl.ACLMessage#getReplyWith()
  */
  public void setReplyWith(String reply) {
    if (reply != null)
      reply_with = new StringBuffer(reply);
    else
      reply_with = new StringBuffer();
  }

  /**
     Writes the <code>:in-reply-to</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param reply The new value for the slot.
     @see jade.lang.acl.ACLMessage#getInReplyTo()
  */
  public void setInReplyTo(String reply) {
    if (reply != null)
      in_reply_to = new StringBuffer(reply);
    else
      in_reply_to = new StringBuffer();
  }
  
  /**
     Writes the <code>:encoding</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getEncoding()
  */
  public void setEncoding(String str) {
    if (str != null)
      encoding = new StringBuffer(str);
    else
      encoding = new StringBuffer();
  }

  /**
     Writes the <code>:language</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot.
     @see jade.lang.acl.ACLMessage#getLanguage()
  */
  public void setLanguage(String str) {
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
  public void setOntology(String str) {
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
  public void setReplyBy(String str) {
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
     Reads <code>:receiver</code> slot.
     @return An <code>Iterator</code> containing the Agent IDs of the
     receiver agents for this message.
  */
  public Iterator getAllReceiver() {
    return dests.iterator();
  }

  /**
     Reads <code>:reply_to</code> slot.
     @return An <code>Iterator</code> containing the Agent IDs of the
     reply_to agents for this message.
  */
  public Iterator getAllReplyTo() {
    return reply_to.iterator();
  }

  /**
     Reads <code>:sender</code> slot.
     @return The value of <code>:sender</code>slot.
     @see jade.lang.acl.ACLMessage#setSender(AID).
  */
  public AID getSender() {
    return (AID)source.clone();
  }

  /**
    Returns the string corresponding to the integer for the performative
    @return the string corresponding to the integer for the performative; 
    "NOT-UNDERSTOOD" if the integer is out of range.
  */
  public static String getPerformative(int perf){
    try {
      return new String((String)performatives.get(perf));
    } catch (Exception e) {
      return new String((String)performatives.get(NOT_UNDERSTOOD));
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
   * @see jade.lang.acl.ACLMessage#setContent(String)
   * @see jade.lang.acl.ACLMessage#getContentObject()
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
     @see jade.lang.acl.ACLMessage#setInReplyTo(String).
  */
  public String getInReplyTo() {
    return new String(in_reply_to);
  }



  /**
     Reads <code>:encoding</code> slot.
     @return The value of <code>:encoding</code>slot.
     @see jade.lang.acl.ACLMessage#setEncoding(String).
  */
  public String getEncoding() {
    return new String(encoding);
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
 


  /**
   * Add a new user defined parameter to this ACLMessage.
   * Notice that according to the FIPA specifications, the keyword of a
   * user-defined parameter must start with the String ":X-". 
   * If it does not, then this method adds the prefix silently!
   * @param key the property key.
   * @param value the property value
  **/
   public void addUserDefinedParameter(String key, String value) {
     if (key.startsWith(":X-") || key.startsWith(":x-"))
       userDefProps.setProperty(key,value);
     else {
       System.err.println("WARNING: ACLMessage.addUserDefinedParameter. The key must start with :X-. Prefix has been silently added.");
       userDefProps.setProperty(":X-"+key,value);
     }
   }

    /**
     * Searches for the user defined parameter with the specified key. 
     * The method returns
     * <code>null</code> if the parameter is not found.
     *
     * @param   key   the parameter key.
     * @return  the value in this ACLMessage with the specified key value.
     */
   public String getUserDefinedParameter(String key){
     return userDefProps.getProperty(key);
   }

  /**
   * get a clone of the data structure with all the user defined parameters
   **/
   public Properties getAllUserDefinedParameters() {
     return (Properties)userDefProps.clone();
   }

  /**
   * Removes the key and its corresponding value from the list of user
   * defined parameters in this ACLMessage.
   * @ param key the key that needs to be removed
     @return true if the property has been found and removed, false otherwise
   */
   public boolean removeUserDefinedParameter(String key) {
     return (userDefProps.remove(key) != null);
   }

  /**
     Writes an ACL message object on a stream as a character
     string. This method allows to write a string representation of an
     <code>ACLMessage</code> object onto a character stream.
     @param w A <code>Writer</code> object to write the message onto.
     @see jade.lang.acl.ACLMessage#fromText(Reader r)
  */
  void toText(Writer w) {
    try {
      w.write("(");
      w.write(getPerformative(getPerformative()) + "\n");
      w.write(SENDER + " ");
      source.toText(w);
      w.write("\n");
      if (dests.size() > 0) {
	w.write(RECEIVER + " (set ");
	Iterator it = dests.iterator();
	while(it.hasNext()) {
	  ((AID)it.next()).toText(w);
	  w.write(" ");
	}
	w.write(")\n");
      }
      if (reply_to.size() > 0) {
	w.write(REPLY_TO + " (set \n");
	Iterator it = reply_to.iterator();
	while(it.hasNext()) {
	  ((AID)it.next()).toText(w);
	  w.write(" ");
	}
	w.write(")\n");
      }
      if(content.length() > 0)
	w.write(CONTENT + " " + content + "\n");
      if(reply_with.length() > 0)
	w.write(REPLY_WITH + " " + reply_with + "\n");
      if(in_reply_to.length() > 0)
	w.write(IN_REPLY_TO + " " + in_reply_to + "\n");
      if(encoding.length() > 0)
	w.write(ENCODING + " " + encoding + "\n");
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
      Enumeration e = userDefProps.propertyNames();
      String tmp;
      while (e.hasMoreElements()) {
	tmp = (String)e.nextElement();
	w.write(" " + tmp + " " + userDefProps.getProperty(tmp) + "\n");
      }
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
      result.dests = (ArrayList)dests.clone();       // Deep copy
      result.reply_to = (ArrayList)reply_to.clone(); // Deep copy
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
  source=new AID();
  dests.clear();
  reply_to.clear();
  performative = NOT_UNDERSTOOD;
  content=new StringBuffer();
  reply_with=new StringBuffer();
  in_reply_to=new StringBuffer();
  encoding=new StringBuffer();
  language=new StringBuffer();
  ontology=new StringBuffer();
  reply_by=new StringBuffer();
  reply_byInMillisec = new Date().getTime();
  protocol=new StringBuffer();
  conversation_id=new StringBuffer();
  userDefProps = new Properties();
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
    m.clearAllReceiver();
    Iterator it = reply_to.iterator();
    while (it.hasNext())
      m.addReceiver((AID)it.next());
    if (reply_to.isEmpty())
      m.addReceiver(getSender());
    m.clearAllReplyTo();
    m.setLanguage(getLanguage());
    m.setOntology(getOntology());
    m.setProtocol(getProtocol());
    m.setSender(null);
    m.setInReplyTo(getReplyWith());
    if (source != null)
      m.setReplyWith(source.getName() + java.lang.System.currentTimeMillis()); 
    m.setConversationId(getConversationId());
    m.setReplyBy(null);
    m.setContent(null);
    m.setEncoding(null);
    return m;
  }

}
