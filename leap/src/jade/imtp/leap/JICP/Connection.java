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


package jade.imtp.leap.JICP;

import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import java.io.*;
//#MIDP_EXCLUDE_BEGIN
import java.net.*;
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
#MIDP_INCLUDE_END*/
import jade.util.Logger;

/**
 * Class declaration
 * @author Steffen Rusitschka - Siemens
 */
public class Connection {

	//#MIDP_EXCLUDE_BEGIN
  private Socket       sc;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
  private StreamConnection sc;
	#MIDP_INCLUDE_END*/
  private InputStream  is;
  private OutputStream os;
  private ByteArrayOutputStream bos;

  protected Connection() {
  }
  
  /**
   * Constructor declaration
   */
  public Connection(TransportAddress ta) throws IOException {
		//#MIDP_EXCLUDE_BEGIN
  	// For some reason the local address or port may be in use
  	while (true) {
  		try {  		
  			sc = new Socket(ta.getHost(), Integer.parseInt(ta.getPort()));
    		break;
  		}
  		catch (BindException be) {
  			// Do nothing and try again
  		}
  	}
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
    String url = "socket://"+ta.getHost()+":"+ta.getPort();
    sc = (StreamConnection) Connector.open(url, Connector.READ_WRITE, false);
		#MIDP_INCLUDE_END*/
  }

  //#MIDP_EXCLUDE_BEGIN
  /**
   * Constructor declaration
   */
  public Connection(Socket s) {
  	sc = s;
  }
  //#MIDP_EXCLUDE_END

  /**
   */
  public OutputStream getOutputStream() throws IOException {
    if (sc == null) {
      throw new IOException("connection not open");
    } 
    if (bos == null) {
  		bos = new ByteArrayOutputStream() {
  			public void flush() throws IOException {
  				if (os == null) {
			    	//#MIDP_EXCLUDE_BEGIN
			      os = sc.getOutputStream();
			    	//#MIDP_EXCLUDE_END
			    	/*#MIDP_INCLUDE_BEGIN
			      os = sc.openOutputStream();
			    	#MIDP_INCLUDE_END*/
  				}
  				os.write(buf, 0, count);
  				os.flush();
  				reset();
  			}
  		};
    } 
    return bos;
  } 

  /**
   */
  public InputStream getInputStream() throws IOException {
    if (sc == null) {
      throw new IOException("connection not open");
    } 
    if (is == null) {
    	//#MIDP_EXCLUDE_BEGIN
      is = sc.getInputStream();
    	//#MIDP_EXCLUDE_END
    	/*#MIDP_INCLUDE_BEGIN
      is = sc.openInputStream();
    	#MIDP_INCLUDE_END*/
    } 
    return is;
  } 

  /**
   */
  public void close() throws IOException {
    if (sc == null) {
      throw new IOException("connection not open");
    } 
    if (is != null) {
      is.close();
      is = null;
    } 
    if (os != null) {
      os.close();
      os = null;
    } 
    sc.close();
    sc = null;
  } 

  //#MIDP_EXCLUDE_BEGIN
  /**
   */
  public String getRemoteHost() throws Exception {
    return sc.getInetAddress().getHostAddress();
  }
  //#MIDP_EXCLUDE_END
  
  /**
   */
  public static String getLocalHost() throws Exception {
  	//#MIDP_EXCLUDE_BEGIN
    String host = InetAddress.getLocalHost().getHostAddress();

    if ("127.0.0.1".equals(host)) {
      // Try with the name
      host = InetAddress.getLocalHost().getHostName();

      if ("localhost".equals(host)) {
        throw new Exception("Can't retrieve local host");
      } 
    } 

    return host;
  	//#MIDP_EXCLUDE_END
    /*#MIDP_INCLUDE_BEGIN
    throw new Exception("Not supported");
    #MIDP_INCLUDE_END*/
  } 
}

