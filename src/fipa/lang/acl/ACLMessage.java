/*
 * $Id$
 */

package fipa.lang.acl;

import java.net.*;
import java.io.*;

/**
 * La classe aclMessage rappresenta l'astrazione di un messaggio ACL conforme alle
 * specifiche FIPA. Tutti i campi sono costituiti da una coppia <IT>keyword: value</IT>.
 * Le keyword sono delle strighe di tipo <em>final</em>, quindi parametri.
 * I valori sono settati con i metodi <em>set</em> e letti con i metodi <em>get</em>.
 */
public class ACLMessage implements Serializable {

  private final String SOURCE          = new String(" :sender ");
  private final String DEST            = new String(" :receiver ");
  private final String CONTENT         = new String(" :content ");
  private final String REPLY_WITH      = new String(" :reply-with ");
  private final String IN_REPLY_TO     = new String(" :in-reply-to ");
  private final String ENVELOPE        = new String(" :envelope ");
  private final String LANGUAGE        = new String(" :language ");
  private final String ONTOLOGY        = new String(" :ontology ");
  private final String REPLY_BY        = new String(" :reply-by ");
  private final String PROTOCOL        = new String(" :protocol ");
  private final String CONVERSATION_ID = new String(" :conversation-id ");

  private String        source;
  private String        dest;
  private String        msgType;
  private String        msgContent;
  private String        reply_with;
  private String        in_reply_to;
  private String        envelope;
  private String        language;
  private String        ontology;
  private String        reply_by;
  private String        protocol;
  private String        conversation_id;

  private int           srcPort;

/**
 * Questo e' l'array di byte che serve direttamente per la trasmissione sul socket.
 * @see Agent#send
 */
  private byte          message[];

/**
 * Questo e' un buffer usato durante la costruzione del messaggio dai metodi <em>set</em>.
 */
  private StringBuffer  tempMessage; // temporaneous message during preparation for transmission 

/**
 * Costruttore usato durante la fase di ricezione, nella quale non serve riempire
 * i campi durante la costruzione del messaggio.
 */
  public ACLMessage() {
    tempMessage = new StringBuffer();
  }

/**
 * Costruttore usato surante la fase di spedizione.
 * Costruisce e appende il campo <IT>source</IT>.
 * NOTICE: Agent name not compliant with section 7.5.1 of FIPA 97 Part I (at least target is missing)
 */
  public ACLMessage( String source, int port ) {
    this();
    this.source = new String( source );
    //this.source = new String( source + "@localhost:" + port );
    //this.source = new String( "iiop://localhost/" + source + ":" + port );
    tempMessage.append(SOURCE + this.source);
    srcPort     = port;
  }

/**
 * Metodi per il settaggio dei campi del messaggio ACL
 * NOTICE: correctness of actual value of parameters is not checked
 */
  public void setSource( String source ) {
    this.source = new String(source);
    tempMessage.append(SOURCE + source);
  }

  public void setDest( String dest ) {
    this.dest = new String(dest);
    tempMessage.append(DEST + dest);
  }

  public void setType( String type ) {
    msgType = new String(type);
    tempMessage.insert(0, "(" + type);
  }

  public void setContent( String content ) {
    msgContent = new String(content);
    tempMessage.append(CONTENT + content);
  }

  public void setReplyWith( String reply ) {
    reply_with = new String(reply);
    tempMessage.append(REPLY_WITH + reply);
  }

  public void setReplyTo( String reply ) {
    in_reply_to = new String(reply);
    tempMessage.append(IN_REPLY_TO + reply);
  }
  
  public void setEnvelope( String str ) {
    envelope = new String(str);
    tempMessage.append(ENVELOPE + str);
  }

  public void setLanguage( String str ) {
    language = new String(str);
    tempMessage.append(LANGUAGE + str);
  }

  public void setOntology( String str ) {
    ontology = new String(str);
    tempMessage.append(ONTOLOGY + str);
  }

  public void setReplyBy( String str ) {
    reply_by = new String(str);
    tempMessage.append(REPLY_BY + str);
  }

  public void setProtocol( String str ) {
    protocol = new String(str);
    tempMessage.append(PROTOCOL + str);
  }

  public void setConversationId( String str ) {
    conversation_id = new String(str);
    tempMessage.append(CONVERSATION_ID + str);
  }

  // Methods to get parameters of the message
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
    return msgContent;
  }

  public int getSrcPort() {
    return srcPort;
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
 * Metodo usato in preparazione alla vera fase di spedizione.
 * Costruisce l'array di bytes che verra' spedito sul socket a partire
 * dai valori dei campi appena settati.
 * N.B.: termina il messaggio con un <it>'\0'</it>.
 */
  public void send() {

    message = new byte[tempMessage.length()+2];

    int pos = 0;
    for( pos=0; pos<tempMessage.length(); pos++) message[pos] = (byte)tempMessage.charAt(pos);
    message[pos++] = (byte)')';
    message[pos] = (byte)'\n';
    String s = new String( message, 0x00, 0, pos );
    System.out.println(s);

  }

/**
 * Metodo usato per fare il parsing di un array di byte nel quale rintracciare
 * le coppie keyword-valore per poter riempire i campi del messaggio.
 * @param s E' l'array di byte di cui fare il parsing.
 */
  public void receive( String s ) {

    //System.out.println("receiving: " + s);
    try {
      StreamTokenizer st = new StreamTokenizer( new  StringBufferInputStream( s ));

      st.wordChars( '_', '_' );
      st.wordChars( '_', '_' );
      st.wordChars( ';', ';' );
      //st.wordChars( ':', ':' );
      st.wordChars( '/', '/' );
      st.wordChars( '"', '"' );
      st.wordChars( '?', '?' );
      st.wordChars( '&', '&' );
      st.wordChars( '%', '%' );
      st.wordChars( '=', '=' );
      st.wordChars( ''', ''' );
      st.wordChars( '@', '@' );
      st.quoteChar( '"' );

      st.nextToken();

      st.wordChars( '(', '(' );
      st.wordChars( ')', ')' );

      st.nextToken();
      StringBuffer tempString = new StringBuffer( st.sval );
      boolean after = false;
      int type = 2;

      st.nextToken();
      for(; st.ttype != st.TT_EOF; st.nextToken()) {

	if( st.ttype == ':' ) after = true;
	else if( st.ttype == st.TT_WORD ){
	  if( after ) {
	    if( st.sval.equalsIgnoreCase("sender") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 0;
	    } else if( st.sval.equalsIgnoreCase("receiver") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 1;
	    } else if( st.sval.equalsIgnoreCase("content") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 3;
	    } else if( st.sval.equalsIgnoreCase("reply-with") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 4;
	    } else if( st.sval.equalsIgnoreCase("in-reply-to") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 5;
	    } else if( st.sval.equalsIgnoreCase("ontology") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 6;
	    } else if( st.sval.equalsIgnoreCase("protocol") ) {
	      setOldString(tempString, type); tempString = new StringBuffer(); after = false; type = 7;
	    } else {
	      if( st.sval != null && st.sval.equals("agent-services") ) tempString.append(" "); 
	      tempString.append(":"); tempString.append( st.sval ); after = false; 
	    }
	  } else {
	    if( tempString.length() > 0 ) tempString.append(" ");
	    tempString.append( st.sval ); after = false;
	  }
	} else if( st.ttype == st.TT_NUMBER ) {
	  if( after ) { tempString.append(":"); after = false; }
	  else if( tempString.length() > 0 ) tempString.append(" ");
	  tempString.append( new Integer((int)st.nval).toString() );
	} else if( st.ttype == '"' ) {
	  if( tempString.length() > 0 ) tempString.append(" \"");
	  //System.out.println(" reading: " + st.sval);
	  tempString.append( st.sval );
	  tempString.append("\"");
	  after = false;
	}
      }
      setOldString(tempString, type);
    } catch( IOException ioe ) {
      ioe.printStackTrace();
    }

  }

/**
 * Metodo ausiliario usato durante la fase di parsing.
 */
  private void setOldString( StringBuffer s, int type ) {

    if( s != null ) {
      if( type == 0 )      source      = new String( s.toString() );
      else if( type == 1 ) dest        = new String( s.toString() );
      else if( type == 2 ) msgType     = new String( s.toString() );
      else if( type == 3 ) msgContent  = new String( s.toString() );
      else if( type == 4 ) reply_with  = new String( s.toString() );
      else if( type == 5 ) in_reply_to = new String( s.toString() );
      else if( type == 6 ) ontology    = new String( s.toString() );
      else if( type == 7 ) protocol    = new String( s.toString() );
    }

  }

/**
 * Metodo usato dall'agente chiamate per farsi restituire l'array di byte
 * ottenuto dopo aver riempito tutti i campi del messaggio.
 * @return L'array di byte pronto per essere spedito sul socket.
 */
  public byte[] getMessage() {
    return message;
  }


  private static int counter = 0; // This variable is only used as a counter in dump()
  public void dump() {

    counter++;	
    System.out.println( counter + ") " + msgType.toUpperCase());
    if (source != null && source.length() > 0)                   System.out.println("   " + SOURCE + source);
    if (dest!= null && dest.length() > 0)                        System.out.println("   " + DEST + dest);
    if (msgContent != null && msgContent.length() > 0)           System.out.println("   " + CONTENT + msgContent);
    if (reply_with != null && reply_with.length() > 0)           System.out.println("   " + REPLY_WITH + reply_with);
    if (in_reply_to != null && in_reply_to.length() > 0)         System.out.println("   " + IN_REPLY_TO + in_reply_to);
    if (envelope != null && envelope.length() > 0)               System.out.println("   " + ENVELOPE + envelope);
    if (language != null && language.length() > 0)               System.out.println("   " + LANGUAGE + language);
    if (ontology != null && ontology.length() > 0)               System.out.println("   " + ONTOLOGY + ontology);
    if (reply_by != null && reply_by.length() > 0)               System.out.println("   " + REPLY_BY + reply_by);
    if (protocol != null && protocol.length() > 0)               System.out.println("   " + PROTOCOL + protocol);
    if (conversation_id != null && conversation_id.length() > 0) System.out.println("   " + CONVERSATION_ID + conversation_id);
    System.out.println();
  }

/**
 * Metodo usato per duplicare un messaggio appena letto e riempire il buffer <it>tempMessage</it>.
 * In questo modo il messaggio puo' essere rispedito, ad esempio dall'ACC, ad una nuova destinazione.
 * @see dfWorker#parseMsg
 */
  public void duplicate() {

    tempMessage.append("(" + msgType + " ");
    if (source.length() > 0)                                     tempMessage.append(SOURCE + source);
    if (dest.length() > 0)                                       tempMessage.append(DEST + dest);
    if (msgContent != null && msgContent.length() > 0)           tempMessage.append(CONTENT + msgContent);
    if (reply_with != null && reply_with.length() > 0)           tempMessage.append(REPLY_WITH + reply_with);
    if (in_reply_to != null && in_reply_to.length() > 0)         tempMessage.append(IN_REPLY_TO + in_reply_to);
    if (envelope != null && envelope.length() > 0)               tempMessage.append(ENVELOPE + envelope);
    if (language != null && language.length() > 0)               tempMessage.append(LANGUAGE + language);
    if (ontology != null && ontology.length() > 0)               tempMessage.append(ONTOLOGY + ontology);
    if (reply_by != null && reply_by.length() > 0)               tempMessage.append(REPLY_BY + reply_by);
    if (protocol != null && protocol.length() > 0)               tempMessage.append(PROTOCOL + protocol);
    if (conversation_id != null && conversation_id.length() > 0) tempMessage.append(CONVERSATION_ID + conversation_id);
  }

}
