/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.imtp.leap.http;

//#MIDP_EXCLUDE_FILE

import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import jade.util.Logger;
import jade.imtp.leap.JICP.Connection;

import java.io.*;
import java.net.*;

/**
 * Connection class to handle HTTP requests/responses on 
 * the server side.
 * Note that unlike the HTTPClientConnection that "looks 
 * like" a permanent pipe where several request/response sessions 
 * can occur, the HTTPServerConnection handles a single
 * request/response session.
 * @author Giovanni Caire - TILAB
 */
public class HTTPServerConnection extends Connection {

  private Socket sc;
  private InputStream  is;
  private OutputStream os;
  private OutputStream bos;
  private InputStream bis;
  private boolean readAvailable;
  private boolean writeAvailable;

  /**
   * Constructor declaration
   */
  public HTTPServerConnection(Socket s) {
  	sc = s;
		readAvailable = true;
		writeAvailable = true;
		
		// Create the output stream
		bos = new ByteArrayOutputStream() {
			public void flush() throws IOException {
				if (writeAvailable) {
					// Create an HTTPResponse having buf as payload 
					HTTPResponse response = new HTTPResponse();
					response.setCode("200");
					response.setMessage("OK");
					response.setHttpType("HTTP/1.0");
					response.setPayload(buf, 0, count);
					// Write the HTTPResponse to os and close the connection
  				os = sc.getOutputStream();
					response.writeTo(os);
  				HTTPServerConnection.this.close();
				}
				else {
					throw new IOException("Write not available");
				}
			}
		};
		
		// Create the input stream. Note that we do not extend 
		// ByteArrayInputStream as its read() method does not 
		// throw IOException.
		bis = new InputStream() {
			private byte[] buf;
			private int count = 0;
			private int pos = 0;
			public int read() throws IOException {
				if (pos == count) {
					// Fill the buffer
			    if (readAvailable) {
				    HTTPRequest request = new HTTPRequest();
  					is = sc.getInputStream();
				    request.readFrom(is);
				    buf = request.getPayload();
				    count = buf.length;
				    pos = 0;
				    readAvailable = false;
			    }
			    else {
						throw new IOException("Read not available");
					}
				}
				return (buf[pos++] & 0x000000ff);
			}
		};
  }

  /**
   */
  public OutputStream getOutputStream() throws IOException {
    return bos;
  } 

  /**
   */
  public InputStream getInputStream() throws IOException {
    return bis;
  } 

  /**
   */
  public void close() throws IOException {
		readAvailable = false;
		writeAvailable = false;
    if (is != null) {
      is.close();
      is = null;
    } 
    if (os != null) {
      os.close();
      os = null;
    } 
    if (sc != null) {
	    sc.close();
	    sc = null;
    }
  } 

  /**
   */
  public String getRemoteHost() throws Exception {
    return sc.getInetAddress().getHostAddress();
  }
}

