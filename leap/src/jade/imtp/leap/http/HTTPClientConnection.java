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
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.JICPPacket;

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
class HTTPClientConnection extends Connection {

	static final String RECIPIENT_ID_FIELD = "recipient-id";
	
	//#MIDP_EXCLUDE_BEGIN
  private HttpURLConnection hc;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
  private HttpConnection hc;
	#MIDP_INCLUDE_END*/
  private String url;
  private InputStream  is;
  private OutputStream os;
  private boolean opened;
  
  /**
   * Constructor declaration
   */
  public HTTPClientConnection(TransportAddress ta) {
    url = "http://"+ta.getHost()+":"+ta.getPort()+"/jade";
    opened = false;
  }
  
  public int writePacket(JICPPacket pkt) throws IOException {
  	if (!opened) {
  		int ret = 0;
	  	//#MIDP_EXCLUDE_BEGIN
			hc = (HttpURLConnection) (new URL(url)).openConnection();
			hc.setDoOutput(true);
			hc.setRequestMethod("POST");
			hc.connect();
			os = hc.getOutputStream();
	    ret = pkt.writeTo(os);
	  	//#MIDP_EXCLUDE_END
			
	  	/*#MIDP_INCLUDE_BEGIN
			hc = (HttpConnection) Connector.open(url, Connector.READ_WRITE, false);
			if (pkt.getType() == JICPProtocol.CONNECT_MEDIATOR_TYPE) {
				hc.setRequestMethod(HttpConnection.GET);
				hc.setRequestProperty(RECIPIENT_ID_FIELD, pkt.getRecipientID());
  		}
  		else {
				hc.setRequestMethod(HttpConnection.POST);
		    os = hc.openOutputStream();
	    	ret = pkt.writeTo(os);
  		}
	    #MIDP_INCLUDE_END*/
	    
	    opened = true;
	    return ret;
  	}
  	else {
  		throw new IOException("Write not available");
  	}
  }
  
  public JICPPacket readPacket() throws IOException {
    if (opened) {
    	try {
		    //#MIDP_EXCLUDE_BEGIN
				is = hc.getInputStream();
		    //#MIDP_EXCLUDE_END
		    /*#MIDP_INCLUDE_BEGIN
				is = hc.openInputStream();
		    #MIDP_INCLUDE_END*/
		    
		    return JICPPacket.readFrom(is);
    	}
    	finally {
    		try {
	    		close();
    		}
    		catch (Exception e) {
    		}
    	}
    }
    else {
			throw new IOException("Can't read from a closed connection");
    }
  }
						
  /**
   */
  public void close() throws IOException {
  	opened = false;
		try {is.close();} catch (Exception e) {}
    is = null;
		try {os.close();} catch (Exception e) {}
    os = null;
    try {
    	//#MIDP_EXCLUDE_BEGIN
    	hc.disconnect();
    	//#MIDP_EXCLUDE_END
    	/*#MIDP_INCLUDE_BEGIN
	    hc.close();
    	#MIDP_INCLUDE_END*/
    }
    catch (Exception e) {}
    hc = null;
  } 

  //#MIDP_EXCLUDE_BEGIN
  /**
   */
  public String getRemoteHost() throws Exception {
  	throw new Exception("Unsupported operation");
  }
  //#MIDP_EXCLUDE_END
}

