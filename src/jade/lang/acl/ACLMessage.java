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
import jade.util.leap.Serializable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ClassNotFoundException;

import java.util.Date;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.util.leap.Properties;
import java.util.Enumeration;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.Envelope;


/**
 * The class ACLMessage implements an ACL message compliant to the <b>FIPA 2000</b> "FIPA ACL Message Structure Specification" (fipa000061) specifications.
 * All parameters are couples <em>keyword: value</em>.
 * All keywords are <code>private final String</code>.
 * All values can be set by using the methods <em>set</em> and can be read by using
 * the methods <em>get</em>. <p>
 * <p> The methods <code> setByteSequenceContent() </code> and 
 * <code> getByteSequenceContent() </code> allow to send arbitrary
 * sequence of bytes
 * over the content of an ACLMessage.
 * <p> The couple of methods 
 * <code> setContentObject() </code> and 
 * <code> getContentObject() </code> allow to send
 * serialized Java objects over the content of an ACLMessage.
 * These method are not strictly 
 * FIPA compliant so their usage is not encouraged.
 * @author Fabio Bellifemine - CSELT
 * @version $Date$ $Revision$
 * @see <a href=http://www.fipa.org/specs/fipa00061/XC00061D.html>FIPA Spec</a>
 */
 
public class ACLMessage implements Cloneable, Serializable {
	// Explicitly set for compatibility between standard and micro version
	private static final long serialVersionUID=3945353187608998130L;

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


 
  /**
  @serial
  */
  private AID source = null;

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
    // At a given time or content or byteSequenceContent are != null,
    // it is not allowed that both are != null
  private StringBuffer content = null;
  private byte[] byteSequenceContent = null;

  /**
  @serial
  */
  private StringBuffer reply_with = null;

  /**
  @serial
  */
  private StringBuffer in_reply_to = null;

  /**
  @serial
  */
  private StringBuffer encoding = null;

  /**
  @serial
  */
  private StringBuffer language = null;

  /**
  @serial
  */
  private StringBuffer ontology = null;

  /**
  @serial
  */
  private long reply_byInMillisec = 0; 

  /**
  @serial
  */
  private StringBuffer protocol = null;

  /**
  @serial
  */
  private StringBuffer conversation_id = null;

  /**
  @serial
  */
  private Properties userDefProps = new Properties();

  private Envelope messageEnvelope;

  //__BACKWARD_COMPATIBILITY__BEGIN
  /**
  Returns the list of the communicative acts.
  @deprecated Use getAllPerformativeNames() instead
  */
  public static java.util.List getAllPerformatives()
  {
  	return ((ArrayList) performatives).toList();
  }
  //__BACKWARD_COMPATIBILITY__END
  
  /**
  Returns the list of the communicative acts as an array of <code>String</code>.
  */
  public static String[] getAllPerformativeNames()
  {
  	// FIXME: performatives should become an array and this method should
  	// just return it.
  	String[] names = new String[performatives.size()];
  	int i = 0;
  	Iterator it = performatives.iterator();
  	while (it.hasNext()) {
  		names[i++] = (String) it.next();
  	}
  	return names;
  }
  
  /**
     @deprecated Since every ACL Message must have a message type, you
     should use the new constructor which gets a message type as a
     parameter.  To avoid problems, now this constructor silently sets
     the message type to <code>not-understood</code>.
     @see jade.lang.acl.ACLMessage#ACLMessage(int)
  */
  public ACLMessage() {
    performative = NOT_UNDERSTOOD;
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
      source = null;
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
   * <p>Notice that, in general, setting a String content and getting
   * back a byte sequence content - or viceversa - does not return
   * the same value, i.e. the following relation does not hold
   * <code>
   * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
   * is equal to getByteSequenceContent()
   * </code>
   * @param content The new value for the slot.
   * @see jade.lang.acl.ACLMessage#getContent()
   * @see jade.lang.acl.ACLMessage#setByteSequenceContent(byte[])
   * @see jade.lang.acl.ACLMessage#setContentObject(Serializable s)
   */
  public void setContent(String content) {
      byteSequenceContent = null; //make to null the other variable
    if (content != null)
      this.content = new StringBuffer(content);
    else
      this.content = null;
  }

  /**
   * Writes the <code>:content</code> slot. <em><b>Warning:</b> no
   * checks are made to validate the slot value.</em> <p>
   * <p>Notice that, in general, setting a String content and getting
   * back a byte sequence content - or viceversa - does not return
   * the same value, i.e. the following relation does not hold
   * <code>
   * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
   * is equal to getByteSequenceContent()
   * </code>
   * @param content The new value for the slot.
   * @see jade.lang.acl.ACLMessage#setContent()
   * @see jade.lang.acl.ACLMessage#getByteSequenceContent()
   * @see jade.lang.acl.ACLMessage#setContentObject(Serializable s)
   */
  public void setByteSequenceContent(byte[] content) {
      this.content = null; //make to null the other variable
      byteSequenceContent = content;
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
  public void setContentObject(java.io.Serializable s) throws IOException
  {
  	ByteArrayOutputStream c = new ByteArrayOutputStream();
  	ObjectOutputStream oos = new ObjectOutputStream(c);
  	oos.writeObject(s);
  	oos.flush();
	setByteSequenceContent(c.toByteArray());
  }


  /**
  * This method returns the content of this ACLMessage when they have
  * been written via the method <code>setContentObject</code>.
  * It is not FIPA compliant so its usage is not encouraged.
  * For example to read Java objects from the content 
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
  public java.io.Serializable getContentObject() throws UnreadableException
  {
  	
    try{
      ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(getByteSequenceContent()));
      java.io.Serializable s = (java.io.Serializable)oin.readObject();
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
      reply_with = null;
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
      in_reply_to = null;
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
      encoding = null;
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
      language = null;
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
      ontology = null;
  }


  /**
     Writes the <code>:reply-by</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param str The new value for the slot, as ISO8601 time.
     @see jade.lang.acl.ACLMessage#getReplyBy()
     @see jade.lang.acl.ACLMessage#setReplyByDate(Date)
     @deprecated The value of the <code>reply-by</code> slot
     must be a valid Date, the method <code>setReplyByDate</code> should
     be used that guarantees avoiding problems. If the passed
     parameter represents a wrong date, this method silently converts
     its value to null.
  */
  public void setReplyBy(String str) {
    if (str != null) {
      try {
	reply_byInMillisec = ISO8601.toDate(str).getTime();
      } catch (Exception e) {
	reply_byInMillisec = 0; 
      }
    } else {
      reply_byInMillisec = 0; 
    }
  }	

  /**
     Writes the <code>:reply-by</code> slot. <em><b>Warning:</b> no
     checks are made to validate the slot value.</em>
     @param date The new value for the slot.
     @see jade.lang.acl.ACLMessage#getReplyByDate()
  */
  public void setReplyByDate(Date date) {
      reply_byInMillisec = (date==null?0:date.getTime());
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
      protocol = null;
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
      conversation_id = null;
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
    if(source != null)
      return (AID)source.clone();
    else
      return null;
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
     * This method allows to check if the content of this ACLMessage
     * is a byteSequence or a String
     * @return true if it is a byteSequence, false if it is a String
     **/
    public boolean hasByteSequenceContent(){
	return (byteSequenceContent != null);
    }

  /**
   * Reads <code>:content</code> slot. <p>
   * <p>Notice that, in general, setting a String content and getting
   * back a byte sequence content - or viceversa - does not return
   * the same value, i.e. the following relation does not hold
   * <code>
   * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
   * is equal to getByteSequenceContent()
   * </code>
   * @return The value of <code>:content</code> slot.
   * @see jade.lang.acl.ACLMessage#setContent(String)
   * @see jade.lang.acl.ACLMessage#getByteSequenceContent()
   * @see jade.lang.acl.ACLMessage#getContentObject()
   * @see java.io.ObjectInputStream
  */
  public String getContent() {
    if(content != null)
      return new String(content);
    else if (byteSequenceContent != null)
	return new String(byteSequenceContent);
    return null;
  }

 /**
   * Reads <code>:content</code> slot. <p>
   * <p>Notice that, in general, setting a String content and getting
   * back a byte sequence content - or viceversa - does not return
   * the same value, i.e. the following relation does not hold
   * <code>
   * getByteSequenceContent(setByteSequenceContent(getContent().getBytes())) 
   * is equal to getByteSequenceContent()
   * </code>
   * @return The value of <code>:content</code> slot.
   * @see jade.lang.acl.ACLMessage#setContent(String)
   * @see jade.lang.acl.ACLMessage#getContent()
   * @see jade.lang.acl.ACLMessage#getContentObject()
   * @see java.io.ObjectInputStream
  */
  public byte[] getByteSequenceContent() {
      if (content != null) 
	  return content.toString().getBytes();
      else if (byteSequenceContent != null)
          return byteSequenceContent;
      return null;
  }

  /**
     Reads <code>:reply-with</code> slot.
     @return The value of <code>:reply-with</code>slot.
     @see jade.lang.acl.ACLMessage#setReplyWith(String).
  */
  public String getReplyWith() {
    if(reply_with != null)
      return new String(reply_with);
    else return null;
  }

  /**
     Reads <code>:reply-to</code> slot.
     @return The value of <code>:reply-to</code>slot.
     @see jade.lang.acl.ACLMessage#setInReplyTo(String).
  */
  public String getInReplyTo() {
    if(in_reply_to != null)
      return new String(in_reply_to);
    else return null;
  }



  /**
     Reads <code>:encoding</code> slot.
     @return The value of <code>:encoding</code>slot.
     @see jade.lang.acl.ACLMessage#setEncoding(String).
  */
  public String getEncoding() {
    if(encoding != null)
      return new String(encoding);
    else
      return null;
  }

  /**
     Reads <code>:language</code> slot.
     @return The value of <code>:language</code>slot.
     @see jade.lang.acl.ACLMessage#setLanguage(String).
  */
  public String getLanguage() {
    if(language != null)
      return new String(language);
    else
      return null;
  }

  /**
     Reads <code>:ontology</code> slot.
     @return The value of <code>:ontology</code>slot.
     @see jade.lang.acl.ACLMessage#setOntology(String).
  */
  public String getOntology() {
    if(ontology != null)
      return new String(ontology);
    else
      return null;
  }

  /**
     Reads <code>:reply-by</code> slot.
     @return The value of <code>:reply-by</code>slot, as a string.
     @see jade.lang.acl.ACLMessage#setReplyBy(String).
     @see jade.lang.acl.ACLMessage#getReplyByDate().
     @deprecated Since the value of this slot is a Date by definition, then
     the <code>getReplyByDate</code> should be used that returns a Date
  */
  public String getReplyBy() {
      if(reply_byInMillisec != 0)
	  return ISO8601.toString(new Date(reply_byInMillisec));
      else
	  return null;
  }

  /**
     Reads <code>:reply-by</code> slot.
     @return The value of <code>:reply-by</code>slot, as a
     <code>Date</code> object.
     @see jade.lang.acl.ACLMessage#setReplyByDate(Date).
  */
  public Date getReplyByDate() {
    if(reply_byInMillisec != 0)
      return new Date(reply_byInMillisec);
    else
      return null;
  }

  /**
     Reads <code>:protocol</code> slot.
     @return The value of <code>:protocol</code>slot.
     @see jade.lang.acl.ACLMessage#setProtocol(String).
  */
  public String getProtocol() {
    if(protocol != null)
      return new String(protocol);
    else
      return null;
  }

  /**
     Reads <code>:conversation-id</code> slot.
     @return The value of <code>:conversation-id</code>slot.
     @see jade.lang.acl.ACLMessage#setConversationId(String).
  */
  public String getConversationId() {
    if(conversation_id != null)
      return new String(conversation_id);
    else
      return null;
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
       if (key.startsWith("X-") || key.startsWith("x-"))
	 userDefProps.setProperty(":"+key,value);
       else
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
   * @param key the key that needs to be removed
     @return true if the property has been found and removed, false otherwise
   */
   public boolean removeUserDefinedParameter(String key) {
     return (userDefProps.remove(key) != null);
   }

  /**
     Attaches an envelope to this message. The envelope is used by the
     <b><it>ACC</it></b> for inter-platform messaging.
     @param e The <code>Envelope</code> object to attach to this
     message.
     @see jade.lang.acl#getEnvelope()
     @see jade.lang.acl#setDefaultEnvelope()
   */
  public void setEnvelope(Envelope e) {
    messageEnvelope = e;
  }


  /**
     Writes the message envelope for this message, using the
     <code>:sender</code> and <code>:receiver</code> message slots to
     fill in the envelope.
     @see jade.lang.acl#setEnvelope(Envelope e)
     @see jade.lang.acl#getEnvelope()
   */
  public void setDefaultEnvelope() {
    messageEnvelope = new Envelope();
    messageEnvelope.setFrom(source);
    Iterator it = dests.iterator();
    while(it.hasNext())
      messageEnvelope.addTo((AID)it.next());
    messageEnvelope.setAclRepresentation(StringACLCodec.NAME);
    messageEnvelope.setDate(new Date());
  }

  /**
     Reads the envelope attached to this message, if any.
     @return The envelope for this message.
     @see jade.lang.acl#setEnvelope(Envelope e)
     @see jade.lang.acl#setDefaultEnvelope()
   */
  public Envelope getEnvelope() {
    return messageEnvelope;
  }

  /**
     Writes an ACL message object on a stream as a character
     string. This method allows to write a string representation of an
     <code>ACLMessage</code> object onto a character stream.
     @param w A <code>Writer</code> object to write the message onto.
     @deprecated the <code>toString</code> method should be used instead
     of this method.
  */
  public void toText(Writer w) {
      try {
	  w.write(toString());
	  w.flush();
      } catch(IOException ioe) {
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
     If the content is a bytesequence, then it is automatically converted
     into Base64 encoding. 
     @return A <code>String</code> representing this message.
  */
  public String toString(){
      return StringACLCodec.toString(this);
  }


 /**
  * Resets all the message slots.
 */
 public void reset() {
  source = null;
  dests.clear();
  reply_to.clear();
  performative = NOT_UNDERSTOOD;
  content = null;
  byteSequenceContent = null;
  reply_with = null;
  in_reply_to = null;
  encoding = null;
  language = null;
  ontology = null;
  reply_byInMillisec = 0;
  protocol = null;
  conversation_id = null;
  userDefProps.clear();
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
    else
      m.setReplyWith("X"+java.lang.System.currentTimeMillis()); 
    m.setConversationId(getConversationId());
    m.setReplyByDate(null);
    m.setContent(null);
    m.setEncoding(null);
    
    //Set the Aclrepresentation of the reply message to the aclrepresentation of the sent message 
    if (messageEnvelope != null)
    {
	m.setDefaultEnvelope(); // reset the envelope after having been cloned
    	String aclCodec= messageEnvelope.getAclRepresentation();
    	if (aclCodec != null)
	    m.getEnvelope().setAclRepresentation(aclCodec);
    }
    else
    m.setEnvelope(null);

    return m;
  }


}
