/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

The updating of this file to JADE 2.0 has been partially supported by the
IST-1999-10211 LEAP Project

This file refers to parts of the FIPA 99/00 Agent Message Transport
Implementation Copyright (C) 2000, Laboratoire d'Intelligence
Artificielle, Ecole Polytechnique Federale de Lausanne

GNU Lesser General Public License

This library is free software; you can redistribute it sand/or
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

/**
 * HTTPIO.java
 *
 *
 * @author Jose Antonio Exposito
 * @author MARISM-A Development group ( marisma-info@ccd.uab.es )
 * @version 0.1
 * @author Nicolas Lhuillier (Motorola Labs)
 * @version 1.0
 */


package jade.mtp.http;

import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import jade.domain.FIPAAgentManagement.Envelope;

import jade.util.Logger;

public class HTTPIO {
	
  // Response codes
  public static final String OK    = "200 OK";;
  public static final String ERROR = "406 Not Acceptable";
  public static final String UNAV  = "503 Service Unavailable";
  // HTTP constants
  public static final String HTTP1 = "HTTP/1.";
  public static final String PROXY = "Proxy-Connection: ";
  public static final String CR      = "\r\n";
  public static final String POST    = "POST";
  public static final String CONTENT = "Content-Type: ";
  public static final String CLENGTH = "Content-Length: ";
  public static final String MM      = "multipart/mixed";
  public static final String BND     = "boundary";
  public static final String APPLI   = "application/";
  public static final String CONN    = "Connection: "; 
  public static final String CLOSE   = "close"; 
  public static final String KA      = "Keep-Alive"; 
  public static final String HTTP    = "HTTP/1.1";
  public static final String CACHE   = "Cache-Control: no-cache";
  public static final String MIME    = "Mime-Version: 1.0";
  public static final String HOST    = "Host: ";
  public static final String DL      = "--";
  public static final String BLK     = "";
  
  private static Logger logger = Logger.getMyLogger(HTTPIO.class.getName());
  

  /* *********************************************** 
   *                 WRITE METHODS
   * ***********************************************/	
  
  
  /**
   * Write the message to the OutputStream associated to the Sender
   */
  public static void writeAll(BufferedWriter bw ,String msg) throws IOException {       
    bw.write(msg+"\n");
    bw.flush();
  }
  
  /**
   * Create a generic message of HTTP with the input msgCode 
   * and type of connection (close or Keep-Alive)
   */
	public static String createHTTPResponse(String msgCode, String type){
		StringBuffer msg = new StringBuffer(HTTP);
    msg.append(" ").append(msgCode).append(CR);
    msg.append(CONTENT).append("text/html").append(CR);
    msg.append(CACHE).append(CR);
    msg.append(CONN).append(type).append(CR);
    msg.append(CR);
    msg.append("<HTML><BODY><H1>").append(msgCode).append("</H1></BODY></HTML>");
		return msg.toString();        
	}
	      
  /**
   * Prepare the HTML header
   */
  public static StringBuffer createHTTPHeader(HTTPAddress host, int length, String policy, String boundary, boolean proxy) {
    //Put the header
    StringBuffer header = new StringBuffer(200);
    header.append(POST).append(" ");
    header.append(host.toString()).append(" ").append(HTTP).append(CR);
    header.append(CACHE).append(CR);
    header.append(MIME).append(CR);
    header.append(HOST).append(host.getHost()).append(":").append(host.getPort()).append(CR);
    
    header.append(CONTENT).append(MM).append(" ; ").append(BND).append("=\"");
    header.append(boundary).append("\"").append(CR);
    
    //put the Content-Length
    header.append(CLENGTH).append(length).append(CR);
    
    //put the Connection policy
    if (proxy) {
      header.append(PROXY).append(policy).append(CR);
    }
    else {
      header.append(CONN).append(policy).append(CR);
    }
    header.append(CR);
    return header;
  }

  /**
   * Prepare the HTML body
   */
  public static String createHTTPBody(Envelope env, String boundary, String payload) {
    //PREPARE BODY
    StringBuffer body = new StringBuffer(100);
    body.append("This is not part of the MIME multipart encoded message.").append(CR);
    body.append(DL).append(boundary).append(CR);
    
    //Insert The XML envelope
    // Put the Content-Type
    body.append(CONTENT).append(APPLI).append("xml").append(CR);
    body.append(CR); //A empty line
    env.setPayloadLength(new Long((long)payload.length()));
    body.append(XMLCodec.encodeXML(env));
    body.append("\n");
    //Put the boundary delimit.
    body.append(DL).append(boundary).append(CR);
    
    //Insert the ACL message
    //Put the Content-Type  
    String payloadEncoding = env.getPayloadEncoding();
    if ((payloadEncoding != null) && 
        (payloadEncoding.length() > 0)) {
      body.append(CONTENT).append(env.getAclRepresentation());
      body.append("; charset=").append(env.getPayloadEncoding());
    }			
    else {
      body.append(CONTENT).append(APPLI).append("text");	      
    }
    body.append(CR).append(CR);
			
    //ACL part
    //Insert the ACL payload
    body.append(payload).append(CR);
    //Put the final boundary
    body.append(DL).append(boundary).append(DL).append(CR);
    
    return body.toString();
  }


  /* *********************************************** 
   *             READS METHODS
   * ***********************************************/     

  /**
   * Blocks on read until something is available on the stream 
   * or the stream is closed
   */
  /*
    public static String blockOnRead(BufferedReader br) throws IOException {
    //Skip empty lines
    String line = null;
    while(BLK.equals(line=br.readLine()));
    return line;
    }
  */
  
  /** 
   * Parse the input message, this message is received from the master server 
   * @param type return type of connection: close or Keep-Alive
   */  
  public static String readAll(BufferedReader br, StringBuffer xml, StringBuffer acl, StringBuffer type) 
    throws IOException {
    
    //For the Control of sintaxis  
    String  host = null;
    //boolean foundMime       = false;
    boolean foundBoundary   = false;
    boolean findContentType = false;
    String  boundary = null;
    //String  line = null;
    String  typeConnection = null;
    
    //Reset the Buffers
    // NL: Not supported on PJava
    /*
      if(xml.length()>0)
      xml.delete(0,xml.length());
      if(acl.length()>0)
      acl.delete(0,acl.length());	   
      if(connection.length()>0)
      connection.delete(0,connection.length());
    */
    //try {
    String line;
    while(BLK.equals(line=br.readLine())); // skip empty lines
    
    if(line==null) throw new IOException();
       
    StringTokenizer st = new StringTokenizer(line);
    try {
      if(!(st.nextToken()).equalsIgnoreCase(POST) ) { 
        logger.log(Logger.WARNING,"Malformed POST");
        type.append(CLOSE);
        return ERROR;
      }
      st.nextToken(); // Consumme a token
      if(!(st.nextToken().toUpperCase().startsWith("HTTP/1."))) { 
        logger.log(Logger.WARNING,"Malformed HTTP/1.1 ");
        type.append(CLOSE);
        return ERROR;
      }
    }
    catch(NoSuchElementException nsee) {
      logger.log(Logger.WARNING,"Malformed start line !: "+line);
      type.append(CLOSE);
      return ERROR;
    }
    
    //Process rest of header
    while (!(line=br.readLine()).equals(BLK)) {
      if (line.startsWith(HOST)) {
        host = processLine(line); //De momento solo controlamos que este
      }
      /* // NL do not test MIME version for interoperability with other MTP 
         if (line.toLowerCase().startsWith(MIME.toLowerCase())) {		  
         foundMime = true;
         }
      */
      if (line.toLowerCase().startsWith(CONN.toLowerCase())) {
        typeConnection= processLine(line);
      }
      if (line.toLowerCase().startsWith(CONTENT.toLowerCase())) {	
        //Process the left part
        if (!(processLine(line).toLowerCase().startsWith(MM))) {
          logger.log(Logger.WARNING,"MULTIPART/MIXED");
          type.append(CLOSE);
          return ERROR;
        }
        //Process the right part
        int pos = line.indexOf(BND);
        if (pos == -1) {
          // Boundary on next line
          line=br.readLine();
          if ((pos = line.indexOf(BND)) == -1) {
            // Bounday not found
            logger.log(Logger.WARNING,"MIME boundary not found");
            type.append(CLOSE);
            return ERROR;
          }
        }
        line = line.substring(pos+BND.length());
        pos = line.indexOf("\"")+1;
        boundary = DL+line.substring(pos,line.indexOf("\"",pos));
        foundBoundary = true;
      }
    }//end while
       
    //if( !foundBoundary || !foundMime) {
    if(!foundBoundary) {
      logger.log(Logger.WARNING,"Mime header error");
      type.append(CLOSE);
      return ERROR;
    }
    
    if (typeConnection == null) {
      type.append(KA); //Default Connection
    }	
    else {
      type.append(typeConnection); //Connection of request
    }
      
    //jump to first  "--Boundary" 
    while((line=br.readLine()).equals(BLK)); // skip empty lines
    do {
      if (line.startsWith(boundary)) { 
        break;
      }
    }
    while(!(line=br.readLine()).equals(BLK));
    
    while((line=br.readLine()).equals(BLK)); // skip empty lines
    // Skip content-type
    do {    
      if(line.toLowerCase().startsWith(CONTENT.toLowerCase())) { 
        break;
      }
    }
    while(!(line=br.readLine()).equals(BLK));
    
    //Capture the XML part
    //Capture the message envelope
    while(!(line=br.readLine()).equals(boundary)) {
      if (! line.equals(BLK)) {
        xml.append(line); 
      }
    }
     
    //Capture the ACL part
    //JMP to ACLMessage
    while((line=br.readLine()).equals(BLK)); // skip empty lines
    // Skip content-type
    do {    
      if(line.toLowerCase().startsWith(CONTENT.toLowerCase())) { 
        break;
      }
    }
    while(!(line=br.readLine()).equals(BLK));
    //Create last boundary for capture the ACLMessage
    boundary = boundary+DL;
    //Capture the acl part.
    // skip blank lines
    while((line=br.readLine()).equals(BLK));
    // handle first line separately
    if (!line.equals(boundary)) {
      acl.append(line);
    }
    // then handle following lines and append a separator
    while(!(line=br.readLine()).equals(boundary)) {
      if (! line.equals(BLK)) {
        acl.append(" ").append(line); 
      }
    }
    
    return OK;
    /*
      }
      catch(NullPointerException npe) {
      // readLine returns null <--> EOF
      System.out.println("null pointer in readAll");
      //npe.printStackTrace();
      type.append(CLOSE);
      return ERROR;
      }
    */
  }
  
    
  /** 
   * Capture and return the code of response message, this message is received from client 
   */  
  public static int getResponseCode(BufferedReader br, StringBuffer type) 
    throws IOException {
    int responseCode = -1;
    try {
      String line = null; 
      //Capture and process the response message
      while (!(line=br.readLine()).startsWith(HTTP1));	
      //capture the response code	     
      responseCode= Integer.parseInt(processLine(line));
      //Read all message
      while(((line=br.readLine())!=null)&&(!line.equals(BLK))) {
        if (line.toLowerCase().startsWith(CONN.toLowerCase())) {
          type.append(processLine(line));
        }
        else if (line.toLowerCase().startsWith(PROXY.toLowerCase())) {
          type.append(processLine(line));
        }
      }
      if (type.length() == 0) {
        type.append(KA); //Default Connection type  
      }
      return responseCode;
    }
    catch(Exception e) {
      // Connection has been closed before we receive confirmation.
      // We do cannot know if message has been received
      type.append(CLOSE);
      return responseCode; // NOT OK
    }
  }
  
   
  /** 
   * return the next information of search in the line
   */
  private static String processLine(String line) 
    throws IOException {  
    
    StringTokenizer st = new StringTokenizer(line);
    try {
      st.nextToken(); // Consumme first token
      return st.nextToken();
    }
    catch(NoSuchElementException nsee) {
      throw new IOException("Malformed line !: "+line);
    }
  }

} // End of HTTPIO class 


