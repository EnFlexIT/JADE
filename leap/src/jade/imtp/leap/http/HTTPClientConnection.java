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

import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import jade.imtp.leap.JICP.Connection;

import java.io.*;
//#MIDP_EXCLUDE_BEGIN
import java.net.*;
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
#MIDP_INCLUDE_END*/

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class HTTPClientConnection extends Connection {

	//#MIDP_EXCLUDE_BEGIN
  private HttpURLConnection hc;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
  private HttpConnection hc;
	#MIDP_INCLUDE_END*/
  private String url;
  private InputStream  is;
  private OutputStream os;
  private OutputStream bos;
  private InputStream bis;
  private boolean opened;

  /**
   * Constructor declaration
   */
  public HTTPClientConnection(TransportAddress ta) {
    url = "http://"+ta.getHost()+":"+ta.getPort()+"/jade";
  	opened = false;
		
		// Create the output stream
		bos = new ByteArrayOutputStream() {
			public void flush() throws IOException {
				try {
					// Be sure the connection is closed
					if (!opened) {
			    	//#MIDP_EXCLUDE_BEGIN
						hc = (HttpURLConnection) (new URL(url)).openConnection();
						hc.setDoOutput(true);
						hc.setRequestMethod("POST");
						hc.connect();
						os = hc.getOutputStream();
			    	//#MIDP_EXCLUDE_END
			    	/*#MIDP_INCLUDE_BEGIN
				    hc = (HttpConnection) Connector.open(url, Connector.READ_WRITE, false);
						hc.setRequestMethod(HttpConnection.POST);
			      os = hc.openOutputStream();
			    	#MIDP_INCLUDE_END*/
						os.write(buf, 0, count);
						opened = true;
					}
					else {
						throw new IOException("Can't write on an opened connection");
					}
				}
				finally {
					// Reset the ByteArrayOutputStream even if flush() failed
					reset();
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
				if (pos >= count) {
					// Fill the buffer
			    if (opened) {
				    //#MIDP_EXCLUDE_BEGIN
						is = hc.getInputStream();
						int length = hc.getContentLength();
				    //#MIDP_EXCLUDE_END
				    /*#MIDP_INCLUDE_BEGIN
						is = hc.openInputStream();
			    	int length = (int) hc.getLength();
				    #MIDP_INCLUDE_END*/
			    	buf = new byte[length];
			    	is.read(buf);
				    count = buf.length;
				    pos = 0;
	  				HTTPClientConnection.this.close();
			    }
			    else {
						throw new IOException("Can't read from a closed connection");
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
  	opened = false;
    if (is != null) {
      is.close();
      is = null;
    } 
    if (os != null) {
      os.close();
      os = null;
    } 
    if (hc != null) {
    	//#MIDP_EXCLUDE_BEGIN
    	hc.disconnect();
    	//#MIDP_EXCLUDE_END
    	/*#MIDP_INCLUDE_BEGIN
	    hc.close();
    	#MIDP_INCLUDE_END*/
	    hc = null;
    }
  } 

  //#MIDP_EXCLUDE_BEGIN
  /**
   */
  public String getRemoteHost() throws Exception {
  	throw new Exception("Unsupported operation");
  }
  //#MIDP_EXCLUDE_END
}

